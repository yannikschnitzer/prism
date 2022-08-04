import argparse
import stormpy as sp
import stormpy.examples
import stormpy.examples.files
import stormpy.simulator
import stormpy.pomdp
import random
import os.path
import inspect

from rlshield.noshield import NoShield
from rlshield.recorder import LoggingRecorder, VideoRecorder, StatsRecorder
from rlshield.model_simulator import SimulationExecutor, Tracker

from gridstorm.plotter import Plotter
import gridstorm.models as models

import logging
logger = logging.getLogger(__name__)
#logging.basicConfig(format='%(levelname)s:%(message)s', level=logging.INFO)

def compute_winning_region(model, formula, initial=True):
    options = sp.pomdp.IterativeQualitativeSearchOptions()
    model = sp.pomdp.prepare_pomdp_for_qualitative_search_Double(model, formula)
    solver = sp.pomdp.create_iterative_qualitative_search_solver_Double(model, formula, options)
    logger.info("compute winning region...")
    if initial:
        solver.compute_winning_policy_for_initial_states(100)
    else:
        solver.compute_winning_region(100)
    logger.info("done.")
    return solver.last_winning_region

def construct_otf_shield(model, winning_region):
    return sp.pomdp.BeliefSupportWinningRegionQueryInterfaceDouble(model, winning_region)

def build_pomdp(program, formula):
    options = stormpy.BuilderOptions([formula])
    options.set_build_state_valuations()
    options.set_build_choice_labels()
    options.set_build_all_labels()
    logger.debug("Start building the POMDP")
    return sp.build_sparse_model_with_options(program, options)

experiment_to_grid_model_names = {
    "avoid": models.surveillance,
    "refuel": models.refuel,
    'obstacle': models.obstacle,
    'obstacle_6_factored': models.obstacle_6_factored,
    'obstacleA': models.obstacleA,
    'obstacleB': models.obstacleB,
    'obstacleC': models.obstacleC,
    'obstacleD': models.obstacleD,
    'obstacleE': models.obstacleE,
    'obstacleF': models.obstacleF,
    "intercept": models.intercept,
    'evade': models.evade,
    'rocks': models.rocks,
    'rocks2Base': models.rocks2Base,
    'rocks2A': models.rocks2A,
    'rocks2B': models.rocks2B,
    'rocks2C': models.rocks2C,
    'rocks2D': models.rocks2D,
    'concrete': models.concrete,
    'abstractA': models.abstractA,
    'abstractB': models.abstractB
}

class ManualInput:
    def __init__(self, path, prop, constants):
        self.path = path
        self.properties = [prop]
        self.constants = constants

def main():
    parser = argparse.ArgumentParser(description='The shielded POMDP simulator.')
    model_group = parser.add_mutually_exclusive_group(required=True)
    model_group.add_argument('--grid-model', '-m', help=f'Model from the gridworld-by-storm visualisation set, choose from {str(experiment_to_grid_model_names.keys())}')
    model_group.add_argument('--prism', help="Specify model from prism file")
    parser.add_argument('--prop', help='Specify property string directly')
    parser.add_argument('--constants', '-c', help="Constants to select the instance of the model", default="")
    parser.add_argument('--load-winning-region', '-wr', help="Load a winning region")
    parser.add_argument('--maxsteps', '-s', help="Maximal number of steps", type=int, default=100)
    #parser.add_argument('--maxrendering', '-r', help='Maximal length of a rendering', type=int, default=100)
    parser.add_argument('--max-runs', '-NN', help="Number of runs", type=int, default=10000000)
    parser.add_argument('--nr-finisher-runs', '-N', type=int, default=1)
    parser.add_argument('--video-path', help="Path for the video")
    parser.add_argument('--stats-path', help="Path for recording stats")
    parser.add_argument('--finishers-only', action='store_true')
    parser.add_argument('--seed', help="Seed for randomised movements", default=3)
    parser.add_argument('--title', help="Title for video")
    parser.add_argument('--logfile', help="File to log to", default="rendering.log")
    parser.add_argument('--noshield', help="Simulate without a shield", action='store_true')

    args = parser.parse_args()
    logging.basicConfig(filename=f'{args.logfile}', filemode='w', level=logging.INFO)
    logging.getLogger("matplotlib").setLevel(logging.INFO)
    #logging.getLogger("rlshield.model_simulator").setLevel(logging.DEBUG)

    if args.prism and not args.prop:
        raise RuntimeError("Prism models require setting the property via --prop")
    if args.grid_model and args.prop:
        raise RuntimeError("Properties cannot be set manually when using the gridstorm models")

    if args.video_path is not None and not os.path.isdir(args.video_path):
        raise RuntimeError(f"Video path {args.video_path} not known!")
    if args.stats_path is not None and not os.path.isdir(args.stats_path):
        raise RuntimeError(f"Stats path {args.stats_path} not known!")

    if args.video_path and not args.grid_model:
        raise RuntimeError("Rendering is only supported for gridstorm models!")

    random.seed(args.seed)
    if args.grid_model:
        logger.info("Look up problem definition....")
        model = experiment_to_grid_model_names[args.grid_model]
        model_constants = list(inspect.signature(model).parameters.keys())
        if args.constants is None and len(model_constants) > 0:
            raise RuntimeError("Model constants {} defined, but not given by command line".format(",".join(model_constants)))
        constants = dict(item.split('=') for item in args.constants.split(","))
        input = model(**constants)
    else:
        input = ManualInput(args.prism, args.prop, args.constants)
        constants = dict(item.split('=') for item in args.constants.split(","))


    if args.load_winning_region:
        logger.info("Load winning region...")
        winning_region, preamble = stormpy.pomdp.BeliefSupportWinningRegion.load_from_file(args.load_winning_region)
        for line in preamble.split('\n'):
            if line == "":
                continue
            if line.startswith("model hash: "):
                hash = int(line[12:])
        compute_shield = False
    else:
        winning_region = None
        compute_shield = not args.noshield

    initial = True
    logger.info("Loading problem definition....")
    prism_program = sp.parse_prism_program(input.path)
    prop = sp.parse_properties_for_prism_program(input.properties[0], prism_program)[0]
    prism_program, props = stormpy.preprocess_symbolic_input(prism_program, [prop], input.constants)
    prop = props[0]
    prism_program = prism_program.as_prism_program()
    raw_formula = prop.raw_formula

    logger.info("Construct POMDP representation...")
    model = build_pomdp(prism_program, raw_formula)
    model = sp.pomdp.make_canonic(model)
    logger.info(model)
    #if model.hash() != hash:
    #    raise RuntimeError("Winning Region does not agree with Model")

    path = "/opt/experiments/translation/"
    import os
    if not os.path.exists(path):
        os.makedirs(path)
    pa ="-"
    for para, value in constants.items():
        pa += str(value) + "-"
    n_states= model.state_valuations.get_nr_of_states()
    #n_states = 20
    print("states" , n_states , args.grid_model)
    acts = model.choice_labeling.get_labels()
    #print("Actions" + acts)

    file = open(path + args.grid_model+ pa + "translate.txt", "w")
    #file.write("Actions" + acts)
    for s in range(n_states):
        meaning = model.state_valuations.get_json(s)
        
        meaning += ", state=" + str(s) + ", obs=" + str(model.get_observation(s)) +"\n"
        print(s, meaning)
        file.write(meaning)

    file.close()

if __name__ == "__main__":
    main()

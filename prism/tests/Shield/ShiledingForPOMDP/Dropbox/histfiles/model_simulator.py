import random

import stormpy as sp
import stormpy.examples
import stormpy.examples.files
import stormpy.simulator
import stormpy.pomdp

import logging
logger = logging.getLogger(__name__)


class Tracker:
    """
    Wraps the belief support tracker for our purposes
    """
    def __init__(self, model, shield):
        self._model = model
        self._tracker = stormpy.pomdp.BeliefSupportTrackerDouble(model)
        self._shield = shield

    def track(self, action, observation):
        logger.debug(f"Track action={action}, observation={observation}")
        self._tracker.track(action, observation)

    def monitor(self):
        result = self._shield.query_current_belief(self._tracker.get_current_belief_support())
        logger.debug("Current belief is {}".format("safe" if result else "not safe"))
        return result

    def shielded_actions(self, action_indices):
        safe_action_indices = []
        for a in action_indices:
            if self._shield.query_action(self._tracker.get_current_belief_support(), a):
                safe_action_indices.append(a)
        return safe_action_indices

    def list_support(self):
        return [s for s in self._tracker.get_current_belief_support()]

    def reset(self):
        self._tracker = stormpy.pomdp.BeliefSupportTrackerDouble(self._model)

from enum import Enum
class InteractiveLandmarkStatus(Enum):
    CLEARED = 1,
    POSITIVE = 2,
    NEGATIVE = 3


class SimulationExecutor:
    """
    Base class that wraps and extends the stormpy simulator for shielding.
    """
    def __init__(self, model, shield):
        self._model = model
        self._simulator = stormpy.simulator.create_simulator(model, seed=42)
        self._simulator.set_full_observability(True) # We want to access the full state space for visualisations.
        self._shield = shield

    def simulate(self, recorder, nr_good_runs = 1, total_nr_runs = 5, maxsteps=30):

        #actionNames = ["r1sense", "r2sense", "north", "south", "east", "west", "u1", "u2", "u3"]
        #state_features = ["started=", "done=", "r1qual=", "r1taken=", "r1lastobs=", "r2qual=", "r2taken=", "r2lastobs=","x=", "y="]
        #print("methods")
        #print(dir(self._shield))
        #print(dir(self._shield._shield))
        #print(help(self._shield._shield.query_action))
    
        result = []
        good_runs = 0
        #TODO what if we are not in a safe state.
        for m in range(total_nr_runs):
            print('run================='+str(m))
            finished = False
            state = self._simulator.restart()
            logger.info("Start new episode.")
            self._shield.reset()
            recorder.start_path()
            recorder.record_state(state)
            recorder.record_belief(self._shield.list_support())
            n_states= self._model.state_valuations.get_nr_of_states()
            #file = open("/opt/experiments/translate.txt", "w")
            #for s in range(n_states):
            #    meaning = self._model.state_valuations.get_json(s)
            #    meaning += ", state=" + str(s) + ", obs=" + str(self._model.get_observation(s)) +"\n"
            # #   file.write(meaning)
            #file.close()


            for s in range(n_states):
                labels = self._model.state_valuations.get_string(0)
                labels = self._model.state_valuations.get_json(s).replace("\"","").replace(":","=")
                meaning = ""
                for f in state_features:
                    value = labels[labels.find(f) + len(f)]
                    if value == 'f':
                        meaning += f + 'false,'
                    elif value == 't':
                        meaning += f + 'true,'
                    else:
                        meaning += f + value + ","
                meaning = "(" + meaning[:-1] + ")"
                string = "StompyMeaning2State.put(\"meaning\", stompyState);"
                string = string.replace("meaning", meaning)
                string = string.replace("stompyState", str(s))
                #print(string)
                
                #labels = labels[1:-1] + "\ts=" + str(s)
                #labels += "\t obs=" + str(self._model.get_observation(s))
                #print(labels)

           # for s in range(n_states):
               # for a in range(5):
                   # next, _  = self._simulator.step(a)
                   # print("s", s, "a", a, "next", next)

            state = self._simulator.restart()
            #print("start state", state, "start belief support", self._shield.list_support())

            for n in range(maxsteps):

                actions = self._simulator.available_actions()
                
                safe_actions = self._shield.shielded_actions(range(len(actions)))
                
                info = ''
                step_info = " step=" + str(n) + " "
               
                ava_info = "\tavialble actions=["
                for i in actions:
                    ava_info += actionNames[i] + " "
                ava_info = ava_info[:-1] + "]"
                
                safe_info ="\tsafe actions=["
                for i in safe_actions:
                    safe_info += actionNames[i] + " "
                safe_info = safe_info[:-1] + ']'
                
                shield_info = "\tshielded actions=["
                for i in actions:
                    if i not in safe_actions:
                        shield_info += actionNames[i] + " "
                shield_info = shield_info[0:-1] + ']'

                logger.debug(f"Number of actions: {actions}. Safe action indices: {safe_actions}")
                if len(safe_actions) == 0:
                    select_action = random.randint(0, len(actions) - 1)
                    action = actions[select_action]
                else:
                    select_action = random.randint(0, len(safe_actions) - 1)
                    action = safe_actions[select_action]

                select_info = " selected=" +  actionNames[action] + " "

                logger.debug(f"Select action: {action}")
                state, _ = self._simulator.step(action)
                self._shield.track(action, self._model.get_observation(state))
                assert state in self._shield.list_support()
                logger.debug(f"Now in state {state}. Belief: {self._shield.list_support()}. Safe: {self._shield.monitor()}")

                state_info = "\tstate=" + str(state)
                support_info = "\tbelief support=" + str(self._shield.list_support())
                is_safe = "\tsafe=" + str(self._shield.monitor())
                obs_info = '\tobs' + str(self._model.get_observation(state))
                info += step_info + select_info + state_info + support_info + is_safe + ava_info + safe_info
                print( step_info + select_info + shield_info + safe_info + state_info + support_info + obs_info + is_safe)
                #print( step_info + select_info +  shield_info + safe_info + ava_info)
                print("get cur support", self._shield._tracker.get_current_belief_support())
                print("get curp", self._shield.list_support())
                #print( step_info + state_info + support_info + is_safe)
                recorder.record_available_actions(actions)
                recorder.record_allowed_actions(safe_actions)
                recorder.record_selected_action(action)
                recorder.record_state(state)
                recorder.record_belief(self._shield.list_support())

                if self._simulator.is_done():
                    logger.info(f"Done after {n} steps!")
                    finished = True
                    good_runs += 1
                    break
            actions = self._simulator.available_actions()
            safe_actions = self._shield.shielded_actions(range(len(actions)))

            recorder.record_available_actions(actions)
            recorder.record_allowed_actions(safe_actions)

            recorder.end_path(finished)
            result.append(self._simulator.is_done())
            if good_runs == nr_good_runs:
                break
        return result

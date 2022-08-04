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


class SimulationExecutor:
    """
    Base class that wraps and extends the stormpy simulator for shielding.
    """
    def __init__(self, model, shield):
        self._model = model
        self._simulator = stormpy.simulator.create_simulator(model, seed=42)
        self._simulator.set_full_observability(True) # We want to access the full state space for visualisations.
        self._shield = shield

    def simulate(self, recorder, nr_good_runs = 1, total_nr_runs = 1, maxsteps=30):
        def get_action_name(state, action):
            if type(state) == type((0, [])):
                state = state[0]
            if action is None:
                return None
            return list(self._model.choice_labeling.get_labels_of_choice(self._model.get_choice_index(state, action)))

        def get_action_name_list(state, action_index_list):
            action_name_list = []
            for action_index in action_index_list:
                action_name_list.append(get_action_name(state, action_index))
            return action_name_list


        result = []
        good_runs = 0
        #TODO what if we are not in a safe state.
        print("SSSSSSSSSSSS")
        actt = self._model.choice_labeling.get_labels()
        print("actt", actt, type(actt))
        actt = list(actt)
        print("sssssssssss", actt)

        for m in range(total_nr_runs):
            finished = False
            state = self._simulator.restart()
            logger.info("Start new episode.")
            self._shield.reset()
            recorder.start_path()
            recorder.record_state(state)
            recorder.record_belief(self._shield.list_support())
            print(state, end = '\t')
            print(self._shield.list_support(), end = '\t')
            for n in range(maxsteps):
                actions = self._simulator.available_actions()
                safe_actions = self._shield.shielded_actions(range(len(actions)))
                logger.debug(f"Number of actions: {actions}. Safe action indices: {safe_actions}")
                if len(safe_actions) == 0:
                    select_action = random.randint(0, len(actions) - 1)
                    action = actions[select_action]
                else:
                    select_action = random.randint(0, len(safe_actions) - 1)
                    action = safe_actions[select_action]
                logger.debug(f"Select action: {action}")
                
                #print("available actions", actions, type(actions), get_action_name_list(state, actions))
                #print("safe_actions", safe_actions, type(safe_actions), get_action_name_list(state, safe_actions))
                #print("action", action, type(action), get_action_name(state, action),"\n")
                
                print(get_action_name_list(state, actions), end = '\t')
                print(get_action_name_list(state, safe_actions), end = '\t')
                print( get_action_name(state, action), end = '\t')
                print(9)
                state, _ = self._simulator.step(action)
                self._shield.track(action, self._model.get_observation(state))
                assert state in self._shield.list_support()
                logger.debug(f"Now in state {state}. Belief: {self._shield.list_support()}. Safe: {self._shield.monitor()}")


                print(state, end = '\t')
                print(self._shield.list_support(), end = '\t')
                print(self._shield.monitor(), end = '\t')
		#print("state", state, type(state), self._model.state_valuations.get_json(state))
                #print("self._shield.list_support()", self._shield.list_support(), type(self._shield.list_support()))

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
            print("actions", actions, type(actions))
            print("safe_actions", safe_actions)
            #print(get_action_name_list(state, actions), end = '\t')
            print("a",get_action_name_list(state, safe_actions), end = '\t')
            recorder.record_available_actions(actions)
            recorder.record_allowed_actions(safe_actions)

            recorder.end_path(finished)
            result.append(self._simulator.is_done())
            if good_runs == nr_good_runs:
                break
        return result


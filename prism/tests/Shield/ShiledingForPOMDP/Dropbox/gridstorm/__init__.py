import os
from gridstorm.annotations import ProgramAnnotation, Direction

class Model:
    def __init__(self, path, annotations, properties, constants="", ego_icon=None):
        self._path = path
        self._anonotations = annotations
        self._properties = properties
        self._ego_icon = ego_icon
        self._constants = constants

    @property
    def path(self):
        return self._path

    @property
    def annotations(self):
        return self._anonotations

    @property
    def properties(self):
        return self._properties

    @property
    def ego_icon(self):
        return self._ego_icon

    @property
    def constants(self):
        return self._constants

class Icon:
    def __init__(self, path, zoom=0.2):
        self.path = path
        self._zoom = zoom

    @property
    def zoom(self):
        return self._zoom


def _example_path(extension):
    return os.path.join(os.path.dirname(__file__),'files',extension)


def _icon_path(suffix):
    return os.path.join(os.path.dirname(__file__), 'icons', suffix)

_grid_obstacle_dict = dict({'ego-xvar-module' : 'robot',
                      'ego-xvar-name' : 'ax',
                      'ego-yvar-module' : 'robot',
                      'ego-yvar-name': 'ay',
                      'xmax-constant': 'axMAX',
                      'ymax-constant': 'ayMAX',
                      'target-label': 'goal',
                      'traps-label': 'traps'
                      })
def obstacle(N):
    return Model(_example_path("obstacle.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")

_drone_dict = dict({'ego-xvar-module' : 'drone',
                      'ego-xvar-name' : 'dx',
                      'ego-yvar-module' : 'drone',
                      'ego-yvar-name': 'dy',
                      'adv-xvar-module': 'agent',
                        'adv-xvar-name': 'ax',
                        'adv-yvar-module': 'agent',
                        'adv-yvar-name': 'ay',
                        'xmax-constant': 'xMAX',
                      'ymax-constant': 'yMAX',
                      'target-label': 'goal',
                       'ego-radius-constant' : "RADIUS",
                       'scan-action': 'scan',
                        'adv-area': ['a'],
                      'traps-label': None
                      })

def evade(N,RADIUS):
    return Model(_example_path("evade.nm"), ProgramAnnotation(_drone_dict),
                 ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N},RADIUS={RADIUS}")

_intercept_dict = dict({'ego-xvar-module' : 'drone',
                      'ego-xvar-name' : 'dx',
                      'ego-yvar-module' : 'drone',
                      'ego-yvar-name': 'dy',
                      'adv-xvar-module': 'agent',
                        'adv-xvar-name': 'ax',
                        'adv-yvar-module': 'agent',
                        'adv-yvar-name': 'ay',
                        'xmax-constant': 'dxMAX',
                      'ymax-constant': 'dyMAX',
                      'traps-label': None,
                        'ego-radius-constant': "RADIUS",
                        'camera': ['CAMERA'],
                        'adv-goals-label': 'exits'
                      })
def intercept(N,RADIUS):
    return Model(_example_path("intercept.nm"), ProgramAnnotation(_intercept_dict),
                 ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N},RADIUS={RADIUS}")


_surveillance_dict = dict({'ego-xvar-module' : 'drone',
                      'ego-xvar-name' : 'dx',
                      'ego-yvar-module' : 'drone',
                      'ego-yvar-name': 'dy',
                      'adv-xvar-module': ['agent','agent2'],
                       'adv-xvar-name': ['ax','ax2'],
                       'adv-yvar-module': ['agent','agent2'],
                        'adv-yvar-name': ['ay','ay2'],
                       'adv-dirvar-module': ['agent', 'agent2'],
                           'adv-dirvar-name': ['dir', 'dir2'],
                           'xmax-constant': 'xMAX',
                      'ymax-constant': 'yMAX',
                      'target-label': 'goal',
                      'traps-label': None,
                      'adv-dirvalue-mapping': {1: Direction.WEST, 0: Direction.EAST},
                       'adv-radius-constant' : "ARADIUS",
                       'ego-radius-constant' : "RADIUS"
                      })
def surveillance(N,RADIUS=2):
    return Model(_example_path("avoid.nm"), ProgramAnnotation(_surveillance_dict), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N},RADIUS={RADIUS}")

_grid_refuel = dict({'ego-xvar-module' : 'rover',
                      'ego-xvar-name' : 'ax',
                      'ego-yvar-module' : 'rover',
                      'ego-yvar-name': 'ay',
                      'xmax-constant': 'axMAX',
                      'ymax-constant': 'ayMAX',
                      'target-label': 'goal',
                      'traps-label': 'traps',
                      'landmarks': 'stationvisit',
                      'resource-module': 'tank',
                      'resource-variable': 'fuel',
                      'resource-name': 'fuel',
                       'resource-maximum-constant': 'fuelCAP'
                      })

def refuel(N, ENERGY):
    return Model(_example_path("refuel.nm"), ProgramAnnotation(_grid_refuel), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N},ENERGY={ENERGY}")

_grid_rocks = dict({'ego-xvar-module' : 'robot',
                      'ego-xvar-name' : 'x',
                      'ego-yvar-module' : 'robot',
                      'ego-yvar-name': 'y',
                      'xmax-constant': 'xMAX',
                      'ymax-constant': 'yMAX',
                      'target-label': 'goal',
                      'traps-label' : None,
                      'interactive-landmarks-x': ['r1x', 'r2x'],
                      'interactive-landmarks-y': ['r1y', 'r2y'],
                      'il-statusvar-module': ['rock1', 'rock2'],
                      'il-statusvar-name': ['r1qual', 'r2qual'],
                      'il-clearancevar-module': ['rock1', 'rock2'],
                      'il-clearancevar-name': ['r1taken', 'r2taken'],
                       'goal-action' : True
                    })

def rocks(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")


_grid_obstacle_dict = dict({'ego-xvar-module' : 'robot',
                      'ego-xvar-name' : 'ax',
                      'ego-yvar-module' : 'robot',
                      'ego-yvar-name': 'ay',
                      'xmax-constant': 'axMAX',
                      'ymax-constant': 'ayMAX',
                      'target-label': 'goal',
                      'traps-label': 'traps'
                      })
def concrete(N):
    return Model(_example_path("concrete.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")

def abstractA(N):
    return Model(_example_path("abstractA.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def abstractB(N):
    return Model(_example_path("abstractB.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")


def obstacleA(N):
    return Model(_example_path("obstacleA.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")

def obstacleB(N):
    return Model(_example_path("obstacleB.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacleC(N):
    return Model(_example_path("obstacleC.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacleD(N):
    return Model(_example_path("obstacleD.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacleE(N):
    return Model(_example_path("obstacleE.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacleF(N):
    return Model(_example_path("obstacleF.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")

def obstacle_6_factored(N):
    return Model(_example_path("obstacle_6_factored.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacle_6_centralized(N):
    return Model(_example_path("obstacle_6_centralized.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")


def obstacle_50_factored(N):
    return Model(_example_path("obstacle_50_factored.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacle_50_centralized(N):
    return Model(_example_path("obstacle_50_centralized.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")


def obstacle_100_factored(N):
    return Model(_example_path("obstacle_100_factored.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacle_100_centralized(N):
    return Model(_example_path("obstacle_100_centralized.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")


def obstacle_1000_factored(N):
    return Model(_example_path("obstacle_1000_factored.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")
def obstacle_1000_centralized(N):
    return Model(_example_path("obstacle_1000_centralized.nm"), ProgramAnnotation(_grid_obstacle_dict),
                 ["Pmax=? [ \"notbad\" U \"goal\"]"], constants=f"N={N}")


def rocks2Base(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2Base.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks2A(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2A.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks2B(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2B.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks2C(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2C.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks2D(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2D.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks2D(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks2D.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks_N8R4_0_0_3_3(N, K=4):
    K = int(K)
    if K == 4:
        return Model(_example_path("rocks_N8R4_0_0_3_3.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")  
def rocks_N8R4_0_4_3_7(N, K=4):
    K = int(K)
    if K == 4:
        return Model(_example_path("rocks_N8R4_0_4_3_7.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks") 
def rocks_N8R4_4_0_3_7(N, K=4):
    K = int(K)
    if K == 4:
        return Model(_example_path("rocks_N8R4_4_0_3_7.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")  
def rocks_N8R4_4_4_7_7(N, K=4):
    K = int(K)
    if K == 4:
        return Model(_example_path("rocks_N8R4_4_4_7_7.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks") 
def rocks_N8R4_exit(N, K=4):
    K = int(K)
    if K == 4:
        return Model(_example_path("rocks_N8R4_exit.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks") 

def rocks_N8R2_factored-4-0-0-3-3(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks_N8R2_factored-4-0-0-3-3.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks_N8R2_factored-4-0-4-3-7(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks_N8R2_factored-4-0-4-3-7.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")


def rocks_N8R2_factored-4-4-0-7-3(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks_N8R2_factored-4-4-0-7-3.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks_N8R2_factored-4-4-4-7-7(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks_N8R2_factored-4-4-4-7-7.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")

def rocks_N8R2_centralized(N, K=2):
    K = int(K)
    if K == 2:
        return Model(_example_path("rocks_N8R2_centralized.nm"), ProgramAnnotation(_grid_rocks), ["Pmax=? [\"notbad\" U \"goal\"]"], constants=f"N={N}")
    else:
        raise RuntimeError("Rocks is only available with 2 or 3 rocks")
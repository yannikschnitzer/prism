import os, sys
import math
import argparse, random
import numpy as np


def init_argparse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        usage="%(prog)s [R] [C] -c [cost of obstacle] -p [probability of failure]",
        description="Generate gridmap.prism files for cliffs example"
    )
    parser.add_argument(
        "-v", "--version", action="version",
        version = f"{parser.prog} version 1.0.0"
    )
    parser.add_argument('R', metavar='R', type=int, default=10, help='rows of the gridmap')
    parser.add_argument('C', metavar='C', type=int, default=10, help='columns of the gridmap')
    parser.add_argument('-p', "--prob", metavar='p', type=float, action='store', default=0.25, help='probability for failed transition')
    parser.add_argument('-c', "--cost", metavar='c', type=float, action='store', default=5, help='cost for obstacle')

    return parser

def compute_obstacles():

    obstacles =[]
    r=[3,3,3,3,3,3,3,3,3,3]
    c=[1,2,3,4,5,6,7,8,9,10]

    #print(r,c)
    return r,c

def write_gridmap(R, C, p=0.25, goal=None, obs_cost=30):

    if goal is None:
        goal = [R, C]

    print(f"Printing with goal:{goal}..")
    r,c = compute_obstacles()

    with open(f'cliffs_{R}_{C}.prism', 'w') as f:
        f.write('mdp\n')

        f.write(f'const int R={R}; \n')
        f.write(f'const int C={C}; \n')
        f.write(f'const double p={p}; // with probability p, a move to a neighboring state state occurs due to seeing and control noise\n')

        f.write(f'\nmodule robot\n')

        f.write(f'\t//define robot position\n')
        f.write(f'\tr:[1..R] init 1; //grid row\n')
        f.write(f'\tc:[1..C] init 1; //grid column\n')

        f.write(f'\n\t// go to the terminal state when hitting an obstacle')

        f.write(f'\n\t[obstacle] ')
        str =''
        for i in range(len(r)):
            if i == (len(r) -1):
                f.write(f'(r={r[i]+1} & c={c[i]+1})')
                str+=f'(r={r[i]+1} & c={c[i]+1})'
            else:
                f.write(f'(r={r[i]+1} & c={c[i]+1}) | ')
                str+=f'(r={r[i]+1} & c={c[i]+1}) | '
        f.write(f'-> (r\'={goal[0]}) & (c\'={goal[1]});\n')

        f.write(f'\n\t// transitions\n')
        f.write(f'\t[east] (c<N | c=N) & !(')
        f.write(str); f.write(f')-> p/3:(r\'=min(r+1, N))+ p/3: (c\'=max(c-1, 1))+ p/3: (r\'=max(r-1, 1)) + (1-p): (c\'=min(c+1, N));\n')

        f.write(f'\t[south] (r<N | r=N) & !(')
        f.write(str); f.write(f')-> (1-p):(r\'=min(r+1, N)) + p/3: (c\'=min(c+1, N)) + p/3:(r\'=min(r+1, N))+ p/3: (c\'=max(c-1, 1));\n')

        f.write(f'\t[west]  (c>1 | c=1) & !(')
        f.write(str); f.write(f')-> p/3:(r\'=max(r-1, 1))+ p/3:(r\'=min(r+1, N))+ p/3: (c\'=min(c+1, N)) + (1-p): (c\'=max(c-1, 1));\n')

        f.write(f'\t[north] (r=1 | r>1) & !(')
        f.write(str); f.write(f')-> (1-p):(r\'=max(r-1, 1)) + p/3: (c\'=max(c-1, 1)) + p/3: (c\'=min(c+1, N)) + p/3:(r\'=min(r+1, N));\n')

        f.write(f'\n\t// terminal state self-loop to avoid deadlock\n\t[] r={goal[0]} & c={goal[1]} -> true;\n')

        f.write(f'endmodule\n')
        f.write(f'\nlabel "goal" = r={goal[0]} & c={goal[1]};\n')
        f.write(f'\nlabel "obs" = ')
        f.write(str); f.write(';\n')

        f.write(f'\nrewards\n')

        f.write(f'\t[east] true : 1;\n')
        f.write(f'\t[south] true : 1;\n')
        f.write(f'\t[west] true : 1;\n')
        f.write(f'\t[north] true : 1;\n')
        f.write(f'\t[obstacle] true : {obs_cost};\n')
        f.write(f'endrewards\n')

        f.close()

    with open(f'cliffs_{R}_{C}.props', 'w') as f:
        f.write(f'Rmin=?[F "goal"] // maybe max of Rmin/Pmax for best policy\n')
        f.write(f'R(cvar)min=? [ F "goal" ];\n')
        f.close()

if __name__ == "__main__":
    parser = init_argparse()
    args = parser.parse_args()

    goal = [args.R, args.C]

    print(vars(args))
    write_gridmap(args.R, args.C, args.prob, goal=goal, obs_cost=args.cost)


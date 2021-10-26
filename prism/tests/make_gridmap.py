import os, sys

import argparse, random


def init_argparse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        usage="%(prog)s [N] [p] [number of obstacles]",
        description="Generate gridmap.prism files with randomly positioned obstacles"
    )
    parser.add_argument(
        "-v", "--version", action="version",
        version = f"{parser.prog} version 1.0.0"
    )
    parser.add_argument('N', metavar='N', type=int, default=10, help='size of the gridmap')
    parser.add_argument('-o', "--obs", metavar='obs', type=int, action='store', default=5, help='number of obstacles must be less than N*N')
    parser.add_argument('-p', "--prob", metavar='p', type=float, action='store', default=0.1, help='probability for transition')

    return parser

def compute_obstacles(N, obs):

    obstacles =[]

    while len(obstacles) < obs:
        temp = random.randint(1,(N*N)-2)
        if  (temp not in obstacles) :
            obstacles.append(temp)

    r=[];c=[]
    for o in obstacles:
        r.append(int(o/N))
        c.append(o%N)
#         print(f'o:{o}, i:{int(o/N)}, j:{o%N}')
    return r,c

def write_gridmap(N, obs, p):

    r,c = compute_obstacles(N, obs)

    with open(f'gridmap/gridmap_{N}_{obs}.prism', 'w') as f:
        f.write('mdp\n')

        f.write(f'const int N={N}; // N*N grid map\n')
        f.write(f'const double p={p}; // with probability p, a move to a neighboring state state occurs due to seeing and control noise\n')

        f.write(f'\nmodule robot\n')

        f.write(f'\t//define robot position\n')
        f.write(f'\tr:[1..N] init 1; //grid row\n')
        f.write(f'\tc:[1..N] init 1; //grid column\n')

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
        f.write(f'-> true;\n')

        f.write(f'\n\t// transitions\n')
        f.write(f'\t[east] (c<N | c=N) & !(')
        f.write(str); f.write(f')-> p:(r\'=min(r+1, N)) + (1-p): (c\'=min(c+1, N));\n')

        f.write(f'\t[south] (r<N | r=N) & !(')
        f.write(str); f.write(f')-> (1-p):(r\'=min(r+1, N)) + p: (c\'=min(c+1, N));\n')

        f.write(f'\t[west]  (c<1 | c=1) & !(')
        f.write(str); f.write(f')-> p:(r\'=max(r-1, 1)) + (1-p): (c\'=max(c-1, 1));\n')

        f.write(f'\t[north] (r=1 | r>1) & !(')
        f.write(str); f.write(f')-> (1-p):(r\'=max(r-1, 1)) + p: (c\'=max(c-1, 1));\n')

        f.write(f'\n\t// terminal state self-loop to avoid deadlock\n\t[] r=N & c=N -> true;\n')

        f.write(f'endmodule\n')
        f.write(f'\nlabel "goal" = r=N & c=N;\n')
        f.write(f'\nrewards\n')

        f.write(f'\t[east] true : 1;\n')
        f.write(f'\t[south] true : 1;\n')
        f.write(f'\t[west] true : 1;\n')
        f.write(f'\t[north] true : 1;\n')
        f.write(f'\t[obstacle] true : 30;\n')
        f.write(f'endrewards\n')

        f.close()

    with open(f'gridmap/gridmap_{N}_{obs}.props', 'w') as f:
        f.write(f'Rmin=?[F "goal"] // maybe max of Rmin/Pmax for best policy\n')
        f.write(f'R(cvar)min=? [ F "goal" ];\n')
        f.close()

if __name__ == "__main__":
    parser = init_argparse()
    args = parser.parse_args()

    if args.obs >= (args.N*args.N):
        print(f' Too many obstacles {args.obs} for an {args.N}*{args.N} grid')
        sys.exit()

    print(vars(args))
    write_gridmap(args.N, args.obs, args.prob)


import os, sys
import math
import argparse, random, re
import numpy as np
from copy import deepcopy

model_name = "gridmap_10_10_mod"
prism_file = "gridmap/"+model_name+".prism"

def init_argparse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        usage="%(prog)s [N] [p] [number of obstacles]",
        description="Generate gridmap.prism files based on a prism file pattern"
    )
    parser.add_argument(
        "-v", "--version", action="version",
        version = f"{parser.prog} version 1.0.0"
    )
    parser.add_argument('N', metavar='N', type=int, default=10, help='size of the gridmap')
    parser.add_argument('-f', "--file", metavar='f', action='store', default=model_name, help='Prism file to use for pattern')
    parser.add_argument('-p', "--prob", metavar='p', type=float, action='store', default=0.1, help='probability for failed transition')
    parser.add_argument('-c', "--cost", metavar='c', type=float, action='store', default=30, help='cost for obstacle')
    parser.add_argument('-o', "--obs", metavar='obs', type=int, action='store', default=5, help='number of obstacles must be less than N*N')
    parser.add_argument('-x', "--combine", action='store_true', help='Combine with more obstacles')
    parser.add_argument('-r', "--random", action='store_true', help='Randomly delete some pattern obstacles')

    return parser

def parse_prism_file(filename):
    rx = re.compile(r'^formula wall=\s*(\(r=(\d+)\s*\&\s*c=(\d+)\)\s*\|?\s*)+', re.M)
    rx_inner = re.compile(r'\(r=(\d+)\s*\&\s*c=(\d+)\)\s*', re.M)
    rx_size= re.compile(r'^const int N\s*=\s*(\d+)', re.M)
    rx_goal = re.compile(r"^label\s*[\"|\s]*goal[\"|\s]*=\s*r=(\d+)\s*\&\s*c=(\d+)\s*;", re.M)
    rx_row = re.compile(r"^\s*r:[\s|.|\d|N|\[|\]]*init\s*(\d+)\s*;", re.M)
    rx_col = re.compile(r"^\s*c:[\s|.|\d|N|\[|\]]*init\s*(\d+)\s*;", re.M)
    obs_locations = []
    map_size=None
    map_goal=None
    map_start=None
    with open(filename) as f:
        raw_data = f.read()
        wall_formula = re.search(rx, raw_data)[0]
        map_size=int(re.search(rx_size, raw_data).group(1))
        goal_search=re.search(rx_goal, raw_data)
        map_goal = tuple([int(goal_search.group(1))-1, int(goal_search.group(2))-1])
        map_start = tuple([int(re.search(rx_row, raw_data).group(1))-1, int(re.search(rx_col, raw_data).group(1))-1])      
        # print(f'goal{map_goal.group(0)}: r={map_goal.group(1)}, c={map_goal.group(2)}')
        # print(wall_formula)
        for m in re.finditer(rx_inner, wall_formula):
            # find row/cols and adjust to 0 based index
            # print(f'row{m.group(0)}, r={m.group(1)},c={m.group(2)} -- converted: r={int(m.group(1))-1}, c={int(m.group(2))-1}')
            obs_locations.append(deepcopy(tuple([int(m.group(1))-1, int(m.group(2))-1])))
    return map_size, map_goal, map_start, obs_locations

def compute_obstacles(N, goal, filename, obs, combine=False, random_keep=False):

    r=[];c=[]
    obstacles =[]
    random_p = 0.25
    obs_total= 0;
    pattern_size, _, _, obs_locations = parse_prism_file(filename)
    ratio  = math.floor(N/pattern_size)
    # print(obs_locations)
    for o in (obs_locations):
        for i in range (ratio):
            for j in range(ratio):
                if random_keep:
                    p = random.random()
                    if p < random_p:
                        print(f'random: p:{p}, random_p:{random_p}')
                        continue
                r.append(o[0]*ratio+i)
                c.append(o[1]*ratio+j)
                obs_total += 1
                obstacles.append((o[0]*ratio+i-1)*(N)+ (o[1]*ratio+j-1))

    zones = [25, 22, 18, 15, 9, 3, 2, 2, 2, 2]
    ranges = len(zones)
    prev =0;
    obs_set = 0

    if combine:
        for i, z in enumerate(zones):
            print(i, ((i+1)*(N/ranges))-1,obs_set, math.ceil(z/100*obs))
            while obs_set < prev+ math.ceil(z/100*obs)+1:
                tempr = random.randint(math.floor(N/ranges),math.floor((N-N/ranges)))
                tempc = random.randint(0,math.floor(((i+1)*(N/ranges))-1))
                temp = (tempr-1)*(N)+ (tempc-1)
                if  (temp not in obstacles):
                    r.append(tempr)
                    c.append(tempc)
                    obstacles.append(temp)
                    obs_set+=1
            prev += math.ceil(z/100*obs)+1
    #print(r,c)
    return r,c, obs_total+obs_set

def write_gridmap(N, p, filename, args, goal=None, obs_cost=30):

    if goal is None:
        goal = [N, 1]

    
    r,c, obs_total = compute_obstacles(N, goal,  "gridmap/"+filename+".prism", args.obs, args.combine, args.random)
    print(f"Printing with goal:{goal}.. with obstacles = {obs_total}")
    str_obs =''
    for i in range(len(r)):
        if i == (len(r) -1):
            str_obs+=f'(r={r[i]+1} & c={c[i]+1})'
        else:
            str_obs+=f'(r={r[i]+1} & c={c[i]+1}) | '

    with open(f'gridmap/gridmap_{N}_{obs_total}.prism', 'w') as f:
        f.write('mdp\n')

        f.write(f'const int N={N}; // N*N grid map\n')
        f.write(f'const double p={p}; // with probability p, a move to a neighboring state state occurs due to seeing and control noise\n')
        f.write(f'formula wall= ')
        f.write(str_obs)
        f.write(';\n')

        f.write(f'\nmodule robot\n')

        f.write(f'\t//define robot position\n')
        f.write(f'\tr:[1..N] init 1; //grid row\n')
        f.write(f'\tc:[1..N] init 1; //grid column\n')


        f.write(f'\n\t// transitions\n')
        f.write(f'\t[east] (c<N | c=N) -> p/3:(r\'=min(r+1, N))+ p/3: (c\'=max(c-1, 1))+ p/3: (r\'=max(r-1, 1)) + (1-p): (c\'=min(c+1, N));\n')

        f.write(f'\t[south] (r<N | r=N)-> (1-p):(r\'=min(r+1, N)) + p/3: (c\'=min(c+1, N)) + p/3:(r\'=max(r-1, 1))+ p/3: (c\'=max(c-1, 1));\n')

        f.write(f'\t[west]  (c>1 | c=1)-> p/3:(r\'=max(r-1, 1))+ p/3:(r\'=min(r+1, N))+ p/3: (c\'=min(c+1, N)) + (1-p): (c\'=max(c-1, 1));\n')

        f.write(f'\t[north] (r=1 | r>1)-> (1-p):(r\'=max(r-1, 1)) + p/3: (c\'=max(c-1, 1)) + p/3: (c\'=min(c+1, N)) + p/3:(r\'=min(r+1, N));\n')

        f.write(f'\n\t// terminal state self-loop to avoid deadlock\n\t[] r={goal[0]} & c={goal[1]} -> true;\n')

        f.write(f'endmodule\n')
        f.write(f'\nlabel "goal" = r={goal[0]} & c={goal[1]};\n')
        f.write(f'\nlabel "obs" = ')
        f.write(str_obs); f.write(';\n')

        f.write(f'\nrewards\n')

        f.write(f'\t[east] !wall : 1;\n')
        f.write(f'\t[south] !wall : 1;\n')
        f.write(f'\t[west] !wall : 1;\n')
        f.write(f'\t[north] !wall : 1;\n\n')

        f.write(f'\t[east] wall : {obs_cost};\n')
        f.write(f'\t[south] wall : {obs_cost};\n')
        f.write(f'\t[west] wall : {obs_cost};\n')
        f.write(f'\t[north] wall : {obs_cost};\n')

        f.write(f'endrewards\n')

        f.close()

    with open(f'gridmap/gridmap_{N}_{obs_total}.props', 'w') as f:
        f.write(f'Rmin=?[F "goal"] // maybe max of Rmin/Pmax for best policy\n')
        f.write(f'R(cvar)min=? [ F "goal" ];\n')
        f.write(f'R(dist)min=? [ F "goal" ];')
        f.close()

if __name__ == "__main__":
    parser = init_argparse()
    args = parser.parse_args()

    goal = [args.N, 1]

    if args.N <0:
        print(f'Error N cannot be < 0: {args.N}.')
        sys.exit()

    print(vars(args))
    write_gridmap(args.N, args.prob, args.file, args, goal=goal, obs_cost=args.cost)


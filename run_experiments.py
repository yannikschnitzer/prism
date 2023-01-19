#!/usr/bin/env python
# coding: utf-8

import os, sys
import csv
import subprocess
import argparse, random
import numpy as np


# #### Functions for saving output files
def check_save_location(exp_folder, exp_name, prefix, debug):

    if not os.path.isdir(exp_folder):
        command = 'mkdir '+ exp_folder
        print("Making experiment folder")
        if debug:
            print(command)
        os.system(command)

    if not os.path.isdir(exp_folder+exp_name):
        command = 'mkdir '+ exp_folder+exp_name
        print("Making experiment folder")
        if debug:
            print(command)
        os.system(command)

# Create Log target
def log_target(exp_folder, exp_name, alg, rep, apdx, debug):
    if 'vi' in alg:
        return
    target = exp_folder+exp_name+'/'+exp_name+'_log_'+alg+'_'+rep+apdx+'.log'
    if debug: 
        print(target)
    return target
# Copy Log file
def copy_log_files(cmd_base, exp_folder, exp_name, alg, rep, apdx, prefix, debug):
    if 'vi' in alg:
        return
    source =  prefix+'log_'+alg+'.log'
    target = exp_folder+exp_name+'/'+exp_name+'_log_'+alg+'_'+rep+apdx+'.log'
    command = cmd_base+source+' '+target
    if debug:
        print(command)
    os.system(command)
            
# Copy VI files
def copy_vi_files(cmd_base, exp_folder, exp_name, alg, rep, apdx, prefix, debug):
    if 'vi' in alg:
        return
    source =  prefix+'distr_'+alg+'_'+rep+'.csv'
    target = exp_folder+exp_name+'/'+exp_name+'_distr_'+alg+'_'+rep+apdx+'.csv'
    command = cmd_base+source+' '+target
    if debug:
        print(command)
    os.system(command)

def copy_trace_files(cmd_base, exp_folder, exp_name, alg, rep, apdx, prefix,debug):
    # Copy trace files
    trace_path = prefix+ 'tests/traces/'
    source = trace_path+'distr_'+alg+'_'+rep.upper()+'_trace.csv'
    target = exp_folder+exp_name+'/'+exp_name+'_distr_'+alg+'_'+rep+apdx+'_trace.csv'
    command = cmd_base+source+' '+target
    if debug:
        print(command)
    os.system(command)

def copy_dtmc_files(cmd_base, exp_folder, exp_name, alg, rep, apdx, prefix, debug):
    # Copy DTMC files
    source = prefix+'distr_dtmc_'+alg+'.csv'
    target = exp_folder+exp_name+'/'+exp_name+'_distr_dtmc_'+alg+'_'+rep+apdx+'.csv'
    command = cmd_base+source+' '+target
    if debug:
        print(command)
    os.system(command)

def create_params(atoms, v_bounds, error, epsilon, b_atoms, b_bounds, alpha):
    # make rows
    row_vi = [atoms]+[ v for v in v_bounds]+[error, epsilon]+[alpha]
    row_b = [b_atoms]+[ b for b in b_bounds]
    print(f'Params: vi:{row_vi}, b:{row_b}')
    
    # saving to params_vi.csv
    with open(prefix+'tests/params_vi.csv', 'w') as f:
        # using csv.writer method from CSV package
        write = csv.writer(f)

        write.writerow(header_vi)
        write.writerow(row_vi)
        
    # saving to params_b.csv
    with open(prefix+'tests/params_b.csv', 'w') as f:
        # using csv.writer method from CSV package
        write = csv.writer(f)

        write.writerow(header_b)
        write.writerow(row_b)


# #### Base experiment

def base_exp(all_experiments, alg_types, rep_types, apdx='', debug=False):
    apd = '_'+ apdx if apdx != '' else apdx
    for exp in all_experiments:
        for alg in alg_types:
            if '-' in alg:
                print(f'\nSkipping experiment:{exp}, alg:{alg}')
                continue
            else:
                for rep in rep_types:
                    print(f'\nRunning base experiment:{exp}, alg:{alg}, rep:{rep}')
                    b_atoms = (b_atoms_vals[5]+1) if exp in experiment_names else config[exp]['b']
                    # create parameters
                    if 'c51' in rep:
                        atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                        create_params(atoms, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])
                    else:
                        atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                        create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                    # create cmd + run
                    base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                    options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep, apd, debug)
                    if debug:
                        print(prism_exec+' '+base_command+options)
                    r=os.system(prism_exec+' '+base_command+options)
                    print(f'Return code: {r}')

                    if r==0:
                        # save output for prism vi
                        if 'exp' in alg:
                            print("... Saving PRISM VI output files...")
                            copy_trace_files(cmd_base_copy, experiment_folder, exp, 'vi', rep, apd, prefix, debug)
                            copy_dtmc_files(cmd_base_copy, experiment_folder, exp, 'vi', rep, apd, prefix, debug)

                        # save output for current algorithm
                        print(f"... Saving {alg} VI output files...")
                        # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, apdx, prefix, debug)
                        copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, apd, prefix, debug)
                        copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, apd, prefix, debug)
                        copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, apd, prefix, debug)
                    else:
                        print(f'Error running experiment')
                        # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, apdx, prefix, debug)

# #### Vary atoms experiment

def vary_atoms_exp(all_experiments, alg_types, rep_types, apdx='', debug=False):

    alg = 'exp'
    apd = '_'+ apdx if apdx != '' else apdx
    for exp in all_experiments:
        for rep in rep_types:
            print(f'\nRunning vary atoms experiment:{exp}, alg:{alg}, rep:{rep}')
            
            for atom_num in (atom_vals[rep]):
                
                if (atom_num > 100) and ('c51' in rep) and (exp in experiment_names):
                    continue
                
                print(f'---- atoms = {atom_num}')
                
                # create parameters
                b_atoms = (b_atoms_vals[5]+1) if exp in experiment_names else config[exp]['b']
                if 'c51' in rep:
                    atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                    create_params(atom_num+1, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])
                else:
                    atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                    create_params(atom_num, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep,'_'+str(atom_num)+apd, debug)
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successfull
                    print(f"... Saving {alg} VI output files...")
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                else:
                    print(f'Error running experiment')
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    
# #### Vary b atoms experiment

def vary_b_exp(all_experiments, alg_types, rep_types, apdx ='', debug=False):

    apd = '_'+apdx if apdx != '' else apdx
    alg = 'cvar'
    for exp in all_experiments:
        for rep in rep_types:
            print(f'\nRunning vary B atoms experiment:{exp}, alg:{alg}, rep:{rep}')
            
            for atom_num in (b_atoms_vals):
                
                print(f'---- b atoms = {atom_num}')
                
                # create parameters
                if 'c51' in rep:
                    atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                    create_params(atoms, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], atom_num+1, [0, config[exp]['vmax']], config[exp]['alpha'])
                else:
                    atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                    create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.71), config[exp]['epsilon'], atom_num+1, [0, config[exp]['vmax']], config[exp]['alpha'])

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep,'_'+str(atom_num)+apd, debug)
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successful
                    print(f"... Saving {alg} VI output files...")
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                else:
                    print(f'Error running experiment')
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    
# #### Vary epsilon experiment

def vary_eps_exp(all_experiments, alg_types, rep_types, apdx='', debug=False):

    alg = 'exp'
    apd= '_'+apdx if apdx !='' else apdx
    eps_vals = [0.01, 0.001, 0.0001, 0.00001]
    for exp in all_experiments:
        for rep in rep_types:
            print(f'\nRunning vary eps experiment:{exp}, alg:{alg}, rep:{rep}')
            
            for val in (eps_vals):
                
                print(f'---- eps = {val}')
                
                b_atoms = (b_atoms_vals[5]+1) if exp in experiment_names else config[exp]['b']
                # create parameters
                if 'c51' in rep:
                    atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                    create_params(atoms, [0, config[exp]['vmax']], 0.01, val, b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])
                else:
                    atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                    create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), val, b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep,'_eps_'+str(val)+apd, debug)
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successfull
                    print(f"... Saving {alg} VI output files...")
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val)+apd, prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val)+apd, prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val)+apd, prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val)+apd, prefix, debug)
                else:
                    print(f'Error running experiment')
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val)+apd, prefix, debug)
                    

# #### Run experiments 
# example command:
# prism/bin/prism -javastack 80m -javamaxmem 14g prism/tests/corridor.prism prism/tests/corridor.props -distrmethod c51 -prop 3 -v -ex -exportstrat stdout -mainlog prism/log_exp.log

def init_argparse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        usage="%(prog)s exp_type [rep] [alg] [set or case study]",
        description=f"Run distr value iteration experiments.\n\
        \n Available experiments: base (-b), vary_atoms(-a), vary_b_atoms(-c), vary_epsilon (-e)\
        \n Available case studies: {list(config.keys())}\
        \n Available Distr representations: {rep_types}\
        \n Available Distr VI optimizations : {alg_types} "
    )
    parser.add_argument(
        "-v", "--version", action="version",
        version = f"{parser.prog} version 1.0.0"
    )
    
    # Additional experiments
    parser.add_argument('-b', "--base", action='store_true', help='Base experiment')
    parser.add_argument('-a', "--varyatoms", action='store_true', help='Vary the number of atoms in the distr representation')
    parser.add_argument('-c', "--varybatoms", action='store_true', help='Vary the number of atoms in the CVaR budget')
    parser.add_argument('-e', "--epsilon", action='store_true', help='Vary epsilon values')
    
    # Other
    parser.add_argument('-r', "--rep", metavar='rep', action='store', default='c51', help='representation of distribution')
    parser.add_argument('-i', "--alg", metavar='alg', action='store', default='all', help='type of optimization')
    parser.add_argument('-s', "--set", metavar='set', action='store', default='all', help='set of case studies or one case study')
    parser.add_argument('-d', "--debug", action='store_true', help='set of case studies or one case study')
    parser.add_argument('-x', "--apdx", metavar='apdx', action='store', default='', help='set an apdx for file names')

    return parser

# ##### Constants

prefix='prism/'
prism_exec = prefix+'bin/prism'
mem_alloc='-javastack 80m -javamaxmem 14g '
experiment_folder = prefix+'tests/experiments/'
rep_base = ' -distrmethod '
tail = ' -v -ex -exportstrat stdout'
log_cmd = ' -mainlog '
atoms_c51 = 101; atoms_qr=1000; def_vmax = 100; big_atoms_c51=101;  big_atoms_qr=200; def_eps=0.00001; def_alpha = 0.7
atom_vals={'c51':[1, 10, 25, 50, 75, 100, 1000], 'qr': [1, 10, 25, 50, 75, 100, 500, 1000, 5000]}
b_atoms_vals =[0, 10, 25, 50, 75, 100]
header_vi =['atoms', 'vmin', 'vmax', 'error', 'epsilon','alpha']
header_b = ['atoms', 'bmin', 'bmax']
alg_map= {'exp': 0, 'cvar': 1}
config = {
    'test': {'model':prefix+'tests/corridor.prism', 'props':prefix+'tests/corridor.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'test10' : {'model':prefix+'tests/gridmap/gridmap_10_10_v2.prism', 'props':prefix+'tests/gridmap/gridmap_10_10.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'b':26, 'alpha':def_alpha},
    'cliffs' : {'model':prefix+'tests/cliffs_v2.prism', 'props':prefix+'tests/cliffs_v2.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'betting_g' :{'model':prefix+'tests/betting_game.prism', 'props':prefix+'tests/betting_game.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'b':101, 'alpha':0.8},
    'ds_treasure' :{'model':prefix+'tests/ds_treasure.prism', 'props':prefix+'tests/ds_treasure.props', 'pn':[3,2], 'vmax': 800, 'epsilon':def_eps, 'b':101, 'alpha':0.8},
    'drones' :{'model':prefix+'tests/drones.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'gridmap10' : {'model':prefix+'tests/gridmap/gridmap_10_10_v2.prism', 'props':prefix+'tests/gridmap/gridmap_10_10.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'mud_nails' : {'model':prefix+'tests/mud_nails.prism', 'props':prefix+'tests/mud_nails.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'uav_phi3': {'model':prefix+'tests/uav.prism', 'props':prefix+'tests/uav.props', 'pn':[8,9],  'vmax': 300, 'epsilon':def_eps, 'b':51, 'alpha':def_alpha},
    'uav_phi4': {'model':prefix+'tests/uav.prism', 'props':prefix+'tests/uav.props', 'pn':[11,12],  'vmax': 300, 'epsilon':def_eps, 'b':51, 'alpha':def_alpha},
    'drones_50': {'model':prefix+'tests/drones_40.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2], 'vmax': 1000, 'epsilon':0.001, 'alpha':def_alpha},
    'drones_25': {'model':prefix+'tests/drones_25.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2],  'vmax': 1000, 'epsilon':0.001, 'b':26, 'alpha':def_alpha},
    'drones_20': {'model':prefix+'tests/drones_20.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2],  'vmax': 600, 'epsilon':0.001, 'b':51, 'alpha':0.8},
    'grid_350': {'model':prefix+'tests/gridmap/gridmap_350_2500.prism', 'props':prefix+'tests/gridmap/gridmap_350_2500.props', 'pn':[3,2], 'vmax': 1000, 'epsilon':0.001, 'b':26, 'alpha':def_alpha},
    'grid_330': {'model':prefix+'tests/gridmap/gridmap_330_2000.prism', 'props':prefix+'tests/gridmap/gridmap_330_2000.props', 'pn':[3,2], 'vmax': 1000, 'epsilon':0.001, 'b':26, 'alpha':def_alpha},
    'grid_320': {'model':prefix+'tests/gridmap/gridmap_320_300.prism', 'props':prefix+'tests/gridmap/gridmap_320_300.props', 'pn':[3,2], 'vmax': 900, 'epsilon':0.001, 'b':31, 'alpha':def_alpha},
    'grid_250_1000': {'model':prefix+'tests/gridmap/gridmap_250_1000.prism', 'props':prefix+'tests/gridmap/gridmap_250_1000.props', 'pn':[3,2], 'vmax': 900, 'epsilon':0.001, 'b':31, 'alpha':def_alpha},
    'grid_250_1200': {'model':prefix+'tests/gridmap/gridmap_250_1200.prism', 'props':prefix+'tests/gridmap/gridmap_250_1200.props', 'pn':[3,2], 'vmax': 900, 'epsilon':0.001, 'b':31, 'alpha':def_alpha},
    'grid_250_1500': {'model':prefix+'tests/gridmap/gridmap_250_1500.prism', 'props':prefix+'tests/gridmap/gridmap_250_1500.props', 'pn':[3,2], 'vmax': 900, 'epsilon':0.001, 'b':31, 'alpha':def_alpha}
}


# ##### Case studies to run 
experiment_names=['test', 'cliffs', 'mud_nails', 'gridmap10', 'drones']
set_experiments = ['cliffs', 'mud_nails','gridmap10', 'drones', 'uav_phi3', 'uav_phi4']
big_experiments = ['drones_25', 'grid_350'] # 'uav_phi3'
perf_experiments = ['cliffs', 'mud_nails', 'uav_phi3', 'grid_350', 'drones_25' ]
new_experiments = ['ds_treasure', 'betting_g', 'grid_330', 'grid_320', 'grid_250_1000', 'grid_250_1200', 'grid_250_1500']
all_experiments = set_experiments+big_experiments+new_experiments #['test', 'test10']
rep_types = ['c51', 'qr'] # 'c51', 'qr'
alg_types= ['exp', 'cvar'] # 'exp', 'cvar'
cmd_base_copy = "cp "

if __name__ == "__main__":
    parser = init_argparse()
    args = parser.parse_args()

    experiments = []
    algs = []
    reps = []

    print(vars(args))

    if args.set =='all':
        experiments = all_experiments
    elif args.set =='perf':
        experiments = perf_experiments
    elif args.set == 'new':
        experiments = new_experiments
    elif args.set == 'big':
        experiments = big_experiments
    elif args.set =='set':
        experiments = set_experiments
    elif args.set in all_experiments:
        experiments = [args.set]
    elif args.set == 'test':
        experiments = [args.set]
    else:
        print('Unrecognized case study set or name')
        sys.exit()

    # create necessary locations
    for exp in experiments:
        check_save_location(experiment_folder, exp, prefix, args.debug)

    if args.alg == 'all':
        algs = alg_types
    elif args.alg in alg_types:
        algs = [args.alg]
    else :
        print(f"Unsupported optimization {args.alg}")
        sys.exit()

    if args.rep =='all':
        reps = rep_types
    elif args.rep in rep_types:
        reps = [args.rep]
    else:
        print(f"Unsupported representation type {args.rep}")
        sys.exit()

    if args.base :
        base_exp(experiments, algs, reps, apdx=args.apdx, debug=args.debug)

    if args.varyatoms: 
        vary_atoms_exp(experiments, algs, reps, apdx=args.apdx, debug=args.debug)

    if args.varybatoms:
        vary_b_exp(experiments, algs, reps, apdx=args.apdx, debug=args.debug)

    if args.epsilon:
        vary_eps_exp(experiments, algs, reps, apdx=args.apdx, debug=args.debug)
    
    




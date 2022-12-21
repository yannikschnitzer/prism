#!/usr/bin/env python
# coding: utf-8

import os
import csv
import subprocess


# #### Functions for saving output files

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

def base_exp():

    debug =False
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
                    options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+alg+'.log'
                    if debug:
                        print(prism_exec+' '+base_command+options)
                    r=os.system(prism_exec+' '+base_command+options)
                    print(f'Return code: {r}')

                    if r==0:
                        # save output for prism vi
                        if 'exp' in alg:
                            print("... Saving PRISM VI output files...")
                            copy_trace_files(cmd_base_copy, experiment_folder, exp, 'vi', rep, '', prefix, debug)
                            copy_dtmc_files(cmd_base_copy, experiment_folder, exp, 'vi', rep, '', prefix, debug)

                        # save output for current algorithm
                        print(f"... Saving {alg} VI output files...")
                        copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '', prefix, debug)
                        copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '', prefix, debug)
                        copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '', prefix, debug)
                        copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '', prefix, debug)
                    else:
                        print(f'Error running experiment')
                        copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '', prefix, debug)

# #### Vary atoms experiment

def vary_atoms_exp():
    debug =False

    alg = 'exp'
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
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+alg+'.log'
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successfull
                    print(f"... Saving {alg} VI output files...")
                    copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                else:
                    print(f'Error running experiment')
                    copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    
# #### Vary b atoms experiment

def run_vary_b_exp():
    debug =False

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
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+alg+'.log'
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successful
                    print(f"... Saving {alg} VI output files...")
                    copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                else:
                    print(f'Error running experiment')
                    copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num), prefix, debug)
                    
# #### Vary epsilon experiment

def vary_eps_exp():
    debug =False

    alg = 'exp'
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
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+alg+'.log'
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successfull
                    print(f"... Saving {alg} VI output files...")
                    copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val), prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val), prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val), prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val), prefix, debug)
                else:
                    print(f'Error running experiment')
                    copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_eps_'+str(val), prefix, debug)
                    

# #### Run experiments 
# example command:
# prism/bin/prism -javastack 80m -javamaxmem 14g prism/tests/corridor.prism prism/tests/corridor.props -distrmethod c51 -prop 3 -v -ex -exportstrat stdout -mainlog prism/log_exp.log

# ##### Constants

prefix='prism/'
prism_exec = prefix+'bin/prism'
mem_alloc='-javastack 80m -javamaxmem 14g '
experiment_folder = prefix+'tests/experiments/'
rep_base = ' -distrmethod '
tail = ' -v -ex -exportstrat stdout'
log_cmd = ' -mainlog '+prefix+'log_'
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
    'uav_phi3': {'model':prefix+'tests/uav.prism', 'props':prefix+'tests/uav.props', 'pn':[8,9],  'vmax': 1000, 'epsilon':def_eps, 'b':26, 'alpha':def_alpha},
    'drones_50': {'model':prefix+'tests/drones_40.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2], 'vmax': 1000, 'epsilon':def_eps, 'alpha':def_alpha},
    'drones_25': {'model':prefix+'tests/drones_25.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2],  'vmax': 1000, 'epsilon':def_eps, 'b':26, 'alpha':def_alpha},
    'grid_350': {'model':prefix+'tests/gridmap/gridmap_350_2500.prism', 'props':prefix+'tests/gridmap/gridmap_350_2500.props', 'pn':[3,2], 'vmax': 1000, 'epsilon':def_eps, 'b':26, 'alpha':def_alpha}
}


# ##### Case studies to run 
experiment_names=['test', 'cliffs', 'mud_nails', 'gridmap10', 'drones']
set_experiments = ['cliffs', 'mud_nails','gridmap10', 'drones', 'uav_phi3']
big_experiments = ['drones_25', 'grid_350'] # 'uav_phi3'
perf_experiments = ['cliffs', 'mud_nails', 'uav_phi3', 'grid_350', 'drones_25' ]
new_experiments = ['ds_treasure', 'betting_g']
all_experiments = ['drones_25'] #['test', 'test10']
apdx = ''
rep_types = ['c51'] # 'c51', 'qr'
alg_types= ['exp', 'cvar'] # 'exp', 'cvar'
cmd_base_copy = "cp "






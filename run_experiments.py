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
        print("Making experiment folder for "+exp_name)
        if debug:
            print(command)
        os.system(command)

    if not os.path.isdir(trace_folder):
        command = 'mkdir '+trace_folder
        print('Making traces folder')
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
    if 'dtmc' in alg:
        source = prefix+'distr.csv' 
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
                        if 'atoms' in config[exp]:
                            atoms = config[exp]['atoms']
                        else:
                            atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                        create_params(atoms, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])
                    else:
                        if 'atoms' in config[exp]:
                            atoms = config[exp]['atoms']-1
                        else:
                            atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                        create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                    # create cmd + run
                    base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                    if 'const' in config[exp]:
                        base_command += ' '+config[exp]['const']
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

# #### Compute risk neutral and risk sensitive with varying alphas experiment

def vary_alpha(all_experiments, rep_types, apdx='', debug=False):
    apd = '_'+ apdx if apdx != '' else apdx
    alg = 'cvar'
    for exp in all_experiments:
        for rep in rep_types:
            for val in (alpha_vals):
                print(f'\nRunning vary alpha experiment:{exp}, alg:{alg}, rep:{rep}, alpha:{val}')
                b_atoms = (b_atoms_vals[5]+1) if exp in experiment_names else config[exp]['b']
                # create parameters
                if 'c51' in rep:
                    if 'atoms' in config[exp]:
                        atoms = config[exp]['atoms']
                    else:
                        atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                    create_params(atoms, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], val)
                else:
                    if 'atoms' in config[exp]:
                        atoms = config[exp]['atoms']-1
                    else:
                        atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                    create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], val)

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                if 'const' in config[exp]:
                    base_command += ' '+config[exp]['const']
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep,'_alpha_'+str(val)+apd, debug)
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successfull
                    print(f"... Saving {alg} VI output files...")
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_alpha_'+str(val)+apd, prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_alpha_'+str(val)+apd, prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_alpha_'+str(val)+apd, prefix, debug)
                else:
                    print(f'Error running experiment')
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, apdx, prefix, debug)

# #### Vary atoms experiment

def vary_atoms_exp(all_experiments, rep_types, apdx='', debug=False):

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
                if 'const' in config[exp]:
                    base_command += ' '+config[exp]['const']
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

# #### Vary atoms experiment

def vary_atoms_cvar(all_experiments, rep_types, apdx='', debug=False):

    alg = 'cvar'
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
                    create_params(atom_num, [0, config[exp]['vmax']], ((1.0/atoms)*10), config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                if 'const' in config[exp]:
                    base_command += ' '+config[exp]['const']
                options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep,'_'+str(atom_num)+'_va'+apd, debug)
                if debug:
                    print(prism_exec+' '+base_command+options)
                r=os.system(prism_exec+' '+base_command+options)
                print(f'Return code: {r}')

                if r==0:
                    # save output for current algorithm if run was successfull
                    print(f"... Saving {alg} VI output files...")
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+'_va'+apd, prefix, debug)
                    copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+'_va'+apd, prefix, debug)
                    copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+'_va'+apd, prefix, debug)
                else:
                    print(f'Error running experiment')
                    # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, '_'+str(atom_num)+apd, prefix, debug)
                    
# #### Vary b atoms experiment

def vary_b_exp(all_experiments, rep_types, apdx ='', debug=False):

    apd = '_'+apdx if apdx != '' else apdx
    alg = 'cvar'
    for exp in all_experiments:
        for rep in rep_types:
            print(f'\nRunning vary B atoms experiment:{exp}, alg:{alg}, rep:{rep}')
            
            for atom_num in (b_atoms_vals):
                
                print(f'---- b atoms = {atom_num}')
                
                # create parameters
                if 'c51' in rep:
                    if 'atoms' in config[exp]:
                        atoms = config[exp]['atoms']
                    else:
                        atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                    create_params(atoms, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], atom_num+1, [0, config[exp]['vmax']], config[exp]['alpha'])
                else:
                    if 'atoms' in config[exp]:
                        atoms = config[exp]['atoms']-1
                    else:
                        atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                    create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.71), config[exp]['epsilon'], atom_num+1, [0, config[exp]['vmax']], config[exp]['alpha'])

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                if 'const' in config[exp]:
                    base_command += ' '+config[exp]['const']
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

def vary_eps_exp(all_experiments, rep_types, apdx='', debug=False):

    alg = 'exp'
    apd= '_'+apdx if apdx !='' else apdx
    for exp in all_experiments:
        for rep in rep_types:
            print(f'\nRunning vary eps experiment:{exp}, alg:{alg}, rep:{rep}')
            
            for val in (eps_vals):
                
                print(f'---- eps = {val}')
                
                b_atoms = (b_atoms_vals[5]+1) if exp in experiment_names else config[exp]['b']
                # create parameters
                if 'c51' in rep:
                    if 'atoms' in config[exp]:
                        atoms = config[exp]['atoms']
                    else:
                        atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                    create_params(atoms, [0, config[exp]['vmax']], 0.01, val, b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])
                else:
                    if 'atoms' in config[exp]:
                        atoms = config[exp]['atoms']-1
                    else:
                        atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                    create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), val, b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                # create cmd + run
                base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']+rep_base+rep
                if 'const' in config[exp]:
                    base_command += ' '+config[exp]['const']
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

def dtmc_comparison(all_experiments, rep_types, apdx='', debug=False):
    alg_types = ['exp', 'dtmc']
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
                        if 'atoms' in config[exp]:
                            atoms = config[exp]['atoms']
                        else:
                            atoms= atoms_c51 if exp in experiment_names else big_atoms_c51
                        create_params(atoms, [0, config[exp]['vmax']], 0.01, config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])
                    else:
                        if 'atoms' in config[exp]:
                            atoms = config[exp]['atoms']-1
                        else:
                            atoms= atoms_qr if exp in experiment_names else big_atoms_qr
                        create_params(atoms, [0, config[exp]['vmax']], ((1.0/atoms)*10 if 'uav' not in exp else 0.07), config[exp]['epsilon'], b_atoms, [0, config[exp]['vmax']], config[exp]['alpha'])

                    # create cmd + run
                    base_command = mem_alloc+config[exp]['model']+' '+config[exp]['props']
                    
                    if 'exp' in alg:
                        base_command+= rep_base+rep+' -mdp'
                    
                    if 'const' in config[exp]:
                        base_command += ' '+config[exp]['const']
                    
                    options =' -prop '+str(config[exp]['pn'][alg_map[alg]])+tail+log_cmd+log_target(experiment_folder, exp,alg, rep, apd, debug)
                    if debug:
                        print(prism_exec+' '+base_command+options)
                    r=os.system(prism_exec+' '+base_command+options)
                    print(f'Return code: {r}')

                    if r==0:
                        # save output for current algorithm
                        print(f"... Saving {alg} VI output files...")
                        # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, apdx, prefix, debug)
                        if 'exp' in alg:
                            copy_vi_files(cmd_base_copy, experiment_folder, exp, alg, rep, apd, prefix, debug)
                            copy_trace_files(cmd_base_copy, experiment_folder, exp, alg, rep, apd, prefix, debug)
                        copy_dtmc_files(cmd_base_copy, experiment_folder, exp, alg, rep, apd, prefix, debug)
                    else:
                        print(f'Error running experiment')
                        # copy_log_files(cmd_base_copy, experiment_folder, exp, alg, rep, apdx, prefix, debug)                   

# #### Run experiments 
# example command:
# prism/bin/prism -javastack 80m -javamaxmem 14g prism/tests/corridor.prism prism/tests/corridor.props -distrmethod c51 -prop 3 -v -ex -exportstrat stdout -mainlog prism/log_exp.log

def init_argparse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        usage="%(prog)s exp_type [rep] [alg] [set or case study]",
        description=f"Run distr value iteration experiments.\n \
        \n Available experiments: base (-b), vary_atoms(-a), vary_atoms_cvar(-t), vary_b_atoms(-c), vary_epsilon (-e), dtmc (-m)\
        \n Available case studies: {list(config.keys())} --- default: all \
        \n Available Distr representations: {rep_types} --- default: c51 \
        \n Available Distr VI optimizations : {alg_types}  --- default: all ",
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument(
        "-v", "--version", action="version",
        version = f"{parser.prog} version 1.1.0"
    )
    
    # Additional experiments
    parser.add_argument('-b', "--base", action='store_true', help='Base experiment')
    parser.add_argument('-a', "--varyatoms", action='store_true', help='Vary the number of atoms in the distr representation from: '+str(atom_vals))
    parser.add_argument('-t', "--varyatomscvar", action='store_true', help='Vary the number of atoms in the distr representation for cvar from: '+str(atom_vals))
    parser.add_argument('-c', "--varybatoms", action='store_true', help='Vary the number of atoms in the CVaR budget from:'+ str(b_atoms_vals))
    parser.add_argument('-e', "--epsilon", action='store_true', help='Vary epsilon values from:'+str(eps_vals))
    parser.add_argument('-p', "--alpha", action='store_true', help='Vary alpha values from: '+str(alpha_vals))
    parser.add_argument('-m', "--dtmc", action='store_true', help='Run approximate DVI and Forward DTMC methods')
    
    # Other
    parser.add_argument('-r', "--rep", metavar='rep', action='store', default='c51', help='representation of distribution')
    parser.add_argument('-i', "--alg", metavar='alg', action='store', default='all', help='type of optimization')
    parser.add_argument('-s', "--set", metavar='set', action='store', default='all', help='set of case studies or one case study')
    parser.add_argument('-d', "--debug", action='store_false', help='Remove debug option for additional prints')
    parser.add_argument('-x', "--apdx", metavar='apdx', action='store', default='', help='set an appendix to output file names')

    return parser

# ##### Constants

prefix='prism/'
prism_exec = prefix+'bin/prism'
mem_alloc='-javastack 100m -javamaxmem 14g '
experiment_folder = prefix+'tests/experiments/'
trace_folder = prefix+'tests/traces/'
rep_base = ' -distrmethod '
tail = ' -v -ex -exportstrat stdout'
log_cmd = ' -mainlog '
atoms_c51 = 101; atoms_qr=1000; def_vmax = 100; big_atoms_c51=101;  big_atoms_qr=100; def_eps=0.00001; def_alpha = 0.7
atom_vals={'c51':[1, 10, 25, 50, 75, 100, 1000], 'qr': [1, 10, 25, 50, 75, 100, 500, 1000, 5000]}
b_atoms_vals =[0, 10, 25, 50, 75, 100]
# alpha_vals = [0.1, 0.2, 0.5, 0.7, 0.9, 0.99]
alpha_vals = [0.1, 0.5, 0.99]
eps_vals = [0.01, 0.001, 0.0001, 0.00001]
header_vi =['atoms', 'vmin', 'vmax', 'error', 'epsilon','alpha']
header_b = ['atoms', 'bmin', 'bmax']
alg_map= {'exp': 0, 'cvar': 1, 'dtmc':2}
config = {
    'test': {'model':prefix+'tests/corridor.prism', 'props':prefix+'tests/corridor.props', 'pn':[3,2], 'vmax': 25, 'atoms':11, 'epsilon':def_eps, 'b':30, 'alpha':def_alpha},
    'cliffs' : {'model':prefix+'tests/cliffs_v2.prism', 'props':prefix+'tests/cliffs_v2.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'betting_g' :{'model':prefix+'tests/betting_game.prism', 'props':prefix+'tests/betting_game.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'b':101, 'alpha':0.8},
    'ds_treasure' :{'model':prefix+'tests/ds_treasure.prism', 'props':prefix+'tests/ds_treasure.props', 'pn':[3,2], 'vmax': 800, 'atoms':201, 'epsilon':def_eps, 'b':101, 'alpha':0.8},
    'drones' :{'model':prefix+'tests/drones.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'gridmap_10' : {'model':prefix+'tests/gridmap/gridmap_10_10.prism', 'props':prefix+'tests/gridmap/gridmap_10_10.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'mud_nails' : {'model':prefix+'tests/mud_nails.prism', 'props':prefix+'tests/mud_nails.props', 'pn':[3,2], 'vmax': def_vmax, 'epsilon':def_eps, 'alpha':def_alpha},
    'uav_var': {'model':prefix+'tests/uav_var.prism', 'props':prefix+'tests/uav_var.props', 'pn':[2,3],  'vmax': 500, 'atoms':201, 'epsilon':def_eps, 'b':101, 'alpha':def_alpha},
    'drones_15': {'model':prefix+'tests/drones_15.prism', 'props':prefix+'tests/drones.props', 'pn':[1,2],  'vmax': 500, 'atoms':201, 'epsilon':0.001, 'b':51, 'alpha':0.9},
    'gridmap_150_3918': {'model':prefix+'tests/gridmap/gridmap_150_3918.prism', 'props':prefix+'tests/gridmap/gridmap_150_3918.props', 'pn':[3,2], 'vmax': 600, 'atoms':201, 'epsilon':0.001, 'b':101, 'alpha':0.8},
    'gridworld_4': {'model':prefix+'tests/gridworld/gridworld.nm', 'props':prefix+'tests/gridworld/gridworld.props', 'pn':[3,2], 'vmax': 50, 'atoms':51, 'epsilon':def_eps, 'b':51, 'alpha':0.9, 'const':'-const xm=04,ym=04,jx_min=01,jx_max=04,jy_min=1,jy_max=5,jr=0.1,fr=0.00'},
    'gridworld_8': {'model':prefix+'tests/gridworld/gridworld.nm', 'props':prefix+'tests/gridworld/gridworld.props', 'pn':[3,2], 'vmax': 50, 'atoms':51, 'epsilon':def_eps, 'b':51, 'alpha':0.9, 'const':'-const xm=08,ym=04,jx_min=02,jx_max=06,jy_min=1,jy_max=5,jr=0.1,fr=0.00'},
    'gridworld_16': {'model':prefix+'tests/gridworld/gridworld.nm', 'props':prefix+'tests/gridworld/gridworld.props', 'pn':[3,2], 'vmax': 50, 'atoms':51, 'epsilon':def_eps, 'b':51, 'alpha':0.9, 'const':'-const xm=16,ym=04,jx_min=06,jx_max=10,jy_min=1,jy_max=5,jr=0.1,fr=0.00'},
    'gridworld_32': {'model':prefix+'tests/gridworld/gridworld.nm', 'props':prefix+'tests/gridworld/gridworld.props', 'pn':[3,2], 'vmax': 50, 'atoms':51, 'epsilon':def_eps, 'b':51, 'alpha':0.9, 'const':'-const xm=32,ym=04,jx_min=14,jx_max=18,jy_min=1,jy_max=5,jr=0.1,fr=0.00'},
    'firewire': {'model':prefix+'tests/firewire/firewire.nm', 'props':prefix+'tests/firewire/firewire.props', 'pn':[3,2], 'vmax': 180, 'atoms':61, 'epsilon':def_eps, 'b':31, 'alpha':0.9, 'const':'-const delay=30,fast=0.1'},
    'wlan2': {'model':prefix+'tests/wlan/wlan2.nm', 'props':prefix+'tests/wlan/wlan.props', 'pn':[3,2], 'vmax': 80, 'atoms':41, 'epsilon':def_eps, 'b':41, 'alpha':0.9, 'const':'-const TRANS_TIME_MAX=315'},
    'selfStabilising_10': {'model':prefix+'tests/quantile/selfStabilising/10procs.prism', 'props':prefix+'tests/quantile/selfStabilising/minimalSteps.props', 'pn':[3,2], 'vmax': 200, 'epsilon':def_eps, 'b':101, 'alpha':def_alpha},
    'selfStabilising_15': {'model':prefix+'tests/quantile/selfStabilising/15procs.prism', 'props':prefix+'tests/quantile/selfStabilising/minimalSteps.props', 'pn':[3,2], 'vmax': 300, 'epsilon':0.001, 'b':51, 'alpha':def_alpha},
    'egl_8_3': {'model':prefix+'tests/dtmcs/egl/egl.pm', 'props':prefix+'tests/dtmcs/egl/messagesA.props', 'pn':[2,-1, 1], 'vmax': 40, 'atoms':41, 'epsilon':def_eps, 'b':101, 'alpha':0.9, 'const':'-const N=8,L=3'},
    'egl_8_4': {'model':prefix+'tests/dtmcs/egl/egl.pm', 'props':prefix+'tests/dtmcs/egl/messagesA.props', 'pn':[2,-1, 1], 'vmax': 40, 'atoms':41, 'epsilon':def_eps, 'b':101, 'alpha':0.9, 'const':'-const N=8,L=4'},
    'egl_8_5': {'model':prefix+'tests/dtmcs/egl/egl.pm', 'props':prefix+'tests/dtmcs/egl/messagesA.props', 'pn':[2,-1, 1], 'vmax': 50, 'atoms':51, 'epsilon':def_eps, 'b':101, 'alpha':0.9, 'const':'-const N=8,L=5'},
    'herman_13': {'model':prefix+'tests/dtmcs/herman/herman13.pm', 'props':prefix+'tests/dtmcs/herman/steps.props', 'pn':[2,-1, 1], 'vmax': 120, 'atoms':121, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'herman_15': {'model':prefix+'tests/dtmcs/herman/herman15.pm', 'props':prefix+'tests/dtmcs/herman/steps.props', 'pn':[2,-1, 1], 'vmax': 120, 'atoms':121, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'herman_17': {'model':prefix+'tests/dtmcs/herman/herman17.pm', 'props':prefix+'tests/dtmcs/herman/steps.props', 'pn':[2,-1, 1], 'vmax': 140, 'atoms':141, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'leader_sync8_5': {'model':prefix+'tests/dtmcs/leader_sync/leader_sync8_5.pm', 'props':prefix+'tests/dtmcs/leader_sync/time.props', 'pn':[2,-1, 1], 'vmax': 20, 'atoms':21, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'leader_sync8_6': {'model':prefix+'tests/dtmcs/leader_sync/leader_sync8_6.pm', 'props':prefix+'tests/dtmcs/leader_sync/time.props', 'pn':[2,-1, 1], 'vmax': 20, 'atoms':21, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'leader_sync8_7': {'model':prefix+'tests/dtmcs/leader_sync/leader_sync8_7.pm', 'props':prefix+'tests/dtmcs/leader_sync/time.props', 'pn':[2,-1, 1], 'vmax': 50, 'atoms':51, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'leader_sync10_3': {'model':prefix+'tests/dtmcs/leader_sync/leader_sync10_3.pm', 'props':prefix+'tests/dtmcs/leader_sync/time.props', 'pn':[2,-1, 1], 'vmax': 30, 'atoms':31, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'leader_sync10_4': {'model':prefix+'tests/dtmcs/leader_sync/leader_sync10_4.pm', 'props':prefix+'tests/dtmcs/leader_sync/time.props', 'pn':[2,-1, 1], 'vmax': 30, 'atoms':31, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
    'leader_sync10_5': {'model':prefix+'tests/dtmcs/leader_sync/leader_sync10_5.pm', 'props':prefix+'tests/dtmcs/leader_sync/time.props', 'pn':[2,-1, 1], 'vmax': 100, 'atoms':101, 'epsilon':def_eps, 'b':101, 'alpha':0.9},
}


###### Case studies to run 
experiment_names=[ 'cliffs', 'mud_nails', 'gridmap_10', 'drones']
set_experiments = ['test', 'betting_g','ds_treasure', 'gridmap_10', 'uav_var', 'drones']
big_experiments = ['drones_15','gridmap_150_3918']
ssp_comparison = ['gridworld_4', 'gridworld_8', 'gridworld_16', 'gridworld_32', 'firewire', 'wlan2' ]
# exp_quantile = ['selfStabilising_10', 'selfStabilising_15']
egl = [ 'egl_8_3', 'egl_8_4', 'egl_8_5'] #, 'egl_8_6'
leader = ['leader_sync8_5', 'leader_sync10_4', 'leader_sync8_6'] # , 'leader_sync12_3']
herman = ['herman_13', 'herman_15', 'herman_17']
exp_dtmc = egl + herman + leader
all_experiments = set_experiments+big_experiments + ssp_comparison + exp_dtmc
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
    elif args.set == 'big':
        experiments = big_experiments
    elif args.set == 'main':
        experiments = ['betting_g','ds_treasure', 'gridmap_150_3918', 'uav_var', 'drones_15']
    elif args.set =='set':
        experiments = set_experiments
    elif args.set in all_experiments:
        experiments = [args.set]
    elif args.set == 'comparison':
        experiments = ssp_comparison
    elif args.set == 'quantile':
        experiments = exp_quantile
    elif args.set == 'dtmc':
        experiments = exp_dtmc
    elif args.set == 'herman':
        experiments = herman
    elif args.set == 'leader':
        experiments = leader
    elif args.set == 'egl':
        experiments = egl
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
        vary_atoms_exp(experiments, reps, apdx=args.apdx, debug=args.debug)

    if args.varyatomscvar: 
        vary_atoms_cvar(experiments, reps, apdx=args.apdx, debug=args.debug)

    if args.varybatoms:
        vary_b_exp(experiments, reps, apdx=args.apdx, debug=args.debug)

    if args.epsilon:
        vary_eps_exp(experiments, reps, apdx=args.apdx, debug=args.debug)
    
    if args.alpha:
        vary_alpha(experiments, reps, apdx=args.apdx, debug=args.debug)

    if args.dtmc:
        dtmc_comparison(experiments, reps, apdx=args.apdx, debug=args.debug)
    
    




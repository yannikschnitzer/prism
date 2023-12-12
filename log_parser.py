import re, os 

dvi_params_1_pat = "atoms:[0-9]+"
dvi_params_2_pat = "alpha:[0-9]+.[0-9]+"
iteration_num_pat = "V\[start\].*[0-9]+"
vi_time_pat = "Value iteration computation (min) : [0-9]+"
dtmc_time_dvi_pat = "DTMC computation (min) : [0-9]+"
dtmc_time_vi_pat = "DTMC computation VI : [0-9]+"
exp_val_dvi_pat = "E at initial state:[0-9]+"
exp_val_vi_pat = "PRISM VI :[0-9]+"
cvar_val_pat = ""

def parse_exp (filename, parse_dtmc = True, debug = True):
    pass

def parse_cvar ():
    pass
# Distributional Probabilistic Model Checking Instructions

**Paper**: "Distributional Probabilistic Model Checking"
by Ingy ElSayed-Aly, David Parker and Lu Feng

Please find the code repository for this paper [here](https://github.com/davexparker/prism/tree/ingy).

## Supporting files
Included below are the PRISM models and properties files for the benchmarks featured in the paper. 

All model files can be found in `prism/tests` and `prism/tests/gridmap/`.

* **Betting Game**: Betting game example (adapted from [RDLH22](https://ojs.aaai.org/index.php/ICAPS/article/view/19814) )
    * Model: [betting_game.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/betting_game.prism)
    * Properties: [betting_game.props](https://github.com/davexparker/prism/blob/ingy/prism/tests/betting_game.props)

* **Deep Sea Treasure**: Deep Sea Treasure example (adapted from [RDLH22](https://ojs.aaai.org/index.php/ICAPS/article/view/19814) )
    * Model: [ds_treasure.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/ds_treasure.prism)
    * Properties: [ds_treasure.props](https://github.com/davexparker/prism/blob/ingy/prism/tests/ds_treasure.props)

* **Obstacle**: Gridworld navigation examples from a source to a target (adapted from [CTMP15](https://proceedings.neurips.cc/paper/2015/file/64223ccf70bbb65a3a4aceac37e21016-Paper.pdf) ):
    * Base Model: [gridmap_10_10.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/gridmap/gridmap_10_10.prism)
    * Properties: [gridmap_10_10.props](https://github.com/davexparker/prism/blob/ingy/prism/tests/gridmap/gridmap_10_10.props)
    * Larger Model (Obstacle 150): [gridmap_150_3918.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/gridmap/gridmap_150_3918.prism)
    * Properties: [gridmap_150_3918.props](https://github.com/davexparker/prism/blob/ingy/prism/tests/gridmap/gridmap_150_3918.props)

* **Energy**: Unmanned drone scenario while managing a battery resource:
    * Base Model: [drones.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/drones.prism)
    * Larger Model (Energy 15): [drones_15.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/drones_15.prism)
    * Properties: [drones.props](https://github.com/davexparker/prism/blob/ingy/prism/tests/drones.props)

* **uav**: Unmanned aerial vehicle (adapted from [FWHT16](https://prismmodelchecker.org/bibitem.php?key=FWHT16))
    * Model: [uav_var.prism](https://github.com/davexparker/prism/blob/ingy/prism/tests/uav_var.prism)
    * Properties: [uav_var.props](https://github.com/davexparker/prism/blob/ingy/prism/tests/uav_var.props)


## Running Instructions

The repository linked above can be run with Java or using a Python file. We recommend a setup with 32 GB of RAM. Make sure to check the right branch is checked out before running by using: 
`git checkout ingy`.

**Python**: We provide a Python 3 script to make running experiments and organize the output files easier. 
**usage**: `python run_experiments.py exp_type [rep] [alg] [set or case study]`
The experiment types `exp_type` available are: base experiment `-b`, vary the number of atoms in the distribution representation with risk neutral DVI `-a`, vary the number of atoms in the distribution representation with risk sensitive DVI `-t`, vary the number of atoms in the slack variable approximation with risk sensitive DVI `-c`, vary the DTMC precision `-e`. 
* The `-r` option selects the distribution representation method: `c51` for categorical and `qr` for quantile representation. 
* The `-s` option selects a case study. Available case studies include:['betting_g', 'ds_treasure', 'drones', 'gridmap_10', 'mud_nails', 'uav_var', 'drones_15', 'gridmap_150_3918'].
* The `-i` option selects risk sensitive or risk neutral DVI: `exp` for risk neutral, `cvar` for risk sensitive. The default value is all. Note that certain experiments can only be run with certain version of DVI.
* The `-x` option selects an appendix string to add to output files

Examples:
* To run risk sensitive and risk neutral DVI with a categorical representation on the Obtacle 10 case study with '_testing' as an appendix to output file names:

    `python run_experiments.py -b -r c51 -s gridmap_10 -x testing`
* To run risk sensitive and risk neutral DVI with a categorical representation on the Obtacle 10 case study, run the vary atoms experiment with risk neutral DVI and the slack approximation experiment:
    
    `python run_experiments.py -b -a -c -r c51 -s gridmap_10`

**Java**: Check the property file to decide which type of DVI to run. Properties including `R(dist)min` runs risk-neutral DVI and properties with `R(cvar)min` will run risk-sensitive DVI. 

The `-prop` option indicates the property number in the `.props` file, the `-distrmethod` option selects the distribution representation method (c51 for categorical and qr for quantile representation). 

The DVI parameters should be updated in the `prism/tests/params_vi.csv` file containing the number of atoms for the distributional representation, vmin and vmax for the representation interval, error for the error threshold to determine convergence, epsilon for the DTMC error, alpha for the alpha CVaR parameter if applicable. 

If using risk-sensitive DVI, the `prism/tests/params_b.csv` file contains the number of atoms, vmin and vmax for the slack variable approximation. 

Examples: 
* Risk-neutral DVI with Betting Game with C51: 
    
    `prism/bin/prism -javastack 80m -javamaxmem 14g prism/tests/betting_game.prism prism/tests/betting_game.props -distrmethod c51 -prop 3 -v -ex -exportstrat stdout -mainlog prism/betting_game_risk_neutral.log`
* Risk-sensitive DVI with Betting Game with C51: 
    
    `prism/bin/prism -javastack 80m -javamaxmem 14g prism/tests/betting_game.prism prism/tests/betting_game.props -distrmethod c51 -prop 2 -v -ex -exportstrat stdout -mainlog prism/betting_game_risk_sensitive.log`
* Risk-neutral DVI with Obstacle 10 with QR:
    
     `prism/bin/prism -javastack 80m -javamaxmem 14g prism/tests/gridmap/gridmap_10_10.prism prism/tests/gridmap/gridmap_10_10.props -distrmethod qr -prop 3 -v -ex -exportstrat stdout -mainlog prism/obstacle_risk_neutral.log`

## Output files

If using the Python script, all the output files for the run will be saved in `prism/tests/experiments/<experiment_name>/` i.e. if running a base experiment for the betting game model, the files will be saved in `prism/tests/experiments/betting_g`. Files with the `<algorithm>` set as 'vi' represent PRISM (non distributional) VI baseline files.
Files:
* Log files: 
    * Base experiment: 
    `<experiment_name>_log_<algorithm>_<representation_type>_<appendix>.log`
    * Vary atoms experiment: 
    `<experiment_name>_log_<algorithm>_<representation_type>_<atoms>_<appendix>.log`
* Trace files: this type of file shows a trajectory sample of the synthesized policy. Start at the initial state and take policy actions until a terminal state is reached. They are of the format: `<experiment_name>_distr_<algorithm>_<representation_type>_<appendix>_trace.csv`
* VI distribution files: these files contain the approximated distribution for the initial state computed during distributional VI. The distribution files save the atom index (r), the associated probability (p) and the atom value (z). They follow the naming format: `<experiment_name>_distr_<algorithm>_<representation_type>_<appendix>.csv` or `<experiment_name>_distr_<algorithm>_<representation_type>_<atoms>_<appendix>.csv`. 
* DTMC distribution files: these files contain the distributions obtained using the forward generation method on the induced DTMC. They save all possible atom values (r) and their associated probability. They follow the naming format: `<experiment_name>_distr_dtmc_<algorithm>_<representation_type>_<appendix>.csv` or `<experiment_name>_distr_dtmc_<algorithm>_<representation_type>_<atoms>_<appendix>.csv`.

## Output visualization

We provide a JupyterLab Python Notebook to visualize results and print distributional metrics: [distr.ipynb](https://github.com/davexparker/prism/blob/ingy/prism/distr.ipynb)

All cells before the Setup section contain the distribution loading and metric computation functions.
Blue distribution graphs show the DTMC distributions, while the green graphs show the approximate DVI distributions.

## PRISM Installation

For detailed installation instructions, check the online manual at:

  http://www.prismmodelchecker.org/manual/InstallingPRISM/Instructions

For source code distributions:
 * enter the PRISM directory and type `cd prism` then `make`

If you have problems check the manual, especially the section "Common Problems And Questions".

## Python Installation

We recommend using a virtual environment or conda environment with Python 3.6+

If using a virtual environment, essential python packages can be installed using:
`pip install -r reqs.txt`

## Licensing

PRISM is distributed under the GNU General Public License (GPL), version 2.
A copy of this license can be found in the file `COPYING.txt`.
For more information, see:

  http://www.gnu.org/licenses/

PRISM uses the CUDD (Colorado University Decision Diagram) library of Fabio Somenzi,
which is freely available. For more information about this library, see:

  http://vlsi.colorado.edu/~fabio/CUDD/

PRISM also uses various other libraries (mainly to be found in the lib directory).
For details of those, and for links to source where we distribute only binaries, see:

http://www.prismmodelchecker.org/other-downloads.php

## Acknowledgements

PRISM was created and is still actively maintained by:

 * Dave Parker (University of Oxford)
 * Gethin Norman (University of Glasgow)
 * Marta Kwiatkowska (University of Oxford) 

Development of the tool is currently led from Oxford by Dave Parker. Other current key developers are:

 * Joachim Klein (formerly Technische Universität Dresden)

We gratefully acknowledge contributions to the PRISM code-base from various sources,
including (in approximately reverse chronological order):

 * Xueyi Zou: Partially observable Markov decision processes (POMDPs)
 * Steffen Märcker: Fixes and improvements, especially in explicit engine
 * Chris Novakovic: Build infrastructure and explicit engine improvements
 * Ernst Moritz Hahn: Parametric model checking, fast adaptive uniformisation + various other features
 * Frits Dannenberg: Fast adaptive uniformisation
 * Vojtech Forejt: Various model checking code, including multi-objective + GUI enhancements
 * Hongyang Qu: Multi-objective model checking
 * Mateusz Ujma: Bug fixes and GUI improvements
 * Christian von Essen: Symbolic/explicit-state model checking
 * Vincent Nimal: Approximate (simulation-based) model checking techniques
 * Mark Kattenbelt: Wide range of enhancements/additions, especially in the GUI
 * Carlos Bederian (working with Pedro D'Argenio): LTL model checking for MDPs
 * Gethin Norman: Precomputation algorithms, abstraction
 * Alistair John Strachan: Port to 64-bit architectures
 * Alistair John Strachan, Mike Arthur and Zak Cohen: Integration of JFreeChart into PRISM
 * Charles Harley and Sebastian Vermehren: GUI enhancements
 * Rashid Mehmood: Improvements to low-level data structures and numerical solution algorithms
 * Stephen Gilmore: Support for the stochastic process algebra PEPA
 * Paolo Ballarini & Kenneth Chan: Port to Mac OS X
 * Andrew Hinton: Original versions of the GUI, Windows port and simulator
 * Joachim Meyer-Kayser: Original implementation of the "Fox-Glynn" algorithm 

For more details see:

  http://www.prismmodelchecker.org/people.php

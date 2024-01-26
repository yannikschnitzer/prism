This README provides details for the experiments presented in the
article

"Advances in Probabilistic Model Checking with PRISM:
 Variable Reordering, Quantiles and Weak Deterministic Buechi
 Automata"

 http://dx.doi.org/10.1007/s10009-017-0456-3

published in the special TACAS'16 issue of STTT.

In case of problems / questions, feel free to contact
klein@tcs.inf.tu-dresden.de.

https://wwwtcs.inf.tu-dresden.de/ALGI/PUB/STTT/

Reordering
----------

For reordering, you'll find the relevant sections (DTMCs, CTMCs and MDPs)
of the PRISM benchmark suite under

reorder/prism-benchmarks

(taken from https://github.com/prismmodelchecker/prism-benchmarks/tree/686e290135201a071e7e73e803d1b547863649c2)

We removed the wlan_dl case study, as there the parameters are not functional out-of-the-box.


For reordering, you can use the following command line switches:

-reorder
-reorder -globalizevariables
-reorder -explodebits 
-reorder -globalizevariables -explodebits

and additional option 

-reordermaxgrowth 2

to specify a max growth factor of 2 (the MTBDD size is allowed to double during sifting).

To automatically provide the appropriate (model-specific) parameters to PRISM,
you can use the prism-auto script. Assuming that $D stands for the prism-directory
(i.e., the one with bin/ as a subdirectory), you can use, e.g.,

$D/etc/scripts/prism-auto -p $D/bin/prism --build -x '-hybrid -reorder -explodebits' $X

where $X is some model directory inside reorder/prism-benchmarks.
The options after -x are passed along to PRISM. For the presented results
we used the -hybrid switch to obtain the "top" variable ordering. For the MTBDD engine (-mtbdd)
there is the option of using -o1 to obtain the "top" variable ordering there.

You can provide a memory limit for CUDD via -cuddmaxmem.




Quantile experiments
--------------------

You can find the models used for the case studies in the following
subdirectories of the quantiles/ directory:

energyAwareJobScheduling
leaderElection
selfStabilising

Inside these directories, you'll also find the quantile properties (in
the *.props files). I.e., an example PRISM call would be

prism/bin/prism quantile/selfStabilising/03procs.prism quantile/selfStabilising/minimalSteps.props


In energyAwareJobScheduling, the model files are the reordered
variant, the original files (pre reordering) are also provided (UNORDERED).

For the benchmarks, the following command-line options were used:

Explicit engine:
 -explicit -zeroRewardSccMethod TARJAN_ITERATIVE -valuesStorage ALL_STATES -debugQuantile 1

semiHybrid:
 -hybrid -cuddmaxmem 50g

semiSparse:
 -sparse -cuddmaxmem 50g

MTBDD: 
 -mtbdd -cuddmaxmem 50g

You can specify a Java memory limit using -javamaxmem and a timeout
using -timeout.


EServer
-------

Two instances of the EServer model have been employed to carry out our
case study (in directory quantile/eserver):

taosd_standard.prism    is the plain model, used to compute the query
    filter(print, quantile (min e, Pmax>.95 [ F{reward{"energy"}<=e} (time_goal) ]), initFeats)
      i.e., for each server configuration, compute the minimal amount of energy required
      to guarantee in 95% of the cases that the server is still running at the end of the day.

taosd_utility.prism     is the utility model, used to compute the query
    filter(print, quantile (min e, Pmax>.95 [ F{reward{"energy"}<=e} (utility>utility_bound & time_goal) ]), initFeats)
      i.e., for each server configuration, compute the minimal amount of energy required
      to guarantee in 95% of the cases that during one day the server is at least 99% of the time 
      without any package drop (SLA violation).
      For computing such an energy-utility quantile, the number of
      time slots with package drops are counted and normalized
      according to the number of time slots per day. Thus, this model is as
      the plain model above but with an additional module modeling the
      SLA violation counter.

      We have performed the experiment with constant value
      utility_bound=99, i.e., command-line argument
        -const 'utility_bound=99'

These experiments have been carried out using the MTBDD engine and
with the following parameters:
  -mtbdd -cuddmaxmem 10g -javamaxmem 20g -maxiters 9999999




Obligation formulas for LTL->WDBA translation
---------------------------------------------

In the file obligation.ltl you'll find the LTL formulas from the
benchmark set that were detected to be syntactically safe, in the LBT
prefix notation, one formula per line. You can construct the automata
with

PRISM_MAINCLASS=automata.LTL2WDBA prism/bin/prism 'U a b' -

where 'U a b' is the LTL formula.

Using

PRISM_MAINCLASS=jltl2dstar.Jltl2dstarCmdLine prism/bin/prism --ltl 'U a b' -

you can obtain the automaton using the standard Java implementation of
ltl2dstar included in PRISM.


The file obligation.rabinizer.ltl contains the same formulas, but in a
format that can be parsed by Rabinizer, which is available at
https://www7.in.tum.de/~kretinsk/rabinizer3.html

The formulas were converted using Spot's ltlfilt tool with

ltlfilt --full-parentheses --lbt-input --unabbreviate="eiMRW"


To invoke Rabinizer, you can use

java -jar /path/to/rabinizer3.1.jar -silent -auto=sr -format=hoa -in=formula -out=std 'formula'


using Pkg
Pkg.add("POMDPModelChecking")
Pkg.add("Cairo")
Pkg.add("Spot")
Pkg.add("RockSample")
Pkg.add("SARSOP")

using POMDPs
using Spot
using POMDPModelChecking
using RockSample
using SARSOP

pomdp = RockSamplePOMDP{2}(map_size=(4,4), 
                        rocks_positions=[(2,3), (3,1)])


#prop = ltl"F good_rock & G !bad_rock & F exit" 
prop = ltl"F good_rock " 

# Define the labeling function which tells which proposition hold true in a given state
# For the rock sample problem, good_rock holds true if the robot is on a good rock location 
# and take the action `sample` (a=5)
# similarly, bad_rock holds true if the robot samples a bad rock
# The exit proposition is true if the robot reached a terminal state
function POMDPModelChecking.labels(pomdp::RockSamplePOMDP, s::RSState, a::Int64)
    if a == RockSample.BASIC_ACTIONS_DICT[:sample] && in(s.pos, pomdp.rocks_positions) # sample 
        rock_ind = findfirst(isequal(s.pos), pomdp.rocks_positions) # slow ?
        if s.rocks[rock_ind]
            return (:good_rock,)
        else
            return (:bad_rock,)
        end
    end
    if isterminal(pomdp, s)
        return (:exit,)
    end
    return ()
end

solver = ModelCheckingSolver(property=prop,
                             solver=SARSOPSolver(precision=1e-3, timeout=5), verbose=true)
policy = solve(solver, pomdp);



using Random
using BeliefUpdaters
using POMDPSimulators
using POMDPGifs

# first simulate the product pomdp
rng = MersenneTwister(2)
up = DiscreteUpdater(policy.problem)
b0 = initialize_belief(up, initialstate(policy.problem))
hr = HistoryRecorder(max_steps=50, rng=rng)
product_hist = simulate(hr, policy.problem, policy, up, b0);

# create a new history with the pomdp state and action, to be replayed and visualized
hist = SimHistory([(s=s.s, a=a) for (s, a) in eachstep(product_hist, "(s,a)")], 
                  discount(pomdp), nothing, nothing)
import Cairo
makegif(pomdp, hist, filename="rocksample.gif", spec="(s,a)");



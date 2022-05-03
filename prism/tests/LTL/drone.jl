using Pkg
Pkg.add("POMDPModelChecking")
Pkg.add("Cairo")
Pkg.add("Spot")
Pkg.add("DroneSurveillance")
Pkg.add("SARSOP")

using POMDPs
using Spot
using POMDPModelChecking
using RockSample
using SARSOP
using POMDPs
using POMDPModelChecking
using Spot
using DroneSurveillance
using SARSOP

pomdp = DroneSurveillancePOMDP(size=(5,5), camera=PerfectCam())

prop = ltl"!detected U b"

function POMDPModelChecking.labels(pomdp::DroneSurveillancePOMDP, s::DSState, a::Int64)
    if s.quad == pomdp.region_B
        return (:b,)
    elseif s.quad == s.agent && !(isterminal(pomdp, s))
        return (:detetected,)
    else
        return ()
    end
end

solver = ModelCheckingSolver(property = prop, 
                             solver = SARSOPSolver(precision=1e-2, timeout=20), verbose=true)

policy = solve(solver, pomdp);


## Simulation and rendering 
using Random
using BeliefUpdaters
using POMDPSimulators
using POMDPGifs

# run the simulation in the product POMDP, policy.problem
rng = MersenneTwister(3)
up = DiscreteUpdater(policy.problem)
b0 = initialize_belief(up, initialstate_distribution(policy.problem))
hr = HistoryRecorder(max_steps=20, rng=rng)
product_hist = simulate(hr, policy.problem, policy, up, b0);

# create a new history with the pomdp state and action, to be replayed and visualized
hist = SimHistory([(s=s.s, a=a) for (s, a) in eachstep(product_hist, "(s,a)")], discount(pomdp), nothing, nothing)
import Cairo
makegif(pomdp, hist, filename="drone_surveillance.gif", spec="(s,a)");


package imdpcomp;

import parser.Values;
import parser.ast.Expression;

import java.util.ArrayList;

import static explicit.ConstructModel.*;
import static explicit.ConstructModel.CompositionType.*;
import static imdpcomp.Experiment.IntervalAbstractionMode.*;
import static imdpcomp.Experiment.ParameterTying.*;

public class Experiment {
    public Model model;
    public Type type;
    public String modelFile;
    public String certainModelFile; //TODO: replace this with UMDP
    public String dtmcSpec;
    public String udtmcSpec;
    public String spec;
    public String robustSpec;
    public String optimisticSpec;
    public Expression dtmcSpec_bisim;
    public Expression spec_bism;
    public Expression robustSpec_bisim;
    public Expression optimisticSpec_bisim;
    public Values parameterValues = new Values();
    public Values identParameters = new Values();
    public Values exactValues = new Values();
    public ParameterTying tieParameters = FULL_TYING;
    public boolean factored = false;
    public CompositionType compositionType = INTERVAL_PRODUCT;
    public double error_tolerance = 0.999;
    public double strategyWeight = 0.9;
    public int seed = 5;
    public int iterations = 1_00_000;
    public int max_episode_length = 50;
    public int multiplier = 5;
    public int maxVIIters = 20000;
    public boolean errorOnNonConvergence = true;
    public ArrayList<Integer> resultIterations = new ArrayList<>();
    public boolean useParametricConvex = true;
    public boolean doBisim = false;
    public int obbtMaxIters = 0;
    public double obbtEps = 10e-6;
    public boolean useDTMCLP = true;
    public boolean useLPToIntervals = false;
    public IntervalAbstractionMode intervalAbstractionMode = FAST;
    public int exprBoundWorkers = 8;


    public enum ParameterTying {
        NO_TYING,
        FULL_TYING,
        DEPENDENCY_TYING // Only relevant for factored
    }

    public enum IntervalAbstractionMode {
        EXACT,
        FAST
    }

    public Experiment(Model model){
        this.setModel(model);
    }

    public Experiment(Model model, CompositionType type){
        this.setModel(model);
        this.compositionType = type;
    }

    public Experiment setIntervalAbstractionMode(IntervalAbstractionMode mode){
        this.intervalAbstractionMode = mode;
        return this;
    }

    public enum Model {
        AIRCRAFT,
        AIRCRAFT_MULTI_SLIP,
        LAKE_SWARM,
        LAKE_SWARM_MULTI_SLIP,
        COIN,
        CSMA,
        RABIN,
        CHAIN,
        CHAIN_MULTI,
        CHAIN_MULTI_SINGLE,
        CHAIN_CONVEX,
        DICE_2,
        DICE_3,
        HERMAN_3,
        DRONE,
        DRONE_MULTI,
        DRONE_MULTI_2,
        STOCK_TRADING_2_2,
        STOCK_TRADING_3_2,
        STOCK_TRADING_2_3,
        STOCK_TRADING_3_3,
        SYSADMIN,
        BETTING_GAME_CONVEX,
        BETTING_GAME_CONVEX_ADAPTIVE,
        SYSADMIN_CONVEX,
        GRID_MIXTURE_1,
        GRID_MIXTURE_STORM,
        GRID_MIXTURE_LAVA,
        ENGAGEMENT,
        ENGAGEMENT_ADAPTIVE,
        ENGAGEMENT_ADAPTIVE_5,
        ENGAGEMENT_ADAPTIVE_10,
        ENGAGEMENT_ADAPTIVE_100,
        ENGAGEMENT_ADAPTIVE_1000,
        KEY_DOOR_MAZE,
        ROUTER,
        DRONE_MIXTURE,
        DRONE_MIXTURE_STEPS,
        SAV2,
        SAV2_ADAPTIVE,
        SAV2_ADAPTIVE_5,
        SAV2_ADAPTIVE_100,
        AIRCRAFT_MIXTURE_ONEMOD,
        AIRCRAFT_MIXTURE_ONEMOD_ADAPTIVE,
        EPIDEMIC,
        SIMPLE_BISIM,
        SIMPLE_BISIM_MDP,
        ROUTING_BISIM,
        PNUELI_ZUCK,
        TEST_BISIM,
        CROWDS,
        CROWDS_PARAM,
        BRP,
        EGL,
        NAND,
        GLIDER,
        BETTING_GAME_PARALLEL,
        TEST_DTMC
    }

    public enum Type {
        REACH,
        REWARD
    }

    public Experiment setCompositonType(CompositionType type){
        this.compositionType = type;
        return this;
    }

    public Experiment setValues(Values values) {
        this.parameterValues = values;
        return this;
    }

    public Experiment setSingleValue(String name, Object value) {
        this.parameterValues.setValue(name, value);
        return this;
    }

    public Experiment setTieParameters(ParameterTying tieParameters) {
        this.tieParameters = tieParameters;
        return this;
    }

    public Experiment setExactValues(Values values) {
        this.exactValues = values;
        return this;
    }

    public Experiment setParametricConvex(boolean useParametricConvex) {
        this.useParametricConvex = useParametricConvex;
        return this;
    }

    public Experiment useBisimulation(boolean useBisimulation) {
        this.doBisim = useBisimulation;
        return this;
    }

    public Experiment useOBBT(int obbtMaxIters){
        this.obbtMaxIters = obbtMaxIters;
        return this;
    }

    public Experiment useLPToIMDP(boolean useLPToIMDP) {
        this.useLPToIntervals = useLPToIMDP;
        return this;
    }

    public boolean resultIteration(int i) {
        return this.resultIterations.contains(i);
    }

    public Experiment setModel(Model model) {
        this.model = model;
        switch (model) {
            case AIRCRAFT -> {
                this.modelFile = "../models/aircraft_collision/aircraft_10x20_resolution_3.prism";
                this.certainModelFile = "../models/aircraft_collision/aircraft_10x20_resolution_3_certain.prism";
                this.robustSpec = "Pmaxmin=? [!\"collision\" U \"goal\"]";
                this.optimisticSpec = "Pmaxmax=? [!\"collision\" U \"goal\"]";
                this.dtmcSpec = "P=? [!\"collision\" U \"goal\"]";
                this.spec = "Pmax=? [!\"collision\" U \"goal\"]";
                this.type = Type.REACH;
                this.max_episode_length = 11;

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.02);
                this.parameterValues.addValue("r", 0.80);
                this.parameterValues.addValue("p", 0.2);
                this.parameterValues.addValue("maxX", 15);
                this.parameterValues.addValue("maxY", 15);
                this.parameterValues.addValue("d2", 0.1);
                this.parameterValues.addValue("d3", 0.05);
                this.parameterValues.addValue("drift1", 0.05);
                this.parameterValues.addValue("drift2", 0.05);
                this.parameterValues.addValue("drift3", 0.05);
            }

            case AIRCRAFT_MULTI_SLIP -> {
                this.modelFile = "../models/aircraft_collision_multislip/aircraft_10x20_resolution_3.prism";
                this.certainModelFile = "../models/aircraft_collision_multislip/aircraft_10x20_resolution_3_certain.prism";
                this.robustSpec = "Pmaxmin=? [!\"collision\" U \"goal\"]";
                this.optimisticSpec = "Pmaxmax=? [!\"collision\" U \"goal\"]";
                this.dtmcSpec = "P=? [!\"collision\" U \"goal\"]";
                this.spec = "Pmax=? [!\"collision\" U \"goal\"]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.02);
                this.parameterValues.addValue("r", 0.80);
                this.parameterValues.addValue("p", 0.2);
                this.parameterValues.addValue("maxX", 15);
                this.parameterValues.addValue("maxY", 10);
            }

            case LAKE_SWARM -> {
                this.modelFile = "../models/lake/frozen_lake_swarm_eps.prism";
                this.certainModelFile = "../models/lake/frozen_lake_swarm_eps_certain.prism";
                this.robustSpec = "Rminmax=? [ F goal ]";
                this.optimisticSpec = "Rminmin=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";
                this.spec = "Rmin=? [ F goal ]";
                this.type = Type.REWARD;

                this.max_episode_length = 50;

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.05);
                this.parameterValues.addValue("N", 10);
                this.parameterValues.addValue("M", 6);
                this.parameterValues.addValue("p", 0.3);
            }

            case LAKE_SWARM_MULTI_SLIP -> {
                this.modelFile = "../models/lake_multislip_large9/frozen_lake_swarm_eps.prism";
                this.certainModelFile = "../models/lake_multislip_large9/frozen_lake_swarm_eps_certain.prism";
                this.robustSpec = "Rminmax=? [ F goal ]";
                this.optimisticSpec = "Rminmin=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";
                this.spec = "Rmin=? [ F goal ]";
                this.type = Type.REWARD;

                this.max_episode_length = 100;

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.04);
                this.parameterValues.addValue("N", 10);
                this.parameterValues.addValue("M", 6);
                this.parameterValues.addValue("p", 0.45);

            }

            case STOCK_TRADING_2_2 -> {
                this.modelFile = "../models/stockmarket/stock_trading_2_2.pm";
                this.certainModelFile = "../models/stockmarket/stock_trading_2_2.pm";
                this.robustSpec = "Rmaxmin=? [ F goal ]";
                this.optimisticSpec = "Rmaxmax=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";
                this.spec = "Rmax=? [ F goal ]";
                this.type = Type.REWARD;

                this.max_episode_length = 11;

                // Set Parameter Values
                this.parameterValues.addValue("T", 10);
                this.parameterValues.addValue("BASE", 0.1);
                this.parameterValues.addValue("SCALE", 0.8);
            }

            case STOCK_TRADING_3_2 -> {
                this.modelFile = "../models/stockmarket/stock_trading_3_2.pm";
                this.certainModelFile = "../models/stockmarket/stock_trading_3_2.pm";
                this.robustSpec = "Rmaxmin=? [ F goal ]";
                this.optimisticSpec = "Rmaxmax=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";
                this.spec = "Rmax=? [ F goal ]";
                this.type = Type.REWARD;

                this.max_episode_length = 11;

                // Set Parameter Values
                this.parameterValues.addValue("T", 10);
                this.parameterValues.addValue("BASE", 0.1);
                this.parameterValues.addValue("SCALE", 0.8);
            }

            case STOCK_TRADING_2_3 -> {
                this.modelFile = "../models/stockmarket/stock_trading_2_3.pm";
                this.certainModelFile = "../models/stockmarket/stock_trading_2_3.pm";
                this.robustSpec = "Rmaxmin=? [ F goal ]";
                this.optimisticSpec = "Rmaxmax=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";
                this.spec = "Rmax=? [ F goal ]";
                this.type = Type.REWARD;

                this.max_episode_length = 11;

                // Set Parameter Values
                this.parameterValues.addValue("T", 10);
                this.parameterValues.addValue("BASE", 0.1);
                this.parameterValues.addValue("SCALE", 0.8);
            }

            case STOCK_TRADING_3_3 -> {
                this.modelFile = "../models/stockmarket/stock_trading_3_3.pm";
                this.certainModelFile = "../models/stockmarket/stock_trading_3_3.pm";
                this.robustSpec = "Rmaxmin=? [ F goal ]";
                this.optimisticSpec = "Rmaxmax=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";
                this.spec = "Rmax=? [ F goal ]";
                this.type = Type.REWARD;

                this.max_episode_length = 11;

                // Set Parameter Values
                this.parameterValues.addValue("T", 5);
                this.parameterValues.addValue("BASE", 0.1);
                this.parameterValues.addValue("SCALE", 0.8);
            }

            case SYSADMIN -> {
                int N = 5;
                int T = 20;

                this.modelFile = String.format("../models/sysadmin/sysadmin_ring_N%s_T%s.pm", N, T);
                this.certainModelFile = String.format("../models/sysadmin/sysadmin_ring_N%s_T%s.pm", N, T);
                this.robustSpec = "Rmaxmin=? [ F (t_0 = T) ]";
                this.optimisticSpec = "Rmaxmax=? [ F (t_0 = T) ]";
                this.dtmcSpec = "R=? [ F (t_0 = T) ]";
                this.spec = "Rmax=? [ F (t_0 = T) ]";
                this.type = Type.REWARD;

                this.max_episode_length = 6;

                // Set Parameter Values
                this.parameterValues.addValue("T", T);
                this.parameterValues.addValue("N", N);
                this.parameterValues.addValue("p0", 0.1);
                this.parameterValues.addValue("p1", 0.6);
            }

            case SYSADMIN_CONVEX -> {
                int N = 30;

                this.modelFile = String.format("../parametric_convex_models/sys_admin.prism");
                this.certainModelFile = String.format("../parametric_convex_models/sys_admin.prism");
                this.robustSpec = "Pmaxmin=? [ !\"fail\" U \"goal\" ]";
                this.optimisticSpec = "Pmaxmax=? [ !\"fail\" U \"goal\" ]";
                this.dtmcSpec = "P=? [ !\"fail\" U \"goal\" ]";
                this.spec = "Pmax=? [ !\"fail\" U \"goal\" ]";
                this.type = Type.REACH;

                this.max_episode_length = 10;
                this.maxVIIters = 10000000;

                // Set Parameter Values
                this.parameterValues.addValue("N", N);
                this.parameterValues.addValue("p", 1.0 / (2*N));
                this.parameterValues.addValue("q", 1.0 / (2.01*N));
            }


            case COIN -> {
                this.modelFile = "../models/coin/coin4.prism";
                this.certainModelFile = "../models/coin/coin4_certain.prism";
                this.robustSpec = "R{\"flips\"}maxmin=? [ F \"finished\" ]";
                this.dtmcSpec = "R{\"flips\"}=? [ F \"finished\" ]";

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.05);
                this.parameterValues.addValue("p", 0.45);
                this.parameterValues.addValue("K", 2);
            }

            case CSMA -> {
                this.modelFile = "../models/csma/csma3_2.prism";
                this.certainModelFile = "../models/csma/csma3_2_certain.prism";
                this.robustSpec = "R{\"time\"}minmax=? [ F \"all_delivered\" ]";
                this.dtmcSpec = "R{\"time\"}=? [ F \"all_delivered\" ]";

                // Set Parameter Values
                this.parameterValues.addValue("p1", 0.5);
                this.parameterValues.addValue("p2", 0.25);
                this.parameterValues.addValue("eps", 0.1);
            }

            case RABIN -> {
                this.modelFile = "../models/rabin/rabin_rewards_3.prism";
                this.certainModelFile = "../models/rabin/rabin_rewards_3_certain.prism";
                this.robustSpec = "Pmaxmin=?[ !\"one_critical\" U (p1=2) {draw1=1 & !\"one_critical\"}{min} ]";
                this.dtmcSpec = "P=?[ !\"one_critical\" U (p1=2) {draw1=1 & !\"one_critical\"}{min} ]";

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.02); // 0 <= eps <= 0.03125
            }

            case CHAIN -> {
                this.modelFile = "../models/chain/chain_2.prism";
                this.certainModelFile = "../models/chain/chain_2_certain.prism";
                this.robustSpec = "Rminmax=? [F \"goal\"]";
                this.optimisticSpec = "Rminmin=? [F \"goal\"]";
                this.dtmcSpec = "R=? [F \"goal\"]";
                this.spec = "Rmin=? [F \"goal\"]";
                this.type = Type.REWARD;

                // Set Parameter Values
                this.parameterValues.addValue("H", 5);
                this.parameterValues.addValue("p", 0.4);
                this.parameterValues.addValue("q", 0.6);
                this.parameterValues.addValue("r", 0.5);
                this.parameterValues.addValue("eps", 0.02);
            }

            case CHAIN_MULTI -> {
                this.modelFile = "../models/chain/chain_multi_succ.prism";
                this.certainModelFile = "../models/chain/chain_multi_succ_certain.prism";
                this.robustSpec = "Rminmax=? [F \"goal\"]";
                this.optimisticSpec = "Rminmin=? [F \"goal\"]";
                this.dtmcSpec = "R=? [F \"goal\"]";
                this.spec = "Rmin=? [F \"goal\"]";
                this.type = Type.REWARD;

                // Set Parameter Values
                this.parameterValues.addValue("H", 8);
                this.parameterValues.addValue("p", 0.4);
                this.parameterValues.addValue("q", 0.6);
                this.parameterValues.addValue("r", 0.5);
                this.parameterValues.addValue("eps", 0.02);

                this.multiplier = 2;
                this.max_episode_length = 20;
                this.maxVIIters = 100000;
            }

            case CHAIN_MULTI_SINGLE -> {
                this.modelFile = "../models/chain/chain_multi_succ_single.prism";
                this.certainModelFile = "../models/chain/chain_multi_succ_single_certain.prism";
                this.robustSpec = "Rminmax=? [F \"goal\"]";
                this.optimisticSpec = "Rminmin=? [F \"goal\"]";
                this.dtmcSpec = "R=? [F \"goal\"]";
                this.spec = "Rmin=? [F \"goal\"]";
                this.type = Type.REWARD;

                // Set Parameter Values
                this.parameterValues.addValue("H", 9);
                this.parameterValues.addValue("p", 0.6);
                this.parameterValues.addValue("q", 0.4);
                this.parameterValues.addValue("r", 0.5);
                this.parameterValues.addValue("eps", 0.02);
            }

            case CHAIN_CONVEX -> {
                this.modelFile = "../parametric_convex_models/chain_convex.prism";
                this.certainModelFile = "../parametric_convex_models/chain_convex.prism";
                this.robustSpec = "Rminmax=? [F \"goal\"]";
                this.optimisticSpec = "Rminmin=? [F \"goal\"]";
                this.dtmcSpec = "R=? [F \"goal\"]";
                this.spec = "Rmin=? [F \"goal\"]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 20;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.1);
                this.parameterValues.addValue("q", 0.12);
            }

            case BETTING_GAME_CONVEX -> {
                this.modelFile = "../parametric_convex_models/bet_fav.prism";
                this.certainModelFile = "../parametric_convex_models/bet_fav.prism";
                this.robustSpec = "Rmaxmin=? [F \"done\"]";
                this.optimisticSpec = "Rmaxmax=? [F \"done\"]";
                this.dtmcSpec = "R=? [F \"done\"]";
                this.spec = "Rmax=? [F \"done\"]";
                this.type = Type.REWARD;

                this.multiplier = 4;
                this.max_episode_length = 10;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("n", 250);
                this.parameterValues.addValue("p", 0.55);
            }

            case BETTING_GAME_CONVEX_ADAPTIVE -> {
                this.modelFile = "../parametric_convex_models/bet_fav_adaptive.prism";
                this.certainModelFile = "../parametric_convex_models/bet_fav_adaptive.prism";
                this.robustSpec = "Rmaxmin=? [F \"done\"]";
                this.optimisticSpec = "Rmaxmax=? [F \"done\"]";
                this.dtmcSpec = "R=? [F \"done\"]";
                this.spec = "Rmax=? [F \"done\"]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("n", 150);
                this.parameterValues.addValue("p", 0.55);
            }

            case GRID_MIXTURE_1 -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/grid_mixture.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/grid_mixture.prism";
                this.robustSpec = "R{\"steps\"}minmax=? [ F \"goal\" ]";
                this.optimisticSpec = "R{\"steps\"}minmin=? [ F \"goal\" ]";
                this.dtmcSpec = "R{\"steps\"}=? [ F \"goal\" ]";
                this.spec = "R{\"steps\"}min=? [ F \"goal\" ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 30;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.4);
            }

            case GRID_MIXTURE_STORM -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/grid_mixture_storm.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/grid_mixture_storm.prism";
                this.robustSpec = "R{\"total_cost\"}minmax=? [ F \"goal\" ]";
                this.optimisticSpec = "R{\"total_cost\"}minmin=? [ F \"goal\" ]";
                this.dtmcSpec = "R{\"total_cost\"}=? [ F \"goal\" ]";
                this.spec = "R{\"total_cost\"}min=? [ F \"goal\" ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 20;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.4);
            }

            case GRID_MIXTURE_LAVA -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/grid_mixture_lava.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/grid_mixture_lava.prism";
                this.robustSpec = "R{\"total_cost\"}minmax=? [ F \"goal\" ]";
                this.optimisticSpec = "R{\"total_cost\"}minmin=? [ F \"goal\" ]";
                this.dtmcSpec = "R{\"total_cost\"}=? [ F \"goal\" ]";
                this.spec = "R{\"total_cost\"}min=? [ F \"goal\" ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 25;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.4);
            }

            case ENGAGEMENT -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/engagement.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/engagement.prism";
                this.robustSpec = "Rminmax = ? [ F (\"purchase\" | \"churn\") ]";
                this.optimisticSpec = "Rminmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.dtmcSpec = "R = ? [ F (\"purchase\" | \"churn\") ]";
                this.spec = "Rmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 100000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.4);
            }

            case SIMPLE_BISIM -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/simple_bisim.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/simple_bisim.prism";
                this.robustSpec = "Pmaxmin = ? [ F \"cache\"]";
                this.optimisticSpec = "Pmaxmax = ? [ F \"cache\"]";
                this.dtmcSpec = "P = ? [ F \"cache\"]";
                this.spec = "P min= ? [ F \"cache\"]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.2);
                this.parameterValues.addValue("q", 0.3);
                this.parameterValues.addValue("r", 0.4);
            }

            case SIMPLE_BISIM_MDP -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/simple_mdp_bisim.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/simple_mdp_bisim.prism";
                this.robustSpec = "Pmaxmin = ? [ F \"drop\" ]";
                this.optimisticSpec = "Pmaxmax = ? [ F \"drop\"]";
                this.dtmcSpec = "P = ? [ F \"drop\"]";
                this.spec = "P max= ? [ F \"drop\"]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.35);
                this.parameterValues.addValue("q", 0.65);
                this.parameterValues.addValue("r", 0.4);
            }

            case ROUTING_BISIM -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/routing_bisim.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/routing_bisim.prism";
                this.robustSpec = "Pmaxmin = ? [ !\"fail\" U \"goal\" ]";
                this.optimisticSpec = "Pmaxmax = ? [ !\"fail\" U \"goal\" ]";
                this.dtmcSpec = "P = ? [ !\"fail\" U \"goal\" ]";
                this.spec = "Pmax = ? [ !\"fail\" U \"goal\" ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

// sizes
                this.parameterValues.addValue("L", 15);
                this.parameterValues.addValue("M", 500);
                this.parameterValues.addValue("B", 6);

// dynamics
                this.parameterValues.addValue("h",  0.01);
                this.parameterValues.addValue("s",  0.15);

// upper-layer forward (same as your original spirit)
                this.parameterValues.addValue("pL", 0.20);
                this.parameterValues.addValue("pM", 0.12);
                this.parameterValues.addValue("pN", 0.08);

// bottom-layer bucket vectors (sumRL=0.18, sumRM=0.12, sumRN=0.08)
                this.parameterValues.addValue("rL1", 0.060);
                this.parameterValues.addValue("rL2", 0.040);
                this.parameterValues.addValue("rL3", 0.030);
                this.parameterValues.addValue("rL4", 0.025);
                this.parameterValues.addValue("rL5", 0.015);
                this.parameterValues.addValue("rL6", 0.010);

                this.parameterValues.addValue("rM1", 0.045);
                this.parameterValues.addValue("rM2", 0.030);
                this.parameterValues.addValue("rM3", 0.020);
                this.parameterValues.addValue("rM4", 0.015);
                this.parameterValues.addValue("rM5", 0.005);
                this.parameterValues.addValue("rM6", 0.005);

                this.parameterValues.addValue("rN1", 0.030);
                this.parameterValues.addValue("rN2", 0.020);
                this.parameterValues.addValue("rN3", 0.015);
                this.parameterValues.addValue("rN4", 0.010);
                this.parameterValues.addValue("rN5", 0.003);
                this.parameterValues.addValue("rN6", 0.002);
            }

            case TEST_BISIM -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/test_bisim.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/test_bisim.prism";
                this.robustSpec = "Pmaxmin = ? [ F (\"goal\") ]";
                this.optimisticSpec = "Pmaxmax = ? [ F (\"goal\") ]";
                this.dtmcSpec = "P = ? [ F (\"goal\") ]";
                this.spec = "Pmax = ? [ F (\"goal\") ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;
// ===== (1) web_3tier_Kfe6_Kapp6_Kdb4.prism =====
// Sum(fe*) = 0.86  -> 0.14 fallback-to-fail at FE hub
                this.parameterValues.addValue("fe1", 0.17);
                this.parameterValues.addValue("fe2", 0.16);
                this.parameterValues.addValue("fe3", 0.15);
                this.parameterValues.addValue("fe4", 0.14);
                this.parameterValues.addValue("fe5", 0.13);
                this.parameterValues.addValue("fe6", 0.11);

// Sum(ap*) = 0.78  -> 0.22 fallback-to-fail at App hub
                this.parameterValues.addValue("ap1", 0.16);
                this.parameterValues.addValue("ap2", 0.15);
                this.parameterValues.addValue("ap3", 0.14);
                this.parameterValues.addValue("ap4", 0.12);
                this.parameterValues.addValue("ap5", 0.11);
                this.parameterValues.addValue("ap6", 0.10);

// Sum(db*) = 0.78  -> 0.22 fallback-to-fail at DB hub
                this.parameterValues.addValue("db1", 0.24);
                this.parameterValues.addValue("db2", 0.22);
                this.parameterValues.addValue("db3", 0.18);
                this.parameterValues.addValue("db4", 0.14);

// Within-tier success once a node is picked (high but < 1)
                this.parameterValues.addValue("qFe", 0.97);
                this.parameterValues.addValue("qAp", 0.96);
                this.parameterValues.addValue("qDb", 0.95);
            }

            case PNUELI_ZUCK -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/pnueli-zuck.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/pnueli-zuck.prism";
                this.robustSpec = "Pmaxmin = ? [ !\"cs\" U \"contend\" ]";
                this.optimisticSpec = "Pmaxmax = ? [ !\"cs\" U \"contend\" ]";
                this.dtmcSpec = "P = ? [ !\"cs\" U \"contend\" ]";
                this.spec = "Pmax = ? [ !\"cs\" U \"contend\" ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                this.parameterValues.addValue("p_fast",  0.3);

            }

            case CROWDS -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/crowd.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/crowd.prism";
                this.robustSpec = "Pmaxmin = ? [ F (observe0 > 1) ]";
                this.optimisticSpec = "Pmaxmax = ? [ F (observe0 > 1) ]";
                this.dtmcSpec = "P = ? [ F (observe0 > 1) ]";
                this.spec = "Pmax = ? [ F (observe0 > 1) ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 100000;

                this.parameterValues.addValue("PF",  0.8);
                this.parameterValues.addValue("badC",  0.091);

            }

            case CROWDS_PARAM -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/crowd_param.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/crowd_param.prism";
                this.robustSpec = "Pmaxmin = ? [ F (observe0 > 1) ]";
                this.optimisticSpec = "Pmaxmax = ? [ F (observe0 > 1) ]";
                this.dtmcSpec = "P = ? [ F (observe0 > 1) ]";
                this.spec = "Pmax = ? [ F (observe0 > 1) ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 1000000;

                this.parameterValues.addValue("CrowdSize", 2);
                this.parameterValues.addValue("PF",  0.8);
                this.parameterValues.addValue("badC",  0.091);
                this.parameterValues.addValue("p_half_1",  1.0/2.0);
                this.parameterValues.addValue("p_half_2",  1.0/2.0);
                this.parameterValues.addValue("p_third_1",  1.0/3.0);
                this.parameterValues.addValue("p_third_2",  1.0/3.0);
                this.parameterValues.addValue("p_third_3",  1.0/3.0);
                this.parameterValues.addValue("p_fourth",  1.0/4.0);
                this.parameterValues.addValue("p_fith",  1.0/5.0);
                this.parameterValues.addValue("p_tenth",  1.0/10.0);
                this.parameterValues.addValue("p_fifteenth",  1.0/15.0);
                this.parameterValues.addValue("p_twenty",  1.0/20.0);

            }

            case BRP -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/brp.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/brp.prism";
                this.robustSpec = "Pmaxmin = ? [ F s=5 & srep=2 ]";
                this.optimisticSpec = "Pmaxmax = ? [ F s=5 & srep=2 ]";
                this.dtmcSpec = "P = ? [ F s=5 & srep=2 ]";
                this.spec = "Pmax = ? [ F s=5 & srep=2 ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 100000;

                this.parameterValues.addValue("pL",  0.69);
                this.parameterValues.addValue("pK",  0.6);

            }

            case EGL -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/egl.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/egl.prism";
                this.robustSpec = "Pmaxmin = ? [ F !\"knowA\" & \"knowB\" ]";
                this.optimisticSpec = "Pmaxmax = ? [ F !\"knowA\" & \"knowB\" ]";
                this.dtmcSpec = "P = ? [F !\"knowA\" & \"knowB\" ]";
                this.spec = "Pmax = ? [ F !\"knowA\" & \"knowB\" ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 100000;

                this.parameterValues.addValue("p_1",  0.5);
                this.parameterValues.addValue("p_2",  0.5);
            }

            case NAND -> {
                this.modelFile = "../parametric_convex_models/bisimulation_models/nand.prism";
                this.certainModelFile = "../parametric_convex_models/bisimulation_models/nand.prism";
                this.robustSpec = "Pmaxmin = ? [ F s=4 & z/5<0.1 ]";
                this.optimisticSpec = "Pmaxmax = ? [ F s=4 & z/5<0.1 ]";
                this.dtmcSpec = "P = ? [ F s=4 & z/5<0.1 ]";
                this.spec = "Pmax = ? [ F s=4 & z/5<0.1 ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 100000;

                this.parameterValues.addValue("prob1",  0.9);
                this.parameterValues.addValue("perr",  0.02);
            }


            case ENGAGEMENT_ADAPTIVE -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive.prism";
                this.robustSpec = "Rminmax = ? [ F (\"purchase\" | \"churn\") ]";
                this.optimisticSpec = "Rminmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.dtmcSpec = "R = ? [ F (\"purchase\" | \"churn\") ]";
                this.spec = "Rmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("L", 300);
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.4);
            }

            case ENGAGEMENT_ADAPTIVE_5 -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_5.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_5.prism";
                this.robustSpec = "Rminmax = ? [ F (\"purchase\" | \"churn\") ]";
                this.optimisticSpec = "Rminmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.dtmcSpec = "R = ? [ F (\"purchase\" | \"churn\") ]";
                this.spec = "Rmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("L", 300);
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.1);
                this.parameterValues.addValue("theta4", 0.25);
            }

            case ENGAGEMENT_ADAPTIVE_10 -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_10.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_10.prism";
                this.robustSpec = "Rminmax = ? [ F (\"purchase\" | \"churn\") ]";
                this.optimisticSpec = "Rminmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.dtmcSpec = "R = ? [ F (\"purchase\" | \"churn\") ]";
                this.spec = "Rmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("L", 20);
                this.parameterValues.addValue("theta1", 0.08);
                this.parameterValues.addValue("theta2", 0.1);
                this.parameterValues.addValue("theta3", 0.11);
                this.parameterValues.addValue("theta4", 0.05);
                this.parameterValues.addValue("theta5", 0.04);
                this.parameterValues.addValue("theta6", 0.06);
                this.parameterValues.addValue("theta7", 0.12);
                this.parameterValues.addValue("theta8", 0.14);
                this.parameterValues.addValue("theta9", 0.02);
            }

            case ENGAGEMENT_ADAPTIVE_100 -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_100.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_100.prism";
                this.robustSpec = "Rminmax = ? [ F (\"purchase\" | \"churn\") ]";
                this.optimisticSpec = "Rminmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.dtmcSpec = "R = ? [ F (\"purchase\" | \"churn\") ]";
                this.spec = "Rmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.identParameters.addValue("L", 300);
                this.parameterValues.addValue("L", 300);
                for (int i = 1; i < 100; i++) {
                    this.parameterValues.addValue("theta" + i, 0.009);
                }
            }

            case ENGAGEMENT_ADAPTIVE_1000 -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_1000.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/engagement_adaptive_1000.prism";
                this.robustSpec = "Rminmax = ? [ F (\"purchase\" | \"churn\") ]";
                this.optimisticSpec = "Rminmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.dtmcSpec = "R = ? [ F (\"purchase\" | \"churn\") ]";
                this.spec = "Rmin = ? [ F (\"purchase\" | \"churn\") ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.identParameters.addValue("L", 20);
                this.parameterValues.addValue("L", 20);
                for (int i = 0; i < 1000; i++) {
                    this.parameterValues.addValue("theta" + i, 0.0009);
                }
            }



            case GLIDER -> {
                this.modelFile = "../parametric_convex_models/polynomial_mdps/glider.prism";
                this.certainModelFile = "../parametric_convex_models/polynomial_mdps/glider.prism";
                this.robustSpec = "Rminmax = ? [ F \"goal\"]";
                this.optimisticSpec = "Rminmin = ? [ F \"goal\"]";
                this.dtmcSpec = "R = ? [ F \"goal\"]";
                this.spec = "Rmin = ? [ F \"goal\"]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("w", 21);
                this.parameterValues.addValue("h", 17);
                this.parameterValues.addValue("theta_h", 0.3);
                this.parameterValues.addValue("theta_v", 0.7);
            }

            case BETTING_GAME_PARALLEL -> {
                this.modelFile = "../parametric_convex_models/polynomial_mdps/bet_parallel.prism";
                this.certainModelFile = "../parametric_convex_models/polynomial_mdps/bet_parallel.prism";
                this.robustSpec = "Rmaxmin=? [F \"done\"]";
                this.optimisticSpec = "Rmaxmax=? [F \"done\"]";
                this.dtmcSpec = "R=? [F \"done\"]";
                this.spec = "Rmax=? [F \"done\"]";
                this.type = Type.REWARD;

//                this.robustSpec = "Pmaxmin=? [F money + money2 >= 25]";
//                this.optimisticSpec = "Pmaxmax=? [F money + money2 >= 25]";
//                this.dtmcSpec = "P=? [F money + money2 >= 25]";
//                this.spec = "Pmax=? [F money + money2 >= 25]";
//                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 10;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("p_1", 0.55);
                this.parameterValues.addValue("p_2", 0.53);
            }

            case KEY_DOOR_MAZE -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/key-door-maze.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/key-door-maze.prism";
                this.robustSpec = "Rminmax = ? [ F \"goal\" ]";
                this.optimisticSpec = "Rminmin = ? [ F \"goal\" ]";
                this.dtmcSpec = "R=? [ F \"goal\" ]";
                this.spec = "Rmin = ? [ F\"goal\" ]";
                this.type = Type.REWARD;

//                this.robustSpec = "Pmaxmin = ? [ !\"storm\" U \"goal\" ]";
//                this.optimisticSpec = "Pmaxmax = ? [ !\"storm\" U \"goal\" ]";
//                this.dtmcSpec = "P=? [ !\"storm\" U \"goal\" ]";
//                this.spec = "Pmax = ? [ !\"storm\" U \"goal\" ]";
//                this.type = Type.REACH;

                this.multiplier = 3;
                this.max_episode_length = 200;
                this.maxVIIters = 1000000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.11);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
                this.parameterValues.addValue("theta4", 0.3);
            }

            case EPIDEMIC -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/epidemic.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/epidemic.prism";
                this.robustSpec = "Pminmax = ? [ !\"outbreak\" U \"eradicated\" ]";
                this.optimisticSpec = "Pminmin = ? [ !\"outbreak\" U \"eradicated\" ]";
                this.dtmcSpec = "P = ? [ !\"outbreak\" U \"eradicated\" ]";
                this.spec = "Pmin = ? [ !\"outbreak\" U \"eradicated\" ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 400;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.45);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.1);
                this.parameterValues.addValue("theta4", 0.15);

            }

            case TEST_DTMC -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/test_dtmc.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/test_dtmc.prism";
                this.robustSpec = "Pmaxmin = ? [ F (\"goal\") ]";
                this.optimisticSpec = "Pmaxmax = ? [ F (\"goal\") ]";
                this.dtmcSpec = "P = ? [ F (\"goal\") ]";
                this.spec = "Pmax = ? [ F (\"goal\") ]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 3;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.6);
                this.parameterValues.addValue("theta2", 0.2);
            }


            case DRONE_MIXTURE -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/drone_mixture.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/drone_mixture.prism";
                this.robustSpec = "Pmaxmin=? [!crash U target]";
                this.optimisticSpec = "Pmaxmax=? [!crash U target]";
                this.dtmcSpec = "P=? [!crash U target]";
                this.spec = "Pmax=? [!crash U target]";
                this.type = Type.REACH;

                this.multiplier = 2;
                this.max_episode_length = 100;
                this.maxVIIters = 1000000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.1);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
                this.parameterValues.addValue("theta4", 0.3);
            }

            case DRONE_MIXTURE_STEPS -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/drone_mixture_steps.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/drone_mixture_steps.prism";
                this.robustSpec = "Rminmax=? [F target]";
                this.optimisticSpec = "Rminmin=? [F target]";
                this.dtmcSpec = "R=? [F target]";
                this.spec = "Rmin=? [F target]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 50;
                this.maxVIIters = 1000000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.1);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
                this.parameterValues.addValue("theta4", 0.3);
            }

            case SAV2-> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/sav_mixture.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/sav_mixture.prism";

                this.spec = "Pmax=? [!(\"Crash\") U (\"Target\")]";
                this.robustSpec = "Pmaxmin=? [!(\"Crash\") U (\"Target\")]";
                this.optimisticSpec = "Pmaxmax=? [!(\"Crash\") U (\"Target\")]";
                this.dtmcSpec = "P=? [!(\"Crash\") U (\"Target\")]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("Xsize", 45);
                this.parameterValues.addValue("Ysize", 45);
                this.parameterValues.addValue("theta1", 0.39);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
            }

            case SAV2_ADAPTIVE-> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/sav_mixture_adaptive.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/sav_mixture_adaptive.prism";

                this.spec = "Pmax=? [!(\"Crash\") U (\"Target\")]";
                this.robustSpec = "Pmaxmin=? [!(\"Crash\") U (\"Target\")]";
                this.optimisticSpec = "Pmaxmax=? [!(\"Crash\") U (\"Target\")]";
                this.dtmcSpec = "P=? [!(\"Crash\") U (\"Target\")]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("Xsize", 45);
                this.parameterValues.addValue("Ysize", 45);
                this.parameterValues.addValue("theta1", 0.4);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
            }

            case SAV2_ADAPTIVE_5-> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/sav_mixture_adaptive_5.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/sav_mixture_adaptive_5.prism";

                this.spec = "Pmax=? [!(\"Crash\") U (\"Target\")]";
                this.robustSpec = "Pmaxmin=? [!(\"Crash\") U (\"Target\")]";
                this.optimisticSpec = "Pmaxmax=? [!(\"Crash\") U (\"Target\")]";
                this.dtmcSpec = "P=? [!(\"Crash\") U (\"Target\")]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("Xsize", 45);
                this.parameterValues.addValue("Ysize", 45);
                this.parameterValues.addValue("theta1", 0.4);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
                this.parameterValues.addValue("theta4", 0.14);
            }

            case SAV2_ADAPTIVE_100-> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/sav_mixture_adaptive_100.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/sav_mixture_adaptive_100.prism";

                this.spec = "Pmax=? [!(\"Crash\") U (\"Target\")]";
                this.robustSpec = "Pmaxmin=? [!(\"Crash\") U (\"Target\")]";
                this.optimisticSpec = "Pmaxmax=? [!(\"Crash\") U (\"Target\")]";
                this.dtmcSpec = "P=? [!(\"Crash\") U (\"Target\")]";
                this.type = Type.REACH;

                // Set Parameter Values

                // Set Parameter Values
                this.identParameters.addValue("Xsize", 12);
                this.identParameters.addValue("Ysize", 12);
                this.parameterValues.addValue("Xsize", 12);
                this.parameterValues.addValue("Ysize", 12);

                for (int i = 0; i < 100; i++) {
                    this.parameterValues.addValue("theta" + i, 0.009);
                }

            }

            case AIRCRAFT_MIXTURE_ONEMOD -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/aircraft_mixture_onemod.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/aircraft_mixture_onemod.prism";

                this.spec = "Pmax=? [!collision U \"goal\"]";
                this.robustSpec = "Pmaxmin=? [!collision U \"goal\"]";
                this.optimisticSpec = "Pmaxmax=? [!collision U \"goal\"]";
                this.dtmcSpec = "P=?  [!collision U \"goal\"]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.369);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.3);
            }

            case AIRCRAFT_MIXTURE_ONEMOD_ADAPTIVE -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/aircraft_mixture_onemod_adaptive.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/aircraft_mixture_onemod_adaptive.prism";

                this.spec = "Pmax=? [!collision U \"goal\"]";
                this.robustSpec = "Pmaxmin=? [!collision U \"goal\"]";
                this.optimisticSpec = "Pmaxmax=? [!collision U \"goal\"]";
                this.dtmcSpec = "P=?  [!collision U \"goal\"]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.4);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
            }

            case DICE_2 -> {
                this.modelFile = "../models/dice/dice2.prism";
                this.certainModelFile = "../models/dice/dice2_certain.prism";
                this.robustSpec = "Rminmax=? [ F (s1=7 & s2 = 7) ]";
                this.optimisticSpec = "Rminmin=? [ F (s1=7 & s2 = 7) ]";
                this.spec = "Rmin=? [ F (s1=7 & s2 = 7) ]";
                this.dtmcSpec = "R=? [ F (s1=7 & s2 = 7) ]";
                this.type = Type.REWARD;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.4);
                this.parameterValues.addValue("eps", 0.1);
            }

            case DICE_3 -> {
                this.modelFile = "../models/dice/dice3.prism";
                this.certainModelFile = "../models/dice/dice3_certain.prism";
                this.robustSpec = "Rminmax=? [ F (s1=7 & s2 = 7 & s3 = 7) ]";
                this.optimisticSpec = "Rminmin=? [ F (s1=7 & s2 = 7 & s3 = 7) ]";
                this.spec = "Rmin=? [ F (s1=7 & s2 = 7 & s3 = 7) ]";
                this.dtmcSpec = "R=? [ F (s1=7 & s2 = 7 & s3 = 7) ]";
                this.type = Type.REWARD;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.22);
                this.parameterValues.addValue("q", 0.4);
                this.parameterValues.addValue("eps", 0.1);
            }

            case HERMAN_3 -> {
                this.modelFile = "../models/herman/herman7.prism";
                this.certainModelFile = "../models/herman/herman7_certain.prism";
                this.robustSpec = "Rminmax=? [ F \"stable\"]";
                this.dtmcSpec = "R=? [ F \"stable\" ]";

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.3333333);
                this.parameterValues.addValue("eps", 0.1);
            }

            case DRONE -> {
                this.modelFile = "../models/drone/drone.prism";
                this.certainModelFile = "../models/drone/drone_certain.prism";
                this.robustSpec = "Pmaxmin=? [!crash U target]";
                this.optimisticSpec = "Pmaxmax=? [!crash U target]";
                this.dtmcSpec = "P=? [!crash U target]";
                this.spec = "Pmax=? [!crash U target]";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.2);
                this.parameterValues.addValue("eps", 0.1);
            }

            case DRONE_MULTI -> {
                this.modelFile = "../models/drone_multislip/drone.prism";
                this.certainModelFile = "../models/drone_multislip/drone_certain.prism";
                this.robustSpec = "Pmaxmin=? [!crash U target]";
                this.optimisticSpec = "Pmaxmax=? [!crash U target]";
                this.dtmcSpec = "P=? [!crash U target]";
                this.spec = "Pmax=? [!crash U target]";
                this.type = Type.REACH;

                this.max_episode_length = 50;
                this.maxVIIters = 3500;
                this.multiplier = 5;

                // Set Parameter V alues
                this.parameterValues.addValue("MAXX", 5);
                this.parameterValues.addValue("MAXY", 5);
                this.parameterValues.addValue("MAXZ", 5);
                this.parameterValues.addValue("p", 0.3);
                this.parameterValues.addValue("eps", 0.029);
            }

            case DRONE_MULTI_2 -> {
                this.modelFile = "../models/drone_multislip/drone_2.prism";
                this.certainModelFile = "../models/drone_multislip/drone_certain.prism";
                this.robustSpec = "Pmaxmin=? [!crash U target]";
                this.dtmcSpec = "P=? [!crash U target]";

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.3);
                this.parameterValues.addValue("eps", 0.03);
            }


        }
        return this;
    }
}

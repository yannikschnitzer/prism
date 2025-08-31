package imdpcomp;

import parser.Values;

import java.util.ArrayList;

import static explicit.ConstructModel.*;
import static explicit.ConstructModel.CompositionType.*;
import static imdpcomp.Experiment.ParameterTying.*;

public class Experiment {
    public Model model;
    public Type type;
    public String modelFile;
    public String certainModelFile; //TODO: replace this with UMDP
    public String dtmcSpec;
    public String spec;
    public String robustSpec;
    public String optimisticSpec;
    public Values parameterValues = new Values();
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
    public ArrayList<Integer> resultIterations = new ArrayList<>();
    public boolean useParametricConvex = true;

    public enum ParameterTying {
        NO_TYING,
        FULL_TYING,
        DEPENDENCY_TYING // Only relevant for factored
    }

    public Experiment(Model model){
        this.setModel(model);
    }

    public Experiment(Model model, CompositionType type){
        this.setModel(model);
        this.compositionType = type;
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
        KEY_DOOR_MAZE,
        ROUTER,
        DRONE_MIXTURE,
        DRONE_MIXTURE_STEPS,
        SAV2,
        SAV2_ADAPTIVE,
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

    public Experiment setExactValues(Values values) {
        this.exactValues = values;
        return this;
    }

    public Experiment setParametricConvex(boolean useParametricConvex) {
        this.useParametricConvex = useParametricConvex;
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
                int N = 10;
                int T = 8;

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
                int N = 10;

                this.modelFile = String.format("../parametric_convex_models/sys_admin.prism");
                this.certainModelFile = String.format("../parametric_convex_models/sys_admin.prism");
                this.robustSpec = "Rmaxmin=? [ F \"fail\" ]";
                this.optimisticSpec = "Rmaxmax=? [ F \"fail\" ]";
                this.dtmcSpec = "R=? [ F \"fail\" ]";
                this.spec = "Rmax=? [ F \"fail\" ]";
                this.type = Type.REWARD;

                this.max_episode_length = 10;

                // Set Parameter Values
                this.parameterValues.addValue("p", 0.04);
                this.parameterValues.addValue("q", 0.05);
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

                this.multiplier = 2;
                this.max_episode_length = 10;
                this.maxVIIters = 20000;

                // Set Parameter Values
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
                this.robustSpec = "Pmaxmin = ? [ F \"purchase\" ]";
                this.optimisticSpec = "Pmaxmax = ? [ F \"purchase\" ]";
                this.dtmcSpec = "P = ? [ F \"purchase\" ]";
                this.spec = "Pmax = ? [ F\"purchase\" ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 30;
                this.maxVIIters = 20000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.3);
                this.parameterValues.addValue("theta2", 0.4);
            }

            case KEY_DOOR_MAZE -> {
                this.modelFile = "../parametric_convex_models/mixture_mdps/key-door-maze.prism";
                this.certainModelFile = "../parametric_convex_models/mixture_mdps/key-door-maze.prism";
                this.robustSpec = "Rminmax = ? [ F \"goal\" ]";
                this.optimisticSpec = "Rminmin = ? [ F \"goal\" ]";
                this.dtmcSpec = "R=? [ F \"goal\" ]";
                this.spec = "Rmin = ? [ F\"goal\" ]";
                this.type = Type.REWARD;

                this.multiplier = 2;
                this.max_episode_length = 100;
                this.maxVIIters = 1000000;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.1);
                this.parameterValues.addValue("theta2", 0.2);
                this.parameterValues.addValue("theta3", 0.15);
                this.parameterValues.addValue("theta4", 0.3);
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
                this.modelFile = "models/sav.prism";
                this.type = Type.REACH;

                // Set Parameter Values
                this.parameterValues.addValue("theta1", 0.4);
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
                this.modelFile = "models/sav.prism";
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

package imdpcomp;

import explicit.ConstructModel;
import parser.Values;

public class Experiment {
    public Model model;
    public Type type;
    public String modelFile;
    public String certainModelFile; //TODO: replace this with UMDP
    public String dtmcSpec;
    public String robustSpec;
    public Values parameterValues = new Values();
    public Values exactValues = new Values();

    public ConstructModel.CompositionType compositionType = ConstructModel.CompositionType.INTERVAL_PRODUCT;

    public Experiment(Model model){
        this.setModel(model);
    }

    public Experiment(Model model, ConstructModel.CompositionType type){
        this.setModel(model);
        this.compositionType = type;
    }

    public enum Model {
        AIRCRAFT,
        LAKE_SWARM,
        LAKE_SWARM_MULTI_SLIP,
        COIN,
        CSMA,
        RABIN,
        CHAIN,
        CHAIN_MULTI,
        CHAIN_MULTI_SINGLE
    }

    public enum Type {
        REACH,
        REWARD
    }

    public Experiment setCompositonType(ConstructModel.CompositionType type){
        this.compositionType = type;
        return this;
    }

    public Experiment setValues(Values values) {
        this.parameterValues = values;
        return this;
    }

    public Experiment setExactValues(Values values) {
        this.exactValues = values;
        return this;
    }

    public Experiment setModel(Model model) {
        this.model = model;
        switch (model) {
            case AIRCRAFT -> {
                this.modelFile = "../models/aircraft_collision/aircraft_10x20_resolution_3.prism";
                this.certainModelFile = "../models/aircraft_collision/aircraft_10x20_resolution_3_certain.prism";
                this.robustSpec = "Pmaxmin=? [!\"collision\" U \"goal\"]";
                this.dtmcSpec = "P=? [!\"collision\" U \"goal\"]";

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.04);
            }

            case LAKE_SWARM -> {
                this.modelFile = "../models/lake/frozen_lake_swarm_eps.prism";
                this.certainModelFile = "../models/lake/frozen_lake_swarm_eps_certain.prism";
                this.robustSpec = "Rminmax=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.05);
                this.parameterValues.addValue("N", 7);
                this.parameterValues.addValue("M", 7);
                this.parameterValues.addValue("p", 0.35);
            }

            case LAKE_SWARM_MULTI_SLIP -> {
                this.modelFile = "../models/lake_multislip/frozen_lake_swarm_eps.prism";
                this.certainModelFile = "../models/lake_multislip/frozen_lake_swarm_eps_certain.prism";
                this.robustSpec = "Rminmax=? [ F goal ]";
                this.dtmcSpec = "R=? [ F goal ]";

                // Set Parameter Values
                this.parameterValues.addValue("eps", 0.04);
                this.parameterValues.addValue("N", 8);
                this.parameterValues.addValue("M", 8);
                this.parameterValues.addValue("p", 0.45);
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
                this.dtmcSpec = "R=? [F \"goal\"]";

                // Set Parameter Values
                this.parameterValues.addValue("H", 8);
                this.parameterValues.addValue("p", 0.4);
                this.parameterValues.addValue("q", 0.6);
                this.parameterValues.addValue("r", 0.5);
                this.parameterValues.addValue("eps", 0.02);
            }

            case CHAIN_MULTI -> {
                this.modelFile = "../models/chain/chain_multi_succ.prism";
                this.certainModelFile = "../models/chain/chain_multi_succ_certain.prism";
                this.robustSpec = "Rminmax=? [F \"goal\"]";
                this.dtmcSpec = "R=? [F \"goal\"]";

                // Set Parameter Values
                this.parameterValues.addValue("H", 7);
                this.parameterValues.addValue("p", 0.4);
                this.parameterValues.addValue("q", 0.6);
                this.parameterValues.addValue("r", 0.5);
                this.parameterValues.addValue("eps", 0.02);
            }

            case CHAIN_MULTI_SINGLE -> {
                this.modelFile = "../models/chain/chain_multi_succ_single.prism";
                this.certainModelFile = "../models/chain/chain_multi_succ_single_certain.prism";
                this.robustSpec = "Rminmax=? [F \"goal\"]";
                this.dtmcSpec = "R=? [F \"goal\"]";

                // Set Parameter Values
                this.parameterValues.addValue("H", 9);
                this.parameterValues.addValue("p", 0.4);
                this.parameterValues.addValue("q", 0.6);
                this.parameterValues.addValue("r", 0.5);
                this.parameterValues.addValue("eps", 0.02);
            }

        }
        return this;
    }
}

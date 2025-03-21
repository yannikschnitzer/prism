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
    public Values parameterValues;
    public Values exactValues = new Values();

    public ConstructModel.CompositionType compositionType;

    public Experiment(Model model){
        this.setModel(model);
    }

    public Experiment(Model model, ConstructModel.CompositionType type){
        this.setModel(model);
        this.compositionType = type;
    }

    public enum Model {
        AIRCRAFT
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
                this.dtmcSpec = "Pmax=? [!\"collision\" U \"goal\"]";
                this.compositionType = ConstructModel.CompositionType.INTERVAL_PRODUCT;
                this.exactValues.addValue("eps", 0.00);
            }
        }
        return this;
    }
}

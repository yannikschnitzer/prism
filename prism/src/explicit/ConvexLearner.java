package explicit;

import param.Function;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;

public class ConvexLearner {

    private Prism prism;

    public static void main(String[] args) throws PrismException {
        UMDPModelChecker umdp = new UMDPModelChecker(null);
        MDP<Function> mdp = new MDPSimple<>();
        ConvexLearner cx = new ConvexLearner();
        cx.initializePrism();
    }

    @SuppressWarnings("unchecked")
    public void initializePrism() throws PrismException {
        this.prism = new Prism(new PrismDevNullLog());
        this.prism.initialise();
        this.prism.setEngine(Prism.EXPLICIT);
        this.prism.setGenStrat(true);
    }

}

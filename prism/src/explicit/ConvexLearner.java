package explicit;

import param.Function;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;

public class ConvexLearner {

    private Prism prism;

    public static void main(String[] args) throws PrismException {
        UMDPModelChecker umdp = new UMDPModelChecker(null);

        MDPSimple<Function> mdp = new MDPSimple<>();
        mdp.addStates(4);



    }

}

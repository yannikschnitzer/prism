package explicit;

import prism.PrismException;

/**
 * 
 * Note: This would be a BiConsumer<Integer,Double> in Java 8
 *
 */
public interface TransitionConsumer
{

	public void accept(int target, double prob) throws PrismException;
	
}

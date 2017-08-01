package explicit;

import prism.PrismException;

public interface SuccessorConsumer
{
	public void accept(int target) throws PrismException;
}

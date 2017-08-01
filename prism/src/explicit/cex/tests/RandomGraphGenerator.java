package explicit.cex.tests;

import java.util.Random;

public class RandomGraphGenerator
{
	
	private Random rnd;
	
	public RandomGraphGenerator() {
		rnd = new Random();
	}
	
	public RandomGraphGenerator(int seed) {
		rnd = new Random(seed);
	}
	
	public ProbabilisticGraph makeSCCTree(int averageBranchingFactor, int maxDepth, int averageSCCSize) {
		
		//ProbabilisticGraph originalTree = makeTree(averageBranchingFactor, maxDepth);
		ProbabilisticGraph originalTree = makePath(3);
		
		ProbabilisticGraph intermediate = new ProbabilisticGraph(originalTree);
		
		System.out.println("Starting point: ");
		System.out.println(intermediate);
		System.out.println("--------------------------");
		
		for (int i = 0; i < originalTree.getNumStates(); i++) {
			ProbabilisticGraph g = makeSCC(rnd.nextInt(2*averageSCCSize)+1);
			System.out.println("New SCC: ");
			System.out.println(g);
			System.out.println("--------------------------");
			
			intermediate.expandNodeIntoGraph(i, g);
			
			System.out.println("New Result: ");
			System.out.println(intermediate);
			System.out.println("--------------------------");
		}
		
		// The original nodes have been replaced, so we shift everything back to begin with 0
		ProbabilisticGraph result = new ProbabilisticGraph(intermediate, - originalTree.getNumStates());
		result.normalizeProbabilities();
		return result;
	}
	
	public ProbabilisticGraph makeTree(int averageBranchingFactor, int maxDepth) {
		ProbabilisticGraph result = new ProbabilisticGraph(1);

		result.getInputStates().add(0);
		branchFrom(0, averageBranchingFactor, maxDepth, result);
		
		return result;
	}

	private void branchFrom(int from, int averageBranchingFactor, int maxDepth, ProbabilisticGraph g)
	{
		int numChildren = rnd.nextInt(2 * averageBranchingFactor + 1);

		if (maxDepth == 0 || numChildren == 0) {
			g.getOutputStates().add(from);
			return;
		}
		
		for (int i = 0; i < numChildren; i++) {
			int child = g.getNumStates();
			g.addTransition(from, child, 1.0 / (double)numChildren);
			g.setNumStates(g.getNumStates()+1);
			branchFrom(child, averageBranchingFactor, maxDepth-1, g);
		}
	}

	public ProbabilisticGraph makePath(int nodes) {
		assert(nodes >= 2);
		
		ProbabilisticGraph result = new ProbabilisticGraph(nodes);
		result.getInputStates().add(0);
		result.getOutputStates().add(nodes-1);
		for (int trg = 1; trg < nodes; trg++) {
			result.addTransition(trg-1, trg, 1);
		}
		return result;
	}

	public ProbabilisticGraph makeSCC(int numStates) {
		assert(numStates >= 1);
		ProbabilisticGraph result = expandRandomlyToSCC(new ProbabilisticGraph(numStates));
		result.addInitialState(0);
		result.addOutputState(numStates - 1);
		return result;
	}
	
	public ProbabilisticGraph expandRandomlyToSCC(ProbabilisticGraph g) {
		ProbabilisticGraph result = new ProbabilisticGraph(g);
		
		while (!result.isStronglyConnected()) {
			//System.out.println(result);
			//System.out.println("--------------------------------");
			
			boolean hasAdded = false;
			while (!hasAdded) {
				int src = rnd.nextInt(result.getNumStates());
				int trg = rnd.nextInt(result.getNumStates());
				if (!result.hasTransition(src, trg)) {
					result.addTransition(src, trg, 1);
					hasAdded = true;
				}
			}
		}
		
		result.normalizeProbabilities();
		
		return result;
	}
	
	public ProbabilisticGraph makeUniformlyRandomGraph(int nodes, int numInputs, int numOutputs, double edgeProb) {
		assert(numInputs + numOutputs <= nodes);
		assert(edgeProb >= 0 && edgeProb <= 1);
		
		ProbabilisticGraph result = new ProbabilisticGraph(nodes);
		
		for (int i = 0; i < numInputs; i++) {
			result.getInputStates().add(i);
		}
		for (int j = nodes - numOutputs; j < nodes; j++) {
			result.getOutputStates().add(j);
		}
		
		for (int i = 0; i < nodes; i++) {
			for (int j = 0; j < nodes; j++) {
				if (rnd.nextDouble() <= edgeProb) {
					result.addTransition(i, j, 1);
				}
			}
		}
		
		result.normalizeProbabilities();
		
		return result;
	}
	
	public static void main(String[] args) {
		RandomGraphGenerator rgg = new RandomGraphGenerator();
		
		//System.out.println(rgg.makeUniformlyRandomGraph(10, 1, 1, 0.2));
		
		/*for (int i = 1; i < 10; i++) {
			System.out.println("--------------------------------");
			System.out.println("---------- STEPS FOR " + i + " ---------");
			System.out.println("--------------------------------");
			ProbabilisticGraph g = rgg.makeStronglyConnected(new ProbabilisticGraph(i));
			System.out.println("--------------------------------");
			System.out.println("----------- RESULT -------------");
			System.out.println("--------------------------------");
			System.out.println(g);
			System.out.println("--------------------------------");
			System.out.println("--------------------------------");
		}*/
		
		//System.out.println(rgg.makeTree(2, 5));
		
		System.out.println(rgg.makeSCCTree(2, 2, 5));
	}
	
}

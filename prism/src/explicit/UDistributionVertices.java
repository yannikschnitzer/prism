package explicit;

import common.Interval;
import java.util.*;

public class UDistributionVertices<Value> implements UDistribution<Value>{


    int[] support;
    double [][] vertices;
    List<List<Interval<Value>>> marginals;

    public UDistributionVertices (List<List<Interval<Value>>> marginals, List<Integer> support) {
        this.support = support.stream().mapToInt(Integer::intValue).toArray();
        this.marginals = marginals;
        System.out.println("Support: " + Arrays.toString(this.support) + " Marginals: " + marginals);

        buildVertices();
    }

    public UDistributionVertices(int[] support, double[][] vertices) {
        this.support = support;
        this.vertices = vertices;
    }

    protected void buildVertices(){

    }

    @Override
    public boolean contains(int j)
    {
        return Arrays.stream(support).anyMatch(o -> o == j);
    }

    @Override
    public boolean isSubsetOf(BitSet set) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsOneOf(BitSet set)
    {
        return set.stream().anyMatch(this::contains);
    }

    @Override
    public Set<Integer> getSupport()
    {
        HashSet<Integer> set = new HashSet<>();
        for (int j : support) {
            set.add(j);
        }
        return set;
    }

    @Override
    public boolean isEmpty()
    {
        return support.length == 0;
    }

    @Override
    public int size()
    {
        return support.length;
    }

    /**
     * Do a single row of matrix-vector multiplication followed by min/max,
     * i.e. return min/max_P { sum_j P(s,j)*vect[j] }
     * @param vect Vector to multiply by
     * @param minMax Min/max uncertainty (via isMinUnc/isMaxUnc)
     */
    @Override
    public double mvMultUnc(double[] vect, MinMax minMax)
    {
        return 0;
    }

    @Override
    public UDistribution<Value> copy()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UDistribution<Value> copy(int[] permut)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString()
    {
        String s = "Vertices, ";
        s += "Support: " + Arrays.stream(support)
                .boxed()
                .toList();
        s += " ";
        s += "Vertices: " + Arrays.deepToString(vertices);
        return s;
    }

}


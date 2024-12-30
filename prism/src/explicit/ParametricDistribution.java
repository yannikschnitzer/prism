package explicit;

import param.Function;
import param.FunctionFactory;

import java.util.Map;

public class ParametricDistribution
{
    public int size;
    public Function[] probs;
    public int[] index;

    private FunctionFactory fact;

    public ParametricDistribution(int size)
    {
        this.size = size;
        this.probs = new Function[size];
        this.index = new int[size];
    }

    @Override
    public String toString()
    {
        String s = "";
        for (int i = 0; i < size; i++) {
            if (i > 0) s += " + ";
            s += "[" + probs[i] + "]:" + index[i];
        }
        return s;
    }

    public static ParametricDistribution extractParametricDistribution(Distribution<Function> pdist) {
        ParametricDistribution dist = new ParametricDistribution(pdist.size());
        int i = 0;
        for (Map.Entry<Integer, Function> entry : pdist.map.entrySet()) {
            dist.probs[i] = entry.getValue();
            dist.index[i] = entry.getKey();
            i++;
        }
        return dist;
    }
}

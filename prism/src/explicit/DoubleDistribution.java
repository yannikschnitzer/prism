package explicit;

import java.util.Map;

public class DoubleDistribution
{
    public int size;
    public double[] probs;
    public int[] index;

    public DoubleDistribution(int size)
    {
        this.size = size;
        this.probs = new double[size];
        this.index = new int[size];
    }

    public double sum()
    {
        double sum = 0.0;
        for (double prob : probs) {
            sum += prob;
        }
        return sum;
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

    public static DoubleDistribution extractDoubleDistribution(Distribution<Double> frequencies) {
        DoubleDistribution dist = new DoubleDistribution(frequencies.size());
        int i = 0;
        for (Map.Entry<Integer, Double> entry : frequencies.map.entrySet()) {
            dist.probs[i] = ((Map.Entry<Integer, Double>) entry).getValue();
            dist.index[i] = entry.getKey();
            i++;
        }
        return dist;
    }
}

package learning.Data;

public class DataPoint {

    private int episode;
    private final int accumulated_samples;
    private double estimated_value_imdp;
    private double value_imdp_policy;
    private double estimated_value_convex;
    private double value_convex_policy;

    public DataPoint(int position, double value) {
        this.accumulated_samples = position;
        this.estimated_value_imdp = value;
    }

    public DataPoint(int accumulated_samples, int episode, double[] results) {
        this.episode = episode;
        this.accumulated_samples = accumulated_samples;
        this.estimated_value_imdp = results[0];
        this.value_imdp_policy = results[1];
        this.estimated_value_convex = results[2];
        this.value_convex_policy = results[3];
    }

    public int getAccumulatedSamples() {
        return accumulated_samples;
    }


    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }


        if (!(o instanceof DataPoint d)) {
            return false;
        }


        boolean eq = false;
        // todo implement equality on d and this

        return eq;
    }

    public double getValue_imdp_policy() {
        return value_imdp_policy;
    }

    public void setValue_imdp_policy(double value_imdp_policy) {
        this.value_imdp_policy = value_imdp_policy;
    }

    public double getEstimated_value_convex() {
        return estimated_value_convex;
    }

    public void setEstimated_value_convex(double estimated_value_convex) {
        this.estimated_value_convex = estimated_value_convex;
    }

    public double getValue_convex_policy() {
        return value_convex_policy;
    }

    public void setValue_convex_policy(double value_convex_policy) {
        this.value_convex_policy = value_convex_policy;
    }

    public double getEstimated_value_imdp() {
        return estimated_value_imdp;
    }

    public void setEstimated_value_imdp(double estimated_value_imdp) {
        this.estimated_value_imdp = estimated_value_imdp;
    }

    public int getEpisode() {
        return episode;
    }
}

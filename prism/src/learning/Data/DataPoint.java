package learning.Data;

public class DataPoint {

    private final int accumulated_samples;
    private int episode;
    private long time;
    private double estimated_robust_value_umdp;
    private double estimated_optimistic_value_umdp;
    private double value_umdp_robust_policy;
    private double value_umdp_optimistic_policy;
    private double modelBuildingTime;
    private double modelCheckingTimeRobust;
    private double modelCheckingTimeOptimistic;
    private double modelCheckingTimeDTMC;
    private double uncOptimalPolicyresultRobust;
    private double uncOptimalPolicyresultOptimistic;

    public DataPoint(int position, double value) {
        this.accumulated_samples = position;
        this.estimated_robust_value_umdp = value;
    }

    public DataPoint(int accumulated_samples, int episode, long time, double[] results) {
        this.episode = episode;
        this.time = time;
        this.accumulated_samples = accumulated_samples;
        this.estimated_robust_value_umdp = results[0];
        this.estimated_optimistic_value_umdp = results[7];
        this.value_umdp_robust_policy = results[1];
        this.value_umdp_optimistic_policy = results[2];
        this.modelBuildingTime = results[3] / 1_000_000_000.0;
        this.modelCheckingTimeRobust = results[4] / 1_000_000_000.0;
        this.modelCheckingTimeOptimistic = results[5] / 1_000_000_000.0;
        this.modelCheckingTimeDTMC = results[6] / 1_000_000_000.0;
        this.uncOptimalPolicyresultRobust = results[8];
        this.uncOptimalPolicyresultOptimistic = results[9];
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

    public int getEpisode() {
        return episode;
    }

    public double getEstimated_robust_value_umdp() {
        return estimated_robust_value_umdp;
    }

    public double getEstimated_optimistic_value_umdp() {
        return estimated_optimistic_value_umdp;
    }

    public void setEstimated_robust_value_umdp(double estimated_robust_value_umdp) {
        this.estimated_robust_value_umdp = estimated_robust_value_umdp;
    }

    public double getValue_umdp_robust_policy() {
        return value_umdp_robust_policy;
    }

    public void setValue_umdp_robust_policy(double value_umdp_robust_policy) {
        this.value_umdp_robust_policy = value_umdp_robust_policy;
    }

    public double getValue_umdp_optimistic_policy() {
        return value_umdp_optimistic_policy;
    }

    public void setValue_umdp_optimistic_policy(double value_umdp_optimistic_policy) {
        this.value_umdp_optimistic_policy = value_umdp_optimistic_policy;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getTimeSeconds() {
        return time / 1_000_000_000.0;
    }

    public double getModelBuildingTime() {
        return modelBuildingTime;
    }

    public void setModelBuildingTime(double modelBuildingTime) {
        this.modelBuildingTime = modelBuildingTime;
    }

    public double getModelCheckingTimeRobust() {
        return modelCheckingTimeRobust;
    }

    public void setModelCheckingTimeRobust(double modelCheckingTimeRobust) {
        this.modelCheckingTimeRobust = modelCheckingTimeRobust;
    }

    public double getModelCheckingTimeOptimistic() {
        return modelCheckingTimeOptimistic;
    }

    public void setModelCheckingTimeOptimistic(double modelCheckingTimeOptimistic) {
        this.modelCheckingTimeOptimistic = modelCheckingTimeOptimistic;
    }

    public double getModelCheckingTimeDTMC() {
        return modelCheckingTimeDTMC;
    }

    public void setModelCheckingTimeDTMC(double modelCheckingTimeDTMC) {
        this.modelCheckingTimeDTMC = modelCheckingTimeDTMC;
    }

    public double getUncOptimalPolicyresultRobust() {
        return uncOptimalPolicyresultRobust;
    }

    public void setUncOptimalPolicyresultRobust(double uncOptimalPolicyresultRobust) {
        this.uncOptimalPolicyresultRobust = uncOptimalPolicyresultRobust;
    }

    public double getUncOptimalPolicyresultOptimistic() {
        return uncOptimalPolicyresultOptimistic;
    }

    public void setUncOptimalPolicyresultOptimistic(double uncOptimalPolicyresultOptimistic) {
        this.uncOptimalPolicyresultOptimistic = uncOptimalPolicyresultOptimistic;
    }
}

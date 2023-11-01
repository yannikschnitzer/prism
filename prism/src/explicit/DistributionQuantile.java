package explicit;

// import java.util.Iterator;
// import java.util.Map;
import java.util.ArrayList;
import prism.PrismLog;

 class DistributionQuantile extends DiscreteDistribution {
    
    double p = 0;
    int atoms = 1;
    double delta_p = 1;
    PrismLog mainLog;
    ArrayList<Double> tau_hat;
    ArrayList<Double> z ;

    // Constructor
    public DistributionQuantile(int atoms, double vmin, double vmax, int numStates, PrismLog log){
        super();
        
        this.atoms = atoms;
        mainLog = log;
        p =1.0/atoms;
        z = new ArrayList<Double>(atoms);
        tau_hat = new ArrayList<Double>(atoms);

        for (int i = 0; i < atoms; i++) {
            this.tau_hat.add( (2*i + 1)*p/2.0);
        }

        Collections.fill(z, 0); 
    }

    // initialize the distribution (already inia)
    @Override
    public void clear(){
        Collections.fill(z, 0); 
    }

    // project a given array to finite support (same distribution parameters : vmin, vmiax support)
    // here arr is an array of the probability values for the same support
    @Override
    public void project(ArrayList<Double> arr){


    }

    // project a given array to finite support (different distribution parameters but same number of atoms)
    @Override
    public  void project(ArrayList<Double> probs, ArrayList<Double> supp, double vmin, double vmax){
       
    }

    // update saved distribution
    @Override
    public void update(ArrayList<Double> arr){
        this.p = new ArrayList<Double>(Arrays.asList(arr));
    }

    // compute expected value of the distribution
    @Override
    public double getExpValue()
    {
       
    }

    @Override
    public double getExpValue(ArrayList<Double> temp)
    {
        
    }

    // compute CVaR with a given alph
    @Override
    public double getCvarValue(double alpha)
    {
        
    }

    // compute CVaR with a given alpha 
    @Override
    public double getCvarValue(ArrayList<Double> probs, double alpha)
    {
       
    }

    // compute value at risk
    @Override
    public double getVar(double alpha)
    {

    }

    // compute value at risk
    @Override
    public double getVar(ArrayList<Double> probs, double alpha)
    {

    }

    // compute variance
    @Override
    public double getVariance(){

    }

    // compute variance
    @Override
    public double getVariance(ArrayList<Double> probs){

    }

    // compute W for relevant p, categorical: p=2, quantile p=1
    @Override
    public double getW(ArrayList<Double> arr)
    {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= abs(arr.get(i) - z.get(i));
        }
        return sum* (1.0/atoms);
    }

    @Override
    public double getW(ArrayList<Double> arr, ArrayList<Double> arr2 )
    {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= abs(arr.get(i) - arr2.get(i));
        }
        return sum* (1.0/atoms);
    }

    // Make sure to only compare qunatile to quantile
    @Override
    public double getW(DiscreteDistribution distr)
    {
        return this.getW(distr.getSupport());
    }

    // iterator over the probability values of the distribution
    @Override
    public ArrayList<Double> getValues()
    {
        ArrayList<Double> vals = new ArrayList<Double>(atoms);
        Collections.fill(vals, this.p);
        return vals;
    }

    // iterator over the values of the distribution
    @Override
    public  ArrayList<Double> getSupport()
    {
        return this.z;
    }
}
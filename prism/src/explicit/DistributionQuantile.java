package explicit;

// import java.util.Iterator;
import java.util.*;
import edu.jas.util.MapEntry;
import java.util.ArrayList;
import prism.PrismLog;
import static java.lang.Math.*;

 class DistributionQuantile extends DiscreteDistribution {
    
    double p = 0;
    int atoms = 1;
    double delta_p = 1;
    PrismLog mainLog;
    ArrayList<Double> tau_hat;
    ArrayList<Double> z ;

    // Constructor
    public DistributionQuantile(int atoms, PrismLog log){
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
    // If the support is the same (same number of atoms aka same probability)
    // then we just need to make sure it is sorted.
    @Override
    public void project(ArrayList<Double> arr){
        z =  new ArrayList<Double>(arr); // FIXME
        Collections.sort(z);
        // TODO do the same cutoff as the other one
    } // FIXME

    // project a given array to finite support (different distribution parameters but same number of atoms)
    @Override
    public void project(ArrayList<Double> probs, ArrayList<Double> supp){
        double cum_p = 0.0;
        ArrayList<MapEntry<Double, Double>> multimap = new ArrayList<>();
        Map.Entry<Double, Double> entry;
        for (int j = 0; j < atoms; j++) {
            multimap.add(new MapEntry<>(probs.get(i), supp.get(i)));
        }

        multimap.sort(Map.Entry.comparingByValue());
        z.clear();
        Iterator<MapEntry<Double, Double>> it = multimap.iterator();

        while(it.hasNext() & z.size() < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat[z.size()]) {
                z.add(entry.getValue());
            }
        }

    }

    // project a given array to finite support (different distribution parameters but same number of atoms)
    @Override
    public void project(ArrayList<Double> probs, ArrayList<Double> supp, double vmin, double vmax){
        double cum_p = 0.0;
        ArrayList<MapEntry<Double, Double>> multimap = new ArrayList<>();
        Map.Entry<Double, Double> entry;
        for (int j = 0; j < atoms; j++) {
            multimap.add(new MapEntry<>(probs.get(i), supp.get(i)));
        }

        multimap.sort(Map.Entry.comparingByValue());
        z.clear();
        Iterator<MapEntry<Double, Double>> it = multimap.iterator();

        while(it.hasNext() & z.size() < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat[z.size()]) {
                z.add(entry.getValue());
            }
        }

    }

    // update saved distribution
    @Override
    public void update(ArrayList<Double> arr){
        this.p = (ArrayList) arr.clone();;
    }

    // compute expected value of the distribution
    @Override
    public double getExpValue()
    {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p * z.get(j);
        }
        return sum;
    }

    @Override
    public double getExpValue(ArrayList<Double> temp)
    {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p * temp.get(j);
        }
        return sum;
    }

    // compute CVaR with a given alph
    @Override
    public double getCvarValue(double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        double denom = 0.0;

        // assume already sorted

        for (int i=atoms-1; i>=0; i--){
            if (sum_p < lim){
                if(sum_p+ p < lim){
                    sum_p += p;
                    res += (1/lim) * z.get(i) * p;
                } else{
                    denom = lim - sum_p;
                    sum_p += denom;
                    res += (1/lim) *denom*z.get(i);
                }
            }
        }

        return res;
    }

    // compute CVaR with a given alpha 
    @Override
    public double getCvarValue(ArrayList<Double> probs, double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        double denom = 0.0;
        ArrayList<Double> temp =  new ArrayList<Double>(probs);
        Collections.sort(temp);
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < lim){
                if(sum_p+ p < lim){
                    sum_p += p;
                    res += (1/lim) * temp.get(i) * p;
                } else{
                    denom = lim - sum_p;
                    sum_p += denom;
                    res += (1/lim) *denom*temp.get(i);
                }
            }
        }

        return res;
    }

    // compute value at risk
    @Override
    public double getVar(double alpha)
    {
        // Assume already sorted
        double res =0.0;
        double sum_p =0.0;

        for(int j=atoms-1; j>=0; j--){
            if (sum_p < lim){
                if(sum_p+ p < lim){
                    sum_p += p;
                } else{
                    res =z.get(j);
                }
            }
        }

        return res;
    }

    // compute value at risk
    @Override
    public double getVar(ArrayList<Double> probs, double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        ArrayList<Double> temp =  new ArrayList<Double>(probs);
        Collections.sort(temp);

        for(int j=atoms-1; j>=0; j--){
            if (sum_p < lim){
                if(sum_p+ p < lim){
                    sum_p += p;
                } else{
                    res =temp.get(j);
                }
            }
        }

        return res;
    }

    // compute variance of this dustribution 
    @Override
    public double getVariance(){
        double mu = getExpValue(z);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((z.get(j) * p) - mu), 2);
        }

        return res;
    }

    // compute variance
    @Override
    public double getVariance(ArrayList<Double> probs){
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs.get(j) * p) - mu), 2);
        }

        return res;
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

    @Override
    public String toString()
    {
        String temp = "";
        final AtomicInteger indexHolder = new AtomicInteger();
        z.forEach((z_i) -> {
            final int index = indexHolder.getAndIncrement();
            temp += z_i + "," + p +","+ tau_hat.get(index)+ "\n";
        });
        return temp;
    }

    @Override
    public String toString(DecimalFormat df)
    {
        String temp = "";
        final AtomicInteger indexHolder = new AtomicInteger();
        z.forEach((z_i) -> {
            final int index = indexHolder.getAndIncrement();
            temp += df.format(z_i) + "," + p +","+ tau_hat.get(index)+ "\n";
        });
        return temp;
    }

    @Override
    public Double size()
    {
        return atoms;
    }
}
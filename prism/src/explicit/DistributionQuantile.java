package explicit;

// import java.util.Iterator;
import java.text.DecimalFormat;
import java.util.*;
import edu.jas.util.MapEntry;
import java.util.ArrayList;

import prism.PrismLog;
import static java.lang.Math.*;

 class DistributionQuantile extends DiscreteDistribution {
    
    double p;
    int atoms ;
    PrismLog mainLog;
    ArrayList<Double> tau_hat;
    ArrayList<Double> z ;

    // Constructor
    public DistributionQuantile(int atoms, PrismLog log){
        super();
        
        this.atoms = atoms;
        mainLog = log;
        p =1.0/atoms;
        z = new ArrayList<>(atoms);
        tau_hat = new ArrayList<>(atoms);

        for (int i = 0; i < atoms; i++) {
            this.tau_hat.add( (2*i + 1)*p/2.0);
            this.z.add(0.0);
        }
    }

    // clear the distribution
    @Override
    public void clear(){
        Collections.fill(z, 0.0);
    }

    // Remove memory
    @Override
    public void empty() {z.clear();}

     @Override
     public void clone(DiscreteDistribution source) {
         this.z = (ArrayList<Double>) source.getSupports().clone();
     }

     // project a given array to finite support (same distribution parameters : vmin, vmax support)
    // here arr is an array of the probability values for the same support
    // If the support is the same (same number of atoms aka same probability)
    // then we just need to make sure it is sorted.
    @Override
    public void project(ArrayList<Double> arr){
        z =  new ArrayList<>(arr); // FIXME
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
            multimap.add(new MapEntry<>(probs.get(j), supp.get(j)));
        }

        multimap.sort(Map.Entry.comparingByValue());
        this.empty();
        Iterator<MapEntry<Double, Double>> it = multimap.iterator();

        while(it.hasNext() && z.size() < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat.get(z.size())) {
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
        for (int i = 0; i < atoms; i++) {
            multimap.add(new MapEntry<>(probs.get(i), supp.get(i)));
        }

        multimap.sort(Map.Entry.comparingByValue());
        this.empty();
        Iterator<MapEntry<Double, Double>> it = multimap.iterator();

        while(it.hasNext() && z.size() < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat.get(z.size())) {
                z.add(entry.getValue());
            }
        }

    }

    @Override 
    // FIXME: add an adaptive version
    // TODO: add error metric compute
    public void project(TreeMap<Double, Double> particles)
    {
        double cum_p = 0.0;
        this.empty();

        for (Map.Entry<String, String> entry : particles.entrySet()){
            if(z.size() == atoms){
                break;
            }
            cum_p += entry.getValue();
            if(cum_p >= tau_hat.get(z.size())) {
                z.add(entry.getKey());
            }
        }
    }

    // update saved distribution
    @Override
    public void update(ArrayList<Double> arr){
        this.z = (ArrayList<Double>) arr.clone();
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

    // compute CVaR with a given alpha
    @Override
    public double getCvarValue(double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        double denom ;

        // assume already sorted

        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ p < alpha){
                    sum_p += p;
                    res += (1/alpha) * z.get(i) * p;
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) *denom*z.get(i);
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
        double denom ;
        ArrayList<Double> temp =  new ArrayList<>(probs);
        Collections.sort(temp);
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ p < alpha){
                    sum_p += p;
                    res += (1/alpha) * temp.get(i) * p;
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) *denom*temp.get(i);
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
            if (sum_p < alpha){
                if(sum_p+ p < alpha){
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
        ArrayList<Double> temp =  new ArrayList<>(probs);
        Collections.sort(temp);

        for(int j=atoms-1; j>=0; j--){
            if (sum_p < alpha){
                if(sum_p+ p < alpha){
                    sum_p += p;
                } else{
                    res =temp.get(j);
                }
            }
        }

        return res;
    }

    // compute variance of this distribution
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

    // Make sure to only compare quantile to quantile
    @Override
    public double getW(DiscreteDistribution distr)
    {
        return this.getW(distr.getSupports());
    }

    // iterator over the probability values of the distribution
    @Override
    public ArrayList<Double> getValues()
    {
        ArrayList<Double> vals = new ArrayList<>(atoms);
        vals.addAll(Collections.nCopies(atoms, this.p));
        return vals;
    }

     @Override
     public Double getValue(int index)
     {
         return p;
     }

     @Override
     public Double getSupport(int index) {
         return z.get(index);
     }

     // iterator over the values of the distribution
    @Override
    public  ArrayList<Double> getSupports()
    {
        return this.z;
    }

    @Override
    public String toString()
    {
        StringBuilder temp = new StringBuilder();
        int index = 0;
        for (Double z_i: z) {
            temp.append(z_i);
            index ++;
            if(index < atoms) {
                temp.append(",");
            }
        }
        return temp.toString();
    }

    @Override
    public String toString(DecimalFormat df)
    {
        StringBuilder temp = new StringBuilder();
        int index = 0;
        for (Double z_i: z) {
            temp.append(df.format(z_i));
            index ++;
            if(index < atoms) {
                temp.append(",");
            }
        }
        return temp.toString();
    }

     @Override
     public String toFile() {
         StringBuilder temp = new StringBuilder();
         int index = 0;
         for (Double z_i: z) {
             temp.append(z_i).append(",").append(p).append(",");
             temp.append(tau_hat.get(index)).append("\n");
             index ++;
         }
         return temp.toString();
     }

     @Override
     public String toFile(DecimalFormat df) {
         StringBuilder temp = new StringBuilder();
         int index = 0;
         for (Double z_i: z) {
             temp.append(df.format(z_i)).append(",").append(df.format(p)).append(",");
             temp.append(df.format(tau_hat.get(index))).append("\n");
             index ++;
         }
         return temp.toString();
     }

     @Override
    public int size() { return this.z.size();}

     @Override
     public int getAtoms() { return atoms;}
}
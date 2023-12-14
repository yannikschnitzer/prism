package explicit;

// import java.util.Iterator;
import java.util.*;
import edu.jas.util.MapEntry;
import java.util.ArrayList;
import java.text.DecimalFormat;
import prism.PrismLog;
import static java.lang.Math.*;
import prism.PrismException;

 class DistributionQuantile extends DiscreteDistribution {
    
    double p;
    int atoms ;
    PrismLog mainLog;
    double [] tau_hat; // quantile midpoints
    double [] z ; // support values

    // Constructor
    public DistributionQuantile(int atoms, PrismLog log){
        super();
        
        this.atoms = atoms;
        mainLog = log;
        p =1.0/atoms;
        z = new double [atoms];
        tau_hat = new double [atoms];

        for (int i = 0; i < atoms; i++) {
            this.tau_hat[i] = (2*i + 1)*p/2.0;
        }
    }

    // clear the distribution
    @Override
    public void clear(){
        Arrays.fill(z, 0.0);
    }

    // Remove memory
    // INFO: this might need to be changed later
    @Override
    public void empty() {this.clear();}

     @Override
     public void clone(DiscreteDistribution source) {
        double [] source_supp = source.getSupports();
         this.z =Arrays.copyOf(source_supp, source_supp.length);
     }

     // project a given array to finite support (same distribution parameters : vmin, vmax support)
    // here arr is an array of the probability values for the same support
    // If the support is the same (same number of atoms aka same probability)
    // then we just need to make sure it is sorted.
    @Override
    public void project(ArrayList<Double> arr){
        z =  arr.stream().mapToDouble(i -> i).toArray(); // FIXME
        Arrays.sort(z);
        // TODO: do the same cutoff as the other one?
    } 

    // assume probs.size=supp.size()= atoms
     // project a given array to finite support (different distribution parameters but same number of atoms)
    @Override
    public void project(ArrayList<Double> probs, ArrayList<Double> supp) throws PrismException {
        double cum_p = 0.0;
        if (probs.size() != atoms || supp.size() != atoms){
            String error_msg = "This function only works when the support size is the same as probs and supp. Provided: probs size:";
            throw new PrismException(error_msg+probs.size()+", supp size:"+supp.size());
        }
        ArrayList<MapEntry<Double, Double>> multimap = new ArrayList<>();
        Map.Entry<Double, Double> entry;
        for (int j = 0; j < atoms; j++) {
            multimap.add(new MapEntry<>(probs.get(j), supp.get(j)));
        }

        multimap.sort(Map.Entry.comparingByValue());
        this.empty();
        Iterator<MapEntry<Double, Double>> it = multimap.iterator();

        int index = 0;
        while(it.hasNext() && index < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat[index]) {
                z[index] = entry.getValue();
            }
            index ++;
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

        int index = 0;
        while(it.hasNext() && index < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat[index]) {
                z[index] = entry.getValue();
            }
            index ++;
        }

    }

    @Override 
    // TODO: add error metric compute
    public void project(TreeMap<Double, Double> particles) {
        double cum_p = 0.0;
        //double exp_value = 0;
        double temp_value;
        int index = 0;
        this.empty();
        
        for (Map.Entry<Double, Double> entry : particles.entrySet()){
            if(index >= atoms){
                break;
            }
            temp_value = entry.getValue();
            cum_p += temp_value; // check probability of entry
            if(cum_p >= tau_hat[index]) {
                if(temp_value>p) {
                    int temp = (int)floor(temp_value/p);
                    // make sure z doesn't overflow
                    Arrays.fill(z, index, index + min(temp, atoms-index), entry.getKey());
                    index += min(temp, atoms-index);
                    // if it is still greater than tau(curr atom), add one more
                    if(index < atoms && cum_p >= tau_hat[index]){
                        z[index] = entry.getKey();
                        index ++;
                    }

                }else {
                    z[index] = entry.getKey();
                    index ++;
                }
            }
            // exp_value += entry.getKey()*entry.getValue();
        }
        // mainLog.println("before project :"+ particles);
        // mainLog.println("after :" +z);
        //  mainLog.println("size :" +z.size());
        // Exp value
        // errors[0] += (exp_value - this.getExpValue());
        // CvaR value 
        // errors[1] += (this.getCvarValue(particles) - this.getCvarValue());

    }

    // update saved distribution
    @Override
    public void update(double [] arr){
        this.z = Arrays.copyOf(arr, arr.length);
    }

    // compute expected value of the distribution
    @Override
    public double getExpValue() {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p * z[j];
        }
        return sum;
    }

    @Override
    public double getExpValue(double [] temp) {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p * temp[j];
        }
        return sum;
    }

    @Override
    // For treemap
    public double getExpValue(TreeMap<Double, Double> particles) {
        double sum =0;
        for (Map.Entry<Double, Double> entry : particles.entrySet()){
            // entry.getValue = probability, entry.getKey = support
            sum+= entry.getValue() * entry.getKey();
        }
        return sum;
    }

    // compute CVaR with a given alpha
    @Override
    public double getCvarValue(double alpha) {
        double res =0.0;
        double sum_p =0.0;
        double denom ;

        // assume it is already sorted
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ p < alpha){
                    sum_p += p;
                    res += (1/alpha) * z[i] * p;
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) *denom*z[i];
                }
            }
        }

        return res;
    }

    // compute CVaR with a given alpha 
    @Override
    public double getCvarValue(ArrayList<Double> probs, double alpha) {
        double res =0.0;
        double sum_p =0.0;
        double denom ;
        ArrayList<Double> temp =  new ArrayList<>(probs);
        Collections.sort(temp);
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ probs.get(i) < alpha){
                    sum_p += probs.get(i);
                    res += (1/alpha) * temp.get(i) * probs.get(i);
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) * denom * temp.get(i);
                }
            }
        }

        return res;
    }

        // compute CVaR with a given alpha 
    @Override
    public double getCvarValue(TreeMap<Double, Double> particles, double alpha) {
        double res =0.0;
        double sum_p =0.0;
        double denom ;
        // view map containing reverse view of mapping
        Map<Double, Double> reverseMap = particles.descendingMap();
        for (Map.Entry<Double, Double> entry : reverseMap.entrySet()){
            mainLog.print(entry + " - ");
            if (sum_p < alpha){
                if(sum_p + entry.getValue() < alpha){
                    sum_p += entry.getValue();
                    res += (1/alpha) * entry.getKey() * entry.getValue();
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) * denom * entry.getKey() ;
                }
            }
        }

        return res;
    }

     @Override
     public double getCvarValue(double alpha, double b) {
         double res = 0;
         for (int i=0; i<atoms; i++){
             if (z[i] > 0){
                 res += p * max(0, z[i]-b);
             }
         }

         res = b + 1/(1-alpha) * res;

         return res;
     }

     @Override
     public double getCvarValue(double[] arr, double alpha, double b) {
         double res = 0;
         for (int i=0; i<atoms; i++){
             if (arr[i] > 0){
                 res += p * max(0, arr[i]-b);
             }
         }

         res = b + 1/(1-alpha) * res;

         return res;
     }

     // compute value at risk
    @Override
    public double getVar(double alpha) {
        // Assume already sorted
        double res =0.0;
        double sum_p =0.0;

        for(int j=atoms-1; j>=0; j--){
            if (sum_p < alpha){
                if(sum_p+ p < alpha){
                    sum_p += p;
                } else{
                    res =z[j];
                }
            }
        }

        return res;
    }

    // compute value at risk
    @Override
    public double getVar(ArrayList<Double> probs, double alpha) {
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
    public double getVariance() {
        double mu = getExpValue(z);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((z[j] * p) - mu), 2);
        }

        return res;
    }

    // compute variance
    @Override
    public double getVariance(double[] probs){
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs[j] * p) - mu), 2);
        }

        return res;
    }

     // Compute inner optimization from Bauerle and Ott
     // paper : Markov Decision Processes with Average-Value-At-Risk Criteria
     //  E[[dist-b]+]
     @Override
     public double getInnerOpt(double b) {
         double res = 0;
         for (int j=0; j<atoms; j++){
             res += p * max(0, (z[j] - b));
         }
         return res;
     }

     @Override
     public double getInnerOpt(double [] arr, double b) {
         double res = 0;
         for (int j=0; j<atoms; j++){
             res += p * max(0, (arr[j] - b));
         }
         return res;
     }

     // compute W for relevant p, categorical: p=2, quantile p=1
    @Override
    public double getW(double [] arr) {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= abs(arr[i] - z[i]);
        }
        return sum* (1.0/atoms);
    }

    @Override
    public double getW(ArrayList<Double> arr, ArrayList<Double> arr2) {
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
    public double [] getValues() {
        double [] vals = new double [atoms];
        Arrays.fill(vals,this.p);
        return vals;
    }

     @Override
     public double getValue(int index)
     {
         return p;
     }

     @Override
     public double getSupport(int index) {
         return z[index];
     }

     // iterator over the values of the distribution
    @Override
    public  double [] getSupports()
    {
        return this.z;
    }

    @Override
    public String toString()
    {
        StringBuilder temp = new StringBuilder();
        Arrays.stream(z).forEach(e -> temp.append(e + ", " ));
        return temp.toString();
    }

    @Override
    public String toString(DecimalFormat df) {
        StringBuilder temp = new StringBuilder();
        Arrays.stream(z).forEach(e -> temp.append(df.format(e) + ", " ));
        return temp.toString();
    }

     @Override
     public String toFile() {
         StringBuilder temp = new StringBuilder();
         int index = 0;
         for (double z_i: z) {
             temp.append(z_i).append(",").append(p).append(",");
             temp.append(tau_hat[index]).append("\n");
             index ++;
         }
         return temp.toString();
     }

     @Override
     public String toFile(DecimalFormat df) {
         StringBuilder temp = new StringBuilder();
         int index = 0;
         for (double z_i: z) {
             temp.append(df.format(z_i)).append(",").append(df.format(p)).append(",");
             temp.append(df.format(tau_hat[index])).append("\n");
             index ++;
         }
         return temp.toString();
     }

     @Override
    public int size() { return this.z.length;}

     @Override
     public int getAtoms() { return atoms;}

//    @Override
//    public double [] getErrors(){
//        return errors;
//    }
}
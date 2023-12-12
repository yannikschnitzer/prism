package explicit;

// import java.util.Iterator;
// import java.util.Map;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.*;

class DistributionCategorical extends DiscreteDistribution {
    int atoms ;
    double delta_z;
    double [] z ;
    double [] p;
    double v_min ;
    double v_max ;
    boolean isAdaptive = false;
    int max_atoms;
    double max_delta;
//    double alpha;
    prism.PrismLog mainLog;
    // errors [0] is the exp error
    // errors [1] is the cvar error
    double [] errors = new double [2]; // FIXME: make this parameterized

    // Constructor for non adaptive
    public DistributionCategorical(int atoms, double vmin, double vmax, prism.PrismLog log){
        super();

        this.atoms = atoms;
        this.max_atoms = atoms;
//        this.alpha = alpha;
        this.v_min = vmin;
        this.v_max = vmax;
        this.mainLog = log;
        this.z = new double [atoms];
        this.p = new double [atoms];
        
        if (atoms > 1) {
            this.delta_z = (vmax - vmin) / (atoms - 1);
        }
        else {
                this.delta_z = 0;
        }

        for (int i = 0; i < atoms; i++) {
            if (i == atoms -1){ // hard set vmax to prevent small rounding error
                this.z[i] = vmax;
            } else {
                this.z[i] = vmin + i * this.delta_z;
            }
            this.p[i]= (i==0? 1.0:0.0);
        }
    }

    // Constructor for adaptive
//     public DistributionCategorical(int max_atoms, double desired_delta, prism.PrismLog log){
//         super();

//         // initialize with 2 atoms only
//         this.max_atoms = max_atoms;
// //        this.alpha = alpha;
//         this.v_min = 0;
//         this.v_max = 1;
//         this.mainLog = log;
//         this.isAdaptive = true;
//         this.z = new ArrayList<>(max_atoms/2); // FIXME: not sure how to initialize z capacity
//         this.p = new ArrayList<>(max_atoms/2);
//         this.delta_z = 1;
//         this.atoms = 2;
//         this.max_delta = (desired_delta < 1? 1:desired_delta); // represents the max support gap
        

//         for (int i = 0; i < atoms; i++) {
//             if (i == atoms -1){ // hard set vmax to prevent small rounding error
//                 this.z.add(v_max);
//             } else {
//                 this.z.add(v_min + i * this.delta_z);
//             }
//             this.p.add((i==0? 1.0:0.0));
//         }
//     }

    // initialize the distribution
    @Override
    public void clear(){
        Collections.fill(p, 0.0);
        p[0] = 1.0;
    }

    // remove memory
    @Override
    public void empty() { p.clear();}

    @Override
    public void clone(DiscreteDistribution source) {
        double [] source_vals = source.getValues();
        this.p = Arrays.copyOf(source_vals, source_vals.length);
    }

    // project a given array to finite support (same distribution parameters : vmin, vmiax support)
    // here arr is an array of the probability values for the same support
    @Override
    public void project(ArrayList<Double> arr){
        double temp ; double b; int l,u;
        // set probability array to 0
        Collections.fill(p, 0.0);

        for (int j=0; j<arr.size(); j++){
            temp = max(v_min, min(v_max, this.z[j]));
            b = ((temp - v_min) / this.delta_z);
            l= (int) floor(b); u= (int) ceil(b);

            if ( l- u != 0){
                p[l] += (arr.get(j) * (u - b));
                p[u] += (arr.get(j) * (b - l));
            } else{
                p[l] += arr.get(j);
            }
        }

    }

    // project a given array of probs and support to finite support
    public void project(ArrayList<Double> probs, ArrayList<Double> supp){
        double temp; double b; int l,u;
        // recompute delta_z
        delta_z = (v_max - v_min) / (atoms - 1);
        // set probability array to 0
        Arrays.fill(p, 0.0);

        for (int j=0; j<probs.size(); j++){
            temp = max(v_min, min(v_max, supp.get(j)));
            b = ((temp - v_min) / delta_z);
            l= (int) floor(b); u= (int) ceil(b);

            if ( l- u != 0){
                p[l] += (probs.get(j) * (u -b));
                p[u] += (probs.get(j) * (b-l));
            } else{
                p[l] += probs.get(j);
            }
        }
    }

    // project a given array to finite support (different distribution parameters but same number of atoms)
    public  void project(ArrayList<Double> probs, ArrayList<Double> supp, double vmin, double vmax){
        double temp; double b; int l,u;
        // recompute delta_z
        delta_z = (vmax - vmin) / (atoms - 1);
        // set probability array to 0
        Arrays.fill(p, 0.0);

        // if the bounds have changed, update the discrete support
        if(vmin != this.v_min || vmax != this.v_max){
            for (int i = 0; i < atoms; i++) {
                if (i == atoms -1){ // hard set vmax to prevent small rounding error
                    this.z[i] =vmax;
                } else {
                    this.z[i]= vmin + i * delta_z;
                }
            }
        }

        for (int j=0; j<probs.size(); j++){
            temp = max(vmin, min(vmax, supp.get(j)));
            b = ((temp - vmin) / delta_z);
            l= (int) floor(b); u= (int) ceil(b); // lower and upper indices

            if ( l- u != 0){
                p[l] += (probs.get(j) * (u -b));
                p[u] += (probs.get(j) * (b-l));
            } else{
                p[l] += probs.get(j);
            }
        }
    }

    // TODO: do we want to try to reduce the delta z gap as well?
    @Override 
    public void project(TreeMap<Double, Double> particles)
    {
        // If adaptive, update distribution parameters
        if(isAdaptive){
            int req_atoms = (int) ceil((particles.lastKey() - particles.firstKey())/ max_delta) + 1;

            // if there are too many values for the desired max gap, trim based on probability values
            while(req_atoms > max_atoms){
                if(particles.get(particles.lastKey()) < particles.get(particles.firstKey())){
                    particles.remove(particles.firstKey());
                } else{
                    particles.remove(particles.lastKey());
                }
                req_atoms = (int) ceil(((particles.lastKey() - particles.firstKey())/ max_delta) + 1);
            }

            if(req_atoms <= 1){
                v_max = ((particles.lastKey() - particles.firstKey()) == 0.0 ? particles.lastKey()+1: particles.lastKey());
                req_atoms += 1;
            } else {
                v_max = particles.lastKey();
            }
            v_min = particles.firstKey();
            delta_z = (v_max - v_min) / (req_atoms - 1);
            // INFO: this is where we would check the delta_z gap and if it can be reduced
            atoms = req_atoms;
            this.z.clear();
            for (int i = 0; i < atoms; i++) {
                if (i == atoms -1){ // hard set vmax to prevent small rounding error
                    this.z[i] = v_max;
                } else {
                    this.z[i] = v_min + i * delta_z;
                }
            }

            if(p.size() < atoms) { // needs to be increased
                p.addAll(Collections.nCopies(atoms - p.size(), 0.0));
            } else if(p.size() > atoms) {
                p.subList(atoms, p.size()+1).clear();
            }
        }

        double exp_value = 0;
        double exp_value_approx = 0;
        double b, temp; int u,l;

        // set probability array to 0
        Collections.fill(p, 0.0);

        // project
        for (Map.Entry<Double, Double> entry : particles.entrySet()){
            // if the probability associated with the particle is 0, skip.
            if(entry.getValue() > 0) {
                temp = max(v_min, min(v_max, entry.getKey()));
                b = ((temp - v_min) / delta_z);
                l = (int) floor(b);
                u = (int) ceil(b);

                exp_value += entry.getKey() * entry.getValue();

                if (l - u != 0 && (b-l)>0.00001) {
                    p[l] += (entry.getValue() * (u - b));
                    p[u] += (entry.getValue() * (b - l));
                } else {
                    p[l] += entry.getValue();
                }
            }
        }

        // mainLog.println("before project :"+ particles);
        // mainLog.println("after :" +p);
        // mainLog.println("size :" +atoms);

        // Update saved error on metric
        // errors[0] += (exp_value - exp_value_approx);
        // FIXME: make  sure this conversion works
//        errors[1] += (this.getCvarValue(particles, alpha) - this.getCvarValue(alpha));

    }

    // update saved distribution
    public void update(ArrayList<Double> arr){
        p =  arr.toArray();
        // if (p.size() > atoms) {
        //     atoms = p.size();
        // }
    }

    // compute expected value of the distribution
    @Override
    public double getExpValue()
    {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z[j] * p[j];
        }
        return sum;
    }

    public double getExpValue(ArrayList<Double> temp)
    {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z[j] * temp[j];
        }
        return sum;
    }

    @Override
    // For treemap
    public double getExpValue(TreeMap<Double, Double> particles)
    {
        double sum =0;
        for (Map.Entry<Double, Double> entry : particles.entrySet()){
            // entry.getValue = probability, entry.getKey = support
            sum+= entry.getValue() * entry.getKey();
        }
        return sum;
    }

    // compute CVaR with a given alpha 
    public double getCvarValue (double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        double denom;
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ p[i] < alpha){
                    sum_p += p[i];
                    res += (1/alpha) * p[i] * z[i];
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
    public double getCvarValue(ArrayList<Double> probs, double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        double denom ;
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ probs.get(i) < alpha){
                    sum_p += probs.get(i);
                    res += (1/alpha) * probs.get(i) *z[i];
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) *denom*z[i];
                }
            }
        }

        return res;
    }

    // compute CVaR with a given alpha for treemap
    @Override
    public double getCvarValue(TreeMap<Double, Double> particles, double alpha)
    {
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
                    denom = alpha - sum_p; // compute remainder probability
                    sum_p += denom;
                    res += (1/alpha) * denom * entry.getKey() ;
                }
            }
        }
        return res;
    }

    // compute value at risk
    @Override
    public double getVar(double alpha)
    {
        double sum_p = 0.0;
        double res = 0.0;
        for(int j=atoms-1; j>=0; j--){
            if (sum_p < alpha){
                if(sum_p + p[j] < alpha){
                    sum_p += p[j];
                }
                else{
                    res = z[j];
                }
            }
        }

        return res;
    }

    // compute value at risk
    public double getVar(ArrayList<Double> probs, double alpha)
    {
        double sum_p = 0.0;
        double res = 0.0;
        for(int j=atoms-1; j>=0; j--){
            if (sum_p < alpha){
                if(sum_p + probs.get(j) < alpha){
                    sum_p += probs.get(j);
                }
                else{
                    res = z[j];
                }
            }
        }

        return res;
    }

    // compute variance
    @Override
    public double getVariance(){
        double mu = getExpValue(p);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((p[j] * z[j]) - mu), 2);
        }

        return res;
    }

    // compute variance
    public double getVariance(ArrayList<Double> probs){
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs.get(j) * z[j]) - mu), 2);
        }

        return res;
    }

    @Override
    public double getW(ArrayList<Double> arr, ArrayList<Double> arr2)
    {
        double sum = 0;
        double [] cum_p = new double[2];
        for (int i =0; i<atoms; i++)
        {
            cum_p[0] += arr.get(i);
            cum_p[1] += arr2.get(i);
            sum+= pow((cum_p[0] - cum_p[1]), 2) * delta_z;
        }
        return sqrt(sum);
    }

    // compute W for relevant p, categorical: p=2, quantile p=1
    @Override
    public double getW(ArrayList<Double> arr)
    {
        double sum = 0;
        double [] cum_p = new double[2];
        for (int i =0; i<atoms; i++)
        {
            cum_p[0] += arr.get(i);
            cum_p[1] += p[i];
            sum+= pow((cum_p[0] - cum_p[1]), 2) * delta_z;
        }
        return sqrt(sum);
    }

    // Make sure to compare categorical to categorical
    @Override
    public double getW(DiscreteDistribution distr)
    {
        return this.getW(distr.getValues());
    }

    // the values of the distribution
    public double [] getValues()
    {
        return this.p;
    }

    public double getValue(int index)
    {
        return this.p[index];
    }

    // iterator over the values of the distribution
    public double [] getSupports()
    {
        return this.z;
    }

    public double getSupport(int atom_index)
    {
        return this.z[atom_index];
    }

    @Override
    public String toString()
    {
        return p.toString();
    }

    @Override
    public String toString(DecimalFormat df)
    {
        StringBuilder temp = new StringBuilder();
        int index = 0;
        Arrays.stream(p).forEach(e -> temp.append(df.format(e) + ", " ));
        return p.toString();
    }

    // Printing for files (csv)
    @Override
    public String toFile()
    {
        StringBuilder temp = new StringBuilder();
        int index = 0;
        for(double p_i: p )
        {
            temp.append(index).append(",").append(p_i).append(",")
                    .append(z.get(index)).append("\n");
            index ++;
        }
        return temp.toString();
    }

    // Printing for files (csv
    @Override
    public String toFile(DecimalFormat df)
    {
        StringBuilder temp = new StringBuilder();
        int index = 0;
        for(double p_i: p )
        {
            temp.append(index).append(",").append(df.format(p_i)).append(",")
                    .append(df.format(z.get(index))).append("\n");
            index ++;
        }
        return temp.toString();
    }

    @Override
    public int size()
    {
        return this.p.length();
    }

    @Override
    public int getAtoms() {
        return atoms;
    }

//    @Override
//    public double [] getErrors()
//    {
//        return errors;
//    }
}
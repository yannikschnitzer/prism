package explicit;

// import java.util.Iterator;
// import java.util.Map;
import java.util.ArrayList;

 class DistributionCategorical extends DiscreteDistribution {
    int atoms = 1;
    double delta_z = 1;
    ArrayList<Double> z ;
    ArrayList<Double> p;
    double v_min ;
    double v_max ;

    prism.PrismLog mainLog;

    // Constructor
    public DistributionCategorical(int atoms, double vmin, double vmax, prism.PrismLog log){
        super();

        this.atoms = atoms;
        this.v_min = vmin;
        this.v_max = vmax;
        this.mainLog = log;
        this.z = new ArrayList<Double>(atoms);
        
        if (atoms > 1) {
            this.delta_z = (vmax - vmin) / (atoms - 1);
        }
        else {
                this.delta_z = 0;
        }

        double [] temp2 = new double[this.atoms];
        temp2[0] =1.0;
        this.p = new ArrayList<Double>(Arrays.asList(temp2));

        for (int i = 0; i < atoms; i++) {
            this.z.add(vmin + i *this.delta_z);
        }
    }

    // initialize the distribution
    @Override
    public void clear(){
        double [] temp2 = new double[this.atoms];
        temp2[0] =1.0;
        this.p = (ArrayList) (Arrays.asList(array)).clone();
    }

    // project a given array to finite support (same distribution parameters : vmin, vmiax support)
    // here arr is an array of the probability values for the same support
    @Override
    public void project(ArrayList<Double> arr){
        double temp = 0; double b=0;
        p.clear();
        p.addAll(Collections.nCopies(atoms, 0));

        for (int j=0; j<probs.length(); j++){
            temp = max(v_min, min(v_max, this.z[j]));
            b = ((temp - v_min) / this.delta_z);
            l= (int) floor(b); u= (int) ceil(b);

            if ( l- u != 0){
                p.set(l, this.p.get(l) + (arr.get(j) * (u - b)));
                p.set(u, this.p.get(u) + (arr.get(j) * (b - l)));
            } else{
                p.set(l, this.p.get(l) + arr.get(j));
            }
        }

    }

    // project a given array of probs and support to finite support
    public void project(ArrayList<Double> probs, ArrayList<Double> supp){
        double temp = 0; double b=0;
        // recompute delta_z
        delta_z = (vmax - vmin) / (atoms - 1); 
        // clear probability array
        p.clear();
        p.addAll(Collections.nCopies(atoms, 0));

        for (int j=0; j<probs.length(); j++){
            temp = max(vmin, min(vmax, supp.get(j)));
            b = ((temp - vmin) / delta_z);
            l= (int) floor(b); u= (int) ceil(b);

            if ( l- u != 0){
                p.set(l, this.p.get(l) + (probs.get(j) * (u -b)));
                p.set(u, this.p.get(u) + (probs.get(j) * (b-l)));
            } else{
                p.set(l, this.p.get(l) + probs.get(j));
            }
        }
    }

    // project a given array to finite support (different distribution parameters but same number of atoms)
    public  void project(ArrayList<Double> probs, ArrayList<Double> supp, double vmin, double vmax){
        double temp = 0; double b=0;
        // recompute delta_z
        delta_z = (vmax - vmin) / (atoms - 1); 
        // clear probability array
        p.clear();
        p.addAll(Collections.nCopies(atoms, 0));

        // if the bounds have changed, update the discrete support
        if(vmin != this.v_min || vmax != this.v_max){
            for (int i = 0; i < atoms; i++) {
                this.z.set(i, vmin + i *delta_z);
            }
        }

        for (int j=0; j<probs.length(); j++){
            temp = max(vmin, min(vmax, supp.get(j)));
            b = ((temp - vmin) / delta_z);
            l= (int) floor(b); u= (int) ceil(b);

            if ( l- u != 0){
                p.set(l, this.p.get(l) + (probs.get(j) * (u -b)));
                p.set(u, this.p.get(u) + (probs.get(j) * (b-l)));
            } else{
                p.set(l, this.p.get(l) + probs.get(j));
            }
        }
    }

    // update saved distribution
    public void update(ArrayList<Double> arr){
        this.p = (ArrayList) arr.clone();
    }

    // compute expected value of the distribution
    @Override
    public double getExpValue()
    {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z.get(j) * p.get(j);
        }
        return sum;
    }

    public double getExpValue(ArrayList<Double> temp)
    {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z.get(j) * temp.get(j);
        }
        return sum;
    }

    // compute CVaR with a given alpha 
    public double getCvarValue(double alpha)
    {
        double res =0.0;
        double sum_p =0.0;
        double denom = 0.0;
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ p.get(i) < alpha){
                    sum_p += p.get(i);
                    res += (1/alpha) * p.get(i) * z.get(i);
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
        double denom = 0.0;
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < alpha){
                if(sum_p+ probs.get(i) < alpha){
                    sum_p += probs.get(i);
                    res += (1/alpha) * probs.get(i) * z.get(i);
                } else{
                    denom = alpha - sum_p;
                    sum_p += denom;
                    res += (1/alpha) *denom*z.get(i);
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
                if(sum_p + p.get(j) < alpha){
                    sum_p += p.get(j);
                }
                else{
                    res = z.get(j);
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
                    res = z.get(j);
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
            res += (1.0 / atoms) * pow(((p.get(j) * z.get(j)) - mu), 2);
        }

        return res;
    }

    // compute variance
    public double getVariance(ArrayList<Double> probs){
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs[j] * z.get(j)) - mu), 2);
        }

        return res;
    }

    @Override
    public double getW(ArrayList<Double> arr, ArrayList<Double> arr2 )
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
            cum_p[1] += p.get(i);
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

    // iterator over the values of the distribution
    public ArrayList<Double> getValues()
    {
        return this.p;
    }

    // iterator over the values of the distribution
    public  ArrayList<Double> getSupport()
    {
        return this.z;
    }

    @Override
    public String toString()
    {
        String temp = "";
        final AtomicInteger indexHolder = new AtomicInteger();
        p.forEach((p_i) -> {
            final int index = indexHolder.getAndIncrement();
            temp += index + "," + p_i +","+ z.get(index)+ "\n";
        });
        return temp;
    }

    @Override
    public String toString(DecimalFormat df)
    {
        String temp = "";
        final AtomicInteger indexHolder = new AtomicInteger();
        p.forEach((p_i) -> {
            final int index = indexHolder.getAndIncrement();
            temp += index + "," + df.format(p_i) +","+ df.format(z.get(index))+ "\n";
        });
        return temp;
    }
}
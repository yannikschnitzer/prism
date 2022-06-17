package explicit;

import edu.jas.util.MapEntry;
import prism.PrismLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.Math.*;

public class DistributionalBellmanQR extends DistributionalBellman {

    double [][] z ;
    double [] p;
    double alpha=1;
    int numStates;
    int atoms = 1;
    double delta_p = 1;
    prism.PrismLog mainLog;
    double [] tau_hat;
    
    public DistributionalBellmanQR(int atoms, int n, PrismLog log)
    {
        super();
        this.atoms = atoms;
        this.numStates = n;
        mainLog = log;
        this.delta_p = 1.0/atoms;
        this.p = new double[atoms];
        this.tau_hat = new double[atoms];
        z = new double[n][atoms];

        for (int i = 0; i < atoms; i++) {
            this.tau_hat[i] = ( (2*i +1)*delta_p/2.0);
            this.p[i] =  delta_p;
        }
    }

    @Override
    public double getExpValue(double[] temp) {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p[j] * temp[j];
        }
        return sum;
    }

    @Override
    public double getValueCvar(double[] probs, double lim) {
        double res =0.0;
        double sum_p =0.0;
        double denom = 0.0;
        double [] temp = Arrays.copyOf(probs, probs.length);
        Arrays.sort(temp);
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < lim){
                if(sum_p+ p[i] < lim){
                    sum_p += p[i];
                    res += (1/lim) * temp[i] * p[i];
                } else{
                    denom = lim - sum_p;
                    sum_p += denom;
                    res += (1/lim) *denom*temp[i];
                }
            }
        }

        return res;
    }

    @Override
    public double getVar(double[] probs, double lim) {
        double res =0.0;
        double sum_p =0.0;
        double [] temp = Arrays.copyOf(probs, probs.length);
        Arrays.sort(temp);

        for(int j=atoms-1; j>=0; j--){
            if (sum_p < lim){
                if(sum_p+ p[j] < lim){
                    sum_p += p[j];
                } else{
                    res =temp[j];
                }
            }
        }

        return res;
    }

    @Override
    public double getVariance(double[] probs) {
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs[j] * p[j]) - mu), 2);
        }

        return res;
    }

    @Override
    public double getW(double[] dist1, double[] dist2) {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= pow((dist1[i]* p[i] - dist2[i]* p[i]),2) ;
        }
        return sqrt(sum);
    }

    @Override
    public double getW(double[] dist1, int state) {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= pow((dist1[i]* p[i] - z[state][i]* p[i]),2);
        }
        return sqrt(sum);
    }

    public double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, int numTransitions, double gamma, double state_reward)
    {

        ArrayList<MapEntry<Double, Double>> multimap = new ArrayList<>();
        double [] result;

        // Update based on transition probabilities and support values
        while (trans_it.hasNext()) {
            Map.Entry<Integer, Double> e = trans_it.next();
            for (int j = 0; j < atoms; j++) {
                multimap.add(new MapEntry<>(delta_p * e.getValue(), gamma*z[e.getKey()][j] + state_reward));
            }
        }

        // Sort the list using lambda expression
        multimap.sort(Map.Entry.comparingByValue());

        // Consolidate based on probability
        result = consolidate(multimap.iterator());

        return result;
    }

    public double[] consolidate(Iterator<MapEntry<Double, Double>> it){
        double cum_p = 0.0;
        int index =0;
        Map.Entry<Double, Double> entry;
        double [] result = new double[atoms];

        while(it.hasNext() & index < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat[index]) {
                result[index] = entry.getValue();
                index += 1;
            }
        }

        return result;
    }

    public void update(double [] temp, int state){
        z[state] = Arrays.copyOf(temp, temp.length);
    }

    @Override
    public double[] getDist(int i) {
        return z[i];
    }

    @Override
    public double[] adjust_support(TreeMap distr) {
        //TODO
        return new double[0];
    }

    @Override
    public void writeToFile(int state, String filename) {
        if (filename == null) {filename="distr_exp_qr.csv";}
        try (PrintWriter pw = new PrintWriter(new File("prism/"+filename))) {
            pw.println("r,p,z");
            for (int r = 0; r < atoms; r++) {
                Double prob = p[r];
                prob = (prob == null) ? 0.0 : prob;
                pw.println(z[state][r] + "," + prob+","+tau_hat[r]);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(int n) {

    }


}

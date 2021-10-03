package explicit;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import static java.lang.Math.*;

public class DistributionalBellman {

    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][] p;
    int nactions = 4;
    double v_min ;
    double v_max ;
    int numStates;
    prism.PrismLog mainLog;


    public DistributionalBellman(int atoms, double vmin, double vmax, int nactions, int numStates, prism.PrismLog log){
        this.atoms = atoms;
        this.z = new double[atoms];
        this.nactions = nactions;
        this.delta_z = (vmax - vmin) / (atoms -1);
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        mainLog = log;

        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        this.p = new double [numStates][atoms];
    }

    public DistributionalBellman(int atoms, double vmin, double vmax, int numStates, prism.PrismLog log){
        this.atoms = atoms;
        this.z = new double[atoms];
        this.delta_z = (vmax - vmin) / (atoms -1);
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        mainLog = log;

        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        this.p = new double [numStates][atoms];
    }

    public double [] getZ()
    {
        return this.z;
    }

    public void initialize_p( int numStates) {

        this.p = new double[numStates][this.atoms];
        double [] temp2 = new double[this.atoms];
        temp2[0] =1.0;
        for (int i = 0; i < numStates; i++) {

            this.p[i]= Arrays.copyOf(temp2, temp2.length);
        }
    }

    // updates probabilities for 1 action
    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it) {
       double [] sum_p= new double[atoms];
        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            for (int j=0; j<atoms; j++) {
                sum_p[j] += e.getValue() * p[e.getKey()][j];
            }

        }
        return sum_p;
    }

    public double [] update_support(double gamma, double state_reward, double []sum_p){

        double [] m = new double [atoms];

        for (int j =0; j<atoms; j++){
            double temp = max(v_min, min(v_max, state_reward+gamma*z[j]));
            double b = (temp - v_min)/delta_z;
            int l= (int) floor(b); int u= (int) ceil(b);

            if ( l- u != 0){
                m[l] += sum_p[j] * (u -b);
                m[u] += sum_p[j] * (b-l);
            } else{
                m[l] += sum_p[j];
            }

            if (sum_p[j] >0){
                mainLog.println("b:"+b+ " j:"+j+" pj:"+sum_p[j]+" l:"+l+" u:"+ u+" lr:"+m[l]+" ur:"+m[u]);
            }
        }

        return m;
    }

    public void update_p(double [] temp, int state){
        p[state] = Arrays.copyOf(temp, temp.length);
    }

    public double getValue(double [] temp){
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z[j] * temp[j];
        }
        return sum;
    }

    public double [][] getP ()
    {
        return p;
    }

}

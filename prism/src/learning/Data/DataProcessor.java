package learning.Data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataProcessor {

    public void dumpDataRobustPolicies(String directoryPath, String name, ArrayList<DataPoint> dataPoints) {
        try {
            String path = directoryPath + name + ".csv";

            FileWriter writer = new FileWriter(path, false);

            writer.write("Episode,Guarantee IMDP,Performance IMDP Policy,Guarantee Convex MDP,Performance Convex Policy");
            writer.write(System.lineSeparator());

            for (DataPoint entry : dataPoints) {
                    String row = entry.getEpisode() + ","
                            + entry.getEstimated_value_imdp() + ","
                            + entry.getValue_imdp_policy() + ","
                            + entry.getEstimated_value_convex() + ","
                            + entry.getValue_convex_policy() ;
                    writer.write(row + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


}

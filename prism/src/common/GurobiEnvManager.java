package common;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;

/**
 * Provides a single shared Gurobi environment per JVM to avoid repeatedly
 * opening WLS sessions in long benchmark runs.
 */
public final class GurobiEnvManager {
    private static GRBEnv sharedEnv;

    private GurobiEnvManager() {}

    public static synchronized GRBEnv getSharedEnv() {
        if (sharedEnv == null) {
            try {
                sharedEnv = new GRBEnv(true);
                sharedEnv.set(GRB.IntParam.OutputFlag, 0);
                sharedEnv.start();
            } catch (GRBException e) {
                throw new RuntimeException(e);
            }
        }
        return sharedEnv;
    }

    public static synchronized void disposeSharedEnv() {
        if (sharedEnv == null) {
            return;
        }
        try {
            sharedEnv.dispose();
        } catch (Throwable ignore) {
            // best-effort cleanup
        } finally {
            sharedEnv = null;
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(GurobiEnvManager::disposeSharedEnv));
    }
}


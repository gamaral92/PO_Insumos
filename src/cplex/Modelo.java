package cplex;

import ilog.concert.*;
import ilog.cplex.*;

/**
 *
 * @author a11030
 */
public class Modelo {

    public void modelo() {
        try {
            IloCplex cplex = new IloCplex();
        } catch (IloException exception) {
            System.err.print("Concert exception caught: " + exception);
        }
    }

}

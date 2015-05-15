/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horario;

import ilog.concert.*;
import ilog.cplex.*;

/**
 *
 * @author a11030
 */
public class Modelo {

    public void modelo() {
        try {
            IloCplex model = new IloCplex();

            /**
             * Iniciando as variaveis booleanas
             */
            IloNumVar[][][][] X = new IloNumVar[5][6][15][27];
            for (int dia = 0; dia < 5; dia++) {
                for (int horario = 0; horario < 6; horario++) {
                    for (int turma = 0; turma < 15; turma++) {
                        for (int professor = 0; professor < 27; professor++) {
                            X[dia][horario][turma][professor] = model.boolVar();
                        }
                    }
                }
            }
            /**
             * Função objetivo
             */
            IloLinearNumExpr objective = model.linearNumExpr();
            for (int dia = 0; dia < 5; dia++) {
                for (int horario = 0; horario < 6; horario++) {
                    for (int turma = 0; turma < 15; turma++) {
                        for (int professor = 0; professor < 27; professor++) {
                            objective.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                    }
                }
            }
            model.addMaximize(objective);
            
            /**
             * Necessidade t,p E Z+, para todo t,p
             */
            for (int turma = 0; turma < 15; turma++) {
                for (int professor = 0; professor < 27; professor++) {
                    IloLinearNumExpr expr = model.linearNumExpr();
                    for (int dia = 0; dia < 5; dia++) {
                        for (int horario = 0; horario < 6; horario++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                    }
                    //model.addEq(expr, N[turma][professor]);
                }
            }
            /**
             * Professor não pode estar em mais de uma turma ao mesmo tempo
             */
            for (int professor = 0; professor < 27; professor++) {
                for (int horario = 0; horario < 6; horario++) {
                    for (int dia = 0; dia < 5; dia++) {
                        IloLinearNumExpr expr = model.linearNumExpr();
                        for (int turma = 0; turma < 15; turma++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                        model.addLe(expr, 1.0);
                    }
                }
            }
            /**
             * Professor não da mais de duas horas aula por dia
             */
            for (int turma = 0; turma < 15; turma++) {
                for (int dia = 0; dia < 5; dia++) {
                    for (int professor = 0; professor < 27; professor++) {
                        IloLinearNumExpr expr = model.linearNumExpr();
                        for (int horario = 0; horario < 6; horario++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                        model.addLe(expr, 2.0);
                    }
                }
            }
            /**
             * Indisponibilidade dia x horario x professor E {0,1}, para todo dia,horario,professor
             * 1 não pode dar aula
             * 0 pode dar aula
             */
            for (int dia = 0; dia < 5; dia++) {
                for (int horario = 0; horario < 6; horario++) {
                    for (int professor = 0; professor < 27; professor++) {
//                        if(I[dia][horario][professor] == 1){
//                            IloLinearNumExpr expr = model.linearNumExpr();
//                            for (int turma = 0; turma < 15; turma++) {
//                                expr.addTerm(1.0, X[dia][horario][turma][professor]);
//                            }
//                            model.addEq(expr, 0.0);
//                        }
                    }
                }
            }

            if (model.solve()) {
            } else {
                System.out.println("A feasible solution may still be present, but IloCplex has not been able to prove its feasibility.");
            }
        } catch (IloException exception) {
            System.err.print("Concert exception caught: " + exception);
        }
    }

}

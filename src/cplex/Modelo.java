package cplex;

import ilog.concert.*;
import ilog.cplex.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author a11030
 */
public class Modelo {

    private int insumos;
    private int maquinas;
    private int valor;
    private int[] pesos;
    private int[][] custo;

    public boolean readFile(String nomeArquivo) {
        try {
            FileReader fileReader = new FileReader(new File(nomeArquivo));
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.insumos = Integer.parseInt(linha);
            }
            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.maquinas = Integer.parseInt(linha);
            }
            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.valor = Integer.parseInt(linha);
            }

            this.pesos = new int[insumos];
            this.custo = new int[insumos][maquinas];

            int posicao = 0;
            while (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                String[] parametros = linha.split("\t");
                pesos[posicao] = Integer.parseInt(parametros[1]);
                for (int i = 2; i < parametros.length; i++) {
                    String[] param = parametros[i].replace("m", "").replace(")", "").replace("(", " ").split(" ");
                    custo[posicao][Integer.parseInt(param[0]) - 1] = Integer.parseInt(param[1]);
                }
                posicao++;
            }

            bufferedReader.close();
            fileReader.close();
            return true;
        } catch (IOException | NumberFormatException ex) {
            System.out.println("Arquivo " + nomeArquivo + " não encontrado.");
            return false;
        }
    }

    public void modelo() {
        try {
            IloCplex model = new IloCplex();

            /**
             * Iniciando as variaveis booleanas
             */
            IloNumVar[][] itens = new IloNumVar[insumos][];
            for (int i = 0; i < insumos; i++) {
                itens[i] = model.boolVarArray(maquinas);
            }

            /**
             * Função objetivo
             */
            IloLinearNumExpr objective = model.linearNumExpr();
            for (int i = 0; i < insumos; i++) {
                for (int j = 0; j < maquinas; j++) {
                    try {
                        objective.addTerm(pesos[i] / custo[i][j], itens[i][j]);
                    } catch (ArithmeticException exception) {
                    }
                }
            }
            model.addMaximize(objective);

            /**
             * Restrição: soma da linha <=1
             */
            for (int i = 0; i < insumos; i++) {
                IloLinearNumExpr expr = model.linearNumExpr();
                for (int j = 0; j < maquinas; j++) {
                    expr.addTerm(1, itens[i][j]);
                }
                model.addLe(expr, 1);
            }
            /**
             * Restrição: soma da coluna <=1
             */
            for (int i = 0; i < maquinas; i++) {
                IloLinearNumExpr expr = model.linearNumExpr();
                for (int j = 0; j < insumos; j++) {
                    expr.addTerm(1, itens[j][i]);
                }
                model.addLe(expr, 1);
            }
            /**
             * Restrição: Valor deve ser menor igual ao v
             */
            IloLinearNumExpr expr = model.linearNumExpr();
            for (int i = 0; i < insumos; i++) {
                for (int j = 0; j < maquinas; j++) {
                    expr.addTerm(pesos[i] * custo[i][j], itens[i][j]);
                }
            }
            model.addLe(expr, valor);

            if (model.solve()) {
                System.out.println("Status = " + model.getStatus());
                System.out.println("Value = " + model.getObjValue());
                int qtd = 0;
                for (int i = 0; i < insumos; i++) {
                    for (int j = 0; j < maquinas; j++) {
                        qtd += model.getValue(itens[i][j]);
                    }
                }
                System.out.println("Insumos = " + qtd);
                int gasto = 0;
                for (int i = 0; i < insumos; i++) {
                    if (verificarItem(model.getValues(itens[i]))) {
                        System.out.print("Insumo " + (i + 1) + "\t\t");
                        for (int j = 0; j < maquinas; j++) {
                            double value = model.getValue(itens[i][j]);
                            if(value == 1.0){
                                gasto += pesos[i] * custo[i][j];
                            }
                            System.out.print(Math.abs(model.getValue(itens[i][j])) + "\t");
                        }
                        System.out.println("");
                    }
                }
                System.out.println("Valor = " + valor);
                System.out.println("Gasto = " + gasto);
            } else {
                System.out.println("A feasible solution may still be present, but IloCplex has not been able to prove its feasibility.");
            }
        } catch (IloException exception) {
            System.err.print("Concert exception caught: " + exception);
        }
    }

    private boolean verificarItem(double[] values) {
        int soma = 0;
        for (int i = 0; i < values.length; i++) {
            soma += values[i];
        }
        return soma == 1;
    }

}

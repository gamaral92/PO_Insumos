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
    private double valor;
    private double[] pesos;
    private double[][] custo;

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

            this.pesos = new double[insumos];
            this.custo = new double[insumos][maquinas];

            int posicao = 0;
            while (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                String[] parametros = linha.split("\t");
                //String[] parametros = linha.replace(" ", "\t").split("\t");
                //pesos[posicao] = Integer.parseInt(parametros[1]);
                pesos[posicao] = Double.parseDouble(parametros[1]);
                for (int i = 2; i < parametros.length; i++) {
                    String[] param = parametros[i].replace("m", "").replace(")", "").replace("(", " ").split(" ");
                    //custo[posicao][Integer.parseInt(param[0]) - 1] = Integer.parseInt(param[1]);
                    custo[posicao][Integer.parseInt(param[0]) - 1] = Double.parseDouble(param[1]);
                }
                posicao++;
            }

            bufferedReader.close();
            fileReader.close();
            return true;
        } catch (IOException | NumberFormatException ex) {
            System.out.println("Arquivo " + nomeArquivo + " não foi possível ler.");
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
                    if (custo[i][j] != 0.0) {
                        objective.addTerm(pesos[i] / custo[i][j], itens[i][j]);
                    }
                    //NÃO ADICIONA CUSTO BENEFICIO QUE NAO EXISTE
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
            for (int j = 0; j < maquinas; j++) {
                IloLinearNumExpr expr = model.linearNumExpr();
                for (int i = 0; i < insumos; i++) {
                    expr.addTerm(1, itens[i][j]);
                }
                model.addLe(expr, 1);
            }
            /**
             * Restrição: Valor deve ser menor igual ao v
             */
            IloLinearNumExpr expr = model.linearNumExpr();
            for (int i = 0; i < insumos; i++) {
                for (int j = 0; j < maquinas; j++) {
                    if (custo[i][j] > 0) {
                        expr.addTerm(pesos[i] * custo[i][j], itens[i][j]);
                    }
                }
            }
            model.addLe(expr, valor);

            if (model.solve()) {
                //getRelatorioMatriz(model, itens);
                getRelatorio(model, itens);
            } else {
                System.out.println("A feasible solution may still be present, but IloCplex has not been able to prove its feasibility.");
            }
        } catch (IloException exception) {
            System.err.print("Concert exception caught: " + exception);
        }
    }

    /**
     * Relatorio da solução do CPLEX
     *
     * @param model
     * @param itens
     * @throws IloException
     */
    private void getRelatorioMatriz(IloCplex model, IloNumVar[][] itens) throws IloException {
        System.out.println("-----------------------------------------");
        int gasto = 0;
        System.out.print("\t\t");
        for (int j = 0; j < maquinas; j++) {
            System.out.print("\tm" + (j + 1));
        }
        System.out.println("");
        for (int i = 0; i < insumos; i++) {
            if (verificarItem(model.getValues(itens[i]))) {
                System.out.print("Insumo " + (i + 1) + "\t\t");
                for (int j = 0; j < maquinas; j++) {
                    double value = model.getValue(itens[i][j]);
                    if (value == 1.0) {
                        gasto += pesos[i] * custo[i][j];
                    }
                    System.out.print(Math.abs(model.getValue(itens[i][j])) + "\t");
                }
                System.out.println("");
            }
        }
        int insumo = 0;
        for (int i = 0; i < insumos; i++) {
            for (int j = 0; j < maquinas; j++) {
                insumo += model.getValue(itens[i][j]);
            }
        }
        System.out.println("CplexStatus = " + model.getCplexStatus());
        System.out.println("BestObjValue = " + model.getBestObjValue());
        System.out.println("CplexTime = " + model.getCplexTime());
        System.out.println("Insumos\t\t=\t" + insumos);
        System.out.println("Insumos usados\t=\t" + insumo);
        System.out.println("Valor\t\t=\t" + valor);
        System.out.println("Gasto\t\t=\t" + gasto);
    }

    /**
     * Relatorio da solução do CPLEX
     *
     * @param model
     * @param itens
     * @throws IloException
     */
    private void getRelatorio(IloCplex model, IloNumVar[][] itens) throws IloException {
        System.out.println("-----------------------------------------");
        int gasto = 0;
        int kg = 0;
        System.out.println("");
        for (int i = 0; i < insumos; i++) {
            if (verificarItem(model.getValues(itens[i]))) {
                System.out.print("Insumo " + (i + 1) + "\t\t");
                for (int j = 0; j < maquinas; j++) {
                    double value = model.getValue(itens[i][j]);
                    if (value == 1.0) {
                        gasto += pesos[i] * custo[i][j];
                        kg += pesos[i];
                        System.out.print(pesos[i] + "\tm" + (j + 1) + "(" + custo[i][j] + ")");
                    }
                }
                System.out.println("");
            }
        }
        int insumo = 0;
        for (int i = 0; i < insumos; i++) {
            for (int j = 0; j < maquinas; j++) {
                insumo += model.getValue(itens[i][j]);
            }
        }
        System.out.println("---------------------");
        System.out.println("CplexStatus = " + model.getCplexStatus());
        System.out.println("BestObjValue = " + model.getBestObjValue());
        System.out.println("CplexTime = " + model.getCplexTime());
        System.out.println("kg\t\t=\t" + kg);
        System.out.println("Insumos\t\t=\t" + insumos);
        System.out.println("Insumos usados\t=\t" + insumo);
        System.out.println("Valor\t\t=\t" + valor);
        System.out.println("Gasto\t\t=\t" + gasto);
    }

    /**
     *
     * @param values
     * @return true se o insumo foi utilizado pelo cplex
     */
    private boolean verificarItem(double[] values) {
        int soma = 0;
        for (int i = 0; i < values.length; i++) {
            soma += values[i];
        }
        return soma == 1;
    }

}

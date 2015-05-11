/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cplex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author gabrielamaral
 */
public class GerarArquivo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Random random = new Random(System.currentTimeMillis());

        int insumos = 20;
        //int insumos = validaRandom(random.nextInt(20), random, 20);
        int maquinas = validaRandom(random.nextInt(20), random, 20);
        int valor = validaRandom(random.nextInt(100000), random, 100000);

        try {
            FileWriter fileWriter = new FileWriter(new File("teste.txt"));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(insumos + "");
            bufferedWriter.newLine();
            bufferedWriter.write(maquinas + "");
            bufferedWriter.newLine();
            bufferedWriter.write(valor + "");
            bufferedWriter.newLine();

            for (int i = 0; i < insumos; i++) {
                int peso = validaRandom(random.nextInt(250), random, 250);
                bufferedWriter.write("i" + (i + 1) + "\t" + peso + gerarCustos(random, maquinas));
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static String gerarCustos(Random random, int maquinas) {
        int quantidadeMaquinasQueProcessam = validaRandom(random.nextInt(maquinas), random, maquinas);
        String m = "";
        for (int i = 0; i < quantidadeMaquinasQueProcessam; i++) {
            if (random.nextBoolean()) {
                m += "\tm" + (i + 1) + "(" + validaRandom(random.nextInt(50), random, 50) + ")";
            }
        }
        return m;
    }

    private static int validaRandom(int nextInt, Random random, int seed) {
        if(nextInt == 0){
            nextInt = random.nextInt(seed);
            while(nextInt == 0){
                nextInt = random.nextInt(seed);
            }
        }
        return nextInt;
    }

}

package cplex;

/**
 *
 * @author gabrielamaral
 */
public class Main {
    
    public static void main(String[] args) {
        Modelo modelo = new Modelo();
        if(modelo.readFile("teste5.txt")){
            modelo.modelo();
        } else{
            System.out.println("Erro na leitura do arquivo!");
        }
    }
    
}
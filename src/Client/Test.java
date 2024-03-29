
package Client;


import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;

public class Test {
    public static void main(String[] args) {
        // On commence par definir un EndUser avec une adresse mail
        String mail= "antoine@gmail.com";
        EndUser endUser = new EndUser(mail);
        if (endUser.loadingSuccessful==false) { //On requete le serveur puisque le le loading a echoue
            String serverUrl = "http://localhost:8080/api/getPublicParameters?id=" + endUser.ID;

            // Use RestTemplate to make a GET request to the server
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(serverUrl, String.class);

            // Extract the public parameters from the response and give them to endUser
            String publicParameters = response.getBody();
            System.out.println("Server Response: " + publicParameters);
            endUser.get_Public_Parameters_Sw(publicParameters);
        }

        //On cree une nouvelle donnee
        String data = "Il fait beau.";
        int dataID = 1;
        String topic = "meteo";
        int decoupage = 3;

        System.out.println("Chaine decoupee:");
        Blocks.printStringArray(Blocks.splitStringIntoN(data, decoupage));

        Blocks blocks = Blocks.newData(endUser, dataID, topic, data, decoupage);
         /**
        System.out.println("Matrice A:");
        EndUserSIS.printMatrix(EndUserSIS.computeMatrixA(endUser, blocks.paramA));

        System.out.println("Matrice X:");
        EndUserSIS.printMatrix(EndUserSIS.computeMatrixX(endUser, decoupage, blocks.dataBlocks));

        System.out.println("Matrice V:");
        EndUserSIS.printMatrix(blocks.V);
        **/

        //On ajoute une nouvelle video dans le FOG
        String JSON = blocks.toJson();

        //Recuperation d'un JSON pour le parser en Blocks
        Blocks blocksRecieve = Blocks.fromJson(JSON);
        /**
        System.out.println("Matrice A:");
        EndUserSIS.printMatrix(EndUserSIS.computeMatrixA(endUser, blocksRecieve.paramA));

        System.out.println("Matrice X:");
        EndUserSIS.printMatrix(EndUserSIS.computeMatrixX(endUser, decoupage, blocksRecieve.dataBlocks));

        System.out.println("Matrice V:");
        EndUserSIS.printMatrix(blocksRecieve.V);
         **/

        //Je vérifie l'intégrité
        EndUserIBS.IBS_signature_verification(endUser, blocksRecieve);

        //On modifie maintenant les données pour detecter une erreur d'intégrité
        Blocks forgedBlocks = blocks;
        String oldString = forgedBlocks.dataBlocks[0];
        int position = forgedBlocks.dataBlocksMap.get(oldString);
        forgedBlocks.dataBlocks[0] = "charge_utile ";

        //On remplace dans le HashMap pour pas générer d'erreurs dans l'execution
        forgedBlocks.dataBlocksMap.remove(oldString);
        forgedBlocks.dataBlocksMap.put(forgedBlocks.dataBlocks[0], position);

        JSON = forgedBlocks.toJson();
        EndUserIBS.IBS_signature_verification(endUser, forgedBlocks);
        };

 /**
    public static void main(String[] args) {
        // Example setup
        int l = 3;
        int m = 4;
        int N = 5;
        BigInteger[][] A = generateSampleMatrix(l, m);
        BigInteger q = BigInteger.valueOf(17);  // Replace with your actual q value

        // Create instances of EndUserSIS and DataManipulation
        EndUser endUser = new EndUser("antoine@gmail.com");
        Blocks blocks= new Blocks();

        // Test deleteBlock
        BigInteger[][] VDelete = generateSampleMatrix(l, N);  // Replace with actual dataManipulation
        blocks.deleteBlock(endUser,2, VDelete);
        printMatrix("VDelete", VDelete);

        // Test updateBlock
        BigInteger[][] XUpdate = generateSampleMatrix(m, 1);  // Replace with actual dataManipulation
        BigInteger[][] VUpdate = generateSampleMatrix(l, N);  // Replace with actual dataManipulation
        blocks.updateBlock(1, XUpdate, VUpdate, "newBlock");
        printMatrix("VUpdate", VUpdate);

        // Test insertBlock
        BigInteger[][] XInsert = generateSampleMatrix(m, 1);  // Replace with actual dataManipulation
        BigInteger[][] VInsert = generateSampleMatrix(l, N);  // Replace with actual dataManipulation
        blocks.insertBlock(3, XInsert, VInsert, "newDataBlock");
        printMatrix("VInsert", VInsert);
    }

    // Helper method to generate a sample matrix for testing
    private static BigInteger[][] generateSampleMatrix(int rows, int cols) {
        BigInteger[][] matrix = new BigInteger[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = BigInteger.valueOf(i * 10 + j);  // Replace with actual Blocks
            }
        }
        return matrix;
    }

    // Helper method to print a matrix for testing
    private static void printMatrix(String name, BigInteger[][] matrix) {
        System.out.println(name + ":");
        for (BigInteger[] row : matrix) {
            for (BigInteger value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
  **/
}

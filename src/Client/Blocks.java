package Client;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.util.HashMap;

public class Blocks {
    protected int dataID;
    protected String topic;
    protected String IDw;
    protected HashMap<String, Integer> dataBlocksMap; // HashMap pour stocker le block avec son numero dans la data
    protected String[] dataBlocks;
    protected BigInteger[] paramA;
    protected BigInteger[][] V;
    protected IBSsignature signature;

    public Blocks() {}

    public Blocks(Block block, String[] dataBlocks, HashMap<String, Integer> dataBlocksMap) {
        this.dataID = block.dataID;
        this.topic = block.topic;
        this.IDw = block.IDw;
        this.dataBlocksMap = dataBlocksMap;
        this.dataBlocks = dataBlocks;
        this.paramA = block.paramA;
        V = block.V;
        this.signature = IBSsignature.fromStringArray(block.signature);
    }

    //Fonction permettant de créer une nouvelle data
    public static Blocks newData(EndUser endUser, int dataID, String topic, String data, int N) {
        Blocks blocks = new Blocks();
        blocks.dataID = dataID;
        blocks.topic = topic;
        blocks.IDw = endUser.ID;
        blocks.dataBlocks = splitStringIntoN(data, N);
        BigInteger[][] X = EndUserSIS.computeMatrixX(endUser, N, blocks.dataBlocks);
        blocks.paramA = endUser.paramA;
        BigInteger[][] A = EndUserSIS.computeMatrixA(endUser, blocks.paramA);
        blocks.V = EndUserSIS.computeMatrixV(endUser, N, A, X);
        blocks.signature = EndUserIBS.IBS_signature_generation(endUser, blocks);

        blocks.dataBlocksMap = new HashMap<>();
        // Stocke chaque sous-chaîne dans le HashMap avec sa position comme valeur
        for (int i = 0; i < N; i++) {
            blocks.dataBlocksMap.put(blocks.dataBlocks[i], i); // La position commence à 0
        }

        return blocks;
    }

    public static String[] splitStringIntoN(String inputString, int N) {
        // Vérification que la chaîne d'entrée n'est pas nulle ou vide et que N est positif
        if (inputString == null || inputString.isEmpty() || N <= 0) {
            return new String[0]; // Retourne un tableau vide si la chaîne est invalide ou si N est négatif
        }
        int length = inputString.length();
        int chunkSize = length / N; // Taille de chaque morceau de la chaîne

        String[] result = new String[N];
        // Découpe la chaîne en morceaux de taille chunkSize et les stocke dans le tableau de résultats
        for (int i = 0; i < N; i++) {
            int startIndex = i * chunkSize;
            int endIndex = (i + 1) * chunkSize;
            result[i] = inputString.substring(startIndex, endIndex);
        }
        return result;
    }

    public static void printStringArray(String[] array) {
        for (String element : array) {
            System.out.println(element);
        }
        System.out.println();
    }

    //Parse JSON to generate Blocks
    public static Blocks fromJson(String json) {
        Gson gson = new Gson();
        Blocks blocks = new Blocks();

        // Diviser la chaîne JSON en sous-chaînes représentant chaque bloc individuel
        String[] blockJsonArray = json.split("\n");
        System.out.println("JSON récupéré:");
        Blocks.printStringArray(blockJsonArray);
        Block[] blockArray = new Block[blockJsonArray.length];
        HashMap<String, Integer> dataBlocksMap = new HashMap<>();
        String[] dataBlocks = new String[blockJsonArray.length];
        // Pour chaque sous-chaîne JSON, désérialiser en un objet Block et l'ajouter à la liste de blocs
        for (int i =0; i<blockJsonArray.length; i++) {
            blockArray[i] = gson.fromJson(blockJsonArray[i], Block.class);
            dataBlocksMap.put(blockArray[i].dataBlock, blockArray[i].i);
            dataBlocks[i] = blockArray[i].dataBlock;
        }
        return new Blocks(blockArray[0], dataBlocks, dataBlocksMap);
    }

    //write class to Json
    public String toJson() {
        // Convertir chaque objet Blocks plusiseurs objets Block concaténé dans un JSON
        Gson gson = new Gson();
        StringBuilder jsonBuilder = new StringBuilder();
        for (String dataBlock : this.dataBlocks) {
            String json = gson.toJson(new Block(this, dataBlock));
            jsonBuilder.append(json).append("\n"); // Ajouter un saut de ligne entre chaque JSON
        }

        // Résultat final contenant les JSON concaténés
        String finalJson = jsonBuilder.toString();
        System.out.println("JSON produit:");
        System.out.println(finalJson);
        return finalJson;
    }

    public static byte[] VtoBytes(BigInteger[][] V){

        // Taille totale du tableau de bytes nécessaire
        int totalSize = V.length * V[0].length* Byte.SIZE;
        byte[] result = new byte[totalSize];

        int currentIndex = 0;
        // Convertir chaque élément de la matrice en bytes
        for (int i = 0; i < V.length; i++) {
            for (int j = 0; j < V[0].length; j++) {
                BigInteger currentElement = V[i][j];
                byte[] elementBytes = currentElement.toByteArray();

                // Copier les bytes dans le tableau de résultat
                System.arraycopy(elementBytes, 0, result, currentIndex, elementBytes.length);
                currentIndex += elementBytes.length;
            }
        }
        return result;
    }
    public static byte[] paramAtoBytes(BigInteger[] paramA){
        byte[] array1 = paramA[0].toByteArray();
        byte[] array2 = paramA[0].toByteArray();
        byte[] array3 = paramA[0].toByteArray();
        // Calcul de la taille totale du nouveau tableau
        int totalLength = array1.length + array2.length + array3.length;

        // Création du nouveau tableau
        byte[] result = new byte[totalLength];

        // Copie des trois tableaux dans le nouveau tableau
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        System.arraycopy(array3, 0, result, array1.length + array2.length, array3.length);

        return result;
    }

    public boolean checkPrivilege(EndUser endUser){
        if (endUser.ID.equalsIgnoreCase(this.IDw)) {
            System.out.println("Vous ne pouvez pas manipuler des données qui ne vous appartiennent pas !!!!");
            return false;
        } else {
            System.out.println("Vous êtes autorisé à manipuler les données.");
            return true;
        }
    }

    public boolean deleteBlock(EndUser endUser, int i) {
        if (!checkPrivilege(endUser)) {
            return false;
        }

        // Step 1: Delete the block Di and its security parameters
        // Update dataBlocks and dataBlocksMap after identifying the block to delete
        this.dataBlocksMap.remove(dataBlocks[i]);

        // Shift dataBlocks to fill the gap left by the removed block
        for (int j = i; j < dataBlocks.length - 1; j++) {
            dataBlocks[j] = dataBlocks[j + 1];
        }
        dataBlocks[dataBlocks.length - 1] = null;

        // Update the dataBlocksMap for shifted blocks
        for (int j = i; j < dataBlocks.length - 1; j++) {
            if (dataBlocks[j] != null) {
                this.dataBlocksMap.put(dataBlocks[j], j);
            }
        }

        // Step 2: Create a new matrix V with reduced size
        BigInteger[][] newV = new BigInteger[V.length][V[0].length - 1];

        // Copy the relevant columns from the old V to the new V
        for (int row = 0; row < V.length; row++) {
            for (int col = 0, newCol = 0; col < V[0].length; col++) {
                if (col != i) {
                    newV[row][newCol] = V[row][col];
                    newCol++;
                }
            }
        }

        // Update V to be the newV
        this.V = newV;

        // Update the signature using the new V
        this.signature = EndUserIBS.IBS_signature_generation(endUser, this);

        return true;
    }


    public boolean updateBlock(EndUser endUser, int i, BigInteger[][] X, String newBlock) {
        if (!checkPrivilege(endUser)){return false;}
        // Step 1: Construct a new vector x'i using the new block D'i
        this.addBlockAtEnd(newBlock);

        BigInteger[][] newX = EndUserSIS.computeMatrixX(endUser, this.dataBlocks.length, this.dataBlocks);

        // Extract the first column of newX
        for (int row = 0; row < newX.length; row++) {
            X[row][i] = newX[row][0];
        }
        // Step 2: Compute the vector v'i = A * x'i mod q using computeMatrixV
        BigInteger[][] A = EndUserSIS.computeMatrixA(endUser, paramA);
        BigInteger[][] newV = EndUserSIS.computeMatrixV(endUser, this.dataBlocks.length, A, X);

        // Step 3: Construct a new matrix V by replacing the vector vi by v'i
        for (int row = 0; row < V.length; row++) {
            V[row][i] = newV[row][0];
        }

        this.signature = EndUserIBS.IBS_signature_generation(endUser, this);
        return true;
    }

    public boolean insertBlock(EndUser endUser, int i, BigInteger[][] X, String newDataBlock) {
        if (!checkPrivilege(endUser)){return false;}
        // Step 1: Construct a new vector x'i using the new block D'i
        this.addBlockAtEnd(newDataBlock);
        X[i] = EndUserSIS.computeMatrixX(endUser, this.dataBlocks.length, this.dataBlocks)[0];

        // Step 2: Compute the vector v'i = A * x'i mod q using computeMatrixV
        BigInteger[][] A = EndUserSIS.computeMatrixA(endUser, paramA);
        BigInteger[][] newV = EndUserSIS.computeMatrixV(endUser, this.dataBlocks.length, A, X);

        // Update the V matrix with the new vector v'i
        for (int row = 0; row < V.length; row++) {
            V[row][i] = newV[row][0];
        }

        // Step 3: Add the new vector v'i into the matrix V
        // Step 4: Increase by 1 the indexes of the data blocks that are greater than i
        for (int j = V[0].length - 1; j >= i; j--) {
            for (int row = 0; row < V.length; row++) {
                V[row][j + 1] = V[row][j];
            }
        }
        for (int row = 0; row < V.length; row++) {
            V[row][i] = V[row][i].add(A[row][i].multiply(X[row][0]));
        }

        this.signature = EndUserIBS.IBS_signature_generation(endUser, this);
        return true;
    }

    public void addBlockAtEnd(String newBlock) {
        for (int i = 0; i < this.dataBlocks.length; i++) {
            if (this.dataBlocks[i] == null) {
                this.dataBlocks[i] = newBlock;

                this.dataBlocksMap.put(newBlock, i);

                return;
            }
        }
    }

}

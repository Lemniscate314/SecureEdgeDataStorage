package Client;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;

public class Blocks {
    protected int dataID;
    protected String IDw;
    protected HashMap<String, Integer> dataBlocksMap; // HashMap pour stocker le block avec son numero dans la data
    protected String[] dataBlocks;
    protected BigInteger[] paramA;
    protected BigInteger[][] V;
    protected IBSsignature signature;

    public Blocks() {
        // Récupérer toutes les clés du HashMap et les stocker dans dataBlocks
        Set<String> keys = dataBlocksMap.keySet();
        this.dataBlocks = keys.toArray(new String[0]);
    }

    //load class form Json
    public static Blocks fromJson(String json) {
        return new Gson().fromJson(json, Blocks.class);
    }
    //write class to Json
    public String toJson() {
        return new Gson().toJson(this);
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
        // Taille totale du tableau de bytes nécessaire
        int totalSize = paramA.length * Byte.SIZE;
        byte[] result = new byte[totalSize];

        int currentIndex = 0;
        // Convertir chaque élément du tableau en bytes
        for (BigInteger currentElement : paramA) {
            byte[] elementBytes = currentElement.toByteArray();

            // Copier les bytes dans le tableau de résultat
            System.arraycopy(elementBytes, 0, result, currentIndex, elementBytes.length);
            currentIndex += elementBytes.length;
        }
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

    // Data deletion
    public boolean deleteBlock(EndUser endUser, int i) {
        if (!checkPrivilege(endUser)){return false;}
        // Step 1: Delete the block Di and its security parameters
        // No specific action needed here as it depends on your specific implementation

        // Step 2: Delete the vector vi
        for (int row = 0; row < V[0].length; row++) {
            V[row][i] = BigInteger.ZERO;
        }

        // Step 3: Decrease by 1 the indexes of data blocks that are larger than i
        for (int j = i + 1; j < V.length; j++) {
            if (j - 1 >= 0) {
                for (int row = 0; row < V[0].length; row++) {
                    V[row][j - 1] = V[row][j];
                }
            }
        }
        return true;
    }

    public boolean updateBlock(EndUser endUser, int i, BigInteger[][] X, String newBlock) {
        if (!checkPrivilege(endUser)){return false;}
        // Step 1: Construct a new vector x'i using the new block D'i
        this.addBlockAtEnd(newBlock);

        BigInteger[][] newX = EndUserSIS.constructMatrixX(endUser, this.dataBlocks.length, this.dataBlocks);

        // Extract the first column of newX
        for (int row = 0; row < newX.length; row++) {
            X[row][i] = newX[row][0];
        }
        // Step 2: Compute the vector v'i = A * x'i mod q using computeMatrixV
        BigInteger[][] A = EndUserSIS.computeMatrixA(endUser, paramA);
        BigInteger[][] newV = EndUserSIS.computeMatrixV(endUser, this.dataBlocks.length, A, X);

        // Update the V matrix with the new vector v'i
        for (int row = 0; row < V.length; row++) {
            V[row][i] = newV[row][0];
        }

        // Step 3: Construct a new matrix V by replacing the vector vi by v'i

        return true;
    }

    public boolean insertBlock(EndUser endUser, int i, BigInteger[][] X, String newDataBlock) {
        if (!checkPrivilege(endUser)){return false;}
        // Step 1: Construct a new vector x'i using the new block D'i
        this.addBlockAtEnd(newDataBlock);
        X[i] = EndUserSIS.constructMatrixX(endUser, this.dataBlocks.length, this.dataBlocks)[0];

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
        return true;
    }

    public void addBlockAtEnd(String newBlock) {
        for (int i = 0; i < this.dataBlocks.length; i++) {
            if (this.dataBlocks[i] == null) {
                this.dataBlocks[i] = newBlock;
                return;
            }
        }
    }
}

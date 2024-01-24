package Client.Sis;

import java.math.BigInteger;

public class Data {
    private int l;              // Number of rows in matrix A
    private int N;              // Number of data blocks
    private BigInteger[][] A;   // Matrix A from the setup phase
    private BigInteger q;       // The modulus, a polynomial of l
    private DataMatrixConstruction dataMatrixConstruction;
    private String[] dataBlocks;

    public Data(int l, int N, BigInteger[][] A, BigInteger q, DataMatrixConstruction dataMatrixConstruction) {
        this.l = l;
        this.N = N;
        this.A = A;
        this.q = q;
        this.dataMatrixConstruction = dataMatrixConstruction;
        this.dataBlocks =  new String[N];
    }

    // Data deletion
    public void deleteBlock(int i, BigInteger[][] V) {
        // Step 1: Delete the block Di and its security parameters
        // No specific action needed here as it depends on your specific implementation

        // Step 2: Delete the vector vi
        for (int row = 0; row < l; row++) {
            V[row][i] = BigInteger.ZERO;
        }

        // Step 3: Decrease by 1 the indexes of data blocks that are larger than i
        for (int j = i + 1; j < N; j++) {
            if (j - 1 >= 0) {
                for (int row = 0; row < l; row++) {
                    V[row][j - 1] = V[row][j];
                }
            }
        }
        N--;
    }

    public void updateBlock(int i, BigInteger[][] X, BigInteger[][] V, String newBlock) {
        // Step 1: Construct a new vector x'i using the new block D'i
        this.addBlockAtEnd(newBlock);

        BigInteger[][] newX = dataMatrixConstruction.constructMatrixX(this.dataBlocks);

        // Extract the first column of newX
        for (int row = 0; row < newX.length; row++) {
            X[row][i] = newX[row][0];
        }
        // Step 2: Compute the vector v'i = A * x'i mod q using computeMatrixV
        BigInteger[][] newV = dataMatrixConstruction.computeMatrixV(X);

        // Update the V matrix with the new vector v'i
        for (int row = 0; row < l; row++) {
            V[row][i] = newV[row][0];
        }

        // Step 3: Construct a new matrix V by replacing the vector vi by v'i
    }

    public void insertBlock(int i, BigInteger[][] X, BigInteger[][] V, String newDataBlock) {
        // Step 1: Construct a new vector x'i using the new block D'i
        this.addBlockAtEnd(newDataBlock);
        X[i] = this.dataMatrixConstruction.constructMatrixX(this.dataBlocks)[0];

        // Step 2: Compute the vector v'i = A * x'i mod q using computeMatrixV
        BigInteger[][] newV = dataMatrixConstruction.computeMatrixV(X);

        // Update the V matrix with the new vector v'i
        for (int row = 0; row < l; row++) {
            V[row][i] = newV[row][0];
        }

        // Step 3: Add the new vector v'i into the matrix V
        // Step 4: Increase by 1 the indexes of the data blocks that are greater than i
        for (int j = N - 1; j >= i; j--) {
            for (int row = 0; row < l; row++) {
                V[row][j + 1] = V[row][j];
            }
        }
        for (int row = 0; row < l; row++) {
            V[row][i] = V[row][i].add(A[row][i].multiply(X[row][0]));
        }
        N++;
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

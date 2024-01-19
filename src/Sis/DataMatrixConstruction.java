package Sis;

import Cryptography.IBS.IBSscheme;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class DataMatrixConstruction {
    IBSscheme ibSscheme = new IBSscheme();
    private final BigInteger[][] A = ibSscheme.generatedMatrixA; // Matrix A from the setup phase
    private final BigInteger q = ibSscheme.getQ();     // The modulus, a polynomial of l
    private final int m = ibSscheme.getM();     // m is the number of columns in matrix A
    private final int l = ibSscheme.getL();     // l is the number of rows in matrix A
    private final static int lambda = 256;       // Parameter lambda
    private int N;            // Number of data blocks

    public DataMatrixConstruction(int N) {
        this.N = N;
    }

    // Hash function H3
    public byte[] hashFunctionH3(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    // Constructs the matrix X by hashing each data block and filling the vectors xi. N is m*N
    public BigInteger[][] constructMatrixX(String[] dataBlocks) {
        if (dataBlocks.length != N) {
            throw new IllegalArgumentException("Number of data blocks should be equal to N");
        }

        BigInteger[][] X = new BigInteger[m][N];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < N; j++) {
                byte[] hash = hashFunctionH3(dataBlocks[j]); // Hashing each data block

                // Calculate the starting index for each injection
                int bitIndex = i % lambda;

                // Fill the matrix X with the hash bit sequences
                X[i][j] = new BigInteger(1, hash).testBit(bitIndex) ? BigInteger.ONE : BigInteger.ZERO;
            }
        }

        return X;
    }


    // Computes the matrix V as V = A*X mod q
    public BigInteger[][] computeMatrixV(BigInteger[][] X) {
        // Convert BigInteger matrices to double arrays
        double[][] AArray = new double[l][m];
        double[][] XArray = new double[m][N];

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < m; j++) {
                AArray[i][j] = A[i][j].doubleValue();
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < N; j++) {
                XArray[i][j] = X[i][j].doubleValue();
            }
        }

        // Create RealMatrix from double arrays
        RealMatrix AMatrix = new BlockRealMatrix(AArray);
        RealMatrix XMatrix = new BlockRealMatrix(XArray);

        // Perform matrix multiplication using Apache Commons Math
        RealMatrix VMatrix = AMatrix.multiply(XMatrix);

        // Convert the result back to BigInteger[][] for consistency
        BigInteger[][] V = new BigInteger[l][N];
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < N; j++) {
                V[i][j] = BigInteger.valueOf((long) VMatrix.getEntry(i, j)).mod(q);
            }
        }

        return V;
    }

}
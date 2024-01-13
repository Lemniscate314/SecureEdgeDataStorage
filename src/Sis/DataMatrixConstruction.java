package Sis;

import Cryptography.IBS.IBSscheme;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class DataMatrixConstruction {
    IBSscheme ibSscheme = new IBSscheme();
    private final BigInteger[][] A = ibSscheme.generateMatrixA(); // Matrix A from the setup phase
    private final BigInteger q = ibSscheme.getQ();     // The modulus, a polynomial of l
    private final int m = ibSscheme.getM();     // m is the number of columns in matrix A
    private final int l = ibSscheme.getL();     // l is the number of rows in matrix A
    private int lambda;       // Parameter lambda
    private int N;            // Number of data blocks

    public DataMatrixConstruction(int lambda, int N) {
        this.lambda = lambda;
        this.N = N;
    }

    // for tests purposes
    public int getM() {
        return m;
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

    // Constructs the matrix X by hashing each data block and filling the vectors xi
    public BigInteger[][] constructMatrixX(String[] dataBlocks) {
        if (dataBlocks.length != N) {
            throw new IllegalArgumentException("Number of data blocks should be equal to N");
        }

        BigInteger[][] X = new BigInteger[N][m];

        for (int i = 0; i < N; i++) {
            byte[] hash = hashFunctionH3(dataBlocks[i]); // Hashing each data block
            // Fill the vector xi with the hash bit sequences
            for (int j = 0; j < m; j++) {
                // Using the hash value directly, can be adjusted
                X[i][j] = new BigInteger(1, hash).mod(BigInteger.valueOf(lambda));
            }
        }

        return X;
    }

    // Computes the matrix V as V = A*X mod q
    public BigInteger[][] computeMatrixV(BigInteger[][] X) {
        BigInteger[][] V = new BigInteger[l][N]; // V has dimensions of l x N

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < N; j++) {
                BigInteger sum = BigInteger.ZERO;
                for (int k = 0; k < m; k++) {
                    sum = sum.add(A[i][k].multiply(X[j][k])).mod(q); // Calculate each element of V
                }
                V[i][j] = sum;
            }
        }

        return V;
    }
}


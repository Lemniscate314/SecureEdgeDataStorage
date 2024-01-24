package Client;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EndUserSIS {
    public BigInteger[][] computeMatrixA(int nbRow, int nbColumn,BigInteger q, BigInteger I0, BigInteger C0, BigInteger a) {
        BigInteger[][] matrixA = new BigInteger[nbRow][nbColumn];
        BigInteger In = I0;
        BigInteger Cn = C0;
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                matrixA[i][j] = GenerateRandomNumber.generateRandomNumber(In, Cn, a, q); // Using the generateRandomNumber method to generate each entry
            }
        }
        return matrixA;
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

    // Constructs the matrix X by hashing each Block block and filling the vectors xi. N is m*N
    public BigInteger[][] constructMatrixX(int l, int m, int N, int lambda, String[] dataBlocks) {
        if (dataBlocks.length != N) {
            throw new IllegalArgumentException("Number of Block blocks should be equal to N");
        }

        BigInteger[][] X = new BigInteger[m][N];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < N; j++) {
                byte[] hash = hashFunctionH3(dataBlocks[j]); // Hashing each Block block

                // Calculate the starting index for each injection
                int bitIndex = i % lambda;

                // Fill the matrix X with the hash bit sequences
                X[i][j] = new BigInteger(1, hash).testBit(bitIndex) ? BigInteger.ONE : BigInteger.ZERO;
            }
        }

        return X;
    }

    // Computes the matrix V as V = A*X mod q
    public BigInteger[][] computeMatrixV(int l, int m, BigInteger q, int N, BigInteger[][] A, BigInteger[][] X) {
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
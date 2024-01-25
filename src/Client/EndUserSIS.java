package Client;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EndUserSIS {
    public static BigInteger[][] computeMatrixA(EndUser endUser, BigInteger[] paramA) {
        BigInteger[][] matrixA = new BigInteger[endUser.l][endUser.m];
        BigInteger In = paramA[0];
        BigInteger Cn = paramA[2];
        for (int i = 0; i < endUser.l; i++) {
            for (int j = 0; j < endUser.m; j++) {
                matrixA[i][j] = FunctionsFromServer.generateRandomNumber(In, paramA[1], Cn, endUser.q); // Using the generateRandomNumber method to generate each entry
            }
        }
        return matrixA;
    }
    // Constructs the matrix X by hashing each Blocks block and filling the vectors xi. N is m*N
    public static BigInteger[][] constructMatrixX(EndUser endUser, int N, String[] dataBlocks) {
        if (dataBlocks.length != N) {
            throw new IllegalArgumentException("Number of Blocks blocks should be equal to N");
        }

        BigInteger[][] X = new BigInteger[endUser.m][N];

        for (int i = 0; i < endUser.m; i++) {
            // Calculate the starting index for each injection
            int bitIndex = i % endUser.lambda;
            for (int j = 0; j < N; j++) {
                byte[] hash = FunctionsFromServer.hashFunctionH3(dataBlocks[j]); // Hashing each Blocks block

                // Fill the matrix X with the hash bit sequences
                X[i][j] = new BigInteger(1, hash).testBit(bitIndex) ? BigInteger.ONE : BigInteger.ZERO;
            }
        }
        return X;
    }

    // Computes the matrix V as V = A*X mod q
    public static BigInteger[][] computeMatrixV(EndUser endUser, int N, BigInteger[][] A, BigInteger[][] X) {
        // Convert BigInteger matrices to double arrays
        double[][] AArray = new double[endUser.l][endUser.m];
        double[][] XArray = new double[endUser.m][N];

        for (int i = 0; i < endUser.l; i++) {
            for (int j = 0; j < endUser.m; j++) {
                AArray[i][j] = A[i][j].doubleValue();
            }
        }

        for (int i = 0; i < endUser.m; i++) {
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
        BigInteger[][] V = new BigInteger[endUser.l][N];
        for (int i = 0; i < endUser.l; i++) {
            for (int j = 0; j < N; j++) {
                V[i][j] = BigInteger.valueOf((long) VMatrix.getEntry(i, j)).mod(endUser.q);
            }

        }
        return V;
    }

}
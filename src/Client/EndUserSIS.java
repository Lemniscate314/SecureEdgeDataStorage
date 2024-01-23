package Client;

import java.math.BigInteger;

public class EndUserSIS {
    public BigInteger[][] computeMatrixA(int nbRow, int nbColumn, BigInteger I0, BigInteger C0, BigInteger a) {
        BigInteger[][] matrixA = new BigInteger[nbRow][nbColumn];
        In = I0;
        Cn = C0;
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                matrixA[i][j] = generateRandomNumber(); // Using the generateRandomNumber method to generate each entry
            }
        }
        return matrixA;
    }
}

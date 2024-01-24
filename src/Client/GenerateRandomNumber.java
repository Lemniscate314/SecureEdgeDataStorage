package Client;

import java.math.BigInteger;

public class GenerateRandomNumber {
    // Implement I, a uniform random number generator
    public static BigInteger generateRandomNumber(BigInteger In, BigInteger Cn, BigInteger a, BigInteger q) {
        BigInteger product = a.multiply(In).add(Cn);
        BigInteger nextIn = product.mod(q); // Calculate In+1
        Cn = product.divide(q); // Calculate Cn+1
        In = nextIn; // Update In for the next iteration
        return nextIn;
    }
}

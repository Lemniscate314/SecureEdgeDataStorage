package Client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FunctionsFromServer {
    // Implement I, a uniform random number generator
    public static BigInteger[] generateRandomNumber(BigInteger In, BigInteger Cn, BigInteger a, BigInteger q) {
        BigInteger product = a.multiply(In).add(Cn);
        BigInteger nextIn = product.mod(q); // Calculate In+1
        Cn = product.divide(q); // Calculate Cn+1
        In = nextIn; // Update In for the next iteration
        return new BigInteger[]{nextIn, In, Cn};
    }
    public static byte[] hashFunctionH3(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

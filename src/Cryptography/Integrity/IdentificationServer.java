package Cryptography.Integrity;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class IdentificationServer {
    private static Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    private static Field G0 = pairing.getG1();
    private static Field G1 = pairing.getG2();
    private static Field Zp = pairing.getZr();

    private Element P;
    private Element ts;

    public IdentificationServer() {
        P = G0.newRandomElement().getImmutable();

        ts = Zp.newRandomElement().getImmutable();
    }

    public Element generatePublicKey() {
        Element PKs = P.duplicate().mulZn(ts).getImmutable();
        return PKs;
    }
    private Element hashFunctionH1(String identity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(identity.getBytes());

            return G0.newElementFromHash(hash, 0, hash.length).getImmutable();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Element hashFunctionH2(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            return Zp.newElementFromBytes(hash).getImmutable();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] hashFunctionH3(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        IdentificationServer server = new IdentificationServer();
        Element publicKey = server.generatePublicKey();
        System.out.println("Public Key: " + publicKey);
    }
}

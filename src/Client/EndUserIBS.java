package Client;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import java.util.Properties;

public class EndUserIBS {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();

    public static IBSsignature IBS_signature_generation(EndUser endUser, Block block) {
        IBSsignature sigma = new IBSsignature();
        Element k = Zp.newRandomElement();
        Element P1 = G0.newRandomElement();

        Element r = pairing.pairing(endUser.P, P1).powZn(k);
        sigma.setW1(pairing(block.V.toBytes() + block.paramA[0].toBytes() + block.paramA[1].toBytes() + block.paramA[2].toBytes + r.toBytes()));

        sigma.setW2((endUser.Sw.duplicate().mulZn(sigma.getW1())).add(P1.duplicate().mulZn(k)));
        return sigma;
    }

    public static boolean IBS_signature_verification(EndUser endUser, Block block) {
        boolean bool = false;

        String [] D = new String [1];
        D[0] = block.Di;
        // On construit le vecteur xi
        BigInteger[][] X = EndUserSIS.constructMatrixX(endUser, block.V.length, D);
        // On reconstrut la matrice A à partir des paramA reçus
        BigInteger[][] A = EndUserSIS.computeMatrixA(endUser, block.paramA);

        // On construit la matrice V' que l'on complète avec V
        BigInteger[][] W = EndUserSIS.computeMatrixV(endUser, block.V.length, A, X);
        BigInteger[][] Vprime = block.V;
        for (int i = 0; i < Vprime[block.i].length; i++) {
            if (block.i==i) {
                Vprime[block.i][i] = W[0][i];
            }
        }

        byte[] IDbytes = block.IDw.getBytes();
        // On applique la fonction de hachage H1 à l'ID
        Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);

        Element rprime = pairing.pairing(block.signature.getW2(), endUser.P)
                .mul(pairing.pairing(Qid, endUser.PK.duplicate().negate()).mulZn(block.signature.getW1()));
        bool = block.signature.getW1().isEqual(pairing(
                Vprime.toBytes() + block.paramA[0].toBytes() + block.paramA[1].toBytes() + block.paramA[2].toBytes + rprime.toBytes()));
        return bool;
    }

    private static byte[] hashFunctionH3(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

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

    public static IBSsignature IBS_signature_generation(Element P, String ID, Element Sw, BigInteger[][] V, Object[] paramA) {
        IBSsignature sigma = new IBSsignature();
        Element k = Zp.newRandomElement();
        Element P1 = G0.newRandomElement();

        Element r = pairing.pairing(P, P1).powZn(k);
        sigma.setW1(pairing(V.toBytes() + paramA[0].toBytes() + paramA[1].toBytes() + paramA[2].toBytes + r.toBytes()));

        sigma.setW2((Sw.duplicate().mulZn(sigma.getW1())).add(P1.duplicate().mulZn(k)));
        return sigma;
    }

    public static boolean IBS_signature_verification(Element P, Element PK, IBSsignature signature, String IDw, HashMap<String, String> dataBlocksI,
            Object[] paramA, BigInteger[][] V) {
        boolean bool = false;
        // On initialise des tableaux qui vont stocker les données récupérées et le
        // numero de block associé (contenu dans le HashMap)
        String[] dataBlocks = new String[dataBlocksI.size()];
        int[] dataNumber = new int[dataBlocksI.size()];

        // Parcourir la HashMap et mettre chaque blocket numero de block dans le tableau
        int i = 0;
        for (Map.Entry<String, String> entry : dataBlocksI.entrySet()) {
            dataBlocks[i] = entry.getValue();
            dataNumber[i] = Integer.parseInt(entry.getValue());
            i++;
        }
        // On construit les vecteurs xi
        BigInteger[][] X = EndUserSIS.constructMatrixX(dataBlocks);
        // On reconstrut la matrice A à partir des paramA reçus
        BigInteger[][] A = EndUserSIS.generateMatrixA(paramA[0], paramA[1], paramA[2]);

        // On construit la matrice V' que l'on complète avec V
        BigInteger[][] W = EndUserSIS.computeMatrixV(A, X);
        BigInteger[][] Vprime = V;
        for (i = 0; i < dataNumber.length; i++) {
            Vprime[dataNumber[i]] = W[dataNumber[i]];
        }

        byte[] IDbytes = IDw.getBytes();
        // On applique la fonction de hachage H1 à l'ID
        Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);

        Element rprime = pairing.pairing(signature.getW2(), P)
                .mul(pairing.pairing(Qid, PK.duplicate().negate()).mulZn(signature.getW1()));
        bool = signature.getW1().isEqual(pairing(
                Vprime.toBytes() + paramA[0].toBytes() + paramA[1].toBytes() + paramA[2].toBytes + rprime.toBytes()));
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

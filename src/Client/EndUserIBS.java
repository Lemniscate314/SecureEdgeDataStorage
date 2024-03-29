package Client;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class EndUserIBS {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();

    public static IBSsignature IBS_signature_generation(EndUser endUser, Blocks blocks) {
        IBSsignature sigma = new IBSsignature();
        Element k = Zp.newRandomElement();
        Element P1 = G0.newRandomElement();

        Element r = pairing.pairing(endUser.P, P1).powZn(k);
        byte[] concatenation = concatenateByteArrays(Blocks.VtoBytes(blocks.V), Blocks.paramAtoBytes(blocks.paramA), r.toBytes());
        sigma.setW1(Zp.newElementFromHash(concatenation, 0, concatenation.length));
        sigma.setW2((endUser.Sw.duplicate().mulZn(sigma.getW1())).add(P1.duplicate().mulZn(k)));
        return sigma;
    }

    public static boolean IBS_signature_verification(EndUser endUser, Blocks blocks) {
        boolean bool = false;

        // On construit le vecteur xi
        BigInteger[][] X = EndUserSIS.computeMatrixX(endUser, blocks.dataBlocks.length, blocks.dataBlocks);
        // On reconstrut la matrice A à partir des paramA reçus
        BigInteger[][] A = EndUserSIS.computeMatrixA(endUser, blocks.paramA);

        // On construit la matrice V' que l'on complète avec V
        BigInteger[][] W = EndUserSIS.computeMatrixV(endUser, blocks.dataBlocks.length, A, X);
        BigInteger[][] Vprime = blocks.V;
        for (int i = 0; i < Vprime.length; i++) {
            for (int j = 0; j < Vprime[0].length; j++) {
                if (blocks.dataBlocksMap.get(blocks.dataBlocks[j]).intValue() == j) {
                    Vprime[i][j] = W[i][j];
                }
            }
        }

        byte[] IDbytes = blocks.IDw.getBytes();
        // On applique la fonction de hachage H1 à l'ID
        Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);

        Element rprime1 = pairing.pairing(blocks.signature.getW2(), endUser.P);
        Element rprime2 = pairing.pairing(Qid, endUser.PK.duplicate().negate()).mulZn(blocks.signature.getW1());
        Element rprime = rprime1.duplicate().mul(rprime2.duplicate());

        //On concatene V paramA et rprime
        byte[] concatenation = concatenateByteArrays(Blocks.VtoBytes(Vprime), Blocks.paramAtoBytes(blocks.paramA), rprime.toBytes());
        bool = blocks.signature.getW1().isEqual(Zp.newElementFromHash(concatenation, 0, concatenation.length));
        if (bool == true){System.out.println("Signature correct");}
        else{System.out.println("Problème d'intégrité");}
        return bool;
    }

    public static byte[] concatenateByteArrays(byte[] array1, byte[] array2, byte[] array3) {
        int length1 = array1.length;
        int length2 = array2.length;
        int length3 = array3.length;
        byte[] result = new byte[length1 + length2 + length3];

        // Copier le premier tableau
        System.arraycopy(array1, 0, result, 0, length1);
        // Copier le deuxième tableau après le premier
        System.arraycopy(array2, 0, result, length1, length2);
        // Copier le troisième tableau après le deuxième
        System.arraycopy(array3, 0, result, length1 + length2, length3);
        return result;
    }
    public static void printByteArray(byte[] byteArray) {
        for (byte b : byteArray) {
            System.out.print(b + " ");
        }
        System.out.println(); // Ajoutez une nouvelle ligne à la fin pour une meilleure lisibilité
    }
}

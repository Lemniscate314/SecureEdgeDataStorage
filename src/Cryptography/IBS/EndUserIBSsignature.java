package Cryptography.IBS;

import Sis.DataMatrixConstruction;

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
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import java.util.Properties;

public class EndUserIBSsignature {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();
    protected Element P;
    protected Element PK;
    protected String ID;
    protected Element Sw;
    static SecureRandom random = new SecureRandom();
    public static final BigInteger a = new BigInteger(128, random); // The multiplier
    static final BigInteger I0 = new BigInteger(128, random); // Initial value of I
    static final BigInteger C0 = new BigInteger(128, random); // Initial value of C
    protected static String configFilePath = "src/Cryptography/IBS/UserParameters.properties";

    public EndUserIBSsignature() {
        load_Sw_PK_P();
    }

    public static BigInteger getA() {
        return a;
    }

    public static BigInteger getI0() {
        return I0;
    }

    public static BigInteger getC0() {
        return C0;
    }

    public static Pairing getPairing() {
        return pairing;
    }

    public static Field getZp() {
        return Zp;
    }

    public static Field getG0() {
        return G0;
    }

    public static Field getG1() {
        return G1;
    }

    public Element getP() {
        return P;
    }

    public void setP(Element p) {
        P = p;
    }

    public Element getPK() {
        return PK;
    }

    public void setPK(Element PK) {
        this.PK = PK;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Element getSw() {
        return Sw;
    }

    public void setSw(Element sw) {
        Sw = sw;
    }

    public static String getConfigFilePath() {
        return configFilePath;
    }

    public static void setConfigFilePath(String configFilePath) {
        EndUserIBSsignature.configFilePath = configFilePath;
    }

    // Fonction qui load les paramètres depuis le fichier configFilePath
    // Si le fichier est vide alors elle print qu'il faut requeter le serveur
    protected void load_Sw_PK_P() {
        // Fichier de configuration pour stocker la clé secrète
        Properties prop = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(configFilePath);
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String chaine1 = prop.getProperty("Sw");
        String chaine2 = prop.getProperty("PK");
        String chaine3 = prop.getProperty("P");
        if (chaine1.length() != 0 && chaine2.length() != 0) {// La clé existe et est stocké dans le fichier
            try {
                this.Sw = G0.newElementFromBytes(Base64.decode(chaine1));
                this.PK = G0.newElementFromBytes(Base64.decode(chaine2));
                this.P = G0.newElementFromBytes(Base64.decode(chaine3));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(
                    "Besoin de récupérer les parametres publics du serveur.\nVoir la fonction new_Sw_PK_P() de la classe EndUserIBSsignature.");
        }
    }

    // Fonction qui récupère les paramètres publics utiles à IBS et gère l'écriture
    // dans un fichier
    public void new_Sw_PK_P(Element newSw, Element newPK, Element newP, Element newN, Element newM, Element newQ) {
        Properties prop = new Properties();
        this.Sw = newSw;
        this.PK = newSw;
        this.P = newP;
        this.n = newN;
        this.m = newM;
        this.q = newQ;
        try {
            // On convertit les Elements en string
            prop.setProperty("Sw", Base64.encodeBytes(this.Sw.toBytes()));
            prop.setProperty("PK", Base64.encodeBytes(this.PK.toBytes()));
            prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
            prop.setProperty("n", Base64.encodeBytes(this.n.toBytes()));
            prop.setProperty("m", Base64.encodeBytes(this.m.toBytes()));
            prop.setProperty("q", Base64.encodeBytes(this.q.toBytes()));
            prop.store(new FileOutputStream(configFilePath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IBSsignature IBS_signature_generation(Element P, String ID, BigInteger[][] V, Object[] paramA) {
        IBSsignature sigma = new IBSsignature();
        Element k = Zp.newRandomElement();
        Element P1 = G0.newRandomElement();

        Element r = pairing.pairing(this.P, P1).powZn(k);
        sigma.setW1(pairing(V.toBytes() + paramA[0].toBytes() + paramA[1].toBytes() + paramA[2].toBytes + r.toBytes()));

        sigma.setW2((Sw.duplicate().mulZn(sigma.getW1())).add(P1.duplicate().mulZn(k)));
        return sigma;
    }

    public boolean IBS_signature_verification(IBSsignature signature, String IDw, HashMap<String, String> dataBlocksI,
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
        BigInteger[][] X = constructMatrixX(dataBlocks);
        // On reconstrut la matrice A à partir des paramA reçus
        BigInteger[][] A = generateMatrixA(paramA[0], paramA[1], paramA[2]);

        // On construit la matrice V' que l'on complète avec V
        BigInteger[][] W = computeMatrixV(A, X);
        BigInteger[][] Vprime = V;
        for (i = 0; i < dataNumber.length; i++) {
            Vprime[dataNumber[i]] = W[dataNumber[i]];
        }

        byte[] IDbytes = ID.getBytes();
        // On applique la fonction de hachage H1 à l'ID
        Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);

        Element rprime = pairing.pairing(signature.getW2(), P)
                .mul(pairing.pairing(Qid, PK.duplicate().negate()).mulZn(signature.getW1()));
        bool = signature.getW1().isEqual(pairing(
                Vprime.toBytes() + paramA[0].toBytes() + paramA[1].toBytes() + paramA[2].toBytes + rprime.toBytes()));
        return bool;
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

    protected byte[] XOR(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) ((int) a[i] ^ (int) b[i]);
        }
        return c;
    }
    /*
     * public IBSsignature Encryption_Basic_IBE(Element P, Element Ppub, String ID,
     * String message){
     * IBSsignature C = new IBSsignature();
     * Element r = Zr.newRandomElement();
     * C.setW1(P.duplicate().mulZn(r));
     * byte[] IDbytes = ID.getBytes();
     * //On applique la fonction de hachage H1 à l'ID
     * Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);
     * //On applique le couplage sur Ppub et Qid puis le hachage par H2
     * C.setW2(pairing.pairing(Qid, Ppub).powZn(r).toBytes());
     * //On effectue un XOR avec le message en clair
     * C.setW2(XOR(message.getBytes(), C.getW2()));
     * return C;
     * }
     * public byte[] Decryption_Basic_IBE(Element P, Element Ppub, Element
     * private_key_ID, IBSsignature C){
     * byte[] M2 = pairing.pairing(private_key_ID, C.getW1()).toBytes();
     * byte[] M = XOR(C.getW2(), M2);
     * return M;
     * }
     * 
     * public static void main(String[] args){
     * IBSscheme schema = new IBSscheme();
     * IBSsignature cypher = schema.Encryption_Basic_IBE(schema.P, schema.PK,
     * "antoine.auger27@gmail.com", "Bonjour Antoine, comment vas-tu ?");
     * byte[] plaintext = schema.Decryption_Basic_IBE(schema.P, schema.P,
     * schema.generate_private_key_ID("antoine.auger27@gmail.com"), cypher);
     * System.out.println(new String(plaintext, StandardCharsets.US_ASCII));
     * }
     */
}

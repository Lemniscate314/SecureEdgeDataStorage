package Cryptography.IBS;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class IBSscheme {
    EndUserIBSsignature endUserIBSsignature = new EndUserIBSsignature();
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();
    protected Element P;
    protected Element PK;
    protected Element MSK;
    protected HashMap<String, Element>  Key_couples= new HashMap();
    protected ArrayList<String> IDs= new ArrayList();
    protected static String configFilePath = "src/Cryptography/IBS/ServerParameters.properties";
    static SecureRandom random = new SecureRandom();
    public BigInteger a = endUserIBSsignature.getA(); // The multiplier
    public BigInteger I0 = endUserIBSsignature.getI0();  // Initial value of I
    public BigInteger C0 = endUserIBSsignature.getC0();  // Initial value of C
    private BigInteger In; // Current value of I
    private BigInteger Cn; // Current value of C
    public static int l = 2; // Primary security parameter
    public static BigInteger q = BigInteger.valueOf(l).pow(2).nextProbablePrime(); // The modulus q = next probable prime number of l^2
    public static int m = (int)(l * Math.log(q.doubleValue()) / Math.log(2)) + 1; // Ensuring m > l.log(q)
    public BigInteger[][] generatedMatrixA = computeMatrixA(l, m);


    // Constructor
    public IBSscheme(){
        generate_MSK_P();
        this.PK = (this.P).duplicate().mulZn(this.MSK);
        //On reconstruit les clés privés et utilisateurs
        Key_couples.clear();
        build_HashMap();

        //SIS parameters initialization
        this.In = I0; // Initialize In with I0
        this.Cn = C0; // Initialize Cn with C0
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
    public Element getPK() {
        return PK;
    }
    public Element getMSK() {
        return MSK;
    }
    public HashMap<String, Element> getKey_couples() {
        return Key_couples;
    }
    public ArrayList<String> getIDs() {
        return IDs;
    }
    public BigInteger getIn() {
        return In;
    }
    public void setIn(BigInteger in) {
        In = in;
    }

    public BigInteger getQ() {
        return q;
    }

    public int getL() {
        return l;
    }

    public int getM() {
        return m;
    }

    // Implement I, a uniform random number generator
    public BigInteger generateRandomNumber() {
        In = I0;
        Cn = C0;
        BigInteger product = a.multiply(In).add(Cn);
        BigInteger nextIn = product.mod(q); // Calculate In+1
        Cn = product.divide(q); // Calculate Cn+1
        In = nextIn; // Update In for the next iteration
        return nextIn;
    }

    public BigInteger[][] computeMatrixA(int nbRow, int nbColumn) {
        BigInteger[][] matrixA = new BigInteger[nbRow][nbColumn];
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                matrixA[i][j] = generateRandomNumber(); // Using the generateRandomNumber method to generate each entry
            }
        }
        return matrixA;
    }

    protected void new_IBSscheme(){
        new_MSK_P();
        this.PK = (this.P).duplicate().mulZn(this.MSK);
    }

    //Fonction qui genere la clé privé maitre et gere la lecture dans un fichier
    protected void generate_MSK_P(){
        //Fichier de configuration pour stocker la clé secrète
        Properties prop = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(configFilePath);
            prop.load(in);
        } catch(IOException e) {e.printStackTrace();}
        String chaine1 = prop.getProperty("MSK");
        String chaine2 = prop.getProperty("P");
        if (chaine1.length() != 0 && chaine2.length() != 0){//La clé existe et est stocké dans le fichier
            try{
                this.MSK = Zp.newElementFromBytes(Base64.decode(chaine1));
                this.P = G0.newElementFromBytes(Base64.decode(chaine2));
            } catch(IOException e) {e.printStackTrace();}
        }
        else{new_MSK_P();}
    }

    //Fonction qui genere une nouvelle clé privé maitre et gère l'écriture dans un fichier
    protected void new_MSK_P(){
        Properties prop = new Properties();
        this.MSK = Zp.newRandomElement();
        this.P = G0.newRandomElement();
        try{
            //On convertit les Elements en string
            prop.setProperty("MSK", Base64.encodeBytes(this.MSK.toBytes()));
            prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
            prop.setProperty("l", Base64.encodeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(this.l).array()));
            prop.setProperty("m", Base64.encodeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(this.m).array()));
            prop.setProperty("q", Base64.encodeBytes(this.q.toByteArray()));
            prop.store(new FileOutputStream(configFilePath), null);
        } catch(IOException e) {e.printStackTrace();}
    }

    public Element generate_private_key_ID(String ID){
        if (Key_couples.get(ID) == null) {
            byte[] IDbytes = ID.getBytes();
            //On applique la fonction de hachage H1 à l'ID
            Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);
            //On calcule la clé privé de l'utilisateur ID
            Element private_key_ID = Qid.duplicate().mulZn(this.MSK);
            //On l'ajoute dans le Hashmap
            this.Key_couples.put(ID, private_key_ID);
            return private_key_ID;
        }
        else {return Key_couples.get(ID);}
    }
    protected void build_HashMap(){
        for (String adresse: IDs){generate_private_key_ID(adresse);}
    }
}

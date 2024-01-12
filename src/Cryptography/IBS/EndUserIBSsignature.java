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
    protected static String configFilePath = "src/Cryptography/IBS/UserParameters.properties";

    public EndUserIBSsignature(){
        load_Sw_PK_P();
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


    //Fonction qui genere la clé privé maitre et gere la lecture dans un fichier
    protected void load_Sw_PK_P(){
        //Fichier de configuration pour stocker la clé secrète
        Properties prop = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(configFilePath);
            prop.load(in);
        } catch(IOException e) {e.printStackTrace();}
        String chaine1 = prop.getProperty("Sw");
        String chaine2 = prop.getProperty("PK");
        String chaine3 = prop.getProperty("P");
        if (chaine1.length() != 0 && chaine2.length() != 0){//La clé existe et est stocké dans le fichier
            try{
                this.Sw = G0.newElementFromBytes(Base64.decode(chaine1));
                this.PK = G0.newElementFromBytes(Base64.decode(chaine2));
                this.P = G0.newElementFromBytes(Base64.decode(chaine3));
            } catch(IOException e) {e.printStackTrace();}
        }
        else{
            System.out.println("Besoin de récupérer les parametres publics du serveur.\nVoir la fonction new_Sw_PK_P() de la classe EndUserIBSsignature.");
        }
    }

    //Fonction qui genere une nouvelle clé privé maitre et gère l'écriture dans un fichier
    public void new_Sw_PK_P(Element newSw, Element newPK, Element newP){
        Properties prop = new Properties();
        this.Sw = newSw;
        this.PK = newSw;
        this.P = newP;
        try{
            //On convertit les Elements en string
            prop.setProperty("Sw", Base64.encodeBytes(this.Sw.toBytes()));
            prop.setProperty("PK", Base64.encodeBytes(this.PK.toBytes()));
            prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
            prop.store(new FileOutputStream(configFilePath), null);
        } catch(IOException e) {e.printStackTrace();}
    }

    public IBSsignature IBS_signature_generation(Element P, String ID) {
        IBSsignature sigma = new IBSsignature();
        Element k = Zp.newRandomElement();
        Element P1 = G0.newRandomElement();

        Element r = pairing.pairing(this.P, P1).powZn(k);
        //En attente de la partie 3.4.3.1 et 3.4.3.2
        //sigma.setW1(pairing(V.toBytes() + paramA.toBytes() + r.toBytes()));

        sigma.setW2((Sw.duplicate().mulZn(sigma.getW1())).add(P1.duplicate().mulZn(k)));
        return sigma;
    }

    public boolean IBS_signature_verification(IBSsignature signature, Element P, Element PK, String ID) {
        boolean bool = false;
        //En attente de la partie 3.4.3.1 et 3.4.3.2
        byte[] IDbytes = ID.getBytes();
        //On applique la fonction de hachage H1 à l'ID
        Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);
        Element rprime = pairing.pairing(signature.getW2(), P).mul(pairing.pairing(Qid, PK.duplicate().negate()).mulZn(signature.getW1()));
        //bool = signature.getW1().isEqual(pairing(Vprime.toBytes() + paramA.toBytes() + rprime.toBytes()));
        return bool;
    }
    protected byte[] XOR(byte[] a, byte[] b){
        byte[] c = new byte[a.length];
        for(int i=0; i<a.length; i++){c[i ]= (byte) ((int)a[i]^(int)b[i]);}
        return c;
    }
    /*
    public IBSsignature Encryption_Basic_IBE(Element P, Element Ppub, String ID, String message){
        IBSsignature C = new IBSsignature();
        Element r = Zr.newRandomElement();
        C.setW1(P.duplicate().mulZn(r));
        byte[] IDbytes = ID.getBytes();
        //On applique la fonction de hachage H1 à l'ID
        Element Qid = G0.newElementFromHash(IDbytes, 0, IDbytes.length);
        //On applique le couplage sur Ppub et Qid puis le hachage par H2
        C.setW2(pairing.pairing(Qid, Ppub).powZn(r).toBytes());
        //On effectue un XOR avec le message en clair
        C.setW2(XOR(message.getBytes(), C.getW2()));
        return C;
    }
    public byte[] Decryption_Basic_IBE(Element P, Element Ppub, Element private_key_ID, IBSsignature C){
        byte[] M2 = pairing.pairing(private_key_ID, C.getW1()).toBytes();
        byte[] M = XOR(C.getW2(), M2);
        return M;
    }

    public static void main(String[] args){
        IBSscheme schema = new IBSscheme();
        IBSsignature cypher = schema.Encryption_Basic_IBE(schema.P, schema.PK, "antoine.auger27@gmail.com", "Bonjour Antoine, comment vas-tu ?");
        byte[] plaintext = schema.Decryption_Basic_IBE(schema.P, schema.P, schema.generate_private_key_ID("antoine.auger27@gmail.com"), cypher);
        System.out.println(new String(plaintext, StandardCharsets.US_ASCII));
    }
     */
}


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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
public class IBSscheme {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zr = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();
    protected Element P;
    protected Element PK;
    protected Element MSK;
    protected HashMap<String, Element>  Key_couples= new HashMap();
    protected ArrayList<String> IDs= new ArrayList();
    protected static String configFilePath = "src/Cryptography/IBS/MSK.properties";

    public IBSscheme(){
        generate_MSK_P();
        this.PK = (this.P).duplicate().mulZn(this.MSK);
        //On reconstruit les clés privés et utilisateurs
        Key_couples.clear();
        build_HashMap();
    }

    public static Pairing getPairing() {
        return pairing;
    }
    public static Field getZr() {
        return Zr;
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

    protected void new_Setup_IBS(){
        new_MSK_P();
        this.PK = (this.P).duplicate().mulZn(this.MSK);
        //On reconstruit les clés privés et utilisateurs
        Key_couples.clear();
        build_HashMap();
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
                this.MSK = Zr.newElementFromBytes(Base64.decode(chaine1));
                this.P = G0.newElementFromBytes(Base64.decode(chaine2));
            } catch(IOException e) {e.printStackTrace();}
        }
        new_MSK_P();
    }

    //Fonction qui genere une nouvelle clé privé maitre et gère l'écriture dans un fichier
    protected void new_MSK_P(){
        Properties prop = new Properties();
        this.MSK = Zr.newRandomElement();
        this.P = G0.newRandomElement();
        try{
            //On convertit les Elements en string
            prop.setProperty("MSK", Base64.encodeBytes(this.MSK.toBytes()));
            prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
            prop.store(new FileOutputStream(configFilePath), null);
        } catch(IOException e) {e.printStackTrace();}
    }

    public Element[] Public_Parameters(){
        Element[] PP = new Element[2];
        PP[0] = this.P;
        PP[1] = this.PK;
        return PP;
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

    public IBSsignature IBS_signature_generation(Element P, String ID) {
        IBSsignature sigma = new IBSsignature();
        Element k = Zr.newRandomElement();
        Element P1 = G0.newRandomElement();

        Element r = pairing.pairing(this.P, P1).powZn(k);
        //En attente de la partie 3.4.3.1 et 3.4.3.2
        //sigma.setW1(pairing(V.toBytes() + paramA.toBytes() + r.toBytes()));

        sigma.setW2((generate_private_key_ID(ID).duplicate().mulZn(sigma.getW1())).add(P1.duplicate().mulZn(k)));
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

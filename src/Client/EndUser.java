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
import java.util.Properties;

import com.google.gson.Gson;


public class EndUser {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();
    protected Element P;
    protected Element PK;
    protected String ID;
    protected Element Sw;
    protected int l;     // l is the number of rows in matrix A
    protected int m;     // m is the number of columns in matrix A
    protected BigInteger q;     // The modulus, a polynomial of l
    protected final static int lambda = 256;       // Parameter lambda
    protected static SecureRandom random = new SecureRandom();
    protected BigInteger I0; // Initial value of I
    protected BigInteger a; // The multiplier
    protected BigInteger C0; // Initial value of C
    protected BigInteger[] paramA; //parameters to regenerate A
    protected BigInteger In; // Current value of I
    protected BigInteger Cn; // Current value of C
    protected static String configFilePath = "src/Client/UserParameters.properties";
    protected boolean loadingSuccessful; //Attribut permettant de determiner si une requete au server est necessaire

    public EndUser(String email) {
        this.ID=email;
        this.loadingSuccessful=load_Public_Parameters_Sw();
        load_ParamA();
        this.paramA = new BigInteger[]{this.I0, this.a, this.C0};
        this.In = I0; // Initialize In with I0
        this.Cn = C0; // Initialize Cn with C0
    }

    // Fonction qui load les paramètres de la matrice A depuis le fichier configFilePath
    // Si le fichier est vide alors elle génère de nouveau paramA
    protected boolean load_ParamA() {
        // Fichier de configuration pour stocker la clé secrète
        Properties prop = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(configFilePath);
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String chaine1 = prop.getProperty("I0");
        String chaine2 = prop.getProperty("a");
        String chaine3 = prop.getProperty("C0");
        if (chaine1 != null && !chaine1.isEmpty() &&
                chaine2 != null && !chaine2.isEmpty() &&
                chaine3 != null && !chaine3.isEmpty()) {// La clé existe et est stocké dans le fichier
            try {
                this.I0 = new BigInteger(chaine1);
                this.a = new BigInteger(chaine2);
                this.C0 = new BigInteger(chaine3);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            System.out.println("Loading successful");
            return true;
        }
        else {
            System.out.println("Nouveaux Paramètres pour la matrice A.");
            return false;
        }
    }

    // Fonction qui génère de nouveaux paramètre de la matrice A et gère l'écriture dans un fichier
    public void new_ParamA() {
        Properties prop = new Properties();
        this.I0 = new BigInteger(128, random); // Initial value of I
        this.a = new BigInteger(128, random); // The multiplier
        this.C0 = new BigInteger(128, random); // Initial value of C

        //generateur de nombre pseud-aléatoire
        //this.I=Public_Parameters_Sw[6];
        try {
            // On convertit les Elements en string
            prop.setProperty("I0", this.I0.toString());
            prop.setProperty("a", this.a.toString());
            prop.setProperty("C0", this.C0.toString());
            prop.store(new FileOutputStream(configFilePath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fonction qui load les paramètres depuis le fichier configFilePath
    // Si le fichier est vide alors elle print qu'il faut requeter le serveur
    protected boolean load_Public_Parameters_Sw() {
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
        String chaine2 = prop.getProperty("P");
        String chaine3 = prop.getProperty("PK");
        String chaine4 = prop.getProperty("l");
        String chaine5 = prop.getProperty("m");
        String chaine6 = prop.getProperty("q");
        if (chaine1 != null && !chaine1.isEmpty() &&
                chaine2 != null && !chaine2.isEmpty() &&
                chaine3 != null && !chaine3.isEmpty() &&
                chaine4 != null && !chaine4.isEmpty() &&
                chaine5 != null && !chaine5.isEmpty() &&
                chaine6 != null && !chaine6.isEmpty()) {// La clé existe et est stocké dans le fichier
            try {
                this.Sw = G0.newElementFromBytes(Base64.decode(chaine1));
                this.PK = G0.newElementFromBytes(Base64.decode(chaine2));
                this.P = G0.newElementFromBytes(Base64.decode(chaine3));
                this.l = Integer.parseInt(chaine4);
                this.m = Integer.parseInt(chaine5);
                this.q = new BigInteger(chaine6);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Loading successful");
            return true;
        }
        else {
            System.out.println("Besoin de récupérer les parametres publics du serveur.");
            return false;
        }
    }

    // Fonction qui récupère les paramètres publics et gère l'écriture dans un fichier
    public void get_Public_Parameters_Sw(String publicParametersJson) {
        Gson gson = new Gson();
        PropertiesObject propertiesObject = gson.fromJson(publicParametersJson, PropertiesObject.class);
        try { //On stocke les parametres dans le fichier
            Properties prop = new Properties();
            prop.setProperty("Sw", propertiesObject.Sw);
            prop.setProperty("P", propertiesObject.P);
            prop.setProperty("PK", propertiesObject.PK);
            prop.setProperty("l", propertiesObject.l);
            prop.setProperty("m", propertiesObject.m);
            prop.setProperty("q", propertiesObject.q);
            prop.store(new FileOutputStream(configFilePath), null);
            //On instancie maintenant nos attributs
            this.Sw = G0.newElementFromBytes(Base64.decode(prop.getProperty("Sw")));
            this.PK = G0.newElementFromBytes(Base64.decode(prop.getProperty("SPK")));
            this.P = G0.newElementFromBytes(Base64.decode(prop.getProperty("P")));
            this.l = Integer.parseInt(prop.getProperty("l"));
            this.m = Integer.parseInt(prop.getProperty("m"));
            this.q = new BigInteger(prop.getProperty("q"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully receive parameters from Server");
    }
    // Classe pour stocker les paramètres du fichier .properties et la clé privé d'un EndUser
    static class PropertiesObject {
        String P;
        String PK;
        String l;
        String m;
        String q;
        String Sw;
    }
}

package Server;

import com.google.gson.Gson;

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

public class IdentificationServer {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();
    protected Element P;
    protected Element PK;
    protected Element MSK;
    protected HashMap<String, Element>  Key_couples= new HashMap();
    protected ArrayList<String> IDs= new ArrayList();
    protected static String configFilePath = "src/Server/ServerParameters.properties";
    static SecureRandom random;
    public static int l; // Primary security parameter
    public static BigInteger q; // The modulus q = next probable prime number of l^2
    public static int m; // Ensuring m > l.log(q)
    //On convertit la fonction en json
    //String jsonFunction = new Gson().toJson(new Serverfunctions());


    // Constructor
    public IdentificationServer(){
        load_Public_Parameters_MSK();
    }

    //Fonction qui genere la clé privé maitre et gere la lecture dans un fichier
    protected void load_Public_Parameters_MSK(){
        //Fichier de configuration pour stocker la clé secrète
        Properties prop = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(configFilePath);
            prop.load(in);
        } catch(IOException e) {e.printStackTrace();}
        String chaine1 = prop.getProperty("MSK");
        String chaine2 = prop.getProperty("P");
        String chaine3 = prop.getProperty("PK");
        String chaine4 = prop.getProperty("l");
        String chaine5 = prop.getProperty("m");
        String chaine6 = prop.getProperty("q");
        System.out.println("MSK: " + chaine1);
        System.out.println("P: " + chaine2);
        System.out.println("PK: " + chaine3);
        System.out.println("l: " + chaine4);
        System.out.println("m: " + chaine5);
        System.out.println("q: " + chaine6);

        if (chaine1 != null && !chaine1.isEmpty() &&
                chaine2 != null && !chaine2.isEmpty() &&
                chaine3 != null && !chaine3.isEmpty() &&
                chaine4 != null && !chaine4.isEmpty() &&
                chaine5 != null && !chaine5.isEmpty() &&
                chaine6 != null && !chaine6.isEmpty()) {// La clé existe et est stocké dans le fichier
            try {
                this.MSK = Zp.newElementFromBytes(Base64.decode(chaine1));
                this.P = G0.newElementFromBytes(Base64.decode(chaine2));
                this.PK = G0.newElementFromBytes(Base64.decode(chaine3));
                this.l = Integer.parseInt(chaine4);
                this.m = Integer.parseInt(chaine5);
                this.q = new BigInteger(chaine6);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Loading successful");
        }
        else{
            System.out.println("On genere de nouveaux paramètres publics.");
            new_Public_Parameters_MSK();
        }
    }

    //Fonction qui genere une nouvelle clé privé maitre et gère l'écriture dans un fichier
    protected void new_Public_Parameters_MSK(){
        Properties prop = new Properties();
        this.MSK = Zp.newRandomElement();
        this.P = G0.newRandomElement();
        this.PK = (this.P).duplicate().mulZn(this.MSK);
        this.random = new SecureRandom();
        this.l = 64; // Primary security parameter
        this.q = BigInteger.valueOf(l).pow(2).nextProbablePrime(); // The modulus q = next probable prime number of l^2
        this.m = (int)(2*l * Math.log(q.doubleValue())) + 1; // Ensuring m > l.log(q)
        while (m % 256 != 0) {m++;} // and m//lambda
        try{
            //On convertit les Elements en string
            prop.setProperty("MSK", Base64.encodeBytes(this.MSK.toBytes()));
            prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
            prop.setProperty("PK", Base64.encodeBytes(this.PK.toBytes()));
            prop.setProperty("l", String.valueOf(this.l));
            prop.setProperty("m", String.valueOf(this.m));
            prop.setProperty("q", this.q.toString());
            prop.store(new FileOutputStream(configFilePath), null);
        } catch(IOException e) {e.printStackTrace();}
        //On reconstruit les clés privés et utilisateurs
        Key_couples.clear();
        build_HashMap();
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

    public String send_Public_Parameters_MSK(String ID){
        //Fichier de configuration pour stocker la clé secrète
        Properties prop = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(configFilePath);
            prop.load(in);
        } catch(IOException e) {e.printStackTrace();}
        // Création d'un objet pour stocker les paramètres du fichier .properties
        PropertiesObject propertiesObject = new PropertiesObject();
        propertiesObject.P = prop.getProperty("P");
        propertiesObject.PK = prop.getProperty("PK");
        propertiesObject.l = prop.getProperty("l");
        propertiesObject.m = prop.getProperty("m");
        propertiesObject.q = prop.getProperty("q");
        propertiesObject.Sw = Base64.encodeBytes(generate_private_key_ID(ID).toBytes());

        // Conversion de l'objet en JSON
        Gson gson = new Gson();
        return gson.toJson(propertiesObject);
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

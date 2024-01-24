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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication

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
    static SecureRandom random = new SecureRandom();
    public static int l = 2; // Primary security parameter
    public static BigInteger q = BigInteger.valueOf(l).pow(2).nextProbablePrime(); // The modulus q = next probable prime number of l^2
    public static int m = (int)(l * Math.log(q.doubleValue()) / Math.log(2)) + 1; // Ensuring m > l.log(q)
    //On convertit la fonction en json
    String jsonFunction = new Gson().toJson(new GenerateRandomNumber());


    // Constructor
    public IdentificationServer(){
        load_Public_Parameters_MSK();
        this.PK = (this.P).duplicate().mulZn(this.MSK);
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
        if (chaine1.length() != 0 && chaine2.length() != 0 && chaine3.length() != 0 && chaine4.length() != 0 && chaine5.length() != 0 && chaine6.length() != 0) {// La clé existe et est stocké dans le fichier
            try {
                this.MSK = Zp.newElementFromBytes(Base64.decode(chaine1));
                this.PK = G0.newElementFromBytes(Base64.decode(chaine2));
                this.P = G0.newElementFromBytes(Base64.decode(chaine3));
                this.l = ByteBuffer.wrap(Base64.decode(chaine4)).getInt();
                this.m = ByteBuffer.wrap(Base64.decode(chaine5)).getInt();
                this.q = new BigInteger(Base64.decode(chaine6));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        try{
            //On convertit les Elements en string
            prop.setProperty("MSK", Base64.encodeBytes(this.MSK.toBytes()));
            prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
            prop.setProperty("l", Base64.encodeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(this.l).array()));
            prop.setProperty("m", Base64.encodeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(this.m).array()));
            prop.setProperty("q", Base64.encodeBytes(this.q.toByteArray()));
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

    public Object[] send_Public_Parameters_MSK(String ID){
        Object[] PublicParameters_Sw = new Element[6];
        PublicParameters_Sw[0] = this.generate_private_key_ID(ID);
        PublicParameters_Sw[1] = this.P;
        PublicParameters_Sw[2] = this.PK;
        PublicParameters_Sw[3] = this.l;
        PublicParameters_Sw[4] = this.m;
        PublicParameters_Sw[5] = this.q;
        PublicParameters_Sw[5] = this.jsonFunction;
        return PublicParameters_Sw;
    }

    public static void main(String[] args) {
        SpringApplication.run(IdentificationServer.class, args);
    }

    //Class permettant de repondre au requêtes afin de d'envoyer les Public Parameters
    // et la clé privé associé à l'id reçus par le server
    @RestController
    @RequestMapping("/api")
    public static class ServerController {
        @GetMapping("/getPublicParameters")
        public ResponseEntity<Object[]> getPublicParameters(@RequestParam String id) {
            IdentificationServer identificationServer = new IdentificationServer();
            Object[] publicParameters = identificationServer.send_Public_Parameters_MSK(id);
            return ResponseEntity.ok(publicParameters);
        }
    }
}

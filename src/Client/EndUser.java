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

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


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
    protected static final BigInteger a = new BigInteger(128, random); // The multiplier
    protected static final BigInteger I0 = new BigInteger(128, random); // Initial value of I
    protected static final BigInteger C0 = new BigInteger(128, random); // Initial value of C
    protected BigInteger In; // Current value of I
    protected BigInteger Cn; // Current value of C
    protected BigInteger[][] generatedMatrixA = EndUserSIS.computeMatrixA(l, m, I0, C0, a);
    protected static String configFilePath = "src/Cryptography/IBS/UserParameters.properties";
    protected boolean loadingSuccessful; //Attribut permettant de determiner si une requete au server est necessaire

    public EndUser(String email) {
        this.ID=email;
        this.In = I0; // Initialize In with I0
        this.Cn = C0; // Initialize Cn with C0
        this.loadingSuccessful=load_Public_Parameters_Sw();
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
        if (chaine1.length() != 0 && chaine2.length() != 0 && chaine3.length() != 0 && chaine4.length() != 0 && chaine5.length() != 0 && chaine6.length() != 0) {// La clé existe et est stocké dans le fichier
            try {
                this.Sw = G0.newElementFromBytes(Base64.decode(chaine1));
                this.PK = G0.newElementFromBytes(Base64.decode(chaine2));
                this.P = G0.newElementFromBytes(Base64.decode(chaine3));
                this.l = ByteBuffer.wrap(Base64.decode(chaine4)).getInt();
                this.m = ByteBuffer.wrap(Base64.decode(chaine5)).getInt();
                this.q = new BigInteger(Base64.decode(chaine6));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        else {
            System.out.println("Besoin de récupérer les parametres publics du serveur.");
            return false;
        }
    }

    // Fonction qui récupère les paramètres publics utiles à IBS et gère l'écriture dans un fichier
    public void new_Public_Parameters_Sw(Object[] Public_Parameters_Sw) {
        if (Public_Parameters_Sw.length==6){
            Properties prop = new Properties();
            this.Sw = (Element) Public_Parameters_Sw[0];
            this.P = (Element) Public_Parameters_Sw[1];
            this.PK = (Element) Public_Parameters_Sw[2];
            this.l = (int) Public_Parameters_Sw[3];
            this.m = (int) Public_Parameters_Sw[4];
            this.q = (BigInteger) Public_Parameters_Sw[5];
            //generateur de nombre pseud-aléatoire
            //this.I=Public_Parameters_Sw[6];
            try {
                // On convertit les Elements en string
                prop.setProperty("Sw", Base64.encodeBytes(this.Sw.toBytes()));
                prop.setProperty("P", Base64.encodeBytes(this.P.toBytes()));
                prop.setProperty("PK", Base64.encodeBytes(this.PK.toBytes()));
                prop.setProperty("l", Base64.encodeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(this.l).array()));
                prop.setProperty("m", Base64.encodeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(this.m).array()));
                prop.setProperty("q", Base64.encodeBytes(this.q.toByteArray()));
                prop.store(new FileOutputStream(configFilePath), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{System.out.println("Erreur du nombres de parametres publics");}
    }

    public static void main(String[] args) {
        String mail= "antoine@gmail.com";
        EndUser endUser = new EndUser(mail);
        if (endUser.loadingSuccessful==false) { //On requete le serveur puisque le le loading a echoue
            String serverUrl = "http://localhost:80/api/getPublicParameters?id=" + endUser.ID;

            // Use RestTemplate to make a GET request to the server
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object[]> response = restTemplate.getForEntity(serverUrl, Object[].class);

            // Extract the public parameters from the response
            Object[] publicParameters = response.getBody();

            // Use the public parameters as needed
            // ...
        }
    }
}

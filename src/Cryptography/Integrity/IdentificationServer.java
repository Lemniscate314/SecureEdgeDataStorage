package Cryptography.Integrity;
import Cryptography.IBS.IBSscheme;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IdentificationServer {
    private IBSscheme IBSscheme;

    public IdentificationServer() {
        this.IBSscheme = new IBSscheme();
    }

    public Element[] sendPublicParameters_Sw(String ID){
        Element[] PublicParameters_Sw = new Element[6];
        PublicParameters_Sw[0] = this.IBSscheme.getP();
        PublicParameters_Sw[1] = this.IBSscheme.getPK();
        PublicParameters_Sw[5] = this.IBSscheme.generate_private_key_ID(ID);
        //je suppose que la classe s'appelle SISscheme
        //PP[2] = this.SISscheme.getN();
        //PP[3] = this.SISscheme.getM();
        //PP[4] = this.SISscheme.getQ();
        return PublicParameters_Sw;
    }

    public static void main(String[] args) {
        IdentificationServer server = new IdentificationServer();
    }
}

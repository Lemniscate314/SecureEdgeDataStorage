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

    public Object[] sendPublicParameters_Sw(String ID){
        Object[] PublicParameters_Sw = new Element[6];
        PublicParameters_Sw[0] = this.IBSscheme.getP();
        PublicParameters_Sw[1] = this.IBSscheme.getPK();
        PublicParameters_Sw[5] = this.IBSscheme.generate_private_key_ID(ID);
        PublicParameters_Sw[2] = this.IBSscheme.getL();
        PublicParameters_Sw[3] = this.IBSscheme.getM();
        PublicParameters_Sw[4] = this.IBSscheme.getQ();
        return PublicParameters_Sw;
    }

    public static void main(String[] args) {
        IdentificationServer server = new IdentificationServer();
    }
}

package Client;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import java.io.FileOutputStream;
import java.io.IOException;

public class IBSsignature {
    static protected Pairing pairing = PairingFactory.getPairing("src/params/curves/a.properties");
    static protected Field Zp = pairing.getZr();
    static protected Field G0 = pairing.getG1();
    static protected Field G1 = pairing.getG2();
    protected Element w1;
    protected Element w2;

    public IBSsignature() {
    }
    public IBSsignature(Element w1, Element w2) {
        this.w1 = w1;
        this.w2 = w2;
    }
    public Element getW1() {
        return w1;
    }
    public Element getW2() {
        return w2;
    }
    public void setW1(Element w1) {
        this.w1 = w1;
    }
    public void setW2(Element w2) {
        this.w2 = w2;
    }

    public String[] toStringArray(){
        String[] signature = {Base64.encodeBytes(this.w1.toBytes()), Base64.encodeBytes(this.w2.toBytes())};
        return signature;
    }
    public static IBSsignature fromStringArray(String[] signatureString){
        IBSsignature signature = new IBSsignature();
        try{
        signature.setW1(Zp.newElementFromBytes(Base64.decode(signatureString[0])));
        signature.setW1(G0.newElementFromBytes(Base64.decode(signatureString[1])));
        } catch(IOException e) {e.printStackTrace();}
        return signature;
    }
}

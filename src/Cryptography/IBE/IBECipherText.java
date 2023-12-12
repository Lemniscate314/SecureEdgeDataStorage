package Cryptography.IBE;
import it.unisa.dia.gas.jpbc.Element;
public class IBECipherText {
    protected Element U;
    protected byte[] V;

    public IBECipherText() {
    }
    public IBECipherText(Element U, byte[] V) {
        this.U = U;
        this.V = V;
    }

    public Element getU() {
        return U;
    }

    public void setU(Element u) {
        U = u;
    }

    public byte[] getV() {
        return V;
    }

    public void setV(byte[] v) {
        V = v;
    }
}

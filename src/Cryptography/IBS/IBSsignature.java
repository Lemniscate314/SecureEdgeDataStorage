package Cryptography.IBS;
import it.unisa.dia.gas.jpbc.Element;
public class IBSsignature {
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
}

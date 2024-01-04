package Cryptography.Integrity;

import it.unisa.dia.gas.jpbc.Element;



public class EndUser {
    private Element Sw;
    private Element PK;

    public EndUser() {
        Sw = null;
        PK = null;
    }

    public void receiveSecretShare(Element Ss_w) {

        if (Sw == null) {
            Sw = Ss_w.duplicate().getImmutable();
        } else {
            Sw.add(Ss_w);
        }
    }

    public void calculatePublicKey(Element PKs) {
        PK = PKs.duplicate().getImmutable();
    }
}

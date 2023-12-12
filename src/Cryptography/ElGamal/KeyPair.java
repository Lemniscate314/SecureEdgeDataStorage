package Cryptography.ElGamal;

import it.unisa.dia.gas.jpbc.Element;

public record KeyPair(Element privateKey, Element publicKey) {
}

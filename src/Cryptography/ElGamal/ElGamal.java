package Cryptography.ElGamal;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;

public class ElGamal {

    public static void main(String[] args) {

        PairingParameters pairingParams = PairingFactory.getPairingParameters("Cryptography/params/curves/a.properties");
        Pairing pairing = PairingFactory.getPairing(pairingParams);
        Element generator = pairing.getZr().newRandomElement();

        KeyPair keyPair = generateKeyPair(pairing,generator);
        Element message = pairing.getZr().newElement();



        CipherText cipherText = encrypt(message, keyPair.publicKey(), pairing,generator);

        Element decryptedMessage = decrypt(cipherText, keyPair.privateKey());
        System.out.println("Message: " + Base64.getEncoder().encodeToString(message.toBytes()));
        System.out.println("C1: " + Base64.getEncoder().encodeToString(cipherText.c1().toBytes()));
        System.out.println("C2: " + Base64.getEncoder().encodeToString(cipherText.c2().toBytes()));
        System.out.println("Messagz décrypté: " + Base64.getEncoder().encodeToString(decryptedMessage.toBytes()));


        if (message.isEqual(decryptedMessage)) {
            System.out.println("Décryptage réussi!");
        } else {
            System.out.println("Décrytage incorrect.");
        }

    }

    public static KeyPair generateKeyPair(Pairing pairing,Element generator) {

        Element privateKey = pairing.getZr().newRandomElement();
        Element publicKey = generator.duplicate().mulZn(privateKey);

        return new KeyPair(privateKey, publicKey);
    }

    public static CipherText encrypt(Element message, Element publicKey, Pairing pairing, Element generator) {
        Element encryptionKey = pairing.getZr().newRandomElement();

        Element c1 = generator.duplicate().mulZn(encryptionKey);
        Element c2 =  message.duplicate().mulZn(publicKey.duplicate().mulZn(encryptionKey).duplicate());

        return new CipherText(c1, c2);
    }
    public static Element decrypt(CipherText cipherText, Element privateKey) {


        Element decryptionKey = cipherText.c1().duplicate().mulZn(privateKey);
        Element invertDecryptionKey = decryptionKey.duplicate().invert();
        Element message = invertDecryptionKey.mulZn(cipherText.c2().duplicate());

        return message;
    }



}

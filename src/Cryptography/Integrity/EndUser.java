package Cryptography.Integrity;
import Cryptography.IBS.EndUserIBSsignature;

import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;


public class EndUser {
    protected EndUserIBSsignature EndUserIBSsignature;
    //je suppose que la classe s'appelle EndUserSISsignature
    //protected EndUserIBSsignature EndUserSISsignature;

    public void getPublicParameters_Sw(Object[] PublicParameters_Sw){
        if (PublicParameters_Sw.length==6){
            this.EndUserIBSsignature.new_Sw_PK_P((Element) PublicParameters_Sw[5], (Element) PublicParameters_Sw[0], (Element) PublicParameters_Sw[1], (int) PublicParameters_Sw[2], (int )PublicParameters_Sw[3], (BigInteger) PublicParameters_Sw[4]);
        }
        else{System.out.println("Erreur du nombres de parametres publics");}
    }
}

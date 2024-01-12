package Cryptography.Integrity;
import Cryptography.IBS.EndUserIBSsignature;

import it.unisa.dia.gas.jpbc.Element;



public class EndUser {
    protected EndUserIBSsignature EndUserIBSsignature;
    //je suppose que la classe s'appelle EndUserSISsignature
    //protected EndUserIBSsignature EndUserSISsignature;

    public void getPublicParameters_Sw(Element[] PublicParameters_Sw){
        if (PublicParameters_Sw.length==6){
            this.EndUserIBSsignature.new_Sw_PK_P(PublicParameters_Sw[5], PublicParameters_Sw[0], PublicParameters_Sw[1]);
            //this.EndUserSISsignature.setN(PublicParameters_Sw[2]);
            //this.EndUserSISsignature.setM(PublicParameters_Sw[3]);
            //this.EndUserSISsignature.setQ(PublicParameters_Sw[4]);
        }
        else{System.out.println("Erreur du nombres de parametres publics");}
    }
}

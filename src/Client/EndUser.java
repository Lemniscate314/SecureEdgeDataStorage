package Client;

import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;


public class EndUser {
    protected EndUserIBS EndUserIBS;
    //je suppose que la classe s'appelle EndUserSISsignature
    //protected EndUserIBS EndUserSISsignature;

    public void getPublicParameters_Sw(Object[] PublicParameters_Sw){
        if (PublicParameters_Sw.length==6){
            this.EndUserIBS.new_Sw_PK_P((Element) PublicParameters_Sw[5], (Element) PublicParameters_Sw[0], (Element) PublicParameters_Sw[1], (int) PublicParameters_Sw[2], (int )PublicParameters_Sw[3], (BigInteger) PublicParameters_Sw[4]);
        }
        else{System.out.println("Erreur du nombres de parametres publics");}
    }
}

package Client;

import com.google.gson.Gson;
import java.math.BigInteger;

public class Block {
    protected int dataID;
    protected String ID;
    protected int i;
    protected String Di;
    protected Object[] paramA;
    protected BigInteger[][] V;
    protected IBSsignature signature;

    public static Block fromJson(String json) {
        return new Gson().fromJson(json, Block.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}

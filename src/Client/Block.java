package Client;

import java.math.BigInteger;

// Classe représentant un bloc et utilisé pour manipuler les JSON
public class Block {
    public int dataID;
    public String topic;
    public String IDw;
    public int i; //place du block dans la donnée
    public String dataBlock;
    public BigInteger[] paramA;
    public BigInteger[][] V;
    public String[] signature;
    public Block(Blocks blocks, String dataBlock) {
        this.dataID = blocks.dataID;
        this.topic = blocks.topic;
        this.IDw = blocks.IDw;
        this.dataBlock = dataBlock;
        this.i = blocks.dataBlocksMap.get(dataBlock);
        this.paramA = blocks.paramA;
        this.V = blocks.V;
        this.signature = blocks.signature.toStringArray();
    }
}

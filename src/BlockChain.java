import java.util.ArrayList;

public class BlockChain {

    static ArrayList<Block> blockchain = new ArrayList<>();
    static int difficulty = 5;

    public static void main(String[] args) {

        // Genesis 블록 : 블록체인에서 최초의 블록
        blockchain.add(new Block("Genesis Block", "0"));
        blockchain.get(0).mineBlock(difficulty);
        blockchain.add(new Block("Second Block", blockchain.get(blockchain.size() -1).hash));
        blockchain.get(1).mineBlock(difficulty);
        blockchain.add(new Block("Third Block", blockchain.get(blockchain.size() -1).hash));
        blockchain.get(2).mineBlock(difficulty);

        System.out.println("BlockChain Valid: " + isChainValid());

    }

    public static Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0','0');

        for(int i=1;i<blockchain.size();i++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);

            // hash 값이 유효한지 확인
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("Current Hashes not equal");
                return false;
            }

            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("Previous Hashes not equal");
                return false;
            }

            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)){
                System.out.println("채굴 되지 않음");
                return false;
            }

        }
        return true;
    }


}

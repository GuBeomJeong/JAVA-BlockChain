package blockchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {

    static ArrayList<Block> blockchain = new ArrayList<>();
    // 여러 개의 트랜잭션이 모이게 되어 블록체인의 길이가 점점 길어진다.
    // 그럴 경우 UTXO를 찾는데 걸리는 시간 또한 길어진다.
    // 이 문제를 해결하기 위해 UTXO의 collection을 만들어준다.
    // 비트쾬은 UTXO pool이 따로 있다.
    static HashMap<String,TransactionOutput> UTXOs = new HashMap<>();
    static int difficulty = 5;
    static Wallet walletA;
    static Wallet walletB;

    public static void main(String[] args) {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();

        System.out.println("Private and public keys: ");
        System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
        System.out.println(StringUtil.getStringFromKey(walletA.publicKey));

        // walletA에서 walletB로 송금하는 트랙잭션
        Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5,null);
        // 서명
        transaction.generateSignature(walletA.privateKey);
        // 검증
        System.out.println("Is signature verified");
        System.out.println(transaction.verifySignature());

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

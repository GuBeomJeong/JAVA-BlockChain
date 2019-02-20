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

    static int difficulty = 3;
    static float minimumTransaction = 0.1f;
    static Wallet walletA;
    static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // 100 coin을 지갑 A에게 보내는 genesis 트랜잭션 생성
        // coinbase 트랜잭션을 생성하고 그 트랜잭션을 genesis 블록에 넣는 과정
        genesisTransaction = new Transaction(coinbase.publicKey,walletA.publicKey,100f,null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient,genesisTransaction.value,genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id,genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis block...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is attempting to send funds (40) to WalletB..");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey,40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA is attempting to send more funds (1000) than it has.. ");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey,1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();
    }

    public static Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0','0');

        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id,genesisTransaction.outputs.get(0));

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

            TransactionOutput tempOutput;
            for(int t=0;t<currentBlock.transactions.size(); t++){
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()){
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }

                // 현재 수수료 개념이 없기때문에 input과 output의 총량은 서로 같아야한다.
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()){
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                // 블록의 트랜잭션들의 input값이 올바른지 확인
                for(TransactionInput input : currentTransaction.inputs){
                    tempOutput = tempUTXOs.get(input.transactionOutputId);
                    // input을 올바르게 참조하는가?
                    if(tempOutput == null){
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    // 참조한 값과 value가 동일한가?
                    if(input.UTXO.value != tempOutput.value){
                        System.out.println("#Referenced input on Transaction(" + t + ") is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                // 수취인이 정확한가?
                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                // 거스듬론의 수취인이 정확한가?
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }

        }

        System.out.println("Blockchain is valid");
        return true;
    }


    public static void addBlock(Block newBlock){
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}

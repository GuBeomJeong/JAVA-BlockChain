package blockchain;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash; // 블록을 식별할 수 있는 해시 값
    public String previousHash; // 전 블록의 해시 값
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    private long timeStamp;
    private int nonce; // 블록을 채굴하기 위해 사용되는 값


//    public Block(String data, String previousHash) {
//        this.data = data;
//        this.previousHash = previousHash;
//        this.timeStamp = new Date().getTime();
//        this.hash = calculateHash();
//        this.nonce = 0;
//
//    }

    public Block(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
        this.nonce = 0;
    }

    public String calculateHash() {
        // 블록의 고유 식별자인 hash를 만들기 위해 전 블록의 해
        // 해시값과 해당 블록의 data, timestamp가 들어간다.

        return StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
    }

    // 모든 노드가 올바른 블록체인을 공유하고 있다는 것을 입증하기 위해 합의 알고리즘을 사용
    // 비트코인에서는 PoW(Proof of Work)라는 합의 알고리즘을 사용
    // PoW는 컴퓨팅 파워를 이용해 블록의 hash값을 추적하여 블록체인 네트워크에 새로운 블록을 추가하는 방식
    // 이 과정을 채굴(Mining)이라고 부른다.
    // hash값 추적은 hash값 첫 부분의 0 개수를 추적하는 과정
    // 다른 변수값을 블록에 넣으면서 조건을 만족할 때 까지 hashing하는 것
    // 0의 개수가 많아질수록 추적이 어려워지며 '난이도(diffuculty)'가 높다
    public void mineBlock(int difficulty){
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        // difficulty 만큼의 0을 만듬
        String target = new String(new char[difficulty]).replace('\0','0');

        // 0의 개수가 같을 때 까지
        while(!hash.substring(0,difficulty).equals(target)){
            nonce++;
            hash = calculateHash();
        }

        System.out.println("블록 채굴 : " + hash);

    }

    // 블록에 트랜잭션을 추가
    public boolean addTransaction(Transaction transaction){
        // 트랜잭션의 유효성 확인
        if(transaction == null) return false;
        if((!previousHash.equals("0"))){
            if((!transaction.processTransaction())){
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }



}

package blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 블록체인(비트코인)에서 의미하는 지갑은 주소를 저장하고 트랜잭션을 생성할 수 있는 소프트웨어
// 지갑이라고는 하지만 실제로 코인을 넣고 빼는 것이 아니다.
// 트랜잭션 결과로 생긴 output을 추적하여 쓸 수 있는 코인이 있는지 확인한다.
public class Wallet {

    // 지갑의 소유주를 구분하기 위해 필요하다.
    // 존재 증명과 식별을 위해 존재 ( 디지털 서명 )
    public PrivateKey privateKey;
    public PublicKey publicKey;

    // 비트코인에서는 특수한 output에 대해 UTXO(Unspent Transaction Outputs)이라고 하며 사용하지 않은 거래의 출력이라고 한다.
    // 새로운 트랜잭션을 생성하는 것은 UTXO를 추적하여 이전 거래의 output을 새로운 트랜잭션의 input으로 사용하는 것이다.
    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();


    public Wallet(){
        generateKeyPair();
    }

    // 키 쌍을 만드는데 타원 곡선 암호 알고리즘을 사용
    public void generateKeyPair(){
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec,random); // 256 바이트
            KeyPair keyPair = keyGen.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    // 잔금을 반환하고 UTXOs(HashMap)에 그 list를 저장한다.
    public float getBalance(){
        float total = 0;
        for(Map.Entry<String,TransactionOutput> item : BlockChain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { // 해당 UTXO가 내 것인지
                UTXOs.put(UTXO.id,UTXO); // 맞으면 내 지갑의 UTXOs에 추가
                total += UTXO.value;
            }
        }

        return total;
    }

    // 트랜잭션을 발생시키고 반환한다.
    public Transaction sendFunds(PublicKey _recipient, float value){
        if(getBalance() < value){ // 송금할 돈이 없으면
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        // 트랜잭션 안의 input list
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;

        // UTXO를 추적하여 사용할 수 있는 코인 확인 후 저장
        for(Map.Entry<String,TransactionOutput> item : UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        // 트랜잭션 생성
        Transaction newTransaction = new Transaction(publicKey,_recipient,value,inputs);

        // 서명
        newTransaction.generateSignature(privateKey);

        // 사용한 UTXO는 삭제
        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }

}

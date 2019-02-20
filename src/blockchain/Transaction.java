package blockchain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

    public String transactionId; // 트랙잭션 hash값
    public PublicKey sender; // 송신자의 공개키
    public PublicKey reciepient; // 수신자의 공개키
    public float value; // 전달할 금액
    public byte[] signature; // 암호화된 서명


    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // 생성된 트랜잭션의 수

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // 트랜잭션 id를 구하기 위해 Hashing하는 메서드
    private String calculateHash(){
        sequence++; // 같은 hash값을 피하기 위해

        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender)
                + StringUtil.getStringFromKey(reciepient)
                + Float.toString(value) + sequence
        );
    }

    // 서명에 필요한 데이터는 송신자의 공개키, 수신자의 공개키, 송금량이다.
    // 실제 비트코인에서의 sinature 메커니즘은 이와 조금 다르다.
    public void generateSignature(PrivateKey privateKey){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey,data);
    }

    // Sinatures는 채굴자들에 의헤서 검증된다.
    public boolean verifySignature(){
        String data = StringUtil.getStringFromKey(sender)+ StringUtil.getStringFromKey(reciepient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender,data,signature);
    }

    // 트랜잭션이 유효하면 true 반납
    // 트랜잭션 유효성 검사 -> 거래 할 UTXO가 충분한 지 확인
    // -> 거래금과 잔금 반환 -> 쓰인 UTXO 삭제, 새로운 output 생성
    public boolean processTransaction() {

        if (verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        for (TransactionInput i : inputs) {
            i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
        }

        if (getInputsValue() < BlockChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // 거스름돈
        float leftOver = getInputsValue() - value;
        transactionId = calculateHash();
        // 수취인에게 전달
        outputs.add(new TransactionOutput(this.reciepient, value, transactionId));
        // 잔돈은 송금인에게 반납
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        // 새로 생성된 output을 UTXO collection에 추가한다.
        for (TransactionOutput o : outputs) {
            BlockChain.UTXOs.put(o.id, o);
        }

        // 쓰인 UTXO는 collection에서 삭제한다.
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            BlockChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // 현재 트랜잭션의 input 총량
    public float getInputsValue(){
        float total = 0;
        for(TransactionInput i : inputs){
            if(i.UTXO == null) continue;
            total += i.UTXO.value;
        }

        return total;
    }

    // 현재 트랜잭션의 output 총량
    public float getOutputsValue(){
        float total = 0;
        for(TransactionOutput o : outputs){
            total += o.value;
        }
        return total;
    }

}

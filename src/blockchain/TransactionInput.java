package blockchain;

public class TransactionInput {
    // TransactionOutputs의 transactionId를 참조
    public String transactionOutputId;
    // 참조한 output의 UTXO를 갖는다.
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
}

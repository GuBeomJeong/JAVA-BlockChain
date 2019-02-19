package blockchain;

import java.security.PublicKey;

// 트랜잭션의 output은 전송 된 최종 금액이 표시
// 이것은 새로운 거래를 생성할 떄 input이 참조하게 되어 보낼 수 있는
// 코인이 있다는 것을 증명한다.
public class TransactionOutput {
    public String id;
    public PublicKey reciepient; // 수취인의 공개키
    public float value; // 수취인에게 돌아갈 코인의 양
    public String parentTransactionId; // 해당 output이 포함된 transaction의 id

    public TransactionOutput(PublicKey receipient, float value, String parentTransactionId){
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient) + Float.toString(value)+parentTransactionId);

    }

    // 송금이 잘 되었는지 확인
    public boolean isMine(PublicKey publicKey){
        return (publicKey == reciepient);
    }
}

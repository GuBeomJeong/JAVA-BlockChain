package blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

// 블록체인(비트코인)에서 의미하는 지갑은 주소를 저장하고 트랜잭션을 생성할 수 있는 소프트웨어
public class Wallet {

    // 지갑의 소유주를 구분하기 위해 필요하다.
    // 존재 증명과 식별을 위해 존재 ( 디지털 서명 )
    public PrivateKey privateKey;
    public PublicKey publicKey;

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

}

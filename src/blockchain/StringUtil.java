package blockchain;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {

    public static String applySha256(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for(int i = 0 ; i < hash.length;i++){
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1){
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }catch(Exception e){
            throw new RuntimeException(e);

        }
    }

    // ECDSA(타원 곡선 암호화 알고리즘)을 적용하여 Signature를 획득
    // 송신자가 자신의 개인키를 이용해 데이터를 암호화하는 메서드
    public static byte[] applyECDSASig(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];
        try{
            dsa = Signature.getInstance("ECDSA","BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return output;

    }

    // Signature가 유효한지 식별
    // 암호화된 데이터를 송신자의 공개키로 디코딩하여 데이터의 무결성을 식별
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature){
        try{
            Signature ecdsaVerify = Signature.getInstance("ECDSA","BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    // 타원 곡선 알고리즘으로 생성된 key(Byte)를 String으로 반환
    public static String getStringFromKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // (지갑이)트랜잭션에 필요한 데이터들을 머클트리로 구현하기 위함
    // Merkle 트리를 만드는게 ㅏ니라 Merkle 트리의 root만을 반환
    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();

        // 트랜잭션의 id값만 저장 ( hash값)
        for(Transaction transaction : transactions){
            previousTreeLayer.add(transaction.transactionId);
        }

        ArrayList<String> treeLayer = previousTreeLayer;

        while(count > 1){
            treeLayer = new ArrayList<String>();
            for(int i=1;i < previousTreeLayer.size(); i=i+2){
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));

            }

            count = treeLayer.size();
            previousTreeLayer = treeLayer;

        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;

    }

}

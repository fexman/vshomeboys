package solution.communication;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AESOperator extends ChannelOperator {

	Cipher aesEncrypt;
	Cipher aesDecrypt;
	
	public AESOperator(SecretKey key, byte[] iv) throws IOException {
		
		try {
			aesEncrypt = Cipher.getInstance("AES/CTR/NoPadding");
			aesDecrypt = Cipher.getInstance("AES/CTR/NoPadding"); 
		} catch (NoSuchAlgorithmException e) {
			//Wont happen
		} catch (NoSuchPaddingException e) {
			//auch
		} 
		
		try {
			aesEncrypt.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
			aesDecrypt.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv));
		} catch (InvalidKeyException e) {
			throw new IOException("AES error: InvalidKeyException");
		} catch (InvalidAlgorithmParameterException e) {
			throw new IOException("AES error: InvalidAlgorithmParameterException");
		}

		
	}
	
	@Override
	public Serializable receiveOperation(Serializable s) throws IOException {
		try {
			return solution.util.CryptoUtil.deserialize(aesDecrypt.doFinal((byte[]) s));
		} catch (IllegalBlockSizeException e) {
			throw new IOException("Decrypt error: Illegal block size!");
		} catch (BadPaddingException e) {
			throw new IOException("Decrypt error: Bad padding!");
		}
	}

	@Override
	public Serializable transmitOperation(Serializable s) throws IOException {
		try {
			return aesEncrypt.doFinal(solution.util.CryptoUtil.serialize(s));
		} catch (IllegalBlockSizeException e) {
			throw new IOException("Encrypt error: Illegal block size!");
		} catch (BadPaddingException e) {
			throw new IOException("Encrypt error: Bad padding!");
		}
	}

	@Override
	public String OPERATOR_IDENT() {
		return "AES";
	}

}

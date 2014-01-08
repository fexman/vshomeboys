package solution.communication;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;


public class BiDirectionalRsaOperator extends ChannelOperator {

	private Cipher publicKeyEncrypt;
	private Cipher privateKeyDecrypt;
	
	
	public BiDirectionalRsaOperator(String pathToPublicKey, String pathToPrivateKey, final String password) throws IOException {
		
		//Load public key
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();

		//Load private key with provided password
		try {
			in = new PEMReader(new FileReader(pathToPrivateKey),  new PasswordFinder() {
		
				@Override
				public char[] getPassword() {
					return password.toCharArray();
				}
				
			});
		} catch (IOException e) {
			throw new IOException("RSA failure: Unkown user! (key is missing)");
		}

		PrivateKey privateKey = null;
		try {
			KeyPair keyPair = (KeyPair) in.readObject(); 
			privateKey = keyPair.getPrivate();
		} catch (IOException e) {
			throw new IOException("RSA failure: Wrong password!");
		} finally {
			in.close();
		}
		
		try {
			//Init ciphers
			publicKeyEncrypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding"); 
			publicKeyEncrypt.init(Cipher.ENCRYPT_MODE, publicKey);
			privateKeyDecrypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding"); 
			privateKeyDecrypt.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (InvalidKeyException e) {
			throw new IOException("RSA failure: Path was okay, but key was invalid.");
		} catch (NoSuchAlgorithmException e1) {
			throw new IOException("RSA failure: Unkown algorithm.");
		} catch (NoSuchPaddingException e1) {
			throw new IOException("RSA failure: Unkown padding.");
		}
	}
	
	@Override
	public Serializable transmitOperation(Serializable s) throws IOException {
			try {
				return publicKeyEncrypt.doFinal(solution.util.CryptoUtil.serialize(s));
			} catch (IllegalBlockSizeException e) {
				throw new IOException("Encrypt error: Illegal block size!");
			} catch (BadPaddingException e) {
				throw new IOException("Encrypt error: Bad padding!");
			}
	}

	@Override
	public Serializable receiveOperation(Serializable s) throws IOException {
			try {
				return solution.util.CryptoUtil.deserialize(privateKeyDecrypt.doFinal((byte[]) s));
			} catch (IllegalBlockSizeException e) {
				throw new IOException("Decrypt error: Illegal block size!");
			} catch (BadPaddingException e) {
				throw new IOException("Decrypt error: Bad padding!");
			}
	}
	
	@Override
	public String OPERATOR_IDENT() {
		return "2DRSA";
	}


}

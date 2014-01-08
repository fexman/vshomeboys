package solution.communication;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class OneDirectionalRsaOperator extends ChannelOperator {

	private Cipher privateKeyDecrypt;

	public OneDirectionalRsaOperator(String pathToPrivateKey,
			final String password) throws IOException {

		// Load private key with provided password
		PEMReader in = null;
		try {
			in = new PEMReader(new FileReader(pathToPrivateKey),
					new PasswordFinder() {

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
			// Init ciphers
			privateKeyDecrypt = Cipher
					.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			privateKeyDecrypt.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (InvalidKeyException e) {
			throw new IOException(
					"RSA failure: Path was okay, but key was invalid.");
		} catch (NoSuchAlgorithmException e1) {
			throw new IOException("RSA failure: Unkown algorithm.");
		} catch (NoSuchPaddingException e1) {
			throw new IOException("RSA failure: Unkown padding.");
		}
	}

	@Override
	public Serializable transmitOperation(Serializable s) throws IOException {
		throw new IOException("Only receiving possible with OneDirectionalRSA!");
	}

	@Override
	public Serializable receiveOperation(Serializable s) throws IOException {
		try {
			return solution.util.CryptoUtil.deserialize(privateKeyDecrypt
					.doFinal((byte[]) s));
		} catch (IllegalBlockSizeException e) {
			throw new IOException("Decrypt error: Illegal block size!");
		} catch (BadPaddingException e) {
			throw new IOException("Decrypt error: Bad padding!");
		}
	}

	@Override
	public String OPERATOR_IDENT() {
		return "1DRSA";
	}
}
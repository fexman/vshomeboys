package solution.communication;

import java.io.IOException;
import java.io.Serializable;


public class Base64Operator extends ChannelOperator {

	@Override
	public Serializable transmitOperation(Serializable s) throws IOException {
			return solution.util.CryptoUtil.base64Encode(s);
	}

	@Override
	public Serializable receiveOperation(Serializable s) throws IOException {
		return solution.util.CryptoUtil.base64Decode((String) s);
	}
	
	@Override
	public String OPERATOR_IDENT() {
		return "BASE64";
	}

	

}

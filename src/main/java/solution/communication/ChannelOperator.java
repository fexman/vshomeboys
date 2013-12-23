package solution.communication;

import java.io.IOException;
import java.io.Serializable;

public abstract class ChannelOperator {
	
	public abstract Serializable receiveOperation(Serializable s) throws IOException;
	
	public abstract Serializable transmitOperation(Serializable s) throws IOException;
	
	public abstract String OPERATOR_IDENT();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((OPERATOR_IDENT() == null) ? 0 : OPERATOR_IDENT().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChannelOperator other = (ChannelOperator) obj;
		if (OPERATOR_IDENT() == null) {
			if (other.OPERATOR_IDENT() != null)
				return false;
		} else if (!OPERATOR_IDENT().equals(other.OPERATOR_IDENT()))
			return false;
		return true;
	}
	
	
	
}

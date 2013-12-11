package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;

public interface RequestProcessor {
	
	/**
	 * Triggers specific behaviour for specific caller/request combiniation
	 * @param caller Caller-Object to whom the given "request" is passed for further processing
	 * @param request a request object
	 * @return the caller generated response to the given request/caller bundle. null if passed request-object is not known or caller-object is not specified to process given reqest-object (caller/request mismatch)
	 * @throws IOException
	 */
	Response process(Object caller, Request request) throws IOException;

}

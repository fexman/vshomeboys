package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import proxy.IProxy;

public class BuyRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((IProxy)caller).buy((BuyRequest)request);
	}
	
}

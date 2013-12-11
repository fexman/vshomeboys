package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.LoginRequest;
import proxy.IProxy;

public class LoginRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((IProxy)caller).login((LoginRequest)request);
	}

}

package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import proxy.IProxy;

public class LogoutRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((IProxy)caller).logout();
	}

}

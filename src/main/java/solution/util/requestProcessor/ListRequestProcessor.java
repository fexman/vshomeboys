package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import server.IFileServer;
import solution.proxy.Proxy;

public class ListRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		if (caller instanceof Proxy) {
			return ((Proxy)caller).list();
		}
		return ((IFileServer)caller).list();
	}

}

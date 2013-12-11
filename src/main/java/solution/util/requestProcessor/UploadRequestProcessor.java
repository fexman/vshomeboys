package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.UploadRequest;
import proxy.IProxy;
import server.IFileServer;
import solution.proxy.Proxy;

public class UploadRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		if (caller instanceof Proxy) {
			return ((IProxy)caller).upload((UploadRequest)request);
		}
		return ((IFileServer)caller).upload((UploadRequest)request);
	}

}

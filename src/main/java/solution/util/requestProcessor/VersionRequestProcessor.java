package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.VersionRequest;
import server.IFileServer;

public class VersionRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((IFileServer)caller).version((VersionRequest)request);
	}

}

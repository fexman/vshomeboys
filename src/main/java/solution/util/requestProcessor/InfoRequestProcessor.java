package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.InfoRequest;
import server.IFileServer;

public class InfoRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((IFileServer)caller).info((InfoRequest)request);
	}

}

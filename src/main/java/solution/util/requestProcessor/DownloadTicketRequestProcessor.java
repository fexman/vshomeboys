package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.DownloadTicketRequest;
import proxy.IProxy;

public class DownloadTicketRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((IProxy)caller).download((DownloadTicketRequest)request);
	}

}

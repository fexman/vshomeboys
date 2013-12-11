package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import solution.fileserver.FileServer;

public class DownloadFileRequestProcessor implements RequestProcessor {

	@Override
	public Response process(Object caller, Request request) throws IOException {
		return ((FileServer)caller).download((DownloadFileRequest)request);
	}

}

package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.DownloadTicketRequest;

public class DownloadTicketRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {

        if (r instanceof DownloadTicketRequest) {
            
            return proxy.download((DownloadTicketRequest) r);

        } else {

            return null;
        }
    }
}
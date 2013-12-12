package solution.util.requestProcessor;

import java.io.IOException;

import server.IFileServer;
import message.Request;
import message.Response;
import message.request.InfoRequest;

public class InfoRequestHandler implements Handler<IFileServer> {

    @Override
    public Response handle(Request r, IFileServer server) throws IOException {

        if (r instanceof InfoRequest) {

            return server.info((InfoRequest) r);

        } else {

            return null;
        }
    }
}
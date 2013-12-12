package solution.util.requestProcessor;

import java.io.IOException;

import server.IFileServer;
import message.Request;
import message.Response;
import message.request.ListRequest;

public class ServerListRequestHandler implements Handler<IFileServer> {

    @Override
    public Response handle(Request r, IFileServer server) throws IOException {

        if (r instanceof ListRequest) {

            return server.list();

        } else {

            return null;
        }
    }
}
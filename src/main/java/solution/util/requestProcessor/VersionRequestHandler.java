package solution.util.requestProcessor;

import java.io.IOException;

import server.IFileServer;
import message.Request;
import message.Response;
import message.request.VersionRequest;

public class VersionRequestHandler implements Handler<IFileServer> {

    @Override
    public Response handle(Request r, IFileServer server) throws IOException {
        
        if (r instanceof VersionRequest) {

            return server.version((VersionRequest) r);

        } else {

            return null;
        }
    }
}
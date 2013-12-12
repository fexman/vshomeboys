package solution.util.requestProcessor;

import java.io.IOException;

import server.IFileServer;
import message.Request;
import message.Response;
import message.request.UploadRequest;

public class ServerUploadRequestHandler implements Handler<IFileServer> {

    @Override
    public Response handle(Request r, IFileServer server) throws IOException {
        
        if (r instanceof UploadRequest) {

            return server.upload((UploadRequest) r);

        } else {

            return null;
        }
    }
}
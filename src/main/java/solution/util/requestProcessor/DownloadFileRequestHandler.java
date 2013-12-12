package solution.util.requestProcessor;

import java.io.IOException;

import server.IFileServer;
import message.Request;
import message.Response;
import message.request.DownloadFileRequest;

public class DownloadFileRequestHandler implements Handler<IFileServer> {

    @Override
    public Response handle(Request r, IFileServer server) throws IOException {

        if (r instanceof DownloadFileRequest) {
            
            return server.download((DownloadFileRequest) r);

        } else {

            return null;
        }
    }
}
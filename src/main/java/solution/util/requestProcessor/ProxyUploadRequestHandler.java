package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.UploadRequest;

public class ProxyUploadRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {
        
        if (r instanceof UploadRequest) {

            return proxy.upload((UploadRequest) r);

        } else {

            return null;
        }
    }
}
package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.CreditsRequest;

public class CreditsRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {

        if (r instanceof CreditsRequest) {

            return proxy.credits();

        } else {

            return null;
        }
    }
}
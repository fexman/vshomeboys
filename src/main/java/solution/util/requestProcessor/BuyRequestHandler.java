package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.BuyRequest;

public class BuyRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {

        if (r instanceof BuyRequest) {
            
            return proxy.buy((BuyRequest) r);

        } else {

            return null;
        }
    }
}
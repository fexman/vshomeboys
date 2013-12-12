package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.ListRequest;

public class ProxyListRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {

        if (r instanceof ListRequest) {

            return proxy.list();

        } else {

            return null;
        }
    }
}
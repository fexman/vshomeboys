package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.LogoutRequest;

public class LogoutRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {
        
        if (r instanceof LogoutRequest) {

            return proxy.logout();

        } else {

            return null;
        }
    }
}
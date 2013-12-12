package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import message.Request;
import message.Response;
import message.request.LoginRequest;

public class LoginRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {

        if (r instanceof LoginRequest) {

            return proxy.login((LoginRequest) r);

        } else {

            return null;
        }
    }
}
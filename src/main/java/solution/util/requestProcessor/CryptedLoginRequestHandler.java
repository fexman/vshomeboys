package solution.util.requestProcessor;

import java.io.IOException;

import proxy.IProxy;
import solution.message.request.CryptedLoginRequest;
import message.Request;
import message.Response;

public class CryptedLoginRequestHandler implements Handler<IProxy> {

    @Override
    public Response handle(Request r, IProxy proxy) throws IOException {

        if (r instanceof CryptedLoginRequest) {

            return proxy.login((CryptedLoginRequest) r);

        } else {

            return null;
        }
    }
}
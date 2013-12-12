package solution.util.requestProcessor;

import java.io.IOException;

import message.Request;
import message.Response;

/**
 * Maps Requests to method calls.
 */
public interface Handler<T> {

	/**
	 * 
	 * @param r	!= null: Request, that is handled
	 * @param t	!= null: depending on r, different methods, that return Requests are called
	 * @return Response / null, defined by the called method
	 * @throws IOException	behaviour defined by the called method
	 */
    public Response handle(Request r, T t) throws IOException;
}
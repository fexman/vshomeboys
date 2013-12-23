package solution.util.requestProcessor;

import java.io.IOException;
import java.util.HashMap;

import proxy.IProxy;
import server.IFileServer;
import solution.AbstractServer;
import solution.message.request.CryptedLoginRequest;
import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;

/**
 * Handles requests and maps them to the respective methods from the caller. The
 * caller is either a proxy or a fileserver.
 */

public class RequestHandlerUtil {

	private static HashMap<Class<? extends Request>, Handler<IProxy>> proxyHandlers;
	private static HashMap<Class<? extends Request>, Handler<IFileServer>> serverHandlers;

	static {

		proxyHandlers = new HashMap<Class<? extends Request>, Handler<IProxy>>();
		serverHandlers = new HashMap<Class<? extends Request>, Handler<IFileServer>>();

		proxyHandlers.put(BuyRequest.class, new BuyRequestHandler());
		proxyHandlers.put(CreditsRequest.class, new CreditsRequestHandler());
		proxyHandlers.put(DownloadTicketRequest.class, new DownloadTicketRequestHandler());
		proxyHandlers.put(CryptedLoginRequest.class, new CryptedLoginRequestHandler());
		proxyHandlers.put(ListRequest.class, new ProxyListRequestHandler());
		proxyHandlers.put(LogoutRequest.class, new LogoutRequestHandler());
		proxyHandlers.put(LoginRequest.class, new LoginRequestHandler());
		proxyHandlers.put(UploadRequest.class, new ProxyUploadRequestHandler());

		serverHandlers.put(UploadRequest.class, new ServerUploadRequestHandler());
		serverHandlers.put(InfoRequest.class, new InfoRequestHandler());
		serverHandlers.put(VersionRequest.class, new VersionRequestHandler());
		serverHandlers.put(DownloadFileRequest.class, new DownloadFileRequestHandler());
		serverHandlers.put(ListRequest.class, new ServerListRequestHandler());
	}

	/**
	 * Handles requests coming from a proxy
	 * 
	 * @param r
	 *            != null: request, that is handled
	 * @param server
	 *            != null: any method defined by IProxy or IFileServer can be
	 *            called from the RequestHandler
	 * @return Response / null, defined by called method from Handler.
	 * @throws IOException
	 *             determined by behaviour of called methods
	 */
	public static Response handle(Request request, AbstractServer server) throws IOException {

		Response r = null;

		if (server instanceof IProxy) {

			r = proxyHandlers.get(request.getClass()).handle(request, (IProxy) server);

		} else {

			if (server instanceof IFileServer) {
				
				r = serverHandlers.get(request.getClass()).handle(request, (IFileServer) server);
			}
		}

		return r;
	}
}
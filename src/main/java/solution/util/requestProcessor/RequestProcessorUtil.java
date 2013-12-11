package solution.util.requestProcessor;

import java.io.IOException;
import java.util.HashMap;

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
 * Links Objects (from objectoutputstreams) with dynamic (request-)types and 
 * passes them to correctly to the  caller for further processing.
 * @author Felix
 *
 */

public class RequestProcessorUtil {
	
	private static final HashMap<Class<?>,RequestProcessor> processors;
	static {
		processors = new HashMap<Class<?>,RequestProcessor>();
		processors.put(BuyRequest.class,new BuyRequestProcessor());
		processors.put(CreditsRequest.class,new CreditsRequestProcessor());
		processors.put(DownloadTicketRequest.class,new DownloadTicketRequestProcessor());
		processors.put(ListRequest.class, new ListRequestProcessor());
		processors.put(LogoutRequest.class, new LogoutRequestProcessor());
		processors.put(LoginRequest.class, new LoginRequestProcessor());
		processors.put(UploadRequest.class, new UploadRequestProcessor());
		processors.put(InfoRequest.class, new InfoRequestProcessor());
		processors.put(VersionRequest.class, new VersionRequestProcessor());
		processors.put(DownloadFileRequest.class, new DownloadFileRequestProcessor());
	}
	
	/**
	 * Links Objects (from objectoutputstreams) with dynamic (request-)types and 
	 * passes them to correctly to the  caller for further processing.
	 * @param caller Caller-Object to whom the given "request" is passed for further processing
	 * @param request (hopefully) a request object
	 * @return the caller generated response to the given request/caller bundle. null if passed request-object is not known or caller-object is not specified to process given request-object (caller/request mismatch)
	 * @throws IOException
	 */
	public static Response process(Object caller, Object request) throws IOException {
		
		RequestProcessor rp = processors.get(request.getClass());
		if (rp == null) {
			return null;
		}
		Response r;
		try {
			r = rp.process(caller, (Request)request);
		} catch (ClassCastException e) { //In case request for proxy is sent to fileserver or vice versa, else casts should be POC
			return null;
		}
		return r;
	}
	

}

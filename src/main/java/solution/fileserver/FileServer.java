package solution.fileserver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.DownloadFileResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadTicket;
import server.IFileServer;
import solution.AbstractTcpServer;
import solution.util.FileUtils;
import util.ChecksumUtils;

public class FileServer extends AbstractTcpServer implements IFileServer  {
	
	private final String path;
	
	public FileServer(Socket socket, Set<AbstractTcpServer> connections, String path)
			throws IOException {
		super(socket, connections);
		this.path = path;
		stopListening();
	}

	@Override
	public Response list() throws IOException {
		
		println("Got list request.");
		
		File f = new File(path);
		File[] files = f.listFiles();
		
		HashSet<String> fileNames = new HashSet<String>();
		
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					fileNames.add(files[i].getName());
				}
			}
		}
		
		return new ListResponse(fileNames);
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		
		DownloadTicket t = request.getTicket();
		String filepath = path + "/" + t.getFilename();
		
		println("Got download request for file: " + filepath); 
		
		try {
			
			if (!ChecksumUtils.verifyChecksum(t.getUsername(), FileUtils.getFile(filepath), 0, t.getChecksum())) {
				return new MessageResponse("Invalid Ticket: Invalid checksum.");
			}

			byte[] content = FileUtils.getBytesFromFile(filepath);
			return new DownloadFileResponse(t, content);
		} catch (IOException e) {
			return new MessageResponse("Invalid Ticket: File not found.");
		}


	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		
		println("Got info request for: " + request.getFilename());
		File f = new File(path + "/" + request.getFilename());

		if (f.exists()) {
			return new InfoResponse(request.getFilename(),f.length());
		}
		return new InfoResponse(request.getFilename(),-1);
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		println("Got version request for: " + request.getFilename());
		return new VersionResponse(request.getFilename(),0);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException  {
	println("Got upload request for file: " + request.getFilename());
		try {
		FileUtils.writeBytesToFile(path+ "/" + request.getFilename(),request.getContent());
		} catch (IOException e) {
			return new MessageResponse("Error: Fileserver could not write file.");
		}
		return new MessageResponse("Upload successful.");
	}

	@Override
	public void customShutDown() {
		//Nothing to do here!
		
	}
	
	

}

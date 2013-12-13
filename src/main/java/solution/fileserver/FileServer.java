package solution.fileserver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

public class FileServer extends AbstractTcpServer implements IFileServer {

	private final String path;
	ConcurrentHashMap<String, Integer> files;

	public FileServer(Socket socket, Set<AbstractTcpServer> connections, String path, ConcurrentHashMap<String, Integer> files) throws IOException {
		super(socket, connections);
		this.path = path;
		this.files = files;
		stopListening();
	}

	@Override
	public Response list() throws IOException {

		println("Got list request.");

		return new ListResponse(files.keySet());
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
			return new InfoResponse(request.getFilename(), f.length());
		}
		return new InfoResponse(request.getFilename(), -1);
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		println("Got version request for: " + request.getFilename() + "\n" +
				"Version: " + files.get(request.getFilename()));
		return new VersionResponse(request.getFilename(), files.get(request.getFilename()));
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		println("Got upload request for file: " + request.getFilename());
		try {
			FileUtils.writeBytesToFile(path + "/" + request.getFilename(), request.getContent());
			versionFile(request.getFilename());
		} catch (IOException e) {
			return new MessageResponse("Error: Fileserver could not write file.");
		}
		return new MessageResponse("Upload successful.");
	}

	@Override
	public void customShutDown() {
		// Nothing to do here!

	}

	/**
	 * Increases version number of existing file by 1 or initializes new file with version number 0.
	 * @param filename
	 */
	private void versionFile(String filename) {
		
		if (files.containsKey(filename)) {
			
			files.put(filename, files.get(filename) + 1);
			
		} else {
			
			files.put(filename, 0);
		}
	}
}
package solution.fileserver;

import java.io.File;
import java.io.IOException;
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
import solution.AbstractServer;
import solution.communication.TcpChannel;
import solution.util.FileUtils;
import util.ChecksumUtils;

public class FileServer extends AbstractServer implements IFileServer {

	private final String path;
	ConcurrentHashMap<String, Integer> files;

	public FileServer(final TcpChannel tcpChannel, Set<AbstractServer> connections, String path,
			ConcurrentHashMap<String, Integer> files) throws IOException {
		super(tcpChannel, connections);
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
		String filename = request.getFilename();
		println("Got version request for: " + filename);

		if (files.containsKey(filename)) {
			return new VersionResponse(request.getFilename(), files.get(request.getFilename()));
		} else {
			return new VersionResponse(request.getFilename(), -1);
		}
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		println("Got upload request for file: " + request.getFilename() + "with version " + request.getVersion());
		try {
			FileUtils.writeBytesToFile(path + "/" + request.getFilename(), request.getContent());
			versionFile(request.getFilename(), request.getVersion());
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
	 * Sets the version number of filename to version
	 * 
	 * @param filename
	 * @param version
	 */
	private void versionFile(String filename, int version) {

		files.put(filename, version);
	}
}
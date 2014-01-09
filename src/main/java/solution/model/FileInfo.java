package solution.model;

import java.io.Serializable;
import java.util.Comparator;

public class FileInfo implements Comparable<FileInfo>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int version;
	private int downloads;
	private String filename;
	
	public FileInfo() {
		version = 0;
		downloads = 0;
		filename = "";
	}

	public FileInfo(int version, int downloads) {
		this.version = version;
		this.downloads = downloads;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public int compareTo(FileInfo o) {
		return Comparators.DOWNLOAD.compare(this, o);
	}
	
	public static class Comparators {
		public static Comparator<FileInfo> DOWNLOAD = new Comparator<FileInfo>() {
			@Override
			public int compare(FileInfo f1, FileInfo f2) {
				return f2.downloads - f1.downloads;
			}
		};
	}
}

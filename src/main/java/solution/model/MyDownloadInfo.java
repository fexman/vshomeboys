package solution.model;

/**
 * Contains information about a file and the number of downloads of that file.
 */
public class MyDownloadInfo implements Comparable<MyDownloadInfo> {

    private String filename;
    private int downloads;
    
    public MyDownloadInfo(String filename, int downloads) {
        this.filename = filename;
        this.downloads = downloads;
    }

    public String getFilename() {
        return filename;
    }
    
    public int getDownloads() {
        return downloads;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        
        MyDownloadInfo other = (MyDownloadInfo) obj;
        return  this.getFilename().equals(other.getFilename()) &&
                other.getDownloads() == this.getDownloads();
    }

    @Override
    public int compareTo(MyDownloadInfo other) {

        if (downloads < other.getDownloads()) {
            return -1;
        }

        if (downloads > other.getDownloads()) {
            return 1;
        }

        return 0;
    }

    @Override
    public String toString() {

        return filename + "\t" + downloads;
    }
}
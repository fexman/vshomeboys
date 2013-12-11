package solution.fileserver;

import util.Config;
import cli.Shell;

public class FileServerCli1 extends FileServerCli {

	public static void main(String[] args) {
		
		new FileServerCli1(new Config("fs1"),new Shell("Fileserver 1",System.out, System.in));

	}
	
	public FileServerCli1(Config conf, Shell shell) {
		super(conf, shell);
	}
}

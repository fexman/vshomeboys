package solution.fileserver;

import util.Config;
import cli.Shell;

public class FileServerCli2 extends FileServerCli {

	public static void main(String[] args) {
		
		new FileServerCli2(new Config("fs2"),new Shell("Fileserver 2",System.out, System.in));

	}
	
	public FileServerCli2(Config conf, Shell shell) {
		super(conf, shell);
	}
}

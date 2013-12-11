package solution.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import message.request.LoginRequest;
import solution.model.MyFileServerInfo;

public class CheapTest {

	public static void main(String[] args) {
		HashSet<MyFileServerInfo> servers = new HashSet<MyFileServerInfo>();
		
		try {
			servers.add(new MyFileServerInfo(InetAddress.getByName("127.0.0.1"),22));
			System.out.println("Size: " + servers.size());
			MyFileServerInfo mfs2 = new MyFileServerInfo(InetAddress.getByName("127.0.0.1"),22);
			System.out.println("Contains: " + servers.contains(mfs2));
			servers.add(mfs2);
			System.out.println("Size: " + servers.size());
			servers.remove(new MyFileServerInfo(InetAddress.getByName("127.0.0.1"),22));
			System.out.println("Size: " + servers.size());
			
			System.out.println("\n---- SEP ----\n");
			
			System.out.println("Match1: "+Pattern.matches("!alive [0-9]{1,5}", "!alive 10024")); //true
			System.out.println("Match2: "+Pattern.matches("!alive [0-9]{1,5}", "!blive 10024")); //false
			System.out.println("Match3: "+Pattern.matches("!alive [0-9]{1,5}", "!alive ")); //false
			System.out.println("Match4: "+Pattern.matches("!alive [0-9]{1,5}", "!alive 655351")); //false
			System.out.println("Match5: "+Pattern.matches("!alive [0-9]{1,5}", "!alive 10024")); //true
			
			System.out.println("\n---- SEP ----\n");
			
			HashMap<Class<?>,String> testM = new HashMap<Class<?>,String>();
			testM.put(String.class, "strings");
			testM.put(LoginRequest.class, "login");
			
			System.out.println();


		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		


	}
	
	public static void check(String test) {
		System.out.println("got string: " + test);
	}
	public static void check(String test, Object o) {
		System.out.println("got string: " + test);
	}
	

}

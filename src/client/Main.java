package client;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		if (args.length != 2)
			usage();
		
		int port = 0;
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			usage();
		}
		try{
			new Client(args[0], port);
		} catch(IOException e){
			System.err.println("ERROR: Couldn't instantiate client: " + e.getMessage());
		}
	}
	
	private static void usage() {
		System.err.println("Usage: client <hostname> <port>");
		System.exit(1);
	}
	
}



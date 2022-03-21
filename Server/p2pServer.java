

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class p2pServer {

        static int port;
        static String host;
        static Registry registry = null;
                
	static public void main(String args[]) throws Exception
	{
                
                if(args.length!=2){
                    System.out.println("Usage: p2pServer <host> <port>");
                    System.exit(0);
                }
                host=args[0];
                port = Integer.parseInt(args[1]);      
		//assign security manager
		if(System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}
		
                    
		//create instance
		ServerImpl serverImpl = new ServerImpl();
		
		//bind in rmi registry
		try {
                        
                        registry = LocateRegistry.createRegistry(port);
			registry.bind("rmi://"+host+":" + port + "/server", serverImpl);
		 } catch (AlreadyBoundException e) {
			 // TODO Auto-generated catch block
			 System.out.println("\nError - Server is already bound.\n");
			 System.exit(0);
		 }
		
		
		System.out.println("Server \""+"rmi://"+host+":" + port + "/server"+"\" running...");
		
		//main program loop
		int num=1;
                HiloS h = new HiloS(serverImpl);
                h.start();
		System.out.println("Enter 0 to exit: ");
		while(num!=0)
		{
			Scanner scan = new Scanner(System.in);
			num=scan.nextInt();
		}
		h.stop();
		//unbind and exit
		registry.unbind("rmi://"+host+":" + port + "/server");
		System.exit(0);	
	}
	
        

        

	
}

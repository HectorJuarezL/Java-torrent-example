
import java.rmi.*;

//interface for Clientserver
public interface ClientI extends java.rmi.Remote {

	//method to return file from client
	public byte[] obtain(String file,int piece) 
		throws RemoteException;
        
        public boolean probe()
                throws RemoteException;
        
        public Progreso getProgress(String filename) 
                throws RemoteException;
        
        public int getId() 
                throws RemoteException;
	
}
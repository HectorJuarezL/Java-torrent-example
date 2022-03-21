
import java.rmi.*;
import java.util.List;

//interface for index server
public interface ServerI extends java.rmi.Remote {

	
	
        
        //registra los datos de un peer
        public boolean registryPeer(PeerData peer) 
		throws RemoteException; 
        
        public boolean unregistryPeer(PeerData peer) 
		throws RemoteException; 
        
        //registra un archivo y el peer asociado a este
	public  boolean registrySeeder(Integer peer,Torrent filename) 
		throws RemoteException; 
        
        public  boolean registryLeecher(Integer peer,Torrent filename) 
		throws RemoteException; 

	//busca el archivo y retorna una lista de los id de los peers que lo tienen
	public  List<Integer> searchFile(String filename)
		throws RemoteException;
        
        public  List<PeerData> getSeeders(String filename)
		throws RemoteException;
        
        public  List<PeerData> getLeechers(String filename)
		throws RemoteException;
        
        //retorna los datos de un peer
        public  PeerData getPeer(int peerId)
		throws RemoteException;
        
        public Torrent getTorrent(String filename)
		throws RemoteException;
        
        public String probe() throws RemoteException;
	
}

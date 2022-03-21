

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerImpl extends UnicastRemoteObject implements ServerI {

	private static final long serialVersionUID = 1L;

	//hashmap that maps filenames to a list of integers(peerids)
	private volatile ConcurrentMap<String, Torrent> Tracker;
	private volatile ConcurrentMap<Integer,PeerData> peersId;

        
        DateFormat dateFormat;
        Calendar cal;
        

	//constructor
	public ServerImpl() throws RemoteException {
		super();
                Tracker = new ConcurrentHashMap<String, Torrent>();
                peersId = new ConcurrentHashMap<Integer,PeerData>();
                dateFormat = new SimpleDateFormat("HH:mm:ss");
                
	}
        
        @Override
	public synchronized boolean registryPeer(PeerData peer) throws RemoteException {
            PeerData tmp=peersId.get(peer.Id);
            if(tmp == null )  //peerId no registrado
		{
			peersId.put(peer.Id,peer);
                        cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                        System.out.println("Se ha añadido el peer "+peer.Id);
                        return true;
		}
            else{
                if(peer.equals(tmp) ){
                    //en teoria, es el mismo peer asi que solo returna true y ya
                    return true;
                }else{//en caso de que no sea el mismo, hace una conexion con el antiguo peer
                    Registry registry = LocateRegistry.getRegistry(peer.host,peer.port);
                    try{
                        ClientI peerService =  (ClientI) registry.lookup("rmi://"+peer.host+":"+peer.port+"/" + "Peer" + peer.Id);
                        peerService.probe();
                    }catch(NotBoundException e){
                        //Si tuvo algun problema al encontrar el peer, este se elimina de la lista y se añade el nuevo
                        peersId.put(peer.Id,peer);
                        return true;
                    }
                        return false;//significa que se obtuvo conexion con el anterior peer, por lo tanto devuelve un false pues no añade el nuevo peer
                }
            }
        }
        
        public synchronized boolean unregistryPeer(PeerData peer) throws RemoteException {
            
           
            Registry registry = LocateRegistry.getRegistry(peer.host,peer.port);
            try{
                ClientI peerService =  (ClientI) registry.lookup("rmi://"+peer.host+":"+peer.port+"/" + "Peer" + peer.Id);
                peerService.probe();//verifica si aun existe el stub
                return false; //en caso de que funcione, retorna falso y no elimina el peer, esto evita que algun cliente quiera eliminar a alguien mas
            }catch(NotBoundException e){
                //Si tuvo algun problema al encontrar el peer, este se elimina de la lista
                peersId.remove(peer.Id, peer);
                cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                System.out.println("Se ha eliminado el peer "+peer.Id);
                return true;
            }
            
        }
        
        
        public synchronized void checkPeers(){
            
            Iterator<Map.Entry<Integer, PeerData>> it = peersId.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Integer, PeerData> entry = it.next();
                Integer key = entry.getKey();
                PeerData peer = entry.getValue();
                try{
                    Registry registry = LocateRegistry.getRegistry(peer.host,peer.port);
                    ClientI peerService =  (ClientI) registry.lookup("rmi://"+peer.host+":"+peer.port+"/" + "Peer" + peer.Id);
                    peerService.probe();
                }catch(NotBoundException e){
                    //Si tuvo algun problema al encontrar el peer, este se elimina de la lista 
                    peersId.remove(key);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("Se ha eliminado el peer "+key);
                    
                }catch(RemoteException e){
                    //Si tuvo algun problema al encontrar el peer, este se elimina de la lista 
                    peersId.remove(key);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("Se ha eliminado el peer "+key);
                }
            }
        }
        
	@Override
	public synchronized boolean registrySeeder(Integer peer,Torrent filename) throws RemoteException {
		// add filenames and peerid to the registry (assign by return peerids to clients)
	 
		Torrent tmp=null; //= new Torrent();
		//verifica si el archivo ya esta en el registro
		tmp = Tracker.get(filename.getName());
		
                if(tmp == null )  //si no encuentra el archivo
                {
                        Tracker.put(filename.getName(),filename );
                        cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                        System.out.println("S["+peer+"] ha añadido el archivo: "+filename.getName());
                }
                else //el archivo ya existe
                {
                    //verifica si el peer ya esta en la lista, en caso de que no, lo añade
                    for(Integer id :  tmp.getSeeders()){
                            if(Objects.equals(id, peer))  //el peer ya esta asociado con el archivo
                            {
                                    return true;//sale de la funcion
                            }
                    }
                    //el archivo existe pero el peer no, entonces lo añade
                    tmp.addSeeder(peer);
                    Tracker.put(filename.getName(), tmp);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("S["+peer+"] ha actualizado el archivo: "+filename.getName());

                }
                return true;
	}
        
        public synchronized boolean registryLeecher(Integer peer,Torrent filename) throws RemoteException {
		// add filenames and peerid to the registry (assign by return peerids to clients)
	 
		Torrent tmp=null; //= new Torrent();
		//verifica si el archivo ya esta en el registro
		tmp = Tracker.get(filename.getName());
                if(tmp == null )  //si no encuentra el archivo
                {
                        Tracker.put(filename.getName(),filename );
                        cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                        System.out.println("L["+peer+"] ha añadido el archivo: "+filename.getName());
                }
                else //el archivo ya existe
                {
                    //verifica si el peer ya esta en la lista, en caso de que no, lo añade
                    for(Integer id :  tmp.getLeechers()){
                            if(Objects.equals(id, peer))  //el peer ya esta asociado con el archivo
                            {
                                    return true;//sale de la funcion
                            }
                    }
                    //el archivo existe pero el peer no, entonces lo añade
                    tmp.addLeecher(peer);
                    Tracker.put(filename.getName(), tmp);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("L["+peer+"] ha sido añadido al archivo: "+filename.getName());

                }
		return true;
	}
	
        
                     
	
	/**
	 * method for clients to search the index for a file
	 */
        
	@Override
	public List<Integer> searchFile(String filename) throws RemoteException {
		// search the data structure for the file names.  return list of peers
		Torrent torrent = null;
		torrent = Tracker.get(filename);
                if(torrent ==null){
                        return null;
                }
                else{
                        return torrent.getSeeders();
                }
	}
        
        @Override
	public List<PeerData> getSeeders(String filename) throws RemoteException {
		// search the data structure for the file names.  return list of peers
		Torrent torrent = null;
                ArrayList<PeerData> peers=null;
		torrent = Tracker.get(filename);
                if(torrent ==null){
                        //System.out.println("Solicitud de archivo no encontrado");
                        return null;
                }
                else{
                    peers = new ArrayList<PeerData>();
                        
                        Iterator it = torrent.getSeeders().iterator();
                        while(it.hasNext()) {
                            Integer i = (Integer)it.next();
                            PeerData p = peersId.get(i);
                            if(p==null)
                                torrent.dropSeeder(i);
                            else{
                                peers.add(p);
                            }
                        }
                }
                return peers;
	}
        
        @Override
	public List<PeerData> getLeechers(String filename) throws RemoteException {
		// search the data structure for the file names.  return list of peers
		Torrent torrent = null;
                ArrayList<PeerData> peers = null;
		torrent = Tracker.get(filename);
                if(torrent ==null){
                        //System.out.println("Solicitud de archivo no encontrado");
                        return null;
                }
                else{
                    peers = new ArrayList<PeerData>();
                        for(Integer i: torrent.getLeechers())
                        {
                            PeerData p = peersId.get(i);
                            if(p==null)
                                torrent.dropLeecher(i);
                            else{
                                peers.add(p);
                            }
                        }
                }
                return peers;
	}
	
        public PeerData getPeer(int peerId) throws RemoteException{
            PeerData tmp = peersId.get(peerId);
            if(tmp==null)return null;
            return new PeerData(tmp);
        }
        
        public Torrent getTorrent(String filename) {
            Torrent tmp = null;
            tmp = Tracker.get(filename);
            return tmp;
        }
        
        public String probe() throws RemoteException{return "ok";}
	
}

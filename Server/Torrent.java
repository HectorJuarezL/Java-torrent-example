
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Torrent implements Serializable,Remote{
    
    private String name;
    private final int pieceLength=524288; //longitud de cada pieza en Bytes (512KB)
    private int pieces; //numero de piezas
    private long length; //longitud total del archivo en bytes
    private ArrayList<Integer> seeders;
    private ArrayList<Integer> leechers; 
    
    
    private static final long serialVersionUID = 1L;

    public Torrent()throws RemoteException{}

    public Torrent(String name, int pieceLength, int pieces, int length) throws RemoteException{
        this.name = name;
        //this.pieceLength = pieceLength;//en un futuro se podria modificar el programa para poder cambiar el tamaño de las piezas
        this.pieces = (int)Math.ceil((double)length/pieceLength);
        this.length = length;
        this.seeders = new ArrayList<>();
        this.leechers = new ArrayList<>();
    }
    
    public Torrent(String name, long length) throws RemoteException{
        this.name = name;
        this.length = length;
        this.pieces =  (int)Math.ceil((double)length/pieceLength);
        this.seeders = new ArrayList<>();
        this.leechers = new ArrayList<>();
    }
    
    public void addSeeder(Integer peerId)throws RemoteException{
        this.leechers.remove(peerId);//esto para eliminarlo de leecher si lo era
        this.seeders.remove(peerId);//esto es para evitar tener duplicados
        this.seeders.add(peerId);
    }

    public void dropSeeder(Integer peerId )throws RemoteException{
        this.seeders.remove(peerId);
    }

    public ArrayList<Integer> getSeeders() throws RemoteException{
        return (ArrayList<Integer>) seeders.clone();
    }
    
    public void addLeecher(Integer peerId)throws RemoteException{
        this.leechers.remove(peerId);//esto es para evitar tener duplicados
        this.leechers.add(peerId);
    }

    public void dropLeecher(Integer peerId )throws RemoteException{
        this.leechers.remove(peerId);
    }

    public ArrayList<Integer> getLeechers() throws RemoteException{
        return (ArrayList<Integer>) leechers.clone();
    }
    
    public void setSeeders(ArrayList<Integer> seeders) throws RemoteException {
        this.seeders =seeders;
    }

    public void setLeechers(ArrayList<Integer> leechers) throws RemoteException {
        this.leechers = leechers;
    }

    public String getName() throws RemoteException{
        return name;
    }

    public int getPieceLength() throws RemoteException{
        return pieceLength;
    }

    public int getPieces() throws RemoteException{
        return pieces;
    }

    public long getLength() throws RemoteException{
        return length;
    }

    public Torrent copy() throws RemoteException{
        Torrent to = new Torrent(this.name,this.length);
        to.setLeechers(this.getLeechers());
        to.setSeeders(this.getSeeders());
        return to;
    }
  
  
    
    

}
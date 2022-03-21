
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//interface for Clientserver
public class ClientImpl extends UnicastRemoteObject implements ClientI  {

	// peer identifier
	
        final int PIECE_LENGTH=524288;
    
	long avgResponseTime = 0;
	long aggregateResponseTime = 0;
	int numLookups = 0;
        ServerI IndexServer;
        String instanceName;
        String dirname;
        File dir;
        
        int inConnections;
        int outConnections;
        
        PeerData p;
        final Fragmentacion frg;
        
        
        ConcurrentMap<String,Progreso> progreso;
	
	// constructor
	public ClientImpl(ServerI IndexServer,PeerData p) throws RemoteException {

                super();
                progreso = new ConcurrentHashMap<String,Progreso>();
                this.IndexServer = IndexServer;
                this.p = p;
                instanceName = "Peer" + p.Id;
                dirname = instanceName;
                dir = new File(dirname);
		if (!dir.exists()) {
			System.out.println("Creating new shared directory");
			dir.mkdir();
		}
                System.out.println(1);
                System.out.println(2);
                frg=new Fragmentacion();
	}
        
        public boolean run(){
            // main UI loop
		int choice=0;
		String s;
		Scanner scan = new Scanner(System.in);
		InputStreamReader stream = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(stream);
		boolean loop = true;
		String filename;
		
                register(dir);
                try {
                    System.out.println("\n\n" + p);
                    System.out.println("Options:");
                    System.out.println("1 - Search for filename");
                    System.out.println("2 - Obtain filename");
                    System.out.println("3 - List files in shared directory");
                    System.out.println("4 - Exit");
                    
                    System.out.print("\n\n>");

                    s = scan.nextLine();
                    try { choice = Integer.parseInt(s.trim()); }
                    catch(NumberFormatException e) {
                        //System.out.println("\nPlease enter an integer\n");
                    }

                    switch (choice) {


                        case 1:
                            System.out.print("Enter filename: ");
                            filename = in.readLine();
                            System.out.print("\n");
                            search(filename);
                            break;

                        case 2:
                            System.out.print("Enter filename: ");
                            filename = in.readLine();
                            getFile(filename);
                            
                            break;

                        case 3:
                            list();
                            break;

                        case 4:
                            
                            loop = false;
                            break;

                        
                        default:
                            System.out.println("\nPlease enter a number between 1 and 4\n");
                            break;
                    }

                }
                catch(IOException ex) {
                    Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                    //System.out.println("\nPlease enter an integer\n");
                }

                return loop;
        }

        @Override
	public byte[] obtain(String file, int piece) {

		if(inConnections<15)inConnections++;//COMPRUEBA EL LIMITE DE CONEXIONES
                else return null;
                
                byte[] bytes = null;
                
                
		// create reader in order to read local file into byte array
                
                
                
		
		String pathfile = instanceName + "/" + file+"."+piece+".bin";

		// test if file exists
		File readfile = new File(pathfile);
		if (!readfile.exists()) {
                    String pathfile2frag=instanceName + "/" + file;
                    File file2frag = new File(pathfile2frag);
                    if (!file2frag.exists()) {
                        
                        inConnections--;
                        return null;
                    }
                    if(piece==0){//significa que es la primera pieza
                        int size = (int) file2frag.length();
                        if(size<=PIECE_LENGTH){
                            // significa que el archivo es mas pequeño que los tamaños de las piezas
                            bytes = frg.read(readfile);
                            inConnections--;
                            return bytes;
                        }
                    }
                    frg.fragmentar(pathfile2frag,instanceName+"/",PIECE_LENGTH);
			
		}

		
		
                bytes = frg.read(readfile);
		inConnections--;
                if(bytes==null){
                    return null;
                }
		return bytes;

	}

	/**
	 * Method for clients to list files currently in their shared directory
	 */
	
        public void list() {
		// lists files in shared directory
		File[] sharedfiles = dir.listFiles();

		System.out.println("\n\nFiles in shared directory: ");
		
                for (int i = 0; i < sharedfiles.length; i++) {
                    
                    if(! sharedfiles[i].getName().endsWith(".bin")){
                        System.out.println(sharedfiles[i].getName());
                    }
                    
		}
		System.out.print("\n\n");

	}

	/**
	 * Searches index server for filename and returns list of peers sharing that
	 * file
	 */
	private void search(String filename)
			throws RemoteException {

		
		numLookups++;
		// Get response time also
		final long lookupstartTime = System.nanoTime();
		final long lookupendTime;
		try {
			List<Integer> peers =  IndexServer.searchFile(filename);

			// No one sharing that file
			if (peers == null) {
				System.out.println("\n\nNo se han encontrado peers con el archivo ("+ filename + ")\n\n");
				return;
			}

			// 1 or more peers has file
			System.out.print("Los siguientes Peers tienen el archivo (" + filename+ ") :\n");
			
                        for(Integer pId : peers){
                            System.out.println(pId);
                        }
                        
			System.out.print("\n\n");

		} finally {
			lookupendTime = System.nanoTime();
		}
		final long lookupduration = lookupendTime - lookupstartTime;
		System.out.println("Lookup Response time: " + lookupduration
				+ " ns");
		aggregateResponseTime+=lookupduration;
		
	}

	/**
	 * Method to register shared files with Index Server Automatically called at
	 * run time of client Subsequently called each time a new file is downloaded
	 * into shared directory
	 * 
	 */
	private void register(File dir){

		// go through shared directory and register filenames with index server
		File[] sharedfiles = dir.listFiles();
                int totalSF=0;
                Torrent tmp;
		System.out.println(dir.getPath());
		// no files
		if (sharedfiles.length == 0) {
                    System.out.println("No existen archivos");
                    return;
		}
                
                try{
                    IndexServer.registryPeer(p);
                }catch(RemoteException em){
                    System.out.println("\nError - No se pudo conectar con el servidor.\nSe intentará denuevo en 5s;");
                    try {
                        Thread.sleep(5000);
                        register(dir);
                        return;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try{
                    // register all files
                    for (int i = 0; i < sharedfiles.length; i++) {

                        if(! sharedfiles[i].getName().endsWith(".bin")){
                            tmp = new Torrent(sharedfiles[i].getName(),sharedfiles[i].length());
                            tmp.addSeeder(p.Id);
                            IndexServer.registrySeeder(p.Id,tmp);
                            Progreso prg = new Progreso(tmp.getName(),tmp.getPieces());
                            prg.setFull();
                            progreso.put(sharedfiles[i].getName(),prg);
                            totalSF++;
                        }
                    }
                }catch(RemoteException e){
                    System.out.println("Error al conectar con el servidor");
                    return;
                }
                System.out.println("# of files registered: " + totalSF);

	}

        
	/**
	 * Method to set up connection with peer, call obtain() to get file, write
	 * file to local shared directory and register file with Index Server
	 * 
	 */
        
        public Progreso getProgress(String filename){
            return progreso.get(filename);
        }
        
        
	private void getFile(String filename)
			throws FileNotFoundException, IOException {

                //obtiene los datos del torrent
                List<PeerData> seeders=null;
                List<PeerData> leechers=null;
                int[] connection = new int[4];//esto es para saber los Id de los peer con los que ya se conecto (ademas, permite 4 conexiones por archivo)
                int i=0;
                int k=0;
                Torrent to= IndexServer.getTorrent(filename);
                if(to==null){
                    System.out.println("File not found");
                    return;
                }
		
                Progreso prg = new Progreso(filename,to.getPieces(),connection);
                VentanaD vent = new VentanaD(this,filename,prg);
                
                progreso.put(filename,prg);
                
                seeders = IndexServer.getSeeders(filename);
                if(seeders==null){
                    System.out.println("There are not enough seeders");
                    return;
                }
                seeders.remove(p);//quita al peer actual de la lista para evitar conectarse consigo mismo
                if(seeders.size()<1){
                    System.out.println("There are not enough seeders");
                    return;
                }
                
                if(prg.getPiecesLength()==1){//es de 512KB o menor
                    HiloP hilo;
                    for(PeerData sd: seeders){
                        System.out.println("El archivo es pequeño");
                        hilo = new HiloP(sd,prg,instanceName,filename);
                        if(hilo.probe()){
                            hilo.start();
                            return;
                        }
                    }
                    System.out.println("There are not enough seeders");
                    return;
                }
                leechers = IndexServer.getLeechers(filename);
                
                Torrent tmp = new Torrent(filename,to.getLength());
                tmp.addLeecher(p.Id);
                IndexServer.registryLeecher(p.Id,tmp);
                
                
                HiloP[] hilos = new HiloP[4];
                vent.setVisible(true);
                
                
                Iterator<PeerData> it = seeders.iterator();
                
                while(it.hasNext()){
                    PeerData sd = (PeerData)it.next();
                    hilos[0] = new HiloP(sd,prg,instanceName,filename);
                    if(hilos[0].probe()){
                        System.out.println("Se conectara con SD:"+sd);
                        hilos[0].start();
                        connection[k]=sd.Id;
                        k++;
                        break;
                    }
                }
                
                if(hilos[0]==null){
                    System.out.println("No fue posible conectarse con ningun seeder\nSe ha cancelado la descarga");
                    return;
                }
                leechers.remove(p);
                if(leechers.size()>0){
                    for(PeerData lch: leechers){
                        hilos[k] = new HiloP(lch,prg,instanceName,filename);
                        if(hilos[k].probe()){
                            System.out.println("Se conectara con LCH:"+lch);
                            hilos[k].start();
                            connection[k]=lch.Id;
                            k++;
                            if(k==4){
                                break;
                            }
                        }
                    }
                }
                
                if(k<3){
                    while(it.hasNext()){
                    PeerData sd = (PeerData)it.next();
                        hilos[k] = new HiloP(sd,prg,instanceName,filename);
                        if(hilos[k].probe()){
                            System.out.println("Se conectara con sd:"+sd);
                            hilos[k].start();
                            connection[k]=sd.Id;
                            k++;
                            if(k==4){
                                break;
                            }
                        }
                        i++;
                    }
                }
                
                to.addLeecher(p.Id);
                
                System.out.println("Numero de conexiones: "+k);
                
               
	}
        
        public void unir(String filename,int piezas){
            
                System.out.println("Uniendo archivo...");
                // write file to shared directory
                String strFilePath = instanceName + "/" + filename;
                File tmpFile = new File(filename);
                if(frg.unirPartes(strFilePath,piezas)){
                    System.out.println("Archivo unido exitosamente.!\n>");
                    try{
                        Torrent tmp = new Torrent(filename,tmpFile.length());
                        tmp.addSeeder(p.Id);
                        IndexServer.registrySeeder(p.Id,tmp);
                    }catch(RemoteException e){
                        System.out.println("Error al conectar con el servidor");
                        return;
                    }
                }
                
            
            
        }
        
        public int getId() throws RemoteException{
            return p.Id;
        }
        
        @Override
        public boolean probe() throws RemoteException{
            //System.out.println("Id peer: "+p.Id);
            return true;
        }
}

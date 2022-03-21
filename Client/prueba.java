//NOTA: Este archivo lo uso para resolver problemas que he llegado a encontrar, por ejemplo, un problema con el registro que tenia.



import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class prueba{


public static void main(String[] args){

                if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
    
		try {
                        Registry registry = LocateRegistry.getRegistry(4455);
			ClientI servingPeer =  (ClientI) registry.lookup("rmi://localhost:4455/Client2");
			//servingPeer.list();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			System.out.println("\nError - Invalid peer entered.  Please enter valid peer");
                        e.printStackTrace();
                }catch (RemoteException e) {
                        System.out.println("\nError - RemoteException");
                        e.printStackTrace();
                }
		
	}
}
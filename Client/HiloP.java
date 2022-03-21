/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author daybr_000
 */


public class HiloP {
    
        private Timer timer;
        private TimeOutTask task;
        Progreso p,pB;
        PeerData rp;
        String instanceName;
        String filename;
        Fragmentacion frg;
        ClientI servingPeer=null;
        
        HiloP(PeerData rp,Progreso p,String instanceName, String filename){
            this.rp=rp;
            this.p=p;
            this.instanceName=instanceName;
            this.filename=filename;
            frg = new Fragmentacion();
        }
        public void start() {
        //EJERCICIO
             
             if(p.getPiecesLength()==1){
                 try{
                    byte[] temp = null;
                    int i;
                    Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                    servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                                + "Peer" + rp.Id);
                    while(p.getValue()<100){
                        if((i = p.getIndexPieceNull()) == -1){
                            break;
                        }
                        temp=servingPeer.obtain(filename,i);
                        if(temp==null){
                            System.out.println("Error, temp=null");
                        }
                        frg.write(temp,instanceName+"/"+filename);
                        p.setPieceValue(i,(short)2);
                    }
                    return;
                } catch (NotBoundException e) {
                        System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
                }catch (RemoteException e) {
                        System.out.println("\nError - RemoteException <class HiloP:start>");
                        return;
                }
             }
             timer = new Timer();
             task = new TimeOutTask();
             timer.schedule(task, 0);
        }
        
        public boolean probe(){
            boolean res=false;
            try {//obtiene el stub
                Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                                + "Peer" + rp.Id);
                res=servingPeer.probe();
            } catch (NotBoundException e) {
                System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }catch (RemoteException e) {
                System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }
            
            return res;
        }

        public void stop() {
            timer.cancel();
            timer.purge();
        }
        class TimeOutTask extends TimerTask {

        
            @Override
            public void run() {
                byte[] temp = null;
                int i;
                try{
                    System.out.println("Conectando con: "+rp);
                    Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                    servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                                + "Peer" + rp.Id);
                    pB=servingPeer.getProgress(filename);
                    while(p.getValue()<100){
                        if((i = p.getIndexPieceNull(pB)) == -1){
                            break;
                        }
                        temp=servingPeer.obtain(filename,i);
                        if(temp==null){
                            System.out.println("Error: temp=null; i="+i);
                            continue;
                        }
                        frg.write(temp,instanceName+"/"+filename,i);
                        p.setPieceValue(i,(short)2);
                    }
                    stop();
                } catch (NotBoundException e) {
                        System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
                }catch (RemoteException e) {
                        System.out.println("\nError - RemoteException <class HiloP:run>");
                        return;
                }
                    
            }
        }
    }
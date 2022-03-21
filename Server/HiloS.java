/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 *
 * @author daybr_000
 */


public class HiloS {
    
        private Timer timer;
        private TimeOutTask task;
        ServerImpl s;
        long startTime;
        long endTime;
        
        HiloS(ServerImpl s){
            this.s=s;
        }
        public void start() {
        //EJERCICIO
             timer = new Timer();
             task = new TimeOutTask();
             startTime = System.nanoTime();
             timer.scheduleAtFixedRate(task, 0, 5000);//cada 5s
        }
        


        public void stop() {
        
            endTime=System.nanoTime();
            double duration = (endTime - startTime)/1000000000.0;
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("Sesion total time: "+ df.format(duration) + "s");
            timer.cancel();
            timer.purge();
        }
        class TimeOutTask extends TimerTask {

            //Progreso p;
            
            //TimeOutTask(Progreso p){
            //    this.p=p;
            //}
        
            @Override
            public void run() {
                    s.checkPeers();
                    
            }
        }
    }
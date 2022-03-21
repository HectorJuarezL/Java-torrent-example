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


public class HiloV {
    
        private Timer timer;
        private TimeOutTask task;
        JProgressBar pbFile;
        Progreso p;
        ClientImpl c;
        
        long startTime;
        long endTime;
        
        HiloV(ClientImpl c,JProgressBar pbFile ,Progreso p){
            this.c=c;
            this.pbFile=pbFile;
            this.p=p;
        }
        public void start() {
        //EJERCICIO
             timer = new Timer();
             task = new TimeOutTask();
             startTime = System.nanoTime();
             timer.scheduleAtFixedRate(task, 0, 100);
        }
        


        public void stop() {
        
            endTime=System.nanoTime();
            double duration = (endTime - startTime)/1000000000.0;
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("Download Response time: "+ df.format(duration) + "s");
            JOptionPane.showMessageDialog(null, "Descarga finalizada");
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
                    pbFile.setValue(p.getValue());
                    if(p.getValue()==100){
                        if(p.getPiecesLength()>1)
                            c.unir(p.getFilename(),p.getPiecesLength());
                        stop();
                    }
            }
        }
    }
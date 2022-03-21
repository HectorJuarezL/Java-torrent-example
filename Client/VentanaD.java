/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 *
 * @author daybr_000
 */
public class VentanaD extends JFrame{
    
    JProgressBar pbFile;
    
    VentanaD(ClientImpl c,String title, Progreso p){
        super("Downloading");
        pbFile = new JProgressBar();
        //pbFile.setValue(p.v);
        pbFile.setMaximum(100);
        pbFile.setStringPainted(true);
        pbFile.setBorder(BorderFactory.createTitledBorder("Downloading: "+title));
        
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = this.getContentPane();
        contentPane.add(pbFile, BorderLayout.SOUTH);
        
        HiloV hv= new HiloV(c,pbFile,p);
        hv.start();
        setSize(300, 100);
        setLocationRelativeTo(null);
    }
    

        
}

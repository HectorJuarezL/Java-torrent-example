
import java.io.Serializable;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author daybr_000
 */
public class Progreso implements Remote, Serializable {
    
    private static final long serialVersionUID = 1L;
    private String filename;
    private short pieces[];//0=no tiene la pieza;1=se esta descargando;2=pieza descargada
    private int[] connection;
    private boolean full=false;//indica si esta completo
    
    Progreso(String filename,short[] pieces,int[] connection){
        this.filename=filename;
        this.pieces = pieces;
        this.connection=connection;
    }
    
    Progreso(String filename,int totalPieces,int[] connection){
        this.filename=filename;
        pieces = new short[totalPieces];
        this.connection=connection;
    }
    
    Progreso(String filename,int totalPieces){
        this.filename=filename;
    }
    
    public void setFull(){
        full=true;
    }

    public String getFilename(){
        return filename;
    }
    
    public int getValue() {
        int v=0;
        for(short piece: pieces ){
            if(piece==2)v++;
        }
        v=100*v/pieces.length;
        if(v==100) full=true;
        return v;
    }

    public short[] getPieces() {
        return pieces;
    }
    
    public int getPiecesLength() {
        return pieces.length;
    }

    public synchronized void setPieces(short[] pieces) {
        this.pieces = pieces;
    }
    
    public short getPieceValue(int i) throws StackOverflowError{
        return pieces[i];
    }

    public synchronized void setPieceValue(int i,short value) throws StackOverflowError{
        pieces[i] = value;
    }
    
    public synchronized int getIndexPieceNull(){//obtiene el indice de la primera pieza sin descargar
        for(int i=0;i<pieces.length;i++ ){
            if(pieces[i]==0){
                pieces[i]=1;
                return i;
            }
        }
        return -1;
    }
    
    public boolean getState(){
        return full;
    }
    
    public synchronized int getIndexPieceNull(Progreso p){//obtiene el indice de la primera pieza sin descargar
        
        if(p.getState()){
            for(int i=0;i<pieces.length;i++ ){
                if(pieces[i]==0){
                    pieces[i]=1;
                    return i;
                }
            }
        }else{
            for(int i=0;i<pieces.length;i++ ){
                if(pieces[i]==0 && getPieceValue(i)==2){
                    pieces[i]=1;
                    return i;
                }
            }
        }
        
        return -1;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.filename);
        hash = 89 * hash + Arrays.hashCode(this.pieces);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Progreso other = (Progreso) obj;
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        if (!Arrays.equals(this.pieces, other.pieces)) {
            return false;
        }
        return true;
    }
    
    
    
}

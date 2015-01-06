/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.gui.stpm.clean;

/**
 *
 * @author hercules
 */
public class TrajetoryCleanDto {

    private long gid;
    private int tid;
    private double tempo, tempo2, distancia, velocidade;

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public double getTempo2() {
        return tempo2;
    }

    public void setTempo2(double tempo2) {
        this.tempo2 = tempo2;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public double getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(double velocidade) {
        this.velocidade = velocidade;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrajetoryCleanDto other = (TrajetoryCleanDto) obj;
        if (this.gid != other.gid) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.gid ^ (this.gid >>> 32));
        return hash;
    }

}

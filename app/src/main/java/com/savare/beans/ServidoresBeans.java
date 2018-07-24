package com.savare.beans;

/**
 * Created by Bruno on 14/11/2017.
 */

public class ServidoresBeans {
    private int idServidores, porta;
    private String ipServidor, nomeServidor;

    public int getIdServidores() {
        return idServidores;
    }

    public void setIdServidores(int idServidores) {
        this.idServidores = idServidores;
    }

    public String getNomeServidor() {
        return nomeServidor;
    }

    public void setNomeServidor(String nomeServidor) {
        this.nomeServidor = nomeServidor;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getIpServidor() {
        return ipServidor;
    }

    public void setIpServidor(String ipServidor) {
        this.ipServidor = ipServidor;
    }
}

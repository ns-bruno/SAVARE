package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 09/03/2016.
 */
public class RetornoWebServiceBeans {

    private int codigoRetorno;
    private String mensagemRetorno;
    private Object extra;

    public int getCodigoRetorno() {
        return codigoRetorno;
    }

    public void setCodigoRetorno(int codigoRetorno) {
        this.codigoRetorno = codigoRetorno;
    }

    public String getMensagemRetorno() {
        return mensagemRetorno;
    }

    public void setMensagemRetorno(String mensagemRetorno) {
        this.mensagemRetorno = mensagemRetorno;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}

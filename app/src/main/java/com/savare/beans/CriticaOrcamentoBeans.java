package com.savare.beans;

/**
 * Created by Bruno on 27/02/2017.
 */

public class CriticaOrcamentoBeans {

    private int idCritica, idOrcamento, codigoRetornoWebservice;
    private String dataCadastro, status, retornoWebservice;

    public int getIdCritica() {
        return idCritica;
    }

    public void setIdCritica(int idCritica) {
        this.idCritica = idCritica;
    }

    public int getIdOrcamento() {
        return idOrcamento;
    }

    public void setIdOrcamento(int idOrcamento) {
        this.idOrcamento = idOrcamento;
    }

    public int getCodigoRetornoWebservice() {
        return codigoRetornoWebservice;
    }

    public void setCodigoRetornoWebservice(int codigoRetornoWebservice) {
        this.codigoRetornoWebservice = codigoRetornoWebservice;
    }

    public String getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(String dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRetornoWebservice() {
        return retornoWebservice;
    }

    public void setRetornoWebservice(String retornoWebservice) {
        this.retornoWebservice = retornoWebservice;
    }
}

package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 12/08/2016.
 */
public class CartaoBeans {

    private int idCartao, codigoCartao;
    private String dataAlteracao, descricaoCartao;

    public int getIdCartao() {
        return idCartao;
    }

    public void setIdCartao(int idCartao) {
        this.idCartao = idCartao;
    }

    public int getCodigoCartao() {
        return codigoCartao;
    }

    public void setCodigoCartao(int codigoCartao) {
        this.codigoCartao = codigoCartao;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getDescricaoCartao() {
        return descricaoCartao;
    }

    public void setDescricaoCartao(String descricaoCartao) {
        this.descricaoCartao = descricaoCartao;
    }
}

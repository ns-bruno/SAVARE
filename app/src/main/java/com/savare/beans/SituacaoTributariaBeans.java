package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 15/08/2016.
 */
public class SituacaoTributariaBeans {

    private int idSituacaoTributaria, codigoSituacaoTributaria;
    private String dataAlteracao, descricaoSituacaoTributaria, tipo, origem;

    public int getIdSituacaoTributaria() {
        return idSituacaoTributaria;
    }

    public void setIdSituacaoTributaria(int idSituacaoTributaria) {
        this.idSituacaoTributaria = idSituacaoTributaria;
    }

    public int getCodigoSituacaoTributaria() {
        return codigoSituacaoTributaria;
    }

    public void setCodigoSituacaoTributaria(int codigoSituacaoTributaria) {
        this.codigoSituacaoTributaria = codigoSituacaoTributaria;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getDescricaoSituacaoTributaria() {
        return descricaoSituacaoTributaria;
    }

    public void setDescricaoSituacaoTributaria(String descricaoSituacaoTributaria) {
        this.descricaoSituacaoTributaria = descricaoSituacaoTributaria;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }
}

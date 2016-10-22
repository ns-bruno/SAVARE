package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 15/08/2016.
 */
public class LocalEstoqueBeans {


    private int idLocalEstoque, idEmpresa, codigoLocalEstoque;
    private String dataAlteracao, descricaoLocalEstoque, ativo, tipoVenda;

    public int getIdLocalEstoque() {
        return idLocalEstoque;
    }

    public void setIdLocalEstoque(int idLocalEstoque) {
        this.idLocalEstoque = idLocalEstoque;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getCodigoLocalEstoque() {
        return codigoLocalEstoque;
    }

    public void setCodigoLocalEstoque(int codigoLocalEstoque) {
        this.codigoLocalEstoque = codigoLocalEstoque;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getDescricaoLocalEstoque() {
        return descricaoLocalEstoque;
    }

    public void setDescricaoLocalEstoque(String descricaoLocalEstoque) {
        this.descricaoLocalEstoque = descricaoLocalEstoque;
    }

    public String getAtivo() {
        return ativo;
    }

    public void setAtivo(String ativo) {
        this.ativo = ativo;
    }

    public String getTipoVenda() {
        return tipoVenda;
    }

    public void setTipoVenda(String tipoVenda) {
        this.tipoVenda = tipoVenda;
    }
}

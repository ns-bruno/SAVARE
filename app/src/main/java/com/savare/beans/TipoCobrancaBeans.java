package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 13/08/2016.
 */
public class TipoCobrancaBeans {

    private int idTipoCobranca, codigo;
    private String dataAlteracao, descricaoTipoCobranca, siglaTipoCobranca;

    public int getIdTipoCobranca() {
        return idTipoCobranca;
    }

    public void setIdTipoCobranca(int idTipoCobranca) {
        this.idTipoCobranca = idTipoCobranca;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getDescricaoTipoCobranca() {
        return descricaoTipoCobranca;
    }

    public void setDescricaoTipoCobranca(String descricaoTipoCobranca) {
        this.descricaoTipoCobranca = descricaoTipoCobranca;
    }

    public String getSiglaTipoCobranca() {
        return siglaTipoCobranca;
    }

    public void setSiglaTipoCobranca(String siglaTipoCobranca) {
        this.siglaTipoCobranca = siglaTipoCobranca;
    }
}

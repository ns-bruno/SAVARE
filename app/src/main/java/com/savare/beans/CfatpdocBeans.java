package com.savare.beans;

public class CfatpdocBeans {
    private int idCfatpdoc,
                codigo;
    private SmaempreBeans smaempre;
    private String  dtAlt,
                    descricao,
                    sigla,
                    tipo;

    public int getIdCfatpdoc() {
        return idCfatpdoc;
    }

    public void setIdCfatpdoc(int idCfatpdoc) {
        this.idCfatpdoc = idCfatpdoc;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public SmaempreBeans getSmaempre() {
        return smaempre;
    }

    public void setSmaempre(SmaempreBeans smaempre) {
        this.smaempre = smaempre;
    }

    public String getDtAlt() {
        return dtAlt;
    }

    public void setDtAlt(String dtAlt) {
        this.dtAlt = dtAlt;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}

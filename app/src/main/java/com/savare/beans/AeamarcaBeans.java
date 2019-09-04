package com.savare.beans;

public class AeamarcaBeans {

    private int idAeamarca;
    private String  dtAlt,
                    descricao;
    private double fatorVenda;

    public int getIdAeamarca() {
        return idAeamarca;
    }

    public void setIdAeamarca(int idAeamarca) {
        this.idAeamarca = idAeamarca;
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

    public double getFatorVenda() {
        return fatorVenda;
    }

    public void setFatorVenda(double fatorVenda) {
        this.fatorVenda = fatorVenda;
    }
}

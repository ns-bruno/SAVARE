package com.savare.beans;

public class AeaembalBeans {

    private int idAeaembal,
                modulo,
                decimais;
    private AeaproduBeans aeaprodu;
    private AeaunvenBeans aeaunven;
    private String  dtAlt,
                    principal,
                    descricao,
                    ativo;
    private double  fatorConversao,
                    fatorPreco;

    public int getIdAeaembal() {
        return idAeaembal;
    }

    public void setIdAeaembal(int idAeaembal) {
        this.idAeaembal = idAeaembal;
    }

    public int getModulo() {
        return modulo;
    }

    public void setModulo(int modulo) {
        this.modulo = modulo;
    }

    public int getDecimais() {
        return decimais;
    }

    public void setDecimais(int decimais) {
        this.decimais = decimais;
    }

    public AeaproduBeans getAeaprodu() {
        return aeaprodu;
    }

    public void setAeaprodu(AeaproduBeans aeaprodu) {
        this.aeaprodu = aeaprodu;
    }

    public AeaunvenBeans getAeaunven() {
        return aeaunven;
    }

    public void setAeaunven(AeaunvenBeans aeaunven) {
        this.aeaunven = aeaunven;
    }

    public String getDtAlt() {
        return dtAlt;
    }

    public void setDtAlt(String dtAlt) {
        this.dtAlt = dtAlt;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getAtivo() {
        return ativo;
    }

    public void setAtivo(String ativo) {
        this.ativo = ativo;
    }

    public double getFatorConversao() {
        return fatorConversao;
    }

    public void setFatorConversao(double fatorConversao) {
        this.fatorConversao = fatorConversao;
    }

    public double getFatorPreco() {
        return fatorPreco;
    }

    public void setFatorPreco(double fatorPreco) {
        this.fatorPreco = fatorPreco;
    }
}

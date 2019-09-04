package com.savare.beans;

public class AeaproduBeans {

    private int idAeaprodu;
    private AeafamilBeans aeafamil;
    private AeaclaseBeans aeaclase;
    private AeagrupoBeans aeagrupo;
    private AeasgrupBeans aeasgrup;
    private AeamarcaBeans aeamarca;
    private AeaunvenBeans aeaunven;
    private CfafotosBeans cfafotos;

    private String dtCad,
                    dtAlt,
                    descricao,
                    descricaoAuxiliar,
                    codigoEstrutural,
                    referencia,
                    codigoBarras,
                    ativo,
                    tipo;
    private double  pesoLiquido,
                    pesoBruto;



    public int getIdAeaprodu() {
        return idAeaprodu;
    }

    public void setIdAeaprodu(int idAeaprodu) {
        this.idAeaprodu = idAeaprodu;
    }

    public AeafamilBeans getAeafamil() {
        return aeafamil;
    }

    public void setAeafamil(AeafamilBeans aeafamil) {
        this.aeafamil = aeafamil;
    }

    public AeaclaseBeans getAeaclase() {
        return aeaclase;
    }

    public void setAeaclase(AeaclaseBeans aeaclase) {
        this.aeaclase = aeaclase;
    }

    public AeagrupoBeans getAeagrupo() {
        return aeagrupo;
    }

    public void setAeagrupo(AeagrupoBeans aeagrupo) {
        this.aeagrupo = aeagrupo;
    }

    public AeasgrupBeans getAeasgrup() {
        return aeasgrup;
    }

    public void setAeasgrup(AeasgrupBeans aeasgrup) {
        this.aeasgrup = aeasgrup;
    }

    public AeamarcaBeans getAeamarca() {
        return aeamarca;
    }

    public void setAeamarca(AeamarcaBeans aeamarca) {
        this.aeamarca = aeamarca;
    }

    public AeaunvenBeans getAeaunven() {
        return aeaunven;
    }

    public void setAeaunven(AeaunvenBeans aeaunven) {
        this.aeaunven = aeaunven;
    }

    public String getDtCad() {
        return dtCad;
    }

    public void setDtCad(String dtCad) {
        this.dtCad = dtCad;
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

    public String getDescricaoAuxiliar() {
        return descricaoAuxiliar;
    }

    public void setDescricaoAuxiliar(String descricaoAuxiliar) {
        this.descricaoAuxiliar = descricaoAuxiliar;
    }

    public String getCodigoEstrutural() {
        return codigoEstrutural;
    }

    public void setCodigoEstrutural(String codigoEstrutural) {
        this.codigoEstrutural = codigoEstrutural;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getAtivo() {
        return ativo;
    }

    public void setAtivo(String ativo) {
        this.ativo = ativo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPesoLiquido() {
        return pesoLiquido;
    }

    public void setPesoLiquido(double pesoLiquido) {
        this.pesoLiquido = pesoLiquido;
    }

    public double getPesoBruto() {
        return pesoBruto;
    }

    public void setPesoBruto(double pesoBruto) {
        this.pesoBruto = pesoBruto;
    }

    public CfafotosBeans getCfafotos() {
        return cfafotos;
    }

    public void setCfafotos(CfafotosBeans cfafotos) {
        this.cfafotos = cfafotos;
    }
}

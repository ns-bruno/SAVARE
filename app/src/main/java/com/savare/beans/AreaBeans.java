package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 07/01/2016.
 */
public class AreaBeans {

    private int idArea, codigo;
    private String descricaoArea;
    private double descontoAtacadoVista, descontoAtacadoPrazo, descontoVarejoVista, descontoVarejoPrazo, descontoServicoVista, descontoServicoPrazo;
    private char promocao;

    public int getIdArea() {
        return idArea;
    }

    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getDescricaoArea() {
        return descricaoArea;
    }

    public void setDescricaoArea(String descricaoArea) {
        this.descricaoArea = descricaoArea;
    }

    public double getDescontoAtacadoVista() {
        return descontoAtacadoVista;
    }

    public void setDescontoAtacadoVista(double descontoAtacadoVista) {
        this.descontoAtacadoVista = descontoAtacadoVista;
    }

    public double getDescontoAtacadoPrazo() {
        return descontoAtacadoPrazo;
    }

    public void setDescontoAtacadoPrazo(double descontoAtacadoPrazo) {
        this.descontoAtacadoPrazo = descontoAtacadoPrazo;
    }

    public double getDescontoVarejoVista() {
        return descontoVarejoVista;
    }

    public void setDescontoVarejoVista(double descontoVarejoVista) {
        this.descontoVarejoVista = descontoVarejoVista;
    }

    public double getDescontoVarejoPrazo() {
        return descontoVarejoPrazo;
    }

    public void setDescontoVarejoPrazo(double descontoVarejoPrazo) {
        this.descontoVarejoPrazo = descontoVarejoPrazo;
    }

    public double getDescontoServicoVista() {
        return descontoServicoVista;
    }

    public void setDescontoServicoVista(double descontoServicoVista) {
        this.descontoServicoVista = descontoServicoVista;
    }

    public double getDescontoServicoPrazo() {
        return descontoServicoPrazo;
    }

    public void setDescontoServicoPrazo(double descontoServicoPrazo) {
        this.descontoServicoPrazo = descontoServicoPrazo;
    }

    public char getPromocao() {
        return promocao;
    }

    public void setPromocao(char promocao) {
        this.promocao = promocao;
    }
}

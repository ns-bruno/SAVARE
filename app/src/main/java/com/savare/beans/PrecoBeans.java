package com.savare.beans;

/**
 * Created by Bruno on 20/02/2017.
 */

public class PrecoBeans {

    private int idPreco, idProduto, idClifo, idPlanoPagamento;
    private String dataAlteracao;
    private double vendaAtacado, vendaVarejo;

    public int getIdPreco() {
        return idPreco;
    }

    public void setIdPreco(int idPreco) {
        this.idPreco = idPreco;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getIdClifo() {
        return idClifo;
    }

    public void setIdClifo(int idClifo) {
        this.idClifo = idClifo;
    }

    public int getIdPlanoPagamento() {
        return idPlanoPagamento;
    }

    public void setIdPlanoPagamento(int idPlanoPagamento) {
        this.idPlanoPagamento = idPlanoPagamento;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public double getVendaAtacado() {
        return vendaAtacado;
    }

    public void setVendaAtacado(double vendaAtacado) {
        this.vendaAtacado = vendaAtacado;
    }

    public double getVendaVarejo() {
        return vendaVarejo;
    }

    public void setVendaVarejo(double vendaVarejo) {
        this.vendaVarejo = vendaVarejo;
    }
}

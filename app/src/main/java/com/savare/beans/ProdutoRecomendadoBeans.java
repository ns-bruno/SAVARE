package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 17/08/2016.
 */
public class ProdutoRecomendadoBeans {

    private int idPrecoRecomendado, idProduto, idAreas, idCidade, idClifoVendedor, idClifo, idEmpresa, posicao;
    private double quantidadeVendida, valorTotalVenda, valorTotalCusto;

    public int getIdPrecoRecomendado() {
        return idPrecoRecomendado;
    }

    public void setIdPrecoRecomendado(int idPrecoRecomendado) {
        this.idPrecoRecomendado = idPrecoRecomendado;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getIdAreas() {
        return idAreas;
    }

    public void setIdAreas(int idAreas) {
        this.idAreas = idAreas;
    }

    public int getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(int idCidade) {
        this.idCidade = idCidade;
    }

    public int getIdClifoVendedor() {
        return idClifoVendedor;
    }

    public void setIdClifoVendedor(int idClifoVendedor) {
        this.idClifoVendedor = idClifoVendedor;
    }

    public int getIdClifo() {
        return idClifo;
    }

    public void setIdClifo(int idClifo) {
        this.idClifo = idClifo;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public double getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public void setQuantidadeVendida(double quantidadeVendida) {
        this.quantidadeVendida = quantidadeVendida;
    }

    public double getValorTotalVenda() {
        return valorTotalVenda;
    }

    public void setValorTotalVenda(double valorTotalVenda) {
        this.valorTotalVenda = valorTotalVenda;
    }

    public double getValorTotalCusto() {
        return valorTotalCusto;
    }

    public void setValorTotalCusto(double valorTotalCusto) {
        this.valorTotalCusto = valorTotalCusto;
    }
}

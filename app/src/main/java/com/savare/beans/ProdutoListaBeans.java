package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ProdutoListaBeans implements Parcelable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7659710670453241684L;

	private ProdutoBeans produto;
	private int idPLoja;
	private double valorUnitarioAtacado,
				   valorUnitarioVarejo,
			       valorPromocaoAtacadoVista,
			       valorPromocaoAtacadoPrazo,
				   valorPromocaoVarejoVista,
				   valorPromocaoVarejoPrazo,
				   valorTabelaAtacado,
				   valorTabelaVarejo,
				   custoReposicaoN,
				   custoCompleto,
				   estoqueFisico,
				   estoqueContabil;
	private char atacadoVarejo,
				 estaNoOrcamento;
	private boolean produtoNovo = false;
	private String 	produtoPromocaoAtacado,
					produtoPromocaoVarejo,
					produtoPromocaoServico;
	
	public ProdutoListaBeans(Parcel dados) {
		this.estaNoOrcamento = dados.readString().charAt(0);
		this.valorUnitarioAtacado = dados.readDouble();
		this.valorUnitarioVarejo = dados.readDouble();
		this.valorPromocaoAtacadoVista = dados.readDouble();
		this.valorPromocaoVarejoVista = dados.readDouble();
		this.custoReposicaoN = dados.readDouble();
		this.custoCompleto = dados.readDouble();
		this.estoqueFisico = dados.readDouble();
		this.estoqueContabil = dados.readDouble();
		this.atacadoVarejo = dados.readString().charAt(0);
	    this.produto.setCodigoEstrutural(dados.readString());
	    this.produto.setIdProduto(dados.readInt());
	    this.produto.setDescricaoProduto(dados.readString());
	    this.produto.setDescricaoMarca(dados.readString());
	    this.produto.setTipoProduto(dados.readString());
	    List<EmbalagemBeans> listaEmbalagem = new ArrayList<EmbalagemBeans>();
	    dados.readList(listaEmbalagem, EmbalagemBeans.class.getClassLoader());
	    this.produto.setListaEmbalagem(listaEmbalagem);
	    
	}
	

	/**
	 * Construtor Padrao.
	 */
	public ProdutoListaBeans() {
	}

	/**
	 * @return the produto
	 */
	public ProdutoBeans getProduto() {
		return produto;
	}

	/**
	 * @param produto the produto to set
	 */
	public void setProduto(ProdutoBeans produto) {
		this.produto = produto;
	}

	/**
	 * @return the valorUnitario
	 */
	public double getValorUnitarioAtacado() {
		return valorUnitarioAtacado;
	}

	/**
	 * Valor do produto ja com todos os acrecimos e/ou descontos.
	 * 
	 * @param valorUnitarioAtacado the valorUnitario to set
	 */
	public void setValorUnitarioAtacado(double valorUnitarioAtacado) {
		this.valorUnitarioAtacado = valorUnitarioAtacado;
	}

	/**
	 * @return the estoqueFisico
	 */
	public double getEstoqueFisico() {
		return estoqueFisico;
	}

	/**
	 * @param estoqueFisico the estoqueFisico to set
	 */
	public void setEstoqueFisico(double estoqueFisico) {
		this.estoqueFisico = estoqueFisico;
	}

	/**
	 * @return the estoqueContabil
	 */
	public double getEstoqueContabil() {
		return estoqueContabil;
	}

	/**
	 * @param estoqueContabil the estoqueContabil to set
	 */
	public void setEstoqueContabil(double estoqueContabil) {
		this.estoqueContabil = estoqueContabil;
	}

	/**
	 * @return the valorUnitarioVarejo
	 */
	public double getValorUnitarioVarejo() {
		return valorUnitarioVarejo;
	}

	/**
	 * @param valorUnitarioVarejo the valorUnitarioVarejo to set
	 */
	public void setValorUnitarioVarejo(double valorUnitarioVarejo) {
		this.valorUnitarioVarejo = valorUnitarioVarejo;
	}

	/**
	 * @return the custoReposicaoN
	 */
	public double getCustoReposicaoN() {
		return custoReposicaoN;
	}

	/**
	 * @param custoReposicaoN the custoReposicaoN to set
	 */
	public void setCustoReposicaoN(double custoReposicaoN) {
		this.custoReposicaoN = custoReposicaoN;
	}


	/**
	 * @return the custoCompleto
	 */
	public double getCustoCompleto() {
		return custoCompleto;
	}

	/**
	 * @param custoCompleto the custoCompleto to set
	 */
	public void setCustoCompleto(double custoCompleto) {
		this.custoCompleto = custoCompleto;
	}

	public double getValorPromocaoAtacadoVista() {
		return valorPromocaoAtacadoVista;
	}

	public void setValorPromocaoAtacadoVista(double valorPromocaoAtacadoVista) {
		this.valorPromocaoAtacadoVista = valorPromocaoAtacadoVista;
	}

	public double getValorPromocaoAtacadoPrazo() {
		return valorPromocaoAtacadoPrazo;
	}

	public void setValorPromocaoAtacadoPrazo(double valorPromocaoAtacadoPrazo) {
		this.valorPromocaoAtacadoPrazo = valorPromocaoAtacadoPrazo;
	}

	public double getValorPromocaoVarejoVista() {
		return valorPromocaoVarejoVista;
	}

	public void setValorPromocaoVarejoVista(double valorPromocaoVarejoVista) {
		this.valorPromocaoVarejoVista = valorPromocaoVarejoVista;
	}

	public double getValorPromocaoVarejoPrazo() {
		return valorPromocaoVarejoPrazo;
	}

	public void setValorPromocaoVarejoPrazo(double valorPromocaoVarejoPrazo) {
		this.valorPromocaoVarejoPrazo = valorPromocaoVarejoPrazo;
	}

	/**
	 * @return the atacadoVarejo
	 */
	public char getAtacadoVarejo() {
		return atacadoVarejo;
	}

	/**
	 * @param atacadoVarejo the atacadoVarejo to set
	 */
	public void setAtacadoVarejo(char atacadoVarejo) {
		this.atacadoVarejo = atacadoVarejo;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	/**
	 * @return the estaNoOrcamento
	 */
	public char getEstaNoOrcamento() {
		return estaNoOrcamento;
	}


	/**
	 * @param estaNoOrcamento the estaNoOrcamento to set
	 */
	public void setEstaNoOrcamento(char estaNoOrcamento) {
		this.estaNoOrcamento = estaNoOrcamento;
	}


	/**
	 * @return the idPLoja
	 */
	public int getIdPLoja() {
		return idPLoja;
	}


	/**
	 * @param idPLoja the idPLoja to set
	 */
	public void setIdPLoja(int idPLoja) {
		this.idPLoja = idPLoja;
	}


	/**
	 * @return the valorTabelaAtacado
	 */
	public double getValorTabelaAtacado() {
		return valorTabelaAtacado;
	}


	/**
	 * @param valorTabelaAtacado the valorTabelaAtacado to set
	 */
	public void setValorTabelaAtacado(double valorTabelaAtacado) {
		this.valorTabelaAtacado = valorTabelaAtacado;
	}


	/**
	 * @return the valorTabelaVarejo
	 */
	public double getValorTabelaVarejo() {
		return valorTabelaVarejo;
	}


	/**
	 * @param valorTabelaVarejo the valorTabelaVarejo to set
	 */
	public void setValorTabelaVarejo(double valorTabelaVarejo) {
		this.valorTabelaVarejo = valorTabelaVarejo;
	}

	public boolean isProdutoNovo() {
		return produtoNovo;
	}

	public void setProdutoNovo(boolean produtoNovo) {
		this.produtoNovo = produtoNovo;
	}

	public String getProdutoPromocaoAtacado() {
		return produtoPromocaoAtacado;
	}

	public void setProdutoPromocaoAtacado(String produtoPromocaoAtacado) {
		this.produtoPromocaoAtacado = produtoPromocaoAtacado;
	}

	public String getProdutoPromocaoVarejo() {
		return produtoPromocaoVarejo;
	}

	public void setProdutoPromocaoVarejo(String produtoPromocaoVarejo) {
		this.produtoPromocaoVarejo = produtoPromocaoVarejo;
	}

	public String getProdutoPromocaoServico() {
		return produtoPromocaoServico;
	}

	public void setProdutoPromocaoServico(String produtoPromocaoServico) {
		this.produtoPromocaoServico = produtoPromocaoServico;
	}

	/**
	 * Pega todos os dados que deseja ser transportado para
	 * outra activity.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(String.valueOf(estaNoOrcamento));
		dest.writeDouble(valorUnitarioAtacado);
		dest.writeDouble(valorUnitarioVarejo);
		dest.writeDouble(valorPromocaoAtacadoVista);
		dest.writeDouble(valorPromocaoVarejoVista);
		dest.writeDouble(custoReposicaoN);
		dest.writeDouble(custoCompleto);
		dest.writeDouble(estoqueFisico);
		dest.writeDouble(estoqueContabil);
	    dest.writeString(String.valueOf(atacadoVarejo));
	    dest.writeString(produto.getCodigoEstrutural());
	    dest.writeInt(produto.getIdProduto());
	    dest.writeString(produto.getDescricaoProduto());
	    dest.writeString(produto.getDescricaoMarca());
	    dest.writeString(String.valueOf(produto.getTipoProduto()));
	    dest.writeList(produto.getListaEmbalagem());
	    
	}
	
	public static final Parcelable.Creator<ProdutoListaBeans> CREATOR = new Creator<ProdutoListaBeans>() {
		@Override
		public ProdutoListaBeans[] newArray(int size) {
			return new ProdutoListaBeans[size];
		}
		
		@Override
		public ProdutoListaBeans createFromParcel(Parcel source) {
			ProdutoListaBeans produtoVenda = new ProdutoListaBeans();
			produtoVenda.setEstaNoOrcamento(source.readString().charAt(0));
			produtoVenda.setValorUnitarioAtacado(source.readDouble());
			produtoVenda.setValorUnitarioVarejo(source.readDouble());
			produtoVenda.setValorPromocaoAtacadoVista(source.readDouble());
			produtoVenda.setValorPromocaoVarejoVista(source.readDouble());
			produtoVenda.setCustoReposicaoN(source.readDouble());
			produtoVenda.setCustoCompleto(source.readDouble());
			produtoVenda.setEstoqueFisico(source.readDouble());
			produtoVenda.setEstoqueContabil(source.readDouble());
			produtoVenda.setAtacadoVarejo(source.readString().charAt(0));
			
			
			ProdutoBeans p = new ProdutoBeans();
			p.setCodigoEstrutural(source.readString());
			p.setIdProduto(source.readInt());
			p.setDescricaoProduto(source.readString());
			p.setDescricaoMarca(source.readString());
			p.setTipoProduto(source.readString());
			List<EmbalagemBeans> embalagem = new ArrayList<EmbalagemBeans>();
			source.readList(embalagem, EmbalagemBeans.class.getClassLoader());
			// Adiciona a lista de embalagem no produto
			p.setListaEmbalagem(embalagem);
			// Adiciona os detalhes do produto
			produtoVenda.setProduto(p);
			
			return produtoVenda;
		}
	};

	
	
	
	
	
	

}

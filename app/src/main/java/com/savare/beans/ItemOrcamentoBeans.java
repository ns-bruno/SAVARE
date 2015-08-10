package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemOrcamentoBeans implements Parcelable {
	
	private ProdutoBeans produto;
	private PessoaBeans pessoaVendedor;
	private UnidadeVendaBeans unidadeVenda;
	private EstoqueBeans estoqueVenda;
	private OrcamentoBeans orcamento;
	
	private int idOrcamento,
				idItemOrcamento,
				seguencia;
	private double quantidade,
				   valorCusto,
				   valorBruto,
				   ValorDesconto,
				   valorLiquido,
				   valorTabela,
				   valorCustoUnitario,
				   valorBrutoUnitario,
				   valorDescontoUnitario,
				   valorLiquidoUnitario;
	private String guid,
				   dataCadastro,
				   dataAlteracao,
				   complemento,
				   sequencialDesconto;
	private char promocao,
				 tipoProduto;
	
	private boolean tagSelectContext;
	
	public ItemOrcamentoBeans(Parcel dados) {
		this.produto = (ProdutoBeans) dados.readValue(ProdutoBeans.class.getClassLoader());
		this.pessoaVendedor = (PessoaBeans) dados.readValue(PessoaBeans.class.getClassLoader());
		this.unidadeVenda = (UnidadeVendaBeans) dados.readValue(UnidadeVendaBeans.class.getClassLoader());
		this.estoqueVenda = (EstoqueBeans) dados.readValue(EstoqueBeans.class.getClassLoader());
		this.idOrcamento = dados.readInt();
		this.idItemOrcamento = dados.readInt();
		this.seguencia = dados.readInt();
		this.quantidade = dados.readDouble();
		this.valorCusto = dados.readDouble();
		this.valorBruto = dados.readDouble();
		this.ValorDesconto = dados.readDouble();
		this.valorLiquido = dados.readDouble();
		this.valorCustoUnitario = dados.readDouble();
		this.valorBrutoUnitario = dados.readDouble();
		this.valorDescontoUnitario = dados.readDouble();
		this.valorLiquidoUnitario = dados.readDouble();
		this.guid = dados.readString();
		this.complemento = dados.readString();
		this.sequencialDesconto = dados.readString();
		this.promocao = dados.readString().charAt(0);
		this.tipoProduto = dados.readString().charAt(0);
	}
	
	public ItemOrcamentoBeans() {
	}
	
	

	/**
	 * @return the tagSelectContext
	 */
	public boolean isTagSelectContext() {
		return tagSelectContext;
	}

	/**
	 * @param tagSelectContext the tagSelectContext to set
	 */
	public void setTagSelectContext(boolean tagSelectContext) {
		this.tagSelectContext = tagSelectContext;
	}

	/**
	 * @return the orcamento
	 */
	public OrcamentoBeans getOrcamento() {
		return orcamento;
	}

	/**
	 * @param orcamento the orcamento to set
	 */
	public void setOrcamento(OrcamentoBeans orcamento) {
		this.orcamento = orcamento;
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
	 * @return the idOrcamento
	 */
	public int getIdOrcamento() {
		return idOrcamento;
	}

	/**
	 * @param idOrcamento the idOrcamento to set
	 */
	public void setIdOrcamento(int idOrcamento) {
		this.idOrcamento = idOrcamento;
	}

	/**
	 * @return the pessoaVendedor
	 */
	public PessoaBeans getPessoaVendedor() {
		return pessoaVendedor;
	}

	/**
	 * @param pessoaVendedor the pessoaVendedor to set
	 */
	public void setPessoaVendedor(PessoaBeans pessoaVendedor) {
		this.pessoaVendedor = pessoaVendedor;
	}

	/**
	 * @return the seguencia
	 */
	public int getSeguencia() {
		return seguencia;
	}

	/**
	 * @param seguencia the seguencia to set
	 */
	public void setSeguencia(int seguencia) {
		this.seguencia = seguencia;
	}

	/**
	 * @return the quantidade
	 */
	public double getQuantidade() {
		return quantidade;
	}

	/**
	 * @param quantidade the quantidade to set
	 */
	public void setQuantidade(double quantidade) {
		this.quantidade = quantidade;
	}

	/**
	 * @return the valorCusto
	 */
	public double getValorCusto() {
		return valorCusto;
	}

	/**
	 * @param valorCusto the valorCusto to set
	 */
	public void setValorCusto(double valorCusto) {
		this.valorCusto = valorCusto;
	}

	/**
	 * @return the valorBruto
	 */
	public double getValorBruto() {
		return valorBruto;
	}

	/**
	 * @param valorBruto the valorBruto to set
	 */
	public void setValorBruto(double valorBruto) {
		this.valorBruto = valorBruto;
	}

	/**
	 * @return the valorDesconto
	 */
	public double getValorDesconto() {
		return ValorDesconto;
	}

	/**
	 * @param valorDesconto the valorDesconto to set
	 */
	public void setValorDesconto(double valorDesconto) {
		ValorDesconto = valorDesconto;
	}

	/**
	 * @return the valorCustoUnitario
	 */
	public double getValorCustoUnitario() {
		return valorCustoUnitario;
	}

	/**
	 * @param valorCustoUnitario the valorCustoUnitario to set
	 */
	public void setValorCustoUnitario(double valorCustoUnitario) {
		this.valorCustoUnitario = valorCustoUnitario;
	}

	/**
	 * @return the valorBrutoUnitario
	 */
	public double getValorBrutoUnitario() {
		return valorBrutoUnitario;
	}

	/**
	 * @param valorBrutoUnitario the valorBrutoUnitario to set
	 */
	public void setValorBrutoUnitario(double valorBrutoUnitario) {
		this.valorBrutoUnitario = valorBrutoUnitario;
	}

	/**
	 * @return the valorDescontoUnitario
	 */
	public double getValorDescontoUnitario() {
		return valorDescontoUnitario;
	}

	/**
	 * @param valorDescontoUnitario the valorDescontoUnitario to set
	 */
	public void setValorDescontoUnitario(double valorDescontoUnitario) {
		this.valorDescontoUnitario = valorDescontoUnitario;
	}

	/**
	 * @return the valorLiquido
	 */
	public double getValorLiquido() {
		return valorLiquido;
	}

	/**
	 * @param valorLiquido the valorLiquido to set
	 */
	public void setValorLiquido(double valorLiquido) {
		this.valorLiquido = valorLiquido;
	}

	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @return the dataCadastro
	 */
	public String getDataCadastro() {
		return dataCadastro;
	}

	/**
	 * @param dataCadastro the dataCadastro to set
	 */
	public void setDataCadastro(String dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	/**
	 * @return the dataAlteracao
	 */
	public String getDataAlteracao() {
		return dataAlteracao;
	}

	/**
	 * @param dataAlteracao the dataAlteracao to set
	 */
	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	/**
	 * @return the complemento
	 */
	public String getComplemento() {
		return complemento;
	}

	/**
	 * @param complemento the complemento to set
	 */
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	/**
	 * @return the sequencialDesconto
	 */
	public String getSequencialDesconto() {
		return sequencialDesconto;
	}

	/**
	 * @param sequencialDesconto the sequencialDesconto to set
	 */
	public void setSequencialDesconto(String sequencialDesconto) {
		this.sequencialDesconto = sequencialDesconto;
	}

	/**
	 * @return the promocao
	 */
	public char getPromocao() {
		return promocao;
	}

	/**
	 * @param promocao the promocao to set
	 */
	public void setPromocao(char promocao) {
		this.promocao = promocao;
	}

	/**
	 * @return the tipoProduto
	 */
	public char getTipoProduto() {
		return tipoProduto;
	}

	/**
	 * @param tipoProduto the tipoProduto to set
	 */
	public void setTipoProduto(char tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	/**
	 * @return the valorLiquidoUnitario
	 */
	public double getValorLiquidoUnitario() {
		return valorLiquidoUnitario;
	}

	/**
	 * @param valorLiquidoUnitario the valorLiquidoUnitario to set
	 */
	public void setValorLiquidoUnitario(double valorLiquidoUnitario) {
		this.valorLiquidoUnitario = valorLiquidoUnitario;
	}

	/**
	 * @return the unidadeVenda
	 */
	public UnidadeVendaBeans getUnidadeVenda() {
		return unidadeVenda;
	}

	/**
	 * @param unidadeVenda the unidadeVenda to set
	 */
	public void setUnidadeVenda(UnidadeVendaBeans unidadeVenda) {
		this.unidadeVenda = unidadeVenda;
	}

	/**
	 * @return the estoqueVenda
	 */
	public EstoqueBeans getEstoqueVenda() {
		return estoqueVenda;
	}

	/**
	 * @param estoqueVenda the estoqueVenda to set
	 */
	public void setEstoqueVenda(EstoqueBeans estoqueVenda) {
		this.estoqueVenda = estoqueVenda;
	}

	public double getValorTabela() {
		return valorTabela;
	}

	public void setValorTabela(double valorTabela) {
		this.valorTabela = valorTabela;
	}

	/**
	 * @return the idItemOrcamento
	 */
	public int getIdItemOrcamento() {
		return idItemOrcamento;
	}

	/**
	 * @param idItemOrcamento the idItemOrcamento to set
	 */
	public void setIdItemOrcamento(int idItemOrcamento) {
		this.idItemOrcamento = idItemOrcamento;
	}
	

	@Override
	public int describeContents() {
		return 0;
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeValue(this.produto);
		dest.writeValue(this.pessoaVendedor);
		dest.writeValue(this.unidadeVenda);
		dest.writeValue(this.estoqueVenda);
		dest.writeInt(this.idOrcamento);
		dest.writeInt(this.idItemOrcamento);
		dest.writeInt(this.seguencia);
		dest.writeDouble(this.quantidade);
		dest.writeDouble(this.valorCusto);
		dest.writeDouble(this.valorBruto);
		dest.writeDouble(this.ValorDesconto);
		dest.writeDouble(this.valorLiquido);
		dest.writeDouble(this.valorCustoUnitario);
		dest.writeDouble(this.valorBrutoUnitario);
		dest.writeDouble(this.valorDescontoUnitario);
		dest.writeDouble(this.valorLiquidoUnitario);
		dest.writeString(this.guid);
		dest.writeString(this.complemento);
		dest.writeString(this.sequencialDesconto);
		dest.writeString(String.valueOf(this.promocao));
		dest.writeString(String.valueOf(this.tipoProduto));
		
	}
	
	public static final Parcelable.Creator<ItemOrcamentoBeans> CREATOR = new Creator<ItemOrcamentoBeans>() {

		@Override
		public ItemOrcamentoBeans createFromParcel(Parcel source) {
			return new ItemOrcamentoBeans(source);
		}

		@Override
		public ItemOrcamentoBeans[] newArray(int size) {
			return new ItemOrcamentoBeans[size];
		}
		
	};
	
	
	

}

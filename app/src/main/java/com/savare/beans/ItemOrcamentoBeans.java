package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

public class ItemOrcamentoBeans implements Parcelable, KvmSerializable {
	
	private ProdutoBeans produto;
	private PessoaBeans pessoaVendedor;
	private UnidadeVendaBeans unidadeVenda;
	private EstoqueBeans estoqueVenda;
	private OrcamentoBeans orcamento;
	private PlanoPagamentoBeans planoPagamento;
	
	private int idOrcamento,
				idItemOrcamento,
				idVendedorTemp,
				idUnidadeTemp,
				idEstoqueTemp,
				idPlanoPagamentoTemp,
				sequencia;
	private double quantidade,
				   quantidadeFaturada,
				   valorCusto,
				   valorBruto,
                   valorDesconto,
				   valorLiquido,
				   valorLiquidoFaturado,
				   valorTabela,
				   valorTabelaFaturado,
				   valorTabelaFaturadoUnitario,
				   valorTabelaUnitario,
				   valorCustoUnitario,
				   valorBrutoUnitario,
				   valorDescontoUnitario,
				   pesoBruto,
				   pesoLiquido,
				   valorLiquidoUnitario;
	private String guid,
				   guidOrcamento,
				   dataCadastro,
				   dataAlteracao,
				   complemento,
				   sequencialDesconto,
				   statusRetorno;
	private String promocao,
				   tipoProduto;
	
	private boolean tagSelectContext,
					tagEnviado;
	
	public ItemOrcamentoBeans(Parcel dados) {
		this.produto = (ProdutoBeans) dados.readValue(ProdutoBeans.class.getClassLoader());
		this.pessoaVendedor = (PessoaBeans) dados.readValue(PessoaBeans.class.getClassLoader());
		this.unidadeVenda = (UnidadeVendaBeans) dados.readValue(UnidadeVendaBeans.class.getClassLoader());
		this.estoqueVenda = (EstoqueBeans) dados.readValue(EstoqueBeans.class.getClassLoader());
		this.idOrcamento = dados.readInt();
		this.idItemOrcamento = dados.readInt();
		this.sequencia = dados.readInt();
		this.quantidade = dados.readDouble();
		this.valorCusto = dados.readDouble();
		this.valorBruto = dados.readDouble();
		this.valorDesconto = dados.readDouble();
		this.valorLiquido = dados.readDouble();
		this.valorCustoUnitario = dados.readDouble();
		this.valorBrutoUnitario = dados.readDouble();
		this.valorDescontoUnitario = dados.readDouble();
		this.valorLiquidoUnitario = dados.readDouble();
		this.guid = dados.readString();
		this.complemento = dados.readString();
		this.sequencialDesconto = dados.readString();
		this.promocao = dados.readString();
		this.tipoProduto = dados.readString();
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

	public boolean isTagEnviado() {
		return tagEnviado;
	}

	public void setTagEnviado(boolean tagEnviado) {
		this.tagEnviado = tagEnviado;
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
	 * @return the sequencia
	 */
	public int getSequencia() {
		return sequencia;
	}

	/**
	 * @param sequencia the sequencia to set
	 */
	public void setSequencia(int sequencia) {
		this.sequencia = sequencia;
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
		return valorDesconto;
	}

	/**
	 * @param valorDesconto the valorDesconto to set
	 */
	public void setValorDesconto(double valorDesconto) {
		this.valorDesconto = valorDesconto;
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

	public String getGuidOrcamento() {
		return guidOrcamento;
	}

	public void setGuidOrcamento(String guidOrcamento) {
		this.guidOrcamento = guidOrcamento;
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
	public String getPromocao() {
		return promocao;
	}

	/**
	 * @param promocao the promocao to set
	 */
	public void setPromocao(String promocao) {
		this.promocao = promocao;
	}

	/**
	 * @return the tipoProduto
	 */
	public String getTipoProduto() {
		return tipoProduto;
	}

	/**
	 * @param tipoProduto the tipoProduto to set
	 */
	public void setTipoProduto(String tipoProduto) {
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

	public PlanoPagamentoBeans getPlanoPagamento() {
		return planoPagamento;
	}

	public void setPlanoPagamento(PlanoPagamentoBeans planoPagamento) {
		this.planoPagamento = planoPagamento;
	}

	public double getQuantidadeFaturada() {
		return quantidadeFaturada;
	}

	public void setQuantidadeFaturada(double quantidadeFaturada) {
		this.quantidadeFaturada = quantidadeFaturada;
	}

	public double getValorTabelaFaturado() {
		return valorTabelaFaturado;
	}

	public void setValorTabelaFaturado(double valorTabelaFaturado) {
		this.valorTabelaFaturado = valorTabelaFaturado;
	}

	public double getValorLiquidoFaturado() {
		return valorLiquidoFaturado;
	}

	public void setValorLiquidoFaturado(double valorLiquidoFaturado) {
		this.valorLiquidoFaturado = valorLiquidoFaturado;
	}

	public String getStatusRetorno() {
		return statusRetorno;
	}

	public void setStatusRetorno(String statusRetorno) {
		this.statusRetorno = statusRetorno;
	}

	public double getValorTabelaFaturadoUnitario() {
		return valorTabelaFaturadoUnitario;
	}

	public void setValorTabelaFaturadoUnitario(double valorTabelaFaturadoUnitario) {
		this.valorTabelaFaturadoUnitario = valorTabelaFaturadoUnitario;
	}

	public double getValorTabelaUnitario() {
		return valorTabelaUnitario;
	}

	public void setValorTabelaUnitario(double valorTabelaUnitario) {
		this.valorTabelaUnitario = valorTabelaUnitario;
	}

	public double getPesoBruto() {
		return pesoBruto;
	}

	public void setPesoBruto(double pesoBruto) {
		this.pesoBruto = pesoBruto;
	}

	public double getPesoLiquido() {
		return pesoLiquido;
	}

	public void setPesoLiquido(double pesoLiquido) {
		this.pesoLiquido = pesoLiquido;
	}

    public int getIdVendedorTemp() {
        return idVendedorTemp;
    }

    public void setIdVendedorTemp(int idVendedorTemp) {
        this.idVendedorTemp = idVendedorTemp;
    }

    public int getIdUnidadeTemp() {
        return idUnidadeTemp;
    }

    public void setIdUnidadeTemp(int idUnidadeTemp) {
        this.idUnidadeTemp = idUnidadeTemp;
    }

    public int getIdEstoqueTemp() {
        return idEstoqueTemp;
    }

    public void setIdEstoqueTemp(int idEstoqueTemp) {
        this.idEstoqueTemp = idEstoqueTemp;
    }

    public int getIdPlanoPagamentoTemp() {
        return idPlanoPagamentoTemp;
    }

    public void setIdPlanoPagamentoTemp(int idPlanoPagamentoTemp) {
        this.idPlanoPagamentoTemp = idPlanoPagamentoTemp;
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
		dest.writeInt(this.sequencia);
		dest.writeDouble(this.quantidade);
		dest.writeDouble(this.valorCusto);
		dest.writeDouble(this.valorBruto);
		dest.writeDouble(this.valorDesconto);
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


	@Override
	public Object getProperty(int i) {

		switch (i){
            case 0:
                return guid;
            case 1:
                return idOrcamento;
            case 2:
                return idEstoqueTemp;
            case 3:
                return idPlanoPagamentoTemp;
            case 4:
                return idUnidadeTemp;
            case 5:
                return idVendedorTemp;
            case 6:
                return sequencia;
            case 7:
                return ""+quantidade;
            case 8:
                return ""+valorCusto;
            case 9:
                return ""+valorBruto;
            case 10:
                return ""+valorDesconto;
            case 11:
                return promocao;
            case 12:
                return tipoProduto;
            case 13:
                return complemento;
            case 14:
                return guidOrcamento;

			case 15:
				return ""+valorTabela;
        }
		return null;
	}

	@Override
	public int getPropertyCount() {
		return 16;
	}

	@Override
	public void setProperty(int i, Object o) {
        switch (i){
            case 0:
                this.guid = o.toString();
                break;

            case 1:
                this.idOrcamento = Integer.parseInt(o.toString());
                break;

            case 2:
                this.idEstoqueTemp = Integer.parseInt(o.toString());
                break;

            case 3:
                this.idPlanoPagamentoTemp = Integer.parseInt(o.toString());
                break;

            case 4:
                this.idUnidadeTemp = Integer.parseInt(o.toString());
                break;

            case 5:
                this.idVendedorTemp = Integer.parseInt(o.toString());
                break;

            case 6:
                this.sequencia = Integer.parseInt(o.toString());
                break;

            case 7:
                this.quantidade = Double.parseDouble(o.toString());
                break;

            case 8:
                this.valorCusto = Double.parseDouble(o.toString());
                break;

            case 9:
                this.valorBruto = Double.parseDouble(o.toString());
                break;

            case 10:
                this.valorDesconto = Double.parseDouble(o.toString());
                break;

            case 11:
                this.promocao = o.toString();
                break;

            case 12:
                this.tipoProduto = o.toString();
                break;

            case 13:
                this.complemento = o.toString();
                break;

            case 14:
                this.guidOrcamento = o.toString();
                break;

			case 15:
				this.valorTabela = Double.parseDouble(o.toString());
				break;
        }
	}

	@Override
	public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i){
            case 0:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "guid";
                break;

            case 1:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idOrcamento";
                break;

            case 2:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idEstoqueTemp";
                break;

            case 3:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idPlanoPagamentoTemp";
                break;

            case 4:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idUnidadeTemp";
                break;

            case 5:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idVendedorTemp";
                break;

            case 6:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "sequencia";
                break;

            case 7:
                propertyInfo.type = Double.class;
                propertyInfo.name = "quantidade";
                break;

            case 8:
                propertyInfo.type = Double.class;
                propertyInfo.name = "valorCusto";
                break;

            case 9:
                propertyInfo.type = Double.class;
                propertyInfo.name = "valorBruto";
                break;

            case 10:
                propertyInfo.type = Double.class;
                propertyInfo.name = "valorDesconto";
                break;

            case 11:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "promocao";
                break;

            case 12:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "tipoProduto";
                break;

            case 13:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "complemento";
                break;

            case 14:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "guidOrcamento";
                break;

			case 15:
				propertyInfo.type = Double.class;
				propertyInfo.name = "valorTabela";
				break;
        }
	}
}

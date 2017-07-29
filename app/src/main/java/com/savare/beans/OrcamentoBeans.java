package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;
import java.util.List;

public class OrcamentoBeans implements Parcelable, KvmSerializable {
	
	private int idOrcamento,
				idEmpresa,
				idPessoa,
				idPessoaVendedor,
				idEstado,
				idCidade,
				idRomaneio,
				idTipoDocumento,
				numero;
	private String nomeRazao,
				   siglaEstado,
				   cidade,
				   dataCadastro,
				   dataAlteracao,
				   observacao,
				   status,
				   statusRetorno,
				   guid,
				   pessoaCliente,
				   cpfCnpj,
				   rgIe,
				   enderecoCliente,
				   bairroCliente,
				   cepCliente,
				   tipoEntrega,
				   horarioLocalizacao,
				   tipoLocalizacao;
	
	private String tipoVenda;
	
	private double totalOrcamento,
				   totalOrcamentoFaturado,
				   totalOrcamentoBruto,
				   totalOrcamentoCusto,
				   totalTabela,
				   totalTabelaFaturado,
				   totalDesconto,
				   totalFrete,
				   totalSeguro,
				   totalOutros,
				   totalEncargosFinanceiros,
				   latitude,
				   pesoLiquido,
				   pesoBruto,
				   longitude,
				   altitude,
				   precisaoLocalizacao;
	
	private boolean tagSelectContext,
					tagEnviado;

	private List<ItemOrcamentoBeans> listaItemOrcamento;

	public OrcamentoBeans(Parcel dados) {
		this.idOrcamento = dados.readInt();
		this.idEmpresa = dados.readInt();
		this.idPessoa = dados.readInt();
		this.idPessoaVendedor = dados.readInt();
		this.nomeRazao = dados.readString();
		this.tipoVenda = dados.readString();
	}
	
	public OrcamentoBeans() {
		
	}
	
	
	public boolean isTagSelectContext() {
		return tagSelectContext;
	}

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
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the observacao
	 */
	public String getObservacao() {
		return observacao;
	}

	/**
	 * @param observacao the observacao to set
	 */
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	/**
	 * Pega se o orçamento/pedido eh para o atacado ou varejo
	 * @return
	 */
	public String getTipoVenda() {
		return tipoVenda;
	}

	public void setTipoVenda(String tipoVenda) {
		this.tipoVenda = tipoVenda;
	}

	/**
	 * @return the nomeRazao
	 */
	public String getNomeRazao() {
		return nomeRazao;
	}

	/**
	 * @param nomeRazao the nomeRazao to set
	 */
	public void setNomeRazao(String nomeRazao) {
		this.nomeRazao = nomeRazao;
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
	 * @return the idEmpresa
	 */
	public int getIdEmpresa() {
		return idEmpresa;
	}

	/**
	 * @param idEmpresa the idEmpresa to set
	 */
	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	/**
	 * @return the idPessoa
	 */
	public int getIdPessoa() {
		return idPessoa;
	}

	/**
	 * @param idPessoa the idPessoa to set
	 */
	public void setIdPessoa(int idPessoa) {
		this.idPessoa = idPessoa;
	}

	/**
	 * @return the idPessoaVendedor
	 */
	public int getIdPessoaVendedor() {
		return idPessoaVendedor;
	}

	/**
	 * @param idPessoaVendedor the idPessoaVendedor to set
	 */
	public void setIdPessoaVendedor(int idPessoaVendedor) {
		this.idPessoaVendedor = idPessoaVendedor;
	}
	

	/**
	 * @return the totalOrcamento
	 */
	public double getTotalOrcamento() {
		return totalOrcamento;
	}

	/**
	 * @param totalOrcamento the totalOrcamento to set
	 */
	public void setTotalOrcamento(double totalOrcamento) {
		this.totalOrcamento = totalOrcamento;
	}

	/**
	 * @return the estado
	 */
	public String getSiglaEstado() {
		return siglaEstado;
	}

	/**
	 * @param estado the estado to set
	 */
	public void setSiglaEstado(String estado) {
		this.siglaEstado = estado;
	}

	/**
	 * @return the cidade
	 */
	public String getCidade() {
		return cidade;
	}

	/**
	 * @param cidade the cidade to set
	 */
	public void setCidade(String cidade) {
		this.cidade = cidade;
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

	public String getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	/**
	 * @return the totalOrcamentoBruto
	 */
	public double getTotalOrcamentoBruto() {
		return totalOrcamentoBruto;
	}

	/**
	 * @param totalOrcamentoBruto the totalOrcamentoBruto to set
	 */
	public void setTotalOrcamentoBruto(double totalOrcamentoBruto) {
		this.totalOrcamentoBruto = totalOrcamentoBruto;
	}

	public double getTotalTabela() {
		return totalTabela;
	}

	public void setTotalTabela(double totalTabela) {
		this.totalTabela = totalTabela;
	}

	public double getTotalOrcamentoFaturado() {
		return totalOrcamentoFaturado;
	}

	public void setTotalOrcamentoFaturado(double totalOrcamentoFaturado) {
		this.totalOrcamentoFaturado = totalOrcamentoFaturado;
	}

	public double getTotalTabelaFaturado() {
		return totalTabelaFaturado;
	}

	public void setTotalTabelaFaturado(double totalTabelaFaturado) {
		this.totalTabelaFaturado = totalTabelaFaturado;
	}

	public double getTotalDesconto() {
		return totalDesconto;
	}

	public void setTotalDesconto(double totalDesconto) {
		this.totalDesconto = totalDesconto;
	}

	public double getTotalFrete() {
		return totalFrete;
	}

	public void setTotalFrete(double totalFrete) {
		this.totalFrete = totalFrete;
	}

	public double getTotalSeguro() {
		return totalSeguro;
	}

	public void setTotalSeguro(double totalSeguro) {
		this.totalSeguro = totalSeguro;
	}

	public double getTotalOutros() {
		return totalOutros;
	}

	public void setTotalOutros(double totalOutros) {
		this.totalOutros = totalOutros;
	}

	public double getTotalEncargosFinanceiros() {
		return totalEncargosFinanceiros;
	}

	public void setTotalEncargosFinanceiros(double totalEncargosFinanceiros) {
		this.totalEncargosFinanceiros = totalEncargosFinanceiros;
	}

	/**
	 * @return the statusRetorno
	 */
	public String getStatusRetorno() {
		return statusRetorno;
	}

	public int getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(int idEstado) {
		this.idEstado = idEstado;
	}

	public int getIdCidade() {
		return idCidade;
	}

	public void setIdCidade(int idCidade) {
		this.idCidade = idCidade;
	}

	public int getIdTipoDocumento() {
		return idTipoDocumento;
	}

	public void setIdTipoDocumento(int idTipoDocumento) {
		this.idTipoDocumento = idTipoDocumento;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getPessoaCliente() {
		return pessoaCliente;
	}

	public void setPessoaCliente(String pessoaCliente) {
		this.pessoaCliente = pessoaCliente;
	}

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public String getRgIe() {
		return rgIe;
	}

	public void setRgIe(String rgIe) {
		this.rgIe = rgIe;
	}

	public String getEnderecoCliente() {
		return enderecoCliente;
	}

	public void setEnderecoCliente(String enderecoCliente) {
		this.enderecoCliente = enderecoCliente;
	}

	public String getBairroCliente() {
		return bairroCliente;
	}

	public void setBairroCliente(String bairroCliente) {
		this.bairroCliente = bairroCliente;
	}

	public String getCepCliente() {
		return cepCliente;
	}

	public void setCepCliente(String cepCliente) {
		this.cepCliente = cepCliente;
	}

	public String getTipoEntrega() {
		return tipoEntrega;
	}

	public void setTipoEntrega(String tipoEntrega) {
		this.tipoEntrega = tipoEntrega;
	}

	public String getHorarioLocalizacao() {
		return horarioLocalizacao;
	}

	public void setHorarioLocalizacao(String horarioLocalizacao) {
		this.horarioLocalizacao = horarioLocalizacao;
	}

	public String getTipoLocalizacao() {
		return tipoLocalizacao;
	}

	public void setTipoLocalizacao(String tipoLocalizacao) {
		this.tipoLocalizacao = tipoLocalizacao;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getPrecisaoLocalizacao() {
		return precisaoLocalizacao;
	}

	public void setPrecisaoLocalizacao(double precisaoLocalizacao) {
		this.precisaoLocalizacao = precisaoLocalizacao;
	}

	public int getIdRomaneio() {
		return idRomaneio;
	}

	public void setIdRomaneio(int idRomaneio) {
		this.idRomaneio = idRomaneio;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public double getTotalOrcamentoCusto() {
		return totalOrcamentoCusto;
	}

	public void setTotalOrcamentoCusto(double totalOrcamentoCusto) {
		this.totalOrcamentoCusto = totalOrcamentoCusto;
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

	public List<ItemOrcamentoBeans> getListaItemOrcamento() {
		return listaItemOrcamento;
	}

	public void setListaItemOrcamento(List<ItemOrcamentoBeans> listaItemOrcamento) {
		this.listaItemOrcamento = listaItemOrcamento;
	}

	/**
	 * @param statusRetorno the statusRetorno to set
	 */
	public void setStatusRetorno(String statusRetorno) {
		this.statusRetorno = statusRetorno;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.idOrcamento);
		dest.writeInt(this.idEmpresa);
		dest.writeInt(this.idPessoa);
		dest.writeInt(this.idPessoaVendedor);
		dest.writeString(this.nomeRazao);
		dest.writeString(String.valueOf(this.tipoVenda));
	}
	
	public static final Parcelable.Creator<OrcamentoBeans> CREATOR = new Creator<OrcamentoBeans>() {

		@Override
		public OrcamentoBeans createFromParcel(Parcel source) {
			return new OrcamentoBeans(source);
		}

		@Override
		public OrcamentoBeans[] newArray(int size) {
			return new OrcamentoBeans[size];
		}
		
	};

	@Override
	public Object getProperty(int i) {

        switch (i){
            case 0:
                return this.guid;
            case 1:
                return this.idEmpresa;
            case 2:
                return this.idPessoa;
            case 3:
                return this.idEstado;
            case 4:
                return this.idCidade;
            case 5:
                return this.idTipoDocumento;
            case 6:
                return ""+this.totalOrcamentoCusto;
            case 7:
                return ""+this.totalTabela;
            case 8:
                return ""+this.totalDesconto;
            case 9:
                return ""+this.totalFrete;
            case 10:
                return ""+this.totalSeguro;
            case 11:
                return ""+this.totalOutros;
            case 12:
                return this.tipoVenda;
            case 13:
                return this.pessoaCliente;
            case 14:
                return this.nomeRazao;
            case 15:
                return this.rgIe;
            case 16:
                return this.cpfCnpj;
            case 17:
                return this.enderecoCliente;
            case 18:
                return this.bairroCliente;
            case 19:
                return this.cepCliente;
            case 20:
                return this.observacao;
            case 21:
                return this.dataCadastro;
        }
		return null;
	}

	@Override
	public int getPropertyCount() {
		return 22;
	}

	@Override
	public void setProperty(int i, Object o) {
        switch (i){
            case 0:
                this.guid = o.toString();
                break;

            case 1:
                this.idEmpresa = Integer.parseInt(o.toString());
                break;

            case 2:
                this.idPessoa = Integer.parseInt(o.toString());
                break;

            case 3:
                this.idEstado = Integer.parseInt(o.toString());
                break;

            case 4:
                this.idCidade = Integer.parseInt(o.toString());
                break;

            case 5:
                this.idTipoDocumento = Integer.parseInt(o.toString());
                break;

            case 6:
                this.totalOrcamentoCusto = Double.parseDouble(o.toString());
                break;

            case 7:
                this.totalOrcamentoBruto = Double.parseDouble(o.toString());
                break;

            case 8:
                this.totalDesconto = Double.parseDouble(o.toString());
                break;

            case 9:
                this.totalFrete = Double.parseDouble(o.toString());
                break;

            case 10:
                this.totalSeguro = Double.parseDouble(o.toString());
                break;

            case 11:
                this.totalOutros = Double.parseDouble(o.toString());
                break;

            case 12:
                this.tipoVenda = o.toString();
                break;

            case 13:
                this.pessoaCliente = o.toString();
                break;

            case 14:
                this.nomeRazao = o.toString();
                break;

            case 15:
                this.rgIe = o.toString();
                break;

            case 16:
                this.cpfCnpj = o.toString();
                break;

            case 17:
                this.enderecoCliente = o.toString();
                break;

            case 18:
                this.bairroCliente = o.toString();
                break;

            case 19:
                this.cepCliente = o.toString();
                break;

            case 20:
                this.observacao = o.toString();
                break;

            case 21:
                this.dataCadastro = o.toString();
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
                propertyInfo.name = "idEmpresa";
                break;

            case 2:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idPessoa";
                break;

            case 3:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idEstado";
                break;

            case 4:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idCidade";
                break;

            case 5:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idTipoDocumento";
                break;

            case 6:
                propertyInfo.type = Double.class;
                propertyInfo.name = "totalOrcamentoCusto";
                break;

            case 7:
                propertyInfo.type = Double.class;
                propertyInfo.name = "totalOrcamentoBruto";
                break;

            case 8:
                propertyInfo.type = Double.class;
                propertyInfo.name = "totalDesconto";
                break;

            case 9:
                propertyInfo.type = Double.class;
                propertyInfo.name = "totalFrete";
                break;

            case 10:
                propertyInfo.type = Double.class;
                propertyInfo.name = "totalSeguro";
                break;

            case 11:
                propertyInfo.type = Double.class;
                propertyInfo.name = "totalOutros";
                break;

            case 12:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "tipoVenda";
                break;

            case 13:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "pessoaCliente";
                break;

            case 14:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "nomeRazao";
                break;

            case 15:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "rgIe";
                break;

            case 16:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "cpfCnpj";
                break;

            case 17:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "enderecoCliente";
                break;

            case 18:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "bairroCliente";
                break;

            case 19:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "cepCliente";
                break;

            case 20:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "observacao";
                break;

            case 21:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "dataCadastro";
                break;
        }
	}
}

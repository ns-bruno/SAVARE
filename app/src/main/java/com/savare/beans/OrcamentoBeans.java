package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OrcamentoBeans implements Parcelable {
	
	private int idOrcamento,
				idEmpresa,
				idPessoa,
				idPessoaVendedor;
	private String nomeRazao,
				   siglaEstado,
				   cidade,
				   dataCadastro,
				   observacao,
				   status,
				   statusRetorno;
	
	private char tipoVenda;
	
	private double totalOrcamento,
				   totalOrcamentoFaturado,
				   totalOrcamentoBruto,
				   totalTabela,
				   totalTabelaFaturado;
	
	private boolean tagSelectContext;

	public OrcamentoBeans(Parcel dados) {
		this.idOrcamento = dados.readInt();
		this.idEmpresa = dados.readInt();
		this.idPessoa = dados.readInt();
		this.idPessoaVendedor = dados.readInt();
		this.nomeRazao = dados.readString();
		this.tipoVenda = dados.readString().charAt(0);
	}
	
	public OrcamentoBeans() {
		
	}
	
	
	public boolean isTagSelectContext() {
		return tagSelectContext;
	}

	public void setTagSelectContext(boolean tagSelectContext) {
		this.tagSelectContext = tagSelectContext;
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

	public char getTipoVenda() {
		return tipoVenda;
	}

	public void setTipoVenda(char tipoVenda) {
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

	/**
	 * @return the statusRetorno
	 */
	public String getStatusRetorno() {
		return statusRetorno;
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

}

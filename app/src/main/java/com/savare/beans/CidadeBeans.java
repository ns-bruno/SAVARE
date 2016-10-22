package com.savare.beans;

public class CidadeBeans {
	
	private int idCidade,
				idEstado,
				ddd,
				codigoIbge;
	
	private String descricao,
				   cep,
				   dataAlteracao;

	private EstadoBeans estado;

	/**
	 * @return the idCidade
	 */
	public int getIdCidade() {
		return idCidade;
	}

	/**
	 * @param idCidade the idCidade to set
	 */
	public void setIdCidade(int idCidade) {
		this.idCidade = idCidade;
	}

	public EstadoBeans getEstado() {
		return estado;
	}

	public void setEstado(EstadoBeans estado) {
		this.estado = estado;
	}

	/**
	 * @return the idEstado
	 */
	public int getIdEstado() {
		return idEstado;
	}

	/**
	 * @param idEstado the idEstado to set
	 */
	public void setIdEstado(int idEstado) {
		this.idEstado = idEstado;
	}

	/**
	 * @return the ddd
	 */
	public int getDdd() {
		return ddd;
	}

	/**
	 * @param ddd the ddd to set
	 */
	public void setDdd(int ddd) {
		this.ddd = ddd;
	}

	/**
	 * @return the codigoIbge
	 */
	public int getCodigoIbge() {
		return codigoIbge;
	}

	/**
	 * @param codigoIbge the codigoIbge to set
	 */
	public void setCodigoIbge(int codigoIbge) {
		this.codigoIbge = codigoIbge;
	}

	/**
	 * @return the descricao
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param descricao the descricao to set
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * @return the cep
	 */
	public String getCep() {
		return cep;
	}

	/**
	 * @param cep the cep to set
	 */
	public void setCep(String cep) {
		this.cep = cep;
	}

	public String getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
}

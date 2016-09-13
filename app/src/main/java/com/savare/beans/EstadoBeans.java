package com.savare.beans;

public class EstadoBeans {
	
	private int codigoEstado,
				idEstado;
	private String siglaEstado,
				   descricaoEstado,
					dataAlteracao,
					tipoIpiSaida;
	private double icmsSaida, ipiSaida;
	/**
	 * @return the codigoEstado
	 */
	public int getCodigoEstado() {
		return codigoEstado;
	}
	/**
	 * @param codigoEstado the codigoEstado to set
	 */
	public void setCodigoEstado(int codigoEstado) {
		this.codigoEstado = codigoEstado;
	}
	/**
	 * @return the siglaEstado
	 */
	public String getSiglaEstado() {
		return siglaEstado;
	}
	/**
	 * @param siglaEstado the siglaEstado to set
	 */
	public void setSiglaEstado(String siglaEstado) {
		this.siglaEstado = siglaEstado;
	}
	/**
	 * @return the descricaoEstado
	 */
	public String getDescricaoEstado() {
		return descricaoEstado;
	}
	/**
	 * @param descricaoEstado the descricaoEstado to set
	 */
	public void setDescricaoEstado(String descricaoEstado) {
		this.descricaoEstado = descricaoEstado;
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

	public String getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	public String getTipoIpiSaida() {
		return tipoIpiSaida;
	}

	public void setTipoIpiSaida(String tipoIpiSaida) {
		this.tipoIpiSaida = tipoIpiSaida;
	}

	public double getIcmsSaida() {
		return icmsSaida;
	}

	public void setIcmsSaida(double icmsSaida) {
		this.icmsSaida = icmsSaida;
	}

	public double getIpiSaida() {
		return ipiSaida;
	}

	public void setIpiSaida(double ipiSaida) {
		this.ipiSaida = ipiSaida;
	}
}

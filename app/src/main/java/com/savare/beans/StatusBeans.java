package com.savare.beans;

public class StatusBeans {
	
	private int idStatus,
				codigo;
	
	private String dataAlteracao,
				   descricao,
				   mensagem;
	
	private String bloqueia,
				 descontoPromocao,
				 parcelaEmAberto,
				 vistaPrazo;
	
	private double descontoAtacadoVista,
				   descontoAtacadoPrazo,
				   descontoVarejoVista,
				   descontoVarejoPrazo,
				   descontoServicoVista,
				   descontoServicoPrazo;

	/**
	 * @return the idStatus
	 */
	public int getIdStatus() {
		return idStatus;
	}

	/**
	 * @param idStatus the idStatus to set
	 */
	public void setIdStatus(int idStatus) {
		this.idStatus = idStatus;
	}

	/**
	 * @return the codigo
	 */
	public int getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo the codigo to set
	 */
	public void setCodigo(int codigo) {
		this.codigo = codigo;
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
	 * @return the mensagem
	 */
	public String getMensagem() {
		return mensagem;
	}

	/**
	 * @param mensagem the mensagem to set
	 */
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	/**
	 * @return the bloqueia
	 */
	public String getBloqueia() {
		return bloqueia;
	}

	/**
	 * @param bloqueia the bloqueia to set
	 */
	public void setBloqueia(String bloqueia) {
		this.bloqueia = bloqueia;
	}

	/**
	 * @return the descontoPromocao
	 */
	public String getDescontoPromocao() {
		return descontoPromocao;
	}

	/**
	 * @param descontoPromocao the descontoPromocao to set
	 */
	public void setDescontoPromocao(String descontoPromocao) {
		this.descontoPromocao = descontoPromocao;
	}

	/**
	 * @return the parcelaEmAberto
	 */
	public String getParcelaEmAberto() {
		return parcelaEmAberto;
	}

	/**
	 * @param parcelaEmAberto the parcelaEmAberto to set
	 */
	public void setParcelaEmAberto(String parcelaEmAberto) {
		this.parcelaEmAberto = parcelaEmAberto;
	}

	/**
	 * @return the vistaPrazo
	 */
	public String getVistaPrazo() {
		return vistaPrazo;
	}

	/**
	 * @param vistaPrazo the vistaPrazo to set
	 */
	public void setVistaPrazo(String vistaPrazo) {
		this.vistaPrazo = vistaPrazo;
	}

	/**
	 * @return the descontoAtacadoVista
	 */
	public double getDescontoAtacadoVista() {
		return descontoAtacadoVista;
	}

	/**
	 * @param descontoAtacadoVista the descontoAtacadoVista to set
	 */
	public void setDescontoAtacadoVista(double descontoAtacadoVista) {
		this.descontoAtacadoVista = descontoAtacadoVista;
	}

	/**
	 * @return the descontoAtacadoPrazo
	 */
	public double getDescontoAtacadoPrazo() {
		return descontoAtacadoPrazo;
	}

	/**
	 * @param descontoAtacadoPrazo the descontoAtacadoPrazo to set
	 */
	public void setDescontoAtacadoPrazo(double descontoAtacadoPrazo) {
		this.descontoAtacadoPrazo = descontoAtacadoPrazo;
	}

	/**
	 * @return the descontoVarejoVista
	 */
	public double getDescontoVarejoVista() {
		return descontoVarejoVista;
	}

	/**
	 * @param descontoVarejoVista the descontoVarejoVista to set
	 */
	public void setDescontoVarejoVista(double descontoVarejoVista) {
		this.descontoVarejoVista = descontoVarejoVista;
	}

	/**
	 * @return the descontoServicoVista
	 */
	public double getDescontoServicoVista() {
		return descontoServicoVista;
	}

	/**
	 * @param descontoServicoVista the descontoServicoVista to set
	 */
	public void setDescontoServicoVista(double descontoServicoVista) {
		this.descontoServicoVista = descontoServicoVista;
	}

	/**
	 * @return the descontoServicoPrazo
	 */
	public double getDescontoServicoPrazo() {
		return descontoServicoPrazo;
	}

	/**
	 * @param descontoServicoPrazo the descontoServicoPrazo to set
	 */
	public void setDescontoServicoPrazo(double descontoServicoPrazo) {
		this.descontoServicoPrazo = descontoServicoPrazo;
	}

	public double getDescontoVarejoPrazo() {
		return descontoVarejoPrazo;
	}

	public void setDescontoVarejoPrazo(double descontoVarejoPrazo) {
		this.descontoVarejoPrazo = descontoVarejoPrazo;
	}
}

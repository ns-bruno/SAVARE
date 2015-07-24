package com.savare.beans;

public class TipoClienteBeans {

	private int idTipoCliente,
				codigoTipoCliente;
	private String descricaoTipoCliente;
	private double descontoAtacadoVista,
				   descontoAtacadoPrazo,
				   descontoVarejoVista,
				   descontoVarejoPrazo;
	private char descontoPromocao,
				 vendeAtacadoVarejo;
	
	public TipoClienteBeans() {
		
	}

	/**
	 * @return the idTipoCliente
	 */
	public int getIdTipoCliente() {
		return idTipoCliente;
	}

	/**
	 * @param idTipoCliente the idTipoCliente to set
	 */
	public void setIdTipoCliente(int idTipoCliente) {
		this.idTipoCliente = idTipoCliente;
	}

	/**
	 * @return the codigoTipoCliente
	 */
	public int getCodigoTipoCliente() {
		return codigoTipoCliente;
	}

	/**
	 * @param codigoTipoCliente the codigoTipoCliente to set
	 */
	public void setCodigoTipoCliente(int codigoTipoCliente) {
		this.codigoTipoCliente = codigoTipoCliente;
	}

	/**
	 * @return the descricaoTipoCliente
	 */
	public String getDescricaoTipoCliente() {
		return descricaoTipoCliente;
	}

	/**
	 * @param descricaoTipoCliente the descricaoTipoCliente to set
	 */
	public void setDescricaoTipoCliente(String descricaoTipoCliente) {
		this.descricaoTipoCliente = descricaoTipoCliente;
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
	 * @return the descontoVarejoPrazo
	 */
	public double getDescontoVarejoPrazo() {
		return descontoVarejoPrazo;
	}

	/**
	 * @param descontoVarejoPrazo the descontoVarejoPrazo to set
	 */
	public void setDescontoVarejoPrazo(double descontoVarejoPrazo) {
		this.descontoVarejoPrazo = descontoVarejoPrazo;
	}

	/**
	 * @return the descontoPromocao
	 */
	public char getDescontoPromocao() {
		return descontoPromocao;
	}

	/**
	 * @param descontoPromocao the descontoPromocao to set
	 */
	public void setDescontoPromocao(char descontoPromocao) {
		this.descontoPromocao = descontoPromocao;
	}

	/**
	 * @return the vendeAtacadoVarejo
	 */
	public char getVendeAtacadoVarejo() {
		return vendeAtacadoVarejo;
	}

	/**
	 * @param vendeAtacadoVarejo the vendeAtacadoVarejo to set
	 */
	public void setVendeAtacadoVarejo(char vendeAtacadoVarejo) {
		this.vendeAtacadoVarejo = vendeAtacadoVarejo;
	}
	
	
}

package com.savare.beans;

public class RamoAtividadeBeans {

	private int idRamoAtividade,
				codigo;
	private String dataAlteracao,
				   descricaoRamoAtividade;
	private char descontoPromocao;
	private double descontoAtacadoVista,
				   descontoAtacadoPrazo,
				   descontoVarejoVista,
				   descontoVarejoPrazo;
	
	public RamoAtividadeBeans() {
		
	}
	
	/**
	 * @return the idRamoAtividade
	 */
	public int getIdRamoAtividade() {
		return idRamoAtividade;
	}
	/**
	 * @param idRamoAtividade the idRamoAtividade to set
	 */
	public void setIdRamoAtividade(int idRamoAtividade) {
		this.idRamoAtividade = idRamoAtividade;
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
	 * @return the descricaoRamoAtividade
	 */
	public String getDescricaoRamoAtividade() {
		return descricaoRamoAtividade;
	}
	/**
	 * @param descricaoRamoAtividade the descricaoRamoAtividade to set
	 */
	public void setDescricaoRamoAtividade(String descricaoRamoAtividade) {
		this.descricaoRamoAtividade = descricaoRamoAtividade;
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
	 * @return the descontoVarejoAtacado
	 */
	public double getDescontoVarejoPrazo() {
		return descontoVarejoPrazo;
	}
	/**
	 * @param descontoVarejoAtacado the descontoVarejoAtacado to set
	 */
	public void setDescontoVarejoPrazo(double descontoVarejoAtacado) {
		this.descontoVarejoPrazo = descontoVarejoAtacado;
	}
	
	
}

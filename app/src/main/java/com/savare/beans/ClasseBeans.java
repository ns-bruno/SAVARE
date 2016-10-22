package com.savare.beans;

public class ClasseBeans {
	
	private int idClasse,
				codigoClasse;
	private String descricaoClasse, dataAlteracao;
	/**
	 * @return the idClasse
	 */
	public int getIdClasse() {
		return idClasse;
	}
	/**
	 * @param idClasse the idClasse to set
	 */
	public void setIdClasse(int idClasse) {
		this.idClasse = idClasse;
	}
	/**
	 * @return the codigoClasse
	 */
	public int getCodigoClasse() {
		return codigoClasse;
	}
	/**
	 * @param codigoClasse the codigoClasse to set
	 */
	public void setCodigoClasse(int codigoClasse) {
		this.codigoClasse = codigoClasse;
	}
	/**
	 * @return the descricaoClasse
	 */
	public String getDescricaoClasse() {
		return descricaoClasse;
	}
	/**
	 * @param descricaoClasse the descricaoClasse to set
	 */
	public void setDescricaoClasse(String descricaoClasse) {
		this.descricaoClasse = descricaoClasse;
	}

	public String getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
}

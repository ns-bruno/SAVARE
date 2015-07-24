package com.savare.beans;

public class TelefoneBeans {

	private int idTelefone,
				idPessoa,
				ddd;
	private String telefone,
				   obsTelefone;
	/**
	 * @return the idTelefone
	 */
	public int getIdTelefone() {
		return idTelefone;
	}
	/**
	 * @param idTelefone the idTelefone to set
	 */
	public void setIdTelefone(int idTelefone) {
		this.idTelefone = idTelefone;
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
	 * @return the telefone
	 */
	public String getTelefone() {
		return telefone;
	}
	/**
	 * @param telefone the telefone to set
	 */
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getObsTelefone() {
		return obsTelefone;
	}

	public void setObsTelefone(String obsTelefone) {
		this.obsTelefone = obsTelefone;
	}
}

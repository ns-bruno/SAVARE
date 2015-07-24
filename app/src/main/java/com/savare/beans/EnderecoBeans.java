package com.savare.beans;

public class EnderecoBeans {
	
	private int idEndereco;
	private String cep,
				   bairro,
				   logradouro,
				   numero,
				   complemento,
				   email;
	private char tipoEndereco;
	
	/**
	 * @return the idEndereco
	 */
	public int getIdEndereco() {
		return idEndereco;
	}
	/**
	 * @param idEndereco the idEndereco to set
	 */
	public void setIdEndereco(int idEndereco) {
		this.idEndereco = idEndereco;
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
	/**
	 * @return the bairro
	 */
	public String getBairro() {
		return bairro;
	}
	/**
	 * @param bairro the bairro to set
	 */
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}
	/**
	 * @return the logradouro
	 */
	public String getLogradouro() {
		return logradouro;
	}
	/**
	 * @param logradouro the logradouro to set
	 */
	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}
	/**
	 * @return the numero
	 */
	public String getNumero() {
		return numero;
	}
	/**
	 * @param numero the numero to set
	 */
	public void setNumero(String numero) {
		this.numero = numero;
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
	 * @return the tipoEndereco
	 */
	public char getTipoEndereco() {
		return tipoEndereco;
	}
	/**
	 * @param tipoEndereco the tipoEndereco to set
	 */
	public void setTipoEndereco(char tipoEndereco) {
		this.tipoEndereco = tipoEndereco;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}

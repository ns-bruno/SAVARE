package com.savare.beans;

import java.util.List;

public class TitulosListaBeans {

	private int idPessoa;
	private String status,
				   nomeRazao,
				   nomeFantasia,
				   documento,
				   portadorBanco,
				   vencimento;
	private boolean atrazado;
	private double valorRestante,
				   totalAReceber,
				   totalCredito;
	private List<ParcelaBeans> listaParcela;
	
	public TitulosListaBeans() {
	}
	
	/**
	 * @return the totalAReceber
	 */
	public double getTotalAReceber() {
		return totalAReceber;
	}
	/**
	 * @param totalAReceber the totalAReceber to set
	 */
	public void setTotalAReceber(double totalAReceber) {
		this.totalAReceber = totalAReceber;
	}
	/**
	 * @return the totalCredito
	 */
	public double getTotalCredito() {
		return totalCredito;
	}
	/**
	 * @param totalCredito the totalCredito to set
	 */
	public void setTotalCredito(double totalCredito) {
		this.totalCredito = totalCredito;
	}
	/**
	 * @return the idPessoa
	 */
	public int getIdPessoa() {
		return idPessoa;
	}
	/**
	 * @return the atrazado
	 */
	public boolean isAtrazado() {
		return atrazado;
	}
	/**
	 * @param atrazado the atrazado to set
	 */
	public void setAtrazado(boolean atrazado) {
		this.atrazado = atrazado;
	}
	/**
	 * @param idPessoa the idPessoa to set
	 */
	public void setIdPessoa(int idPessoa) {
		this.idPessoa = idPessoa;
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
	 * @return the nomeFantasia
	 */
	public String getNomeFantasia() {
		return nomeFantasia;
	}
	/**
	 * @param nomeFantasia the nomeFantasia to set
	 */
	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}
	/**
	 * @return the documento
	 */
	public String getDocumento() {
		return documento;
	}
	/**
	 * @param documento the documento to set
	 */
	public void setDocumento(String documento) {
		this.documento = documento;
	}
	/**
	 * @return the portadorBanco
	 */
	public String getPortadorBanco() {
		return portadorBanco;
	}
	/**
	 * @param portadorBanco the portadorBanco to set
	 */
	public void setPortadorBanco(String portadorBanco) {
		this.portadorBanco = portadorBanco;
	}
	/**
	 * @return the vencimento
	 */
	public String getVencimento() {
		return vencimento;
	}
	/**
	 * @param vencimento the vencimento to set
	 */
	public void setVencimento(String vencimento) {
		this.vencimento = vencimento;
	}
	/**
	 * @return the valorRestante
	 */
	public double getValorRestante() {
		return valorRestante;
	}
	/**
	 * @param valorRestante the valorRestante to set
	 */
	public void setValorRestante(double valorRestante) {
		this.valorRestante = valorRestante;
	}
	/**
	 * @return the listaParcela
	 */
	public List<ParcelaBeans> getListaParcela() {
		return listaParcela;
	}
	/**
	 * @param listaParcela the listaParcela to set
	 */
	public void setListaParcela(List<ParcelaBeans> listaParcela) {
		this.listaParcela = listaParcela;
	}
	
	
	
}

package com.savare.beans;

public class EmpresaBeans {

	private int idEmpresa, diasAtrazo, semMovimento, validadeFichaCliente, quantidadeDiasDestacaProduto, quantidadeCasasDecimais;
	private String nomeRazao,
				   nomeFantasia,
				   CpfCnpj,
				   titpoAcumuloCreditoAtacado,
				   titpoAcumuloCreditoVarejo,
				   periodocrceditoAtacado,
				   periodocrceditoVarejo,
				   dataAlt,
				   orcamentoSemEstoque,
			   	   vendeBloqueadoOrcamento,
			   	   vendeBloqueadoPedido,
			   	   multiplosPlanos,
				   fechaVendaCreditoNegativoAtacado,
				   fechaVendaCreditoNegativoVarejo;
	private double jurosDiario, valorMinimoPrazoVarejo, valorMinimoPrazoAtacado, valorMinimoVistaVarejo,
					valorMinimoVistaAtacado;
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
	 * @return the cpfCnpj
	 */
	public String getCpfCnpj() {
		return CpfCnpj;
	}
	/**
	 * @param cpfCnpj the cpfCnpj to set
	 */
	public void setCpfCnpj(String cpfCnpj) {
		CpfCnpj = cpfCnpj;
	}


	public String getTitpoAcumuloCreditoAtacado() {
		return titpoAcumuloCreditoAtacado;
	}

	public void setTitpoAcumuloCreditoAtacado(String titpoAcumuloCreditoAtacado) {
		this.titpoAcumuloCreditoAtacado = titpoAcumuloCreditoAtacado;
	}

	public String getTitpoAcumuloCreditoVarejo() {
		return titpoAcumuloCreditoVarejo;
	}

	public void setTitpoAcumuloCreditoVarejo(String titpoAcumuloCreditoVarejo) {
		this.titpoAcumuloCreditoVarejo = titpoAcumuloCreditoVarejo;
	}

	public String getPeriodocrceditoAtacado() {
		return periodocrceditoAtacado;
	}

	public void setPeriodocrceditoAtacado(String periodocrceditoAtacado) {
		this.periodocrceditoAtacado = periodocrceditoAtacado;
	}

	public String getPeriodocrceditoVarejo() {
		return periodocrceditoVarejo;
	}

	public void setPeriodocrceditoVarejo(String periodocrceditoVarejo) {
		this.periodocrceditoVarejo = periodocrceditoVarejo;
	}

	public int getDiasAtrazo() {
		return diasAtrazo;
	}

	public void setDiasAtrazo(int diasAtrazo) {
		this.diasAtrazo = diasAtrazo;
	}

	public int getSemMovimento() {
		return semMovimento;
	}

	public void setSemMovimento(int semMovimento) {
		this.semMovimento = semMovimento;
	}

	public int getValidadeFichaCliente() {
		return validadeFichaCliente;
	}

	public void setValidadeFichaCliente(int validadeFichaCliente) {
		this.validadeFichaCliente = validadeFichaCliente;
	}

	public int getQuantidadeDiasDestacaProduto() {
		return quantidadeDiasDestacaProduto;
	}

	public void setQuantidadeDiasDestacaProduto(int quantidadeDiasDestacaProduto) {
		this.quantidadeDiasDestacaProduto = quantidadeDiasDestacaProduto;
	}

	public int getQuantidadeCasasDecimais() {
		return quantidadeCasasDecimais;
	}

	public void setQuantidadeCasasDecimais(int quantidadeCasasDecimais) {
		this.quantidadeCasasDecimais = quantidadeCasasDecimais;
	}

	public String getDataAlt() {
		return dataAlt;
	}

	public void setDataAlt(String dataAlt) {
		this.dataAlt = dataAlt;
	}

	public String getOrcamentoSemEstoque() {
		return orcamentoSemEstoque;
	}

	public void setOrcamentoSemEstoque(String orcamentoSemEstoque) {
		this.orcamentoSemEstoque = orcamentoSemEstoque;
	}

	public String getVendeBloqueadoOrcamento() {
		return vendeBloqueadoOrcamento;
	}

	public void setVendeBloqueadoOrcamento(String vendeBloqueadoOrcamento) {
		this.vendeBloqueadoOrcamento = vendeBloqueadoOrcamento;
	}

	public String getVendeBloqueadoPedido() {
		return vendeBloqueadoPedido;
	}

	public void setVendeBloqueadoPedido(String vendeBloqueadoPedido) {
		this.vendeBloqueadoPedido = vendeBloqueadoPedido;
	}

	public String getMultiplosPlanos() {
		return multiplosPlanos;
	}

	public void setMultiplosPlanos(String multiplosPlanos) {
		this.multiplosPlanos = multiplosPlanos;
	}

	public String getFechaVendaCreditoNegativoAtacado() {
		return fechaVendaCreditoNegativoAtacado;
	}

	public void setFechaVendaCreditoNegativoAtacado(String fechaVendaCreditoNegativoAtacado) {
		this.fechaVendaCreditoNegativoAtacado = fechaVendaCreditoNegativoAtacado;
	}

	public String getFechaVendaCreditoNegativoVarejo() {
		return fechaVendaCreditoNegativoVarejo;
	}

	public void setFechaVendaCreditoNegativoVarejo(String fechaVendaCreditoNegativoVarejo) {
		this.fechaVendaCreditoNegativoVarejo = fechaVendaCreditoNegativoVarejo;
	}

	public double getJurosDiario() {
		return jurosDiario;
	}

	public void setJurosDiario(double jurosDiario) {
		this.jurosDiario = jurosDiario;
	}

	public double getValorMinimoPrazoVarejo() {
		return valorMinimoPrazoVarejo;
	}

	public void setValorMinimoPrazoVarejo(double valorMinimoPrazoVarejo) {
		this.valorMinimoPrazoVarejo = valorMinimoPrazoVarejo;
	}

	public double getValorMinimoPrazoAtacado() {
		return valorMinimoPrazoAtacado;
	}

	public void setValorMinimoPrazoAtacado(double valorMinimoPrazoAtacado) {
		this.valorMinimoPrazoAtacado = valorMinimoPrazoAtacado;
	}

	public double getValorMinimoVistaVarejo() {
		return valorMinimoVistaVarejo;
	}

	public void setValorMinimoVistaVarejo(double valorMinimoVistaVarejo) {
		this.valorMinimoVistaVarejo = valorMinimoVistaVarejo;
	}

	public double getValorMinimoVistaAtacado() {
		return valorMinimoVistaAtacado;
	}

	public void setValorMinimoVistaAtacado(double valorMinimoVistaAtacado) {
		this.valorMinimoVistaAtacado = valorMinimoVistaAtacado;
	}
}
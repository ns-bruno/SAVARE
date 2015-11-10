package com.savare.beans;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PessoaBeans implements Parcelable {
	
	private StatusBeans statusPessoa;
	private CidadeBeans cidadePessoa;
	private EstadoBeans estadoPessoa;
	private EnderecoBeans enderecoPessoa;
	private RamoAtividadeBeans ramoAtividade;
	private TipoClienteBeans tipoClientePessoa;
	private TipoDocumentoBeans tipoDocumentoPessoa;
	private PortadorBancoBeans portadorBancoPessoa;
	private PlanoPagamentoBeans planoPagamentoPessoa;
	private List<TelefoneBeans> listaTelefone;
	
	private String dataAlteracao,
				   cpfCnpj,
				   ieRg,
				   nomeRazao,
				   nomeFantasia,
				   dataNascimento,
				   inscricaoJunta,
				   insccricaoSuframa,
				   inscricaoMunicipal,
				   inscricaoProdutor,
				   empresaTrabalho,
				   observacao,
				   Conjuge,
				   cpfConjuge,
				   dataNascimentoConjuge,
				   complementoCargoConjuge,
				   rgConjuge,
				   empresaConjuge,
				   dataAdmissaoConjuge,
				   dataUltimaCompra,
				   dataRenovacao,
				   dataUltimaVisita;
	
	private int idPessoa,
				codigoCliente,
				codigoFuncionario,
				codigoUsuario,
				codigoTransportadora,
				quantidadeFuncionarios,
				numeroDependenteMaior,
				numeroDependenteMenor;
	
	private char cliente,
				 fornecedor,
				 funcionario,
				 usuario,
				 representante,
				 concorrente,
				 transportadora,
				 sexo,
				 pessoa,
				 civil,
				 orgaoEmissorConjuge,
				 ativo,
				 enviarExtrato,
				 tipoExtrato,
				 conjugePodeComprar;
	
	private double  rendaMesGiro,
					outrasRendas,
					capitalSocial,
					estoqueMercadorias,
					estoqueMateriaPrima,
					movimentoVendas,
					despesas,
					limiteCompra,
					limiteConjuge,
					rendaConjuge,
					descontoAtacadoVista,
					descontoAtacadoPrazo,
					descontoVarejoVista,
					descontoVarejoPrazo,
					creditoAcumulado,
					totalAPagar,
					totalVencido,
					totalPago;
	private boolean cadastroNovo;
	
	public PessoaBeans(Parcel dados) {
		this.idPessoa = dados.readInt();
		this.nomeRazao = dados.readString();
	}
	
	public PessoaBeans() {
		
	}

	/**
	 * @return the totalPago
	 */
	public double getTotalPago() {
		return totalPago;
	}

	/**
	 * @param totalPago the totalPago to set
	 */
	public void setTotalPago(double totalPago) {
		this.totalPago = totalPago;
	}

	/**
	 * @return the totalVencido
	 */
	public double getTotalVencido() {
		return totalVencido;
	}

	/**
	 * @param totalVencido the totalVencido to set
	 */
	public void setTotalVencido(double totalVencido) {
		this.totalVencido = totalVencido;
	}

	/**
	 * @return the totalAPagar
	 */
	public double getTotalAPagar() {
		return totalAPagar;
	}

	/**
	 * @param totalAPagar the totalAPagar to set
	 */
	public void setTotalAPagar(double totalAPagar) {
		this.totalAPagar = totalAPagar;
	}

	/**
	 * @return the creditoAcumulado
	 */
	public double getCreditoAcumulado() {
		return creditoAcumulado;
	}

	/**
	 * @param creditoAcumulado the creditoAcumulado to set
	 */
	public void setCreditoAcumulado(double creditoAcumulado) {
		this.creditoAcumulado = creditoAcumulado;
	}

	/**
	 * @return the dataUltimaVisita
	 */
	public String getDataUltimaVisita() {
		return dataUltimaVisita;
	}

	/**
	 * @param dataUltimaVisita the dataUltimaVisita to set
	 */
	public void setDataUltimaVisita(String dataUltimaVisita) {
		this.dataUltimaVisita = dataUltimaVisita;
	}

	/**
	 * @return the ramoAtividade
	 */
	public RamoAtividadeBeans getRamoAtividade() {
		return ramoAtividade;
	}

	/**
	 * @param ramoAtividade the ramoAtividade to set
	 */
	public void setRamoAtividade(RamoAtividadeBeans ramoAtividade) {
		this.ramoAtividade = ramoAtividade;
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
	 * @return the cpfCnpj
	 */
	public String getCpfCnpj() {
		return cpfCnpj;
	}

	/**
	 * @param cpfCnpj the cpfCnpj to set
	 */
	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	/**
	 * @return the ieRg
	 */
	public String getIeRg() {
		return ieRg;
	}

	/**
	 * @param ieRg the ieRg to set
	 */
	public void setIeRg(String ieRg) {
		this.ieRg = ieRg;
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
	 * @return the dataNascimento
	 */
	public String getDataNascimento() {
		return dataNascimento;
	}

	/**
	 * @param dataNascimento the dataNascimento to set
	 */
	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	/**
	 * @return the inscricaoJunta
	 */
	public String getInscricaoJunta() {
		return inscricaoJunta;
	}

	/**
	 * @param inscricaoJunta the inscricaoJunta to set
	 */
	public void setInscricaoJunta(String inscricaoJunta) {
		this.inscricaoJunta = inscricaoJunta;
	}

	/**
	 * @return the insccricaoSuframa
	 */
	public String getInsccricaoSuframa() {
		return insccricaoSuframa;
	}

	/**
	 * @param insccricaoSuframa the insccricaoSuframa to set
	 */
	public void setInsccricaoSuframa(String insccricaoSuframa) {
		this.insccricaoSuframa = insccricaoSuframa;
	}

	/**
	 * @return the inscricaoMunicipal
	 */
	public String getInscricaoMunicipal() {
		return inscricaoMunicipal;
	}

	/**
	 * @param inscricaoMunicipal the inscricaoMunicipal to set
	 */
	public void setInscricaoMunicipal(String inscricaoMunicipal) {
		this.inscricaoMunicipal = inscricaoMunicipal;
	}

	/**
	 * @return the inscricaoProdutor
	 */
	public String getInscricaoProdutor() {
		return inscricaoProdutor;
	}

	/**
	 * @param inscricaoProdutor the inscricaoProdutor to set
	 */
	public void setInscricaoProdutor(String inscricaoProdutor) {
		this.inscricaoProdutor = inscricaoProdutor;
	}

	/**
	 * @return the empresaTrabalho
	 */
	public String getEmpresaTrabalho() {
		return empresaTrabalho;
	}

	/**
	 * @param empresaTrabalho the empresaTrabalho to set
	 */
	public void setEmpresaTrabalho(String empresaTrabalho) {
		this.empresaTrabalho = empresaTrabalho;
	}

	/**
	 * @return the observacao
	 */
	public String getObservacao() {
		return observacao;
	}

	/**
	 * @param observacao the observacao to set
	 */
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	/**
	 * @return the conjuge
	 */
	public String getConjuge() {
		return Conjuge;
	}

	/**
	 * @param conjuge the conjuge to set
	 */
	public void setConjuge(String conjuge) {
		Conjuge = conjuge;
	}

	/**
	 * @return the cpfConjuge
	 */
	public String getCpfConjuge() {
		return cpfConjuge;
	}

	/**
	 * @param cpfConjuge the cpfConjuge to set
	 */
	public void setCpfConjuge(String cpfConjuge) {
		this.cpfConjuge = cpfConjuge;
	}

	/**
	 * @return the dataNascimentoConjuge
	 */
	public String getDataNascimentoConjuge() {
		return dataNascimentoConjuge;
	}

	/**
	 * @param dataNascimentoConjuge the dataNascimentoConjuge to set
	 */
	public void setDataNascimentoConjuge(String dataNascimentoConjuge) {
		this.dataNascimentoConjuge = dataNascimentoConjuge;
	}

	/**
	 * @return the complementoCargoConjuge
	 */
	public String getComplementoCargoConjuge() {
		return complementoCargoConjuge;
	}

	/**
	 * @param complementoCargoConjuge the complementoCargoConjuge to set
	 */
	public void setComplementoCargoConjuge(String complementoCargoConjuge) {
		this.complementoCargoConjuge = complementoCargoConjuge;
	}

	/**
	 * @return the rgConjuge
	 */
	public String getRgConjuge() {
		return rgConjuge;
	}

	/**
	 * @param rgConjuge the rgConjuge to set
	 */
	public void setRgConjuge(String rgConjuge) {
		this.rgConjuge = rgConjuge;
	}

	/**
	 * @return the empresaConjuge
	 */
	public String getEmpresaConjuge() {
		return empresaConjuge;
	}

	/**
	 * @param empresaConjuge the empresaConjuge to set
	 */
	public void setEmpresaConjuge(String empresaConjuge) {
		this.empresaConjuge = empresaConjuge;
	}

	/**
	 * @return the dataAdmissaoConjuge
	 */
	public String getDataAdmissaoConjuge() {
		return dataAdmissaoConjuge;
	}

	/**
	 * @param dataAdmissaoConjuge the dataAdmissaoConjuge to set
	 */
	public void setDataAdmissaoConjuge(String dataAdmissaoConjuge) {
		this.dataAdmissaoConjuge = dataAdmissaoConjuge;
	}

	/**
	 * @return the dataUltimaCompra
	 */
	public String getDataUltimaCompra() {
		return dataUltimaCompra;
	}

	/**
	 * @param dataUltimaCompra the dataUltimaCompra to set
	 */
	public void setDataUltimaCompra(String dataUltimaCompra) {
		this.dataUltimaCompra = dataUltimaCompra;
	}

	/**
	 * @return the dataRenovacao
	 */
	public String getDataRenovacao() {
		return dataRenovacao;
	}

	/**
	 * @param dataRenovacao the dataRenovacao to set
	 */
	public void setDataRenovacao(String dataRenovacao) {
		this.dataRenovacao = dataRenovacao;
	}

	/**
	 * @return the codigoCliente
	 */
	public int getCodigoCliente() {
		return codigoCliente;
	}

	/**
	 * @param codigoCliente the codigoCliente to set
	 */
	public void setCodigoCliente(int codigoCliente) {
		this.codigoCliente = codigoCliente;
	}

	
	/**
	 * @return the codigoFuncionario
	 */
	public int getCodigoFuncionario() {
		return codigoFuncionario;
	}

	/**
	 * @param codigoFuncionario the codigoFuncionario to set
	 */
	public void setCodigoFuncionario(int codigoFuncionario) {
		this.codigoFuncionario = codigoFuncionario;
	}

	/**
	 * @return the codigoUsuario
	 */
	public int getCodigoUsuario() {
		return codigoUsuario;
	}

	/**
	 * @param codigoUsuario the codigoUsuario to set
	 */
	public void setCodigoUsuario(int codigoUsuario) {
		this.codigoUsuario = codigoUsuario;
	}

	
	/**
	 * @return the codigoTransportadora
	 */
	public int getCodigoTransportadora() {
		return codigoTransportadora;
	}

	/**
	 * @param codigoTransportadora the codigoTransportadora to set
	 */
	public void setCodigoTransportadora(int codigoTransportadora) {
		this.codigoTransportadora = codigoTransportadora;
	}

	/**
	 * @return the quantidadeFuncionarios
	 */
	public int getQuantidadeFuncionarios() {
		return quantidadeFuncionarios;
	}

	/**
	 * @param quantidadeFuncionarios the quantidadeFuncionarios to set
	 */
	public void setQuantidadeFuncionarios(int quantidadeFuncionarios) {
		this.quantidadeFuncionarios = quantidadeFuncionarios;
	}

	/**
	 * @return the numeroDependenteMaior
	 */
	public int getNumeroDependenteMaior() {
		return numeroDependenteMaior;
	}

	/**
	 * @param numeroDependenteMaior the numeroDependenteMaior to set
	 */
	public void setNumeroDependenteMaior(int numeroDependenteMaior) {
		this.numeroDependenteMaior = numeroDependenteMaior;
	}

	/**
	 * @return the numeroDependenteMenor
	 */
	public int getNumeroDependenteMenor() {
		return numeroDependenteMenor;
	}

	/**
	 * @param numeroDependenteMenor the numeroDependenteMenor to set
	 */
	public void setNumeroDependenteMenor(int numeroDependenteMenor) {
		this.numeroDependenteMenor = numeroDependenteMenor;
	}

	/**
	 * @return the cliente
	 */
	public char getCliente() {
		return cliente;
	}

	/**
	 * @param cliente the cliente to set
	 */
	public void setCliente(char cliente) {
		this.cliente = cliente;
	}

	/**
	 * @return the fornecedor
	 */
	public char getFornecedor() {
		return fornecedor;
	}

	/**
	 * @param fornecedor the fornecedor to set
	 */
	public void setFornecedor(char fornecedor) {
		this.fornecedor = fornecedor;
	}

	/**
	 * @return the funcionario
	 */
	public char getFuncionario() {
		return funcionario;
	}

	/**
	 * @param funcionario the funcionario to set
	 */
	public void setFuncionario(char funcionario) {
		this.funcionario = funcionario;
	}

	/**
	 * @return the usuario
	 */
	public char getUsuario() {
		return usuario;
	}

	/**
	 * @param usuario the usuario to set
	 */
	public void setUsuario(char usuario) {
		this.usuario = usuario;
	}

	/**
	 * @return the representante
	 */
	public char getRepresentante() {
		return representante;
	}

	/**
	 * @param representante the representante to set
	 */
	public void setRepresentante(char representante) {
		this.representante = representante;
	}

	/**
	 * @return the concorrente
	 */
	public char getConcorrente() {
		return concorrente;
	}

	/**
	 * @param concorrente the concorrente to set
	 */
	public void setConcorrente(char concorrente) {
		this.concorrente = concorrente;
	}

	/**
	 * @return the transportadora
	 */
	public char getTransportadora() {
		return transportadora;
	}

	/**
	 * @param transportadora the transportadora to set
	 */
	public void setTransportadora(char transportadora) {
		this.transportadora = transportadora;
	}

	/**
	 * @return the sexo
	 */
	public char getSexo() {
		return sexo;
	}

	/**
	 * @param sexo the sexo to set
	 */
	public void setSexo(char sexo) {
		this.sexo = sexo;
	}

	/**
	 * @return the pessoa
	 */
	public char getPessoa() {
		return pessoa;
	}

	/**
	 * @param pessoa the pessoa to set
	 */
	public void setPessoa(char pessoa) {
		this.pessoa = pessoa;
	}

	/**
	 * @return the civil
	 */
	public char getCivil() {
		return civil;
	}

	/**
	 * @param civil the civil to set
	 */
	public void setCivil(char civil) {
		this.civil = civil;
	}

	/**
	 * @return the orgaoEmissorConjuge
	 */
	public char getOrgaoEmissorConjuge() {
		return orgaoEmissorConjuge;
	}

	/**
	 * @param orgaoEmissorConjuge the orgaoEmissorConjuge to set
	 */
	public void setOrgaoEmissorConjuge(char orgaoEmissorConjuge) {
		this.orgaoEmissorConjuge = orgaoEmissorConjuge;
	}

	/**
	 * @return the ativo
	 */
	public char getAtivo() {
		return ativo;
	}

	/**
	 * @param ativo the ativo to set
	 */
	public void setAtivo(char ativo) {
		this.ativo = ativo;
	}

	/**
	 * @return the enviarExtrato
	 */
	public char getEnviarExtrato() {
		return enviarExtrato;
	}

	/**
	 * @param enviarExtrato the enviarExtrato to set
	 */
	public void setEnviarExtrato(char enviarExtrato) {
		this.enviarExtrato = enviarExtrato;
	}

	/**
	 * @return the tipoExtrato
	 */
	public char getTipoExtrato() {
		return tipoExtrato;
	}

	/**
	 * @param tipoExtrato the tipoExtrato to set
	 */
	public void setTipoExtrato(char tipoExtrato) {
		this.tipoExtrato = tipoExtrato;
	}

	/**
	 * @return the conjugePodeComprar
	 */
	public char getConjugePodeComprar() {
		return conjugePodeComprar;
	}

	/**
	 * @param conjugePodeComprar the conjugePodeComprar to set
	 */
	public void setConjugePodeComprar(char conjugePodeComprar) {
		this.conjugePodeComprar = conjugePodeComprar;
	}

	/**
	 * @return the rendaMesGiro
	 */
	public double getRendaMesGiro() {
		return rendaMesGiro;
	}

	/**
	 * @param rendaMesGiro the rendaMesGiro to set
	 */
	public void setRendaMesGiro(double rendaMesGiro) {
		this.rendaMesGiro = rendaMesGiro;
	}

	/**
	 * @return the outrasRendas
	 */
	public double getOutrasRendas() {
		return outrasRendas;
	}

	/**
	 * @param outrasRendas the outrasRendas to set
	 */
	public void setOutrasRendas(double outrasRendas) {
		this.outrasRendas = outrasRendas;
	}

	/**
	 * @return the capitalSocial
	 */
	public double getCapitalSocial() {
		return capitalSocial;
	}

	/**
	 * @param capitalSocial the capitalSocial to set
	 */
	public void setCapitalSocial(double capitalSocial) {
		this.capitalSocial = capitalSocial;
	}

	/**
	 * @return the estoqueMercadorias
	 */
	public double getEstoqueMercadorias() {
		return estoqueMercadorias;
	}

	/**
	 * @param estoqueMercadorias the estoqueMercadorias to set
	 */
	public void setEstoqueMercadorias(double estoqueMercadorias) {
		this.estoqueMercadorias = estoqueMercadorias;
	}

	/**
	 * @return the estoqueMateriaPrima
	 */
	public double getEstoqueMateriaPrima() {
		return estoqueMateriaPrima;
	}

	/**
	 * @param estoqueMateriaPrima the estoqueMateriaPrima to set
	 */
	public void setEstoqueMateriaPrima(double estoqueMateriaPrima) {
		this.estoqueMateriaPrima = estoqueMateriaPrima;
	}

	/**
	 * @return the movimentoVendas
	 */
	public double getMovimentoVendas() {
		return movimentoVendas;
	}

	/**
	 * @param movimentoVendas the movimentoVendas to set
	 */
	public void setMovimentoVendas(double movimentoVendas) {
		this.movimentoVendas = movimentoVendas;
	}

	/**
	 * @return the despesas
	 */
	public double getDespesas() {
		return despesas;
	}

	/**
	 * @param despesas the despesas to set
	 */
	public void setDespesas(double despesas) {
		this.despesas = despesas;
	}

	/**
	 * @return the limiteConjuge
	 */
	public double getLimiteConjuge() {
		return limiteConjuge;
	}

	/**
	 * @param limiteConjuge the limiteConjuge to set
	 */
	public void setLimiteConjuge(double limiteConjuge) {
		this.limiteConjuge = limiteConjuge;
	}

	/**
	 * @return the rendaConjuge
	 */
	public double getRendaConjuge() {
		return rendaConjuge;
	}

	/**
	 * @param rendaConjuge the rendaConjuge to set
	 */
	public void setRendaConjuge(double rendaConjuge) {
		this.rendaConjuge = rendaConjuge;
	}

	/**
	 * @return the statusPessoa
	 */
	public StatusBeans getStatusPessoa() {
		return statusPessoa;
	}

	/**
	 * @param statusPessoa the statusPessoa to set
	 */
	public void setStatusPessoa(StatusBeans statusPessoa) {
		this.statusPessoa = statusPessoa;
	}

	/**
	 * @return the cidadePessoa
	 */
	public CidadeBeans getCidadePessoa() {
		return cidadePessoa;
	}

	/**
	 * @param cidadePessoa the cidadePessoa to set
	 */
	public void setCidadePessoa(CidadeBeans cidadePessoa) {
		this.cidadePessoa = cidadePessoa;
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
	 * @return the estadoPessoa
	 */
	public EstadoBeans getEstadoPessoa() {
		return estadoPessoa;
	}

	/**
	 * @param estadoPessoa the estadoPessoa to set
	 */
	public void setEstadoPessoa(EstadoBeans estadoPessoa) {
		this.estadoPessoa = estadoPessoa;
	}
	
	/**
	 * @return the tipoClientePessoa
	 */
	public TipoClienteBeans getTipoClientePessoa() {
		return tipoClientePessoa;
	}

	/**
	 * @param tipoClientePessoa the tipoClientePessoa to set
	 */
	public void setTipoClientePessoa(TipoClienteBeans tipoClientePessoa) {
		this.tipoClientePessoa = tipoClientePessoa;
	}

	/**
	 * @return the tipoDocumentoPessoa
	 */
	public TipoDocumentoBeans getTipoDocumentoPessoa() {
		return tipoDocumentoPessoa;
	}

	/**
	 * @param tipoDocumentoPessoa the tipoDocumentoPessoa to set
	 */
	public void setTipoDocumentoPessoa(TipoDocumentoBeans tipoDocumentoPessoa) {
		this.tipoDocumentoPessoa = tipoDocumentoPessoa;
	}

	/**
	 * @return the portadorBancoPessoa
	 */
	public PortadorBancoBeans getPortadorBancoPessoa() {
		return portadorBancoPessoa;
	}

	/**
	 * @param portadorBancoPessoa the portadorBancoPessoa to set
	 */
	public void setPortadorBancoPessoa(PortadorBancoBeans portadorBancoPessoa) {
		this.portadorBancoPessoa = portadorBancoPessoa;
	}

	/**
	 * @return the planoPagamentoPessoa
	 */
	public PlanoPagamentoBeans getPlanoPagamentoPessoa() {
		return planoPagamentoPessoa;
	}

	/**
	 * @param planoPagamentoPessoa the planoPagamentoPessoa to set
	 */
	public void setPlanoPagamentoPessoa(PlanoPagamentoBeans planoPagamentoPessoa) {
		this.planoPagamentoPessoa = planoPagamentoPessoa;
	}

	/**
	 * @return the enderecoPessoa
	 */
	public EnderecoBeans getEnderecoPessoa() {
		return enderecoPessoa;
	}

	/**
	 * @param enderecoPessoa the enderecoPessoa to set
	 */
	public void setEnderecoPessoa(EnderecoBeans enderecoPessoa) {
		this.enderecoPessoa = enderecoPessoa;
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
	 * @return the limiteCompra
	 */
	public double getLimiteCompra() {
		return limiteCompra;
	}

	/**
	 * @param limiteCompra the limiteCompra to set
	 */
	public void setLimiteCompra(double limiteCompra) {
		this.limiteCompra = limiteCompra;
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

	public List<TelefoneBeans> getListaTelefone() {
		return listaTelefone;
	}

	public void setListaTelefone(List<TelefoneBeans> listaTelefone) {
		this.listaTelefone = listaTelefone;
	}

	public boolean isCadastroNovo() {
		return cadastroNovo;
	}

	public void setCadastroNovo(boolean cadastroNovo) {
		this.cadastroNovo = cadastroNovo;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.idPessoa);
		dest.writeString(this.nomeRazao);
	}
	
	public static final Parcelable.Creator<PessoaBeans> CREATOR = new Creator<PessoaBeans>() {

		@Override
		public PessoaBeans createFromParcel(Parcel source) {
			return new PessoaBeans(source);
		}

		@Override
		public PessoaBeans[] newArray(int size) {
			return new PessoaBeans[size];
		}
		
	};
				   

}

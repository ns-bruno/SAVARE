package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 15/08/2016.
 */
public class ParametroBeans {

    private int idParametro, idClifo, idEmpresa, idVendedor, idTipoCobranca, idTipoDocumento,
                idPlanoPagamento, idPortadorBanco, roteiro, frequencia, diasAtrazo, diasCarencia;
    private String dataAlteracao, vendeAtrazado, descontoPromocao, dataUltimaVisita, dataUltimoEnvio,
                    dataUltimoRecebimento, dataProximoContato, atacadoVarejo, vistaPrazo,
                    faturaValorMinimo, parcelaEmAberto;
    private double limite, descontoAtacadoVista, descontoAtacadoPrazo, descontoVarejoVista,
                   descontoVarejoPrazo, jurosDiario;

    public int getIdParametro() {
        return idParametro;
    }

    public void setIdParametro(int idParametro) {
        this.idParametro = idParametro;
    }

    public int getIdClifo() {
        return idClifo;
    }

    public void setIdClifo(int idClifo) {
        this.idClifo = idClifo;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(int idVendedor) {
        this.idVendedor = idVendedor;
    }

    public int getIdTipoCobranca() {
        return idTipoCobranca;
    }

    public void setIdTipoCobranca(int idTipoCobranca) {
        this.idTipoCobranca = idTipoCobranca;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public int getIdPlanoPagamento() {
        return idPlanoPagamento;
    }

    public void setIdPlanoPagamento(int idPlanoPagamento) {
        this.idPlanoPagamento = idPlanoPagamento;
    }

    public int getRoteiro() {
        return roteiro;
    }

    public void setRoteiro(int roteiro) {
        this.roteiro = roteiro;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public int getDiasAtrazo() {
        return diasAtrazo;
    }

    public void setDiasAtrazo(int diasAtrazo) {
        this.diasAtrazo = diasAtrazo;
    }

    public int getDiasCarencia() {
        return diasCarencia;
    }

    public void setDiasCarencia(int diasCarencia) {
        this.diasCarencia = diasCarencia;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getVendeAtrazado() {
        return vendeAtrazado;
    }

    public void setVendeAtrazado(String vendeAtrazado) {
        this.vendeAtrazado = vendeAtrazado;
    }

    public String getDescontoPromocao() {
        return descontoPromocao;
    }

    public void setDescontoPromocao(String descontoPromocao) {
        this.descontoPromocao = descontoPromocao;
    }

    public String getDataUltimaVisita() {
        return dataUltimaVisita;
    }

    public void setDataUltimaVisita(String dataUltimaVisita) {
        this.dataUltimaVisita = dataUltimaVisita;
    }

    public String getDataUltimoEnvio() {
        return dataUltimoEnvio;
    }

    public void setDataUltimoEnvio(String dataUltimoEnvio) {
        this.dataUltimoEnvio = dataUltimoEnvio;
    }

    public String getDataUltimoRecebimento() {
        return dataUltimoRecebimento;
    }

    public void setDataUltimoRecebimento(String dataUltimoRecebimento) {
        this.dataUltimoRecebimento = dataUltimoRecebimento;
    }

    public String getDataProximoContato() {
        return dataProximoContato;
    }

    public void setDataProximoContato(String dataProximoContato) {
        this.dataProximoContato = dataProximoContato;
    }

    public String getAtacadoVarejo() {
        return atacadoVarejo;
    }

    public void setAtacadoVarejo(String atacadoVarejo) {
        this.atacadoVarejo = atacadoVarejo;
    }

    public String getVistaPrazo() {
        return vistaPrazo;
    }

    public void setVistaPrazo(String vistaPrazo) {
        this.vistaPrazo = vistaPrazo;
    }

    public String getFaturaValorMinimo() {
        return faturaValorMinimo;
    }

    public void setFaturaValorMinimo(String faturaValorMinimo) {
        this.faturaValorMinimo = faturaValorMinimo;
    }

    public String getParcelaEmAberto() {
        return parcelaEmAberto;
    }

    public void setParcelaEmAberto(String parcelaEmAberto) {
        this.parcelaEmAberto = parcelaEmAberto;
    }

    public double getLimite() {
        return limite;
    }

    public void setLimite(double limite) {
        this.limite = limite;
    }

    public double getDescontoAtacadoVista() {
        return descontoAtacadoVista;
    }

    public void setDescontoAtacadoVista(double descontoAtacadoVista) {
        this.descontoAtacadoVista = descontoAtacadoVista;
    }

    public double getDescontoAtacadoPrazo() {
        return descontoAtacadoPrazo;
    }

    public void setDescontoAtacadoPrazo(double descontoAtacadoPrazo) {
        this.descontoAtacadoPrazo = descontoAtacadoPrazo;
    }

    public double getDescontoVarejoVista() {
        return descontoVarejoVista;
    }

    public void setDescontoVarejoVista(double descontoVarejoVista) {
        this.descontoVarejoVista = descontoVarejoVista;
    }

    public double getDescontoVarejoPrazo() {
        return descontoVarejoPrazo;
    }

    public void setDescontoVarejoPrazo(double descontoVarejoPrazo) {
        this.descontoVarejoPrazo = descontoVarejoPrazo;
    }

    public double getJurosDiario() {
        return jurosDiario;
    }

    public void setJurosDiario(double jurosDiario) {
        this.jurosDiario = jurosDiario;
    }

    public int getIdPortadorBanco() {
        return idPortadorBanco;
    }

    public void setIdPortadorBanco(int idPortadorBanco) {
        this.idPortadorBanco = idPortadorBanco;
    }
}

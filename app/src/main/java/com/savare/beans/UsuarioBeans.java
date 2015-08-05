package com.savare.beans;

/**
 * Created by Faturamento on 04/08/2015.
 */
public class UsuarioBeans {

    private int idUsuario,
                idEmpresa;
    private String chave,
                   nomeUsuario,
                   loginUsuario,
                   senhaUsuario,
                   email,
                   empresaUsuario,
                   ipServidor,
                   usuarioServidor,
                   senhaServidor,
                   pastaServidor,
                   dataUltimoRecebimento,
                   dataUltimoEnvio;
    private char vendeAtacadoUsuario,
                 vendeVarejoUsuario,
                 ativoUsuario;
    private double valorCreditoAtacado,
                   valorCreditoVarejo,
                   percentualCreditoAtacado,
                   percentualCreditoVarejo;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getLoginUsuario() {
        return loginUsuario;
    }

    public void setLoginUsuario(String loginUsuario) {
        this.loginUsuario = loginUsuario;
    }

    public String getSenhaUsuario() {
        return senhaUsuario;
    }

    public void setSenhaUsuario(String senhaUsuario) {
        this.senhaUsuario = senhaUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmpresaUsuario() {
        return empresaUsuario;
    }

    public void setEmpresaUsuario(String empresaUsuario) {
        this.empresaUsuario = empresaUsuario;
    }

    public String getIpServidor() {
        return ipServidor;
    }

    public void setIpServidor(String ipServidor) {
        this.ipServidor = ipServidor;
    }

    public String getUsuarioServidor() {
        return usuarioServidor;
    }

    public void setUsuarioServidor(String usuarioServidor) {
        this.usuarioServidor = usuarioServidor;
    }

    public String getSenhaServidor() {
        return senhaServidor;
    }

    public void setSenhaServidor(String senhaServidor) {
        this.senhaServidor = senhaServidor;
    }

    public String getPastaServidor() {
        return pastaServidor;
    }

    public void setPastaServidor(String pastaServidor) {
        this.pastaServidor = pastaServidor;
    }

    public String getDataUltimoRecebimento() {
        return dataUltimoRecebimento;
    }

    public void setDataUltimoRecebimento(String dataUltimoRecebimento) {
        this.dataUltimoRecebimento = dataUltimoRecebimento;
    }

    public String getDataUltimoEnvio() {
        return dataUltimoEnvio;
    }

    public void setDataUltimoEnvio(String dataUltimoEnvio) {
        this.dataUltimoEnvio = dataUltimoEnvio;
    }

    public char getVendeAtacadoUsuario() {
        return vendeAtacadoUsuario;
    }

    public void setVendeAtacadoUsuario(char vendeAtacadoUsuario) {
        this.vendeAtacadoUsuario = vendeAtacadoUsuario;
    }

    public char getVendeVarejoUsuario() {
        return vendeVarejoUsuario;
    }

    public void setVendeVarejoUsuario(char vendeVarejoUsuario) {
        this.vendeVarejoUsuario = vendeVarejoUsuario;
    }

    public char getAtivoUsuario() {
        return ativoUsuario;
    }

    public void setAtivoUsuario(char ativoUsuario) {
        this.ativoUsuario = ativoUsuario;
    }

    public double getValorCreditoAtacado() {
        return valorCreditoAtacado;
    }

    public void setValorCreditoAtacado(double valorCreditoAtacado) {
        this.valorCreditoAtacado = valorCreditoAtacado;
    }

    public double getValorCreditoVarejo() {
        return valorCreditoVarejo;
    }

    public void setValorCreditoVarejo(double valorCreditoVarejo) {
        this.valorCreditoVarejo = valorCreditoVarejo;
    }

    public double getPercentualCreditoAtacado() {
        return percentualCreditoAtacado;
    }

    public void setPercentualCreditoAtacado(double percentualCreditoAtacado) {
        this.percentualCreditoAtacado = percentualCreditoAtacado;
    }

    public double getPercentualCreditoVarejo() {
        return percentualCreditoVarejo;
    }

    public void setPercentualCreditoVarejo(double percentualCreditoVarejo) {
        this.percentualCreditoVarejo = percentualCreditoVarejo;
    }
}

package com.savare.beans;

public class CfapositBeans {
    private int idCfaposit,
                idAeaorcam;
    private PessoaBeans pessoaBeans;
    private String  dataVisita,
                    observacao,
                    status;
    private double valor;

    public int getIdCfaposit() {
        return idCfaposit;
    }

    public void setIdCfaposit(int idCfaposit) {
        this.idCfaposit = idCfaposit;
    }

    public int getIdAeaorcam() {
        return idAeaorcam;
    }

    public void setIdAeaorcam(int idAeaorcam) {
        this.idAeaorcam = idAeaorcam;
    }

    public PessoaBeans getPessoaBeans() {
        return pessoaBeans;
    }

    public void setPessoaBeans(PessoaBeans pessoaBeans) {
        this.pessoaBeans = pessoaBeans;
    }

    public String getDataVisita() {
        return dataVisita;
    }

    public void setDataVisita(String dataVisita) {
        this.dataVisita = dataVisita;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    /**
     * Valida a opcao selecionada 0 = Visitou e comprou | 1 = Visitou, mas, não comprou
     * | 2 = Não estava | 3 = Pedido feito por telefone | 4 = Pedido feito pelo balcao/loja
     * @return
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}

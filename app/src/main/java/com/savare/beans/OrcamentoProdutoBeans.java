package com.savare.beans;

import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 18/09/2015.
 */
public class OrcamentoProdutoBeans {

    OrcamentoBeans orcamento;
    List<ItemOrcamentoBeans> listaProdutosOrcamento;

    public OrcamentoBeans getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(OrcamentoBeans orcamento) {
        this.orcamento = orcamento;
    }

    public List<ItemOrcamentoBeans> getListaProdutosOrcamento() {
        return listaProdutosOrcamento;
    }

    public void setListaProdutosOrcamento(List<ItemOrcamentoBeans> listaProdutosOrcamento) {
        this.listaProdutosOrcamento = listaProdutosOrcamento;
    }
}

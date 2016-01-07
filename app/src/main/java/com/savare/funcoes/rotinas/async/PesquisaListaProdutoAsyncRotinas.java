package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.ProdutoListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ProdutoRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 06/10/2015.
 */
public class PesquisaListaProdutoAsyncRotinas extends AsyncTask<String, String, Integer> {

    private Context context;
    private String where,
                   group,
                   codigoOrcamento,
                   atacadoVarejo;
    private int tipo;
    //private ProgressDialog progresso;
    private ListView listViewProduto;
    private ProgressBar progressBar;
    private TextView textProcessoPesquisa;

    public PesquisaListaProdutoAsyncRotinas(Context context, String where, String group, String codigoOrcamento, String atacadoVarejo, int tipo, ListView listViewProduto, ProgressBar progressBar, TextView textProcessoPesquisa) {
        this.context = context;
        this.where = where;
        this.group = group;
        this.codigoOrcamento = codigoOrcamento;
        this.atacadoVarejo = atacadoVarejo;
        this.tipo = tipo;
        this.listViewProduto = listViewProduto;
        this.progressBar = progressBar;
        this.textProcessoPesquisa = textProcessoPesquisa;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Cria novo um ProgressDialogo e exibe
        /*progresso = new ProgressDialog(context);
        progresso.setMessage("Aguarde, vamos buscar o produto que deseja...");
        progresso.setCancelable(false);
        progresso.show();*/

        if (progressBar != null){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        }
        if (textProcessoPesquisa != null){
            textProcessoPesquisa.setVisibility(View.VISIBLE);
            textProcessoPesquisa.setText("Aguarde, vamos buscar o produto que deseja...");
        }
    }

    @Override
    protected Integer doInBackground(String... params) {

        criaListaDeProdutos(where, group, tipo);

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        if (textProcessoPesquisa != null) {
            textProcessoPesquisa.setText(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        // Fecha progressDialogo
        /*if (progresso.isShowing()) {
            progresso.dismiss();
        }*/
        if (progressBar != null){
            progressBar.setVisibility(View.GONE);
        }

        if (textProcessoPesquisa != null){
            textProcessoPesquisa.setVisibility(View.GONE);
        }
    }

    /**
     * Funcao responsavel para preenche a lista com os produtos,
     * de acorto com o tipo de filtro.
     * @param where
     * @param group
     * @param tipo - 0 = Normal | 1 = Campo de Pesquisa(actionBar)
     */
    private void criaListaDeProdutos(String where, String group, int tipo){
        // Atualiza a caixa de dialogo
        publishProgress("Estamos montando o buscador.");

        // Cria variavel para armazenar where auxiliar
        String whereAux = "";
        List<ProdutoListaBeans> listaProdutos = new ArrayList<>();
        final ItemUniversalAdapter adapterProduto;

        // Verifica se o tipo eh normal
        if (tipo == 0) {
            // Instancia a classe de rotina
            ProdutoRotinas produtoRotinas = new ProdutoRotinas(context);

            // Atualiza a caixa de dialogo
            publishProgress("Estamos enviando o buscador no banco de dados.");

            if( (codigoOrcamento != null) && (codigoOrcamento.length() > 0) ){

                // Cria a lista de produto e verifica se os produto existe no orcamento
                listaProdutos = produtoRotinas.listaProduto(where, group, codigoOrcamento, progressBar, textProcessoPesquisa);
            }else {
                // Cria a lista de produto sem verificar se o produto existe no orcamento
                listaProdutos = produtoRotinas.listaProduto(where, group, null, progressBar, textProcessoPesquisa);
            }

            // Lista todos os produtos, para o Campo de Pesquisa(actionBar)
        } else if(tipo == 1){

            // Verifica se a listagem de produto pertence a um orcamento
            if((codigoOrcamento != null) && (codigoOrcamento.length() > 0)){

                whereAux += "(AEAPLOJA.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + codigoOrcamento + ")) ";

            } else {
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                whereAux += "(AEAPLOJA.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")";
            }

            if(where.length() > 0){
                whereAux += " AND " + where;
            }

            // Preenche a lista de PRODUTOS
            criaListaDeProdutos(whereAux, group, 0);

        } // Fim do if tipo 1
        if (tipo == 0) {
            // Instancia o adapter e o seu tipo(produto)
            adapterProduto = new ItemUniversalAdapter(context, ItemUniversalAdapter.PRODUTO);
            // Seta a lista de produtos no adapter
            adapterProduto.setListaProduto(listaProdutos);
            // Informa o tipo da venda (atacado ou varejo)
            adapterProduto.setAtacadoVarejo(atacadoVarejo);

            if (listaProdutos.size() > 0) {
                publishProgress("Preenchendo a lista com o(s) produto(s)");
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {

                    // Seta o listView com o novo adapter que ja esta com a nova lista
                    listViewProduto.setAdapter(adapterProduto);
                }
            });
        }

    } //Fim criaListaDeProdutos

}

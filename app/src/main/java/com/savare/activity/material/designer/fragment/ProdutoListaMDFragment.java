package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.CidadeBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ProdutoRotinas;
import com.savare.provider.SearchableProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 21/12/2015.
 */
public class ProdutoListaMDFragment extends Fragment {

    public static final int TELA_LISTA_PRODUTO = 1,
                            TELA_MAIS_VENDIDOS_CIDADE = 2,
                            TELA_MAIS_VENDIDOS_AREA = 3,
                            TELA_MAIS_VENDIDOS_VENDEDOR = 4,
                            TELA_MAIS_VENDIDOS_EMPRESA = 5,
                            TELA_MAIS_VENDIDOS_CORTES_CHEGARAM = 6;
    public static final String KEY_TIPO_TELA = "KEY_TIPO_TELA";
    private int tipoTela = -1;
    private ListView listViewProdutos;
    private List<ProdutoListaBeans> listaProdutos;
    private ItemUniversalAdapter adapterListaProdutos;
    private ItemUniversalAdapter adapterFiltroCidade;
    private Spinner spinnerFiltro;
    private View viewOrcamento;
    private TextView textMensagem;
    private String idOrcamento, idCliente, atacadoVarejo = "0";
    private ProgressBar progressBarListaProdutos;
    private Boolean pesquisando = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamento = inflater.inflate(R.layout.fragment_produto_lista_universal_md, container, false);

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if(parametro != null){
            tipoTela = parametro.getInt(KEY_TIPO_TELA);
            idOrcamento = parametro.getString(ProdutoListaTabMD.KEY_ID_ORCAMENTO);
            idCliente = parametro.getString(ProdutoListaTabMD.KEY_ID_CLIENTE);
            atacadoVarejo = parametro.getString(ProdutoListaTabMD.KEY_ATACADO_VAREJO);
        }

        recuperarCampos();

        criaListaProdutos();

        return viewOrcamento;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.produto_lista_tab_md, menu);

        // Configuracao associando item de pesquisa com a SearchView
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);

        //SearchView searchView = (SearchView) menu.findItem(R.id.menu_produto_lista_tab_md_fragment_search_pesquisar).getActionView();
        final SearchView searchView;
        MenuItem itemMenuSearch = menu.findItem(R.id.menu_produto_lista_tab_md_search_pesquisar);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
            searchView = (SearchView) itemMenuSearch.getActionView();
        }
        else{
            searchView = (SearchView) MenuItemCompat.getActionView(itemMenuSearch);
        }

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Checa se ja esta fazendo alguma pesquisa
                if (!pesquisando) {
                    // Marca para avisar a app que esta fazendo uma pesquisa
                    pesquisando = true;
                    // Checa se o texto a ser pesquisado esta vasio
                    if (query != null && query.length() > 0) {

                        // Adiciona a query no historico de pesquisa
                        SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(), SearchableProvider.AUTHORITY, SearchableProvider.MODE);
                        searchRecentSuggestions.saveRecentQuery(query, null);

                        // Tira o espaco que contem na query de pesquisa e substitui por %
                        query = query.replaceAll(" ", "%");

                        // Monta a clausula where para buscar no banco de dados
                        String where = "( (AEAPRODU.DESCRICAO LIKE '%" + query + "%') OR "
                                + "(AEAPRODU.CODIGO_ESTRUTURAL LIKE '%" + query + "%') OR "
                                + "(AEAPRODU.DESCRICAO_AUXILIAR LIKE '%" + query + "%') OR "
                                + "(AEAPRODU.REFERENCIA LIKE '%" + query + "%') OR "
                                + "(AEAMARCA.DESCRICAO LIKE '%" + query + "%') )";
                        // Limpa o listView
                        listViewProdutos.setAdapter(null);
                        // Executa
                        LoaderProdutos loaderProdutosAsync = new LoaderProdutos(where);
                        loaderProdutosAsync.execute();

                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setQueryHint(getResources().getString(R.string.pesquisar));
    }

    private void recuperarCampos(){
        listViewProdutos = (ListView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_listView_lista_produto);
        progressBarListaProdutos = (ProgressBar) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_progressBar_lista_produto);
        spinnerFiltro = (Spinner) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_spinner_filtro);
        textMensagem = (TextView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_text_mensagem_geral);
    }

    private void criaListaProdutos(){

        ProdutoRotinas produtoRotinas = new ProdutoRotinas(getContext());

        if (tipoTela == TELA_LISTA_PRODUTO){

        } else if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE){
            // Mosta a opcao de filtro de cidades
            spinnerFiltro.setVisibility(View.VISIBLE);

            adapterFiltroCidade = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.CIDADE);
            // Pega a lista de cidades
            adapterFiltroCidade.setListaCidade(produtoRotinas.listaCidadesMaisVendidos());

            // Checa se retornou apenas um resultado(padrao de nenhum valor)
            if (adapterFiltroCidade.getCount() == 1){
                // Deixa invisivel o spinne e a lista
                listViewProdutos.setVisibility(View.GONE);
                spinnerFiltro.setVisibility(View.GONE);
                textMensagem.setVisibility(View.VISIBLE);

            }else {
                // Preenche o spinne com a lista de cidades
                spinnerFiltro.setAdapter(adapterFiltroCidade);
            }

        } else if (tipoTela == TELA_MAIS_VENDIDOS_AREA){

        } else if (tipoTela == TELA_MAIS_VENDIDOS_VENDEDOR){

        } else if (tipoTela == TELA_MAIS_VENDIDOS_EMPRESA){

        } else if (tipoTela == TELA_MAIS_VENDIDOS_CORTES_CHEGARAM){

        }
    }

    public class LoaderProdutos extends AsyncTask<Void, Void, Void> {
        String where = "";

        public LoaderProdutos(String where) {
            this.where = where;
        }

        // Aqui eh o que acontece antes da tarefa principal ser executado
        @Override
        protected void onPreExecute() {
            // o progressBar agora eh setado como visivel
            progressBarListaProdutos.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Instancia a classe de rotina
                ProdutoRotinas produtoRotinas = new ProdutoRotinas(getContext());

                // Checa se o tipo de tela eh a de lista de produtos simples
                if (tipoTela == TELA_LISTA_PRODUTO) {
                    // Checa se tem algum orcamento vinculado na visualizacao
                    if ((idOrcamento != null) && (idOrcamento.length() > 0)) {

                        // Cria a lista de produto e verifica se os produto existe no orcamento
                        listaProdutos = produtoRotinas.listaProduto(where, null, idOrcamento, progressBarListaProdutos, null);

                    } else {
                        // Cria a lista de produto sem verificar se o produto existe no orcamento
                        listaProdutos = produtoRotinas.listaProduto(where, null, null, progressBarListaProdutos, null);
                    }
                } else if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE) {
                    // Checa se tem algum orcamento vinculado na visualizacao
                    if ((idOrcamento != null) && (idOrcamento.length() > 0)) {

                        //produtoRotinas.listaProdutoMaisVendido(tipoTela, contentvalue, where, null, idOrcamento, progressBarListaProdutos, null);
                    }
                }
                // Checa se a lista de produtos nao esta vazia e nem nula
                if ((listaProdutos != null) && (listaProdutos.size() > 0)){
                    // Instancia o adapter e o seu tipo(produto)
                    adapterListaProdutos = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.PRODUTO);
                    // Seta a lista de produtos no adapter
                    adapterListaProdutos.setListaProduto(listaProdutos);
                    // Informa o tipo da venda (atacado ou varejo)
                    adapterListaProdutos.setAtacadoVarejo(atacadoVarejo);

                } else {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.produtos)
                                    .content(R.string.nao_achamos_nenhum_produto_com_descricao_digitada)
                                    .positiveText(android.R.string.ok)
                                    //.negativeText(R.string.disagree)
                                    .autoDismiss(true)
                                    .show();
                        }
                    });
                }
            }catch (Exception e) {
                // Armazena as informacoes para para serem exibidas e enviadas
                ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "ProdutoListaMDFragment");
                contentValues.put("mensagem", "Erro ao carregar os dados do produto. \n" + e.getMessage());
                contentValues.put("dados", e.toString());
                // Pega os dados do usuario
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                contentValues.put("email", funcoes.getValorXml("Email"));
                // Exibe a mensagem
                funcoes.menssagem(contentValues);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if ((listaProdutos != null) && (listaProdutos.size() > 0)) {
                // Preenche a listView com os produtos buscados
                listViewProdutos.setAdapter(adapterListaProdutos);
            }
            //tirando o ProgressBar da nossa tela
            progressBarListaProdutos.setVisibility(View.GONE);

            pesquisando = false;
        }

    }
}

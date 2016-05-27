package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.activity.material.designer.ListaOrcamentoPedidoMDActivity;
import com.savare.activity.material.designer.OrcamentoProdutoDetalhesTabFragmentMDActivity;
import com.savare.activity.material.designer.ProdutoListaMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.ProdutoRecomendadoSql;
import com.savare.beans.AreaBeans;
import com.savare.beans.CidadeBeans;
import com.savare.beans.FotosBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.FotoRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ProdutoRotinas;
import com.savare.provider.SearchableProvider;

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
    private ItemUniversalAdapter adapterFiltroArea;
    private Spinner spinnerFiltro;
    private View viewOrcamento;
    private TextView textMensagem;
    private String idOrcamento, idCliente, atacadoVarejo = "0", atacadoVarejoAuxiliar = "0", nomeRazao;
    private ProgressBar progressBarListaProdutos;
    private Boolean pesquisando = false;
    private ProdutoListaBeans produtoVendaClicado;
    private long idItemOrcamento = 0;
    public static final String KEY_TELA_PRODUTO_LISTA_ACTIVITY = "ProdutoListaActivity";
    private TextView textCodigoOrcamento, textCodigoPessoa, textNomeRazao, textAtacadoVarejo, textProcessoPesquisa;
    LinearLayout layoutFragmentRodape;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamento = inflater.inflate(R.layout.fragment_produto_lista_universal_md, container, false);

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        recuperarCampos();



        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if(parametro != null){
            tipoTela = parametro.getInt(KEY_TIPO_TELA);
            idOrcamento = parametro.getString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO);
            idCliente = parametro.getString(ProdutoListaMDActivity.KEY_ID_CLIENTE);
            atacadoVarejo = parametro.getString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO);
            atacadoVarejoAuxiliar = atacadoVarejo;
            nomeRazao = parametro.getString(ProdutoListaMDActivity.KEY_NOME_RAZAO);

            if ((idOrcamento != null) && (idCliente.length() > 0)) {
                textCodigoOrcamento.setText(idOrcamento);
                textNomeRazao.setText(nomeRazao);
                textCodigoPessoa.setText(idCliente);
                textAtacadoVarejo.setText(atacadoVarejo);

            } else {

                layoutFragmentRodape.setVisibility(View.GONE);
            }
        }

        criaListaProdutos();

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Checa qual eh o tipo da tela
                if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE){
                    // Limpa o listView
                    listViewProdutos.setAdapter(null);
                    // Executa
                    LoaderProdutos loaderProdutosAsync = new LoaderProdutos(null, spinnerFiltro);
                    loaderProdutosAsync.execute();

                } else if (tipoTela == TELA_MAIS_VENDIDOS_AREA) {

                    // Limpa o listView
                    listViewProdutos.setAdapter(null);
                    // Executa
                    LoaderProdutos loaderProdutosAsync = new LoaderProdutos(null, spinnerFiltro);
                    loaderProdutosAsync.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listViewProdutos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Checa se a lista de produtos pertence a um orcamento
                if ((idOrcamento != null) && (idOrcamento.length() > 0)) {

                    //Pega os dados da pessoa que foi clicado
                    ProdutoListaBeans produtoVenda = (ProdutoListaBeans) parent.getItemAtPosition(position);
                    produtoVenda.setAtacadoVarejo((atacadoVarejo != null) ? atacadoVarejo.charAt(0) : atacadoVarejoAuxiliar.charAt(0));

                    //Bundle bundle = new Bundle();
                    //bundle.putParcelable("AEAORCAM", preencheDadosOrcamento());
                    //bundle.putString("ID_AEAPRODU", "" + produtoVenda.getProduto().getIdProduto());
                    //bundle.putString("ATAC_VARE", (atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);
                    //bundle.putInt("POSICAO", position);
                    //bundle.putLong("ID_AEAITORC", idItemOrcamento);
                    //bundle.putString("ID_AEAORCAM", idOrcamento);
                    //bundle.putString("ID_CFACLIFO", idCliente);
                    //bundle.putString("RAZAO_SOCIAL", nomeRazao);
                    //intent.putExtras(bundle);

                    // Abre a tela de detalhes do produto
                    Intent intent = new Intent(getContext(), OrcamentoProdutoDetalhesTabFragmentMDActivity.class);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAORCAM, Integer.parseInt(idOrcamento));
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU, produtoVenda.getProduto().getIdProduto());
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_CFACLIFO, Integer.parseInt(idCliente));
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_RAZAO_SOCIAL, nomeRazao);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_POSICAO, position);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAITORC, (int) idItemOrcamento);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ATACADO_VAREJO, (atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);

                    startActivityForResult(intent, 1);

                } else {
                    // Pega os dados do produto clicado
                    produtoVendaClicado = (ProdutoListaBeans) parent.getItemAtPosition(position);
                    produtoVendaClicado.setAtacadoVarejo((atacadoVarejo != null) ? atacadoVarejo.charAt(0) : atacadoVarejoAuxiliar.charAt(0));

                    // Abre a tela de detalhes do produto
                    Intent intent = new Intent(getContext(), ListaOrcamentoPedidoMDActivity.class);
                    intent.putExtra(ListaOrcamentoPedidoMDActivity.KEY_ORCAMENTO_PEDIDO, "O");
                    intent.putExtra(ListaOrcamentoPedidoMDActivity.KEY_RETORNA_VALOR, ListaOrcamentoPedidoMDActivity.TELA_LISTA_PRODUTOS);
                    // Abre a activity aquardando uma resposta
                    startActivityForResult(intent, 1);
                }
            }
        });

        return viewOrcamento;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.produto_lista_tab_md, menu);

        // Configuracao associando item de pesquisa com a SearchView
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);

        //SearchView searchView = (SearchView) menu.findItem(R.id.menu_produto_lista_tab_md_fragment_search_pesquisar).getActionView();
        final SearchView searchView;
        final MenuItem itemMenuSearch = menu.findItem(R.id.menu_produto_lista_tab_md_search_pesquisar);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
            searchView = (SearchView) itemMenuSearch.getActionView();
        }
        else{
            searchView = (SearchView) MenuItemCompat.getActionView(itemMenuSearch);
        }

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //SearchView searchView = (SearchView) findViewById(R.id.search);
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.branco));
        searchEditText.setHintTextColor(getResources().getColor(R.color.branco));

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

                        if ((tipoTela == TELA_MAIS_VENDIDOS_AREA) || (tipoTela == TELA_MAIS_VENDIDOS_CIDADE)) {
                            // Limpa o listView
                            listViewProdutos.setAdapter(null);
                            // Executa
                            LoaderProdutos loaderProdutosAsync = new LoaderProdutos(where, spinnerFiltro);
                            loaderProdutosAsync.execute();

                        } else {
                            // Limpa o listView
                            listViewProdutos.setAdapter(null);
                            // Executa
                            LoaderProdutos loaderProdutosAsync = new LoaderProdutos(where, null);
                            loaderProdutosAsync.execute();
                            // Tira o foco da searchView e fecha o teclado virtual
                            searchView.clearFocus();
                            return true;
                        }
                        // Tira o foco da searchView e fecha o teclado virtual
                        searchView.clearFocus();

                        // Forca o fechamento do teclado virtual
                        if (viewOrcamento != null) {
                            InputMethodManager imm = (InputMethodManager) viewOrcamento.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(viewOrcamento.getWindowToken(), 0);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setQueryHint(getResources().getString(R.string.pesquisar));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                // Pega a posicao da lista de produtos
                int posicao = data.getExtras().getInt("POSICAO");

                // Informa que o produto esta em um orcamento
                listaProdutos.get(posicao).setEstaNoOrcamento(data.getExtras().getChar("RESULTADO"));
                this.idItemOrcamento = data.getExtras().getLong("ID_AEAITORC");

                ((BaseAdapter) listViewProdutos.getAdapter()).notifyDataSetChanged();

            } else if(resultCode == 100){

                OrcamentoBeans orcamento = new OrcamentoBeans();
                // Pega os dados do orcamento restornado a lista de orcamento
                orcamento = data.getParcelableExtra("AEAORCAM");

                if((orcamento != null) && (orcamento.getIdOrcamento() > 0)){
                    // Mosta o rodape
                    layoutFragmentRodape.setVisibility(View.VISIBLE);

                    idOrcamento = ""+orcamento.getIdOrcamento();
                    nomeRazao = orcamento.getNomeRazao();
                    idCliente = ""+orcamento.getIdPessoa();
                    atacadoVarejo = ""+orcamento.getTipoVenda();

                    textCodigoOrcamento.setText(""+orcamento.getIdOrcamento());
                    textNomeRazao.setText(orcamento.getNomeRazao());
                    textCodigoPessoa.setText("" + orcamento.getIdPessoa());
                    textAtacadoVarejo.setText("" + orcamento.getTipoVenda());

                    Intent dadosParametro = new Intent(getContext(), OrcamentoProdutoDetalhesTabFragmentMDActivity.class);
                    // Pega os dados para enviar para outra tela
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU, produtoVendaClicado.getProduto().getIdProduto());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAORCAM, orcamento.getIdOrcamento());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_CFACLIFO, orcamento.getIdPessoa());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_RAZAO_SOCIAL, orcamento.getNomeRazao());
                    //dadosParametro.putExtra("POSICAO", position);
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAITORC, produtoVendaClicado.getProduto().getIdProduto());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ATACADO_VAREJO, atacadoVarejo);
                    dadosParametro.putExtra(OrcamentoProdutoMDFragment.KEY_TELA_CHAMADA, KEY_TELA_PRODUTO_LISTA_ACTIVITY);

                    startActivityForResult(dadosParametro, 1);
                }

            } else if(resultCode == 101){
                // Mosta o rodape
                layoutFragmentRodape.setVisibility(View.GONE);

                textCodigoOrcamento.setText("");
                textNomeRazao.setText("");
                textCodigoPessoa.setText("");

                idOrcamento = null;
                nomeRazao = null;
                idCliente = null;
            }
        }
    }



    private void recuperarCampos(){
        listViewProdutos = (ListView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_listView_lista_produto);
        progressBarListaProdutos = (ProgressBar) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_progressBar_lista_produto);
        spinnerFiltro = (Spinner) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_spinner_filtro);
        textMensagem = (TextView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_text_mensagem_geral);
        textCodigoOrcamento = (TextView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_text_codigo_orcamento);
        textNomeRazao = (TextView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_text_nome_razao);
        textCodigoPessoa = (TextView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_text_codigo_pessoa);
        textAtacadoVarejo = (TextView) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_text_atacado_varejo);
        layoutFragmentRodape = (LinearLayout) viewOrcamento.findViewById(R.id.fragment_produto_lista_universal_md_linearlayout_rodape);
    }

    private void criaListaProdutos(){

        ProdutoRotinas produtoRotinas = new ProdutoRotinas(getContext());

        if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE){

            adapterFiltroCidade = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.CIDADE);
            // Pega a lista de cidades
            adapterFiltroCidade.setListaCidade(produtoRotinas.listaCidadesMaisVendidos());

            // Checa se retornou apenas um resultado(padrao de nenhum valor)
            if (adapterFiltroCidade.getCount() == 1){
                // Deixa invisivel o spinne e a lista
                listViewProdutos.setVisibility(View.GONE);
                spinnerFiltro.setVisibility(View.GONE);
                layoutFragmentRodape.setVisibility(View.GONE);
                // Mostra a mensagem
                textMensagem.setVisibility(View.VISIBLE);

            }else {
                // Mosta a opcao de filtro de cidades
                spinnerFiltro.setVisibility(View.VISIBLE);

                // Preenche o spinne com a lista de cidades
                spinnerFiltro.setAdapter(adapterFiltroCidade);
            }

        } else if (tipoTela == TELA_MAIS_VENDIDOS_AREA){

            adapterFiltroArea =new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.AREA);
            // Pega a lista de area
            adapterFiltroArea.setListaArea(produtoRotinas.listaAreaMaisVendidos());

            // Checa se retornou apenas um resultado(padrao de nenhum valor)
            if (adapterFiltroArea.getCount() == 1){
                // Deixa invisivel o spinne e a lista
                listViewProdutos.setVisibility(View.GONE);
                spinnerFiltro.setVisibility(View.GONE);
                textMensagem.setVisibility(View.VISIBLE);

            }else {
                // Mosta a opcao de filtro de cidades
                spinnerFiltro.setVisibility(View.VISIBLE);

                // Preenche o spinne com a lista de cidades
                spinnerFiltro.setAdapter(adapterFiltroCidade);
            }

        } else if (tipoTela == TELA_MAIS_VENDIDOS_VENDEDOR){
            ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(getContext());

            if (produtoRecomendadoSql.getCountRows("ID_CFACLIFO_VENDEDOR IS NOT NULL") <= 0){
                // Deixa invisivel o spinne e a lista
                listViewProdutos.setVisibility(View.GONE);
                spinnerFiltro.setVisibility(View.GONE);
                textMensagem.setVisibility(View.VISIBLE);
            }

        } else if (tipoTela == TELA_MAIS_VENDIDOS_EMPRESA){
            ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(getContext());

            if (produtoRecomendadoSql.getCountRows("ID_SMAEMPRE IS NOT NULL") <= 0){
                // Deixa invisivel o spinne e a lista
                listViewProdutos.setVisibility(View.GONE);
                spinnerFiltro.setVisibility(View.GONE);
                textMensagem.setVisibility(View.VISIBLE);
            }
        } else if (tipoTela == TELA_MAIS_VENDIDOS_CORTES_CHEGARAM){
            ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(getContext());

            if (produtoRecomendadoSql.getCountRows("ID_CFACLIFO IS NOT NULL") <= 0){
                // Deixa invisivel o spinne e a lista
                listViewProdutos.setVisibility(View.GONE);
                spinnerFiltro.setVisibility(View.GONE);
                textMensagem.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Funcao para retornar os dados do orcamento
     * @return
     */
    private OrcamentoBeans preencheDadosOrcamento(){
        OrcamentoBeans orcamento = new OrcamentoBeans();
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

        orcamento.setIdOrcamento(Integer.valueOf(idOrcamento));
        orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml("CodigoEmpresa")));
        orcamento.setIdPessoa(Integer.valueOf(idCliente));
        orcamento.setNomeRazao(nomeRazao);
        orcamento.setTipoVenda((atacadoVarejo != null) ? atacadoVarejo.charAt(0) : atacadoVarejoAuxiliar.charAt(0));

        return orcamento;
    }

    public class LoaderProdutos extends AsyncTask<Void, Void, Void> {
        String where = "";
        AreaBeans area;
        CidadeBeans cidade;

        public LoaderProdutos(String where, Spinner spinnerFiltro) {
            this.where = where;

            if (spinnerFiltro != null && spinnerFiltro.getCount() > 0) {
                // Checa qual eh a tela que esta chamando
                if (tipoTela == TELA_MAIS_VENDIDOS_AREA) {
                    // Pega a area selecionada
                    area = (AreaBeans) spinnerFiltro.getSelectedItem();

                    if (area.getDescricaoArea().contains(getString(R.string.nenhuma_opcao_encontrada))) {
                        listViewProdutos.setAdapter(null);
                    }
                } else if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE) {
                    // Pega a cidade selecionada
                    cidade = (CidadeBeans) spinnerFiltro.getSelectedItem();

                    if (cidade.getDescricao().contains(getString(R.string.nenhuma_opcao_encontrada))) {
                        listViewProdutos.setAdapter(null);
                    }
                }
            }
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
                // Checa a tela que esta chamando esta funcao (Mais Vendidos por Cidade)
                } else if (tipoTela == TELA_MAIS_VENDIDOS_AREA) {
                    // Checa se nao pegou uma selecao vazia
                    if (area != null){
                        // Checa se eh a opcao de nenhum selecionado
                        if (area.getDescricaoArea().contains(getString(R.string.todos))){

                            // Pega a lista de produtos baseado na opcao selecionada
                            listaProdutos = produtoRotinas.listaProdutoMaisVendido(TELA_MAIS_VENDIDOS_AREA, null, null, null, idOrcamento, progressBarListaProdutos, null);

                        } else if ( (!area.getDescricaoArea().contains(getString(R.string.todos))) && (!area.getDescricaoArea().contains(getString(R.string.nenhuma_opcao_encontrada))) &&
                                   (!area.getDescricaoArea().contains(getString(R.string.selecione_uma_opcao))) ){

                            ContentValues filtro = new ContentValues();
                            filtro.put(String.valueOf(TELA_MAIS_VENDIDOS_AREA), String.valueOf(area.getIdArea()));
                            // Pega a lista de produtos baseado na opcao selecionada
                            listaProdutos = produtoRotinas.listaProdutoMaisVendido(TELA_MAIS_VENDIDOS_AREA, filtro, null, null, idOrcamento, progressBarListaProdutos, null);
                        }
                    }
                } else if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE){
                    if (cidade != null) {

                        if (cidade.getDescricao().contains(getString(R.string.todos))) {

                            // Pega a lista de produtos baseado na opcao selecionada
                            listaProdutos = produtoRotinas.listaProdutoMaisVendido(TELA_MAIS_VENDIDOS_CIDADE, null, null, null, idOrcamento, progressBarListaProdutos, null);

                        } else if ( (!cidade.getDescricao().contains(getString(R.string.todos))) && (!cidade.getDescricao().contains(getString(R.string.nenhuma_opcao_encontrada))) &&
                                (!cidade.getDescricao().contains(getString(R.string.selecione_uma_opcao))) ){

                            ContentValues filtro = new ContentValues();
                            filtro.put(String.valueOf(TELA_MAIS_VENDIDOS_CIDADE), String.valueOf(cidade.getIdCidade()));
                            // Pega a lista de produtos baseado na opcao selecionada
                            listaProdutos = produtoRotinas.listaProdutoMaisVendido(TELA_MAIS_VENDIDOS_CIDADE, filtro, null, null, idOrcamento, progressBarListaProdutos, null);
                        }
                    }
                } else if ((tipoTela == TELA_MAIS_VENDIDOS_EMPRESA) || (tipoTela == TELA_MAIS_VENDIDOS_VENDEDOR) || (tipoTela == TELA_MAIS_VENDIDOS_CORTES_CHEGARAM)){
                    // Cria uma vareavel para salvar os parametros para filtrar
                    ContentValues filtro = new ContentValues();

                    if ((idOrcamento != null) && (idOrcamento.length() > 0)){
                        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getContext());
                        //
                        String idCliente = orcamentoRotinas.codigoClienteOrcamento(idOrcamento);
                        // Checa se retornou apenas um digito
                        if ((idCliente != null) && (idCliente.length() == 1)){
                            // Checa se retornou zero
                            if (idCliente.contains("0")){
                                // Deixa nulo a vareavel para caso tenha retornado zero
                                idCliente = null;
                            }
                        }
                        // Adiciona o codigo do cliente no filtro
                        filtro.put(String.valueOf(TELA_MAIS_VENDIDOS_CORTES_CHEGARAM), idCliente);
                    }
                    // Pega a lista de produtos baseado
                    listaProdutos = produtoRotinas.listaProdutoMaisVendido(tipoTela, filtro, null, null, idOrcamento, progressBarListaProdutos, null);

                }
                // Checa se a lista de produtos nao esta vazia e nem nula
                if ((listaProdutos != null) && (listaProdutos.size() > 0)){
                    // Instancia o adapter e o seu tipo(produto)
                    adapterListaProdutos = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.PRODUTO);
                    // Seta a lista de produtos no adapter
                    adapterListaProdutos.setListaProduto(listaProdutos);
                    // Informa o tipo da venda (atacado ou varejo)
                    adapterListaProdutos.setAtacadoVarejo((atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);

                } else {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.produtos)
                                    .content(((tipoTela == TELA_LISTA_PRODUTO) || (tipoTela == TELA_MAIS_VENDIDOS_EMPRESA)
                                            || (tipoTela == TELA_MAIS_VENDIDOS_VENDEDOR) || (tipoTela == TELA_MAIS_VENDIDOS_CORTES_CHEGARAM)) ?
                                            R.string.nao_achamos_nenhum_produto_com_descricao_digitada : R.string.nenhum_valor_encontrado)
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

                LoaderImagemProdutos carregarImagemProduto = new LoaderImagemProdutos(getContext());
                carregarImagemProduto.execute();
            }
            //tirando o ProgressBar da nossa tela
            progressBarListaProdutos.setVisibility(View.GONE);

            pesquisando = false;
        }

    } // Fim LoaderProdutos



    public class LoaderImagemProdutos extends AsyncTask<Void, Void, Void> {

        private Context context;

        public LoaderImagemProdutos(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(this.context);

                // Checa se pode mostrar a imagem do produto
                if (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase("S")){
                    // Checa se tem alguma lista de produtos preenchida
                    if (adapterListaProdutos.getListaProduto().size() > 0){

                        for (int i = 0; i < adapterListaProdutos.getListaProduto().size(); i++){
                            FotoRotinas fotoRotinas = new FotoRotinas(context);

                            FotosBeans fotoProduto = fotoRotinas.fotoIdProtudo("" + adapterListaProdutos.getListaProduto().get(i).getProduto().getIdProduto());
                            // Checa se tem alguma foto
                            if ((fotoProduto != null) && (fotoProduto.getFotos().length > 0)){
                                // Atualiza o adapte com a foto do produto
                                adapterListaProdutos.getListaProduto().get(i).getProduto().setImagemProduto(fotoProduto);

                            }
                        }
                        // Envia um sinal para o adapter atualizar
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                adapterListaProdutos.notifyDataSetChanged();
                            }
                        });
                    }
                }
            } catch (Exception e){
                // Armazena as informacoes para para serem exibidas e enviadas
                ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "ProdutoListaMDFragment");
                contentValues.put("mensagem", getResources().getString(R.string.nao_consegimos_carregar_imagem_produtos) + " \n" + e.getMessage());
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
    } // Fim LoaderImagemProdutos
}

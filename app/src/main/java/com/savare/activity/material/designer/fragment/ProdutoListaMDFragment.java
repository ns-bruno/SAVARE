package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.activity.material.designer.LegendaProdutoListaMDActivity;
import com.savare.activity.material.designer.ListaOrcamentoPedidoMDActivity;
import com.savare.activity.material.designer.OrcamentoProdutoDetalhesTabFragmentMDActivity;
import com.savare.activity.material.designer.ProdutoListaMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.ProdutoRecomendadoSql;
import com.savare.banco.storedProcedure.CalculaPrecoSP;
import com.savare.beans.AeaembalBeans;
import com.savare.beans.AeaplojaBeans;
import com.savare.beans.AreaBeans;
import com.savare.beans.CidadeBeans;
import com.savare.beans.CfafotosBeans;
import com.savare.beans.EmpresaBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.EmbalagemRotinas;
import com.savare.funcoes.rotinas.EmpresaRotinas;
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
    private List<AeaplojaBeans> listAeaploja;
    private ItemUniversalAdapter adapterListaProdutos;
    private ItemUniversalAdapter adapterFiltroCidade;
    private ItemUniversalAdapter adapterFiltroArea;
    private Spinner spinnerFiltro;
    private View viewOrcamento;
    private TextView textMensagem;
    private String idOrcamento, idCliente, atacadoVarejo = "0", atacadoVarejoAuxiliar = "0", nomeRazao, vistaPrazo = "0";
    private ProgressBar progressBarListaProdutos;
    private Boolean pesquisando = false;
    private AeaplojaBeans produtoVendaClicado;
    private long idItemOrcamento = 0;
    public static final String KEY_TELA_PRODUTO_LISTA_ACTIVITY = "ProdutoListaActivity";
    private TextView textCodigoOrcamento, textCodigoPessoa, textNomeRazao, textAtacadoVarejo, textProcessoPesquisa;
    private LinearLayout layoutFragmentRodape;
    private boolean pequisarProdutoEstoque = false;
    private LoaderProdutos loaderProdutosAsync;
    private LoaderCalculaPrecoSP loaderCalculaPrecoSP;
    private LoaderImagemProdutos loaderCarregarImagemProduto;
    private LoaderChecaProdutoOrcamento loaderChecaProdutoOrcamento;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamento = inflater.inflate(R.layout.fragment_produto_lista_universal_md, container, false);

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        recuperarCampos();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

        if (funcoes.getValorXml(funcoes.TAG_PESQUISA_PRODUTO_ESTOQUE).equalsIgnoreCase("S")){
            pequisarProdutoEstoque = true;
        }
        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if(parametro != null) {
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
                if (atacadoVarejo.equalsIgnoreCase("0")) {
                    textAtacadoVarejo.setText(R.string.atacado);
                } else {
                    textAtacadoVarejo.setText(R.string.varejo);
                }
            } else {
                layoutFragmentRodape.setVisibility(View.GONE);
            }
        }

        criaListaProdutos();

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Checa qual eh o tipo da tela
                if ( (tipoTela == TELA_MAIS_VENDIDOS_CIDADE) || (tipoTela == TELA_MAIS_VENDIDOS_AREA) ){
                    // Limpa o listView
                    listViewProdutos.setAdapter(null);
                    // Executa
                    if (loaderProdutosAsync == null) {
                        loaderProdutosAsync = new LoaderProdutos();
                    }
                    if (loaderProdutosAsync.getStatus().equals(AsyncTask.Status.RUNNING)){
                        loaderProdutosAsync.cancel(true);
                    }
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
                    AeaplojaBeans aeaploja = (AeaplojaBeans) parent.getItemAtPosition(position);
                    //aeaploja.setAtacadoVarejo((atacadoVarejo != null) ? atacadoVarejo.charAt(0) : atacadoVarejoAuxiliar.charAt(0));

                    // Abre a tela de detalhes do produto
                    Intent intent = new Intent(getContext(), OrcamentoProdutoDetalhesTabFragmentMDActivity.class);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAORCAM, Integer.parseInt(idOrcamento));
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU, aeaploja.getAeaprodu().getIdAeaprodu());
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_CFACLIFO, Integer.parseInt(idCliente));
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_RAZAO_SOCIAL, nomeRazao);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_POSICAO, position);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAITORC, (int) idItemOrcamento);
                    intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ATACADO_VAREJO, (atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);

                    startActivityForResult(intent, 1);

                } else {
                    // Pega os dados do produto clicado
                    produtoVendaClicado = (AeaplojaBeans) parent.getItemAtPosition(position);
                    //produtoVendaClicado.setAtacadoVarejo((atacadoVarejo != null) ? atacadoVarejo.charAt(0) : atacadoVarejoAuxiliar.charAt(0));

                    // Abre a tela de detalhes do produto
                    Intent intent = new Intent(getContext(), ListaOrcamentoPedidoMDActivity.class);
                    intent.putExtra(ListaOrcamentoPedidoMDActivity.KEY_ORCAMENTO_PEDIDO, "O");
                    intent.putExtra(ListaOrcamentoPedidoMDActivity.KEY_RETORNA_VALOR, ListaOrcamentoPedidoMDActivity.TELA_LISTA_PRODUTOS);
                    intent.putExtra(ListaOrcamentoPedidoMDActivity.KEY_ATACADO_VAREJO, (atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);
                    // Abre a activity aquardando uma resposta
                    startActivityForResult(intent, 1);
                }
            }
        });

        return viewOrcamento;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Forca o fechamento do teclado virtual
        if (viewOrcamento != null) {
            InputMethodManager imm = (InputMethodManager) viewOrcamento.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewOrcamento.getWindowToken(), 0);
        }
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
        final EditText searchEditText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
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
                        StringBuilder where = new StringBuilder();
                        where.append(" (( (AEAPRODU.DESCRICAO LIKE '%" + query + "%') OR ");
                        // Checa se eh apenas numero
                        if (query.matches("[-+]?\\d*\\.?\\d+")){
                            where.append("(AEAPRODU.CODIGO = ").append(query).append(") OR ");
                        }
                        where.append("(AEAPRODU.CODIGO_ESTRUTURAL LIKE '%" + query + "%') OR ");
                        where.append("(AEAPRODU.DESCRICAO_AUXILIAR LIKE '%" + query + "%') OR ");
                        where.append("(AEAPRODU.REFERENCIA LIKE '%" + query + "%') OR ");
                        where.append("(AEAMARCA.DESCRICAO LIKE '%" + query + "%') ) OR (AEAMARCA.DESCRICAO LIKE '%"+ query +"%')) ");

                        if (pequisarProdutoEstoque){
                            where.append(" AND (AEAPLOJA.ESTOQUE_F > 0)");
                        }
                        // Limpa o listView
                        listViewProdutos.setAdapter(null);

                        if ( (loaderProdutosAsync == null) || (loaderProdutosAsync.getStatus().equals(AsyncTask.Status.FINISHED))) {
                            loaderProdutosAsync = new LoaderProdutos();
                        }
                        loaderProdutosAsync.where = where.toString();
                        loaderProdutosAsync.execute();
                        // Tira o foco da searchView e fecha o teclado virtual
                        searchView.clearFocus();

                        // Forca o fechamento do teclado virtual
                        if (viewOrcamento != null) {
                            InputMethodManager imm = (InputMethodManager) viewOrcamento.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(viewOrcamento.getWindowToken(), 0);
                        }
                        if (tipoTela == TELA_LISTA_PRODUTO) {
                            return true;
                        }
                    } else {
                        SuperActivityToast.create(getActivity(), getResources().getString(R.string.sem_descricao_para_pesquisar_produto), Style.DURATION_VERY_LONG)
                                .setTextColor(Color.WHITE)
                                .setColor(Color.RED)
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
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
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_produto_lista_tab_md_produtos_novos){
            //SuperActivityToast.create(getActivity(), "Todos os Produtos", SuperToast.Duration.LONG, Style.getStyle(Style.BLUE, SuperToast.Animations.POPUP)).show();

            // Monta a clausula where para buscar no banco de dados
            String where = "( (JULIANDAY(DATE('NOW', 'LOCALTIME')) - JULIANDAY(AEAPRODU.DT_CAD)) <= SMAEMPRE.QTD_DIAS_DESTACA_PRODUTO )";

            // Limpa o listView
            listViewProdutos.setAdapter(null);

            if (loaderProdutosAsync == null) {
                loaderProdutosAsync = new LoaderProdutos();
            }
            if (loaderProdutosAsync.getStatus().equals(AsyncTask.Status.RUNNING)){
                loaderProdutosAsync.cancel(true);
            }
            loaderProdutosAsync.where = where;
            loaderProdutosAsync.execute();

        } else if (item.getItemId() == R.id.menu_produto_lista_tab_md_produtos_promocao){
            // Limpa o listView
            listViewProdutos.setAdapter(null);

            if (loaderProdutosAsync == null) {
                loaderProdutosAsync = new LoaderProdutos();
            }
            if ((loaderProdutosAsync.getStatus().equals(AsyncTask.Status.RUNNING))){
                loaderProdutosAsync.cancel(true);
            }
            loaderProdutosAsync.execute();

        } else if (item.getItemId() == R.id.menu_produto_lista_tab_md_legenda){
            Intent intent = new Intent(getContext(), LegendaProdutoListaMDActivity.class);
            startActivity(intent);

        } else if (item.getItemId() == R.id.menu_produto_lista_tab_md_produtos_sem_estoque){
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
            funcoes.setValorXml(funcoes.TAG_PESQUISA_PRODUTO_ESTOQUE, "N");
            pequisarProdutoEstoque = false;

        } else if (item.getItemId() == R.id.menu_produto_lista_tab_md_produtos_com_estoque){
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
            funcoes.setValorXml(funcoes.TAG_PESQUISA_PRODUTO_ESTOQUE, "S");
            pequisarProdutoEstoque = true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                // Pega a posicao da lista de produtos
                int posicao = data.getExtras().getInt("POSICAO");

                // Informa que o produto esta em um orcamento
                listAeaploja.get(posicao).setEstaNoOrcamento(data.getExtras().getString("RESULTADO"));
                this.idItemOrcamento = data.getExtras().getLong("ID_AEAITORC");

                adapterListaProdutos.notifyDataSetChanged();
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
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU, produtoVendaClicado.getAeaprodu().getIdAeaprodu());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAORCAM, orcamento.getIdOrcamento());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_CFACLIFO, orcamento.getIdPessoa());
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_RAZAO_SOCIAL, orcamento.getNomeRazao());
                    //dadosParametro.putExtra("POSICAO", position);
                    dadosParametro.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAITORC, produtoVendaClicado.getAeaprodu().getIdAeaprodu());
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

            // Checa se tem o codigo do cliente
            if (idCliente != null && !idCliente.isEmpty()){
                String selectCidadeCliente = "(AEAPRREC.ID_CFACIDAD = (SELECT CFAENDER.ID_CFAENDER FROM CFAENDER WHERE (CFAENDER.ID_CFACLIFO = " + idCliente + ") LIMIT 1)) ";
                // Pega a lista de cidades
                adapterFiltroCidade.setListaCidade(produtoRotinas.listaCidadesMaisVendidos(selectCidadeCliente));
            } else {
                // Pega a lista de cidades
                adapterFiltroCidade.setListaCidade(produtoRotinas.listaCidadesMaisVendidos(null));
            }

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
        orcamento.setTipoVenda((atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);

        return orcamento;
    }

    public class LoaderProdutos extends AsyncTask<Void, Void, Void> {
        String where = "";
        AreaBeans area;
        CidadeBeans cidade;


        public LoaderProdutos() {

        }

        // Aqui eh o que acontece antes da tarefa principal ser executado
        @Override
        protected void onPreExecute() {
            // o progressBar agora eh setado como visivel
            progressBarListaProdutos.setVisibility(View.VISIBLE);

            if ( (spinnerFiltro != null) && (spinnerFiltro.getCount() > 0) && (spinnerFiltro.getAdapter() != null) ){
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
                        listAeaploja = produtoRotinas.listaProduto(where, idOrcamento, null, null, progressBarListaProdutos, null);
                    } else {
                        // Cria a lista de produto sem verificar se o produto existe no orcamento
                        listAeaploja = produtoRotinas.listaProduto(where, null, null, null, progressBarListaProdutos, null);
                    }
                // Checa a tela que esta chamando esta funcao (Mais Vendidos por area)
                } else if (tipoTela == TELA_MAIS_VENDIDOS_AREA) {
                    // Checa se nao pegou uma selecao vazia
                    if (area != null){
                        // Checa se eh a opcao de nenhum selecionado
                        if (area.getDescricaoArea().contains(getString(R.string.todos))){

                            // Pega a lista de produtos baseado na opcao selecionada
                            listAeaploja = produtoRotinas.listaProduto(where, idOrcamento, TELA_MAIS_VENDIDOS_AREA, null, progressBarListaProdutos, null);

                        } else if ( (!area.getDescricaoArea().contains(getString(R.string.todos))) &&
                                    (!area.getDescricaoArea().contains(getString(R.string.nenhuma_opcao_encontrada))) &&
                                    (!area.getDescricaoArea().contains(getString(R.string.selecione_uma_opcao))) ){

                            ContentValues filtro = new ContentValues();
                            filtro.put(String.valueOf(TELA_MAIS_VENDIDOS_AREA), String.valueOf(area.getIdArea()));
                            // Pega a lista de produtos baseado na opcao selecionada
                            //listaProdutos = produtoRotinas.listaProdutoMaisVendido(TELA_MAIS_VENDIDOS_AREA, filtro, null, null, idOrcamento, progressBarListaProdutos, null);
                            listAeaploja = produtoRotinas.listaProduto(where, idOrcamento, TELA_MAIS_VENDIDOS_AREA, filtro, progressBarListaProdutos, null);
                        }
                    }
                } else if (tipoTela == TELA_MAIS_VENDIDOS_CIDADE){
                    if (cidade != null) {

                        if (cidade.getDescricao().contains(getString(R.string.todos))) {

                            // Pega a lista de produtos baseado na opcao selecionada
                            listAeaploja = produtoRotinas.listaProduto(where, idOrcamento, TELA_MAIS_VENDIDOS_CIDADE, null, progressBarListaProdutos, null);

                        } else if ( (!cidade.getDescricao().contains(getString(R.string.todos))) && (!cidade.getDescricao().contains(getString(R.string.nenhuma_opcao_encontrada))) &&
                                (!cidade.getDescricao().contains(getString(R.string.selecione_uma_opcao))) ){

                            ContentValues filtro = new ContentValues();
                            filtro.put(String.valueOf(TELA_MAIS_VENDIDOS_CIDADE), String.valueOf(cidade.getIdCidade()));
                            // Pega a lista de produtos baseado na opcao selecionada
                            listAeaploja = produtoRotinas.listaProduto(where, idOrcamento, TELA_MAIS_VENDIDOS_CIDADE, filtro, progressBarListaProdutos, null);
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
                    //listaProdutos = produtoRotinas.listaProdutoMaisVendido(tipoTela, filtro, null, null, idOrcamento, progressBarListaProdutos, null);
                    listAeaploja = produtoRotinas.listaProduto(where, idOrcamento, tipoTela, filtro, progressBarListaProdutos, null);
                }
                // Checa se a lista de produtos nao esta vazia e nem nula
                if ((listAeaploja != null) && (listAeaploja.size() > 0)){
                    // Instancia o adapter e o seu tipo(produto)
                    adapterListaProdutos = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.PRODUTO);
                    // Seta a lista de produtos no adapter
                    adapterListaProdutos.setListAeaploja(listAeaploja);
                    // Informa o tipo da venda (atacado ou varejo)
                    adapterListaProdutos.setAtacadoVarejo((atacadoVarejo != null) ? atacadoVarejo : atacadoVarejoAuxiliar);

                } else {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            //tirando o ProgressBar da nossa tela
                            progressBarListaProdutos.setVisibility(View.GONE);

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
            } catch (final Exception e) {
                //tirando o ProgressBar da nossa tela
                progressBarListaProdutos.setVisibility(View.GONE);

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.produtos)
                                .content("Erro ao carregar os dados do produto. \n" + e.getMessage())
                                .positiveText(android.R.string.ok)
                                //.negativeText(R.string.disagree)
                                .autoDismiss(true)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if ( (listAeaploja != null) && (listAeaploja.size() > 0) ) {
                // Preenche a listView com os produtos buscados
                listViewProdutos.setAdapter(adapterListaProdutos);

                loaderCalculaPrecoSP = new LoaderCalculaPrecoSP(getContext());
                loaderCalculaPrecoSP.execute();
            }
            progressBarListaProdutos.setIndeterminate(true);

            pesquisando = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            //tirando o ProgressBar da nossa tela
            progressBarListaProdutos.setVisibility(View.GONE);

            // Chega se a Thread esta ativa pra poder cancela-la
            if ( (loaderCalculaPrecoSP != null) && (loaderCalculaPrecoSP.getStatus().equals(AsyncTask.Status.RUNNING)) ){
                loaderCalculaPrecoSP.cancel(true);
            }
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
                if (funcoes.getValorXml(funcoes.TAG_IMAGEM_PRODUTO).equalsIgnoreCase("S")){
                    // Checa se tem alguma lista de produtos preenchida
                    if ( (adapterListaProdutos.getListAeaploja() != null) && (adapterListaProdutos.getListAeaploja().size() > 0) ){
                        // Passa por todos os produtos para pesquisar se tem imagem de cada produto
                        for (int i = 0; i < adapterListaProdutos.getListAeaploja().size(); i++){
                            FotoRotinas fotoRotinas = new FotoRotinas(context);

                            CfafotosBeans fotoProduto = fotoRotinas.fotoIdProtudo(String.valueOf(adapterListaProdutos.getListAeaploja().get(i).getAeaprodu().getIdAeaprodu()));
                            // Checa se tem alguma foto
                            if ((fotoProduto != null) && (fotoProduto.getFoto().length > 0)){
                                // Atualiza o adapte com a foto do produto
                                adapterListaProdutos.getListAeaploja().get(i).getAeaprodu().setCfafotos(fotoProduto);
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
            } catch (final Exception e){
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("ProdutoListaMDFragmento")
                                .content(getResources().getString(R.string.nao_consegimos_carregar_imagem_produtos) + " \n" + e.getMessage())
                                .positiveText(android.R.string.ok)
                                //.negativeText(R.string.disagree)
                                .autoDismiss(true)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if ((listAeaploja != null) && (listAeaploja.size() > 0)) {
                loaderChecaProdutoOrcamento = new LoaderChecaProdutoOrcamento(context);
                loaderChecaProdutoOrcamento.execute();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            // Chega se a Thread esta ativa pra poder cancela-la
            if ( (loaderChecaProdutoOrcamento != null) && (loaderChecaProdutoOrcamento.getStatus().equals(AsyncTask.Status.RUNNING)) ){
                loaderChecaProdutoOrcamento.cancel(true);
            }
        }
    } // Fim LoaderImagemProdutos


    public class LoaderCalculaPrecoSP extends AsyncTask<Void, Void, Void> {
        private Context context;

        public LoaderCalculaPrecoSP(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Checa se tem alguma lista de produtos preenchida
                if ( (adapterListaProdutos.getListAeaploja() != null) && (adapterListaProdutos.getListAeaploja().size() > 0) ){
                    int idPlPgto = 0;
                    CalculaPrecoSP calculaPrecoSP = new CalculaPrecoSP(context, null, null);
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                    EmpresaSql empresaSql = new EmpresaSql(context);
                    Cursor dadosPlpgt = empresaSql.query("ID_SMAEMPRE = " + funcoes.getValorXml(funcoes.TAG_CODIGO_EMPRESA));

                    // Checa se retornou algum dado da tabela SMAEMPRE
                    if (dadosPlpgt != null && dadosPlpgt.getCount() > 0){
                        dadosPlpgt.moveToFirst();
                        // Checa se eh Atacado
                        if (atacadoVarejo.equalsIgnoreCase("0")){
                            idPlPgto = dadosPlpgt.getInt(dadosPlpgt.getColumnIndex("ID_AEAPLPGT_ATAC"));
                            // Checa se eh Varejo
                        } else if (atacadoVarejo.equalsIgnoreCase("1")){
                            idPlPgto = dadosPlpgt.getInt(dadosPlpgt.getColumnIndex("ID_AEAPLPGT_VARE"));
                        }
                    }
                    if (idPlPgto == 0){
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(context)
                                        .title("ProdutoListaMDFragment")
                                        .content("Não tem plano de pagamento cadastrado na empresa para vendas no " + (atacadoVarejo.equalsIgnoreCase("0") ? "Atacado" : "Varejo") )
                                        .positiveText(android.R.string.ok)
                                        //.negativeText(R.string.disagree)
                                        .autoDismiss(true)
                                        .show();
                            }
                        });
                    } else {
                        // Passa por todos os produtos para calcular o preco do mesmo
                        for (int i = 0; i < adapterListaProdutos.getListAeaploja().size(); i++) {
                            AeaembalBeans aeaembal = new EmbalagemRotinas(context).selectAeaembal(0,
                                    adapterListaProdutos.getListAeaploja().get(i).getAeaprodu().getIdAeaprodu(),
                                    adapterListaProdutos.getListAeaploja().get(i).getAeaprodu().getAeaunven().getIdAeaunven());
                            ContentValues retornoPreco = calculaPrecoSP.execute(
                                    adapterListaProdutos.getListAeaploja().get(i).getIdAeaploja(),
                                    ((aeaembal != null) ? aeaembal.getIdAeaembal() : 0),
                                    idPlPgto,
                                    ((idCliente != null && idCliente.length() > 0) ? Integer.parseInt(idCliente) : 0),
                                    (!funcoes.getValorXml(funcoes.TAG_CODIGO_USUARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO) ? Integer.parseInt(funcoes.getValorXml(funcoes.TAG_CODIGO_USUARIO)) : 0),
                                    adapterListaProdutos.getListAeaploja().get(i).getDataAtual(),
                                    adapterListaProdutos.getListAeaploja().get(i).getVendaAtac(),
                                    adapterListaProdutos.getListAeaploja().get(i).getVendaVare());

                            if (retornoPreco != null) {
                                adapterListaProdutos.getListAeaploja().get(i).setVendaAtac(retornoPreco.getAsDouble(CalculaPrecoSP.KEY_PRECO_ATACADO));
                                adapterListaProdutos.getListAeaploja().get(i).setVendaVare(retornoPreco.getAsDouble(CalculaPrecoSP.KEY_PRECO_VAREJO));
                                adapterListaProdutos.getListAeaploja().get(i).setProdutoPromocaoAtacado(retornoPreco.getAsString(CalculaPrecoSP.KEY_PRODUTO_PROMOCAO_ATACADO));
                                adapterListaProdutos.getListAeaploja().get(i).setProdutoPromocaoVarejo(retornoPreco.getAsString(CalculaPrecoSP.KEY_PRODUTO_PROMOCAO_VAREJO));
                                adapterListaProdutos.getListAeaploja().get(i).setProdutoPromocaoServico(retornoPreco.getAsString(CalculaPrecoSP.KEY_PRODUTO_PROMOCAO_SERVICO));
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
            } catch (final Exception e){
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("ProdutoListaMDFragmento")
                                .content(e.getMessage())
                                .positiveText(android.R.string.ok)
                                //.negativeText(R.string.disagree)
                                .autoDismiss(true)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if ((listAeaploja != null) && (listAeaploja.size() > 0)){
                loaderCarregarImagemProduto = new LoaderImagemProdutos(getContext());
                loaderCarregarImagemProduto.execute();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            // Chega se a Thread esta ativa pra poder cancela-la
            if ( (loaderCarregarImagemProduto != null) && (loaderCarregarImagemProduto.getStatus().equals(AsyncTask.Status.RUNNING)) ){
                loaderCarregarImagemProduto.cancel(true);
            }
        }
    } // Fim LoaderCalculaPrecoSP

    public class LoaderChecaProdutoOrcamento extends AsyncTask<Void, Void, Void> {
        private Context context;

        public LoaderChecaProdutoOrcamento(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Checa se tem alguma lista de produtos preenchida
                if ( (adapterListaProdutos.getListAeaploja() != null) && (adapterListaProdutos.getListAeaploja().size() > 0) ){

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                    EmpresaRotinas empresaRotinas = new EmpresaRotinas(context);
                    // Pega os dados da emrpesa
                    EmpresaBeans empresa = empresaRotinas.empresa(funcoes.getValorXml(FuncoesPersonalizadas.TAG_CODIGO_EMPRESA));

                    // Passa por todos os produtos para calcular o preco do mesmo
                    for (int i = 0; i < adapterListaProdutos.getListAeaploja().size(); i++){
                        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);
                        // Checa se o produto esta dentro do orcamento
                        if (orcamentoRotinas.selectItemOrcamento(idOrcamento, String.valueOf(adapterListaProdutos.getListAeaploja().get(i).getAeaprodu().getIdAeaprodu())) != null){
                            adapterListaProdutos.getListAeaploja().get(i).setEstaNoOrcamento("1");
                        }
                        // Checa a diferenca de dadta para ver se eh um produto novo
                        if ( (Integer.parseInt(funcoes.diferencaEntreData(funcoes.DIAS, adapterListaProdutos.getListAeaploja().get(i).getAeaprodu().getDtCad(), adapterListaProdutos.getListAeaploja().get(i).getDataAtual()))) <= empresa.getQuantidadeDiasDestacaProduto() ){
                            adapterListaProdutos.getListAeaploja().get(i).setProdutoNovo("1");
                        }

                    }
                    // Envia um sinal para o adapter atualizar
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            adapterListaProdutos.notifyDataSetChanged();
                        }
                    });
                }
            } catch (final Exception e){
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("ProdutoListaMDFragmento")
                                .content(e.getMessage())
                                .positiveText(android.R.string.ok)
                                //.negativeText(R.string.disagree)
                                .autoDismiss(true)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //tirando o ProgressBar da nossa tela
            progressBarListaProdutos.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            //tirando o ProgressBar da nossa tela
            progressBarListaProdutos.setVisibility(View.GONE);
        }
    }
}

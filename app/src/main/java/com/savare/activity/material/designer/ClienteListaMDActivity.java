package com.savare.activity.material.designer;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.activity.fragment.ClienteCadastroFragment;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.adapter.PessoaAdapter;
import com.savare.beans.CidadeBeans;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 02/02/2016.
 */
public class ClienteListaMDActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private ListView listViewPessoa;
    private FloatingActionButton itemMenuNovoCliente;
    private FloatingActionMenu menuFloating;
    private Spinner spinnerListaCidade;
    private ProgressBar progressBarStatus;
    private List<PessoaBeans> listaPessoas;
    private String telaChamou,
            idOrcamento;
    private Toolbar toolbarCabecalho;
    private boolean pesquisando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_lista_md);

        recuperaCampo();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {

            this.telaChamou = intentParametro.getString(ListaOrcamentoPedidoMDActivity.KEY_TELA_CHAMADA);

            if (telaChamou.equals(OrcamentoTabFragmentMDActivity.KEY_TELA_ORCAMENTO_FRAGMENTO)) {
                idOrcamento = intentParametro.getString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO);
            }
        }

        menuFloating.setVisibility(View.GONE);
        itemMenuNovoCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre a tela inicial do sistema
                Intent intentNovo = new Intent(ClienteListaMDActivity.this, ClienteCadastroFragment.class);
                startActivity(intentNovo);
            }
        });

        spinnerListaCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                CidadeBeans cidadeBeans = (CidadeBeans) parent.getSelectedItem();

                if (cidadeBeans.getDescricao().contains(PessoaRotinas.KEY_SELECIONE_CIDADE)){
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaMDActivity.this);
                    String idClifoVista = funcoes.getValorXml(funcoes.TAG_ID_CFACLIFO_VISTA);

                    StringBuilder where = new StringBuilder();
                    // Checa se retornou o id de cliente a vista
                    if ((idClifoVista != null) && (!idClifoVista.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
                        // Monta uma clausula para buscar o cliente
                        where.append(" (CFACLIFO.ID_CFACLIFO = ").append(idClifoVista).append(") ");

                        LoaderPessoa carregarListaPessoa = new LoaderPessoa(ClienteListaMDActivity.this, cidadeBeans, where.toString(), PessoaRotinas.NAO);
                        carregarListaPessoa.execute();
                    }
                    // Checa se tem alguma lista de cidade
                } else if ((!cidadeBeans.getDescricao().contains(PessoaRotinas.KEY_NENHUM_VALOR))) {

                    LoaderPessoa carregarListaPessoa = new LoaderPessoa(ClienteListaMDActivity.this, cidadeBeans, null, PessoaRotinas.NAO);
                    carregarListaPessoa.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listViewPessoa.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if ((telaChamou != null) && (telaChamou.equals(ListaOrcamentoPedidoMDActivity.KEY_TELA_LISTA_ORCAMENTO_PEDIDO))) {

                    PessoaBeans pessoa = new PessoaBeans();
                    pessoa = (PessoaBeans) listViewPessoa.getAdapter().getItem(position);

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaMDActivity.this);

                    // Cria uma intent para returnar um valor para activity ProdutoLista
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
                    returnIntent.putExtra("ID_CFAESTAD", String.valueOf(pessoa.getEstadoPessoa().getCodigoEstado()));
                    returnIntent.putExtra("ID_CFACIDAD", String.valueOf(pessoa.getCidadePessoa().getIdCidade()));
                    returnIntent.putExtra("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
                    returnIntent.putExtra("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
                    returnIntent.putExtra("NOME_CLIENTE", pessoa.getNomeRazao());
                    returnIntent.putExtra("IE_RG_CLIENTE", pessoa.getIeRg());
                    returnIntent.putExtra("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
                    returnIntent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
                    returnIntent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
                    returnIntent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
                    returnIntent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
                    returnIntent.putExtra("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
                    returnIntent.putExtra("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
                    returnIntent.putExtra("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
                    if (pessoa.isCadastroNovo()) {
                        returnIntent.putExtra("CADASTRO_NOVO", "S");
                    }
                    setResult(ListaOrcamentoPedidoMDActivity.RETORNA_CLIENTE, returnIntent);
                    // Fecha a tela de detalhes de produto
                    finish();

                } else if ((telaChamou != null) && (telaChamou.equals(OrcamentoTabFragmentMDActivity.KEY_TELA_ORCAMENTO_FRAGMENTO))) {

                    PessoaBeans pessoa = new PessoaBeans();
                    pessoa = (PessoaBeans) listViewPessoa.getAdapter().getItem(position);

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaMDActivity.this);
                    // Preenche o ContentValues com os dados da pessoa
                    ContentValues dadosCliente = new ContentValues();
                    dadosCliente.put("ID_CFACLIFO", pessoa.getIdPessoa());
                    dadosCliente.put("ID_CFAESTAD", pessoa.getEstadoPessoa().getCodigoEstado());
                    dadosCliente.put("ID_CFACIDAD", pessoa.getCidadePessoa().getIdCidade());
                    dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
                    dadosCliente.put("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
                    dadosCliente.put("NOME_CLIENTE", pessoa.getNomeRazao());
                    dadosCliente.put("IE_RG_CLIENTE", pessoa.getIeRg());
                    dadosCliente.put("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
                    dadosCliente.put("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
                    dadosCliente.put("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
                    dadosCliente.put("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
                    if (pessoa.isCadastroNovo()) {
                        dadosCliente.put("CADASTRO_NOVO", "S");
                    }
                    OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ClienteListaMDActivity.this);
                    // Atualiza o cliente do orcamento
                    int qtdAlterado = orcamentoRotinas.updateOrcamento(dadosCliente, idOrcamento);

                    // Checa se atualizou algum orcamento
                    if (qtdAlterado > 0) {

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("NOME_CLIENTE", pessoa.getNomeRazao());
                        returnIntent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
                        returnIntent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
                        returnIntent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
                        returnIntent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
                        returnIntent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));

                        setResult(OrcamentoTabFragmentMDActivity.RETORNA_CLIENTE, returnIntent);
                        // Fecha a tela de detalhes de produto
                        finish();

                    } else {
                        setResult(OrcamentoTabFragmentMDActivity.ERRO_RETORNA_CLIENTE);
                        // Fecha a tela de detalhes de produto
                        finish();
                    }

                } else {
                    //Pega os dados da pessoa que foi clicado
                    PessoaBeans pessoa = (PessoaBeans) parent.getItemAtPosition(position);

                    // Abre a tela inicial do sistema
                    Intent intent = new Intent(ClienteListaMDActivity.this, ClienteDetalhesMDActivity.class);
                    intent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
                    intent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
                    intent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
                    intent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
                    intent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
                    if (pessoa.isCadastroNovo()) {
                        intent.putExtra("CADASTRO_NOVO", "S");
                    }
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        carregarListaCidades();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cliente_lista_md, menu);

        // Configuração associando item de pesquisa com a SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_cliente_lista_md_search_pesquisar).getActionView();
        searchView.setQueryHint("Pesquisar");

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            /**
             * Botao para submeter a pesquisa.
             * So eh executado quando clicado no botao.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Checa se tem pelo menos 3 caracteres do para pesquisar ou apenas numero
                if ( (query.length() > 3) || (query.matches("[-+]?\\d*\\.?\\d+")) ) {
                    if (!pesquisando) {

                        pesquisando = true;
                        // Remove todos os caracteres que nao for numero
                        String cpfCnpj = query.replaceAll("\\D", "");
                        // Checa se eh um cpf
                        if (cpfCnpj.length() == 11) {
                            // Formata no modelo de cpf
                            cpfCnpj = cpfCnpj.replaceAll("([0-9]{3})([0-9]{3})([0-9]{3})([0-9]{2})", "$1\\.$2\\.$3-$4");
                        } else if (cpfCnpj.length() == 14) {
                            // Formata no modelo de cnpj
                            cpfCnpj = cpfCnpj.replaceAll("([0-9]{2})([0-9]{3})([0-9]{3})([0-9]{4})([0-9]{2})", "$1\\.$2\\.$3/$4-$5");
                        } else {
                            cpfCnpj = query;
                        }
                        String queryNumber = "";
                        // Checa se eh apenas numero
                        if (query.matches("[-+]?\\d*\\.?\\d+")){
                            queryNumber = query;
                        }
                        query = query.toUpperCase();
                        StringBuilder where = new StringBuilder();
                        String tipoFuncionario = new FuncoesPersonalizadas(ClienteListaMDActivity.this).getValorXml(FuncoesPersonalizadas.TAG_TIPO_FUNCIONARIO);
                        // Checa se eh um funcionario interno
                        if ((!tipoFuncionario.equalsIgnoreCase(FuncoesPersonalizadas.NAO_ENCONTRADO)) && (tipoFuncionario.contains("2"))){
                            where.append("CFACLIFO.NOME_RAZAO LIKE '%" + query + "%' OR ");
                            where.append("CFACLIFO.NOME_FANTASIA LIKE '%" + query + "%' OR ");
                            where.append("CFACLIFO.CPF_CGC LIKE '%" + cpfCnpj + "%' ");
                            if (queryNumber.length() > 0) {
                                where.append(" OR CFACLIFO.ID_CFACLIFO = " + queryNumber + " OR CFACLIFO.CODIGO_CLI = " + queryNumber);
                            }
                        } else {
                            where.append("CFACLIFO.NOME_RAZAO LIKE '%" + query + "%' OR ");
                            where.append("CFACLIFO.NOME_FANTASIA LIKE '%" + query + "%' OR ");
                            where.append("CFACLIFO.CPF_CNPJ LIKE '%" + cpfCnpj + "%' OR ");
                            where.append(queryNumber.length() > 0 ? "CFACLIFO.ID_CFACLIFO = " + queryNumber + " OR " : "");
                            where.append(queryNumber.length() > 0 ? "CFACLIFO.CODIGO_CLI = " + queryNumber + " OR " : "");
                            where.append("CFACIDAD.DESCRICAO LIKE '%" + query + "%' OR ");
                            where.append("CFAENDER.BAIRRO LIKE '%" + query + "%' OR ");
                            where.append("CFASTATU.DESCRICAO LIKE '%" + query + "%' ");
                        }
                        //PessoaRotinas pessoaRotinas = new PessoaRotinas(ClienteListaMDActivity.this);

                        CidadeBeans cidade = (CidadeBeans) spinnerListaCidade.getSelectedItem();

                        LoaderPessoa carregaListaPessoa = new LoaderPessoa(ClienteListaMDActivity.this, cidade, where.toString(), PessoaRotinas.SIM);
                        carregaListaPessoa.execute();
                    }
                } else {
                    SuperActivityToast.create(ClienteListaMDActivity.this, getResources().getString(R.string.digite_minimo_letras), Style.DURATION_LONG)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
                return false;
            } // Fim do onQueryTextSubmit

            /**
             * Pega todo o texto digitado
             */
            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            } // Fim do onQueryTextChange


            //OnQueryTextListener

        }); // Fim do setOnQueryTextListener


        return super.onCreateOptionsMenu(menu);
    } // Fim do onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void carregarListaCidades() {
        List<CidadeBeans> listaCidade = new ArrayList<CidadeBeans>();

        PessoaRotinas pessoaRotinas = new PessoaRotinas(ClienteListaMDActivity.this);

        listaCidade = pessoaRotinas.listaCidadeCliente(PessoaRotinas.KEY_TIPO_CLIENTE);

        if ((listaCidade != null) && (listaCidade.size() > 0)) {

            ItemUniversalAdapter adapterListaCidade = new ItemUniversalAdapter(ClienteListaMDActivity.this, ItemUniversalAdapter.CIDADE_DARK);
            adapterListaCidade.setListaCidade(listaCidade);

            spinnerListaCidade.setAdapter(adapterListaCidade);
        }
    }

    private void recuperaCampo() {
        textViewStatus = (TextView) findViewById(R.id.activity_cliente_lista_md_textViewStatus);
        listViewPessoa = (ListView) findViewById(R.id.activity_cliente_lista_md_list_pessoa);
        itemMenuNovoCliente = (FloatingActionButton) findViewById(R.id.activity_cliente_lista_md_novo_cliente);
        menuFloating = (FloatingActionMenu) findViewById(R.id.activity_cliente_lista_md_menu_float);
        spinnerListaCidade = (Spinner) findViewById(R.id.activity_cliente_lista_md_spinner_cidades);
        progressBarStatus = (ProgressBar) findViewById(R.id.activity_cliente_lista_md_progressBar_status);

        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_cliente_lista_md_toolbar_cabecalho);
        // Adiciona uma titulo para toolbar
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.app_name));
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        //toolbarInicio.setLogo(R.mipmap.ic_launcher);
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
    }


    public class LoaderPessoa extends AsyncTask<Void, Void, Void> {

        private Context context;
        private CidadeBeans cidade;
        private String where, usaWebservice;
        PessoaAdapter adapterPessoa;

        public LoaderPessoa(Context context, CidadeBeans cidade, String where, String usaWebservice) {
            this.context = context;
            this.cidade = cidade;
            this.where = where;
            this.usaWebservice = usaWebservice != null && usaWebservice.length() > 0 ? usaWebservice : PessoaRotinas.NAO;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
            // Verifica se tem alguma coisa no listView
            if ((listViewPessoa.getAdapter() != null) && (listViewPessoa.getAdapter().getCount() > 0)) {
                listViewPessoa.setAdapter(null);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Cria a variavel para montar a clausula where do sql
                String whereAux = null;

                PessoaRotinas pessoaRotinas = new PessoaRotinas(context);

                if (cidade.getDescricao().equalsIgnoreCase(PessoaRotinas.KEY_SELECIONE_CIDADE)) {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
                    String tipoFuncionario = funcoes.getValorXml(funcoes.TAG_TIPO_FUNCIONARIO);
                    // Checa se o vendedor eh interno
                    if ((!tipoFuncionario.equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) && (tipoFuncionario.contains("2")) && (this.usaWebservice.equalsIgnoreCase(PessoaRotinas.SIM))) {
                        listaPessoas = pessoaRotinas.listaPessaResumidoWebservice(where, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                    } else {
                        listaPessoas = pessoaRotinas.listaPessoaResumido(where, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                    }
                // Faz uma pesquisa no nome que foi digitado levando em consideracao a cidade selecionada
                } else if ((!cidade.getDescricao().equalsIgnoreCase(PessoaRotinas.KEY_NENHUM_VALOR)) &&
                        (!cidade.getDescricao().equalsIgnoreCase("Todas as Cidades")) &&
                        (!cidade.getDescricao().equalsIgnoreCase(getResources().getString(R.string.sem_cidade)))) {

                    // Monta a clausula where do sql
                    whereAux = "( CFACIDAD.DESCRICAO LIKE '%" + cidade.getDescricao().replace("'", "%") + "%' ) ";
                    if ((where != null) && (where.length() > 1)) {
                        whereAux += " AND " + where;
                    }

                    // Cria a lista com as pessoas de acordo com a cidade selecionada
                    listaPessoas = pessoaRotinas.listaPessoaResumido(whereAux, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);

                } else if (cidade.getDescricao().equalsIgnoreCase(getResources().getString(R.string.sem_cidade))) {
                    whereAux = "( (CFACIDAD.DESCRICAO IS NULL) OR (CFACIDAD.DESCRICAO = '') ) ";

                    if ((where != null) && (where.length() > 1)) {
                        whereAux = where;
                    }

                    // Cria a lista com as pessoas de acordo com a cidade selecionada
                    listaPessoas = pessoaRotinas.listaPessoaResumido(whereAux, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                } else if (cidade.getDescricao().equalsIgnoreCase("Todas as Cidades")) {

                    if ((where != null) && (where.length() > 1)) {
                        // Preenche a lista de pessoas
                        listaPessoas = pessoaRotinas.listaPessoaResumido(where, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                    } else {
                        // Preenche a lista de pessoas
                        listaPessoas = pessoaRotinas.listaPessoaResumido(null, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                    }
                }
                if ((listaPessoas != null) && (listaPessoas.size() > 0)) {
                    // Seta o adapter com a nova lista
                    adapterPessoa = new PessoaAdapter(context, listaPessoas, PessoaAdapter.KEY_CLIENTE);

                }
            } catch (Exception e) {

                new MaterialDialog.Builder(context)
                        .title("ClienteListaMDActivity")
                        .content(context.getResources().getString(R.string.nao_conseguimos_lista_clientes) + "\n" + e.getMessage())
                        .positiveText(R.string.button_ok)
                        .show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressBarStatus.setVisibility(View.GONE);
            pesquisando = false;

            if ((adapterPessoa != null) && (adapterPessoa.getCount() > 0)) {
                // Seta o listView com o novo adapter que ja esta com a nova lista
                listViewPessoa.setAdapter(adapterPessoa);
                textViewStatus.setText(adapterPessoa.getCount() + " :Clientes ");
            } else {
                textViewStatus.setText("0 :Clientes ");
            }
        }
    }

}

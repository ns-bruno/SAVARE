package com.savare.activity.material.designer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.adapter.ClienteDetalhesFragmentAdapter;
import com.savare.banco.funcoesSql.PositivacaoSql;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.EnviarCadastroClienteFtpAsyncRotinas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bruno Nogueira Silva on 30/01/2016.
 */
public class ClienteDetalhesMDActivity extends AppCompatActivity{

    private Toolbar toolbarCabecalho;
    private FloatingActionButton itemMenuNovoOrcamento, itemMenuTitulosCliente, itemMenuEnviarDados, itemMenuPositivarCliente,
                                 itemMenuHistoricoPedidos;
    private FloatingActionMenu floatingMenu;

    private String codigoCli,
            codigoFun,
            codigoUsu,
            codigoTra,
            idCliente;
    private String clienteNovo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_cliente_detalhes_tab_md);

        recuperaCampos();

        Bundle intentParametro = getIntent().getExtras();
        // Checa se foi passado algum parametro
        if (intentParametro != null) {
            // Pega os codigo internos da pessoa
            codigoCli = intentParametro.getString("CODIGO_CLI");
            codigoFun = intentParametro.getString("CODIGO_FUN");
            codigoTra = intentParametro.getString("CODIGO_TRA");
            codigoUsu = intentParametro.getString("CODIGO_USU");
            // Pega o id do cliente
            idCliente = intentParametro.getString("ID_CFACLIFO");
            clienteNovo = intentParametro.getString("CADASTRO_NOVO");

            if ((idCliente != null) && (Integer.parseInt(idCliente) > 0)){
                itemMenuEnviarDados.setVisibility(View.GONE);
            }
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_cliente_detalhes_tab_md_pager);

        ClienteDetalhesFragmentAdapter clienteDetalhesAdapter = new ClienteDetalhesFragmentAdapter(getSupportFragmentManager(), getApplicationContext(), intentParametro);

        viewPager.setAdapter(clienteDetalhesAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.fragment_cliente_detalhes_tab_md_tab_layout);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);
        Context context = ClienteDetalhesMDActivity.this;

        String tipoFuncionario = new FuncoesPersonalizadas(context).getValorXml(FuncoesPersonalizadas.TAG_TIPO_FUNCIONARIO);
        // Checa se eh um vendedor interno
        if ( (tipoFuncionario != null) && (!tipoFuncionario.equalsIgnoreCase(FuncoesPersonalizadas.NAO_ENCONTRADO)) && (tipoFuncionario.equalsIgnoreCase("2"))){
            // Instancia a classe de funcoes sql para pessoa
            PessoaRotinas pessoaRotinas = new PessoaRotinas(context);

            List<PessoaBeans> listaPessoa = null;
            // Pega os dados de uma pessoa especifica
            listaPessoa = pessoaRotinas.listaPessoaResumido("CODIGO_CLI = " + codigoCli, PessoaRotinas.KEY_TIPO_CLIENTE, null);

            if ((listaPessoa == null) || (listaPessoa.size() == 0)){
                floatingMenu.setVisibility(View.GONE);
            }

        }

        itemMenuNovoOrcamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instancia a classe de funcoes sql para pessoa
                PessoaRotinas pessoaRotinas = new PessoaRotinas(getApplicationContext());

                PessoaBeans pessoa = null;
                String where = "CODIGO_CLI = " + codigoCli;
                // Pega os dados de uma pessoa especifica
                pessoa = pessoaRotinas.listaPessoaResumido(where, PessoaRotinas.KEY_TIPO_CLIENTE, null).get(0);

                // Cria um dialog para selecionar atacado ou varejo
                AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(v.getContext());
                // Atributo(variavel) para escolher o tipo da venda
                final String[] opcao = {"Atacado", "Varejo"};
                // Preenche o dialogo com o titulo e as opcoes
                final PessoaBeans finalPessoa = pessoa;
                mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if ((finalPessoa != null) && (finalPessoa.getIdPessoa() != 0) && (finalPessoa.getNomeRazao() != null)) {
                                // Instancia a classe de funcoes
                                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

                                // Preenche o ContentValues com os dados da pessoa
                                ContentValues dadosCliente = new ContentValues();
                                dadosCliente.put("ID_CFACLIFO", finalPessoa.getIdPessoa());
                                dadosCliente.put("ID_CFAESTAD", finalPessoa.getEstadoPessoa().getCodigoEstado());
                                dadosCliente.put("ID_CFACIDAD", finalPessoa.getCidadePessoa().getIdCidade());
                                dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
                                dadosCliente.put("GUID", UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16));
                                dadosCliente.put("ATAC_VAREJO", which);
                                dadosCliente.put("PESSOA_CLIENTE", String.valueOf(finalPessoa.getPessoa()));
                                dadosCliente.put("NOME_CLIENTE", finalPessoa.getNomeRazao());
                                dadosCliente.put("IE_RG_CLIENTE", finalPessoa.getIeRg());
                                dadosCliente.put("CPF_CGC_CLIENTE", finalPessoa.getCpfCnpj());
                                dadosCliente.put("ENDERECO_CLIENTE", finalPessoa.getEnderecoPessoa().getLogradouro() + ", " + finalPessoa.getEnderecoPessoa().getNumero());
                                dadosCliente.put("BAIRRO_CLIENTE", finalPessoa.getEnderecoPessoa().getBairro());
                                dadosCliente.put("CEP_CLIENTE", finalPessoa.getEnderecoPessoa().getCep());

                                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getApplicationContext());

                                // Cria um novo orcamento no banco de dados
                                long numeroOracmento = orcamentoRotinas.insertOrcamento(dadosCliente);

                                // Verifica se retornou algum numero
                                if (numeroOracmento > 0) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
                                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_NOME_RAZAO, finalPessoa.getNomeRazao());
                                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_PESSOA, String.valueOf(finalPessoa.getIdPessoa()));
                                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO, String.valueOf(which));
                                    bundle.putString("AV", "0");

                                    Intent i = new Intent(getApplicationContext(), OrcamentoTabFragmentMDActivity.class);
                                    i.putExtras(bundle);

                                    // Fecha o floatMenu
                                    floatingMenu.close(true);

                                    // Abre outra tela
                                    startActivity(i);

                                } else {
                                    // Dados da mensagem
                                    ContentValues mensagem = new ContentValues();
                                    mensagem.put("comando", 2);
                                    mensagem.put("tela", "CleinteDetalhesActivity");
                                    mensagem.put("mensagem", "Não foi possível criar orçamento");

                                    //funcoes = new FuncoesPersonalizadas(getApplicationContext());
                                    funcoes.menssagem(mensagem);
                                }
                            } else {
                                new MaterialDialog.Builder(getApplicationContext())
                                        .title("ClienteDetalhesMDActivity")
                                        .content(getResources().getString(R.string.nao_localizado_dados_pessoa) +
                                                "\n Falta os dados principais para criar um orçamento.")
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        } catch (Exception e) {
                            // Dados da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 1);
                            mensagem.put("tela", "CleinteDetalhesActivity");
                            mensagem.put("mensagem", "Não conseguimos pegar todos os dados da pessoa.");

                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());
                            funcoes.menssagem(mensagem);
                        }

                    }
                });
                // Faz a mensagem (dialog) aparecer
                mensagemAtacadoVarejo.show();
            }
        });

        itemMenuTitulosCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria uma intent para abrir uma nova activity
                Intent intentTitulos = new Intent(getApplicationContext(), ListaTitulosMDActivity.class);
                intentTitulos.putExtra("ID_CFACLIFO", idCliente);

                // Fecha o floatMenu
                floatingMenu.close(true);

                startActivity(intentTitulos);
            }
        });

        itemMenuEnviarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String where = " (CFACLIFO.ID_CFACLIFO == " + idCliente + ")";

                List<PessoaBeans> listaPessoasCadastro = new ArrayList<PessoaBeans>();

                PessoaRotinas pessoaRotinasCad = new PessoaRotinas(getApplicationContext());

                // Pega a lista de pessoas a serem enviadas os dados
                listaPessoasCadastro = pessoaRotinasCad.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);
                // Checa se retornou alguma lista
                if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0) {
                    EnviarCadastroClienteFtpAsyncRotinas enviarCadastro = new EnviarCadastroClienteFtpAsyncRotinas(getApplicationContext(), EnviarCadastroClienteFtpAsyncRotinas.TELA_CLIENTE_DETALHES);
                    // Executa o envio do cadastro em segundo plano
                    enviarCadastro.execute(listaPessoasCadastro);
                }
            }
        });

        itemMenuPositivarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fecha o menuFloating
                floatingMenu.close(true);

                new MaterialDialog.Builder(v.getContext())
                        .title(R.string.formar_venda)
                        .items(R.array.forma_venda_positivacao)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                // Valida a opcao selecionada 0 = Visitou e comprou | 1 = Visitou, mas, não comprou | 2 = Não estava | 3 = Pedido feito por telefone | 4 = Pedido feito pelo balcao/loja
                                if ((which != 0) && (which != 3) && (which != 4)) {

                                    // Pega os dados da positivacao
                                    String sqlInsert = "INSERT OR REPLACE INTO CFAPOSIT(STATUS, DATA_VISITA, ID_CFACLIFO) VALUES " +
                                            "(" + which + ", " +
                                            "(SELECT (DATE('NOW', 'localtime'))), " +
                                            idCliente + ")";

                                    PositivacaoSql positivacaoSql = new PositivacaoSql(getApplicationContext());

                                    // Inseri a positivacao e checa se inseriu com sucesso
                                    positivacaoSql.execSQL(sqlInsert);

                                    //SuperToast.create(getApplicationContext(), getResources().getString(R.string.positivado_sucesso), SuperToast.Duration.LONG, Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();

                                    SuperActivityToast.create(ClienteDetalhesMDActivity.this, getResources().getString(R.string.positivado_sucesso), Style.DURATION_LONG)
                                            .setTextColor(Color.WHITE)
                                            .setColor(Color.GREEN)
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();

                                } else {
                                    //SuperToast.create(getApplicationContext(), getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.FLYIN)).show();
                                    SuperActivityToast.create(ClienteDetalhesMDActivity.this, getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), Style.DURATION_LONG)
                                            .setTextColor(Color.WHITE)
                                            .setColor(Color.RED)
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();
                                }
                            }
                        })
                        .show();
            }
        });

        itemMenuHistoricoPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fecha o menuFloating
                floatingMenu.close(true);

                // Cria uma intent para abrir uma nova activity
                Intent intentTitulos = new Intent(getApplicationContext(), ListaOrcamentoPedidoMDActivity.class);
                intentTitulos.putExtra("ID_CFACLIFO", idCliente);
                intentTitulos.putExtra(ListaOrcamentoPedidoMDActivity.KEY_ORCAMENTO_PEDIDO, ListaOrcamentoPedidoMDActivity.TIPO_TODOS_PEDIDO);

                startActivity(intentTitulos);
            }
        });

    } // Fim onCreate

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void recuperaCampos(){
        // Recupera o campo para manipular
        toolbarCabecalho = (Toolbar) findViewById(R.id.fragment_cliente_detalhes_tab_md_toolbar_cabecalho);
        setSupportActionBar(toolbarCabecalho);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemMenuNovoOrcamento = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_novo_orcamento);
        itemMenuTitulosCliente = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_titulos_cliente);
        itemMenuEnviarDados = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_enviar_dados_cliente);
        itemMenuPositivarCliente = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_positivar_cliente);
        itemMenuHistoricoPedidos = (FloatingActionButton) findViewById(R.id.fragment_cliente_detalhes_tab_md_menu_item_historico_pedido);
        floatingMenu = (FloatingActionMenu) findViewById(R.id.afragment_cliente_detalhes_tab_md_menu_float);
    }
}

package com.savare.activity.material.designer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import com.dexafree.materialList.view.MaterialListView;
import com.github.clans.fab.FloatingActionButton;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.savare.R;
import com.savare.activity.ClienteListaActivity;
import com.savare.activity.ConfiguracoesActivity;
import com.savare.activity.ListaOrcamentoPedidoActivity;
import com.savare.activity.ListaTitulosActivity;
import com.savare.activity.LogActivity;
import com.savare.activity.SincronizacaoActivity;
import com.savare.activity.fragment.ClienteCadastroFragment;
import com.savare.activity.fragment.OrcamentoTabulacaoFragment;
import com.savare.activity.material.designer.fragment.ProdutoListaTabMD;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.EmpresaBeans;
import com.savare.beans.UsuarioBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.EmpresaRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosFtpAsyncRotinas;

import java.util.UUID;

/**
 * Created by Bruno Nogueira Silva on 04/12/2015.
 */
public class InicioMDActivity extends AppCompatActivity {

    private Toolbar toolbarInicio;
    private Drawer navegacaoDrawerEsquerdo;
    private AccountHeader cabecalhoDrawer;
    private MaterialListView mListView;
    private int cliqueVoltar = 0;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inicio_md);
        // Recupera o campo para manipular
        toolbarInicio = (Toolbar) findViewById(R.id.activity_inicio_md_toolbar_inicio);
        // Adiciona uma titulo para toolbar
        toolbarInicio.setTitle(this.getResources().getString(R.string.app_name));
        toolbarInicio.setTitleTextColor(getResources().getColor(R.color.branco));
        //toolbarInicio.setLogo(R.drawable.ic_launcher);
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarInicio);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(InicioMDActivity.this);

        UsuarioSQL usuarioSQL = new UsuarioSQL(InicioMDActivity.this);
        // Pega os dadosUsuario do usuario no banco de dadosUsuario
        Cursor dadosUsuario = usuarioSQL.query("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));

        // Pega o nome de login do usuario
        String nomeCompletoUsua = funcoes.getValorXml("Usuario");
        boolean enviaAutomatico = false;
        boolean recebeAutomatico = false;
        boolean imagemProduto = false;

        if (funcoes.getValorXml("EnviarAutomatico").equalsIgnoreCase("S")){
            enviaAutomatico = true;
        }

        if (funcoes.getValorXml("ReceberAutomatico").equalsIgnoreCase("S")){
            recebeAutomatico = true;
        }

        if (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase("S")){
            imagemProduto = true;
        }

        // Checa se retornou algum dados do usuario
        if (dadosUsuario != null && dadosUsuario.getCount() > 0){
            // move para o primeiro registro
            dadosUsuario.moveToFirst();
            // Pega o nome completo do usuario
            nomeCompletoUsua = dadosUsuario.getString(dadosUsuario.getColumnIndex("NOME_USUA"));
        }

        // Instancia o cabecalho(conta) do drawer
        cabecalhoDrawer = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorAccent)
                .addProfiles(
                        new ProfileDrawerItem().withName(nomeCompletoUsua).withEmail(funcoes.getValorXml("Email")).withIcon(getResources().getDrawable(R.mipmap.ic_account_circle)))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        //Instancia o drawer
        navegacaoDrawerEsquerdo = new DrawerBuilder()
                .withActivity(this)
            .withToolbar(toolbarInicio)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.clientes).withIcon(R.drawable.ic_action_person),
                        new PrimaryDrawerItem().withName(R.string.orcamentos).withIcon(R.drawable.ic_action_view_as_list),
                        new PrimaryDrawerItem().withName(R.string.produtos).withIcon(R.drawable.ic_action_box_produtct),
                        new PrimaryDrawerItem().withName(R.string.titulos).withIcon(R.drawable.ic_action_coins_pay),
                        new PrimaryDrawerItem().withName(R.string.pedidos_a_enviar).withIcon(R.drawable.ic_action_order),
                        new PrimaryDrawerItem().withName(R.string.pedidos_enviados).withIcon(R.drawable.ic_action_order),
                        new PrimaryDrawerItem().withName(R.string.lixeira).withIcon(R.drawable.ic_action_discard),
                        new PrimaryDrawerItem().withName(R.string.sincronizacao).withIcon(R.drawable.ic_action_cloud),
                        new PrimaryDrawerItem().withName(R.string.configuracoes).withIcon(R.drawable.ic_action_settings),
                        new SectionDrawerItem().withName(R.string.monitoramento),
                        new PrimaryDrawerItem().withName(R.string.logs).withIcon(R.drawable.ic_sim_alert),
                        new SwitchDrawerItem().withName(R.string.enviar_automatico).withIcon(R.mipmap.ic_upload).withChecked(enviaAutomatico).withOnCheckedChangeListener(mOnCheckedChangeListener).withTag("enviar"),
                        new SwitchDrawerItem().withName(R.string.receber_automatico).withIcon(R.mipmap.ic_download).withChecked(recebeAutomatico).withOnCheckedChangeListener(mOnCheckedChangeListener).withTag("receber"),
                        new SwitchDrawerItem().withName(R.string.imagem_produto).withIcon(R.mipmap.ic_image).withChecked(imagemProduto).withOnCheckedChangeListener(mOnCheckedChangeListener).withTag("imagem_produto")

                )
            .withDisplayBelowStatusBar(true)
            .withActionBarDrawerToggleAnimated(true)
            .withDrawerGravity(Gravity.LEFT)
            .withSavedInstance(savedInstanceState)
            .withSelectedItem(-1)
            .withActionBarDrawerToggle(true)
            .withAccountHeader(cabecalhoDrawer)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                    switch (position) {

                        case 1:
                            // Abre a tela de clientes
                            Intent intent = new Intent(InicioMDActivity.this, ClienteListaActivity.class);
                            startActivity(intent);
                            return true;
                        //break;

                        case 2:
                            // Abre a tela Lista de Orcamento
                            Intent intentListaOrcamentoPedido = new Intent(InicioMDActivity.this, ListaOrcamentoPedidoActivity.class);
                            // Salva um valor para transferir para outrao Activity(Tela)
                            intentListaOrcamentoPedido.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.ORCAMENTO);
                            // Abre outra tela
                            startActivity(intentListaOrcamentoPedido);
                            return true;

                        case 3:
                            // Tela de lista de produtos
                            // Cria um dialog para selecionar atacado ou varejo
                            AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(InicioMDActivity.this);
                            // Atributo(variavel) para escolher o tipo da venda
                            final String[] opcao = {"Atacado", "Varejo"};
                            // Preenche o dialogo com o titulo e as opcoes
                            mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // Cria uma intent para abrir uma nova activity
                                    //Intent intentOrcamento = new Intent(InicioMDActivity.this, ProdutoListaActivity.class);
                                    Intent intentOrcamento = new Intent(InicioMDActivity.this, ProdutoListaTabMD.class);
                                    intentOrcamento.putExtra(ProdutoListaTabMD.KEY_ATACADO_VAREJO, String.valueOf(which).toString());
                                    // Abre outra tela
                                    startActivity(intentOrcamento);

                                }
                            });

                            // Faz a mensagem (dialog) aparecer
                            mensagemAtacadoVarejo.show();
                            return true;

                        case 4:
                            // Tela de Lista de Titulos
                            Intent intentListaTitulos = new Intent(InicioMDActivity.this, ListaTitulosActivity.class);
                            // Abre outra tela
                            startActivity(intentListaTitulos);
                            return true;

                        case 5:
                            // Tela de Lista de Pedidos nao enviados
                            Intent intentListaPedido = new Intent(InicioMDActivity.this, ListaOrcamentoPedidoActivity.class);
                            // Salva um valor para transferir para outrao Activity(Tela)
                            intentListaPedido.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.PEDIDO_NAO_ENVIADO);
                            // Abre outra tela
                            startActivity(intentListaPedido);
                            return true;

                        case 6:
                            // Tela de Lista de Pedidos nao enviados
                            Intent intentListaPedidoEnviado = new Intent(InicioMDActivity.this, ListaOrcamentoPedidoActivity.class);
                            // Salva um valor para transferir para outrao Activity(Tela)
                            intentListaPedidoEnviado.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.PEDIDO_ENVIADO);
                            // Abre outra tela
                            startActivity(intentListaPedidoEnviado);
                            return true;

                        case 7:
                            // Tela de orcamentos excluidos(lixeira)
                            Intent intentListaExcluido = new Intent(InicioMDActivity.this, ListaOrcamentoPedidoActivity.class);
                            // Salva um valor para transferir para outrao Activity(Tela)
                            intentListaExcluido.putExtra("ORCAMENTO_PEDIDO", OrcamentoRotinas.EXCLUIDO);
                            // Abre outra tela
                            startActivity(intentListaExcluido);
                            return true;

                        case 8:
                            // Tela de sincronização
                            Intent intentListaSincronizacao = new Intent(InicioMDActivity.this, SincronizacaoActivity.class);
                            // Abre outra tela
                            startActivity(intentListaSincronizacao);
                            return true;

                        case 9:
                            // Tela de sincronização
                            Intent intentListaConfiguracoes = new Intent(InicioMDActivity.this, ConfiguracoesActivity.class);
                            // Abre outra tela
                            startActivity(intentListaConfiguracoes);
                            return true;

                        case 11:
                            // Tela de sincronização
                            Intent intentLogs = new Intent(InicioMDActivity.this, LogActivity.class);
                            // Abre outra tela
                            startActivity(intentLogs);
                            return true;

                        default:
                            return false;
                    }
                }
            })
            .build();
        // Recupera o campo de lista de card da activity(view)
        mListView = (MaterialListView) findViewById(R.id.activity_inicio_md_material_listview);
        // Pega os cliques dos cards
        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(Card card, int position) {
                String s = card.toString();
            }

            @Override
            public void onItemLongClick(Card card, int position) {
                String s = card.toString();
            }
        });

        //FloatingActionMenu menuFloat = (FloatingActionMenu) findViewById(R.id.activity_inicio_md_menu_float);

        FloatingActionButton itemMenuNovoOrcamento = (FloatingActionButton) findViewById(R.id.activity_inicio_md_menu_item_novo_orcamento);
        itemMenuNovoOrcamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre a tela de detalhes do produto
                Intent intent = new Intent(InicioMDActivity.this, ClienteListaActivity.class);
                intent.putExtra(ListaOrcamentoPedidoActivity.KEY_TELA_CHAMADA, ListaOrcamentoPedidoActivity.KEY_TELA_LISTA_ORCAMENTO_PEDIDO);
                // Abre a activity aquardando uma resposta
                startActivityForResult(intent, ListaOrcamentoPedidoActivity.SOLICITA_CLIENTE);
            }
        });

        FloatingActionButton itemMenuNovoCliente = (FloatingActionButton) findViewById(R.id.activity_inicio_md_menu_item_novo_cliente);
        itemMenuNovoCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre a tela inicial do sistema
                Intent intentNovo = new Intent(InicioMDActivity.this, ClienteCadastroFragment.class);
                startActivity(intentNovo);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        navegacaoDrawerEsquerdo.setSelectionAtPosition(-1);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(InicioMDActivity.this);

        if (!funcoes.getValorXml("RecebendoDados").equalsIgnoreCase("S")) {
            // Marca nos parametro internos que a aplicacao que esta recebendo os dados
            //funcoes.setValorXml("RecebendoDados", "S");

            // Desavia o recebimento automatico
            funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, false);

            ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(InicioMDActivity.this, ReceberDadosFtpAsyncRotinas.TELA_INICIO);
            receberDadosFtpAsync.execute();

            Log.i("SAVARE", "Executou a rotina para receber os dados. - InicioActivity");
        }
        // Checa se existe algum card view
        if (mListView.getAdapter().getItemCount() > 0) {
            // Remove todos a lista de cards
            mListView.getAdapter().clearAll();
        }

        geraCardView();

        EmpresaRotinas empresaRotinas = new EmpresaRotinas(InicioMDActivity.this);
        // Pega os dados da empresa
        EmpresaBeans dadosEmpresa = empresaRotinas.empresa(funcoes.getValorXml("CodigoEmpresa"));

        UsuarioRotinas usuarioRotinas = new UsuarioRotinas(InicioMDActivity.this);
        // Pega os dados do usuario(vendedor)
        UsuarioBeans dadosUsuario = usuarioRotinas.usuarioCompleto("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));

        if (dadosEmpresa != null && dadosUsuario != null) {

            // Checa se esta liberado para vender no atacado
            if (dadosUsuario.getVendeAtacadoUsuario() == '1'){
                // Vareavel para salvar a descricao do card
                String descricaoCard = "";

                // Checa se o tipo de acumulo eh por valor para vendas no atacado
                if (dadosEmpresa.getTitpoAcumuloCreditoAtacado().equalsIgnoreCase("V")) {

                    descricaoCard += "Tpo de Acumulo de Crédito:  Por Valor. \n" +
                            "Valor Acumulado: " + getResources().getString(R.string.sigla_real) + " " + funcoes.arredondarValor(dadosUsuario.getValorCreditoAtacado() + " \n");

                    // Checa se o tipo eh por percentual para vendas no atacado
                } else if (dadosEmpresa.getTitpoAcumuloCreditoAtacado().equalsIgnoreCase("P")) {

                    descricaoCard += "Tpo de Acumulo de Crédito:  Por Percentual. \n" +
                            "Percentual Acumulado: " + funcoes.arredondarValor(dadosUsuario.getPercentualCreditoAtacado())+"%" + " \n";

                }
                // Checa os periodo que vai ser acumulado os creditos para vendas no atacado
                if (dadosEmpresa.getPeriodocrceditoAtacado().equalsIgnoreCase("M")) {
                    descricaoCard += "\nPeríodo do Crédito está Mensal. \n";

                } else if (dadosEmpresa.getPeriodocrceditoAtacado().equalsIgnoreCase("Q")) {
                    descricaoCard += "\n Período do Crédito está Quinzenal. \n";

                } else if (dadosEmpresa.getPeriodocrceditoAtacado().equalsIgnoreCase("S")) {
                    descricaoCard += "\n Período do Crédito está Semanal. \n";

                } else if (dadosEmpresa.getPeriodocrceditoAtacado().equalsIgnoreCase("T")) {
                    descricaoCard += "\n Período do Crédito está Semestral. \n";

                } else if (dadosEmpresa.getPeriodocrceditoAtacado().equalsIgnoreCase("A")) {
                    descricaoCard += "\n Período do Crédito está Anual. \n";
                }

                Card cardCreditoAtacado = new Card.Builder(getApplicationContext())
                        .withProvider(new CardProvider())
                        .setLayout(R.layout.material_basic_buttons_card)
                        .setTitle("Resumo de Credito no Atacado")
                        .setDescription(descricaoCard)
                        .addAction(R.id.right_text_button, new TextViewAction(getApplicationContext())
                                .setText("Action")
                                .setListener(new OnActionClickListener() {
                                    @Override
                                    public void onActionClicked(View view, Card card) {
                                        String s = card.toString();
                                    }

                                }))
                        .endConfig()
                        .build();
                // Adiciona o card view em uma lista
                mListView.getAdapter().add(cardCreditoAtacado);

                // Checa se esta liberado para vender no varejo
            }

            if (dadosUsuario.getVendeVarejoUsuario() == '1'){
                String descricaoCard = "";
                // Checa se o tipo eh por valor para vendas no varejo
                if (dadosEmpresa.getTitpoAcumuloCreditoVarejo().equalsIgnoreCase("V")) {
                    descricaoCard += "Tpo de Acumulo de Crédito:  Por Valor. \n" +
                            "Valor Acumulado: " + getResources().getString(R.string.sigla_real) + " " + funcoes.arredondarValor(dadosUsuario.getValorCreditoVarejo() + "\n");

                    // Checa se o tipo eh por percentual para vendas no atacado
                } else if (dadosEmpresa.getTitpoAcumuloCreditoVarejo().equalsIgnoreCase("P")) {
                    descricaoCard += "Tpo de Acumulo de Crédito:  Por Percentual. \n" +
                            "Percentual Acumulado: " + funcoes.arredondarValor(dadosUsuario.getPercentualCreditoVarejo())+"% \n";
                }

                // Checa os periodo que vai ser acumulado os creditos para vendas no atacado
                if (dadosEmpresa.getPeriodocrceditoVarejo().equalsIgnoreCase("M")) {
                    descricaoCard += "\n Período do Crédito está Mensal. \n";

                } else if (dadosEmpresa.getPeriodocrceditoVarejo().equalsIgnoreCase("Q")) {
                    descricaoCard += "\n Período do Crédito está Quinzenal. \n";

                } else if (dadosEmpresa.getPeriodocrceditoVarejo().equalsIgnoreCase("S")) {
                    descricaoCard += "\n Período do Crédito está Semanal. \n";

                } else if (dadosEmpresa.getPeriodocrceditoVarejo().equalsIgnoreCase("T")) {
                    descricaoCard += "\n Período do Crédito está Semestral. \n";

                } else if (dadosEmpresa.getPeriodocrceditoVarejo().equalsIgnoreCase("A")) {
                    descricaoCard += "\n Período do Crédito está Anual. \n";
                }

                Card cardCreditoVarejo = new Card.Builder(getApplicationContext())
                        .withProvider(new CardProvider())
                        .setLayout(R.layout.material_basic_buttons_card)
                        .setTitle("Resumo de Credito no Varejo")
                        .setDescription(descricaoCard)
                        .addAction(R.id.right_text_button, new TextViewAction(getApplicationContext())
                                .setText("Action")
                                .setListener(new OnActionClickListener() {
                                    @Override
                                    public void onActionClicked(View view, Card card) {
                                        String s = card.toString();
                                    }

                                }))
                        .endConfig()
                        .build();

                mListView.getAdapter().add(cardCreditoVarejo);
            }
            // Checa se o modo de sincronizacao esta SyncAccount
            if (dadosUsuario.getModoConexao().equalsIgnoreCase("S")) {
                // Cria a conta para o envio automatico do syncAdapter
                funcoes.CreateSyncAccount(InicioMDActivity.this);
            } else {
                funcoes.cancelarSincronizacaoSegundoPlano();
            }
        }
    } // Fim onResume



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Checa se eh um retorno
        if(requestCode == ListaOrcamentoPedidoActivity.SOLICITA_CLIENTE){
            // Checa se eh um retorno da tela de clientes
            if(resultCode == ListaOrcamentoPedidoActivity.RETORNA_CLIENTE){

                final Intent dadosRetornado = data;

                // Cria um dialog para selecionar atacado ou varejo
                AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(InicioMDActivity.this);
                // Atributo(variavel) para escolher o tipo da venda
                final String[] opcao = {"Atacado", "Varejo"};
                // Preenche o dialogo com o titulo e as opcoes
                mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Preenche o ContentValues com os dados da pessoa
                        ContentValues dadosCliente = new ContentValues();
                        dadosCliente.put("ID_CFACLIFO", dadosRetornado.getStringExtra("ID_CFACLIFO"));
                        dadosCliente.put("ID_CFAESTAD", dadosRetornado.getStringExtra("ID_CFAESTAD"));
                        dadosCliente.put("ID_CFACIDAD", dadosRetornado.getStringExtra("ID_CFACIDAD"));
                        dadosCliente.put("ID_SMAEMPRE", dadosRetornado.getStringExtra("ID_SMAEMPRE"));
                        dadosCliente.put("GUID", UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16));
                        dadosCliente.put("ATAC_VAREJO", which);
                        dadosCliente.put("PESSOA_CLIENTE", dadosRetornado.getStringExtra("PESSOA_CLIENTE"));
                        dadosCliente.put("NOME_CLIENTE", dadosRetornado.getStringExtra("NOME_CLIENTE"));
                        dadosCliente.put("IE_RG_CLIENTE", dadosRetornado.getStringExtra("IE_RG_CLIENTE"));
                        dadosCliente.put("CPF_CGC_CLIENTE", dadosRetornado.getStringExtra("CPF_CGC_CLIENTE"));
                        dadosCliente.put("ENDERECO_CLIENTE", dadosRetornado.getStringExtra("ENDERECO_CLIENTE"));
                        dadosCliente.put("BAIRRO_CLIENTE", dadosRetornado.getStringExtra("BAIRRO_CLIENTE"));
                        dadosCliente.put("CEP_CLIENTE", dadosRetornado.getStringExtra("CEP_CLIENTE"));
						/*dadosCliente.put("LATITUDE", localizacao.getLatitude());
						dadosCliente.put("LONGITUDE", localizacao.getLongitude());
						dadosCliente.put("ALTITUDE", localizacao.getAltitude());
						dadosCliente.put("HORARIO_LOCALIZACAO", localizacao.getHorarioLocalizacao());
						dadosCliente.put("TIPO_LOCALIZACAO", localizacao.getTipoLocalizacao());
						dadosCliente.put("PRECISAO", localizacao.getPrecisao());*/

                        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(InicioMDActivity.this);
                        // Cria um novo orcamento no banco de dados
                        long numeroOracmento = orcamentoRotinas.insertOrcamento(dadosCliente);

                        // Verifica se retornou algum numero
                        if(numeroOracmento > 0){

                            Bundle bundle = new Bundle();
                            bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
                            bundle.putString(OrcamentoTabulacaoFragment.KEY_NOME_RAZAO, dadosRetornado.getStringExtra("NOME_CLIENTE"));
                            bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_PESSOA, dadosRetornado.getStringExtra("ID_CFACLIFO"));
                            bundle.putString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO, String.valueOf(which));
                            bundle.putString("AV", "0");

                            Intent i = new Intent(InicioMDActivity.this, OrcamentoTabulacaoFragment.class);
                            i.putExtras(bundle);

                            // Abre outra tela
                            startActivity(i);
                        }
                    }});

                // Faz a mensagem (dialog) aparecer
                mensagemAtacadoVarejo.show();
            }
        }

    } // FIm onActivityResult


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {

            if (cliqueVoltar < 1){
                // Mostra uma mensagem para clicar novamente em voltar
                Toast.makeText(InicioMDActivity.this, getResources().getString(R.string.clique_sair_novamente_para_sair), Toast.LENGTH_LONG).show();
                cliqueVoltar ++;
                // Cria um temporizador para voltar a zero o clique depois que fechar a menssagem
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cliqueVoltar = 0;
                    }
                }, 3700);

            } else if (cliqueVoltar >= 1){
                // Executa o comando sair
                //InicioMDActivity.super.onBackPressed();
                InicioMDActivity.this.finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Pega os cliques feito no sweep do menu drawer.
     */
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(IDrawerItem iDrawerItem, CompoundButton compoundButton, boolean b) {

            FuncoesPersonalizadas funcoesP = new FuncoesPersonalizadas(InicioMDActivity.this);

            // Checa se a opcao selecionada eh para enviar
            if (iDrawerItem.getTag().toString().contains("enviar")){
                // Checa se foi escolhido verdadeiro ou false
                if (b){
                    funcoesP.setValorXml("EnviarAutomatico", "S");
                } else {
                    funcoesP.setValorXml("EnviarAutomatico", "N");
                }
            }
            // Checa se a opcao selecionada eh para receber
            if (iDrawerItem.getTag().toString().contains("receber")){
                // Checa se foi escolhido verdadeiro ou false
                if (b){
                    funcoesP.setValorXml("ReceberAutomatico", "S");
                } else {
                    funcoesP.setValorXml("ReceberAutomatico", "N");
                }
            }
            // Checa se a opcao seleciona eh para mostrar imagem de produto
            if (iDrawerItem.getTag().toString().contains("imagem_produto")){
                // Checa se foi escolhido verdadeiro ou false
                if (b){
                    funcoesP.setValorXml("ImagemProduto", "S");
                } else {
                    funcoesP.setValorXml("ImagemProduto", "N");
                }
            }
            // Executa a funcao para criar os alarmes em background
            funcoesP.criarAlarmeEnviarReceberDadosAutomatico(true, true);
        }
    };

    private void geraCardView() {
        /*Card card = new Card.Builder(getApplicationContext())
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_basic_buttons_card)
                .setTitle("1. Step: Declare a MaterialListView in your layout")
                .setDescription("<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    android:layout_width=\"match_parent\"\n" +
                        "    android:layout_height=\"match_parent\"\n" +
                        "    android:paddingLeft=\"@dimen/activity_horizontal_margin\"\n" +
                        "    android:paddingRight=\"@dimen/activity_horizontal_margin\"\n" +
                        "    android:paddingTop=\"@dimen/activity_vertical_margin\"\n" +
                        "    android:paddingBottom=\"@dimen/activity_vertical_margin\">\n" +
                        "\n" +
                        "    <com.dexafree.materialList.view.MaterialListView\n" +
                        "        android:layout_width=\"fill_parent\"\n" +
                        "        android:layout_height=\"fill_parent\"\n" +
                        "        android:id=\"@+id/material_listview\"/>\n" +
                        "\n" +
                        "</RelativeLayout>")
                .addAction(R.id.right_text_button, new TextViewAction(getApplicationContext())
                        .setText("Action")
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                View v = view;
                                String s = card.toString();
                            }

                        }))
                .endConfig()
                .build();

        Card card2 = new Card.Builder(getApplicationContext())
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_welcome_card_layout)
                .setTitle("Welcome Card")
                .setTitleColor(getResources().getColor(R.color.branco))
                .setDescription("I am the description \n I am the description \n I am the description \n I am the description \n You're probably tired of writing code to display notifications in your applications, the library abstracts all the notifications construction process for you in a single line of code. Magic? Lie? I summarize in: productivity. To further improve productivity, pugnotification from release 1.2.0 now has support Android Wear.")
                .setDescriptionColor(getResources().getColor(R.color.branco))
                .setSubtitle("My subtitle!")
                .setSubtitleColor(getResources().getColor(R.color.branco))
                .setBackgroundColor(getResources().getColor(R.color.rosa_escuro))
                .setDividerVisible(true)
                .addAction(R.id.ok_button, new TextViewAction(getApplicationContext()).setTextColor(getResources().getColor(R.color.azul_medio_200))
                        .setText("Fazer alguma coisa")
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                String s = card.toString();
                            }
                        }))
                .endConfig()
                .build();

        Card card3 = new Card.Builder(getApplicationContext())
                .withProvider(new CardProvider())
                .setTitle("Card number 4")
                .setDescription("Lorem ipsum dolor sit amet")
                .setLayout(R.layout.material_basic_buttons_card)
                //.setLeftButtonText("Izquierda")
                //.setRightButtonText("Derecha")

                .addAction(R.id.right_text_button, new TextViewAction(getApplicationContext())
                        .setTextColor(getResources().getColor(R.color.azul_medio_200))
                        .setText("Action").setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                String s = card.toString();
                            }
                        }))

                .endConfig()
                .build();


        // Add card
        mListView.getAdapter().add(card);
        mListView.getAdapter().add(card2);
        mListView.getAdapter().add(card3);*/
    }
}
package com.savare.activity.material.designer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.PositivacaoSql;
import com.savare.beans.CidadeBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarPdfRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.EnviarOrcamentoFtpAsyncRotinas;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bruno Nogueira Silva on 15/04/2016.
 */
public class ListaOrcamentoPedidoMDActivity extends AppCompatActivity{

    private Spinner spinnerListaCidade;
    private Toolbar toolbarCabecalho;
    private ListView listViewListaOrcamentoPedido;
    private TextView textTotal, textDataInicial, textDataFinal;
    private FloatingActionMenu menuFloatingButton;
    private FloatingActionButton itemMenuNovoOrcamento, itemMenuRateioPreco;
    private ProgressBar progressBarStatus;
    private TextView textDialogDataFinal;
    private TextView textDialogDataInicial;
    private Dialog dialogPeriodo;
    private List<Integer> listaItemOrcamentoSelecionado = new ArrayList<Integer>();
    private ItemUniversalAdapter adapterListaOrcamentoPedido;
    private ItemUniversalAdapter adapterCidade;
    private String tipoOrcamentoPedido; // O = Orcamento, P = Pedido nao enviados, E = Excluido, N = Pedidos Enviados
    private String retornaValor = "F";
    private String cidade;
    private int totalItemSelecionado = 0;
    private String tipoOrdem = null;
    double totalDiferenca;
    int anoInicialSelecinado = -1;
    int mesInicialSelecionado = -1;
    int diaInicialSelecionado = -1;
    int anoFinalSelecinado = -1;
    int mesFinalSelecionado = -1;
    int diaFinalSelecionado = -1;
    private int mPreviousVisibleItem;
    public static final String TIPO_ORCAMENTO = "O",
            TIPO_PEDIDO_NAO_ENVIADO = "P",
            TIPO_PEDIDO_ENVIADO = "N",
            TIPO_ORCAMENTO_EXCLUIDO = "E",
            TIPO_PEDIDO_RETORNADO_BLOQUEADO = "RB",
            TIPO_PEDIDO_RETORNADO_LIBERADO = "RL",
            TIPO_PEDIDO_RETORNADO_EXCLUIDO = "RE",
            TIPO_PEDIDO_FATURADO = "F",
            ITEM_NAO_CONFERIDO = "NC",
            ITEM_CONFERIDO = "C";
    public static final String KEY_TELA_LISTA_ORCAMENTO_PEDIDO = "ListaOrcamentoPedidosActivity",
            KEY_TELA_CHAMADA = "TELA_CHAMADA",
            KEY_RETORNA_VALOR = "RETORNA_VALOR",
            KEY_ORCAMENTO_PEDIDO = "ORCAMENTO_PEDIDO";
    public static final String TELA_LISTA_PRODUTOS = "LISTA_PRODUTOS";
    public static final int RETORNA_CLIENTE = 100;
    public static final int SOLICITA_CLIENTE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_orcamento_pedido_md);

        recuperaCampo();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {

            this.tipoOrcamentoPedido = intentParametro.getString(KEY_ORCAMENTO_PEDIDO);
            this.retornaValor = intentParametro.getString(KEY_RETORNA_VALOR);

            if(this.retornaValor == null){
                this.retornaValor = "F";
            }

        } else {
            // Dados da mensagem
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");
            mensagem.put("mensagem", "Não foi possível carregar o tipo de lista\n");

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
            funcoes.menssagem(mensagem);
        }

        spinnerListaCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                CarregarDadosOrcamentoPedido carregarDadosOrcamentoPedido = new CarregarDadosOrcamentoPedido(position);
                carregarDadosOrcamentoPedido.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Pega o clique do listListaOrcamentoPedido
        listViewListaOrcamentoPedido.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Checa se quem chamou essa tela eh para retornar
                if ((retornaValor != null) && (retornaValor.equals(TELA_LISTA_PRODUTOS))) {

                    //Pega os dados da pessoa que foi clicado
                    OrcamentoBeans orcamento = (OrcamentoBeans) parent.getItemAtPosition(position);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("AEAORCAM", orcamento);

                    // Cria uma intent para returnar um valor para activity ProdutoLista
                    Intent returnIntent = new Intent();
                    returnIntent.putExtras(bundle);

                    setResult(100, returnIntent);
                    // Fecha a tela de detalhes de produto
                    finish();

                } else {
                    //Pega os dados da pessoa que foi clicado
                    OrcamentoBeans orcamento = (OrcamentoBeans) parent.getItemAtPosition(position);

                    Bundle bundle = new Bundle();
                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO, String.valueOf(orcamento.getIdOrcamento()));
                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_NOME_RAZAO, orcamento.getNomeRazao());
                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_PESSOA, String.valueOf(orcamento.getIdPessoa()));
                    bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO, String.valueOf(orcamento.getTipoVenda()));

                    Intent i = new Intent(ListaOrcamentoPedidoMDActivity.this, OrcamentoTabFragmentMDActivity.class);

                    i.putExtras(bundle);

                    // Abre outra tela
                    startActivity(i);
                }
            }
        });


        listViewListaOrcamentoPedido.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Passa por tota a lista de orcamento/pedido
                for (int i = 0; i < adapterListaOrcamentoPedido.getListaOrcamentoPediso().size(); i++) {
                    // Mar o adapter para mudar a cor do fundo
                    adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(i).setTagSelectContext(false);
                }
                adapterListaOrcamentoPedido.notifyDataSetChanged();
                listaItemOrcamentoSelecionado = null;
                totalItemSelecionado = 0;

                toolbarCabecalho.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Oculta a toolbar
                toolbarCabecalho.setVisibility(View.GONE);

                // Checa se eh orcamento
                if (tipoOrcamentoPedido.equals("O")) {
                    // Cria a variavel para inflar o menu de contexto
                    MenuInflater menuContext = mode.getMenuInflater();
                    menuContext.inflate(R.menu.lista_orcamento_context_md, menu);

                    // Checa se eh pedido
                } else if ((tipoOrcamentoPedido.equals("P")) || (tipoOrcamentoPedido.equals("N"))) {
                    // Cria a variavel para inflar o menu de contexto
                    MenuInflater menuContext = mode.getMenuInflater();
                    menuContext.inflate(R.menu.lista_pedido_context_md, menu);

                    // Checa se eh Lixeira(orcamento excluidos)
                } else if (tipoOrcamentoPedido.equals("E")) {
                    // Cria a variavel para inflar o menu de contexto
                    MenuInflater menuContext = mode.getMenuInflater();
                    menuContext.inflate(R.menu.lista_lixeira_context_md, menu);
                }
                return true;
            }


            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.menu_lista_orcamento_context_md_transformar_pedido:

                        new MaterialDialog.Builder(ListaOrcamentoPedidoMDActivity.this)
                                .title(R.string.formar_venda)
                                .items(R.array.forma_venda_positivacao)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                        // Valida a opcao selecionada 1 = Visitou, mas, não comprou e 2 = Não estava
                                        if ((which != 1) && (which != 2)) {

                                            // Instancia a classe para manipular os orcamento no banco de dados
                                            OrcamentoSql orcamentoSql = new OrcamentoSql(ListaOrcamentoPedidoMDActivity.this);
                                            int totalAtualizado = 0;
                                            for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {

                                                ContentValues dadosPedido = new ContentValues();
                                                dadosPedido.put("STATUS", "P");

                                                totalAtualizado = totalAtualizado + orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " +
                                                        adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
                                            }
                                            // Dados da mensagem
                                            ContentValues mensagem = new ContentValues();
                                            mensagem.put("comando", 2);
                                            mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");

                                            // Verifica se foi deletado algum registro
                                            if (totalAtualizado > 0) {
                                                mensagem.put("mensagem", totalAtualizado + " Transformado(s) em Pedido(s). \n");

                                                for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {

                                                    int idOrcamento = adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento();

                                                    // Pega os dados da positivacao
                                                    String sqlInsert = "INSERT OR REPLACE INTO CFAPOSIT(STATUS, VALOR_VENDA, DATA_VISITA, ID_CFACLIFO, ID_AEAORCAM) VALUES " +
                                                            "(" + which + ", " +
                                                            "(SELECT AEAORCAM.FC_VL_TOTAL FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento + "), " +
                                                            "(SELECT (DATE('NOW', 'localtime'))), " +
                                                            "(SELECT AEAORCAM.ID_CFACLIFO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento + "), " +
                                                            idOrcamento + ")";

                                                    PositivacaoSql positivacaoSql = new PositivacaoSql(ListaOrcamentoPedidoMDActivity.this);

                                                    // Inseri a positivacao e checa se inseriu com sucesso
                                                    positivacaoSql.execSQL(sqlInsert);
                                                }

                                                // Recarrega a lista de orcamento
                                                CarregarDadosOrcamentoPedido carregarDadosOrcamentoPedido = new CarregarDadosOrcamentoPedido(spinnerListaCidade.getSelectedItemPosition());
                                                carregarDadosOrcamentoPedido.execute();

                                            } else {
                                                mensagem.put("mensagem", getResources().getString(R.string.nao_foi_possivel_transformar_orcamento_pedido));
                                            }
                                            // Esvazia a lista de selecionados
                                            //listaItemOrcamentoSelecionado = null;

                                            // Instancia a classe  de funcoes para mostra a mensagem
                                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                                            funcoes.menssagem(mensagem);
                                            // Fecha o menu context
                                            mode.finish();

                                        } else {
                                            Toast.makeText(ListaOrcamentoPedidoMDActivity.this, getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .show();

                        break;

                    case R.id.menu_lista_orcamento_context_md_deletar:

                        // Checa se eh orcamento
                        if (tipoOrcamentoPedido.equals("O")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ListaOrcamentoPedidoMDActivity.this);
                            builder.setMessage(getResources().getString(R.string.tem_certeza_que_deseja_excluir_pedido))
                                    .setPositiveButton(getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            // Instancia a classe para manipular os orcamento no banco de dados
                                            OrcamentoSql orcamentoSqlDelete = new OrcamentoSql(ListaOrcamentoPedidoMDActivity.this);
                                            int totalDeletado = 0;
                                            for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {

                                                ContentValues dadosPedido = new ContentValues();
                                                dadosPedido.put("STATUS", "E");

                                                totalDeletado = totalDeletado + orcamentoSqlDelete.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " +
                                                        adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
                                                // Deleta o item da lista de item original
                                                if (totalDeletado > 0) {
                                                    // Remove o item da lista pricipal
                                                    adapterListaOrcamentoPedido.getListaOrcamentoPediso().remove(listaItemOrcamentoSelecionado.get(i));
                                                    // Remove da lista do adapter
                                                    adapterListaOrcamentoPedido.remove(listaItemOrcamentoSelecionado.get(i));
                                                }
                                            }
                                            // Dados da mensagem
                                            ContentValues mensagemDelete = new ContentValues();
                                            mensagemDelete.put("comando", 2);
                                            mensagemDelete.put("tela", "ListaOrcamentoPedidoMDActivity");

                                            // Verifica se foi deletado algum registro
                                            if (totalDeletado > 0) {
                                                mensagemDelete.put("mensagem", totalDeletado + " Deletado(s). \n");

                                                // Pega a posicao da lista
                                                //int i = actionBar.getSelectedNavigationIndex();
                                                //onNavigationItemSelected(i, adapterCidade.getItemId(i));
                                                onResume();
                                            } else {
                                                mensagemDelete.put("mensagem", getResources().getString(R.string.nao_foi_possivel_deletar_orcamento_selecionado));
                                            }

                                            // Instancia a classe  de funcoes para mostra a mensagem
                                            FuncoesPersonalizadas funcoesDelete = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                                            funcoesDelete.menssagem(mensagemDelete);

                                            // Fecha o menu context
                                            mode.finish();
                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.nao), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Fecha o menu context
                                            mode.finish();
                                        }
                                    });
                            // Create the AlertDialog object and return it
                            builder.create();
                            builder.show();


                        }

                        break;

                    case R.id.menu_lista_lixeira_context_md_restaurar_orcamento:
                        // Instancia a classe para manipular os orcamento no banco de dados
                        OrcamentoSql orcamentoSqlLixeira = new OrcamentoSql(ListaOrcamentoPedidoMDActivity.this);
                        int totalLixeira = 0;
                        for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {

                            ContentValues dadosPedido = new ContentValues();
                            dadosPedido.put("STATUS", "O");

                            totalLixeira = totalLixeira + orcamentoSqlLixeira.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " +
                                    adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
                            // Deleta o item da lista de item original
                            if (totalLixeira > 0) {
                                // Remove o item da lista pricipal
                                adapterListaOrcamentoPedido.getListaOrcamentoPediso().remove(listaItemOrcamentoSelecionado.get(i));
                                // Remove da lista do adapter
                                adapterListaOrcamentoPedido.remove(listaItemOrcamentoSelecionado.get(i));
                            }
                        }
                        // Dados da mensagem
                        ContentValues mensagemLixeira = new ContentValues();
                        mensagemLixeira.put("comando", 2);
                        mensagemLixeira.put("tela", "ListaOrcamentoPedidoMDActivity");

                        // Verifica se foi deletado algum registro
                        if (totalLixeira > 0) {
                            mensagemLixeira.put("mensagem", totalLixeira + " Recuperado(s). \n");

                            // Pega a posicao da lista
                            //int i = actionBar.getSelectedNavigationIndex();
                            //onNavigationItemSelected(i, adapterCidade.getItemId(i));
                            onResume();

                        } else {
                            mensagemLixeira.put("mensagem", "NÃO FOI POSSÍVEL RECUPERAR O(S) OR�AMENTO(S) DELETADO(S). \n");
                        }

                        // Instancia a classe  de funcoes para mostra a mensagem
                        FuncoesPersonalizadas funcoesLixeira = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                        funcoesLixeira.menssagem(mensagemLixeira);
                        // Fecha o menu context
                        mode.finish();

                        break;

                    case R.id.menu_lista_orcamento_context_md_enviar_email:

                        enviarEmail();
                        // Fecha o menu context
                        mode.finish();
                        break;


                    case R.id.menu_lista_pedido_context_md_enviar_email:

                        enviarEmail();
                        // Fecha o menu context
                        mode.finish();
                        break;

                    case R.id.menu_lista_pedido_context_md_enviar_pedido_nuvem:

                        String[] idOrcamento = new String[listaItemOrcamentoSelecionado.size()];
                        for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {
                            idOrcamento[i] = String.valueOf(adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(listaItemOrcamentoSelecionado.get(i)).getIdOrcamento());
                        }

                        EnviarOrcamentoFtpAsyncRotinas enviarOrcamento = new EnviarOrcamentoFtpAsyncRotinas(ListaOrcamentoPedidoMDActivity.this);
                        enviarOrcamento.execute(idOrcamento);

                        break;

                    case R.id.menu_lista_lixeira_context_md_enviar_email:

                        enviarEmail();
                        // Fecha o menu context
                        mode.finish();
                        break;

                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                // Checa se a lista de selecionado eh nula
                if (listaItemOrcamentoSelecionado == null) {
                    listaItemOrcamentoSelecionado = new ArrayList<Integer>();
                }
                // Checa se o comando eh de selecao ou descelecao
                if (checked) {
                    // Incrementa o totalizador
                    totalItemSelecionado = totalItemSelecionado + 1;
                    //listaItemOrcamentoSelecionado.add(listaItemOrcamento.get(position));
                    listaItemOrcamentoSelecionado.add(position);
                    // Mar o adapter para mudar a cor do fundo
                    adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(position).setTagSelectContext(true);
                    adapterListaOrcamentoPedido.notifyDataSetChanged();

                } else {
                    int i = 0;
                    while (i < listaItemOrcamentoSelecionado.size()) {

                        // Checar se a posicao desmarcada esta na lista
                        if (listaItemOrcamentoSelecionado.get(i) == position) {
                            // Remove a posicao da lista de selecao
                            listaItemOrcamentoSelecionado.remove(i);
                            // Diminui o total de itens selecionados
                            totalItemSelecionado = totalItemSelecionado - 1;

                            // Mar o adapter para mudar a cor do fundo
                            adapterListaOrcamentoPedido.getListaOrcamentoPediso().get(position).setTagSelectContext(false);
                            adapterListaOrcamentoPedido.notifyDataSetChanged();
                        }
                        // Incrementa a variavel
                        i++;
                    }
                }
                // Checa se tem mais de um item selecionados
                if (totalItemSelecionado > 1) {
                    // Muda o titulo do menu de contexto quando seleciona os itens
                    mode.setTitle(totalItemSelecionado + " selecionados");
                } else {
                    // Muda o titulo do menu de contexto quando seleciona os itens
                    mode.setTitle(totalItemSelecionado + " selecionado");
                }
            }
        });

        listViewListaOrcamentoPedido.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Funcao para ocultar o float button quando rolar a lista de orcamento/pedido
                if (firstVisibleItem > mPreviousVisibleItem) {
                    menuFloatingButton.hideMenu(true);
                } else if (firstVisibleItem < mPreviousVisibleItem) {
                    menuFloatingButton.showMenu(true);
                }
                mPreviousVisibleItem = firstVisibleItem;
            }
        });



        itemMenuNovoOrcamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre a tela de detalhes do produto
                Intent intent = new Intent(ListaOrcamentoPedidoMDActivity.this, ClienteListaMDActivity.class);
                intent.putExtra(KEY_TELA_CHAMADA, KEY_TELA_LISTA_ORCAMENTO_PEDIDO);

                // Fecha o float buttom menu
                menuFloatingButton.close(true);

                // Abre a activity aquardando uma resposta
                startActivityForResult(intent, SOLICITA_CLIENTE);
            }
        });

        itemMenuRateioPreco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checa se existe produtos no orcamento
                if(adapterListaOrcamentoPedido.getListaOrcamentoPediso().size() > 0){

                    if(adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO){
                        // Muda o tipo de listagem do adapter
                        adapterListaOrcamentoPedido.setTipoItem(adapterListaOrcamentoPedido.LISTA_ORCAMENTO_PEDIDO);
                        ((BaseAdapter) listViewListaOrcamentoPedido.getAdapter()).notifyDataSetChanged();

                    }else {

                        // Muda o tipo de listagem do adapter
                        adapterListaOrcamentoPedido.setTipoItem(adapterListaOrcamentoPedido.RATEIO_ORCAMENTO);
                        ((BaseAdapter) listViewListaOrcamentoPedido.getAdapter()).notifyDataSetChanged();

                        // Cria uma vareavel para pegar a lista de orcamentos
                        List<OrcamentoBeans> listaOrcamentoPedido = new ArrayList<OrcamentoBeans>();
                        // Pega a lista de orcamento
                        listaOrcamentoPedido = adapterListaOrcamentoPedido.getListaOrcamentoPediso();

                        // Variavel para armezenar o total da diferenca entro o preco vendido e o preco de tabela
                        totalDiferenca = 0;
                        // Passa por toda a lista de itens
                        for(int i = 0; i < listaOrcamentoPedido.size(); i++){
                            totalDiferenca = totalDiferenca + (listaOrcamentoPedido.get(i).getTotalOrcamentoBruto() - listaOrcamentoPedido.get(i).getTotalOrcamento());
                        }


                    }

                } else {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                    // Cria uma variavem para inserir as propriedades da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 2);
                    mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");
                    mensagem.put("mensagem", "N�o existe produtos na lista de orcamento. \n");
                    // Executa a mensagem passando por parametro as propriedades
                    funcoes.menssagem(mensagem);
                }

                // Fecha o float buttom menu
                menuFloatingButton.close(true);
            }
        });
    } // FIm onCreate


    @Override
    protected void onResume() {
        super.onResume();

        carregarListaCidade();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.lista_orcamento_md, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Instancia a classe responsavel por carregar a lista de orcamento/pedido
        CarregarDadosOrcamentoPedido carregarDadosOrcamentoPedido = new CarregarDadosOrcamentoPedido(spinnerListaCidade.getSelectedItemPosition());

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                finish();
                break;

            case R.id.menu_lista_orcamento_md_atualizar:

                carregarListaCidade();
                break;

            case R.id.menu_lista_orcamento_md_filtrar_periodo:

                dialogPeriodo = new Dialog(ListaOrcamentoPedidoMDActivity.this);
                // Seta o layout customizado para o dialog
                dialogPeriodo.setContentView(R.layout.layout_dialog_periodo_data);
                // Seta o titulo do dialog
                dialogPeriodo.setTitle(getResources().getString(R.string.periodo));
                dialogPeriodo.setCancelable(true);

                // Associa o campo do dialog customizado
                textDialogDataInicial = (TextView) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_inicial);

                // Checa se ja existe alguma data selecionada
                if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
                    textDialogDataInicial.setText(diaInicialSelecionado + "/" + mesInicialSelecionado + "/" + anoInicialSelecinado);
                }

                // Desailita a edicao do campo
                textDialogDataInicial.setFocusable(false);
                // Pega os clique do campo
                textDialogDataInicial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Process to get Current Date
                        final Calendar c = Calendar.getInstance();

                        if (anoInicialSelecinado < 0) {
                            anoInicialSelecinado = c.get(Calendar.YEAR);
                        }
                        if (mesInicialSelecionado < 0) {
                            mesInicialSelecionado = c.get(Calendar.MONTH) + 1;
                        }
                        if (diaInicialSelecionado < 0) {
                            diaInicialSelecionado = c.get(Calendar.DAY_OF_MONTH);
                        }

                        // Launch Date Picker Dialog
                        DatePickerDialog dataDialog = new DatePickerDialog(ListaOrcamentoPedidoMDActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Preenche o campo com a data
                                textDialogDataInicial.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                // Seta o calendario com a data selecionada
                                //c.set(year, monthOfYear + 1, dayOfMonth);

                                anoInicialSelecinado = year;
                                mesInicialSelecionado = (monthOfYear + 1);
                                diaInicialSelecionado = dayOfMonth;
                            }
                        }, anoInicialSelecinado, mesInicialSelecionado - 1, diaInicialSelecionado);
                        dataDialog.show();
                    }
                });

                textDialogDataFinal = (TextView) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_final);
                // Checa se ja existe alguma data selecionada
                if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
                    textDialogDataFinal.setText(diaFinalSelecionado + "/" + mesFinalSelecionado + "/" + anoFinalSelecinado);
                }
                textDialogDataFinal.setFocusable(false);
                textDialogDataFinal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Process to get Current Date
                        final Calendar c = Calendar.getInstance();

                        if (anoFinalSelecinado < 0) {
                            anoFinalSelecinado = c.get(Calendar.YEAR);
                        }
                        if (mesFinalSelecionado < 0) {
                            mesFinalSelecionado = c.get(Calendar.MONTH) + 1;
                        }
                        if (diaFinalSelecionado < 0) {
                            diaFinalSelecionado = c.get(Calendar.DAY_OF_MONTH);
                        }
                        // Launch Date Picker Dialog
                        DatePickerDialog dataDialog = new DatePickerDialog(ListaOrcamentoPedidoMDActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                textDialogDataFinal.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                //c.set(year, monthOfYear, dayOfMonth);
                                //dataFormatadaFinal.setCalendar(c);
                                anoFinalSelecinado = year;
                                mesFinalSelecionado = monthOfYear + 1;
                                diaFinalSelecionado = dayOfMonth;
                            }
                        }, anoFinalSelecinado, mesFinalSelecionado - 1, diaFinalSelecionado);
                        dataDialog.show();
                    }
                });

                Button buttonCancelar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_cancelar);
                buttonCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Fecha o dialog com seletor de data
                        dialogPeriodo.dismiss();

                        anoInicialSelecinado = -1;
                        mesInicialSelecionado = -1;
                        diaInicialSelecionado = -1;

                        anoFinalSelecinado = -1;
                        mesFinalSelecionado = -1;
                        diaInicialSelecionado = -1;

                        // Recarrega a lista de orcamento
                        CarregarDadosOrcamentoPedido carregarDadosOrcamentoPedido = new CarregarDadosOrcamentoPedido(spinnerListaCidade.getSelectedItemPosition());
                        carregarDadosOrcamentoPedido.execute();
                    }
                });


                Button buttonFiltrar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_filtrar);
                buttonFiltrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Recarrega a lista de orcamento
                        CarregarDadosOrcamentoPedido carregarDadosOrcamentoPedido = new CarregarDadosOrcamentoPedido(spinnerListaCidade.getSelectedItemPosition());
                        carregarDadosOrcamentoPedido.execute();

                        dialogPeriodo.dismiss();
                    }
                });

                dialogPeriodo.show();

                break;

            case R.id.menu_lista_orcamento_md_ordem_decrescente:
                // Seta a ordem decrescente
                tipoOrdem = OrcamentoRotinas.ORDEM_DECRESCENTE;
                // Recarrega a lista de orcamento pedidos
                // Recarrega a lista de orcamento

                carregarDadosOrcamentoPedido.execute();

                adapterListaOrcamentoPedido.notifyDataSetChanged();
                break;

            case R.id.menu_lista_orcamento_md_ordem_crescente:
                // Seta a ordem decrescente
                tipoOrdem = OrcamentoRotinas.ORDEM_CRESCENTE;
                // Recarrega a lista de orcamento pedidos
                // Recarrega a lista de orcamento
                carregarDadosOrcamentoPedido.execute();

                break;

            case R.id.menu_lista_orcamento_md_somente_enviados:

                if(!tipoOrcamentoPedido.equalsIgnoreCase("O")){
                    // Checa se nao esta na lista de pedidos enviados
                    if(!tipoOrcamentoPedido.equalsIgnoreCase("N")){
                        tipoOrcamentoPedido = "N";
                        // Recarrega a lista de cidades
                        carregarListaCidade();
                        // Recarrega a lista
                        carregarDadosOrcamentoPedido.execute();

                    } else {
                        // Dados da mensagem
                        ContentValues mensagem = new ContentValues();
                        mensagem.put("comando", 1);
                        mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");
                        mensagem.put("mensagem", "Voc� j� esta na lista de pedidos enviados.\n");

                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                        funcoes.menssagem(mensagem);
                    }

                } else {
                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 1);
                    mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");
                    mensagem.put("mensagem", "Filtro permitido apenas para a lista de pedidos.\n");

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                    funcoes.menssagem(mensagem);
                }
                break;

            case R.id.menu_lista_orcamento_md_nao_enviados:

                if(!tipoOrcamentoPedido.equalsIgnoreCase("O")){
                    // Checa se nao esta na lista de pedidos nao enviados
                    if(!tipoOrcamentoPedido.equalsIgnoreCase("P")){
                        tipoOrcamentoPedido = "P";
                        // Recarrega a lista de cidades
                        carregarListaCidade();
                        // Recarrega a lista
                        carregarDadosOrcamentoPedido.execute();

                    } else {
                        // Dados da mensagem
                        ContentValues mensagem = new ContentValues();
                        mensagem.put("comando", 1);
                        mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");
                        mensagem.put("mensagem", "Você já esta na lista de pedidos NÃO enviados.\n");

                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                        funcoes.menssagem(mensagem);
                    }
                } else {
                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 1);
                    mensagem.put("tela", "ListaOrcamentoPedidoMDActivity");
                    mensagem.put("mensagem", "Filtro permitido apenas para a lista de pedidos.\n");

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);
                    funcoes.menssagem(mensagem);
                }
                break;

            default:
                break;
        }
        return true;
    } // Fim do onOptionsItemSelected


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Checa se eh um retorno
        if(requestCode == SOLICITA_CLIENTE){
            // Checa se eh um retorno da tela de clientes
            if(resultCode == RETORNA_CLIENTE){

                final Intent dadosRetornado = data;

                // Cria um dialog para selecionar atacado ou varejo
                AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(ListaOrcamentoPedidoMDActivity.this);
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

                        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoMDActivity.this);
                        // Cria um novo orcamento no banco de dados
                        long numeroOracmento = orcamentoRotinas.insertOrcamento(dadosCliente);

                        // Verifica se retornou algum numero
                        if(numeroOracmento > 0){

                            Bundle bundle = new Bundle();
                            bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
                            bundle.putString(OrcamentoTabFragmentMDActivity.KEY_NOME_RAZAO, dadosRetornado.getStringExtra("NOME_CLIENTE"));
                            bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ID_PESSOA, dadosRetornado.getStringExtra("ID_CFACLIFO"));
                            bundle.putString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO, String.valueOf(which));
                            bundle.putString("AV", "0");

                            Intent i = new Intent(ListaOrcamentoPedidoMDActivity.this, OrcamentoTabFragmentMDActivity.class);
                            i.putExtras(bundle);

                            // Abre outra tela
                            startActivity(i);
                        }
                    }});

                // Faz a mensagem (dialog) aparecer
                mensagemAtacadoVarejo.show();




            }
        }
    }


    private void recuperaCampo(){
        listViewListaOrcamentoPedido = (ListView) findViewById(R.id.activity_lista_orcamento_pedido_md_list_pessoa);
        listViewListaOrcamentoPedido.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        textTotal = (TextView) findViewById(R.id.activity_lista_orcamento_pedido_md_text_total);
        textDataInicial = (TextView) findViewById(R.id.activity_lista_orcamento_pedido_md_text_data_inicial);
        textDataFinal = (TextView) findViewById(R.id.activity_lista_orcamento_pedido_md_text_data_final);
        spinnerListaCidade = (Spinner) findViewById(R.id.activity_lista_orcamento_pedido_md_spinner_cidades);
        menuFloatingButton = (FloatingActionMenu) findViewById(R.id.activity_lista_orcamento_pedido_md_menu_float);
        itemMenuNovoOrcamento = (FloatingActionButton) findViewById(R.id.activity_lista_orcamento_pedido_md_novo_orcamento);
        itemMenuRateioPreco = (FloatingActionButton) findViewById(R.id.activity_lista_orcamento_pedido_md_rateio_preco);
        progressBarStatus = (ProgressBar) findViewById(R.id.activity_lista_orcamento_pedido_md_progressBar_status);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_lista_orcamento_pedido_md_toolbar_cabecalho);
        // Adiciona uma titulo para toolbar
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.app_name));
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void carregarListaCidade(){

        List<CidadeBeans> listaCidade = new ArrayList<CidadeBeans>();

        // Instancia a classe para manipular dados do orcamento
        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoMDActivity.this);

        listaCidade = orcamentoRotinas.listaCidadeOrcamentoPedido(this.tipoOrcamentoPedido, null);

        if ((listaCidade != null) && (listaCidade.size() > 0)) {

            adapterCidade = new ItemUniversalAdapter(ListaOrcamentoPedidoMDActivity.this, ItemUniversalAdapter.CIDADE_DARK);
            adapterCidade.setListaCidade(listaCidade);

            spinnerListaCidade.setAdapter(adapterCidade);
        }

    }


    private void enviarEmail(){
        //Cria novo um ProgressDialogo e exibe
        ProgressDialog progress = new ProgressDialog(ListaOrcamentoPedidoMDActivity.this);
        progress.setIndeterminate(true);
        progress.setTitle("Enviar e-mail");
        progress.setMessage("Aguarde, todos os PDF est�o sendo gerados.\n"
                + " Levar� um pouquinho mais de tempo.");
        progress.setCancelable(true);

        progress.show();

        ArrayList<Uri> listaCaminho = new ArrayList<Uri>();;

        OrcamentoBeans orcamento = null;

        for(int i = 0; i < listaItemOrcamentoSelecionado.size(); i++){
            orcamento = new OrcamentoBeans();

            // Pega os dados do orcamento
            orcamento = (OrcamentoBeans) listViewListaOrcamentoPedido.getItemAtPosition(listaItemOrcamentoSelecionado.get(i));

            // Instancia a classe para manipulas os dado do orcamento
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoMDActivity.this);

            orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(""+orcamento.getIdOrcamento()));

            // Instancia a classe responsavel por criar o pdf
            GerarPdfRotinas gerarPdfRotinas = new GerarPdfRotinas(ListaOrcamentoPedidoMDActivity.this);
            // Envia os dados do orcamento
            gerarPdfRotinas.setOrcamento(orcamento);
            // Envia a lista de produtos que pertence ao orcamento
            gerarPdfRotinas.setListaItensOrcamento(orcamentoRotinas.listaItemOrcamentoResumida(null, ""+orcamento.getIdOrcamento(), null, null));

            // Cria o pdf e pega o caminho do arquivo
            String retornoCaminho = gerarPdfRotinas.criaArquivoPdf();

            // Checa se existe algum caminho
            if(retornoCaminho.length() > 0){
                // Adiciona o caminha a uma lista
                listaCaminho.add(Uri.fromFile(new File(retornoCaminho)));
            }

        } // Fim for

        // Fecha a barra de progresso
        progress.dismiss();

        if(listaCaminho.size() > 0){

            PessoaRotinas pessoaRotinas = new PessoaRotinas(ListaOrcamentoPedidoMDActivity.this);

            Intent dadosEmail = new Intent(Intent.ACTION_SEND_MULTIPLE);
            //dadosEmail.setType("text/plai");
            dadosEmail.setType("message/rfc822");
            dadosEmail.putExtra(Intent.EXTRA_EMAIL  , new String[]{pessoaRotinas.emailPessoa(""+orcamento.getIdPessoa())});
            dadosEmail.putExtra(Intent.EXTRA_SUBJECT, "Or�amento/Pedido de N� " + orcamento.getIdOrcamento());
            dadosEmail.putExtra(Intent.EXTRA_STREAM, listaCaminho);
            dadosEmail.putExtra(Intent.EXTRA_TEXT   , "E-Mail enviado pelo App SAVARE.");

            try {
                startActivity(Intent.createChooser(dadosEmail, "Enviar e-mail..."));

            } catch (android.content.ActivityNotFoundException ex) {
                //Toast.makeText(OrcamentoActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    } // Fim enviar email


    public class CarregarDadosOrcamentoPedido extends AsyncTask<Void, Void, Void> {

        private int itemPosition = -1;

        public CarregarDadosOrcamentoPedido(int itemPosition) {
            this.itemPosition = itemPosition;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setIndeterminate(true);

            // Checa se tem alguma data selecionada para visualizar a data inicial
            if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
                textDataInicial.setVisibility(View.VISIBLE);
                textDataInicial.setText(getResources().getString(R.string.data_inicial) + diaInicialSelecionado + "/" + mesInicialSelecionado + "/" + anoInicialSelecinado);
            } else {
                textDataInicial.setVisibility(View.GONE);
            }

            // Checa se tem alguma data selecionada para visualizar a data final
            if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
                textDataFinal.setVisibility(View.VISIBLE);
                textDataFinal.setText(getResources().getString(R.string.data_final) + diaFinalSelecionado + "/" + mesFinalSelecionado + "/" + anoFinalSelecinado);

            } else {
                textDataFinal.setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Instancia a classe de orcamento para manipular dados do orcamento
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoMDActivity.this);

            // Cria uma variavel para armazenar a lista de orcamento
            List<OrcamentoBeans> listaOrcamentoPedido = new ArrayList<OrcamentoBeans>();

            // Checa se esta selecionado todos os orcamento/pedido
            if(adapterCidade.getListaCidade().get(itemPosition).getDescricao().equalsIgnoreCase("Todas as Cidades")) {

                // Preenche a lista de pessoas
                listaOrcamentoPedido = orcamentoRotinas.listaOrcamentoPedido(new String[]{tipoOrcamentoPedido}, wherePeriodoData(), tipoOrdem);
            } else {
                // Pega a palavra que eh para ser removida
                //String remover = adapterCidade.getListaCidade().get(itemPosition).getDescricao().substring(0, 5);

                // Monta a clausula where do sql
                String where = "(CFACIDAD.DESCRICAO LIKE '%" + adapterCidade.getListaCidade().get(itemPosition).getDescricao() + "%')";

                String periodo = wherePeriodoData();

                if((periodo != null) && (periodo.length() > 0)){
                    where += " AND " + periodo;
                }

                // Instancia a classe
                listaOrcamentoPedido = new ArrayList<OrcamentoBeans>();
                // Preenche a lista de pessoas
                listaOrcamentoPedido = orcamentoRotinas.listaOrcamentoPedido(new String[]{tipoOrcamentoPedido}, where, tipoOrdem);
            }

            if (listaOrcamentoPedido != null && listaOrcamentoPedido.size() > 0) {
                // Seta o adapter com a nova lista
                adapterListaOrcamentoPedido = new ItemUniversalAdapter(ListaOrcamentoPedidoMDActivity.this, ItemUniversalAdapter.LISTA_ORCAMENTO_PEDIDO);
                // Preenche o adapter com a lista de orcamento
                adapterListaOrcamentoPedido.setListaOrcamentoPedido(listaOrcamentoPedido);

            } else {
                adapterListaOrcamentoPedido = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (adapterListaOrcamentoPedido != null){
                // Seta o listView com o novo adapter que ja esta com a nova lista
                listViewListaOrcamentoPedido.setAdapter(adapterListaOrcamentoPedido);
            } else {
                listViewListaOrcamentoPedido.setAdapter(null);
            }

            // Instancia a classe para manipular dados do orcamento
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ListaOrcamentoPedidoMDActivity.this);

            cidade = (adapterCidade.getListaCidade().get(itemPosition).getDescricao().equalsIgnoreCase("Todas as Cidades")) ? null : adapterCidade.getListaCidade().get(itemPosition).getDescricao();

            // Pega o periodo selecionado no menu
            String periodo = wherePeriodoData();

            // Checa se faz parte da lista de pedido
            if ((tipoOrcamentoPedido.equals(TIPO_PEDIDO_NAO_ENVIADO)) || (tipoOrcamentoPedido.equals(TIPO_PEDIDO_ENVIADO))){
                // Muda a cor do actionBar
                toolbarCabecalho.setBackgroundColor(getResources().getColor(R.color.laranja_escuro));

                // Checa se esta visualizando rateio de preco
                if( (adapterListaOrcamentoPedido != null ) && (adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO) ){

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);

                    textTotal.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) +
                            " Pedido(s) | Tabela: " + orcamentoRotinas.totalListaOrcamentoBruto(tipoOrcamentoPedido, cidade, periodo) +
                            " - Venda: " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo) +
                            " | Dif.: " + funcoes.arredondarValor(totalDiferenca));

                } else {
                    textTotal.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) +
                            " Pedido(s) | " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo));
                }

                // Checa se faz parte da lista de orcamento
            } else if(tipoOrcamentoPedido.equals(TIPO_ORCAMENTO)){
                // Muda a cor do actionBar
                toolbarCabecalho.setBackgroundColor(getResources().getColor(R.color.verde_escuro));

                // Checa se esta visualizando rateio de preco
                if( (adapterListaOrcamentoPedido != null ) && (adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO) ){

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);

                    textTotal.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) +
                            " Orçamento(s) | Tabela: " + orcamentoRotinas.totalListaOrcamentoBruto(tipoOrcamentoPedido, cidade, periodo) +
                            " - Venda: " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo) +
                            " | Dif.: " + funcoes.arredondarValor(totalDiferenca));
                } else {
                    textTotal.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) +
                            " Orçamento(s) | " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo));
                }

                // Checa se faz parte da lista de orcamentos excluidos
            } else if (tipoOrcamentoPedido.equals(TIPO_ORCAMENTO_EXCLUIDO)){
                // Muda a cor do actionBar
                toolbarCabecalho.setBackgroundColor(getResources().getColor(R.color.vermelho_escuro));

                // Checa se esta visualizando rateio de preco
                if( (adapterListaOrcamentoPedido != null ) && (adapterListaOrcamentoPedido.getTipoItem() == adapterListaOrcamentoPedido.RATEIO_ORCAMENTO) ){

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaOrcamentoPedidoMDActivity.this);

                    textTotal.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) +
                            " Excluido(s) | Tabela: " + orcamentoRotinas.totalListaOrcamentoBruto(tipoOrcamentoPedido, cidade, periodo) +
                            " - Venda: " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo) +
                            " | Dif.: " + funcoes.arredondarValor(totalDiferenca));

                } else {
                    textTotal.setText(orcamentoRotinas.quantidadeListaOrcamento(tipoOrcamentoPedido, cidade, periodo) +
                            " Excluido(s) | " + orcamentoRotinas.totalListaOrcamentoLiquido(tipoOrcamentoPedido, cidade, periodo));
                }
            } // Fim if (this.tipoOrcamentoPedido.equals("E"))



            progressBarStatus.setVisibility(View.GONE);
        }

        /**
         * Pega o periodo que foi selecionado no menu.
         *
         * @return
         */
        private String wherePeriodoData(){
            String where = null;

            // Formata a data
            DateFormat dataFormatadaInicial = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dataFormatadaFinal = new SimpleDateFormat("yyyy-MM-dd");

            // Checa se existe data inicial para pesquisar
            if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
                Calendar c = Calendar.getInstance();

                c.set(anoInicialSelecinado, mesInicialSelecionado - 1, diaInicialSelecionado);

                // Preenche a dataFormatada com a data salva
                dataFormatadaFinal.setCalendar(c);

                where = " (DT_CAD >= '" + dataFormatadaInicial.format(c.getTime()) + " 00:00:00')";
            }

            // Checa se existe data final para pesquisar
            if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
                Calendar c = Calendar.getInstance();

                c.set(anoFinalSelecinado, mesFinalSelecionado - 1, diaFinalSelecionado);

                dataFormatadaFinal.setCalendar(c);

                if((where != null) && (where.length() > 0)){
                    where += " AND (DT_CAD <= '" + dataFormatadaFinal.format(c.getTime()) + " 23:59:59')";
                } else {
                    where = " (DT_CAD <= '" + dataFormatadaFinal.format(c.getTime()) + " 23:59:59')";
                }
            }
            return where;
        }
    }
}

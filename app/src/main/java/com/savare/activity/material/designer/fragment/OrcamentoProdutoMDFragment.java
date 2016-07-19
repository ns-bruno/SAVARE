package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.savare.R;
import com.savare.activity.LogActivity;
import com.savare.activity.material.designer.ClienteListaMDActivity;
import com.savare.activity.material.designer.OrcamentoProdutoDetalhesTabFragmentMDActivity;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.activity.material.designer.ProdutoListaMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.PositivacaoSql;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarPdfRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ParcelaRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.GerarPdfAsyncRotinas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Faturamento on 12/04/2016.
 */
public class OrcamentoProdutoMDFragment extends Fragment {

    private View viewOrcamento;
    private ListView listViewListaProdutoOrcamento;
    private TextView textCodigoOrcamento, textTotal, textAtacadoVarejo;
    private ProgressBar progressBarStatus;
    private Toolbar toolbarCabecalho;
    private String
                    idPessoa = null,
                    idOrcamento = null,
                    razaoSocial = null,
                    tipoOrcamentoPedido;
    private ItemUniversalAdapter adapterItemOrcamento;
    private List<ItemOrcamentoBeans> listaItemOrcamentoSelecionado = new ArrayList<ItemOrcamentoBeans>();
    private Toolbar toolbarRodape;
    private int totalItemSelecionado = 0;
    public static final String  KEY_TELA_ORCAMENTO_FRAGMENTO = "ORCAMENTO_FRAGMENT",
                                KEY_TELA_CHAMADA = "TELA_CHAMADA",
                                KEY_ID_ORCAMENTO = "ID_ORCAMENTO";
    public static final int SOLICITA_CLIENTE = 2,
                            RETORNA_CLIENTE = 100,
    ERRO_RETORNA_CLIENTE = 101;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamento = inflater.inflate(R.layout.fragment_orcamento_produto_md, container, false);

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        recuperarCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if(parametro != null){
            textCodigoOrcamento.setText(""+parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO));
            textAtacadoVarejo.setText(""+parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO));
            idPessoa = parametro.getString(""+OrcamentoTabFragmentMDActivity.KEY_ID_PESSOA);
            idOrcamento = parametro.getString(""+OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO);
            razaoSocial = parametro.getString(""+OrcamentoTabFragmentMDActivity.KEY_NOME_RAZAO);
        }
        // Torna o listView em multiplas selecao
        listViewListaProdutoOrcamento.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listViewListaProdutoOrcamento.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Checa se eh orcamento
                if (tipoOrcamentoPedido.equals("O")) {

                    ItemOrcamentoBeans itemOrcamento = (ItemOrcamentoBeans) parent.getItemAtPosition(position);

                    if ((itemOrcamento != null) && (itemOrcamento.getProduto() != null)) {
                        // Abre a tela de detalhes do produto
                        Intent intent = new Intent(getActivity(), OrcamentoProdutoDetalhesTabFragmentMDActivity.class);

                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU, itemOrcamento.getProduto().getIdProduto());
                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAORCAM, Integer.parseInt(idOrcamento));
                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_CFACLIFO, Integer.parseInt(idPessoa));
                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_RAZAO_SOCIAL, razaoSocial);
                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_POSICAO, position);
                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAITORC, itemOrcamento.getIdItemOrcamento());
                        intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ATACADO_VAREJO, textAtacadoVarejo.getText().toString());
                        intent.putExtra(KEY_TELA_CHAMADA, KEY_TELA_ORCAMENTO_FRAGMENTO);

                        startActivityForResult(intent, 1);
                    } else {
                        // Dados da mensagem
                        ContentValues mensagem = new ContentValues();
                        mensagem.put("comando", 0);
                        mensagem.put("tela", "OrcamentoProdutoMDFragment");
                        mensagem.put("mensagem", "Não foi possível carregar os dados do produto. \n");
                        // Instancia a classe  de funcoes para mostra a mensagem
                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                        funcoes.menssagem(mensagem);
                    }

                } else {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                    // Cria uma variavem para inserir as propriedades da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 2);
                    mensagem.put("tela", "OrcamentoProdutoMDFragment");
                    mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n");
                    // Executa a mensagem passando por parametro as propriedades
                    funcoes.menssagem(mensagem);
                }
            } // Fim setOnItemClickListener
        }); //listViewItemOrcamento.setOnItemClickListener



        listViewListaProdutoOrcamento.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

                // Passa por todos os itens da lista
                for (int i = 0; i < adapterItemOrcamento.getListaItemOrcamento().size(); i++) {
                    // Mar o adapter para mudar a cor do fundo
                    adapterItemOrcamento.getListaItemOrcamento().get(i).setTagSelectContext(false);
                }

                adapterItemOrcamento.notifyDataSetChanged();
                listaItemOrcamentoSelecionado = null;
                totalItemSelecionado = 0;

                toolbarCabecalho.setVisibility(View.VISIBLE);
            }


            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Cria a variavel para inflar o menu de contexto
                MenuInflater menuContext = mode.getMenuInflater();
                menuContext.inflate(R.menu.orcamento_context, menu);

                toolbarCabecalho.setVisibility(View.GONE);

                return true;
            }


            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.menu_orcamento_context_deletar:
                        // Checa se eh um orcamento
                        if (tipoOrcamentoPedido.equals("O")) {

                            AlertDialog.Builder builderConfirmacao = new AlertDialog.Builder(getActivity());
                            builderConfirmacao.setMessage("Tem certeza que deseja excluir o(s) item(ns)?")
                                    .setPositiveButton(getContext().getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            // Instancia a classe para manipular os produto no banco de dados
                                            ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(getActivity());
                                            int totalDeletado = 0;
                                            for (int i = 0; i < listaItemOrcamentoSelecionado.size(); i++) {
                                                // Deleta o item da lista de item original
                                                if ((itemOrcamentoSql.delete("AEAITORC.ID_AEAITORC = " + listaItemOrcamentoSelecionado.get(i).getIdItemOrcamento())) > 0) {
                                                    totalDeletado++;
                                                }
                                            } // Fim for

                                            // Dados da mensagem
                                            final ContentValues mensagem = new ContentValues();
                                            mensagem.put("comando", 2);
                                            mensagem.put("tela", "OrcamentoProdutoMDFragment");

                                            // Verifica se foi deletado algum registro
                                            if (totalDeletado > 0) {
                                                mensagem.put("mensagem", totalDeletado + " Deletado(s). \n");

                                                // Atualiza a lista de produtos
                                                onResume();

                                            } else {
                                                mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_conseguimos_deletar_itens) + "\n");
                                            }

                                            // Instancia a classe  de funcoes para mostra a mensagem
                                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                                            funcoes.menssagem(mensagem);

                                            mode.finish();

                                        }
                                    })
                                    .setNegativeButton(getContext().getResources().getString(R.string.nao), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Fecha o menu context
                                            mode.finish();
                                        }
                                    });
                            // Create the AlertDialog object and return it
                            builderConfirmacao.create();
                            builderConfirmacao.show();

                            //return true;

                        } else {

                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                            // Cria uma variavem para inserir as propriedades da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoProdutoMDFragment");
                            mensagem.put("mensagem", viewOrcamento.getResources().getString(R.string.nao_orcamento) + "\n"
                                    + viewOrcamento.getResources().getString(R.string.nao_pode_deletado));
                            // Executa a mensagem passando por parametro as propriedades
                            funcoes.menssagem(mensagem);
                        }


                        break;


                    default:
                        //return false;
                } // Fim switch
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Checa se a lista de selecionado eh nula
                if (listaItemOrcamentoSelecionado == null) {
                    listaItemOrcamentoSelecionado = new ArrayList<ItemOrcamentoBeans>();
                }
                // Checa se o comando eh de selecao ou descelecao
                if (checked) {
                    // Incrementa o totalizador
                    totalItemSelecionado = totalItemSelecionado + 1;
                    //listaItemOrcamentoSelecionado.add(listaItemOrcamento.get(position));
                    listaItemOrcamentoSelecionado.add((ItemOrcamentoBeans) listViewListaProdutoOrcamento.getItemAtPosition(position));
                    // Mar o adapter para mudar a cor do fundo
                    adapterItemOrcamento.getListaItemOrcamento().get(position).setTagSelectContext(true);
                    adapterItemOrcamento.notifyDataSetChanged();

                } else {
                    int i = 0;
                    while (i < listaItemOrcamentoSelecionado.size()) {

                        // Checar se a posicao desmacada esta na lista
                        if (listaItemOrcamentoSelecionado.get(i) == (ItemOrcamentoBeans) listViewListaProdutoOrcamento.getItemAtPosition(position)) {
                            // Remove a posicao da lista de selecao
                            listaItemOrcamentoSelecionado.remove(i);
                            // Diminui o total de itens selecionados
                            totalItemSelecionado = totalItemSelecionado - 1;
                            // Mar o adapter para mudar a cor do fundo
                            adapterItemOrcamento.getListaItemOrcamento().get(position).setTagSelectContext(false);
                            adapterItemOrcamento.notifyDataSetChanged();
                        }
                        // Incrementa a variavel
                        i++;
                    }
                }
                // Checa se tem mais de um item selecionados
                if (totalItemSelecionado > 1) {
                    // Muda o titulo do menu de contexto quando seleciona os itens
                    mode.setTitle(totalItemSelecionado + " itens selecionados");
                } else {
                    // Muda o titulo do menu de contexto quando seleciona os itens
                    mode.setTitle(totalItemSelecionado + " item selecionado");
                }

            }
        });

        toolbarRodape.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.menu_orcamento_tab_md_adicionar:

                        // Checa se eh um orcamento
                        if (tipoOrcamentoPedido.equals("O")) {
                            // Abre a tela que lista todos os produtos
                            Intent intentOrcamento = new Intent(getContext(), ProdutoListaMDActivity.class);
                            intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, textCodigoOrcamento.getText().toString());
                            intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_ID_CLIENTE, idPessoa);
                            intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, textAtacadoVarejo.getText().toString());
                            intentOrcamento.putExtra(ProdutoListaMDActivity.KEY_NOME_RAZAO, razaoSocial.replaceFirst("- ", ""));
                            startActivity(intentOrcamento);

                        } else {
                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                            // Cria uma variavem para inserir as propriedades da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoActivity");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n"
                                    + getActivity().getResources().getString(R.string.nao_pode_ser_inserido_novos_produtos));
                            // Executa a mensagem passando por parametro as propriedades
                            funcoes.menssagem(mensagem);
                        }
                        break;

                    case R.id.menu_orcamento_tab_md_enviar_email:

                        try {
                            //Cria novo um ProgressDialogo e exibe
                            ProgressDialog progress = new ProgressDialog(getActivity());
                            progress.setMessage("Aguarde, o PDF está sendo Gerado...");
                            progress.setCancelable(false);
                            progress.show();

                            GerarPdfRotinas gerarPdfRotinas = new GerarPdfRotinas(getActivity());
                            // Envia a lista de produtos que pertence ao orcamento
                            gerarPdfRotinas.setListaItensOrcamento(adapterItemOrcamento.getListaItemOrcamento());
                            // Envia os dados do orcamento
                            gerarPdfRotinas.setOrcamento(preencheDadosOrcamento());

                            String retornoCaminho = gerarPdfRotinas.criaArquivoPdf();

                            if (retornoCaminho.length() > 0) {
                                // Fecha a barra de progresso
                                progress.dismiss();

                                File arquivo = new File(retornoCaminho);

                                PessoaRotinas pessoaRotinas = new PessoaRotinas(getActivity());

                                Intent dadosEmail = new Intent(Intent.ACTION_SEND);
                                //dadosEmail.setType("message/rfc822");
                                dadosEmail.setType("text/plain");
                                dadosEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{pessoaRotinas.emailPessoa(idPessoa)});
                                dadosEmail.putExtra(Intent.EXTRA_SUBJECT, "Orçamento/Pedido # " + textCodigoOrcamento.getText());
                                dadosEmail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + arquivo));
                                dadosEmail.putExtra(Intent.EXTRA_TEXT, "E-Mail enviado pelo App SAVARE.");

                                try {
                                    startActivity(Intent.createChooser(dadosEmail, "Enviar e-mail..."));

                                } catch (android.content.ActivityNotFoundException ex) {
                                    SuperToast.create(getContext(), getResources().getString(R.string.nao_possivel_compartilhar_arquivo), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.FLYIN)).show();
                                }
                            } else {
                                progress.dismiss();
                            }

                        } catch (Exception e) {

                        }

                        break;

                    case R.id.menu_orcamento_tab_md_pesquisa:

                        break;

                    case R.id.menu_orcamento_tab_md_atualizar:
                        onResume();
                        break;

                    case R.id.menu_orcamento_tab_md_salvar:

                        try {
                            //ContentValues dadosOrcamento = new ContentValues();
                            //dadosOrcamento.put("TIPO_ORCAMENTO", "ORCAMENTO");

                            GerarPdfAsyncRotinas gerarPdfSalvar = new GerarPdfAsyncRotinas(getActivity());
                            // Seta(envia) os dados do orcamento
                            gerarPdfSalvar.setOrcamento(preencheDadosOrcamento());
                            // Seta(envia) a lista de produtos do orcamento
                            gerarPdfSalvar.setListaItensOrcamento(adapterItemOrcamento.getListaItemOrcamento());

                            gerarPdfSalvar.execute("");

                            // Fecha a view
                            //finish();

                        } catch (Exception e) {
                            //Log.i("thread", e.getMessage());

                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                            // Cria uma variavem para inserir as propriedades da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 0);
                            mensagem.put("tela", "OrcamentoActivity");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_foi_possivel_salvar_orcamento_pdf));
                            mensagem.put("dados", e.toString());
                            mensagem.put("usuario", funcoes.getValorXml("Usuario"));
                            mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                            mensagem.put("email", funcoes.getValorXml("Email"));

                            // Executa a mensagem passando por parametro as propriedades
                            funcoes.menssagem(mensagem);
                        }
                        break;

                    case R.id.menu_orcamento_tab_md_rateio_preco:
                        // Checa se existe produtos no orcamento
                        if ( (adapterItemOrcamento != null) && (adapterItemOrcamento.getListaItemOrcamento().size() > 0) ) {

                            if (adapterItemOrcamento.getTipoItem() == adapterItemOrcamento.RATEIO_ITEM_ORCAMENTO) {
                                adapterItemOrcamento.setTipoItem(adapterItemOrcamento.ITEM_ORCAMENTO);
                                ((BaseAdapter) listViewListaProdutoOrcamento.getAdapter()).notifyDataSetChanged();

                                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

                                textTotal.setText("Total: " + orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));

                            } else {
                                adapterItemOrcamento.setTipoItem(adapterItemOrcamento.RATEIO_ITEM_ORCAMENTO);
                                ((BaseAdapter) listViewListaProdutoOrcamento.getAdapter()).notifyDataSetChanged();

                                // Variavel para armezenar o total da diferenca entro o preco vendido e o preco de tabela
                                double totalDiferenca = 0;
                                // Passa por toda a lista de itens
                                for (int i = 0; i < adapterItemOrcamento.getListaItemOrcamento().size(); i++) {
                                    totalDiferenca = totalDiferenca + (adapterItemOrcamento.getListaItemOrcamento().get(i).getValorTabela() - adapterItemOrcamento.getListaItemOrcamento().get(i).getValorLiquido());
                                }
                                // Instancia a classe de funcoes
                                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                                // Seta o campo com o total da diferenca
                                textTotal.setText("Diferença: " + funcoes.arredondarValor(String.valueOf(totalDiferenca * (-1))));
                            }
                        } else {
                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                            // Cria uma variavem para inserir as propriedades da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoFragment");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_existe_produto_lista_orcamento) + "\n");
                            // Executa a mensagem passando por parametro as propriedades
                            funcoes.menssagem(mensagem);
                        }
                        break;

                    case R.id.menu_orcamento_tab_md_transformar_pedido:


                        // Checa se eh um orcamento
                        if (tipoOrcamentoPedido.equals("O")) {

                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.formar_venda)
                                    .items(R.array.forma_venda_positivacao)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                            // Valida a opcao selecionada 1 = Visitou, mas, não comprou e 2 = Não estava
                                            if ((which != 1) && (which != 2)) {

                                                // Instancia a classe para manipular os orcamento no banco de dados
                                                OrcamentoSql orcamentoSql = new OrcamentoSql(getActivity());
                                                int totalAtualizado = 0;

                                                ContentValues dadosPedido = new ContentValues();
                                                dadosPedido.put("STATUS", "P");

                                                totalAtualizado = totalAtualizado + orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + textCodigoOrcamento.getText());

                                                // Dados da mensagem
                                                ContentValues mensagem = new ContentValues();
                                                mensagem.put("comando", 2);
                                                mensagem.put("tela", "OrcamentoFragment");

                                                // Verifica se foi deletado algum registro
                                                if (totalAtualizado > 0) {
                                                    mensagem.put("mensagem", totalAtualizado + " Transformado(s) em Pedido(s). \n");

                                                    tipoOrcamentoPedido = "P";

                                                    // Pega os dados da positivacao
                                                    String sqlInsert = "INSERT OR REPLACE INTO CFAPOSIT(STATUS, VALOR_VENDA, DATA_VISITA, ID_CFACLIFO, ID_AEAORCAM) VALUES " +
                                                            "(" + which + ", " +
                                                            "(SELECT AEAORCAM.FC_VL_TOTAL FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento + "), " +
                                                            "(SELECT (DATE('NOW', 'localtime'))), " +
                                                            "(SELECT AEAORCAM.ID_CFACLIFO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento + "), " +
                                                            idOrcamento + ")";

                                                    PositivacaoSql positivacaoSql = new PositivacaoSql(getContext());

                                                    // Inseri a positivacao e checa se inseriu com sucesso
                                                    positivacaoSql.execSQL(sqlInsert);

                                                    GerarPdfAsyncRotinas gerarPdfSalvar = new GerarPdfAsyncRotinas(getActivity());
                                                    // Seta(envia) os dados do orcamento
                                                    gerarPdfSalvar.setOrcamento(preencheDadosOrcamento());
                                                    // Seta(envia) a lista de produtos do orcamento
                                                    gerarPdfSalvar.setListaItensOrcamento(adapterItemOrcamento.getListaItemOrcamento());

                                                    gerarPdfSalvar.execute("");

                                                    // Fecha a view
                                                    //finish();

                                                } else {
                                                    mensagem.put("mensagem", getResources().getString(R.string.nao_foi_possivel_transformar_orcamento_pedido));
                                                }
                                                // Instancia a classe de funcoes
                                                FuncoesPersonalizadas funcoes;

                                                // Instancia a classe  de funcoes para mostra a mensagem
                                                funcoes = new FuncoesPersonalizadas(getActivity());
                                                funcoes.menssagem(mensagem);

                                            } else {
                                                SuperToast.create(getContext(), getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.FLYIN)).show();
                                            }
                                        }
                                    })
                                    .show();


                        } else {
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoActivity");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n");

                            // Instancia a classe de funcoes
                            FuncoesPersonalizadas funcoes;

                            funcoes = new FuncoesPersonalizadas(getActivity());
                            funcoes.menssagem(mensagem);
                        }
                        break;

                    case R.id.menu_orcamento_tab_md_trocar_cliente:
                        // Checa se eh um orcamento
                        if (tipoOrcamentoPedido.equals("O")) {
                            // Abre a tela de detalhes do produto
                            Intent intent = new Intent(getActivity(), ClienteListaMDActivity.class);
                            intent.putExtra(KEY_TELA_CHAMADA, KEY_TELA_ORCAMENTO_FRAGMENTO);
                            intent.putExtra(KEY_ID_ORCAMENTO, textCodigoOrcamento.getText().toString());
                            // Abre a activity aquardando uma resposta
                            startActivityForResult(intent, SOLICITA_CLIENTE);

                        } else {
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoFragment");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n");

                            // Instancia a classe de funcoes
                            FuncoesPersonalizadas funcoes;

                            funcoes = new FuncoesPersonalizadas(getActivity());
                            funcoes.menssagem(mensagem);
                        }
                        break;

                    case R.id.menu_orcamento_tab_md_visualizar_logs:

                        // Abre a tela inicial do sistema
                        Intent intent = new Intent(getActivity(), LogActivity.class);
                        intent.putExtra("ID_AEAORCAM", textCodigoOrcamento.getText().toString());
                        intent.putExtra("TABELA", new String[]{"AEAORCAM", "AEAITORC"});
                        startActivity(intent);

                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItemSearch = toolbarRodape.getMenu().getItem(0);
        SearchView searchView = (SearchView) menuItemSearch.getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

        /**
         * Botao para submeter a pesquisa.
         * So eh executado quando clicado no botao.
         */
        @Override
        public boolean onQueryTextSubmit(String query) {
            // Chama a funcao para carregar a lista com todos os produtos
            //criaListaDeProdutos(null, null, 1);

            adapterItemOrcamento.getFilter().filter(query);

            // Seta o adapte com a nova lista, com ou sem o filtro
            adapterItemOrcamento.setListaItemOrcamento(adapterItemOrcamento.getListaItemOrcamento());

            return false;
        } // Fim do onQueryTextSubmit


        /**
         * Pega todo o texto digitado
         */
        @Override
        public boolean onQueryTextChange(String newText) {
            // Checa se nao existe caracter no campo de pesquisa
            if (newText.length() <= 0) {
                // Seta o adapte com a nova lista, sem o filtro
                onResume();
            }
            return false;
        } // Fim do onQueryTextChange

        }); // Fim do setOnQueryTextListener

        return viewOrcamento;
    } // Fim onCreateView


    @Override
    public void onResume() {
        super.onResume();

        CarregarDadosOrcamentoProduto carregarDadosOrcamentoProduto = new CarregarDadosOrcamentoProduto(getContext(), textCodigoOrcamento.getText().toString());
        carregarDadosOrcamentoProduto.execute();

    } // Fim do onResume

    private void recuperarCampos(){
        listViewListaProdutoOrcamento = (ListView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_list_produto_orcamento);
        textCodigoOrcamento = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_text_codigo_orcamento);
        textTotal = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_text_total);
        textAtacadoVarejo = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_text_atacado_varejo);
        progressBarStatus = (ProgressBar) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_progressBar_status);
        toolbarRodape = (Toolbar) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_toolbar_rodape);
        toolbarRodape.inflateMenu(R.menu.orcamento_tab_md);
        toolbarCabecalho = (Toolbar)  getActivity().findViewById(R.id.fragment_orcamento_tab_md_toolbar_cabecalho);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                // Executa apenas a parte do onResume
                onResume();
            }
            // Checa se eh um retorno da tela de cliente
        } else if(requestCode == SOLICITA_CLIENTE){

            if(resultCode == RETORNA_CLIENTE){
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(data.getStringExtra("ID_CFACLIFO") + " - " + data.getStringExtra("NOME_CLIENTE"));
                //getActivity().getActionBar().setTitle(data.getStringExtra("ID_CFACLIFO") + " - " + data.getStringExtra("NOME_CLIENTE"));
                razaoSocial = data.getStringExtra("NOME_CLIENTE");
                idPessoa = data.getStringExtra("ID_CFACLIFO");

            } else if(requestCode == ERRO_RETORNA_CLIENTE){
                // Dados da mensagem
                ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 0);
                mensagem.put("tela", "OrcamentoFragment");
                mensagem.put("mensagem", "Não conseguimos trocar o cliente deste orçamento. \n");
                // Instancia a classe  de funcoes para mostra a mensagem
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                funcoes.menssagem(mensagem);
            }
        }
    }

    /**
     * Funcao para retornar os dados do orcamento
     * @return
     */
    protected OrcamentoBeans preencheDadosOrcamento(){
        OrcamentoBeans orcamento = new OrcamentoBeans();
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

        orcamento.setIdOrcamento(Integer.valueOf(textCodigoOrcamento.getText().toString()));
        orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml("CodigoEmpresa")));
        orcamento.setIdPessoa(Integer.valueOf(idPessoa));
        orcamento.setNomeRazao(razaoSocial);
        // Instancia a classe de rotinas do orcamento para manipular os dados com o banco
        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
        // Pega a obs do banco de dados
        orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(textCodigoOrcamento.getText().toString()));
        // Pega o total do orcamento no banco de dados
        double total = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));
        // Insere o total do orcamento varaviavel orcamento
        orcamento.setTotalOrcamento(total);
        orcamento.setDataCadastro(orcamentoRotinas.dataCadastroOrcamento(textCodigoOrcamento.getText().toString()));

        return orcamento;
    }


    public class CarregarDadosOrcamentoProduto extends AsyncTask<Void, Void, Void> {

        private Context context;
        private List<ItemOrcamentoBeans> listaItemOrcamento;
        private String codigoOrcamento = null, totalOrcamento = null;

        public CarregarDadosOrcamentoProduto(Context context, String codigoOrcamento) {
            this.context = context;
            this.codigoOrcamento = codigoOrcamento;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Visualiza a barra de status
            progressBarStatus.setVisibility(View.VISIBLE);
            // Deixa a barra de progresso de forma indeterminada
            progressBarStatus.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Checo se foi passado algum codigo de orcamento
            if (codigoOrcamento != null) {

                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
                // Pega todos os produtos do orcamento
                listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, codigoOrcamento, null, progressBarStatus);

                // Verifica se existe algum dado na lista
                if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)) {
                    // Preenche o adapter com a lista de produtos do orcamento
                    adapterItemOrcamento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.ITEM_ORCAMENTO, listaItemOrcamento);

                    // Calcula o total dos pedidos listados
                    totalOrcamento = orcamentoRotinas.totalOrcamentoLiquido(codigoOrcamento);

                } else {
                    if (adapterItemOrcamento != null) {
                        // Passa por todos da lista do adapter
                        for (int i = 0; i < adapterItemOrcamento.getCount(); i++) {
                            // Remove todos do adapter se existir
                            adapterItemOrcamento.remove(i);
                            //adapterItemOrcamento.notifyDataSetChanged();
                        }
                    }
                }
                // Pega os status do orcamento, para checar se eh um orcamento ou pedido
                tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(codigoOrcamento);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)){
                // Preenche o list com os itens no layout(adapter) personalizado
                listViewListaProdutoOrcamento.setAdapter(adapterItemOrcamento);
            }
            if (totalOrcamento != null){
                textTotal.setText("Total: " + totalOrcamento);
            }
            ParcelaRotinas parcelaRotinas = new ParcelaRotinas(getContext());
            // Checa se tem algum titulo vencido
            if (parcelaRotinas.totalReceberPagarCliente(idPessoa, ParcelaRotinas.TITULOS_EM_ABERTO_VENCIDOS, ParcelaRotinas.RECEBER, null) > 0){

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

                ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 2);
                mensagem.put("tela", "OrcamentoProdutoMDFragment");
                mensagem.put("mensagem", getResources().getString(R.string.existe_titulos_vencidos));

                funcoes.menssagem(mensagem);
            }
            progressBarStatus.setVisibility(View.GONE);
        }
    } // CarregarDadosOrcamentoProduto
}

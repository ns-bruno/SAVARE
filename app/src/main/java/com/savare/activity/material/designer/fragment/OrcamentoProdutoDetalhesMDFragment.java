package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.activity.material.designer.OrcamentoProdutoDetalhesTabFragmentMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.storedProcedure.CalculaPrecoSP;
import com.savare.beans.EmbalagemBeans;
import com.savare.beans.EstoqueBeans;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;
import com.savare.funcoes.rotinas.EmbalagemRotinas;
import com.savare.funcoes.rotinas.EstoqueRotinas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PlanoPagamentoRotinas;
import com.savare.funcoes.rotinas.ProdutoRotinas;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 21/05/2016.
 */
public class OrcamentoProdutoDetalhesMDFragment extends Fragment {

    private View viewOrcamento;
    private TextView textDescricaoProduto,
            textEstoque,
            textDescontoMaximo,
            textSequencial,
            textCodigoUnico;
    private Spinner spinnerEmbalagem,
            spinnerPlanoPagamentoPreco,
            spinnerEstoque;
    private EditText editQuantidade,
            editUnitarioLiquidoVenda,
            editDesconto,
            editValorTotalDesconto,
            editValorUnitarioDesconto,
            editTotal,
            editValorTabela,
            editObservacao;
    private ProgressBar progressStatus;
    private String telaChamada = "", razaoSocial, atacadoVarejo = "0", vistaPrazo = "0";
    private int idProduto = -1,
            idOrcamento = -1,
            idPessoa = -1,
            posicao = -1;
    private double valorUnitarioVendaAux;
    private List<PlanoPagamentoBeans> listaPlanoPagamentoPreco;
    private ProdutoListaBeans produto;
    private OrcamentoBeans orcamento;
    private ItemUniversalAdapter adapterEstoque;
    private ItemUniversalAdapter adapterEmbalagem;
    private ItemUniversalAdapter adapterPlanoPagamentoPreco;
    private long idItemOrcamento = 0;
    private boolean telaCarregada = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamento = inflater.inflate(R.layout.fragment_orcamento_produto_detalhes_md, container, false);

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        recuperarCampos();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
        funcoes.bloqueiaOrientacaoTela();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();
        // Checa se realmente foi passado dados por parametro
        if (parametro != null){
            try{
                idProduto = parametro.getInt(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU);
                idOrcamento = parametro.getInt(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAORCAM);
                idPessoa = parametro.getInt(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_CFACLIFO);
                posicao = parametro.getInt(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_POSICAO);
                telaChamada = parametro.getString(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_TELA_CHAMADA);
                razaoSocial = parametro.getString(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_RAZAO_SOCIAL);
                atacadoVarejo = parametro.getString(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ATACADO_VAREJO);

                CarregarDadosOrcamentoProduto carregarDadosOrcamentoProduto = new CarregarDadosOrcamentoProduto();
                carregarDadosOrcamentoProduto.execute();

            } catch(Exception e){
                new MaterialDialog.Builder(getContext())
                        .title("OrcamentoProdutoDetalhesActivity")
                        .content("Erro ao pegar os dados(parâmetros) do produto da outra tela (activity). \n"
                                + e.getMessage() +"\n"
                                + "Favor, voltar e selecione novamente um produto")
                        .positiveText(R.string.button_ok)
                        .show();
            } // Fim do catch
        }

        editQuantidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    if(editQuantidade.isFocused()){

                        calculaTodosCampos(editQuantidade.getId());

                    }
                }catch(Exception e){
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Erro grave no campo Quantidade (addTextChangedListener editQuantidade). \n"
                                    + e.getMessage() +"\n"
                                    + "Favor, entrar em contato com a T.I.")
                            .positiveText(R.string.button_ok)
                            .show();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        }); // Fim do editQuantidade


        editUnitarioLiquidoVenda.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editUnitarioLiquidoVenda.setText("");
                return false;
            }
        });
        editUnitarioLiquidoVenda.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    if(editUnitarioLiquidoVenda.isFocused()){

                        calculaTodosCampos(editUnitarioLiquidoVenda.getId());

                    }
                }catch(Exception e){
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Erro grave no campo Unitário (addTextChangedListener editUnitarioLiquido). \n"
                                    + e.getMessage() +"\n"
                                    + "Favor, entrar em contato com a T.I.")
                            .positiveText(R.string.button_ok)
                            .show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }); // Fim do editUnitarioLiquidoVenda


        editDesconto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editDesconto.setText("");
                return false;
            }
        });
        editDesconto.addTextChangedListener( new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    if(editDesconto.isFocused()){

                        calculaTodosCampos(editDesconto.getId());

                    }
                }catch(Exception e){
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Erro grave no campo Desconto (addTextChangedListener editDesconto). \n"
                                    + e.getMessage() +"\n"
                                    + "Favor, entrar em contato com a T.I.")
                            .positiveText(R.string.button_ok)
                            .show();
                    // Instancia a classe da mensagem
                    /*FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 0);
                    mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
                    mensagem.put("mensagem", "Erro no campo desconto (addTextChangedListener editDesconto). \n"
                            + e.getMessage() +"\n"
                            + "Favor, entrar em contato com a TI.");
                    mensagem.put("dados", e.getMessage());
                    mensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    mensagem.put("email", funcoes.getValorXml("Email"));

                    funcoes.menssagem(mensagem);*/
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        editValorTotalDesconto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editValorTotalDesconto.setText("");
                return false;
            }
        });

        editValorTotalDesconto.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if(editValorTotalDesconto.isFocused()){
                        calculaTodosCampos(editValorTotalDesconto.getId());
                    }
                } catch (Exception e) {
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Erro grave no campo Valor de Desconto (addTextChangedListener editValorTotalDesconto). \n"
                                    + e.getMessage() +"\n"
                                    + "Favor, entrar em contato com a T.I.")
                            .positiveText(R.string.button_ok)
                            .show();
                    // Instancia a classe da mensagem
                    /*FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 0);
                    mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
                    mensagem.put("mensagem", "Erro no campo valor de desconto (addTextChangedListener editValorTotalDesconto). \n"
                            + e.getMessage() +"\n"
                            + "Favor, entrar em contato com a TI.");
                    mensagem.put("dados", e.getMessage());
                    mensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    mensagem.put("email", funcoes.getValorXml("Email"));

                    funcoes.menssagem(mensagem);*/
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        editValorUnitarioDesconto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editValorUnitarioDesconto.setText("");
                return false;
            }
        });

        editValorUnitarioDesconto.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if(editValorUnitarioDesconto.isFocused()){
                        calculaTodosCampos(editValorUnitarioDesconto.getId());
                    }
                } catch (Exception e) {
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Erro no campo valor de desconto (addTextChangedListener editValorUnitarioDesconto). \n"
                                    + e.getMessage() +"\n"
                                    + "Favor, entrar em contato com a TI.")
                            .positiveText(R.string.button_ok)
                            .show();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        editTotal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTotal.setText("");
                return false;
            }
        });
        editTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    if(editTotal.isFocused()){

                        calculaTodosCampos(editTotal.getId());

                    }
                }catch(Exception e){
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Erro no campo valor de desconto (addTextChangedListener editTotal). \n"
                                    + e.getMessage() +"\n"
                                    + "Favor, entrar em contato com a TI.")
                            .positiveText(R.string.button_ok)
                            .show();
                    // Instancia a classe da mensagem
                    /*FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 0);
                    mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
                    mensagem.put("mensagem", "Erro grave no campo Total (addTextChangedListener editTotal). \n"
                            + e.getMessage() +"\n"
                            + "Favor, entrar em contato com a TI.");
                    mensagem.put("dados", e.getMessage());
                    mensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    mensagem.put("email", funcoes.getValorXml("Email"));

                    funcoes.menssagem(mensagem);*/
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        }); // Fim editTotal


        spinnerPlanoPagamentoPreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Checa se carregou alguma coisa na lista
                if (adapterPlanoPagamentoPreco != null) {
                    // Muda os valores dos campos de acordo com o selecionado
                    valorUnitarioVendaAux = adapterPlanoPagamentoPreco.getListaPlanoPagamento().get(position).getPrecoProduto();
                }

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

                if((produto.getEstaNoOrcamento() != '1') || (telaCarregada == true)){
                    editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
                    editValorTabela.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
                }
                calculaTodosCampos(spinnerPlanoPagamentoPreco.getId());
                telaCarregada = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerEmbalagem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

                if ((adapterEmbalagem != null) && (adapterEmbalagem.getListaEmbalagem() != null) &&
                        (adapterEmbalagem.getListaEmbalagem().get(position) != null)){
                    calculaTodosCampos(spinnerEmbalagem.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return viewOrcamento;
    } // Fim onCreate

    @Override
    public void onResume() {
        super.onResume();

        // Carrega os dados do estoque
        //carregarDadosEstoque(""+produto.getProduto().getIdProduto());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.orcamento_produto_detalhes_md, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_orcamento_produto_detalhes_md_fragment_salvar:

                SalvarDadosOrcamentoProduto salvarDadosORcamentoProduto = new SalvarDadosOrcamentoProduto();
                salvarDadosORcamentoProduto.execute();
                break;

            case R.id.menu_orcamento_produto_detalhes_md_fragment_deletar:

                if(this.produto.getEstaNoOrcamento() == '1'){

                    AlertDialog.Builder builderConfirmacao = new AlertDialog.Builder(getActivity());
                    builderConfirmacao.setMessage("Tem certeza que deseja excluir o(s) item(ns)?")
                    .setPositiveButton(getContext().getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // Instancia a classe para manipular os produto no banco de dados
                            ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(getActivity());
                            // Deleta o item
                            if ((itemOrcamentoSql.delete("AEAITORC.ID_AEAITORC = " + idItemOrcamento)) > 0) {
                                //SuperToast.create(getActivity(), getContext().getResources().getString(R.string.excluido_sucesso), Style.DURATION_SHORT, new Style(Style.green(), SuperToast.Animations.POPUP)).show();

                                SuperActivityToast.create(getActivity(), getContext().getResources().getString(R.string.excluido_sucesso), Style.DURATION_SHORT)
                                        .setTextColor(Color.WHITE)
                                        .setColor(Color.RED)
                                        .setAnimations(Style.ANIMATIONS_POP)
                                        .show();

                                // Cria uma intent para returnar um valor para activity ProdutoLista
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("RESULTADO", '0');
                                // Pega a posicao do deste produto na lista de produtos
                                returnIntent.putExtra("POSICAO", posicao);
                                returnIntent.putExtra("ID_AEAITORC", idItemOrcamento);

                                // Checa se se quem chemou foi a tela de lista de de orçamento sem associacao de orcamento
                                if ( (telaChamada != null) && (telaChamada.equalsIgnoreCase("ProdutoListaActivity")) ){
                                    getActivity().setResult(101, returnIntent);
                                } else {
                                    getActivity().setResult(getActivity().RESULT_OK, returnIntent);
                                }

                                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                                funcoes.desbloqueiaOrientacaoTela();
                                // Fecha a tela de detalhes de produto
                                getActivity().finish();
                            }
                        }
                    })
                    .setNegativeButton(getContext().getResources().getString(R.string.nao), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    // Create the AlertDialog object and return it
                    builderConfirmacao.create();
                    builderConfirmacao.show();
                    // Envia os dados do produto para inserir no banco de dados
                } else {
                    //SuperActivityToast.create(getActivity(), getContext().getResources().getString(R.string.produto_nao_esta_orcamento), SuperToast.Duration.SHORT, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                    SuperActivityToast.create(getActivity(), getContext().getResources().getString(R.string.produto_nao_esta_orcamento), Style.DURATION_SHORT)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Acossia a variaveis com os campos para poder manipular os campos
     * na view (activity).
     */
    private void recuperarCampos(){
        textDescricaoProduto = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_text_descricao_produto);
        textEstoque = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_text_estoque);
        textDescontoMaximo = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_text_desconto_maximo);
        textSequencial = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_text_sequencial);
        textCodigoUnico = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_text_codigo_unico);
        spinnerEmbalagem = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_spinner_embalagem);
        spinnerPlanoPagamentoPreco = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_spinner_unitario_tabela);
        spinnerEstoque = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_spinner_estoque);
        editQuantidade = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_quantidade);
        editUnitarioLiquidoVenda = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_unitario_liquido);
        editDesconto = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_desconto);
        editValorTotalDesconto = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_valor_desconto);
        editValorUnitarioDesconto = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_valor_unitario_desconto);
        editTotal = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_total);
        editValorTabela = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_valor_tabela);
        editObservacao = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_edit_observacao);
        progressStatus = viewOrcamento.findViewById(R.id.activity_orcamento_produto_detalhes_md_progress_status);
    } // Fim recuperaCampos


    public class CarregarDadosOrcamentoProduto extends AsyncTask<Void, Void, Void> {
        int posicaoPlanoPgto = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressStatus.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Checa se tem algum id de produto
            if (idProduto > 0){
                ProdutoRotinas produtoRotinas = new ProdutoRotinas(getContext());
                // Checa se passou algum numero de orcamento
                if (idOrcamento > 0) {

                    EmbalagemRotinas embalagemRotinas = new EmbalagemRotinas(getContext());
                    // Pega a lista de embalagem do produto
                    List<EmbalagemBeans> listaEmbalagem = embalagemRotinas.selectEmbalagensProduto(String.valueOf(idProduto));
                    // Checa se retornou alguma coisa
                    if ((listaEmbalagem != null) && (listaEmbalagem.size() > 0)) {
                        // Preenche o adapter de embalagem com uma lista
                        adapterEmbalagem = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.EMBALAGEM);
                        adapterEmbalagem.setListaEmbalagem(listaEmbalagem);
                    } else {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(getContext())
                                        .title("OrcamentoProdutoDetalhesActivity")
                                        .content(getResources().getString(R.string.nao_conseguimos_pegar_embalagem_produto))
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
                    }

                    // Instancia a rotinas para buscar os dados
                    PlanoPagamentoRotinas planoRotinas = new PlanoPagamentoRotinas(getContext());
                    // Instancia a lista
                    listaPlanoPagamentoPreco = new ArrayList<PlanoPagamentoBeans>();
                    // Recebe os dados do banco
                    listaPlanoPagamentoPreco = planoRotinas.listaPlanoPagamento("ATIVO = '1' AND ENVIA_PALM = '1'", "DESCRICAO, CODIGO", String.valueOf(atacadoVarejo));
                    // Verifica se retornou a lista de plano de pagamento
                    if ((listaPlanoPagamentoPreco != null) && (listaPlanoPagamentoPreco.size() > 0)) {
                        adapterPlanoPagamentoPreco = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.PLANO_PAGAMENTO_ORCAMENTO);
                        adapterPlanoPagamentoPreco.setListaPlanoPagamento(listaPlanoPagamentoPreco);

                    } else {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                new MaterialDialog.Builder(getContext())
                                        .title("OrcamentoProdutoDetalhesActivity")
                                        .content(getResources().getString(R.string.nao_conseguimos_pegar_plano_pagamento))
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
                    }

                    // Instancia a classe de rotinas do estoque
                    EstoqueRotinas estoqueRotinas = new EstoqueRotinas(getContext());
                    List<EstoqueBeans> listaEstoque = estoqueRotinas.listaEstoqueProduto(String.valueOf(idProduto), "(AEALOCES.TIPO_VENDA = '" + atacadoVarejo + "') OR (AEALOCES.TIPO_VENDA = '2')");
                    // Verifica se retornou a lista de estoque
                    if ((listaEstoque != null) && (listaEstoque.size() > 0) ) {
                        adapterEstoque = new ItemUniversalAdapter(getContext(), ItemUniversalAdapter.ESTOQUE);
                        // Inseri uma lista dentro do adapter
                        adapterEstoque.setListaEstoque(listaEstoque);
                    } else {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            public void run() {
                                //funcoes.menssagem(mensagem);
                                new MaterialDialog.Builder(getContext())
                                        .title("OrcamentoProdutoDetalhesActivity")
                                        .content(getResources().getString(R.string.nao_conseguimos_pegar_lista_estoque))
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        });
                    }

                    produto = produtoRotinas.listaProduto(  "AEAPRODU.ID_AEAPRODU = " + idProduto,
                                                            null,
                                                            String.valueOf(idOrcamento),
                                                            null,
                                                            null,
                                                            Rotinas.NAO,
                                                            adapterPlanoPagamentoPreco.getListaPlanoPagamento().get(posicaoPlanoPgto).getIdPlanoPagamento(),
                                                            Rotinas.SIM).get(0);

                    orcamento = new OrcamentoBeans();

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

                    orcamento.setIdOrcamento(idOrcamento);
                    orcamento.setIdEmpresa(Integer.valueOf(funcoes.getValorXml(FuncoesPersonalizadas.TAG_CODIGO_EMPRESA)));
                    orcamento.setIdPessoa(idPessoa);
                    orcamento.setNomeRazao(razaoSocial);
                    // Instancia a classe de rotinas do orcamento para manipular os dados com o banco
                    OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getContext());
                    // Pega a obs do banco de dados
                    orcamento.setObservacao(orcamentoRotinas.selectObservacaoOrcamento(String.valueOf(idOrcamento)));
                    // Pega o total do orcamento no banco de dados
                    double total = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoLiquido(String.valueOf(idOrcamento)));
                    // Insere o total do orcamento varaviavel orcamento
                    orcamento.setTotalOrcamento(total);
                    orcamento.setDataCadastro(orcamentoRotinas.dataCadastroOrcamento(String.valueOf(idOrcamento)));
                    orcamento.setTipoVenda(atacadoVarejo);

                } else {
                    // Pega lista sem associar com um orcamento
                    produto = produtoRotinas.listaProduto(  "AEAPRODU.ID_AEAPRODU = " + idProduto,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                             Rotinas.NAO,
                                                             adapterPlanoPagamentoPreco.getListaPlanoPagamento().get(posicaoPlanoPgto).getIdPlanoPagamento(),
                                                             Rotinas.SIM).get(0);
                }
                // Pega se a venda eh no atacado ou varejo
                produto.setAtacadoVarejo(atacadoVarejo.charAt(0));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (adapterEmbalagem != null) {
                // Preenche o spinner com o adapter
                spinnerEmbalagem.setAdapter(adapterEmbalagem);
            }
            if (adapterPlanoPagamentoPreco != null){
                spinnerPlanoPagamentoPreco.setAdapter(adapterPlanoPagamentoPreco);
                PlanoPagamentoRotinas planoRotinas = new PlanoPagamentoRotinas(getContext());
                // Pega o posicionamento em que a lista de plano de pagamento eh pra ficar, pega do ultimo item do pedido ou do plano padrao da empresa
                posicaoPlanoPgto = planoRotinas.posicaoPlanoPagamentoLista(listaPlanoPagamentoPreco, String.valueOf(orcamento.getIdOrcamento()));
                spinnerPlanoPagamentoPreco.setSelection(posicaoPlanoPgto);
            }
            if (adapterEstoque != null){
                // Inseri o adapter dentro do spinner
                spinnerEstoque.setAdapter(adapterEstoque);
            }

            // Checa se as variaveis nao estao vazias
            if ((produto != null) && (produto.getProduto() != null) && (orcamento != null)) {
                carregarDadosDoProduto(produto, orcamento);

                //carregarDadosEstoque(String.valueOf(produto.getProduto().getIdProduto()));
            }
            progressStatus.setVisibility(View.GONE);
        }
    } // CarregarDadosOrcamentoProduto


    public class SalvarDadosOrcamentoProduto extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressStatus.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            salvarProdutoOrcamento();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressStatus.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Funcao para carregar os dados do produto em seu devidos campos.
     *
     * @param produtoVenda
     * @param orcamento
     */
    private void carregarDadosDoProduto(ProdutoListaBeans produtoVenda, OrcamentoBeans orcamento){
        // Preenche o titulo da action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(orcamento.getIdOrcamento() + " - " + orcamento.getNomeRazao());

        // Preenche o campos da descricao do produtos
        textDescricaoProduto.setText(produtoVenda.getProduto().getCodigoEstrutural() + " - " + produtoVenda.getProduto().getDescricaoProduto() + " - " +
                (produtoVenda.getProduto().getDescricaoMarca() != null ? produtoVenda.getProduto().getDescricaoMarca() : ""));
        // Instanci classe de funcoes
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

        textEstoque.setText(funcoes.arredondarValor(String.valueOf(produtoVenda.getEstoqueFisico())));

        // Verifica se o estoque eh menor que zero
        if(produtoVenda.getEstoqueFisico() < 1){
            textEstoque.setTextColor(getResources().getColor(R.color.vermelho_escuro));
        }
        // Verifica se a venda eh do atacado
        if(String.valueOf(produtoVenda.getAtacadoVarejo()).equals("0")){

            valorUnitarioVendaAux = produtoVenda.getValorUnitarioAtacado();
            // Preence o campo com o valor do produto
            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
            editValorTabela.setText(funcoes.arredondarValor(valorUnitarioVendaAux));

            // Verifica se o produto esta na promocao
            if ( (produtoVenda.getProdutoPromocaoAtacado() != null) && (produtoVenda.getProdutoPromocaoAtacado().equalsIgnoreCase("1")) ){
                editValorTabela.setTextColor(getResources().getColor(R.color.laranja_escuro));
            }

            // Verifica se a venda eh do varejo
        } else if(String.valueOf(produtoVenda.getAtacadoVarejo()).equals("1")){
            valorUnitarioVendaAux = produtoVenda.getValorUnitarioVarejo();
            // Preence o campo com o valor do produto
            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioVendaAux));
            editValorTabela.setText(funcoes.arredondarValor(valorUnitarioVendaAux));

            // Verifica se o produto esta na promocao
            if ( ((produtoVenda.getProdutoPromocaoVarejo() != null) && (produtoVenda.getProdutoPromocaoVarejo().equalsIgnoreCase("1"))) ||
                 ((produtoVenda.getProdutoPromocaoServico() != null) && (produtoVenda.getProdutoPromocaoServico().equalsIgnoreCase("1")))  ){
                editValorTabela.setTextColor(getResources().getColor(R.color.laranja_escuro));
            }
        } // Fim do if do varejo
        // Verfica se o produto ja esta no orcamento
        if(produtoVenda.getEstaNoOrcamento() == '1'){
            // Instancia a classe de rotinas
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getContext());
            ItemOrcamentoBeans itemOrcamentoBeans = new ItemOrcamentoBeans();

            // Pega os dados de um determinado produto no orcamento usando o idProduto e o idOrcamento
            itemOrcamentoBeans = orcamentoRotinas.selectItemOrcamento(String.valueOf(orcamento.getIdOrcamento()), String.valueOf(produto.getProduto().getIdProduto()));
            // Preenche o campo com a quantidade que foi comprado
            editQuantidade.setText(funcoes.arredondarValor(itemOrcamentoBeans.getQuantidade()));
            // Move o cursor para o final do campo
            editQuantidade.setSelection(editQuantidade.length());
            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getQuantidade()));
            editTotal.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorLiquido()));
            editObservacao.setText(itemOrcamentoBeans.getComplemento());
            editDesconto.setText(funcoes.arredondarValor(((((itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getValorBruto())*100)-100)* -1)));
            editValorTotalDesconto.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorDesconto()));
            editValorUnitarioDesconto.setText(funcoes.arredondarValor(itemOrcamentoBeans.getValorDesconto() / itemOrcamentoBeans.getQuantidade()));
            textSequencial.setText(""+itemOrcamentoBeans.getSequencia());
            textCodigoUnico.setText(itemOrcamentoBeans.getGuid());
            this.idItemOrcamento = itemOrcamentoBeans.getIdItemOrcamento();
            //this.valorUnitarioVendaAux = Double.valueOf(funcoes.arredondarValor(String.valueOf((itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getQuantidade()))).replace(".", "").replace(",", "."));
            this.valorUnitarioVendaAux = (itemOrcamentoBeans.getValorLiquido() / itemOrcamentoBeans.getQuantidade());
        } else {
            textSequencial.setText("");
            textCodigoUnico.setText("");
        }

    } // Fim do carregarDadosDoProduto


    /**
     * Calcula os campos de valores da tela de detalhes de produtos,
     * para salvar no pedidos.
     *
     * @param campoChamada
     */
    private void calculaTodosCampos(int campoChamada){
        double quantidade = 0,
                //valorUnitarioBruto = 0,
                valorUnitarioLiquido = 0,
                percentualDesconto = 0,
                totalLiquido = 0,
                valorDesconto = 0,
                valorUnitarioDesconto = 0;

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

        // Checa se tem quantidade digitada
        if((editQuantidade != null) && (editQuantidade.getText().length() > 0)){
            quantidade = funcoes.desformatarValor(editQuantidade.getText().toString());
        }

        // Checa se tem unitario digitado
        if((editUnitarioLiquidoVenda != null) && (editUnitarioLiquidoVenda.getText().length() > 0)){
            valorUnitarioLiquido = funcoes.desformatarValor(editUnitarioLiquidoVenda.getText().toString());
        }

        // Checa se tem percentual de desconto digitado
        if((editDesconto != null) && (editDesconto.getText().length() > 0)){
            percentualDesconto = funcoes.desformatarValor(editDesconto.getText().toString());
        }

        // Checa se tem algum  total digitado
        if((editTotal != null) && (editTotal.getText().length() > 0)){
            totalLiquido = funcoes.desformatarValor(editTotal.getText().toString());
        }

        if((editValorTotalDesconto != null) && (editValorTotalDesconto.getText().length() > 0)){
            valorDesconto = funcoes.desformatarValor(editValorTotalDesconto.getText().toString());
        }

        if((editValorUnitarioDesconto != null) && (editValorUnitarioDesconto.getText().length() > 0)){
            valorUnitarioDesconto = funcoes.desformatarValor(editValorUnitarioDesconto.getText().toString());
        }

        // Checa se o campo que esta chamando esta funcao eh o campo quantidade
        if(campoChamada == editQuantidade.getId()){
            valorUnitarioLiquido = (this.valorUnitarioVendaAux - (this.valorUnitarioVendaAux * (percentualDesconto / 100)));
            totalLiquido = (valorUnitarioLiquido * quantidade);
            valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
            valorUnitarioDesconto = (valorDesconto / quantidade);

            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
            editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
            editValorTotalDesconto.setText(funcoes.arredondarValor(valorDesconto));
            editValorUnitarioDesconto.setText(funcoes.arredondarValor(valorUnitarioDesconto));
            editTotal.setText(funcoes.arredondarValor(totalLiquido));
        }

        // Checa se o campo que esta chamando esta funcao eh o campo quantidade ou desconto
        if(campoChamada == editDesconto.getId()){
            valorUnitarioLiquido = (this.valorUnitarioVendaAux - (this.valorUnitarioVendaAux * (percentualDesconto / 100)));
            totalLiquido = (valorUnitarioLiquido * quantidade);
            valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
            valorUnitarioDesconto = (valorDesconto / quantidade);

            editQuantidade.setText(funcoes.arredondarValor(quantidade));
            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
            editTotal.setText(funcoes.arredondarValor(totalLiquido));
            editValorTotalDesconto.setText(funcoes.arredondarValor(valorDesconto));
            editValorUnitarioDesconto.setText(funcoes.arredondarValor(valorUnitarioDesconto));
        }

        if(campoChamada == editValorTotalDesconto.getId()){
            valorUnitarioLiquido = ((this.valorUnitarioVendaAux * quantidade) - valorDesconto) / quantidade;
            totalLiquido = (valorUnitarioLiquido * quantidade);
            percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
            valorUnitarioDesconto = (valorDesconto / quantidade);

            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
            editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
            editValorUnitarioDesconto.setText(funcoes.arredondarValor(valorUnitarioDesconto));
            editTotal.setText(funcoes.arredondarValor(totalLiquido));
        }

        if(campoChamada == editValorUnitarioDesconto.getId()){
            valorUnitarioLiquido = ((this.valorUnitarioVendaAux) - valorUnitarioDesconto);
            totalLiquido = (valorUnitarioLiquido * quantidade);
            percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
            valorDesconto = (valorUnitarioDesconto * quantidade);

            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
            editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
            editValorTotalDesconto.setText(funcoes.arredondarValor(valorDesconto));
            editTotal.setText(funcoes.arredondarValor(totalLiquido));
        }

        // Checa se o campo que esta chamando esta funcao eh o campo unitario liquido
        if(campoChamada == editUnitarioLiquidoVenda.getId()){
            percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
            totalLiquido = (valorUnitarioLiquido * quantidade);
            valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
            valorUnitarioDesconto = (valorDesconto / quantidade);

            // Seta os campos com os novos valores
            editQuantidade.setText(funcoes.arredondarValor(quantidade));
            editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
            editValorTotalDesconto.setText(funcoes.arredondarValor(valorDesconto));
            editValorUnitarioDesconto.setText(funcoes.arredondarValor(valorUnitarioDesconto));
            editTotal.setText(funcoes.arredondarValor(totalLiquido));
        }

        // Checa se o campo que esta chamando esta funcao eh o campo total
        if(campoChamada == editTotal.getId()){
            valorUnitarioLiquido = (totalLiquido / quantidade);
            percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100 ) - 100) * -1;
            valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
            valorUnitarioDesconto = (valorDesconto / quantidade);

            // Seta os campos com os novos valores
            editQuantidade.setText(funcoes.arredondarValor(quantidade));
            editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
            editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
            editValorTotalDesconto.setText(funcoes.arredondarValor(valorDesconto));
            editValorUnitarioDesconto.setText(funcoes.arredondarValor(valorUnitarioDesconto));
        }

        if((campoChamada == spinnerPlanoPagamentoPreco.getId()) || (campoChamada == spinnerEmbalagem.getId()) ){
            if (  ((adapterEmbalagem != null) && (adapterEmbalagem.getListaEmbalagem() != null) && (adapterEmbalagem.getListaEmbalagem().size() > 0)) &&
                    ((listaPlanoPagamentoPreco != null) && (listaPlanoPagamentoPreco.size() > 0))) {
                //aqui quando mudar o plano recalcular o preço
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd ");

                String d = mdformat.format(calendar.getTime());

                CalculaPrecoSP calculaPrecoSP = new CalculaPrecoSP(getContext(), null, null);
                ContentValues retornoPreco = calculaPrecoSP.execute(produto.getIdPLoja(),
                        adapterEmbalagem.getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getIdEmbalagem(),
                        listaPlanoPagamentoPreco.get(spinnerPlanoPagamentoPreco.getSelectedItemPosition()).getIdPlanoPagamento(),
                        idPessoa,
                        Integer.parseInt(funcoes.getValorXml(FuncoesPersonalizadas.TAG_CODIGO_USUARIO)),
                        mdformat.format(calendar.getTime()),
                        produto.getValorTabelaAtacado(),
                        produto.getValorTabelaVarejo());

                if (retornoPreco != null) {
                    // Checa se eh atacado
                    if (atacadoVarejo.equalsIgnoreCase("0")) {
                        this.valorUnitarioVendaAux = retornoPreco.getAsDouble(CalculaPrecoSP.KEY_PRECO_ATACADO);
                    } else {
                        this.valorUnitarioVendaAux = retornoPreco.getAsDouble(CalculaPrecoSP.KEY_PRECO_VAREJO);
                    }
                    valorUnitarioLiquido = this.valorUnitarioVendaAux;
                }
                if (quantidade > 0) {
                    percentualDesconto = (((valorUnitarioLiquido / valorUnitarioVendaAux) * 100) - 100) * -1;

                    if (percentualDesconto > 0) {
                        valorUnitarioLiquido = (this.valorUnitarioVendaAux - (this.valorUnitarioVendaAux * (percentualDesconto / 100)));
                    }
                    totalLiquido = (valorUnitarioLiquido * quantidade);
                    valorDesconto = ((this.valorUnitarioVendaAux * quantidade) - totalLiquido);
                    valorUnitarioDesconto = (valorDesconto / quantidade);

                    // Seta os campos com os novos valores
                    editQuantidade.setText(funcoes.arredondarValor(quantidade));
                    editDesconto.setText(funcoes.arredondarValor(percentualDesconto));
                    editValorTotalDesconto.setText(funcoes.arredondarValor(valorDesconto));
                    editValorUnitarioDesconto.setText(funcoes.arredondarValor(valorUnitarioDesconto));
                    editTotal.setText(funcoes.arredondarValor(totalLiquido));
                } else {
                    editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(this.valorUnitarioVendaAux));
                }
                //editUnitarioLiquidoVenda.setText(funcoes.arredondarValor(valorUnitarioLiquido));
                editValorTabela.setText(funcoes.arredondarValor(this.valorUnitarioVendaAux));
            } else {
                new MaterialDialog.Builder(getContext())
                        .title("OrcamentoProdutoDetalhesActivity")
                        .content("Não tem embalagem, ou não tem plano de pagamento cadastrado para fazer o calculo do preço.")
                        .positiveText(R.string.button_ok)
                        .show();
            }
        }

    } // Fim calculaTodosCampos

    /**
     * Valida os dados preenchidos nos campos.
     *
     * @return
     */
    private boolean validarDados(){
        boolean dadosValidos = true;
        // Instancia as rotinas de produtos
        ProdutoRotinas produtoRotinas = new ProdutoRotinas(getContext());
        // Pega o id da embalagem
        int idEmbalagem =  adapterEmbalagem.getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getIdEmbalagem();
        //int idEmbalagem =  produto.getProduto().getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getIdEmbalagem();

        // Pega a quantidade de casas decimais no cadastro do produto
        //int casasDecimais = produtoRotinas.casasDecimaisProduto(""+idEmbalagem, ""+produto.getProduto().getIdProduto());
        int casasDecimais = 3;

        // Checa se existe ponto
        if( (!editQuantidade.getText().toString().isEmpty()) ){

            if ( (editQuantidade.getText().toString().indexOf(".") > 0) ){
                // Pega as casas decimais da quantidade digitada
                String cdAux = editQuantidade.getText().toString().substring(editQuantidade.getText().toString().indexOf(".") + 1);
                // Converte o valor pego apos a virgula
                int decimal = Integer.parseInt(cdAux);

                if(decimal > 0){
                    // Checa se a quantidade de casas decimais esta liberado
                    if(cdAux.length() > casasDecimais){
                        // Retorna falso para informar que os dados digitados nao sao validos
                        dadosValidos = false;

                        new MaterialDialog.Builder(getContext())
                                .title("OrcamentoProdutoDetalhesActivity")
                                .content("Quatidade de digitos após a virgula permitido é igual a " + casasDecimais
                                        + "\n Favor, voltar e digites uma nova quantidade.")
                                .positiveText(R.string.button_ok)
                                .show();
                        dadosValidos = false;
                    }
                }
            }

            if (Double.parseDouble(editQuantidade.getText().toString()) <= 0){
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        //funcoes.menssagem(mensagem);
                        new MaterialDialog.Builder(getContext())
                                .title("OrcamentoProdutoDetalhesActivity")
                                .content("Quantidade do produto esta zerada ou menor que zero.")
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
                dadosValidos = false;
            }
        }

        if ((adapterEstoque.getListaEstoque() == null) || (adapterEstoque.getListaEstoque().size() <= 0)){

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(mensagem);
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Não tem estoque selecionado."
                                   + "\n Favor, entrar em contato com o administrador de TI da sua empresa para que possa enviar os dados corretos do produto.")
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
            dadosValidos = false;
        }

        if ((listaPlanoPagamentoPreco == null) || (listaPlanoPagamentoPreco.get(spinnerPlanoPagamentoPreco.getSelectedItemPosition()) == null)){
            // Dados da mensagem
            /*final ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
            mensagem.put("mensagem", "Não existe plano de pagamento. \n"
                       + "Favor, entrar em contato com o administrador de TI da sua empresa para que possa enviar os dados corretos do produto.");
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext()); */

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(mensagem);
                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Não existe plano de pagamento. \n"
                                    + "Favor, entrar em contato com o administrador de TI da sua empresa para que possa enviar os dados corretos do produto.")
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

            dadosValidos = false;
        } else if((listaPlanoPagamentoPreco != null) && (listaPlanoPagamentoPreco.get(spinnerPlanoPagamentoPreco.getSelectedItemPosition()).getIdPlanoPagamento() == 0)){
            // Dados da mensagem
            /*final ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "OrcamentoProdutoDetalhesActivity");
            mensagem.put("mensagem", "Não foi selecionado um plano de pagamento. \n"
                       + "Favor, Selecione um plano de pagamento.");
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());*/

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(mensagem);

                    new MaterialDialog.Builder(getContext())
                            .title("OrcamentoProdutoDetalhesActivity")
                            .content("Não foi selecionado um plano de pagamento. \n"
                                    + "Favor, Selecione um plano de pagamento.")
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });

            dadosValidos = false;
        }
        return dadosValidos;
    } // Fim validarDados

    private void salvarProdutoOrcamento(){
        if((editQuantidade != null) && (!editQuantidade.getText().equals("")) && (!editQuantidade.getText().toString().isEmpty())){
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

            // Checa os dados informado
            if(validarDados()){
                // Calcula os valores necessario para salvar no banco de dados
                double quantidade = funcoes.desformatarValor(editQuantidade.getText().toString()), //Double.parseDouble(editQuantidade.getText().toString()),
                        vlCusto = (this.produto.getCustoCompleto() * quantidade),
                        vlBruto = (this.valorUnitarioVendaAux * quantidade),
                        vlTabela = 0,
                        totalDigitadoLiquido = funcoes.desformatarValor(editTotal.getText().toString()),
                        vlDesconto = vlBruto - totalDigitadoLiquido,
                        fcCustoUn = vlCusto / quantidade,
                        fcBrutoUn = vlBruto / quantidade,
                        fcDescontoUn = vlDesconto / quantidade,
                        fcLiquido = totalDigitadoLiquido;

                if ((adapterEmbalagem != null) && (adapterEmbalagem.getListaEmbalagem() != null) &&
                        (adapterEmbalagem.getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()) != null)) {

                    double fatorConversao = adapterEmbalagem.getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getFatorConversao();
                    double fatorPreco = adapterEmbalagem.getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getFatorPreco();
                    // Checa se a venda eh no atacado
                    if (atacadoVarejo.equalsIgnoreCase("0")) {
                        vlTabela =  funcoes.desformatarValor(funcoes.arredondarValor(this.produto.getValorTabelaAtacado() * fatorConversao * fatorPreco));
                    } else if (atacadoVarejo.equalsIgnoreCase("1")){
                        vlTabela = funcoes.desformatarValor(funcoes.arredondarValor(this.produto.getValorTabelaVarejo() * fatorConversao * fatorPreco));
                    }
                }
                //Pega os dados do produto
                ContentValues produto = new ContentValues();
                produto.put("ID_AEAORCAM", this.orcamento.getIdOrcamento());
                produto.put("ID_AEAESTOQ", adapterEstoque.getListaEstoque().get(spinnerEstoque.getSelectedItemPosition()).getIdEstoque());
                produto.put("ID_AEAPLPGT", this.listaPlanoPagamentoPreco.get(spinnerPlanoPagamentoPreco.getSelectedItemPosition()).getIdPlanoPagamento());
                produto.put("ID_AEAUNVEN", adapterEmbalagem.getListaEmbalagem().get(spinnerEmbalagem.getSelectedItemPosition()).getIdUnidadeVenda());
                produto.put("ID_CFACLIFO_VENDEDOR", funcoes.getValorXml(FuncoesPersonalizadas.TAG_CODIGO_USUARIO));
                produto.put("ID_AEAPRODU", this.produto.getProduto().getIdProduto());
                produto.put("QUANTIDADE", quantidade);
                produto.put("VL_CUSTO", funcoes.desformatarValor(funcoes.arredondarValor(vlCusto)));
                produto.put("VL_BRUTO", funcoes.desformatarValor(funcoes.arredondarValor(vlBruto)));
                produto.put("VL_TABELA", funcoes.desformatarValor(funcoes.arredondarValor(vlTabela * quantidade)));
                produto.put("VL_TABELA_UN", funcoes.desformatarValor(funcoes.arredondarValor(vlTabela)));
                produto.put("VL_DESCONTO", funcoes.desformatarValor(funcoes.arredondarValor(vlDesconto)));
                produto.put("FC_DESCONTO_UN", funcoes.desformatarValor(funcoes.arredondarValor((vlDesconto / quantidade))));
                produto.put("FC_CUSTO_UN", funcoes.desformatarValor(funcoes.arredondarValor(fcCustoUn)));
                produto.put("FC_BRUTO_UN", funcoes.desformatarValor(funcoes.arredondarValor(fcBrutoUn)));
                produto.put("FC_DESCONTO_UN", funcoes.desformatarValor(funcoes.arredondarValor(fcDescontoUn)));
                produto.put("FC_LIQUIDO", funcoes.desformatarValor(funcoes.arredondarValor(fcLiquido)));
                produto.put("FC_LIQUIDO_UN", funcoes.desformatarValor(funcoes.arredondarValor((fcLiquido / quantidade))));
                produto.put("COMPLEMENTO", editObservacao.getText().toString());
                produto.put("TIPO_PRODUTO", String.valueOf(this.produto.getProduto().getTipoProduto()));
                // Instancia classe para manipular o orcamento
                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getContext());

                // Verifica se o produto ja esta no orcamento
                if(this.produto.getEstaNoOrcamento() == '1'){

                    // Verifica se atualizou com sucesso
                    orcamentoRotinas.updateItemOrcamento(produto, String.valueOf(this.idItemOrcamento));
                    //funcoes.desbloqueiaOrientacaoTela();
                    // Fecha a tela de detalhes de produto
                    //getActivity().finish();
                    // Envia os dados do produto para inserir no banco de dados
                } else {
                    // Salva a proxima sequencia do item
                    produto.put("SEQUENCIA", "(SELECT IFNULL((MAX(SEQUENCIA)), 0) + 1 AS SEQUENCIA FROM AEAITORC WHERE ID_AEAORCAM = " +(String.valueOf(this.orcamento.getIdOrcamento())) + ")");
                    //produto.put("SEQUENCIA", orcamentoRotinas.proximoSequencial(String.valueOf(this.orcamento.getIdOrcamento())));
                    produto.put("GUID", orcamentoRotinas.gerarGuid());

                    if((this.idItemOrcamento = orcamentoRotinas.insertItemOrcamento(produto)) > 0){
                        // Cria uma intent para returnar um valor para activity ProdutoLista
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("RESULTADO", '1');
                        // Pega a posicao deste produto na lista de produtos
                        returnIntent.putExtra("POSICAO", posicao);
                        returnIntent.putExtra("ID_AEAITORC", this.idItemOrcamento);

                        // Checa se se quem chemou foi a tela de lista de de orçamento sem associacao de orcamento
                        if ( (telaChamada != null) && (telaChamada.equalsIgnoreCase("ProdutoListaActivity")) ){
                            getActivity().setResult(101, returnIntent);
                        } else {
                            getActivity().setResult(getActivity().RESULT_OK, returnIntent);
                        }
                    }
                }
                funcoes.desbloqueiaOrientacaoTela();
                // Fecha a tela de detalhes de produto
                getActivity().finish();
            } else {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        SuperActivityToast.create(getActivity(), getContext().getResources().getString(R.string.verifique_campos_obrigatorios), Style.DURATION_SHORT)
                                .setTextColor(Color.WHITE)
                                .setColor(Color.RED)
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                    }
                });
            }
        } else {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    SuperActivityToast.create(getActivity(), getContext().getResources().getString(R.string.quantidade_invalida), Style.DURATION_SHORT)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
            });
        }
    }
}

package com.savare.activity.material.designer.fragment;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PlanoPagamentoRotinas;
import com.savare.funcoes.rotinas.TipoDocumentoRotinas;

/**
 * Created by Bruno Nogueira Silva on 12/04/2016.
 */
public class OrcamentoPrazoMDFragment extends Fragment{

    private View viewOrcamentoPrazo;
    private Spinner spinnerTipoDocumento,
            spinnerPlanoPagamento;
    private TextView textCodigoOrcamento,
            textTotalLiquido,
            textAtacadoVarejo;
    private ProgressBar progressBarStatus;
    private Toolbar toolbarRodape;
    private String atacadoVarejo,
            tipoOrcamentoPedido;
    private ItemUniversalAdapter adapterTipoDocumento,
            adapterPlanoPagamento;
    private int idTipoDocumento,
            idPlanoPagamento;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamentoPrazo = inflater.inflate(R.layout.fragment_orcamento_prazo_md, container, false);

        recuperarCamposTela();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if (parametro != null) {
            // Seta o codigo do orcamento
            textCodigoOrcamento.setText(parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO));
            // Checa se eh uma venda no atacado ou varejo
            if(parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO).equals("0")){
                textAtacadoVarejo.setText("Atacado");

            }else if (parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO).equals("1")){
                textAtacadoVarejo.setText("Varejo");
            }
            // Armazena a variavel de atacado varejo
            atacadoVarejo = parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO);

            // Instancia a classe de rotinas
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
            // Pega o total liquido do orcamento
            textTotalLiquido.setText(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));

        } else {
            // Dados da mensagem
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
            mensagem.put("mensagem", "Não conseguimos carregar os dados do orçamento.\n");

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
            funcoes.menssagem(mensagem);
        }

        toolbarRodape.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.menu_orcamento_prazo_fragment_md_salvar:
                        // Checa se eh um orcamento
                        if (tipoOrcamentoPedido.equals("O")){

                            salvarPrazo();

                        } else {
                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                            // Cria uma variavem para inserir as propriedades da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoPrazoFragment");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n" +
                                    getActivity().getResources().getString(R.string.nao_possivel_inserir_alterar_orcamento));
                            // Executa a mensagem passando por parametro as propriedades
                            funcoes.menssagem(mensagem);
                        }

                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        return viewOrcamentoPrazo;
    } // Fim onCreate

    @Override
    public void onResume() {
        super.onResume();

        CarregarPrazoOrcamento carregarPrazoOrcamento = new CarregarPrazoOrcamento(textCodigoOrcamento.getText().toString(), textAtacadoVarejo.getText().toString());
        carregarPrazoOrcamento.execute();
    }

    private void recuperarCamposTela() {
        spinnerTipoDocumento = (Spinner) viewOrcamentoPrazo.findViewById(R.id.fragment_orcamento_prazo_spinner_tipo_documento);
        spinnerPlanoPagamento = (Spinner) viewOrcamentoPrazo.findViewById(R.id.fragment_orcamento_prazo_spinner_plano_pagamento);
        textCodigoOrcamento = (TextView) viewOrcamentoPrazo.findViewById(R.id.fragment_orcamento_prazo_text_id_orcamento);
        textAtacadoVarejo = (TextView) viewOrcamentoPrazo.findViewById(R.id.fragment_orcamento_prazo_text_atacado_varejo);
        textTotalLiquido = (TextView) viewOrcamentoPrazo.findViewById(R.id.fragment_orcamento_prazo_text_total_liquido);
        progressBarStatus = (ProgressBar) viewOrcamentoPrazo.findViewById(R.id.fragmento_orcamento_prazo_md_progressBar_status);
        toolbarRodape = (Toolbar) viewOrcamentoPrazo.findViewById(R.id.fragment_orcamento_prazo_md_toolbar_rodape);
        toolbarRodape.inflateMenu(R.menu.orcamento_prazo_fragment_md);
    }

    private void salvarPrazo() {
        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
        // Salva o tipo de documento selecionado
        ContentValues valoresOrcamento = new ContentValues();
        valoresOrcamento.put("ID_CFATPDOC", adapterTipoDocumento.getListaTipoDocumento().get(spinnerTipoDocumento.getSelectedItemPosition()).getIdTipoDocumento());
        // Salva o tipo de documento no orcamento
        orcamentoRotinas.updateOrcamento(valoresOrcamento, textCodigoOrcamento.getText().toString());

        // Salva o plano de pagamento em todos os itens do orcamento
        ContentValues valoresItemOrcamento = new ContentValues();
        valoresItemOrcamento.put("ID_AEAPLPGT", adapterPlanoPagamento.getListaPlanoPagamento().get(spinnerPlanoPagamento.getSelectedItemPosition()).getIdPlanoPagamento());
        // Salva o plano de pagamento nos itens do orcamento
        orcamentoRotinas.updatePlanoPagamentoItemOrcamento(valoresItemOrcamento, textCodigoOrcamento.getText().toString());
    }


    public class CarregarPrazoOrcamento extends AsyncTask<Void, Void, Void> {

        private String codigoOrcamento = null, atacadoVarejo = "0";

        public CarregarPrazoOrcamento(String codigoOrcamento, String atacadoVarejo) {
            this.codigoOrcamento = codigoOrcamento;
            this.atacadoVarejo = atacadoVarejo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Intancia a classe de rotinas do tipo de documento
            TipoDocumentoRotinas tipoDocumentoRotinas = new TipoDocumentoRotinas(getActivity());
            // Intancia a classe do adapter
            adapterTipoDocumento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.TIPO_DOCUMENTO);
            // Preenche o adapter com uma lista
            adapterTipoDocumento.setListaTipoDocumento(tipoDocumentoRotinas.listaTipoDocumento(null));


            //Instancia a classe de rotina do plano de pagamento
            PlanoPagamentoRotinas planoPagamentoRotinas = new PlanoPagamentoRotinas(getActivity());
            // Intancia a classe do adapter
            adapterPlanoPagamento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.PLANO_PAGAMENTO);
            // Preenche o adapter com uma lista
            adapterPlanoPagamento.setListaPlanoPagamento(planoPagamentoRotinas.listaPlanoPagamento(null, "DESCRICAO", atacadoVarejo));

            // Instancia a classe para pegar os dados do orcamento
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

            // Pega o id do tipo do documento do orcamento
            idTipoDocumento = orcamentoRotinas.idTipoDocumentoOrcamento(codigoOrcamento);

            // Pega o id do plano de pagamento
            idPlanoPagamento = orcamentoRotinas.idPlanoPagamentoOrcamento(codigoOrcamento);

            // Pega o status do orcamento
            tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(codigoOrcamento);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (adapterTipoDocumento != null){
                // Preenche o spinner com um adapter personalizado
                spinnerTipoDocumento.setAdapter(adapterTipoDocumento);
            }

            if (adapterPlanoPagamento != null){
                // Preenche o spinner com um adapter personalizado
                spinnerPlanoPagamento.setAdapter(adapterPlanoPagamento);

                //Instancia a classe de rotina do plano de pagamento
                PlanoPagamentoRotinas planoPagamentoRotinas = new PlanoPagamentoRotinas(getActivity());

                // Posiciona o spinner no plano de pagamento
                spinnerPlanoPagamento.setSelection(planoPagamentoRotinas.posicaoPlanoPagamentoLista(adapterPlanoPagamento.getListaPlanoPagamento(), textCodigoOrcamento.getText().toString()));
            }

            // Checa se o orcamento tem algum tipo de documento salvo
            if(idTipoDocumento > 0){
                // Passa pela lista de tipo de documento
                for(int i = 0; i < adapterTipoDocumento.getListaTipoDocumento().size(); i++){
                    // Checa se o tipo de documento da lista eh o mesmo do orcamento
                    if(adapterTipoDocumento.getListaTipoDocumento().get(i).getIdTipoDocumento() == idTipoDocumento){
                        // Posiciona a lista no documento que ja tem no orcamento
                        spinnerTipoDocumento.setSelection(i);
                        i = adapterTipoDocumento.getListaTipoDocumento().size();
                    }
                }
            }


            // Checa se o orcamento tem algum tipo de documento salvo
            if(idPlanoPagamento > 0){
                // Passa pela lista de tipo de documento
                for(int i = 0; i < adapterPlanoPagamento.getListaPlanoPagamento().size(); i++){
                    // Checa se o tipo de documento da lista eh o mesmo do orcamento
                    if(adapterPlanoPagamento.getListaPlanoPagamento().get(i).getIdPlanoPagamento() == idPlanoPagamento){
                        // Posiciona a lista no documento que ja tem no orcamento
                        spinnerPlanoPagamento.setSelection(i);
                        i = adapterPlanoPagamento.getListaPlanoPagamento().size();
                    }
                }
            }

            progressBarStatus.setVisibility(View.GONE);
        }
    }
}

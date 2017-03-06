package com.savare.activity.material.designer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.material.designer.OrcamentoProdutoDetalhesTabFragmentMDActivity;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.CriticaOrcamentoBeans;
import com.savare.funcoes.rotinas.CriticaOrcamentoRotina;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

import java.util.List;

/**
 * Created by Bruno on 01/03/2017.
 */

public class OrcamentoCriticaMDFragment extends Fragment {

    private View viewOrcamentoCritica;
    private ListView listViewListaCritica;
    private TextView textCodigoOrcamento, textMensagem;
    private TextView textTotal;
    private TextView textAtacadoVarejo;
    private ProgressBar progressBarStatus;
    private Toolbar toolbarRodape;
    private String idOrcamento = null;
    private ItemUniversalAdapter adapterCriticaOrcamento;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewOrcamentoCritica = inflater.inflate(R.layout.fragment_orcamento_critica_md, container, false);

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        recuperarCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        final Bundle parametro = getArguments();

        if(parametro != null){
            idOrcamento = parametro.getString("" + OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO);
            if ((idOrcamento != null) && (!idOrcamento.isEmpty())){
                textCodigoOrcamento.setText(idOrcamento);
            }
        }

        listViewListaCritica.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CriticaOrcamentoBeans criticaOrcamentoBeans = (CriticaOrcamentoBeans) parent.getItemAtPosition(position);

                // Abre a tela de detalhes do produto
                //Intent intent = new Intent(getActivity(), OrcamentoProdutoDetalhesTabFragmentMDActivity.class);

                //intent.putExtra(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU, itemOrcamento.getProduto().getIdProduto());

                //startActivity(intent);
            }
        });

        toolbarRodape.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return true;
            }
        });
        return viewOrcamentoCritica;
    } // Fim onCreate


    @Override
    public void onResume() {
        super.onResume();

        CarregarDadosCritica carregarDadosCritica = new CarregarDadosCritica(getContext());
        carregarDadosCritica.execute();
    }

    private void recuperarCampos(){
        listViewListaCritica = (ListView) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_list_critica);
        textCodigoOrcamento = (TextView) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_text_codigo_orcamento);
        textMensagem = (TextView) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_text_mensagem_geral);
        textTotal = (TextView) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_text_total);
        textAtacadoVarejo = (TextView) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_text_atacado_varejo);
        progressBarStatus = (ProgressBar) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_progressBar_status);
        toolbarRodape = (Toolbar) viewOrcamentoCritica.findViewById(R.id.fragment_orcamento_critica_md_toolbar_rodape);
        toolbarRodape.inflateMenu(R.menu.orcamento_critica_md);
    }


    private class CarregarDadosCritica extends AsyncTask<Void, Void, Void> {
        private Context context;
        private String totalOrcamento = null;
        private List<CriticaOrcamentoBeans> listaCriticaOrcamento;

        public CarregarDadosCritica(Context context) {
            this.context = context;
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

            if ((idOrcamento != null) && (!idOrcamento.isEmpty())){
                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
                // Calcula o total dos pedidos listados
                totalOrcamento = orcamentoRotinas.totalOrcamentoLiquido(idOrcamento);

                CriticaOrcamentoRotina criticaOrcamentoRotina = new CriticaOrcamentoRotina(context);

                listaCriticaOrcamento = criticaOrcamentoRotina.listaCriticaOrcamento(idOrcamento, progressBarStatus);

                // Verifica se existe algum dado na lista
                if ((listaCriticaOrcamento != null) && (listaCriticaOrcamento.size() > 0)) {

                    adapterCriticaOrcamento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.CRITICA_ORCAMENTO);

                    adapterCriticaOrcamento.setListaCriticaOrcamento(listaCriticaOrcamento);
                } else {
                    if (adapterCriticaOrcamento != null) {
                        // Passa por todos da lista do adapter
                        for (int i = 0; i < adapterCriticaOrcamento.getCount(); i++) {
                            // Remove todos do adapter se existir
                            adapterCriticaOrcamento.remove(i);
                            //adapterCriticaOrcamento.notifyDataSetChanged();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if ((listaCriticaOrcamento != null) && (listaCriticaOrcamento.size() > 0)){
                listViewListaCritica.setAdapter(adapterCriticaOrcamento);
            } else {
                listViewListaCritica.setVisibility(View.GONE);
                textMensagem.setVisibility(View.VISIBLE);
            }
            if (totalOrcamento != null){
                textTotal.setText("Total: " + totalOrcamento);
            }
            progressBarStatus.setVisibility(View.GONE);
        }
    }
}

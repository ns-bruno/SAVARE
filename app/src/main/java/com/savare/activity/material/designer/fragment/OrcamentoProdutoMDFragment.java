package com.savare.activity.material.designer.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

import java.util.List;

/**
 * Created by Faturamento on 12/04/2016.
 */
public class OrcamentoProdutoMDFragment extends Fragment {

    private View viewOrcamento;
    private ListView listViewListaProdutoOrcamento;
    private TextView textCodigoOrcamento, textTotal, textAtacadoVarejo;
    private ProgressBar progressBarStatus;
    private String
                    idPessoa = null,
                    idOrcamento = null,
                    razaoSocial = null,
                    tipoOrcamentoPedido;
    private ItemUniversalAdapter adapterItemOrcamento;


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

        return viewOrcamento;
    } // Fim onCreateView


    @Override
    public void onResume() {
        super.onResume();



    } // Fim do onResume

    private void recuperarCampos(){
        listViewListaProdutoOrcamento = (ListView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_list_produto_orcamento);
        textCodigoOrcamento = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_text_codigo_orcamento);
        textTotal = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_text_total);
        textAtacadoVarejo = (TextView) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_text_atacado_varejo);
        progressBarStatus = (ProgressBar) viewOrcamento.findViewById(R.id.fragment_orcamento_produto_md_progressBar_status);
    }




    public class CarregarDadosOrcamentoProduto extends AsyncTask<Void, Void, Void> {

        private Context context;
        private List<ItemOrcamentoBeans> listaItemOrcamento;
        private String codigoOrcamento = null, totalOrcamento = null;


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
                listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida(null, codigoOrcamento, progressBarStatus);

                // Verifica se existe algum dado na lista
                if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)) {
                    // Preenche o adapter com a lista de produtos do orcamento
                    adapterItemOrcamento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.ITEM_ORCAMENTO, listaItemOrcamento);

                    // Calcula o total dos pedidos listados
                    totalOrcamento = orcamentoRotinas.totalOrcamentoLiquido(codigoOrcamento);

                } else {
                    // Passa por todos da lista do adapter
                    for (int i = 0; i < adapterItemOrcamento.getCount(); i++) {
                        // Remove todos do adapter se existir
                        adapterItemOrcamento.remove(i);
                        //adapterItemOrcamento.notifyDataSetChanged();
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
        }
    } // CarregarDadosOrcamentoProduto
}

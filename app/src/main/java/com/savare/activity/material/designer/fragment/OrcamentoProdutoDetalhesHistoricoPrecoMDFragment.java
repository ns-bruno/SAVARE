package com.savare.activity.material.designer.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.material.designer.OrcamentoProdutoDetalhesTabFragmentMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 23/05/2016.
 */
public class OrcamentoProdutoDetalhesHistoricoPrecoMDFragment extends Fragment {

    private View viewHistorico;
    private ListView listViewHistoricoPreco;
    private ProgressBar progressStatus;
    private TextView textMensagem;
    private ItemUniversalAdapter listaHistoricoPrecoAdapter;
    private int idProduto = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewHistorico = inflater.inflate(R.layout.fragment_orcamento_produto_detalhes_historico_preco_md, container, false);

        recuperarCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();
        // Checa se realmente foi passado dados por parametro
        if (parametro != null){
            idProduto = parametro.getInt(OrcamentoProdutoDetalhesTabFragmentMDActivity.KEY_ID_AEAPRODU);
        }

        return viewHistorico;
    }

    @Override
    public void onResume() {
        super.onResume();

        CarregarHistoricoProduto carregarHistoricoProduto = new CarregarHistoricoProduto();
        carregarHistoricoProduto.execute();
    }

    private void recuperarCampos(){
        listViewHistoricoPreco = (ListView) viewHistorico.findViewById(R.id.fragment_orcamento_produto_detalhes_historico_preco_md_list_hitorico_preco);
        progressStatus = (ProgressBar) viewHistorico.findViewById(R.id.fragment_orcamento_produto_detalhes_historico_preco_md_progress_status);
        textMensagem = (TextView) viewHistorico.findViewById(R.id.fragment_orcamento_produto_detalhes_historico_preco_md_text_mensagem);
    }


    public class CarregarHistoricoProduto extends AsyncTask<Void, Void, Void> {

        private List<ItemOrcamentoBeans> listaItemOrcamento;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressStatus.setVisibility(View.VISIBLE);
            textMensagem.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Checa se foi passado algum id de produto
            if (idProduto > 0) {
                OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
                // Pega todos os produtos do orcamento
                listaItemOrcamento = orcamentoRotinas.listaItemOrcamentoResumida("AEAPRODU.ID_AEAPRODU = " + idProduto, null, "AEAITORC.DT_CAD DESC", progressStatus);

                // Verifica se existe algum dado na lista
                if ((listaItemOrcamento != null) && (listaItemOrcamento.size() > 0)) {
                    // Preenche o adapter com a lista de produtos do orcamento
                    listaHistoricoPrecoAdapter = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.HISTORICO_PRECO_ITEM_ORCAMENTO, listaItemOrcamento);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if ( (listaHistoricoPrecoAdapter != null) && (listaHistoricoPrecoAdapter.getListaItemOrcamento() != null) ){
                listViewHistoricoPreco.setAdapter(listaHistoricoPrecoAdapter);
            } else {
                textMensagem.setVisibility(View.VISIBLE);
            }

            progressStatus.setVisibility(View.GONE);
        }
    }
}

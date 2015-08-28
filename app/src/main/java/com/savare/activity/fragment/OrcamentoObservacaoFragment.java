package com.savare.activity.fragment;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

/**
 * Created by Bruno Nogueira Silva on 10/08/2015.
 */
public class OrcamentoObservacaoFragment extends Fragment {

    private TextView textCodigoOrcamento, textTotal, textAtacadoVarejo;
    private EditText editObservacao;
    private View viewOrcamentoObs;
    private String atacadoVarejo, tipoOrcamentoPedido;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamentoObs = inflater.inflate(R.layout.fragment_orcamento_observacao, container, false);

        recuperarCamposTela();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if (parametro != null) {
            // Seta o codigo do orcamento
            textCodigoOrcamento.setText(parametro.getString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO));
            // Checa se eh uma venda no atacado ou varejo
            if(parametro.getString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO).equals("0")){
                textAtacadoVarejo.setText("Atacado");

            }else if (parametro.getString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO).equals("1")){
                textAtacadoVarejo.setText("Varejo");
            }
            // Armazena a variavel de atacado varejo
            atacadoVarejo = parametro.getString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO);

            // Instancia a classe de rotinas
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
            // Pega o total liquido do orcamento
            textTotal.setText(orcamentoRotinas.totalOrcamentoLiquido(textCodigoOrcamento.getText().toString()));

        } else {
            // Dados da mensagem
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
            mensagem.put("mensagem", "Não conseguimos carregar os dados do orçamento.\n");

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
            funcoes.menssagem(mensagem);
        }

        // Ativa a opcao de menus para este fragment
        setHasOptionsMenu(true);

        return  viewOrcamentoObs;
    } // Fim onCreateView


    @Override
    public void onResume() {
        super.onResume();

        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
        // Pega a obs do banco de dados e preenche o campo de obs
        editObservacao.setText(orcamentoRotinas.selectObservacaoOrcamento(textCodigoOrcamento.getText().toString()));
        // Checa se eh um orcamento ou pedido
        this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(textCodigoOrcamento.getText().toString());

    }// Fim onResume

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu);
        inflater.inflate(R.menu.orcamento_observacao_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_orcamento_observacao_fragment_salvar:

                // Checa se eh um orcamento
                if (tipoOrcamentoPedido.equals("O")){
                    // Preenche os dados para salvar no banco de dados
                    ContentValues dadosObservacao = new ContentValues();
                    dadosObservacao.put("OBS", editObservacao.getText().toString());

                    OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());
                    // Inseri a obs no banco de dados
                    orcamentoRotinas.updateOrcamento(dadosObservacao, textCodigoOrcamento.getText().toString());

                } else {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                    // Cria uma variavem para inserir as propriedades da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 2);
                    mensagem.put("tela", "OrcamentoObservacaoFragment");
                    mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n" +
                                             getActivity().getResources().getString(R.string.nao_pode_ser_inserido_atualizado_alguma_observacao));
                    // Executa a mensagem passando por parametro as propriedades
                    funcoes.menssagem(mensagem);
                }

                break;

            default:
                break;
        }

        return true;
    }

    private void recuperarCamposTela() {
        textCodigoOrcamento = (TextView) viewOrcamentoObs.findViewById(R.id.fragment_orcamento_observacao_text_id_orcamento);
        textAtacadoVarejo = (TextView) viewOrcamentoObs.findViewById(R.id.fragment_orcamento_observacao_text_atacado_varejo);
        textTotal = (TextView) viewOrcamentoObs.findViewById(R.id.fragment_orcamento_observacao_text_total_liquido);
        editObservacao = (EditText) viewOrcamentoObs.findViewById(R.id.fragment_orcamento_observacao_edit_observacao);
    }
}

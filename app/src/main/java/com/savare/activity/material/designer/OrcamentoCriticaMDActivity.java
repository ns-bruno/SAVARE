package com.savare.activity.material.designer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import com.savare.R;
import com.savare.beans.CriticaOrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.CriticaOrcamentoRotina;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

import java.util.List;

/**
 * Created by Bruno on 07/03/2017.
 */

public class OrcamentoCriticaMDActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;
    private TextView textViewNumeroPedido, textViewRazao, textViewStatus, textViewData, textViewMensagem;
    private String idCritica = null;
    public static final String
            KEY_ID_AEACRORC = "ID_AEACRORC",
            KEY_RAZAO_SOCIAL = "RAZAO_SOCIAL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_orcamento_critica_md);

        recuperaCampos();

        setSupportActionBar(toolbarCabecalho);
        // Ativa o botao de votlar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {
            idCritica = intentParametro.getString(KEY_ID_AEACRORC);
            textViewRazao.setText(intentParametro.getString(KEY_RAZAO_SOCIAL));
        }

    } // Fim onCreate

    @Override
    protected void onResume() {
        super.onResume();

        CriticaOrcamentoRotina criticaOrcamentoRotina = new CriticaOrcamentoRotina(getApplicationContext());

        CriticaOrcamentoBeans critica = criticaOrcamentoRotina.criticaOrcamento(idCritica, null);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

        if (critica != null){

            /*OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getApplicationContext());

            List<OrcamentoBeans> listaOrcamento = orcamentoRotinas.listaOrcamentoPedido(null, "ID_AEAORCAM = " + critica.getIdOrcamento(), OrcamentoRotinas.ORDEM_DECRESCENTE);

            if ((listaOrcamento != null) && (listaOrcamento.size() > 0)){
                textViewRazao.setText(listaOrcamento.get(0).getNomeRazao());

            } else {
                textViewRazao.setText(getResources().getString(R.string.sem_razao_social));
            }*/
            textViewNumeroPedido.setText("Nº " + critica.getIdOrcamento());

            if (critica.getStatus().equalsIgnoreCase(OrcamentoRotinas.PEDIDO_ENVIADO)){
                textViewStatus.setText("Enviado Sucesso");
                textViewStatus.setTextColor(getResources().getColor(R.color.verde_escuro));

            } else if (critica.getStatus().equalsIgnoreCase(OrcamentoRotinas.PEDIDO_ERRO_ENVIAR)){
                textViewStatus.setText("Erro ao enviar");
                textViewStatus.setTextColor(getResources().getColor(R.color.vermelho_escuro));

            } else {
                textViewStatus.setText("Status Desconhecido");
                textViewStatus.setTextColor(getResources().getColor(R.color.laranja_escuro));
            }
            textViewData.setText(funcoes.formataDataHora(critica.getDataCadastro()));

            textViewMensagem.setText("Crítica Nº " + critica.getIdCritica() + ". \n" + critica.getRetornoWebservice());
        }
    } // Fim onResume

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
        textViewNumeroPedido = (TextView) findViewById(R.id.activity_orcamento_critica_md_text_numero_pedido);
        textViewRazao = (TextView) findViewById(R.id.activity_orcamento_critica_md_text_nome_razao);
        textViewStatus = (TextView) findViewById(R.id.activity_orcamento_critica_md_text_status);
        textViewData = (TextView) findViewById(R.id.activity_orcamento_critica_md_text_data);
        textViewMensagem = (TextView) findViewById(R.id.activity_orcamento_critica_md_text_mensagem);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_orcamento_critica_md_toolbar_inicio);
        // Adiciona uma titulo para toolbar
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.critica_orcamento));
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
    }
}

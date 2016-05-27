package com.savare.activity.material.designer;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.adapter.OrcamentoProdutoDetalhesTabFragmentoMDAdapter;

/**
 * Created by Bruno Nogueira Silva on 21/05/2016.
 */
public class OrcamentoProdutoDetalhesTabFragmentMDActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;
    private int idProduto, idOrcamento, idPessoa, idItemOrcamento, posicao;
    private String telaChamada, razaoSocial, atacadoVarejo;
    public static final String KEY_ID_AEAPRODU = "ID_AEAPRODU",
                                KEY_ID_AEAORCAM = "ID_AEAORCAM",
                                KEY_ID_CFACLIFO = "ID_CFACLIFO",
                                KEY_ID_AEAITORC = "ID_AEAITORC",
                                KEY_RAZAO_SOCIAL = "RAZAO_SOCIAL",
                                KEY_TELA_CHAMADA = "TELA_CHAMADA",
                                KEY_ATACADO_VAREJO = "ATAC_VERE",
                                KEY_POSICAO = "POSICAO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_orcamento_produto_detalhes_tab_md);

        recuperarCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {
            idProduto = intentParametro.getInt(KEY_ID_AEAPRODU);
            idOrcamento = intentParametro.getInt(KEY_ID_AEAORCAM);
            idPessoa = intentParametro.getInt(KEY_ID_CFACLIFO);
            posicao = intentParametro.getInt(KEY_POSICAO);
            telaChamada = intentParametro.getString(KEY_TELA_CHAMADA);
            razaoSocial = intentParametro.getString(KEY_RAZAO_SOCIAL);
            atacadoVarejo = intentParametro.getString(KEY_ATACADO_VAREJO);
            idItemOrcamento = intentParametro.containsKey(KEY_ID_AEAITORC) ? intentParametro.getInt(KEY_ID_AEAITORC) : -1;
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_orcamento_produto_detalhes_tab_md_pager);

        Bundle argumentos = new Bundle();
        argumentos.putInt(KEY_ID_AEAPRODU, idProduto);
        argumentos.putInt(KEY_ID_AEAORCAM, idOrcamento);
        argumentos.putInt(KEY_ID_CFACLIFO, idPessoa);
        argumentos.putInt(KEY_POSICAO, posicao);
        argumentos.putInt(KEY_ID_AEAITORC, (idItemOrcamento > 0) ? idItemOrcamento : -1);
        argumentos.putString(KEY_TELA_CHAMADA, telaChamada);
        argumentos.putString(KEY_RAZAO_SOCIAL, razaoSocial);
        argumentos.putString(KEY_ATACADO_VAREJO, atacadoVarejo);

        OrcamentoProdutoDetalhesTabFragmentoMDAdapter orcamentoProdutoDetalhesTabFragmentoMDAdapter = new OrcamentoProdutoDetalhesTabFragmentoMDAdapter(getSupportFragmentManager(), getApplicationContext(), argumentos);

        // Seta o adapter dentro da viewPager
        viewPager.setAdapter(orcamentoProdutoDetalhesTabFragmentoMDAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.fragment_orcamento_produto_detalhes_tab_md_tab_layout);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void recuperarCampos(){
        toolbarCabecalho = (Toolbar) findViewById(R.id.fragment_orcamento_produto_detalhes_tab_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.adicionar_produto));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

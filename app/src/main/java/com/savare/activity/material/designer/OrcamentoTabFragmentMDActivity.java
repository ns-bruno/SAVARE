package com.savare.activity.material.designer;

import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.adapter.OrcamentoTabFragmentMDAdapter;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ParcelaRotinas;

/**
 * Created by Bruno Nogueira Silva on 11/04/2016.
 */
public class OrcamentoTabFragmentMDActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;
    private String  idOrcamento,
                    atacadoVarejo = "0",
                    idPessoa,
                    razaoSocial,
                    tipoOrcamentoPedido;
    private boolean abertoTitulosPriveiraVez = false;
    public static final String  KEY_ID_ORCAMENTO = "ID_ORCAMENTO",
                                KEY_ATACADO_VAREJO = "AEACADO_VAREJO",
                                KEY_NOME_RAZAO = "NOME_RAZAO",
                                KEY_ID_PESSOA = "ID_PESSOA";
    public static final int SOLICITA_CLIENTE = 2,
                            RETORNA_CLIENTE = 100,
                            ERRO_RETORNA_CLIENTE = 101;
    public static final String  KEY_TELA_ORCAMENTO_FRAGMENTO = "ORCAMENTO_FRAGMENT",
                                KEY_TELA_CHAMADA = "TELA_CHAMADA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_orcamento_tab_md);

        recuperarCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {
            // Seta o campo codigo consumo total com o que foi passado por parametro
            idOrcamento = intentParametro.getString(KEY_ID_ORCAMENTO);
            //textTotal.setText("Total");
            idPessoa = intentParametro.getString(KEY_ID_PESSOA);
            razaoSocial = intentParametro.getString(KEY_NOME_RAZAO);
            atacadoVarejo = intentParametro.getString(KEY_ATACADO_VAREJO);

            // Seta o titulo da action bar com a razao do cliente
            toolbarCabecalho.setTitle(intentParametro.getString(KEY_ID_PESSOA) + " - " + intentParametro.getString(KEY_NOME_RAZAO));

        } else {
            // Seta o titulo da action bar com a razao do cliente
            toolbarCabecalho.setTitle("Selecione um Cliente");

        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_orcamento_tab_md_pager);

        // Pega os parametros para passar para os fragmets
        Bundle parametros = new Bundle();
        parametros.putString(KEY_ID_ORCAMENTO, idOrcamento);
        parametros.putString(KEY_ATACADO_VAREJO, atacadoVarejo);
        parametros.putString(KEY_ID_PESSOA, idPessoa);
        parametros.putString(KEY_NOME_RAZAO, razaoSocial);

        OrcamentoTabFragmentMDAdapter orcamentoTabMDAdapter = new OrcamentoTabFragmentMDAdapter(getApplicationContext(), getSupportFragmentManager(), parametros);

        // Seta o adapter dentro da viewPager
        viewPager.setAdapter(orcamentoTabMDAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.fragment_orcamento_tab_md_tab_layout);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);

    } // Fim onCreate


    @Override
    protected void onResume() {
        super.onResume();

        // Instancia a classe para manipulacao do orcamento
        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(OrcamentoTabFragmentMDActivity.this);
        // Pega o status do orcamento
        this.tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(idOrcamento);

        ParcelaRotinas parcelaRotinas = new ParcelaRotinas(getApplicationContext());

        if ((abertoTitulosPriveiraVez == false) && (parcelaRotinas.listaTitulos(idPessoa, ParcelaRotinas.TITULOS_EM_ABERTO_VENCIDOS, ParcelaRotinas.RECEBER, null, null).size() > 0)) {

            abertoTitulosPriveiraVez = true;

            /*ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 2);
            mensagem.put("tela", "OrcamentoProdutoMDFragment");
            mensagem.put("mensagem", getResources().getString(R.string.existe_titulos_vencidos));

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());
            funcoes.menssagem(mensagem); */

            // Cria uma intent para abrir uma nova activity
            Intent intentTitulos = new Intent(getApplicationContext(), ListaTitulosMDActivity.class);
            intentTitulos.putExtra("ID_CFACLIFO", idPessoa);
            startActivity(intentTitulos);
        }

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
        toolbarCabecalho = (Toolbar) findViewById(R.id.fragment_orcamento_tab_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.app_name));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

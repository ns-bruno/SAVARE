package com.savare.activity.material.designer.fragment;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.adapter.ProdutoTabMDAdapter;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.provider.SearchableProvider;

/**
 * Created by Bruno Nogueira Silva on 21/12/2015.
 */
public class ProdutoListaTabMD extends AppCompatActivity {

    private Toolbar toolbarInicio;
    private TextView textCodigoOrcamento, textCodigoPessoa, textNomeRazao, textAtacadoVarejo, textProcessoPesquisa;
    private ContentValues dadosParametros;
    public static final String KEY_ATACADO_VAREJO = "ATACADO_VAREJO",
                               KEY_ID_ORCAMENTO = "ID_AEAORCAM",
                               KEY_NOME_RAZAO = "NOME_RAZAO",
                               KEY_ID_CLIENTE = "ID_CFACLIFO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_produto_lista_tab_md);

        // Recupera o campo para manipular
        toolbarInicio = (Toolbar) findViewById(R.id.fragment_produto_lista_tab_md_toolbar_inicio);
        // Adiciona uma titulo para toolbar
        toolbarInicio.setTitle(this.getResources().getString(R.string.produtos));
        toolbarInicio.setTitleTextColor(getResources().getColor(R.color.branco));
        //toolbarInicio.setLogo(R.drawable.ic_launcher);
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarInicio);
        // Adiciona o botao voltar no toolbar
        //getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        recuperaCamposTela();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {
            // Seta o campo codigo consumo total com o que foi passado por parametro
            textCodigoOrcamento.setText(intentParametro.getString(KEY_ID_ORCAMENTO));
            textNomeRazao.setText(intentParametro.getString(KEY_NOME_RAZAO));
            textCodigoPessoa.setText(intentParametro.getString(KEY_ID_CLIENTE));
            textAtacadoVarejo.setText(intentParametro.getString(KEY_ATACADO_VAREJO));
            // Instancia a vareavel
            dadosParametros = new ContentValues();
            // Adiciona os dados para ser passado por parametro
            dadosParametros.put(KEY_ID_ORCAMENTO, intentParametro.getString(KEY_ID_ORCAMENTO));
            dadosParametros.put(KEY_ID_CLIENTE, intentParametro.getString(KEY_ID_CLIENTE));
            dadosParametros.put(KEY_ATACADO_VAREJO, intentParametro.getString(KEY_ATACADO_VAREJO));

        } else {
            // Dados da mensagem
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "ProdutoListaActivity");
            mensagem.put("mensagem", "Não foi possível criar a lista de produtos a partir do orçamento\n"
                    + "Favor, voltar e selecione novamente um cliente para criar um novo orçamento");

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());
            funcoes.menssagem(mensagem);
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_produto_lista_tab_md_pager);

        final ProdutoTabMDAdapter produtoTabMDAdapter = new ProdutoTabMDAdapter(getSupportFragmentManager(), getApplicationContext(), dadosParametros);

        // Seta o adapter dentro da viewPager
        viewPager.setAdapter(produtoTabMDAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.fragment_produto_lista_tab_md_tab_layout);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);
        tabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    } // Fim onCreate

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                break;

            // Limpar o historico de pesquisa
            case R.id.menu_produto_lista_tab_md_search_action_delete:
                SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(this, SearchableProvider.AUTHORITY, SearchableProvider.MODE);
                // Limpa o historico de palavras pesquisadas
                searchRecentSuggestions.clearHistory();

                Toast.makeText(this, "Cookies removidos", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void recuperaCamposTela(){
        textCodigoOrcamento = (TextView) findViewById(R.id.fragment_produto_lista_tab_md_text_codigo_orcamento);
        textNomeRazao = (TextView) findViewById(R.id.fragment_produto_lista_tab_md_text_nome_razao);
        textCodigoPessoa = (TextView) findViewById(R.id.fragment_produto_lista_tab_md_text_codigo_pessoa);
        textAtacadoVarejo = (TextView) findViewById(R.id.fragment_produto_lista_tab_md_text_atacado_varejo);
    }

    public void pesquisarBanco( String query){
        if((query != null ) && (query.length() > 0)){

            //toolbarInicio.setTitle(query);

            SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getApplicationContext(), SearchableProvider.AUTHORITY, SearchableProvider.MODE);
            searchRecentSuggestions.saveRecentQuery(query, null);
        }
    }
}

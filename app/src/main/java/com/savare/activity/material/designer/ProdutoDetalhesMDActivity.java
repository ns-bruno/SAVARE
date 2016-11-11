package com.savare.activity.material.designer;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.savare.R;
import com.savare.activity.material.designer.fragment.ProdutoDetalhesMDFragment;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.FotosBeans;
import com.savare.beans.ProdutoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.FotoRotinas;
import com.savare.funcoes.rotinas.ProdutoRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 13/01/2016.
 */
public class ProdutoDetalhesMDActivity extends AppCompatActivity{

    private Toolbar toolbarCabecalho;
    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialListView mListView;
    private String idProduto;
    public static final String KEY_ID_PRODUTO = "ID_AEAPRODU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_detalhes_md);

        // Recupera o campo para manipular
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_produto_detalhes_md_anim_toolbar_cabecalho);

        setSupportActionBar(toolbarCabecalho);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recuperaCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle intentParametro = getIntent().getExtras();
        // Checa se foi passado algum parametro
        if (intentParametro != null) {
            idProduto = intentParametro.getString(KEY_ID_PRODUTO);
        }
        // Instancia a classe de rotinas de fotos
        FotoRotinas fotoRotinas = new FotoRotinas(getApplicationContext());

        List<FotosBeans> listaFotoProduto = new ArrayList<FotosBeans>();

        // Pega a lista de fotos de um determinado produto
        listaFotoProduto = fotoRotinas.listaFotoProduto(idProduto);

        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_produto_detalhes_md_viewpager_pagina_imagem);

        ProdutoDetalheTabMDAdapter produtoDetalheTabMDAdapter = null;

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ProdutoDetalhesMDActivity.this);

        // Checa se retonou algum alguma foto e se esta configurado para visualizar as imagens
        if ((listaFotoProduto != null) && (listaFotoProduto.size() > 0) && (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase("S"))) {

            produtoDetalheTabMDAdapter = new ProdutoDetalheTabMDAdapter(getSupportFragmentManager(), listaFotoProduto.size(), listaFotoProduto);
        }

        // Seta o adapter dentro da viewPager
        viewPager.setAdapter(produtoDetalheTabMDAdapter);

        // Recupera os campos tabs
        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.activity_produto_detalhes_md_viewpagertab_imagem);
        // Seta as paginas nas tabs
        tabLayout.setViewPager(viewPager);
    } // Fim onCreate

    @Override
        protected void onResume() {
        super.onResume();
        ProdutoRotinas produtoRotinas = new ProdutoRotinas(ProdutoDetalhesMDActivity.this);

        // Pega os dados do produto
        ProdutoBeans produto = produtoRotinas.detalhesProduto(idProduto);
        // Atualiza o titulo do collapsing
        collapsingToolbar.setTitle(produto.getDescricaoProduto());
        // Inseri um novo estilo de texto para quando o collapsing estiver expandido
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);

        List<DescricaoDublaBeans> listaDetalhes = new ArrayList<DescricaoDublaBeans>();

        listaDetalhes = produtoRotinas.listaDetalhesProduto(idProduto);

        // Checa se tem alguma coisa na lista
        if ((listaDetalhes != null) && (listaDetalhes.size() > 0)){
            // Limpa a lista
            mListView.getAdapter().clearAll();
            List<Card> listaCards = new ArrayList<>();
            for (DescricaoDublaBeans descricao : listaDetalhes) {

                Card cardDetalhe = new Card.Builder(getApplicationContext())
                        .withProvider(new CardProvider())
                        .setLayout(R.layout.material_basic_buttons_card)
                        .setTitle(descricao.getTextoSecundario())
                        .setDescription( (descricao.getTextoPrincipal() != null) ? descricao.getTextoPrincipal() : "")
                        /*.addAction(R.id.right_text_button, new TextViewAction(getApplicationContext())
                                .setText("Action")
                                .setListener(new OnActionClickListener() {
                                    @Override
                                    public void onActionClicked(View view, Card card) {
                                        String s = card.toString();
                                    }

                                }))*/
                        .endConfig()
                        .build();
                // Adiciona o card view em uma lista
                listaCards.add(cardDetalhe);
                //mListView.getAdapter().add(cardDetalhe);
            } // Fim do forarch
            mListView.getAdapter().addAll(listaCards);
        }
        // Posiciona a lista de cards no primeiro card
        //mListView.smoothScrollToPosition(0);
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
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.activity_produto_detalhes_md_collapsing_toolbar);
        // Recupera o campo de lista de card da activity(view)
        mListView = (MaterialListView) findViewById(R.id.activity_produto_detalhes_md_material_listview);
    }



    public class ProdutoDetalheTabMDAdapter extends FragmentStatePagerAdapter {

        private int qtdPagina;
        private List<FotosBeans> listaFotoProduto;

        public ProdutoDetalheTabMDAdapter(FragmentManager fm, int qtdPagina, List<FotosBeans> listaFotoProduto) {
            super(fm);
            this.qtdPagina = qtdPagina;
            this.listaFotoProduto = listaFotoProduto;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            fragment = new ProdutoDetalhesMDFragment();
            // Cria uma vareavel para salvar os paramentros
            Bundle argumentos = new Bundle();
            argumentos.putString(ProdutoDetalhesMDFragment.KEY_ID_CFAFOTO, String.valueOf(listaFotoProduto.get(position).getIdFotos()));

            // Coloca o argumento dentro do fragment
            fragment.setArguments(argumentos);

            return fragment;
        }

        @Override
        public int getCount() {
            return qtdPagina;
        }
    }
}

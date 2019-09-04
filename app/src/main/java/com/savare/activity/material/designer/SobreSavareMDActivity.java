package com.savare.activity.material.designer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;

import java.util.ArrayList;
import java.util.List;

public class SobreSavareMDActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;
    private ListView listViewDados;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sobre_savare_md);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_sobre_savare_md_toolbar_inicio);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.sobre_savare));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listViewDados = (ListView) findViewById(R.id.activity_sobre_savare_md_listview_dados);

        List<DescricaoDublaBeans> listaDetalhes = new ArrayList<DescricaoDublaBeans>();

        DescricaoDublaBeans descricao = new DescricaoDublaBeans();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

        descricao.setTextoPrincipal(funcoes.getNomeVersaoAplicacao());
        descricao.setTextoSecundario(getResources().getString(R.string.versao_aplicacao));
        listaDetalhes.add(descricao);

        descricao = new DescricaoDublaBeans();
        descricao.setTextoPrincipal(funcoes.getValorXml(funcoes.TAG_UUID_DISPOSITIVO));
        descricao.setTextoSecundario(getResources().getString(R.string.dispositivo));
        listaDetalhes.add(descricao);

        // Seta o adapter com a nova lista
        ItemUniversalAdapter adapterListaDetalhes = new ItemUniversalAdapter(getApplicationContext(), ItemUniversalAdapter.DETALHES_PRODUTOS);
        // Preenche o adapter com a lista de orcamento
        adapterListaDetalhes.setListaDetalhesProduto(listaDetalhes);

        listViewDados.setAdapter(adapterListaDetalhes);
    }

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
}

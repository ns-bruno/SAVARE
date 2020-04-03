package com.savare.activity.material.designer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.funcoes.rotinas.UltimaAtualizacaoRotinas;

import java.util.List;

public class UltimaAtualizacaoActivity extends AppCompatActivity {

    private Toolbar toolbarCabecalho;
    private ListView listViewDados;
    private ItemUniversalAdapter adapterUltimaAtualizacao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ultima_atualizacao_md);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_ultima_atualizacao_md_toolbar_inicio);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.sobre_savare));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listViewDados = (ListView) findViewById(R.id.activity_ultima_atualizacao_md_listView_ultima_atualizacao);

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(UltimaAtualizacaoActivity.this);
        // Pega todos os dados da tabela
        List<UltimaAtualizacaoBeans> listUltimaAtualizacao = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas(null);

        if ( (listUltimaAtualizacao != null) && (listUltimaAtualizacao.size() > 0) ){
            adapterUltimaAtualizacao = new ItemUniversalAdapter(UltimaAtualizacaoActivity.this, ItemUniversalAdapter.ULTIMA_ATUALIZACAO);
            adapterUltimaAtualizacao.setListaUltimaAtualizacao(listUltimaAtualizacao);
            listViewDados.setAdapter(adapterUltimaAtualizacao);
        }
    }
}

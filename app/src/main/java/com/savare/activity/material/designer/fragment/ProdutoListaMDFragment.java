package com.savare.activity.material.designer.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.savare.R;

/**
 * Created by Bruno Nogueira Silva on 21/12/2015.
 */
public class ProdutoListaMDFragment extends Fragment {

    public static final int TELA_LISTA_PRODUTO = 1,
                            TELA_MAIS_VENDIDOS_CIDADE = 2;
    public static final String KEY_TIPO_TELA = "KEY_TIPO_TELA";
    private int tipoTela = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if(parametro != null){
            tipoTela = parametro.getInt(KEY_TIPO_TELA);
        }

        //hendleSearch(getArguments());

        return inflater.inflate(R.layout.fragment_produto_lista_universal_md, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.produto_lista_tab_md_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem i = item;
        return super.onOptionsItemSelected(item);
    }
}

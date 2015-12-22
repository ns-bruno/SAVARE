package com.savare.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.savare.R;
import com.savare.activity.material.designer.fragment.ProdutoListaMDFragment;

/**
 * Created by Bruno Nogueira Silva on 21/12/2015.
 */
public class ProdutoTabMDAdapter extends FragmentStatePagerAdapter {

    private Context context;

    public ProdutoTabMDAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        //Fragment fragment = null;

        switch (position) {

            case 0:
                Fragment produtoListaFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_LISTA_PRODUTO);

                // Adiciona o argumento no fragment
                produtoListaFragment.setArguments(argumentoFragmet);
                return produtoListaFragment;

            case 1:
                Fragment maisVendidosCidadeFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoCidadeFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoCidadeFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE);

                // Adiciona o argumento no fragment
                maisVendidosCidadeFragment.setArguments(argumentoCidadeFragmet);
                return maisVendidosCidadeFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        //return context.getResources().getStringArray(R.array.tab_produto_md).length;
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titulos = context.getResources().getStringArray(R.array.tab_produto_md);

        return titulos[position];
    }
}

package com.savare.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.savare.R;
import com.savare.activity.material.designer.fragment.OrcamentoProdutoDetalhesHistoricoPrecoMDFragment;
import com.savare.activity.material.designer.fragment.OrcamentoProdutoDetalhesMDFragment;

/**
 * Created by Faturamento on 21/05/2016.
 */
public class OrcamentoProdutoDetalhesTabFragmentoMDAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private Bundle argumentos;

    public OrcamentoProdutoDetalhesTabFragmentoMDAdapter(FragmentManager fm, Context context, Bundle argumentos) {
        super(fm);
        this.context = context;
        this.argumentos = argumentos;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case 0:
                fragment = new OrcamentoProdutoDetalhesMDFragment();
                break;

            case 1:
                fragment = new OrcamentoProdutoDetalhesHistoricoPrecoMDFragment();
                break;
        }

        if (fragment != null) {
            fragment.setArguments(argumentos);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return context.getResources().getStringArray(R.array.tab_orcamento_produto_detalhes_md).length;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titulos = context.getResources().getStringArray(R.array.tab_orcamento_produto_detalhes_md);

        return titulos[position];
    }
}

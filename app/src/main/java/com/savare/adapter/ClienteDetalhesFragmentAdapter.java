package com.savare.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.savare.R;
import com.savare.activity.material.designer.fragment.ClienteDetalhesDadosMDFragment;
import com.savare.activity.material.designer.fragment.ClienteDetalhesGraficoVendasMesMDFragment;

/**
 * Created by Bruno Nogueira Silva on 01/02/2016.
 */
public class ClienteDetalhesFragmentAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private Bundle dadosArgumentos;

    public ClienteDetalhesFragmentAdapter(FragmentManager fm, Context context, Bundle dadosArgumentos) {
        super(fm);
        this.context = context;
        this.dadosArgumentos = dadosArgumentos;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment;

        switch (position){

            case 0:
                fragment = new ClienteDetalhesDadosMDFragment();
                if (dadosArgumentos != null) {
                    fragment.setArguments(dadosArgumentos);
                }
                return fragment;

            case 1:
                fragment = new ClienteDetalhesGraficoVendasMesMDFragment();
                if (dadosArgumentos != null){
                    fragment.setArguments(dadosArgumentos);
                }
                return fragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return context.getResources().getStringArray(R.array.tab_cliente_detalhes_md).length;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titulos = context.getResources().getStringArray(R.array.tab_cliente_detalhes_md);

        return titulos[position];
    }
}

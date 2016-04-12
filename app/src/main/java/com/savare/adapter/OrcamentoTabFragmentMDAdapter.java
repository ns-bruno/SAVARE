package com.savare.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.savare.R;

/**
 * Created by Bruno Nogueira Silva on 11/04/2016.
 */
public class OrcamentoTabFragmentMDAdapter extends FragmentStatePagerAdapter {

    Bundle paramentros;
    Context context;

    public OrcamentoTabFragmentMDAdapter(Context context, FragmentManager fm, Bundle paramentros) {
        super(fm);
        this.paramentros = paramentros;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        switch (position){
            case 0:
                break;
        }
        // Checa se o fragment nao eh nulo
        if (fragment != null){
            fragment.setArguments(paramentros);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return context.getResources().getStringArray(R.array.tab_orcamento_md).length;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titulos = context.getResources().getStringArray(R.array.tab_orcamento_md);

        return titulos[position];
    }
}

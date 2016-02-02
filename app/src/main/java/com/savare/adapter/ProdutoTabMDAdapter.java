package com.savare.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.savare.R;
import com.savare.activity.material.designer.fragment.ProdutoListaMDFragment;
import com.savare.activity.material.designer.ProdutoListaMDActivity;

/**
 * Created by Bruno Nogueira Silva on 21/12/2015.
 */
public class ProdutoTabMDAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private ContentValues dadosArgumentos;
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public ProdutoTabMDAdapter(FragmentManager fm, Context context, ContentValues dadosArgumentos) {
        super(fm);
        this.context = context;
        this.dadosArgumentos = dadosArgumentos;

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

                if (dadosArgumentos != null){
                    argumentoFragmet.putString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO));
                    argumentoFragmet.putString(ProdutoListaMDActivity.KEY_ID_CLIENTE, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_CLIENTE));
                    argumentoFragmet.putString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO));
                    argumentoFragmet.putString(ProdutoListaMDActivity.KEY_NOME_RAZAO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_NOME_RAZAO));
                }
                registeredFragments.put(position, produtoListaFragment);
                // Adiciona o argumento no fragment
                produtoListaFragment.setArguments(argumentoFragmet);
                return produtoListaFragment;

            case 1:
                Fragment maisVendidosCidadeFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoCidadeFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoCidadeFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE);

                if (dadosArgumentos != null){
                    argumentoCidadeFragmet.putString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO));
                    argumentoCidadeFragmet.putString(ProdutoListaMDActivity.KEY_ID_CLIENTE, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_CLIENTE));
                    argumentoCidadeFragmet.putString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO));
                    argumentoCidadeFragmet.putString(ProdutoListaMDActivity.KEY_NOME_RAZAO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_NOME_RAZAO));
                }
                registeredFragments.put(position, maisVendidosCidadeFragment);

                // Adiciona o argumento no fragment
                maisVendidosCidadeFragment.setArguments(argumentoCidadeFragmet);
                return maisVendidosCidadeFragment;

            case 2:
                Fragment maisVendidosAreaFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoAreaFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoAreaFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA);

                if (dadosArgumentos != null){
                    argumentoAreaFragmet.putString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO));
                    argumentoAreaFragmet.putString(ProdutoListaMDActivity.KEY_ID_CLIENTE, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_CLIENTE));
                    argumentoAreaFragmet.putString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO));
                    argumentoAreaFragmet.putString(ProdutoListaMDActivity.KEY_NOME_RAZAO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_NOME_RAZAO));
                }
                registeredFragments.put(position, maisVendidosAreaFragment);

                // Adiciona o argumento no fragment
                maisVendidosAreaFragment.setArguments(argumentoAreaFragmet);
                return maisVendidosAreaFragment;

            case 3:
                Fragment maisVendidosVendedorFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoVendedorFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoVendedorFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_VENDEDOR);

                if (dadosArgumentos != null){
                    argumentoVendedorFragmet.putString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO));
                    argumentoVendedorFragmet.putString(ProdutoListaMDActivity.KEY_ID_CLIENTE, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_CLIENTE));
                    argumentoVendedorFragmet.putString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO));
                    argumentoVendedorFragmet.putString(ProdutoListaMDActivity.KEY_NOME_RAZAO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_NOME_RAZAO));
                }
                registeredFragments.put(position, maisVendidosVendedorFragment);

                // Adiciona o argumento no fragment
                maisVendidosVendedorFragment.setArguments(argumentoVendedorFragmet);
                return maisVendidosVendedorFragment;

            case 4:
                Fragment maisVendidosEmpresaFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoEmpresaFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoEmpresaFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_EMPRESA);

                if (dadosArgumentos != null){
                    argumentoEmpresaFragmet.putString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO));
                    argumentoEmpresaFragmet.putString(ProdutoListaMDActivity.KEY_ID_CLIENTE, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_CLIENTE));
                    argumentoEmpresaFragmet.putString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO));
                    argumentoEmpresaFragmet.putString(ProdutoListaMDActivity.KEY_NOME_RAZAO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_NOME_RAZAO));
                }
                registeredFragments.put(position, maisVendidosEmpresaFragment);

                // Adiciona o argumento no fragment
                maisVendidosEmpresaFragment.setArguments(argumentoEmpresaFragmet);
                return maisVendidosEmpresaFragment;

            case 5:
                Fragment maisVendidosCortesFragment = new ProdutoListaMDFragment();
                // Cria uma vareavel para armazenar os argumentos
                Bundle argumentoCortesFragmet = new Bundle();
                // Informa no argumento o tipo de tela
                argumentoCortesFragmet.putInt(ProdutoListaMDFragment.KEY_TIPO_TELA, ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM);

                if (dadosArgumentos != null){
                    argumentoCortesFragmet.putString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_ORCAMENTO));
                    argumentoCortesFragmet.putString(ProdutoListaMDActivity.KEY_ID_CLIENTE, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ID_CLIENTE));
                    argumentoCortesFragmet.putString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_ATACADO_VAREJO));
                    argumentoCortesFragmet.putString(ProdutoListaMDActivity.KEY_NOME_RAZAO, dadosArgumentos.getAsString(ProdutoListaMDActivity.KEY_NOME_RAZAO));
                }
                registeredFragments.put(position, maisVendidosCortesFragment);

                // Adiciona o argumento no fragment
                maisVendidosCortesFragment.setArguments(argumentoCortesFragmet);
                return maisVendidosCortesFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        return context.getResources().getStringArray(R.array.tab_produto_md).length;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titulos = context.getResources().getStringArray(R.array.tab_produto_md);

        return titulos[position];
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public ContentValues getDadosArgumentos() {
        return dadosArgumentos;
    }

    public void setDadosArgumentos(ContentValues dadosArgumentos) {
        this.dadosArgumentos = dadosArgumentos;
    }
}

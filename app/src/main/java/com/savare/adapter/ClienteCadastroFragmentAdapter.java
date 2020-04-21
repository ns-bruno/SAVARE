package com.savare.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.savare.R;
import com.savare.activity.material.designer.fragment.ClienteCadastroTelefoneMDFragment;
import com.savare.activity.material.designer.fragment.ClienteCadastroDadosMDFragment;

public class ClienteCadastroFragmentAdapter extends FragmentStatePagerAdapter {

	private Bundle paramentros;
	private Context context;

	public ClienteCadastroFragmentAdapter(FragmentManager fm, Context context, Bundle paramentros) {
		super(fm);
		this.context = context;
		this.paramentros = paramentros;
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment = null;
		
		switch (i) {
		
		case 0:
			fragment = new ClienteCadastroDadosMDFragment();
			break;
			
		case 1:
			fragment = new ClienteCadastroTelefoneMDFragment();
			break;
		}
		if (fragment != null) {
			fragment.setArguments(paramentros);
		}
		return fragment;
	}

	@Override
	public int getCount() {

		return context.getResources().getStringArray(R.array.tab_cliente_cadastro_md).length;
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		String[] titulos = context.getResources().getStringArray(R.array.tab_cliente_cadastro_md);

		return titulos[position];
	}

	/**
	 * @return the paramentros
	 */
	public Bundle getParamentros() {
		return paramentros;
	}

	/**
	 * @param paramentros the paramentros to set
	 */
	public void setParamentros(Bundle paramentros) {
		this.paramentros = paramentros;
	}
}

package com.savare.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.savare.activity.fragment.ClienteCadastroDadosFragment;
import com.savare.activity.fragment.ClienteCadastroFragment;
import com.savare.activity.fragment.ClienteCadastroTelefoneFragment;
import com.savare.activity.fragment.OrcamentoFragment;
import com.savare.activity.fragment.OrcamentoPlanoPagamentoFragment;

public class ClienteCadastroFragmentAdapter extends FragmentStatePagerAdapter {

	Bundle paramentros;
	
	public ClienteCadastroFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment = null;
		
		switch (i) {
		
		case ClienteCadastroFragment.DADOS_CLIENTE:
			fragment = new ClienteCadastroDadosFragment();
			fragment.setArguments(paramentros);
			break;
			
		case ClienteCadastroFragment.TELEFONE:
			fragment = new ClienteCadastroTelefoneFragment();
			fragment.setArguments(paramentros);
			break;
		}
		
		return fragment;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return ClienteCadastroFragment.TOTAL_ABAS;
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

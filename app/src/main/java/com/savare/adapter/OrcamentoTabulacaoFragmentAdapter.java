package com.savare.adapter;

import com.savare.activity.fragment.OrcamentoFragment;
import com.savare.activity.fragment.OrcamentoDescontoFragment;
import com.savare.activity.fragment.OrcamentoObservacaoFragment;
import com.savare.activity.fragment.OrcamentoPrazoFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class OrcamentoTabulacaoFragmentAdapter extends FragmentStatePagerAdapter{
	
	Bundle paramentros;

	public OrcamentoTabulacaoFragmentAdapter(FragmentManager fm) {
		super(fm);
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


	@Override
	public Fragment getItem(int i) {
		Fragment fragment = null;
		
		switch (i) {

			case 0:
				fragment = new OrcamentoFragment();
				fragment.setArguments(paramentros);
				break;

			case 1:
				fragment = new OrcamentoDescontoFragment();
				fragment.setArguments(paramentros);
				break;

			case 2:
				fragment = new OrcamentoPrazoFragment();
				fragment.setArguments(paramentros);
				break;

			case 3:
				fragment = new OrcamentoObservacaoFragment();
				fragment.setArguments(paramentros);
				break;
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return 4;
	}
	
}

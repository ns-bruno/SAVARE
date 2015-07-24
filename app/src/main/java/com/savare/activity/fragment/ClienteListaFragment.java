package com.savare.activity.fragment;

import com.savare.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ClienteListaFragment extends Fragment {

	/**
	 * Construtor padrao.
	 */
	public ClienteListaFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_cliente_lista, container, false);
		
		return view;
	}

}

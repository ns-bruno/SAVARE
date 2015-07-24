package com.savare.activity.fragment;

import com.savare.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ResumoFragment extends Fragment {
	

	/**
	 * Construtor padrao.
	 */
	public ResumoFragment() {
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_resumo, container, false);
		
		return view;
	}

}

package com.savare.funcoes.rotinas.receptor;

import com.savare.funcoes.FuncoesPersonalizadas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceptorCriarAlarmeRebootRotinas extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, true);

	}

}

package com.savare.funcoes.rotinas.receptor;

import com.savare.funcoes.FuncoesPersonalizadas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceptorCriarAlarmeRebootRotinas extends BroadcastReceiver {

	boolean enviarAutomatico = false;
	boolean receberAutomatico = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		if (funcoes.getValorXml(funcoes.TAG_ENVIAR_AUTOMATICO).equalsIgnoreCase("S")){
			enviarAutomatico = true;
		}

		if (funcoes.getValorXml(funcoes.TAG_RECEBER_AUTOMATICO).equalsIgnoreCase("S")){
			receberAutomatico = true;
		}

		funcoes.criarAlarmeEnviarAutomatico(enviarAutomatico);
		funcoes.criarAlarmeReceberAutomatico(receberAutomatico);

	}

}

package com.savare.funcoes.rotinas.receptor;

import com.savare.activity.SincronizacaoActivity;
import com.savare.funcoes.rotinas.async.ReceberDadosFtpAsyncRotinas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceptorAlarmeReceberDadosBroadcastRotinas extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(context, ReceberDadosFtpAsyncRotinas.TELA_RECEPTOR_ALARME);
		receberDadosFtpAsync.execute();

	}

}

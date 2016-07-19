package com.savare.funcoes.rotinas.receptor;

import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.async.ReceberDadosFtpAsyncRotinas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceptorAlarmeReceberDadosBroadcastRotinas extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("SAVARE", "onReceive - ReceptorAlarmeReceberDadosBroadcastRotinas");

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);


		if (!funcoes.getValorXml("RecebendoDados").equalsIgnoreCase("S")) {

			// Checa se o tipo de conexao eh por webservice
			if (funcoes.getValorXml("ModoConexao").equalsIgnoreCase("W")){

			} else {
				// Marca nos parametro internos que a aplicacao que esta recebendo os dados
				//funcoes.setValorXml("RecebendoDados", "S");

				// Desavia o recebimento automatico
				funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, false);

				ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(context, ReceberDadosFtpAsyncRotinas.TELA_RECEPTOR_ALARME);
				receberDadosFtpAsync.execute();

				Log.i("SAVARE", "Executou a rotina para receber os dados. - ReceptorAlarmeReceberDadosBroadcastRotinas");
			}
		}

	}

}

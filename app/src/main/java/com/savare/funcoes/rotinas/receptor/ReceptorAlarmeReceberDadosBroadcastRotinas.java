package com.savare.funcoes.rotinas.receptor;

import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosFtpAsyncRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosWebserviceAsyncRotinas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceptorAlarmeReceberDadosBroadcastRotinas extends BroadcastReceiver {

	public static String TAG_RECEBER_DADOS_SAVARE = "RECEBER_DADOS_SAVARE";
	public static Integer TAG_ID_ALARME_RECEBER = 1002;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("SAVARE", "onReceive - ReceptorAlarmeReceberDadosBroadcastRotinas");

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		UsuarioRotinas usuarioRotinas = new UsuarioRotinas(context);
		// Checa se tem mais de 2 horas que foi enviado os ultimos dados
		if (usuarioRotinas.quantidadeHorasUltimoRecebimento() > 2){
			funcoes.setValorXml(funcoes.TAG_RECEBENDO_DADOS, "N");
		}

		if ((!funcoes.getValorXml(funcoes.TAG_RECEBENDO_DADOS).equalsIgnoreCase("S")) && (usuarioRotinas.existeUsuario() == true) &&
				(!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ) {

			// Checa se o tipo de conexao eh por webservice
			if (funcoes.getValorXml("ModoConexao").equalsIgnoreCase("W")){

				ReceberDadosWebserviceAsyncRotinas receberDadosWebservice = new ReceberDadosWebserviceAsyncRotinas(context);
				receberDadosWebservice.execute();

			} else {
				// Desavia o recebimento automatico
				funcoes.criarAlarmeReceberAutomatico(false);
			}
		}

	}

}

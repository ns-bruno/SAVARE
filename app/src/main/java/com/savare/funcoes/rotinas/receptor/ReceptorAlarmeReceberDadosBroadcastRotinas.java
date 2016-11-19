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

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("SAVARE", "onReceive - ReceptorAlarmeReceberDadosBroadcastRotinas");

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		UsuarioRotinas usuarioRotinas = new UsuarioRotinas(context);

		if (usuarioRotinas.quantidadeHorasUltimoRecebimento() > 3){
			funcoes.setValorXml("RecebendoDados", "N");
		}

		if ((!funcoes.getValorXml("RecebendoDados").equalsIgnoreCase("S")) && (usuarioRotinas.existeUsuario() == true) && (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ) {

			// Checa se o tipo de conexao eh por webservice
			if (funcoes.getValorXml("ModoConexao").equalsIgnoreCase("W")){

				ReceberDadosWebserviceAsyncRotinas receberDadosWebservice = new ReceberDadosWebserviceAsyncRotinas(context);
				receberDadosWebservice.execute();

			} else {
				// Marca nos parametro internos que a aplicacao que esta recebendo os dados
				//funcoes.setValorXml("RecebendoDados", "S");

				// Desavia o recebimento automatico
				funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, false);

				//ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(context, ReceberDadosFtpAsyncRotinas.TELA_RECEPTOR_ALARME);
				//receberDadosFtpAsync.execute();

				ReceberDadosWebserviceAsyncRotinas receberDadosWebservice = new ReceberDadosWebserviceAsyncRotinas(context);
				receberDadosWebservice.execute();

				Log.i("SAVARE", "Executou a rotina para receber os dados. - ReceptorAlarmeReceberDadosBroadcastRotinas");
			}
		}

	}

}

package com.savare.funcoes.rotinas.receptor;

import java.util.ArrayList;
import java.util.List;

import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.async.EnviarOrcamentoFtpAsyncRotinas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceptorAlarmeEnviarOrcamentoBroadcastRotinas extends BroadcastReceiver {
	
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
				
		enviarOrcamentoFtpAsync();
	}
	
	private void enviarOrcamentoFtpAsync(){
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		double quatidadeOrcamentoEnviar = funcoes.desformatarValor(orcamentoRotinas.quantidadeListaOrcamento("P", null, null));
		
		// Checa se existe algum orcamento a ser enviado
		if(quatidadeOrcamentoEnviar > 0){
			List<String> lista = new ArrayList<String>();
			
			lista = orcamentoRotinas.listaIdOrcamento("P", null, null);
			
			String [] listaOrcamento = new String[lista.size()]; 
			
			for (int i = 0; i < lista.size(); i++) {
				listaOrcamento[i] = lista.get(i);
			}
			
			// Instancia a classe para executar metodo em segundo plano
			EnviarOrcamentoFtpAsyncRotinas enviarOrcamentoFtpAsync = new EnviarOrcamentoFtpAsyncRotinas(context, EnviarOrcamentoFtpAsyncRotinas.TELA_RECEPTOR_ALARME);
			// Excuta tarefa em segundo plano e passa paramento com a lista de orcamento
			enviarOrcamentoFtpAsync.execute(listaOrcamento);
		}
	}

}

package com.savare.funcoes.rotinas.receptor;

import java.util.ArrayList;
import java.util.List;

import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.EnviarCadastroClienteFtpAsyncRotinas;
import com.savare.funcoes.rotinas.async.EnviarOrcamentoFtpAsyncRotinas;

import android.app.Activity;
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

		//final PessoaRotinas pessoaRotinas = new PessoaRotinas(context);
		//double quantidadeCadastroNovo = pessoaRotinas.quantidadeCadastroPessoaNovo();
		
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
		/*if (quantidadeCadastroNovo > 0){

			String where = " (CFACLIFO.ID_CFACLIFO < 0) AND (CFACLIFO.STATUS_CADASTRO_NOVO = N) ";

			List<PessoaBeans> listaPessoasCadastro = new ArrayList<PessoaBeans>();
			// Pega a lista de pessoa a serem enviadas os dados
			listaPessoasCadastro = pessoaRotinas.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);
			// Checa se retornou alguma lista
			if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0) {
				EnviarCadastroClienteFtpAsyncRotinas enviarCadastro = new EnviarCadastroClienteFtpAsyncRotinas(context, EnviarCadastroClienteFtpAsyncRotinas.TELA_RECEPTOR_ALARME);
				// Executa o envio do cadastro em segundo plano
				enviarCadastro.execute(listaPessoasCadastro);
			}
		}*/
	}

}

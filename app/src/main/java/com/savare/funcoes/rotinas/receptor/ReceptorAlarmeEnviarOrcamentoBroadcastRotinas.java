package com.savare.funcoes.rotinas.receptor;

import java.util.ArrayList;
import java.util.List;

import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;
import com.savare.funcoes.rotinas.async.EnviarCadastroClienteFtpAsyncRotinas;
import com.savare.funcoes.rotinas.async.EnviarDadosWebserviceAsyncRotinas;
import com.savare.funcoes.rotinas.async.EnviarOrcamentoFtpAsyncRotinas;
import com.savare.webservice.WSSisinfoWebservice;

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

		UsuarioRotinas usuarioRotinas = new UsuarioRotinas(context);

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		int horas = usuarioRotinas.quantidadeHorasUltimoEnvio();
		// Checa se tem mais de 3 horas que foi enviado os ultimos dados
		if (usuarioRotinas.quantidadeHorasUltimoEnvio() > 3){
			funcoes.setValorXml("EnviandoDados", "N");
		}

		// Checa se esta enviando dados
		if ((!funcoes.getValorXml("EnviandoDados").equalsIgnoreCase("S")) && (funcoes.getValorXml("EnviarAutomatico").equalsIgnoreCase("S"))) {

			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

			// Marca nos parametro internos que a aplicacao que esta enviando os dados
			funcoes.setValorXml("EnviandoDados", "S");

			// Pega a quantidade de pedidos que precisam ser enviados
			double quatidadeOrcamentoEnviar = funcoes.desformatarValor(orcamentoRotinas.quantidadeListaOrcamento(new String[]{"P"}, null, null));

			//final PessoaRotinas pessoaRotinas = new PessoaRotinas(context);
			//double quantidadeCadastroNovo = pessoaRotinas.quantidadeCadastroPessoaNovo();

			// Checa se existe algum orcamento a ser enviado
			if (quatidadeOrcamentoEnviar > 0) {
				List<String> lista = new ArrayList<String>();

				lista = orcamentoRotinas.listaIdOrcamento("P", OrcamentoRotinas.TABELA_ORCAMENTO, null, null);

				String[] listaOrcamento = new String[lista.size()];

				for (int i = 0; i < lista.size(); i++) {
					listaOrcamento[i] = lista.get(i);
				}

				EnviarDadosWebserviceAsyncRotinas enviarDadosWebservice = new EnviarDadosWebserviceAsyncRotinas(context);
				// Informa quais os pedidos a serem enviados
				enviarDadosWebservice.setIdOrcamentoSelecionado(listaOrcamento);
				// Informa que eh para enviar os dados apenas dos orcamentos e pedidos
				enviarDadosWebservice.setTabelaEnviarDados(new String[]{WSSisinfoWebservice.FUNCTION_INSERT_AEAORCAM, WSSisinfoWebservice.FUNCTION_INSERT_AEAITORC});
				enviarDadosWebservice.execute();

				// Instancia a classe para executar metodo em segundo plano
				//EnviarOrcamentoFtpAsyncRotinas enviarOrcamentoFtpAsync = new EnviarOrcamentoFtpAsyncRotinas(context, EnviarOrcamentoFtpAsyncRotinas.TELA_RECEPTOR_ALARME);
				// Excuta tarefa em segundo plano e passa paramento com a lista de orcamento
				//enviarOrcamentoFtpAsync.execute(listaOrcamento);
			} else {
				// Marca nos parametro internos que a aplicacao que nao esta enviando mais dados
				funcoes.setValorXml("EnviandoDados", "N");
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

}

package com.savare.funcoes.rotinas.receptor;

import java.util.ArrayList;
import java.util.List;

import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;
import com.savare.funcoes.rotinas.async.EnviarDadosWebserviceAsyncRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceptorAlarmeEnviarOrcamentoBroadcastRotinas extends BroadcastReceiver {
	
	private Context context;
	public static String TAG_ENVIAR_ORCAMENTO_SAVARE = "ENVIAR_ORCAMENTO_SAVARE";
	public static Integer TAG_ID_ALARME_ENVIAR = 1001;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
				
		enviarOrcamentoAsync();
	}
	
	private void enviarOrcamentoAsync(){

		UsuarioRotinas usuarioRotinas = new UsuarioRotinas(context);

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		int horas = usuarioRotinas.quantidadeHorasUltimoEnvio();
		// Checa se tem mais de 2 horas que foi enviado os ultimos dados
		if (usuarioRotinas.quantidadeHorasUltimoEnvio() > 2){
			funcoes.setValorXml(funcoes.TAG_ENVIANDO_DADOS, "N");
		}

		// Checa se esta enviando dados
		if ((!funcoes.getValorXml(funcoes.TAG_ENVIANDO_DADOS).equalsIgnoreCase("S")) &&
				(funcoes.getValorXml(funcoes.TAG_ENVIAR_AUTOMATICO).equalsIgnoreCase("S"))) {

			// Marca nos parametro internos que a aplicacao que esta enviando os dados
			funcoes.setValorXml(funcoes.TAG_ENVIANDO_DADOS, "S");

			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

			// Pega a quantidade de pedidos que precisam ser enviados
			double quatidadeOrcamentoEnviar = funcoes.desformatarValor(orcamentoRotinas.quantidadeListaOrcamento(new String[]{OrcamentoRotinas.PEDIDO_NAO_ENVIADO}, null, null));

			// Checa se existe algum orcamento a ser enviado
			if (quatidadeOrcamentoEnviar > 0) {
				List<String> lista = new ArrayList<String>();

				lista = orcamentoRotinas.listaIdOrcamento(OrcamentoRotinas.PEDIDO_NAO_ENVIADO, OrcamentoRotinas.TABELA_ORCAMENTO, null, null);

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

			} else {
				// Marca nos parametro internos que a aplicacao que nao esta enviando mais dados
				funcoes.setValorXml(funcoes.TAG_ENVIANDO_DADOS, "N");
			}
		}
	}

}

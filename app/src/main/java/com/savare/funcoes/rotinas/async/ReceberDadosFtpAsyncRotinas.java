package com.savare.funcoes.rotinas.async;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ImportarDadosTxtRotinas;
import com.savare.funcoes.rotinas.ReceberArquivoTxtServidorFtpRotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;

public class ReceberDadosFtpAsyncRotinas extends AsyncTask<String, String, Integer> {
	
	private Context context;
	private ProgressBar progressReceberDados;
	private TextView textMensagem;
	public static final int TELA_RECEPTOR_ALARME = 0;
	private int telaChamou = -1;
	private String mensagem = " ";
	
	
	public ReceberDadosFtpAsyncRotinas(Context context) {
		this.context = context;
	}
	
	
	public ReceberDadosFtpAsyncRotinas(Context context, ProgressBar progressBar) {
		this.context = context;
		this.progressReceberDados = progressBar;
	}
	
	
	public ReceberDadosFtpAsyncRotinas(Context context, ProgressBar progressBar, TextView textMensagem) {
		this.context = context;
		this.progressReceberDados = progressBar;
		this.textMensagem = textMensagem;
	}
	
	public ReceberDadosFtpAsyncRotinas(Context context, int telaChamou) {
		this.context = context;
		this.telaChamou = telaChamou;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	}
	
	
	@Override
	protected Integer doInBackground(String...params) {
		
		final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		try {
			ReceberArquivoTxtServidorFtpRotinas receberArquivoTxt;
			
			// Checa se que esta chamando esta classe eh o alarme
			if(telaChamou == TELA_RECEPTOR_ALARME){
				receberArquivoTxt = new ReceberArquivoTxtServidorFtpRotinas(context, TELA_RECEPTOR_ALARME);
			} else {
				receberArquivoTxt = new ReceberArquivoTxtServidorFtpRotinas(context, progressReceberDados, textMensagem);
			}
			
			if((params != null) && (params.length > 0)){
				receberArquivoTxt.setBlocoReceber(params[0]);
			}
			// Faz o download dos dados
			ArrayList<String> localDados = new ArrayList<String>();
			// Recebe uma lista de caminhos dos arquivos baixados do servidor FTP
			localDados = receberArquivoTxt.downloadArquivoTxtServidorFtp();
			
			// Checa se retornou algum valor
			if( (localDados != null) & (localDados.size() > 0)){

				// Marca que a aplicacao nao esta mais recebendo dados
				funcoes.setValorXml("RecebendoDados", "S");

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						UsuarioRotinas usuarioRotinas = new UsuarioRotinas(context);
						// Atualiza a data de recebimento dos dados
						usuarioRotinas.atualizaDataHoraRecebimento(null);
					}
				});

				// Passa por todos os registros
				for (int i = 0; i < localDados.size(); i++) {

					// Checa se que esta chamando esta classe eh o alarme
					if(telaChamou == TELA_RECEPTOR_ALARME){
						ImportarDadosTxtRotinas importarDados = new ImportarDadosTxtRotinas(context, localDados.get(i), TELA_RECEPTOR_ALARME);
						importarDados.importarDados();

					} else {
						ImportarDadosTxtRotinas importarDados = new ImportarDadosTxtRotinas(context, localDados.get(i), progressReceberDados, textMensagem);
						// Executa o processo de importacao
						importarDados.importarDados();
					}
				}
			
			} else {
				// Marca que a aplicacao nao esta mais recebendo dados
				funcoes.setValorXml("RecebendoDados", "N");

				// Checa se que esta chamando esta classe eh o alarme
				if(telaChamou != TELA_RECEPTOR_ALARME){
					((Activity) context).runOnUiThread(new Runnable() {
						  public void run() {
							  progressReceberDados.setVisibility(View.GONE);
						  }
					});
				} else {
					mensagem += "Não localizamos o arquivo para receber os dados de atualização.";
				}
			}
		
		} catch (final Exception e) {

			// Marca que a aplicacao nao esta mais recebendo dados
			funcoes.setValorXml("RecebendoDados", "N");

			// Checa se que esta chamando esta classe eh o alarme
			if(telaChamou != TELA_RECEPTOR_ALARME){
				((Activity) context).runOnUiThread(new Runnable() {
					  public void run() {
						
						progressReceberDados.setVisibility(View.GONE);
						
						ContentValues dadosMensagem = new ContentValues();
						
						dadosMensagem.put("comando", 0);
						dadosMensagem.put("tela", "ReceberDadosFtpAsyncRotinas");
						dadosMensagem.put("mensagem", "Erro gravíssimo. Não foi possível pegar os dados do arquivo. \n"
													+ "Possivelmente o layou do arquivo esta errado/desatualizado. \n" + e.getMessage());
						dadosMensagem.put("dados", e.toString());
						dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
						dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
						dadosMensagem.put("email", funcoes.getValorXml("Email"));
						
						funcoes.menssagem(dadosMensagem);
					  }
				});
			}
			//mensagem += "Erro gravíssimo. Não foi possível pegar os dados do arquivo. \n" + e.getMessage() + "\n";
			
		}
		return 0;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		// Checa se que esta chamando esta classe eh o alarme
		if( (mensagem != null) && (mensagem.length() > 1) && (telaChamou != TELA_RECEPTOR_ALARME)){

			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

			ContentValues dadosMensagem = new ContentValues();

			dadosMensagem.put("comando", 0);
			dadosMensagem.put("tela", "ReceberDadosFtpAsyncRotinas");
			dadosMensagem.put("mensagem", mensagem);
			dadosMensagem.put("dados", context + "\n" + mensagem);
			dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
			dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
			dadosMensagem.put("email", funcoes.getValorXml("Email"));

			funcoes.menssagem(dadosMensagem);

			// Cria a intent com identificacao do alarme
			/*Intent intent = new Intent("NOTIFICACAO_SAVARE");
			intent.putExtra("TICKER", "Nova Mensagem quando fomos Receber dados");
			intent.putExtra("TITULO", "SAVARE");
			intent.putExtra("MENSAGEM", mensagem);
			
			context.sendBroadcast(intent);*/
		}
	}

}

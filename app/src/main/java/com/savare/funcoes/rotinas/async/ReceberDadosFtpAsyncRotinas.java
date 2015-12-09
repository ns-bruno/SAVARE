package com.savare.funcoes.rotinas.async;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
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
	public static final int TELA_RECEPTOR_ALARME = 0,
							TELA_LOGIN = 1;
	private int telaChamou = -1;
	private String mensagem = " ";
	
	
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

		// Marca que a aplicacao esta recebendo dados
		funcoes.setValorXml("RecebendoDados", "S");

		try {
			ReceberArquivoTxtServidorFtpRotinas receberArquivoTxt = null;
			
			// Checa se que esta chamando esta classe eh o alarme
			if(telaChamou == TELA_RECEPTOR_ALARME){
				receberArquivoTxt = new ReceberArquivoTxtServidorFtpRotinas(context, TELA_RECEPTOR_ALARME);
			} else {
				// Checa se a barra de progresso e o campos de mensagem estao vazio
				if ((progressReceberDados != null) && (textMensagem != null)) {
					receberArquivoTxt = new ReceberArquivoTxtServidorFtpRotinas(context, progressReceberDados, textMensagem);
				}
			}
			
			if((params != null) && (params.length > 0)){
				receberArquivoTxt.setBlocoReceber(params[0]);
			}
			// Cria variavel para armazenar uma lista dos locais dos arquivos a ser recebido
			ArrayList<String> localDados = new ArrayList<String>();

			// Checa se a classe para fazer o downloads dos arquivos foi instanciada
			if (receberArquivoTxt != null) {
				// Recebe uma lista de caminhos dos arquivos baixados do servidor FTP
				localDados = receberArquivoTxt.downloadArquivoTxtServidorFtp();
			}

			if (telaChamou == TELA_LOGIN){
				File pastaTemporaria = new File(Environment.getExternalStorageDirectory() + "/SAVARE/TEMP");
				// Checa se existe a pasta temporaria
				if (pastaTemporaria.exists()){
					// Lista todos os arquivos que contem na pasta temporaria
					File[] listaArquivosPastaTemp = pastaTemporaria.listFiles();

					if ((listaArquivosPastaTemp != null) && (listaArquivosPastaTemp.length > 0)){
						// Passa por todos os arquivos que existe
						for (File arquivo : listaArquivosPastaTemp){

							// Checa se eh um arquivo e se eh o arquivo de recebimento
							if ((arquivo.isFile()) && (arquivo.getName().contains(funcoes.getValorXml("Usuario"))) &&
									((arquivo.getName().contains(ReceberArquivoTxtServidorFtpRotinas.EXTENCAO_DOWNLOADS_BLOCO_A)) ||
									(arquivo.getName().contains(ReceberArquivoTxtServidorFtpRotinas.EXTENCAO_DOWNLOADS_BLOCO_C)) ||
									(arquivo.getName().contains(ReceberArquivoTxtServidorFtpRotinas.EXTENCAO_DOWNLOADS_BLOCO_R)) ||
									(arquivo.getName().contains(ReceberArquivoTxtServidorFtpRotinas.EXTENCAO_DOWNLOADS_BLOCO_S)) ||
									(arquivo.getName().contains(ReceberArquivoTxtServidorFtpRotinas.EXTENCAO_DOWNLOADS_UNIVERSAL)) )){

								Log.i("SAVARE", "arquivo local localizado - " + arquivo.getPath() + " - ReceberDadosFtpAsyncRotinas");

								// Adiciona o caminho do arquivo a lista de camihos
								localDados.add(arquivo.getPath());
							}
						}
					}
				}
			}

			// Checa se retornou algum valor
			if( (localDados != null) & (localDados.size() > 0)){

				// Passa por todos os registros
				for (int i = 0; i < localDados.size(); i++) {
					Log.i("SAVARE", "importar os dados do arquivo "+ localDados.get(i) + " - ReceberDadosFtpAsyncRotinas");

					// Checa se que esta chamando esta classe eh o alarme
					if((telaChamou == TELA_RECEPTOR_ALARME) || (telaChamou == TELA_LOGIN)){
						ImportarDadosTxtRotinas importarDados = new ImportarDadosTxtRotinas(context, localDados.get(i), TELA_RECEPTOR_ALARME);
						importarDados.importarDados();

					} else {
						ImportarDadosTxtRotinas importarDados = new ImportarDadosTxtRotinas(context, localDados.get(i), progressReceberDados, textMensagem);
						// Executa o processo de importacao
						importarDados.importarDados();
					}
					File pastaTem = new File(Environment.getExternalStorageDirectory() + "/SAVARE/TEMP");
					// Instancia o arquivo recebido
					File arquivoRecebido = new File(localDados.get(i));
					// checa se existe o arquivo
					if (arquivoRecebido.exists()){
						// deleta o arquivo recebido
						arquivoRecebido.delete();
					}

				}

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						UsuarioRotinas usuarioRotinas = new UsuarioRotinas(context);
						// Atualiza a data de recebimento dos dados
						usuarioRotinas.atualizaDataHoraRecebimento(null);
					}
				});
			} else {
				// Marca que a aplicacao nao esta mais recebendo dados
				funcoes.setValorXml("RecebendoDados", "N");

				// Checa se que esta chamando esta classe eh o alarme
				if((telaChamou != TELA_RECEPTOR_ALARME) && (telaChamou != TELA_LOGIN)){
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

			Log.e("SAVARE", "erro " + e.getMessage() + " - ReceberDadosFtpAsyncRotinas");

			// Marca que a aplicacao nao esta mais recebendo dados
			funcoes.setValorXml("RecebendoDados", "N");

			funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, true);

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

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		// Marca que a aplicacao nao esta mais recebendo dados
		funcoes.setValorXml("RecebendoDados", "N");

		funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, true);

		// Checa se que esta chamando esta classe eh o alarme
		if( (mensagem != null) && (mensagem.length() > 1) && (telaChamou != TELA_RECEPTOR_ALARME)){

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

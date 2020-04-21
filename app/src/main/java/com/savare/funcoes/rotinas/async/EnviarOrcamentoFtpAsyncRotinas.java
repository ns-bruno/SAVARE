package com.savare.funcoes.rotinas.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.NotificationCompat;

import com.savare.R;
import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.VersionUtils;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.GerarXmlOrcamentoRotinas;

public class EnviarOrcamentoFtpAsyncRotinas extends AsyncTask<String, String, Integer> {

	private Context context;
	private ProgressDialog progress;
	private FTPClient conexaoFtp = new FTPClient();
	private CopyStreamAdapter streamListener;
	private int orientacaoTela;
	private String mensagemErro;
	private int telaChamada;
	public static final int TELA_RECEPTOR_ALARME = 0;



	public EnviarOrcamentoFtpAsyncRotinas(Context context) {
		this.context = context;
	}
	
	
	public EnviarOrcamentoFtpAsyncRotinas(Context context, int telaChamada) {
		this.context = context;
		this.telaChamada = telaChamada;
	}

	public EnviarOrcamentoFtpAsyncRotinas(Context context, int telaChamada, String mensagemRetorno) {
		this.context = context;
		this.telaChamada = telaChamada;
		this.mensagemErro = mensagemRetorno;
	}
	
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		mensagemErro = " ";
		
		if(telaChamada != TELA_RECEPTOR_ALARME){
			// Desabilita a orientacao automatica do aparelho
			bloqueiaOrientacaoTela();

			// Cria novo um ProgressDialogo e exibe
			progress = new ProgressDialog(context);
			progress.setMessage("Aguarde, gerando os arquivos necessários à serem enviados...");
			progress.setCancelable(false);
			progress.show();
		}
		/*PugNotification.with(context)
					   .load()
					   .title(R.string.enviando_pedidos)
					   .bigTextStyle("Aguarde, gerando os arquivos necessários à serem enviados...")
					   .identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
					   .smallIcon(R.mipmap.ic_launcher)
					   .progress()
					   .value(0, 0, true)
					   .build();*/

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.mipmap.ic_launcher_smallicon)
				.setContentTitle(context.getResources().getString(R.string.enviando_pedidos))
				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText("Aguarde, gerando os arquivos necessários à serem enviados..."))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		String name = "FileNotification";
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			NotificationChannel mChannel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_MIN);
			mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
			mChannel.enableLights(true);
			notificationManager.createNotificationChannel(mChannel);
		}
		notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO, mBuilder.build());
	}

	@Override
	protected Integer doInBackground(final String... params) {

		final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		int totalEnviado = 0;
		int qtdUpdate = 0;
		// Checa se nao foi cancela o progresso
		if (!this.isCancelled()) {

			// Atualiza a mensagem da barra de progresso
			if(telaChamada != TELA_RECEPTOR_ALARME){
				publishProgress("Estamos verificando se existe alguma conexão com a internet, aguarde...");
			}
			/*PugNotification.with(context)
					.load()
					.title(R.string.enviando_pedidos)
					.bigTextStyle("Estamos verificando se existe alguma conexão com a internet, aguarde...")
					.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
					.smallIcon(R.mipmap.ic_launcher)
					.progress()
					.value(0, 0, true)
					.build();*/

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
					.setSmallIcon(R.mipmap.ic_launcher_smallicon)
					.setContentTitle(context.getResources().getString(R.string.enviando_pedidos))
					.setStyle(new NotificationCompat.BigTextStyle()
							.bigText("Estamos verificando se existe alguma conexão com a internet, aguarde..."))
					.setPriority(NotificationCompat.PRIORITY_DEFAULT);
			String name = "FileNotification";
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
				NotificationChannel mChannel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_MIN);
				mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
				mChannel.enableLights(true);
				notificationManager.createNotificationChannel(mChannel);
			}
			notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO, mBuilder.build());



			// Checa se tem internet
			if (funcoes.existeConexaoInternet()) {
				// Instancia a classe responsavel por gerar o arquivo XML
				GerarXmlOrcamentoRotinas gerarXml = new GerarXmlOrcamentoRotinas(context);

				UsuarioSQL usuarioSQL = new UsuarioSQL(context);
				// Pega os dados do usuario e conexoes
				Cursor dadosUsuario = usuarioSQL.query("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));
				// Move para o primeiro registro
				dadosUsuario.moveToFirst();

				// Pega o host(endereco) do servidor FTP
				String hostFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("IP_SERVIDOR_USUA"));
				String usuarioFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("USUARIO_SERVIDOR_USUA"));
				String senhaFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_SERVIDOR_USUA"));
				String pastaFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("PASTA_SERVIDOR_USUA"));

				for (int i = 0; i < params.length; i++) {
					// Passa por paramento o id do orcamento a ser gerado XML
					gerarXml.setIdOrcamento(params[i]);

					// Atualiza a mensagem da barra de progresso
					if(telaChamada != TELA_RECEPTOR_ALARME){
						publishProgress("Estamos gerando o arquivo " + (i + 1) + " de " + params.length + ", aguarde...");
					}
					/*PugNotification.with(context)
							.load()
							.title(R.string.enviando_pedidos)
							.bigTextStyle("Estamos gerando o arquivo " + (i + 1) + " de " + params.length + ", aguarde...")
							.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
							.smallIcon(R.mipmap.ic_launcher)
							.progress()
							.value(0, 0, true)
							.build();*/

					File localXml = new File(gerarXml.criarArquivoXml());

					// Checa se existe algum caminho de arquivo
					if (localXml.getPath().length() > 0) {
						// Atualiza a status do prograsso
						if(telaChamada != TELA_RECEPTOR_ALARME){
							publishProgress("O arquivo " + (i + 1) + " de " + params.length + " foi gerado com sucesso, estamos conectando com o servidor em nuvem, aguarde...");
						}
						/*PugNotification.with(context)
								.load()
								.title(R.string.enviando_pedidos)
								.bigTextStyle("O arquivo " + (i + 1) + " de " + params.length + " foi gerado com sucesso, estamos conectando com o servidor em nuvem, aguarde...")
								.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
								.smallIcon(R.mipmap.ic_launcher)
								.progress()
								.value(0, 0, true)
								.build();*/
						try {
							conexaoFtp.setConnectTimeout(20 * 1000);
							conexaoFtp.setDefaultTimeout(40 * 1000);

							// Conecta com o servidor FTP usando a porta 21
							conexaoFtp.connect(hostFtp);

							boolean status = false;

							// Checa o codigo de resposta, se for positivo, a conexao foi feita
							if (FTPReply.isPositiveCompletion(conexaoFtp.getReplyCode())) {

								// Autenticacao com usuario e senha
								status = conexaoFtp.login(usuarioFtp, funcoes.descriptografaSenha(senhaFtp));
							} else {
								// Atualiza a mensagem do progresso
								if(telaChamada != TELA_RECEPTOR_ALARME){
									publishProgress( mensagemErro = mensagemErro + "Não foi possível conectar no servidor em nuvem. Vamos para próxima etapa, aguarde... \n");
								}
								/*PugNotification.with(context)
										.load()
										.title(R.string.enviando_pedidos)
										.bigTextStyle("Não foi possível conectar no servidor em nuvem. Vamos para próxima etapa, aguarde...")
										.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
										.smallIcon(R.mipmap.ic_launcher)
										.progress()
										.value(0, 0, true)
										.build();*/
							}

							if (status) {
								// Atualiza a mensagem do progresso
								if(telaChamada != TELA_RECEPTOR_ALARME){
									publishProgress("Conectou com sucesso no servidor em nuvem. Estamos enviado o arquivo " + (i + 1) + " de " + params.length + ", aguarde...");
								}
								/*PugNotification.with(context)
										.load()
										.title(R.string.enviando_pedidos)
										.bigTextStyle("Conectou com sucesso no servidor em nuvem. Estamos enviado o arquivo " + (i + 1) + " de " + params.length + ", aguarde...")
										.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
										.smallIcon(R.mipmap.ic_launcher)
										.progress()
										.value(0, 0, true)
										.build();*/
								// Checa se o arquivo xml existe
								if (localXml.exists()) {
									// Setando para o modo de transferencia de Arquivos
									// conexaoFtp.setFileType(FTP.BINARY_FILE_TYPE);
									conexaoFtp.enterLocalPassiveMode();
									conexaoFtp.setFileTransferMode(FTPClient.ASCII_FILE_TYPE);
									conexaoFtp.setFileType(FTPClient.ASCII_FILE_TYPE);

									//String nomeDiretorioFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("LOGIN_USUA"));

									// Vai para uma pasta especifica no servidor FTP
									if (!conexaoFtp.changeWorkingDirectory(pastaFtp.replace("/", ""))) {
										criarDiretorio(pastaFtp.replace("/", ""));
									}
                                    // Pega o Arquivo a ser enviado e transforma em um formato proprio para transferir
									FileInputStream arqEnviar = new FileInputStream(localXml);
									// Pega o nome do arquivo XML
									String nome = localXml.getName();
									// Pega o tamanho total do arquivo XML em bytes
									final double totalByteArquivo = (double) localXml.length();
									final int arquivoAtual = i;

									/*PugNotification.with(context)
											.load()
											.title(R.string.enviando_pedidos)
											.bigTextStyle("Conectou com sucesso no servidor em nuvem. Estamos enviado o arquivo " + (i + 1) + " de " + params.length + ", aguarde...")
											.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
											.smallIcon(R.mipmap.ic_launcher)
											.progress()
											.value(0, (int) localXml.length(), false)
											.build();*/

									// Cria um formato para o formatar numeros
									// reais (double)
									final NumberFormat format = NumberFormat.getInstance();
									format.setMaximumFractionDigits(4);
									format.setMinimumFractionDigits(4);
									format.setMaximumIntegerDigits(3);
									format.setMinimumIntegerDigits(1);
									// format.setRoundingMode(RoundingMode.HALF_UP);

									// Pega em tempo real o que esta acontecendo na transferencia
									streamListener = new CopyStreamAdapter() {
										@Override
										public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
											
											if(telaChamada != TELA_RECEPTOR_ALARME){
												double totalArquivo = (double) totalByteArquivo;
												// Converte o total transferido em double
												double totalTransferido = (double) totalBytesTransferred;
												// Calcula em percentual do total transferido do arquivo XML
												double percentual = ((totalTransferido / totalArquivo) * 100);
	
												publishProgress("Foi transferido " + format.format(percentual) + "%, " + totalBytesTransferred + " de " + totalByteArquivo
														+ " bytes do arquivo " + (arquivoAtual + 1) + " de " + params.length + ", aguarde...");

											}
											/*PugNotification.with(context)
													.load()
													.title(R.string.enviando_pedidos)
													.bigTextStyle("Transferindo o aquivo, aguarde...")
													.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
													.smallIcon(R.mipmap.ic_launcher)
													.progress()
													.value(bytesTransferred, (int) totalByteArquivo, false)
													.build();*/
										}

										@Override
										public void bytesTransferred(CopyStreamEvent arg0) {

										}
									};
									// Associa um ouvite de transferencia de byte
									conexaoFtp.setCopyStreamListener(streamListener);
									// Incrementa o tamanho da transferencia do arquivo
							        conexaoFtp.setBufferSize(1024000);
									
									// Envia o arquivo
									if (conexaoFtp.storeFile("arquivoDeEnvio_" + funcoes.getValorXml("ChaveEmpresa") + ".txt", arqEnviar)) {
										
										if(telaChamada != TELA_RECEPTOR_ALARME){
											publishProgress("Confirmando o envio do arquivo " + (i + 1) + " de " + params.length + ", aguarde...");
										}
										/*PugNotification.with(context)
												.load()
												.title(R.string.enviando_pedidos)
												.bigTextStyle("Confirmando o envio do arquivo " + (i + 1) + " de " + params.length + ", aguarde...")
												.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
												.smallIcon(R.mipmap.ic_launcher)
												.progress()
												.value(0, (int) localXml.length(), false)
												.build();*/

										// Renomeia o arquivo enviado para checar se chegou no servidor FTP com  sucesso
										if (renomeaArquivoFtp("arquivoDeEnvio_" + funcoes.getValorXml("ChaveEmpresa") + ".txt", nome)) {

											if(telaChamada != TELA_RECEPTOR_ALARME){
												publishProgress("Arquvio " + (i + 1) + " de " + params.length + " enviado com sucesso, eliminando os arquivos temporarios, aguarde...");
											}
											/*PugNotification.with(context)
													.load()
													.title(R.string.enviando_pedidos)
													.bigTextStyle("Confirmando o envio do arquivo " + (i + 1) + " de " + params.length + ", aguarde...")
													.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
													.smallIcon(R.mipmap.ic_launcher)
													.progress()
													.value(0, 0, true)
													.build();*/
											// Desconecta do servidor FTP
											conexaoFtp.disconnect();

											// Cria um formato de data
											DateFormat dataFormatada = new SimpleDateFormat("MM_yyyy");
											// Instancia com a data atual
											Date data = new Date();

											File localNovoDiretorio = new File(Environment.getExternalStorageDirectory()+ "/SAVARE/XML/ENVIADOS/"+ dataFormatada.format(data));
											// Checa se a pasta xml existe
											if (!localNovoDiretorio.exists()) {
												localNovoDiretorio.mkdirs();
											}
											// Move o arquivo XML para a pasta  de arquivos enviados
											if (localXml.renameTo(new File(localNovoDiretorio, localXml.getName()))) {

												// Atualiza a mensagem do progressDialog
												if(telaChamada != TELA_RECEPTOR_ALARME){
													publishProgress("Arquvio temporario " + (i + 1) + " de " + params.length + " eliminado com sucesso, vamos passar para o próximo, aguarde...");
												}
												/*PugNotification.with(context)
														.load()
														.title(R.string.enviando_pedidos)
														.bigTextStyle("Arquvio temporario " + (i + 1) + " de " + params.length + " eliminado com sucesso, vamos passar para o próximo, aguarde...")
														.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
														.smallIcon(R.mipmap.ic_launcher)
														.progress()
														.value(0, 0, true)
														.build();*/
												// Pega o id do orcamento
												final String idOrcamento = params[i];
												
												if(telaChamada != TELA_RECEPTOR_ALARME){
													// Executa como um nova Thread para nao gerar erro
													((Activity) context).runOnUiThread(new Runnable() {
														public void run() {
															
															// Instancia a classe para manipular os orcamento no banco de dados
															OrcamentoSql orcamentoSql = new OrcamentoSql(context);
																			
															ContentValues dadosPedido = new ContentValues();
															// Inseri o campo e o novo valor a ser atualizado (ENVIADO)
															dadosPedido.put("STATUS", "N");
															
															if(orcamentoSql.update(dadosPedido, "AEAORCAM.ID_AEAORCAM = " + idOrcamento) <= 0){
																mensagemErro = mensagemErro + "Não foi possível marcar o pedido " + idOrcamento + " como enviado. \n";
															}
															ContentValues dadosEnvio = new ContentValues();
															//
														}
													});
												
												}else {
													ContentValues dadosPedido = new ContentValues();
													// Inseri o campo e o novo valor a ser atualizado (ENVIADO)
													dadosPedido.put("STATUS", "N");
													
													ConexaoBancoDeDados conexaoBanco = new ConexaoBancoDeDados(context, VersionUtils.getVersionCode(context));
													SQLiteDatabase bancoDados;
													
													bancoDados = conexaoBanco.abrirBanco();
													
													qtdUpdate += bancoDados.updateWithOnConflict("AEAORCAM", dadosPedido, "AEAORCAM.ID_AEAORCAM = " + idOrcamento, null, 0);

                                                    bancoDados.execSQL("UPDATE USUARIO_USUA SET DT_ULTIMO_ENVIO = (DATETIME('NOW', 'localtime'))");
												}
												// Incrementa o total de arquivos enviados com sucesso
												totalEnviado++;

												// Marca nas propriedades interna da aplicacao que nao esta mais enviando dados
												funcoes.setValorXml("EnviandoDados", "N");

												if(qtdUpdate <= 0){
													mensagemErro = mensagemErro + "Não foi possível marcar o pedido " + idOrcamento + " como enviado. \n";
												} else {
													mensagemErro = mensagemErro + "Foi enviado " + totalEnviado + " e \n Marcado como enviado " + qtdUpdate + " Pedido(s).";
												}
											} else {
												mensagemErro = mensagemErro + "Erro ao mover o arquivo enviado para a pasta ENVIADOS. " + localXml.getPath() + "\n";
											}
										} else { // Fim if renomeArquivoFtp
											mensagemErro = mensagemErro + "Erro ao checar se o arquivo foi enviado. \n";
										}
											// Fecha o arquivo a ser enviado
										arqEnviar.close();
									} else {// Fim if storeFile
										mensagemErro = mensagemErro + "Erro ao fazer upload do arquivo. \n";
									}
									
								} else {
									mensagemErro += "Não foi encontrado o arquivo para envio. \n";
								}
							} else { // Fim if status
								mensagemErro += "Usuário ou Senha estão errados. Favor verificar \n";
							}
							// Deleta o arquivo XML
							localXml.delete();
							// Fecha a conexao FTP
							conexaoFtp.disconnect();
						
						} catch (final IOException e) {
							// Deleta o arquivo XML a ser enviado
							localXml.delete();
							// Salva os dados da mensagem
							mensagemErro += "Não foi possível enviar para a nuvem nenhum arquivo. \n" + e.getMessage() +  "\n";

						} catch (final Exception e) {
							// Deleta o arquivo XML a ser enviado
							localXml.delete();
							// Salva os dados da mensagem
							mensagemErro += "Não foi possível enviar para a nuvem nenhum arquivo. \n" + e.getMessage() +  "\n";
							
						}
					} else {
						// Atualiza a menssagem quando nao gerar o arquivo xml com sucesso
						if(telaChamada != TELA_RECEPTOR_ALARME){
							publishProgress(mensagemErro = "Arquivo " + (i + 1) + " de " + params.length + "Apresentou algum erro, vamos para o pr�ximo...");
						}
					}
				} // Fim for
			
			} else { // Fim checaInternet
				
				mensagemErro += "Não existe conexão com a internet. \n";
			}
		
		} else { // Fim do canceled
			
			mensagemErro += "A operação de envio foi cancelada. \n";
		}

		return totalEnviado;
	} // Fim doInBackground

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		// Atualiza mensagem
		if(telaChamada != TELA_RECEPTOR_ALARME){
			progress.setMessage(values[0]);
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		funcoes.setValorXml("EnviandoDados", "N");

		if(telaChamada != TELA_RECEPTOR_ALARME){
			ContentValues dadosMensagem = new ContentValues();
			dadosMensagem.put("comando", 0);
			dadosMensagem.put("tela", "EnviarOrcamentoFtpAsyncRotinas");
			dadosMensagem.put("dados", mensagemErro);
			dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
			dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
			dadosMensagem.put("email", funcoes.getValorXml("Email"));
			
			if (result > 0) {
				dadosMensagem.put("comando", 1);
				dadosMensagem.put("mensagem", "Foram enviados " + result + " arquivos.");
			} else {
				dadosMensagem.put("mensagem", mensagemErro);
			}
			funcoes.menssagem(dadosMensagem);
	
			// Fecha progressDialogo
			if (progress.isShowing()) {
				progress.dismiss();
			}
	
			desbloqueiaOrientacaoTela();
		} else {

			/*PugNotification.with(context)
					.load()
					.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
					.title(R.string.enviando_pedidos)
					.bigTextStyle(mensagemErro)
					.smallIcon(R.mipmap.ic_launcher)
					.largeIcon(R.mipmap.ic_launcher)
					.flags(Notification.DEFAULT_ALL)
					.simple()
					.build();*/
		}
	}

	/**
	 * Renomea um arquivo especifico dentro da pasta do servidor FTP.
	 * 
	 * @param nomeAntigo
	 * @param nomeNovo
	 * @return
	 */
	public boolean renomeaArquivoFtp(final String nomeAntigo, final String nomeNovo) {
		try {
			boolean status = conexaoFtp.rename(nomeAntigo, nomeNovo);
			return status;

		} catch (final Exception e) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					ContentValues dadosMensagem = new ContentValues();

					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(
							context);

					dadosMensagem.put("comando", 0);
					dadosMensagem.put("tela", "EnviarOrcamentoFtpAsyncRotinas");
					dadosMensagem.put("mensagem", "Não foi possível renomear o arquivo xml de " + nomeAntigo + " para " + nomeNovo + "\n" + e.getMessage());
					dadosMensagem.put("dados", e.toString());
					dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
					dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
					dadosMensagem.put("email", funcoes.getValorXml("Email"));

					funcoes.menssagem(dadosMensagem);
				}
			});
		}
		return false;
	} // Fim renomeaArquivoFtp

	/**
	 * 
	 * @param nomeArquivo
	 * @return
	 */
	public boolean deletarArquivoFtp(String nomeArquivo) {
		try {
			boolean status = conexaoFtp.deleteFile(nomeArquivo);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	} // deletarArquivoFtp

	/**
	 * 
	 * @param nomeDiretorio
	 * @return
	 */
	public boolean criarDiretorio(final String nomeDiretorio) {
		try {
			boolean status = conexaoFtp.makeDirectory(nomeDiretorio);
			return status;

		} catch (final Exception e) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					ContentValues dadosMensagem = new ContentValues();

					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(
							context);

					dadosMensagem.put("comando", 0);
					dadosMensagem.put("tela", "EnviarOrcamentoFtpAsyncRotinas");
					dadosMensagem.put("mensagem",
							"N�o foi poss�vel criar o diretorio "
									+ nomeDiretorio + "\n" + e.getMessage());
					dadosMensagem.put("dados", e.toString());
					dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
					dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
					dadosMensagem.put("email", funcoes.getValorXml("Email"));

					funcoes.menssagem(dadosMensagem);
				}
			});
		}
		return false;
	} // Fim criarDiretorio

	
	private void bloqueiaOrientacaoTela() {
		// Pega a orientacao atual da tela
		orientacaoTela = context.getResources().getConfiguration().orientation;

		// Checa em qual orientacao esta atualmente para bloquear
		if (orientacaoTela == Configuration.ORIENTATION_PORTRAIT) {
			((Activity) context)
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			((Activity) context)
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	private void desbloqueiaOrientacaoTela() {
		if (orientacaoTela > 0) {
			((Activity) context).setRequestedOrientation(orientacaoTela);
		} else {
			((Activity) context)
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
}

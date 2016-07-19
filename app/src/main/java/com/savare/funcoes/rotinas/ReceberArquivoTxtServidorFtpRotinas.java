package com.savare.funcoes.rotinas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;

import br.com.goncalves.pugnotification.notification.Load;
import br.com.goncalves.pugnotification.notification.PugNotification;

public class ReceberArquivoTxtServidorFtpRotinas {

	private Context context;
	private FTPClient conexaoFtp = new FTPClient();
	private CopyStreamAdapter streamListener;
	private String mensagemErro = "";
	private String blocoReceber;
	private ProgressBar progressDownloads;
	private TextView textMensagemRetorno;
	private double tamanhoArquivo = 0;
	private int telaChamou = -1;
    List<FTPFile> listaDadosArquivoFtp; // Cria uma vareavel para pegar a lista de arquivos que estao no servidor FTP
	Load mLoad;
	public static final int TELA_RECEPTOR_ALARME = 0;
	public static final String EXTENCAO_DOWNLOADS_UNIVERSAL = ".SAVARE",
                               EXTENCAO_DOWNLOADS_BLOCO_S = "BLOCO_S.SAVARE",
                               EXTENCAO_DOWNLOADS_BLOCO_A = "BLOCO_A.SAVARE",
                               EXTENCAO_DOWNLOADS_BLOCO_C = "BLOCO_C.SAVARE",
                               EXTENCAO_DOWNLOADS_BLOCO_R = "BLOCO_R.SAVARE";
	
	public ReceberArquivoTxtServidorFtpRotinas(Context context) {
		this.context = context;
		blocoReceber = null;
	}
	
	public ReceberArquivoTxtServidorFtpRotinas(Context context, int telaChamou) {
		this.context = context;
		blocoReceber = null;
		this.telaChamou = telaChamou;
	}
	
	public ReceberArquivoTxtServidorFtpRotinas(Context context, ProgressBar progressBar) {
		this.context = context;
		this.progressDownloads = progressBar;
		this.blocoReceber = null;
		
		((Activity) context).runOnUiThread(new Runnable() {
			  public void run() {
				progressDownloads.setVisibility(View.VISIBLE);
				progressDownloads.setIndeterminate(true);
				//progressDownloads.setProgress(0);
			  }
		});
	}
	
	public ReceberArquivoTxtServidorFtpRotinas(Context context, ProgressBar progressBar, TextView textMensagem) {
		this.context = context;
		this.progressDownloads = progressBar;
		this.textMensagemRetorno = textMensagem;
		this.blocoReceber = null;
		
		((Activity) context).runOnUiThread(new Runnable() {
			  public void run() {
				progressDownloads.setVisibility(View.VISIBLE);
				progressDownloads.setIndeterminate(true);
				textMensagemRetorno.setVisibility(View.VISIBLE);
				textMensagemRetorno.setText("Verificando se existe internet...");
			  }
		});
		
	} // Fim Construtor
	
	
	/**
	 * Retorna qual o bloco que eh para ser recebido.
	 * 
	 * @return the blocoReceber
	 */
	public String getBlocoReceber() {
		return blocoReceber;
	}

	/**
	 * @param blocoReceber the blocoReceber to set
	 */
	public void setBlocoReceber(String blocoReceber) {
		this.blocoReceber = blocoReceber;
	}



	public ArrayList<String> downloadArquivoTxtServidorFtp(){

		// Cria uma notificacao para ser manipulado
		mLoad = PugNotification.with(context).load()
				.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
				.smallIcon(R.mipmap.ic_launcher)
				.largeIcon(R.drawable.ic_launcher)
				.title(R.string.importar_dados_recebidos)
				.bigTextStyle("Estamos Conectando no Servidor em Nuvem...")
				.flags(Notification.DEFAULT_SOUND);

		ArrayList<String> localArquivoRecebido = new ArrayList<String>();
		
		final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
        UsuarioSQL usuarioSQL = new UsuarioSQL(context);
		// Pega os dados do usuario e conexoes
		Cursor dadosUsuario = usuarioSQL.query("ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));

		if (dadosUsuario != null && dadosUsuario.getCount() > 0){
			// Move para o primeiro registro
			dadosUsuario.moveToFirst();

			// Pega o host(endereco) e outros dados do servidor FTP
			String hostFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("IP_SERVIDOR_USUA"));
			String usuarioFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("USUARIO_SERVIDOR_USUA"));
			String senhaFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("SENHA_SERVIDOR_USUA"));
			String nomeLogin = dadosUsuario.getString(dadosUsuario.getColumnIndex("LOGIN_USUA"));
			String nomeDiretorioFtp = dadosUsuario.getString(dadosUsuario.getColumnIndex("PASTA_SERVIDOR_USUA"));
			String modoConexao = dadosUsuario.getString(dadosUsuario.getColumnIndex("MODO_CONEXAO"));

			File pastaTemp = new File(Environment.getExternalStorageDirectory() + "/SAVARE/TEMP");

			// Checa se tem internet
			if(funcoes.existeConexaoInternet()){
				try {
					// Cria a pasta temp se nao existir
					if(!pastaTemp.exists()){
						pastaTemp.mkdirs();
					}

					conexaoFtp.setConnectTimeout(10 * 1000);
					conexaoFtp.setDefaultTimeout(30 * 1000);

					// Indica que essa notificacao eh do tipo progress
					mLoad.progress().value(0, 0, true).build();

					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
							  public void run() {
									  // Atualiza a mensagem de retorno
									  textMensagemRetorno.setText("Estamos Conectando no Servidor em Nuvem...");
							  }
						});
					}
					// Conecta com o servidor FTP usando a porta 21
					conexaoFtp.connect(hostFtp);

					boolean status = false;

					//Checa o codigo de resposta, se for positivo, a conexao foi feita
					if (FTPReply.isPositiveCompletion(conexaoFtp.getReplyCode())) {

						// Atualiza a mensagem na notificacao
						mLoad.bigTextStyle("Conseguimos Conectar no Servidor, agora vamos logar...").progress().build();

						if(telaChamou != TELA_RECEPTOR_ALARME){
							((Activity) context).runOnUiThread(new Runnable() {
								  public void run() {
									  // Atualiza a mensagem na tela de sincronizacao
									  textMensagemRetorno.setText("Conseguimos Conectar no Servidor, agora vamos logar...");
								  }
							});
						}
						// Autenticacao com usuario e senha
						status = conexaoFtp.login(usuarioFtp, funcoes.descriptografaSenha(senhaFtp));
					} else {
						// Atualiza a mensagem na notificacao
						mLoad.bigTextStyle("Erro ao conectar no Servidor em Nuvem, nem conseguimos logar.").progress().build();

						if(telaChamou != TELA_RECEPTOR_ALARME){
							((Activity) context).runOnUiThread(new Runnable() {
								  public void run() {
									  // Atualiza a mensagem na tela de sincronizacao
									  textMensagemRetorno.setText("Erro ao conectar no Servidor em Nuvem, nem conseguimos logar.");
								  }
							});
						}
						// Atualiza a mensagem do progresso
						mensagemErro = "Não foi possível conectar no servidor em nuvem, nem conseguimos logar. \n";
					}

					if (status){
						// Atualiza a mensagem na notificacao
						mLoad.bigTextStyle("Logado com sucesso.").progress().build();

						if(telaChamou != TELA_RECEPTOR_ALARME){
							((Activity) context).runOnUiThread(new Runnable() {
								  public void run() {
									  // Atualiza a mensagem na tela de sincronizacao
									  textMensagemRetorno.setText("Logado com sucesso.");
								  }
							});
						}
						// Muda o modo de conexao para ativa
						/*if (modoConexao.equalsIgnoreCase("A")){
							conexaoFtp.enterLocalActiveMode();
							conexaoFtp.enterRemoteActiveMode(InetAddress.getByName(hostFtp), FTP.DEFAULT_PORT);

						// Muda o modo de conexao para passiva
						} else */if (modoConexao.equalsIgnoreCase("P")){
								conexaoFtp.enterLocalPassiveMode();
								conexaoFtp.enterRemotePassiveMode();
						}

						//Setando para o modo de transfer�ncia de Arquivos
						conexaoFtp.setFileType(FTPClient.BINARY_FILE_TYPE);
						conexaoFtp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
						conexaoFtp.enterLocalPassiveMode();
						conexaoFtp.setAutodetectUTF8(true);

						// Vai para uma pasta especifica no servidor FTP
						if(!conexaoFtp.changeWorkingDirectory(nomeDiretorioFtp)){
							criarDiretorio(nomeDiretorioFtp);
						}
						if(telaChamou != TELA_RECEPTOR_ALARME){

							// Atualiza a mensagem na notificacao
							mLoad.bigTextStyle("Aquarde... Estamos solicitando os dados.").progress().build();

							((Activity) context).runOnUiThread(new Runnable() {
								public void run() {
									// Atualiza a mensagem na tela de sincronizacao
									textMensagemRetorno.setText("Aquarde... Estamos solicitando os dados.");
								}
							});

							File arquivoSolicitacao = new File(geraArquivoSolicitacaoDados(blocoReceber));
							// Instancia a variavel para salvar os arquivos a serem baixados
							listaDadosArquivoFtp = new ArrayList<FTPFile>();

							if (arquivoSolicitacao.exists()) {

								// Pega o Arquivo a ser enviado e transforma em um formato proprio para transferir
								FileInputStream arqEnviar = new FileInputStream(arquivoSolicitacao);
								// Incrementa o tamanho da transferencia do arquivo
								conexaoFtp.setBufferSize(1024000);

								String nomeTemp = funcoes.getValorXml("ChaveEmpresa") + "_solicitacao.txt";

								// Envia o arquivo de solicitacao
								if (conexaoFtp.storeFile(nomeTemp, arqEnviar)){

									// Renomeia o arquivo enviado para checar se chegou no servidor FTP com  sucesso
									if (renomeaArquivoFtp(nomeTemp, arquivoSolicitacao.getName())) {

										// Atualiza a mensagem na notificacao
										mLoad.bigTextStyle("Solicitação Enviada. 1ª Tentativa para receber os dados. Aguarde...").progress().build();

										((Activity) context).runOnUiThread(new Runnable() {
											public void run() {
												// Atualiza a mensagem na tela de sincronizacao
												textMensagemRetorno.setText("Solicitação Enviada. 1ª Tentativa para receber os dados. Aguarde...");
											}
										});
									}
								}
							}
							// Controle de repeticao
							int controle = 0;

							// Faz ess processo no máximo 3 vezes
							while ((listaDadosArquivoFtp.size() <= 0) && (controle < 3)) {

								controle++;

								if (controle == 3){
									// Atualiza a mensagem na notificacao
									mLoad.bigTextStyle("Solicitação Enviada. Última tentativa para receber os dados. Aguarde... ").progress().build();

									((Activity) context).runOnUiThread(new Runnable() {
										public void run() {
											// Exibe o tempo restante
											textMensagemRetorno.setText("Solicitação Enviada. Última tentativa para receber os dados. Aguarde... ");
										}
									});
								} else {
									// Atualiza a mensagem na notificacao
									mLoad.bigTextStyle("Solicitação Enviada. " + controle + "ª Tentativa para receber os dados. Aguarde... ").progress().build();

									final int finalControle = controle;
									((Activity) context).runOnUiThread(new Runnable() {
										public void run() {
											// Exibe o tempo restante
											textMensagemRetorno.setText("Solicitação Enviada. " + finalControle + "ª Tentativa para receber os dados. Aguarde... ");
										}
									});
								}
								// Pausa o clodigo por 10 segundos
								Thread.sleep(10000);

								// Pega uma lista com todos os arquivos a ser baixados
								listaDadosArquivoFtp = listaDadosArquivoReceber(blocoReceber, nomeLogin);

								/*// Inicia um cronometo regressivo de 20 segundos
								final int finalControle = controle;
								((Activity) context).runOnUiThread(new Runnable() {
									public void run() {
										new CountDownTimer(20000, 1000) {
											@Override
											public void onTick(final long millisUntilFinished) {

												if (finalControle == 3){
													// Exibe o tempo restante
													textMensagemRetorno.setText("Solicitação Enviada. Última tentativa para receber os dados. Aguarde... " + (millisUntilFinished / 1000));
												} else {
													// Exibe o tempo restante
													textMensagemRetorno.setText("Solicitação Enviada. " + finalControle + "ª Tentativa para receber os dados. Aguarde... " + (millisUntilFinished / 1000));
												}
											}
											@Override
											public void onFinish() {

											}
										}.start();
									}
								});
								// Pausa o clodigo por 23 segundos
								Thread.sleep(30000);*/
							} // Fim while
						} else {
							// Pega uma lista com todos os arquivos a ser baixados
							listaDadosArquivoFtp = listaDadosArquivoReceber(blocoReceber, nomeLogin);
						}

						// Pega uma lista com todos os arquivos a ser baixados
						//List<FTPFile> listaDadosArquivoFtp = listaDadosArquivoReceber(blocoReceber);

						// Checa se a lista nao esta vazia
						if ((listaDadosArquivoFtp != null) && (listaDadosArquivoFtp.size() > 0)) {

							// Passa por todos os arquivos da lista
							for (final FTPFile dadosArquivoFtp : listaDadosArquivoFtp) {

								// Checa se nao esta vazio
								if (dadosArquivoFtp != null) {

									// Atualiza a mensagem na notificacao
									mLoad.bigTextStyle("Achamos o arquivo " + dadosArquivoFtp.getName());
									// Transforma a barra de progresso em determinado
									mLoad.progress().value(0, (int) dadosArquivoFtp.getSize(), false).build();

									// Checa qual classe chamou esta
									if (telaChamou != TELA_RECEPTOR_ALARME) {

										((Activity) context).runOnUiThread(new Runnable() {
											public void run() {
												progressDownloads.setIndeterminate(false);
												progressDownloads.setProgress(0);
												// Atualiza a mensagem na tela de sincronizacao
												textMensagemRetorno.setText("Achamos o arquivo " + dadosArquivoFtp.getName());
											}
										});

										// Seta um tamanho maximo da barra de progresso
										progressDownloads.setMax((int) dadosArquivoFtp.getSize());
									}
									tamanhoArquivo = (double) dadosArquivoFtp.getSize();

									// Cria o nome do arquivo a ser baixado
									String nomeAquivo = dadosArquivoFtp.getName();

									// Faz o download do bloco no servidor ftp e pega o caminho
									localArquivoRecebido.add(downloadFtp(nomeAquivo, pastaTemp, nomeDiretorioFtp));

								} else {
									if (telaChamou != TELA_RECEPTOR_ALARME) {
									/*((Activity) context).runOnUiThread(new Runnable() {
										  public void run() {
											  // Atualiza a mensagem na tela de sincronizacao
											  textMensagemRetorno.setText("Não achamos o " + nomeDiretorioFtp + "_" + ImportarDadosTxtRotinas.BLOCOS[posicao] + ".txt");
										  }
									});*/
										progressDownloads.setIndeterminate(true);
									}
									mensagemErro = mensagemErro + "O arquivo " + dadosArquivoFtp.getName() + " esta vazio.";

								}
							}
						} else {
							// Atualiza a mensagem na notificacao
							//mLoad.bigTextStyle("Não achamos nenhum arquivo para fazer downloads.").simple().build();

							// Cria uma notificacao para ser manipulado
							mLoad = PugNotification.with(context)
									.load()
									.identifier(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO)
									.smallIcon(R.mipmap.ic_launcher)
									.largeIcon(R.drawable.ic_launcher)
									.title(R.string.importar_dados_recebidos)
									.bigTextStyle("Não achamos nenhum arquivo para fazer downloads.")
									.flags(Notification.DEFAULT_SOUND);

							mLoad.simple().build();

							if(telaChamou != TELA_RECEPTOR_ALARME){
								((Activity) context).runOnUiThread(new Runnable() {
									public void run() {
										// Atualiza a mensagem na tela de sincronizacao
										textMensagemRetorno.setText("Não achamos nenhum arquivo para fazer downloads.");
									}
								});
								progressDownloads.setIndeterminate(true);

							}
						}

					} else { // Fim if status
						mensagemErro += "Talvez o Usuário ou Senha esteja incorretos";
					}
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
							  public void run() {
								  progressDownloads.setVisibility(View.INVISIBLE);
							  }
						});
					}
					// Fecha conexao
					conexaoFtp.disconnect();
					conexaoFtp = null;

				} catch (IOException e) {
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
							  public void run() {
								  progressDownloads.setVisibility(View.INVISIBLE);
							  }
						});
					}
					//arquivoRecebido.delete();
					mensagemErro += "Não foi possível baixar nenhum arquivo do servidor em Nuvem. \n" + e.getMessage() + "\n";

				} catch (Exception e) {

					mensagemErro += "Erro ao baixar arquivo. \n" + e.getMessage();
				}
			} else {
				mensagemErro += context.getResources().getString(R.string.nao_existe_conexao_internet) + "\n";
			}
		} else {
			mensagemErro += context.getResources().getString(R.string.nao_existe_dados_do_servidor_ftp) + "\n";
		}
		
		// Exibe mensagem se existir
		if( (mensagemErro != null) && (mensagemErro.length() > 1) ){

			// Atualiza a mensagem na notificacao
			mLoad.bigTextStyle(mensagemErro).simple().build();

			if(telaChamou != TELA_RECEPTOR_ALARME){
				((Activity) context).runOnUiThread(new Runnable() {
					  public void run() {
						
						textMensagemRetorno.setText(mensagemErro);
						
					 	ContentValues dadosMensagem = new ContentValues();
							
				    	dadosMensagem.put("comando", 0);
						dadosMensagem.put("tela", "ReceberArquivoTxtServidorFtpRotinas");
						dadosMensagem.put("mensagem", mensagemErro);
						dadosMensagem.put("dados", mensagemErro);
						dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
						dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
						dadosMensagem.put("email", funcoes.getValorXml("Email"));
						
						funcoes.menssagem(dadosMensagem);
					  }
				});
			}
		}
		
		return localArquivoRecebido;
	} // Fim downloadArquivoTxtServidorFtp


	/**
	 * Faz o download do arquivo no servidor FTP.
	 *
	 * @param nomeAquivo
	 * @param pastaDestino - Pasta localizada no dispositivo
	 * @param diretorioFtp
	 * @return
	 */
	private String downloadFtp(final String nomeAquivo, File pastaDestino, String diretorioFtp){
		// Salvar o local que foi feito o downlods junto com o nome do arquivo
		String localArquivoRecebido = "";
		try{
			// Pega em tempo real o que esta acontecendo na transferencia
	        streamListener = new CopyStreamAdapter(){
	        	@Override
				public void bytesTransferred(long totalBytesTransferred, final int bytesTransferred, long streamSize) {
	        		
	        		final NumberFormat format = NumberFormat.getInstance();
					format.setMaximumFractionDigits(4);
					format.setMinimumFractionDigits(4);
					format.setMaximumIntegerDigits(3);
					format.setMinimumIntegerDigits(1);
	        		
					// Converte o total transferido em double
	        		double totalTransferido = (double) totalBytesTransferred;
	        		// Calcula em percentual do total transferido do arquivo XML
	        		final double percentual = ((totalTransferido / tamanhoArquivo) * 100);

					if (mLoad != null) {
						// Atualiza a mensagem na notificacao
						mLoad.bigTextStyle("Fazendo o Download do arquivo " + nomeAquivo + " ... " + format.format(percentual) + "% ");
						mLoad.progress().value((int)totalBytesTransferred, (int) tamanhoArquivo, false).build();
					}
					if(telaChamou != TELA_RECEPTOR_ALARME){
		        		((Activity) context).runOnUiThread(new Runnable() {
	    					  public void run() {
	    						  // Atualiza a mensagem na tela de sincronizacao
	    						  textMensagemRetorno.setText("Fazendo o Download do arquivo " + nomeAquivo + " ... " + format.format(percentual) + "% ");
	    					  }
		        		});
		        	// Atualiza a barra de progresso
	        		progressDownloads.setProgress((int)totalBytesTransferred);
	        		}
				}
				
				@Override
				public void bytesTransferred(CopyStreamEvent arg0) {
					String s = arg0.toString();
					int i = s.length();
				}
	        };
			//conexaoFtp.connect(hor);
	        // Associa um ouvite de transferencia de byte
	        conexaoFtp.setCopyStreamListener(streamListener);
	        // Incrementa o tamanho da transferencia do arquivo
	        conexaoFtp.setBufferSize(1024000);
	        
	        //Cria o outputStream para ser passado como parametro, onde o arquivo sera salvo  
	        FileOutputStream destinoFileStream = new FileOutputStream(pastaDestino + "/" + nomeAquivo);

			// Atualiza a mensagem na notificacao
			mLoad.bigTextStyle("Fazendo o Download... ").progress().build();

	        if(telaChamou != TELA_RECEPTOR_ALARME){
		        ((Activity) context).runOnUiThread(new Runnable() {
					  public void run() {
						  // Atualiza a mensagem na tela de sincronizacao
						  textMensagemRetorno.setText("Fazendo o Download... ");
					  }
		        });
	        }
	        
	        //Faz o download do arquivo  
	        boolean status = conexaoFtp.retrieveFile(nomeAquivo, destinoFileStream);
	        
	        // Cria uma variavel para pegar o local que foi salvo o download
	        File arquivoRecebido = null;
	        
	        // Checa se foi feito download com sucesso
	        if(status){
				// Atualiza a mensagem na notificacao
				mLoad.bigTextStyle("Download do arquivo " + nomeAquivo + " efetuado com sucesso... ").progress().build();

	        	if(telaChamou != TELA_RECEPTOR_ALARME){
		        	((Activity) context).runOnUiThread(new Runnable() {
						  public void run() {
							  // Atualiza a mensagem na tela de sincronizacao
							  textMensagemRetorno.setText("Download do arquivo " + nomeAquivo + " efetuado com sucesso... ");
						  }
		        	});
	        	}
	        	
	        	arquivoRecebido = new File(pastaDestino + "/" + nomeAquivo);
	        	
	        	// Checa se existe o arquivo baixado
	        	if(arquivoRecebido.exists()){
	        		// Pega o endereco do arquivo recebido para ser retornado
	        		localArquivoRecebido = arquivoRecebido.getPath();
	        		
	        		destinoFileStream.close();
	        		// Deleta o arquivo de dados do servidor FTP
	        		conexaoFtp.deleteFile(nomeAquivo);
	        		
	        		//conexaoFtp = null;
	        	} else {
	        		mensagemErro += "O arquivo " + nomeAquivo + " não esta na pasta " + pastaDestino + "\n";
	        	}
	        } else {
	        	mensagemErro += "Não foi possivel fazer o downloads do arquivo " + arquivoRecebido.getPath() + "\n";
	            
	        	destinoFileStream.close();
	    		// Fecha a conexao com o servidor FTP
	    		//conexaoFtp.disconnect();
	        }
		
		}catch(IOException e){
			mensagemErro += "Erro fatal ao fazer download do servidor em nuvem. \n" + e.getMessage();
		}catch(Exception e){
			mensagemErro += "Erro fatal ao fazer download do servidor em nuvem. \n" + e.getMessage();
		}
        return localArquivoRecebido;
	}
	
	
		/**
		 * 
		 * @param nomeDiretorio
		 * @return
		 */
		public boolean criarDiretorio(String nomeDiretorio) {
			try {
				boolean status = conexaoFtp.makeDirectory(nomeDiretorio);
				return status;
			} catch (Exception e) {
				ContentValues dadosMensagem = new ContentValues();

				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

				dadosMensagem.put("comando", 0);
				dadosMensagem.put("tela", "ReceberArquivoTxtServidorFtpRotinas");
				dadosMensagem.put("mensagem", "Não foi possível criar o diretorio " + nomeDiretorio + "\n" + e.getMessage());
				dadosMensagem.put("dados", e.toString());
				dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
				dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
				dadosMensagem.put("email", funcoes.getValorXml("Email"));

				funcoes.menssagem(dadosMensagem);
			}
			return false;
		} // Fim criarDiretorio
	
		
		/**
		 * Pega os dados do arquivo que esta no servidor FTP.
		 * Tados tais como tamanho, nome, exten��o, entre outros.
		 * 
		 * @param nomeArquivo
		 * @return
		 */
		public FTPFile dadosArquivoReceber(String nomeArquivo){
		     try {
		             FTPFile[] ftpFiles = conexaoFtp.listFiles();
		             // Passa por todos os registros
		             for (int i = 0; i < ftpFiles.length; i++) {
		                 // Checa se eh um arquivo
		            	 if(ftpFiles[i].isFile()){
							 // Checa se o arquivo tem a extencao .SAVARE
							 if (ftpFiles[i].getName().contains(EXTENCAO_DOWNLOADS_UNIVERSAL)){

							 }
		                	 // Checa se o arquivo eh o que interesa
		            		 if(ftpFiles[i].getName().equals(nomeArquivo)){
		            			 return ftpFiles[i];
		            		 }
		                 }
		             }
		      } catch(Exception e) {
		             e.printStackTrace();
		      }
		     return null;
		}

		public List<FTPFile> listaDadosArquivoReceber(String blocoReceber, String nomeLogin){
			List<FTPFile> arquivoParaReceber = new ArrayList<FTPFile>();
			//FTPFile[] arquivoParaReceber = null;
			try {
				FTPFile[] ftpFiles = conexaoFtp.listFiles();
				// Passa por todos os registros
				for (int i = 0; i < ftpFiles.length; i++) {
					// Checa se eh um arquivo
					if((ftpFiles[i].isFile()) && (ftpFiles[i].getName().contains(nomeLogin))){

                        if (blocoReceber == null) {
                            // Checa se o arquivo tem a extencao .SAVARE
                            if (ftpFiles[i].getName().contains(EXTENCAO_DOWNLOADS_UNIVERSAL)) {
                                // Adiciona o arquivo com a extencao em uma lista
                                arquivoParaReceber.add(ftpFiles[i]);
                            }
                            // Checa se eh para baixar o bloco S (dados da empresa)
                        } else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_S)){
                            // Checa se o arquivo tem a extencao .SAVARE
                            if (ftpFiles[i].getName().contains(EXTENCAO_DOWNLOADS_BLOCO_S)) {
                                // Adiciona o arquivo com a extencao em uma lista
                                arquivoParaReceber.add(ftpFiles[i]);
                            }
                            // Checa se eh para baixar o bloco A (dados dos produtos)
                        } else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_A)){
                            // Checa se o arquivo tem a extencao .SAVARE
                            if (ftpFiles[i].getName().contains(EXTENCAO_DOWNLOADS_BLOCO_A)) {
                                // Adiciona o arquivo com a extencao em uma lista
                                arquivoParaReceber.add(ftpFiles[i]);
                            }
                            // Checa se eh para baixar o bloco c (dados dos clientes)
                        } else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_C)){
                            // Checa se o arquivo tem a extencao .SAVARE
                            if (ftpFiles[i].getName().contains(EXTENCAO_DOWNLOADS_BLOCO_C)) {
                                // Adiciona o arquivo com a extencao em uma lista
                                arquivoParaReceber.add(ftpFiles[i]);
                            }
                            // Checa se eh para baixar o bloco R (dados de contas receber)
                        } else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_R)){
                            // Checa se o arquivo tem a extencao .SAVARE
                            if (ftpFiles[i].getName().contains(EXTENCAO_DOWNLOADS_BLOCO_R)) {
                                // Adiciona o arquivo com a extencao em uma lista
                                arquivoParaReceber.add(ftpFiles[i]);
                            }
                        }
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			return arquivoParaReceber;
	} // Fim listaDadosArquivoReceber


	public String geraArquivoSolicitacaoDados(String blocos){
		// Instancia formato de classe
		DateFormat dataFormatada = new SimpleDateFormat("ddMMyyyy_HHmmss_SSSS");
		// Instancia com a data atual
		Date data = new Date();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        // Pega a chave do usuario
        String chave = funcoes.getValorXml("ChaveEmpresa");

        // Local e nome do arquivo a ser salvo
        String localArquivo = Environment.getExternalStorageDirectory() + "/SAVARE/TEMP/SO_" + chave + ".SAVARE";

		try {
			// Cria o arquivo txt
			FileWriter arquivoTxt = new FileWriter(localArquivo);

			// Variavel para manipular o arquivo
			PrintWriter gravarArquivo = new PrintWriter(arquivoTxt);

			// Checa se a opcao de bloco esta nula (pega todos os blocos)
			if (blocos == null){
				// Grava texto |BLOCO|FINALIDADE|DISPOSITIVO(CHAVE)|DATA_ULTIMO_RECEBIMENTO|
				gravarArquivo.print("|S|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");
				gravarArquivo.print("|C|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");
				gravarArquivo.print("|A|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");
				gravarArquivo.print("|R|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|");

			// Pega o bloco de dados da empresa
			} else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_S)){
				// Grava texto |BLOCO|FINALIDADE|DISPOSITIVO(CHAVE)|DATA_ULTIMO_RECEBIMENTO|
				gravarArquivo.print("|S|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");

			// Pega o bloco de dados de clientes e associados
			} else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_C)){
				// Grava texto
				gravarArquivo.print("|C|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");

			// Pega o bloco de dados de produtos
			} else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_A)){
				// Grava texto
				gravarArquivo.print("|A|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");

			// Pega o bloco de dados de titulos
			} else if (blocoReceber.equalsIgnoreCase(ImportarDadosTxtRotinas.BLOCO_R)){
				// Grava texto
				gravarArquivo.print("|R|S|" + chave + "|" + funcoes.getValorXml("DataUltimoRecebimento") + "|\n");
			}
			// Fecha o arquivo
			arquivoTxt.close();

            // Checa se o arquivo existe
            if (!(new File(localArquivo).exists())){
                localArquivo = "";
            }
		} catch (IOException e) {
			Log.e("SAVARE", "*ReceberArquivoTxtServidorFtpRotinas" + e.toString());
		}
        return localArquivo;
	} // Fim geraArquivoSolicitacaoDados

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
                    dadosMensagem.put("tela", "ReceberArquivoTxtServidorFtpRotinas");
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
}

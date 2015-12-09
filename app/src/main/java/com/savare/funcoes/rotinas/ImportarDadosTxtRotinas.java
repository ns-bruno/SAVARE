package com.savare.funcoes.rotinas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.banco.funcoesSql.AreasSql;
import com.savare.banco.funcoesSql.CartaoSql;
import com.savare.banco.funcoesSql.CidadeSql;
import com.savare.banco.funcoesSql.ClasseSql;
import com.savare.banco.funcoesSql.CobrancaSql;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.EnderecoSql;
import com.savare.banco.funcoesSql.EstadoSql;
import com.savare.banco.funcoesSql.EstoqueSql;
import com.savare.banco.funcoesSql.GradeSql;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.LocacaoSql;
import com.savare.banco.funcoesSql.MarcaSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.ParametrosSql;
import com.savare.banco.funcoesSql.ParcelaSql;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.PlanoPagamentoSql;
import com.savare.banco.funcoesSql.PortadorBancoSql;
import com.savare.banco.funcoesSql.ProdutoLojaSql;
import com.savare.banco.funcoesSql.ProdutoSql;
import com.savare.banco.funcoesSql.ProfissaoSql;
import com.savare.banco.funcoesSql.RamoAtividadeSql;
import com.savare.banco.funcoesSql.SituacaoTributariaSql;
import com.savare.banco.funcoesSql.StatusSql;
import com.savare.banco.funcoesSql.TipoClienteSql;
import com.savare.banco.funcoesSql.TipoDocumentoSql;
import com.savare.banco.funcoesSql.UnidadeVendaSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.funcoes.FuncoesPersonalizadas;

public class ImportarDadosTxtRotinas {
	
	private Context context;
	private String localDados = "",
				   mensagem = " ";	
	private int telaChamou = -1;
	public static final String BLOCO_S = "BLOCO_S",
							   BLOCO_C = "BLOCO_C",
							   BLOCO_A = "BLOCO_A",
							   BLOCO_R = "BLOCO_R",
							   BLOCO_COMPLETO = "BLOCO_COMPLETO";
	public static final String[] BLOCOS = {BLOCO_S, BLOCO_C, BLOCO_A, BLOCO_R, BLOCO_COMPLETO};
	public static final String BLOCO_S100_SMAEMPRE = "S100",
							   BLOCO_C200_CFAAREAS = "C200",
							   BLOCO_C201_CFAATIVI = "C201",
							   BLOCO_C202_CFASTATU = "C202",
							   BLOCO_C203_CFATPDOC = "C203",
							   BLOCO_C204_CFACCRED = "C204",
							   BLOCO_C205_CFAPORTA = "C205",
							   BLOCO_C206_CFAPROFI = "C206",
							   BLOCO_C207_CFATPCLI = "C207",
							   BLOCO_C208_CFATPCOB = "C208",
							   BLOCO_C209_CFAESTAD = "C209",
							   BLOCO_C210_CFACIDAD = "C210",
							   BLOCO_C211_CFACLIFO = "C211",
							   BLOCO_C212_CFAENDER = "C212",
							   BLOCO_C213_CFAPARAM = "C213",
							   BLOCO_A300_AEAPLPGT = "A300",
							   BLOCO_A301_AEACLASE = "A301",
							   BLOCO_A302_AEAUNVEN = "A302",
							   BLOCO_A303_AEAGRADE = "A303",
							   BLOCO_A304_AEAMARCA = "A304",
							   BLOCO_A305_AEACODST = "A305",
							   BLOCO_A306_AEAPRODU = "A306",
							   BLOCO_A307_AEAEMBAL = "A307",
							   BLOCO_A308_AEAPLOJA = "A308",
							   BLOCO_A309_AEALOCES = "A309",
							   BLOCO_A310_AEAESTOQ = "A310",
							   BLOCO_A311_AEAORCAM = "A311",
							   BLOCO_A312_AEAITORC = "A312",
							   BLOCO_R400_RPAPARCE = "R400";
	public static final String LAYOUT = "001";
	private boolean layoutValido = false;
	private ProgressBar progressRecebimentoDados;
	private TextView textMensagemProcesso;
	public static final int TELA_RECEPTOR_ALARME = 0;

	public ImportarDadosTxtRotinas(Context context, String localDados) {
		this.context = context;
		this.localDados = localDados;
	}
	
	public ImportarDadosTxtRotinas(Context context, String localDados, int telaChamou) {
		this.context = context;
		this.localDados = localDados;
		this.telaChamou = telaChamou;
	}
	

	public ImportarDadosTxtRotinas(Context context, String localDados, ProgressBar progressBar, TextView textMensagem) {
		this.context = context;
		this.localDados = localDados;
		this.progressRecebimentoDados = progressBar;
		this.textMensagemProcesso = textMensagem;
		
		((Activity) context).runOnUiThread(new Runnable() {
			  public void run() {
				progressRecebimentoDados.setVisibility(View.VISIBLE);
				progressRecebimentoDados.setIndeterminate(true);
				textMensagemProcesso.setText("Validando os dados...");
			  }
		});
	}
	
	public void importarDados(){
		Log.i("SAVARE", "Executando a rotina importarDados - ImportarDadosTxtRotinas");

		/*((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				//progressRecebimentoDados.setVisibility(View.VISIBLE);
				progressRecebimentoDados.setIndeterminate(true);
			}
		});*/

		//final Calendar calendario = Calendar.getInstance();
		
		//long tempoInicial = calendario.getTimeInMillis();
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		try {
			// Pego o arquivo txt e insiro um delimitador
			Scanner scannerDados = new Scanner(new FileReader(localDados)).useDelimiter("\\||\\n");
			
			int totalLinha = 0;
			
			//Indicamos o arquivo que sera lido
	        FileReader fileReader = new FileReader(localDados);
	 
	        //Criamos o objeto bufferReader que nos oferece o metodo de leitura readLine()
	        BufferedReader bufferedReader = new BufferedReader(fileReader);
	 
	        //Fazemos um loop linha a linha no arquivo, enquanto ele seja diferente de null.
	        //O metodo readLine() devolve a linha na posicao do loop para a variavel linha.
	        while ( (bufferedReader.readLine() ) != null) {
	            //Aqui imprimimos a linha
	            //System.out.println(linha);
	        	totalLinha++;
	        }
	        //liberamos o fluxo dos objetos ou fechamos o arquivo
	        fileReader.close();
	        bufferedReader.close();

			final int finalTotalLinha = totalLinha;

			if (progressRecebimentoDados != null) {

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {

						// Torna a barra de progresso finita
						progressRecebimentoDados.setIndeterminate(false);
						// Zera a contagem do progresso
						progressRecebimentoDados.setProgress(0);
						// Inseri o total que a barra de progresso pode ir
						progressRecebimentoDados.setMax(finalTotalLinha);
					}
				});
			}
			int incremento = 0;
			
			// Passa por todas as linha do arquivo txt
			while(scannerDados.hasNextLine()){

				Log.i("SAVARE", "Escaneando as linhas(sccanerDados) - " + incremento + " - ImportarDadosTxtRotinas");

				final int finalIncremento = incremento;

				if (progressRecebimentoDados != null) {

					//((Activity) context).runOnUiThread(new Runnable() {
					//	public void run() {
							// Incrementa o progresso
							progressRecebimentoDados.setProgress(finalIncremento);
					///	}
					//});
				}
				// Incrementa o numero de linha scaneadas
				incremento ++;
				
				// Pega a posicao da linha atual
				final int posicaoLinhaAtual = incremento;
				final int totalLinhaRegistro = totalLinha;
				
				// Pega apenas uma linha
				Scanner scannerLinha = new Scanner(scannerDados.nextLine()).useDelimiter("\\|");
				
				// Pega o primeiro token da linha
				String registro = scannerLinha.next();
				
				// Checa se o registro pertence ao bloco 0000
				if(registro.equalsIgnoreCase("0000")){
					// Pega a linha completa
					String linha = scannerLinha.nextLine();
					
					// Checa se o layou esta valido
					layoutValido = checaLayout(linha);
					
					if(layoutValido){

						if(telaChamou != TELA_RECEPTOR_ALARME){
							((Activity) context).runOnUiThread(new Runnable() {
		    					  public void run() {
		    						  // Atualiza o texto da tela de sincronizacao
		    						  textMensagemProcesso.setText("Layout Validado com sucesso.");
		    					  }
							});
						}
					}else{
						if(telaChamou != TELA_RECEPTOR_ALARME){
							((Activity) context).runOnUiThread(new Runnable() {
		    					  public void run() {
		    						  // Atualiza o texto da tela de sincronizacao
		    						  textMensagemProcesso.setText("Layout Inválido.");
		    					  }
							});
						}
						mensagem += "Layout Inválido";
					}
					
				} else if(registro.equalsIgnoreCase(BLOCO_S100_SMAEMPRE) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
	  					  public void run() {
	  						  // Atualiza o texto da tela de sincronizacao
	  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_S100_SMAEMPRE + linha);
	  					  }
						});
					}
					
					importarRegistroEmpresa(linha);

				} else if(registro.equalsIgnoreCase(BLOCO_C200_CFAAREAS) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C200_CFAAREAS + linha);
		  					  }
						});
					}
					
					importarRegistroAreas(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C201_CFAATIVI) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C201_CFAATIVI + linha);
		  					  }
						});
					}
					
					importarRegistroAtividade(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C202_CFASTATU) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" mportando o bloco " + BLOCO_C202_CFASTATU + linha);
		  					  }
						});
					}
					
					importarRegistroStatus(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C203_CFATPDOC) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C203_CFATPDOC + linha);
		  					  }
						});
					}
					
					importarRegistroTipoDocumento(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C204_CFACCRED) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C204_CFACCRED + linha);
		  					  }
						});
					}
					
					importarRegistroCartaoCredito(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C205_CFAPORTA) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C205_CFAPORTA + linha);
		  					  }
						});
					}
					
					importarRegistroPortador(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C206_CFAPROFI) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C206_CFAPROFI + linha);
		  					  }
						});
					}
					
					importarRegistroProfissao(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C207_CFATPCLI) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C207_CFATPCLI + linha);
		  					  }
						});
					}
					
					importarRegistroTipoCliente(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C208_CFATPCOB) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C208_CFATPCOB + linha);
		  					  }
						});
					}
					
					importarRegistroTipoCobranca(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C209_CFAESTAD) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C209_CFAESTAD + linha);
		  					  }
						});
					}
					
					importarRegistroEstado(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C210_CFACIDAD) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C210_CFACIDAD + linha);
		  					  }
						});
					}
					
					importarRegistroCidade(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C211_CFACLIFO) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C211_CFACLIFO + linha);
		  					  }
						});
					}
					
					importarRegistroPessoa(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_C212_CFAENDER) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C212_CFAENDER + linha);
		  					  }
						});
					}
					
					importarRegistroEndereco(linha);
				
				} else if(registro.equalsIgnoreCase(BLOCO_C213_CFAPARAM) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_C213_CFAPARAM + linha);
		  					  }
						});
					}
					
					importarRegistroParametro(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A300_AEAPLPGT) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A300_AEAPLPGT + linha);
		  					  }
						});
					}
					
					importarRegistroPlanoPgto(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A301_AEACLASE) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A301_AEACLASE + linha);
		  					  }
						});
					}
					
					importarRegistroClasse(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A302_AEAUNVEN) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A302_AEAUNVEN + linha);
		  					  }
						});
					}
					
					importarRegistroUnidadeVenda(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A303_AEAGRADE) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A303_AEAGRADE + linha);
		  					  }
						});
					}
					
					importarRegistroGrade(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A304_AEAMARCA) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A304_AEAMARCA + linha);
		  					  }
						});
					}
					
					importarRegistroMarca(linha);
				
				} else if(registro.equalsIgnoreCase(BLOCO_A305_AEACODST) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A305_AEACODST + linha);
		  					  }
						});
					}
					
					importarRegistroSituacaoTributaria(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A306_AEAPRODU) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A306_AEAPRODU + linha);
		  					  }
						});
					}
					
					importarRegistroProduto(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A307_AEAEMBAL) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A307_AEAEMBAL + linha);
		  					  }
						});
					}
					
					importarRegistroEmbalagem(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A308_AEAPLOJA) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A308_AEAPLOJA + linha);
		  					  }
						});
					}
					
					importarRegistroPLoja(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A309_AEALOCES) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A309_AEALOCES + linha);
		  					  }
						});
					}
					
					importarRegistroLocacao(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A310_AEAESTOQ) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A310_AEAESTOQ + linha);
		  					  }
						});
					}
					
					importarRegistroEstoque(linha);
					
				} else if(registro.equalsIgnoreCase(BLOCO_A311_AEAORCAM) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
		  					  public void run() {
		  						  // Atualiza o texto da tela de sincronizacao
		  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_A311_AEAORCAM + linha);
		  					  }
						});
					}

					importarRegistroOrcamento(linha);

				} else if(registro.equalsIgnoreCase(BLOCO_A312_AEAITORC) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
						((Activity) context).runOnUiThread(new Runnable() {
							public void run() {
								// Atualiza o texto da tela de sincronizacao
								textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_R400_RPAPARCE + linha);
							}
						});
					}
					importarRegistroItemOrcamento(linha);

				} else if(registro.equalsIgnoreCase(BLOCO_R400_RPAPARCE) && layoutValido){
					// Pega a linha completa
					final String linha = scannerLinha.nextLine();

					Log.i("SAVARE", linha + " - ImportarDadosTxtRotinas");
					if(telaChamou != TELA_RECEPTOR_ALARME){
					((Activity) context).runOnUiThread(new Runnable() {
	  					  public void run() {
	  						  // Atualiza o texto da tela de sincronizacao
	  						  textMensagemProcesso.setText(posicaoLinhaAtual + " de " + totalLinhaRegistro +" Importando o bloco " + BLOCO_R400_RPAPARCE + linha);
	  					  }
					});
					}
					importarRegistroParcela(linha);
				}

			} // Fim while
			// Pega o tempo atual em milesegundos
			//long tempoFinal = System.currentTimeMillis();

			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);

			//final int tempoCorridoMin = Integer.parseInt(funcoes.diferencaEntreDataHora(FuncoesPersonalizadas.MINUTOS, sdf.format(tempoInicial), sdf.format(tempoFinal)));

			//final int tempoCorridoSeg = (Integer.parseInt(funcoes.diferencaEntreDataHora(FuncoesPersonalizadas.SEGUNDOS, sdf.format(tempoInicial), sdf.format(tempoFinal)))) - (60 * tempoCorridoMin);


			// Fecha os dados do arquivo
			scannerDados.close();
			
			if(telaChamou != TELA_RECEPTOR_ALARME){
				((Activity) context).runOnUiThread(new Runnable() {
					  public void run() {
						  // Coloca a barra de progresso em modo invisivel e sem ocupar o seu proprio espaco
						  progressRecebimentoDados.setVisibility(View.INVISIBLE);
					  }
				});
			}
			
			if(incremento == totalLinha){
				
				if(telaChamou != TELA_RECEPTOR_ALARME){
					((Activity) context).runOnUiThread(new Runnable() {
						  public void run() {
							  // Atualiza o texto da tela de sincronizacao
							  textMensagemProcesso.setText("Recebemos todos os registros");
							  //textMensagemProcesso.setText("Recebemos todos os registros em " + tempoCorridoMin + " Min. e " + tempoCorridoSeg + " Seg.");
						  }
					});
				}
				mensagem += "Recebemos todos os registros.(" + totalLinha + ") \n";
			} else {
				final int totalLinha2 = totalLinha;
				final int incremento2 = incremento;
				
				if(telaChamou != TELA_RECEPTOR_ALARME){
					((Activity) context).runOnUiThread(new Runnable() {
						  public void run() {
							  // Atualiza o texto da tela de sincronizacao
							  textMensagemProcesso.setText("Não foi recebido todos os registros. Diferença de " + (totalLinha2 - incremento2));
									  					   //"\n Recebemos em " + tempoCorridoMin + " Min. e " + tempoCorridoSeg + " Seg.");
						  }
					});
				}
				mensagem += "Não foi recebido todos os registros. \n Diferença de " + (totalLinha2 - incremento2); // + "\n Recebemos em " + tempoCorridoMin + " Min. e " + tempoCorridoSeg + " Seg.";
			}
			// Marca a aplicacao que nao esta mais recebendo dados
			funcoes.setValorXml("RecebendoDados", "N");
			
		} catch (final Exception e) {
			Log.e("SAVARE", "erro, Não foi possível escanear os dados do arquivo. " + e.getMessage() + " - ImportarDadosTxtRotinas");

			funcoes.setValorXml("RecebendoDados", "N");

			mensagem += "Não foi possível escanear os dados do arquivo. \n" + e.getMessage();
			
			if(telaChamou != TELA_RECEPTOR_ALARME){
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						progressRecebimentoDados.setVisibility(View.INVISIBLE);
						textMensagemProcesso.setText(mensagem + "\n" + e.getMessage());
					}
				});

				ContentValues dadosMensagem = new ContentValues();
				
		    	dadosMensagem.put("comando", 0);
				dadosMensagem.put("tela", "ReceberDadosFtpAsyncRotinas");
				dadosMensagem.put("mensagem", "Não foi possível escanear os dados do arquivo. \n" + e.getMessage());
				dadosMensagem.put("dados", e.toString());
				dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
				dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
				dadosMensagem.put("email", funcoes.getValorXml("Email"));
				 
				funcoes.menssagem(dadosMensagem);
			}

		} /*catch (final FileNotFoundException e) {
			
			mensagem += "Não foi possível escanear os dados do arquivo. \n" + e.getMessage() + "\n";
			
			if(telaChamou != TELA_RECEPTOR_ALARME){

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {

						progressRecebimentoDados.setVisibility(View.INVISIBLE);
						textMensagemProcesso.setText(mensagem + "\n" + e.getMessage());

						ContentValues dadosMensagem = new ContentValues();

						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

						dadosMensagem.put("comando", 0);
						dadosMensagem.put("tela", "ReceberDadosFtpAsyncRotinas");
						dadosMensagem.put("mensagem", mensagem);
						dadosMensagem.put("dados", e.toString());
						dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
						dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
						dadosMensagem.put("email", funcoes.getValorXml("Email"));

						funcoes.menssagem(dadosMensagem);
					}
				});
			}
		}*/
		// Checa se que esta chamando esta classe eh o alarme
		if( (mensagem != null) && (mensagem.length() > 0) && (telaChamou == TELA_RECEPTOR_ALARME)){
			// Cria a intent com identificacao do alarme
			Intent intent = new Intent("NOTIFICACAO_SAVARE");
			intent.putExtra("TICKER", "Importação dos Dados");
			intent.putExtra("TITULO", "SAVARE");
			intent.putExtra("MENSAGEM", mensagem);
			
			context.sendBroadcast(intent);
		}
	} // Fim importarDados
	
	private boolean checaLayout(String linha){
		boolean valido = false;
		
		Scanner scannerLayout = new Scanner(linha).useDelimiter("\\|");
		
		if(scannerLayout.next().equalsIgnoreCase(LAYOUT)){
			valido = true;
		}
		
		return valido;
	}

	
	private void importarRegistroEmpresa(String linha){
		
		Scanner scannerEmpresa = new Scanner(linha).useDelimiter("\\|");
		
		final String FINALIDADE = scannerEmpresa.next();
		final String idEmpre = scannerEmpresa.next();
		String dtAlt = scannerEmpresa.next();
		String nomeRazao = scannerEmpresa.next();
		String nomeFantasia = scannerEmpresa.next();
		String cpfCgc = scannerEmpresa.next();
		String orcSemEstoque = scannerEmpresa.next();
		String diasAtrazo = scannerEmpresa.next();
		String semMovimento = scannerEmpresa.next();
		String jurosDiario = scannerEmpresa.next();
		String vendeBloqueadoOrc = scannerEmpresa.next();
		String vendeBloqueadoPed = scannerEmpresa.next();
		String validadeFicha = scannerEmpresa.next();
		String vlMinPrazoVarejo = scannerEmpresa.next();
		String vlMinPrazoAtacado = scannerEmpresa.next();
		String vlMinVistaVarejo = scannerEmpresa.next();
		String vlMinVistaAtacado = scannerEmpresa.next();
		String multiplosPlanos = scannerEmpresa.next();
		String diaDestacaProduto = scannerEmpresa.next();
		String fechaVendaCredNegAtacado = scannerEmpresa.next();
		String fechaVendaCredNegVarejo = scannerEmpresa.next();
		String tipoAcumuloCreditoAtacado = scannerEmpresa.next();
		String tipoAcumuloCreditoVarejo = scannerEmpresa.next();
		String periodoCreditoAtacado = scannerEmpresa.next();
		String periodoCreditoVarejo = scannerEmpresa.next();
		
		scannerEmpresa = null;
	
		// Cria variavel para salvar os dados da empresa e enviar para o banco de dados
		final ContentValues dadosEmpresa = new ContentValues();
		
		// Inseri os valores
		dadosEmpresa.put("ID_SMAEMPRE", idEmpre);
		dadosEmpresa.put("DT_ALT", dtAlt);
		dadosEmpresa.put("NOME_RAZAO", nomeRazao);
		dadosEmpresa.put("NOME_FANTASIA", nomeFantasia);
		dadosEmpresa.put("CPF_CGC", cpfCgc);
		dadosEmpresa.put("ORC_SEM_ESTOQUE", orcSemEstoque);
		dadosEmpresa.put("DIAS_ATRAZO", diasAtrazo);
		dadosEmpresa.put("SEM_MOVIMENTO", semMovimento);
		dadosEmpresa.put("JUROS_DIARIO", jurosDiario);
		dadosEmpresa.put("VENDE_BLOQUEADO_ORC", vendeBloqueadoOrc);
		dadosEmpresa.put("VENDE_BLOQUEADO_PED", vendeBloqueadoPed);
		dadosEmpresa.put("VALIDADE_FICHA_CLIENTE", validadeFicha);
		dadosEmpresa.put("VL_MIN_PRAZO_VAREJO", vlMinPrazoVarejo);
		dadosEmpresa.put("VL_MIN_PRAZO_ATACADO", vlMinPrazoAtacado);
		dadosEmpresa.put("VL_MIN_VISTA_VAREJO", vlMinVistaVarejo);
		dadosEmpresa.put("VL_MIN_VISTA_ATACADO", vlMinVistaAtacado);
		dadosEmpresa.put("MULTIPLOS_PLANOS", multiplosPlanos);
		dadosEmpresa.put("QTD_DIAS_DESTACA_PRODUTO", diaDestacaProduto);
		dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_ATACADO", fechaVendaCredNegAtacado);
		dadosEmpresa.put("FECHA_VENDA_CREDITO_NEGATIVO_VAREJO", fechaVendaCredNegVarejo);
		dadosEmpresa.put("TIPO_ACUMULO_CREDITO_ATACADO", tipoAcumuloCreditoAtacado);
		dadosEmpresa.put("TIPO_ACUMULO_CREDITO_VAREJO", tipoAcumuloCreditoVarejo);
		dadosEmpresa.put("PERIODO_CREDITO_ATACADO", periodoCreditoAtacado);
		dadosEmpresa.put("PERIODO_CREDITO_VAREJO", periodoCreditoVarejo);
	
		final EmpresaSql empresaSql = new EmpresaSql(context);

		// Pega o sql para passar para o statement
		final String sql = empresaSql.construirSqlStatement(dadosEmpresa);
		// Pega o argumento para o statement
		final String[] argumentoSql = empresaSql.argumentoStatement(dadosEmpresa);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//empresaSql.insertOrReplace(dadosEmpresa);
						empresaSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			}else {
				empresaSql.insertOrReplaceFast(sql, argumentoSql);
			}

			// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						empresaSql.update(dadosEmpresa, "ID_SMAEMPRE = " + idEmpre);
					}
				});
			}else {
				empresaSql.update(dadosEmpresa, "ID_SMAEMPRE = " + idEmpre);
			}

			// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						empresaSql.delete("ID_SMAEMPRE = " + idEmpre);
					}
				});
			}else {
				empresaSql.delete("ID_SMAEMPRE = " + idEmpre);
			}
		}
		
		//dadosEmpresa.clear();
	} // Fim importarRegistroEmpresa
	
	private void importarRegistroPessoa(String linha){
		Scanner scannerPessoa = new Scanner(linha).useDelimiter("\\|");
		
		final String FINALIDADE = scannerPessoa.next();
		final String idClifo = scannerPessoa.next();
		String idProfi = scannerPessoa.next();
		String idAtivi = scannerPessoa.next();
		String idAreas = scannerPessoa.next();
		String idTpCli = scannerPessoa.next();
		String idStatu = scannerPessoa.next();
		String idCCred = scannerPessoa.next();
		String idEmpre = scannerPessoa.next();
		String dtAlt = scannerPessoa.next();
		String cpfCnpj = scannerPessoa.next();
		String ieRg = scannerPessoa.next();
		String nomeRazao = scannerPessoa.next();
		String nomeFantasia = scannerPessoa.next();
		String dtNascimento = scannerPessoa.next();
		String codigoCli = scannerPessoa.next();
		String codigoFun = scannerPessoa.next();
		String codigoUsu = scannerPessoa.next();
		String codigoTra = scannerPessoa.next();
		String cliente = scannerPessoa.next();
		String funcionario = scannerPessoa.next();
		String usuario = scannerPessoa.next();
		String transportadora = scannerPessoa.next();
		String sexo = scannerPessoa.next();
		String inscJunta = scannerPessoa.next();
		String inscSuframa = scannerPessoa.next();
		String inscMunicipal = scannerPessoa.next();
		String inscProdutor = scannerPessoa.next();
		String rendaMesGiro = scannerPessoa.next();
		String capitalSocial = scannerPessoa.next();
		String estoqueMercadorias = scannerPessoa.next();
		String estoqueMateriaPrima = scannerPessoa.next();
		String movtoVenda = scannerPessoa.next();
		String despesas = scannerPessoa.next();
		String empresaTrabalha = scannerPessoa.next();
		String obs = scannerPessoa.next();
		String pessoa = scannerPessoa.next();
		String civil = scannerPessoa.next();
		String conjuge = scannerPessoa.next();
		String cpfConjuge = scannerPessoa.next();
		String dtNascimentoConjuge = scannerPessoa.next();
		String qtdeFuncionarios = scannerPessoa.next();
		String outrasRendas = scannerPessoa.next();
		String numeroDependenteMaior = scannerPessoa.next();
		String numeroDependenteMenor = scannerPessoa.next();
		String complementoCargoConjuge = scannerPessoa.next();
		String rgConjuge = scannerPessoa.next();
		String orgaoEmissorConjuge = scannerPessoa.next();
		String limiteConjuge = scannerPessoa.next();
		String empresaConjuge = scannerPessoa.next();
		String admissaoConjuge = scannerPessoa.next();
		String rendaConjuge = scannerPessoa.next();
		String ativo = scannerPessoa.next();
		String enviarExtrato = scannerPessoa.next();
		String tipoExtrato = scannerPessoa.next();
		String conjugePodeComprar = scannerPessoa.next();
		String dtUltimaCompra = scannerPessoa.next();
		String dtRenovacao = scannerPessoa.next();
		
		// Elimina a memoria da variavel
		scannerPessoa = null;
		
		// Cria variavel para salvar os dados da empresa e enviar para o banco de dados
		final ContentValues dadosPessoa = new ContentValues();
		
		// Inseri os valores
		dadosPessoa.put("ID_CFACLIFO", idClifo);
		dadosPessoa.put("ID_CFAPROFI", idProfi);
		dadosPessoa.put("ID_CFAATIVI", idAtivi);
		dadosPessoa.put("ID_CFAAREAS", idAreas);
		dadosPessoa.put("ID_CFATPCLI", idTpCli);
		dadosPessoa.put("ID_CFASTATU", idStatu);
		dadosPessoa.put("ID_CFACCRED", idCCred);
		dadosPessoa.put("ID_SMAEMPRE", idEmpre);
		dadosPessoa.put("DT_ALT", dtAlt);
		dadosPessoa.put("CPF_CNPJ", cpfCnpj);
		dadosPessoa.put("IE_RG", ieRg);
		dadosPessoa.put("NOME_RAZAO", nomeRazao.replace("'", " "));
		dadosPessoa.put("NOME_FANTASIA", nomeFantasia.replace(",", " "));
		dadosPessoa.put("DT_NASCIMENTO", dtNascimento.replace("0000-00-00", ""));
		dadosPessoa.put("CODIGO_CLI", codigoCli);
		dadosPessoa.put("CODIGO_FUN", codigoFun);
		dadosPessoa.put("CODIGO_USU", codigoUsu);
		dadosPessoa.put("CODIGO_TRA", codigoTra);
		dadosPessoa.put("CLIENTE", cliente);
		dadosPessoa.put("FUNCIONARIO", funcionario);
		dadosPessoa.put("USUARIO", usuario);
		dadosPessoa.put("TRANSPORTADORA", transportadora);
		dadosPessoa.put("SEXO", sexo);
		dadosPessoa.put("INSC_JUNTA", inscJunta);
		dadosPessoa.put("INSC_SUFRAMA", inscSuframa);
		dadosPessoa.put("INSC_MUNICIPAL", inscMunicipal);
		dadosPessoa.put("INSC_PRODUTOR", inscProdutor);
		dadosPessoa.put("RENDA_MES_GIRO", rendaMesGiro);
		dadosPessoa.put("CAPITAL_SOCIAL", capitalSocial);
		dadosPessoa.put("EST_MERCADORIAS", estoqueMercadorias);
		dadosPessoa.put("EST_MAT_PRIMA", estoqueMateriaPrima);
		dadosPessoa.put("MOVTO_VENDAS", movtoVenda);
		dadosPessoa.put("DESPESAS", despesas);
		dadosPessoa.put("EMPRESA_TRAB", empresaTrabalha);
		dadosPessoa.put("OBS", obs);
		dadosPessoa.put("PESSOA", pessoa);
		dadosPessoa.put("CIVIL", civil);
		dadosPessoa.put("CONJUGE", conjuge);
		dadosPessoa.put("CPF_CONJUGE", cpfConjuge);
		dadosPessoa.put("DT_NASC_CONJ", dtNascimentoConjuge.replace("0000-00-00", ""));
		dadosPessoa.put("QTDE_FUNCIONARIOS", qtdeFuncionarios);
		dadosPessoa.put("OUTRAS_RENDAS", outrasRendas);
		dadosPessoa.put("NUM_DEP_MAIOR", numeroDependenteMaior);
		dadosPessoa.put("NUM_DEP_MENOR", numeroDependenteMenor);
		dadosPessoa.put("COMPLEMENTO_CARGO_CONJ", complementoCargoConjuge);
		dadosPessoa.put("RG_CONJUGE", rgConjuge);
		dadosPessoa.put("ORGAO_EMISSOR_CONJ", orgaoEmissorConjuge);
		dadosPessoa.put("LIMITE_CONJUGE", limiteConjuge);
		dadosPessoa.put("EMPRESA_CONJUGE", empresaConjuge);
		dadosPessoa.put("ADMISSAO_CONJUGE", admissaoConjuge);
		dadosPessoa.put("RENDA_CONJUGE", rendaConjuge);
		dadosPessoa.put("ATIVO", ativo);
		dadosPessoa.put("ENVIAR_EXTRATO", enviarExtrato);
		dadosPessoa.put("TIPO_EXTRATO", tipoExtrato);
		dadosPessoa.put("CONJ_PODE_COMPRAR", conjugePodeComprar);
		dadosPessoa.put("DT_ULT_COMPRA", dtUltimaCompra.replace("0000-00-00", ""));
		dadosPessoa.put("DT_RENOVACAO", dtRenovacao.replace("0000-00-00", ""));
		dadosPessoa.put("STATUS_CADASTRO_NOVO", "null");
		
		final PessoaSql pessoaSql = new PessoaSql(context);

		// Pega o sql para passar para o statement
		final String sql = pessoaSql.construirSqlStatement(dadosPessoa);
		// Pega o argumento para o statement
		final String[] argumentoSql = pessoaSql.argumentoStatement(dadosPessoa);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//pessoaSql.insertOrReplace(dadosPessoa);
						pessoaSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				pessoaSql.insertOrReplaceFast(sql, argumentoSql);
			}
			
		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						pessoaSql.update(dadosPessoa, "ID_CFACLIFO = " + idClifo);
					}
				});
			} else {
				pessoaSql.update(dadosPessoa, "ID_CFACLIFO = " + idClifo);
			}
			
		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						pessoaSql.delete("ID_CFACLIFO = " + idClifo);
					}
				});
			} else {
				pessoaSql.delete("ID_CFACLIFO = " + idClifo);
			}
		}
		
		//dadosPessoa.clear();
		
	} // Pessoa
	
	
	private void importarRegistroAreas(String linha){
		
		Scanner scannerAreas = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerAreas.next();
		final String idAreas = scannerAreas.next();
		String dtAlt = scannerAreas.next();
		String codigo = scannerAreas.next();
		String descricao = scannerAreas.next();
		String descAtacVista = scannerAreas.next();
		String descAtacPrazo = scannerAreas.next();
		String descVareVista = scannerAreas.next();
		String descVarePrazo = scannerAreas.next();
		String descPromocao = scannerAreas.next();
		// Libera memoria
		scannerAreas = null;
		
		final ContentValues dadosAreas = new ContentValues();
		dadosAreas.put("ID_CFAAREAS", idAreas);
		dadosAreas.put("DT_ALT", dtAlt);
		dadosAreas.put("CODIGO", codigo);
		dadosAreas.put("DESCRICAO", descricao);
		dadosAreas.put("DESC_ATAC_VISTA", descAtacVista);
		dadosAreas.put("DESC_ATAC_PRAZO", descAtacPrazo);
		dadosAreas.put("DESC_VARE_VISTA", descVareVista);
		dadosAreas.put("DESC_VARE_PRAZO", descVarePrazo);
		dadosAreas.put("DESC_PROMOCAO", descPromocao);
		
		final AreasSql areasSql = new AreasSql(context);

		// Pega o sql para passar para o statement
		final String sql = areasSql.construirSqlStatement(dadosAreas);
		// Pega o argumento para o statement
		final String[] argumentoSql = areasSql.argumentoStatement(dadosAreas);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//areasSql.insertOrReplace(dadosAreas);
						areasSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				areasSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						areasSql.update(dadosAreas, "ID_CFAAREAS = " + idAreas);
					}
				});
			} else {
				areasSql.update(dadosAreas, "ID_CFAAREAS = " + idAreas);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						areasSql.delete("ID_CFAAREAS = " + idAreas);
					}
				});
			} else {
				areasSql.delete("ID_CFAAREAS = " + idAreas);
			}
		}
			  
		//dadosAreas.clear();
	} // FIm Areas

	
	private void importarRegistroAtividade(String linha){
		Scanner scannerAtividade = new Scanner(linha).useDelimiter("\\|");
		
		final String FINALIDADE = scannerAtividade.next();
		final String idAtivi = scannerAtividade.next();
		String dtAlt = scannerAtividade.next();
		String codigo = scannerAtividade.next();
		String descricao = scannerAtividade.next();
		String descAtacVista = scannerAtividade.next();
		String descAtacPrazo = scannerAtividade.next();
		String descVareVista = scannerAtividade.next();
		String descVarePrazo = scannerAtividade.next();
		String descPromocao = scannerAtividade.next();
		
		// Libera a memoria da variavel
		scannerAtividade = null;
		
		final ContentValues dadosAtividade = new ContentValues();
		dadosAtividade.put("ID_CFAATIVI", idAtivi);
		dadosAtividade.put("DT_ALT", dtAlt);
		dadosAtividade.put("CODIGO", codigo);
		dadosAtividade.put("DESCRICAO", descricao);
		dadosAtividade.put("DESC_ATAC_VISTA", descAtacVista);
		dadosAtividade.put("DESC_ATAC_PRAZO", descAtacPrazo);
		dadosAtividade.put("DESC_VARE_VISTA", descVareVista);
		dadosAtividade.put("DESC_VARE_PRAZO", descVarePrazo);
		dadosAtividade.put("DESC_PROMOCAO", descPromocao);
		
		final RamoAtividadeSql atividadeSql = new RamoAtividadeSql(context);

		// Pega o sql para passar para o statement
		final String sql = atividadeSql.construirSqlStatement(dadosAtividade);
		// Pega o argumento para o statement
		final String[] argumentoSql = atividadeSql.argumentoStatement(dadosAtividade);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//atividadeSql.insertOrReplace(dadosAtividade);
						atividadeSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				atividadeSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						atividadeSql.update(dadosAtividade, "ID_CFAATIVI = " + idAtivi);
					}
				});
			} else {
				atividadeSql.update(dadosAtividade, "ID_CFAATIVI = " + idAtivi);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						atividadeSql.delete("ID_CFAATIVI = " + idAtivi);
					}
				});
			} else {
				atividadeSql.delete("ID_CFAATIVI = " + idAtivi);
			}
		}
		//dadosAtividade.clear();
	} // Fim atividade
	
	
	private void importarRegistroStatus(String linha){
		Scanner scannerAtividade = new Scanner(linha).useDelimiter("\\|");
		
		final String FINALIDADE = scannerAtividade.next();
		final String idStatu = scannerAtividade.next();
		String dtAlt = scannerAtividade.next();
		String codigo = scannerAtividade.next();
		String descricao = scannerAtividade.next();
		String mensagem = scannerAtividade.next();
		String bloqueia = scannerAtividade.next();
		String descAtacVista = scannerAtividade.next();
		String descAtacPrazo = scannerAtividade.next();
		String descVareVista = scannerAtividade.next();
		String descVarePrazo = scannerAtividade.next();
		String descPromocao = scannerAtividade.next();
		String parcelaEmAberto = scannerAtividade.next();
		String vistaPrazo = scannerAtividade.next();
		
		scannerAtividade = null;
		
		final ContentValues dadosStatus = new ContentValues();
		dadosStatus.put("ID_CFASTATU", idStatu);
		dadosStatus.put("DT_ALT", dtAlt);
		dadosStatus.put("CODIGO", codigo);
		dadosStatus.put("DESCRICAO", descricao);
		dadosStatus.put("MENSAGEM", mensagem);
		dadosStatus.put("BLOQUEIA", bloqueia);
		dadosStatus.put("DESC_ATAC_VISTA", descAtacVista);
		dadosStatus.put("DESC_ATAC_PRAZO", descAtacPrazo);
		dadosStatus.put("DESC_VARE_VISTA", descVareVista);
		dadosStatus.put("DESC_VARE_PRAZO", descVarePrazo);
		dadosStatus.put("DESC_PROMOCAO", descPromocao);
		dadosStatus.put("PARCELA_EM_ABERTO", parcelaEmAberto);
		dadosStatus.put("VISTA_PRAZO", vistaPrazo);
		
		final StatusSql statusSql = new StatusSql(context); 

		// Pega o sql para passar para o statement
		final String sql = statusSql.construirSqlStatement(dadosStatus);
		// Pega o argumento para o statement
		final String[] argumentoSql = statusSql.argumentoStatement(dadosStatus);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//statusSql.insertOrReplace(dadosStatus);
						statusSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				statusSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						statusSql.update(dadosStatus, "ID_CFASTATU = " + idStatu);
					}
				});
			} else {
				statusSql.update(dadosStatus, "ID_CFASTATU = " + idStatu);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						statusSql.delete("ID_CFASTATU = " + idStatu);
					}
				});
			} else {
				statusSql.delete("ID_CFASTATU = " + idStatu);
			}
		}
		//dadosStatus.clear();
	} // Fim Status
	
	
	private void importarRegistroTipoDocumento(String linha){
		
		Scanner scannerTipoDoc = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerTipoDoc.next();
		final String idTpdoc = scannerTipoDoc.next();
		String idEmpre = scannerTipoDoc.next();
		String dtAlt = scannerTipoDoc.next();
		String codigo = scannerTipoDoc.next();
		String descricao = scannerTipoDoc.next();
		String sigla = scannerTipoDoc.next();
		String tipo = scannerTipoDoc.next();
		
		// Libera memoria
		scannerTipoDoc = null;
		
		final ContentValues dadosTipoDoc = new ContentValues();
		dadosTipoDoc.put("ID_CFATPDOC", idTpdoc);
		dadosTipoDoc.put("ID_SMAEMPRE", idEmpre);
		dadosTipoDoc.put("DT_ALT", dtAlt);
		dadosTipoDoc.put("CODIGO", codigo);
		dadosTipoDoc.put("DESCRICAO", descricao);
		dadosTipoDoc.put("SIGLA", sigla);
		dadosTipoDoc.put("TIPO", tipo);
		
		final TipoDocumentoSql tipoDocumentoSql = new TipoDocumentoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = tipoDocumentoSql.construirSqlStatement(dadosTipoDoc);
		// Pega o argumento para o statement
		final String[] argumentoSql = tipoDocumentoSql.argumentoStatement(dadosTipoDoc);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//tipoDocumentoSql.insertOrReplace(dadosTipoDoc);
						tipoDocumentoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				tipoDocumentoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tipoDocumentoSql.update(dadosTipoDoc, "ID_CFATPDOC = " + idTpdoc);
					}
				});
			} else {
				tipoDocumentoSql.update(dadosTipoDoc, "ID_CFATPDOC = " + idTpdoc);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tipoDocumentoSql.delete("ID_CFATPDOC = " + idTpdoc);
					}
				});
			} else {
				tipoDocumentoSql.delete("ID_CFATPDOC = " + idTpdoc);
			}
		}
	} // FIm TipoDocumento
	
	
	private void importarRegistroCartaoCredito(String linha){
		
		Scanner scannerCartao = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerCartao.next();
		final String idCred = scannerCartao.next();
		String dtAlt = scannerCartao.next();
		String codigo = scannerCartao.next();
		String descricao = scannerCartao.next();
		
		scannerCartao = null;
		
		final ContentValues dadosCartao = new ContentValues();
		dadosCartao.put("ID_CFACCRED", idCred);
		dadosCartao.put("DT_ALT", dtAlt);
		dadosCartao.put("CODIGO", codigo);
		dadosCartao.put("DESCRICAO", descricao);
		
		final CartaoSql cartaoSql = new CartaoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = cartaoSql.construirSqlStatement(dadosCartao);
		// Pega o argumento para o statement
		final String[] argumentoSql = cartaoSql.argumentoStatement(dadosCartao);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//cartaoSql.insertOrReplace(dadosCartao);
						cartaoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				cartaoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cartaoSql.update(dadosCartao, "ID_CFACCRED = " + idCred);
					}
				});
			} else {
				cartaoSql.update(dadosCartao, "ID_CFACCRED = " + idCred);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cartaoSql.delete("ID_CFACCRED = " + idCred);
					}
				});
			} else {
				cartaoSql.delete("ID_CFACCRED = " + idCred);
			}
		}
	}
	
	
	private void importarRegistroPortador(String linha){
		
		Scanner scannerPortador = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerPortador.next();
		final String idPorta = scannerPortador.next();
		String dtAlt = scannerPortador.next();
		String codigo = scannerPortador.next();
		String dg = scannerPortador.next();
		String descricao = scannerPortador.next();
		String sigla = scannerPortador.next();
		String tipo = scannerPortador.next();
		
		scannerPortador = null;
		
		final ContentValues dadosPortador = new ContentValues();
		dadosPortador.put("ID_CFAPORTA", idPorta);
		dadosPortador.put("DT_ALT", dtAlt);
		dadosPortador.put("CODIGO", codigo);
		dadosPortador.put("DG", dg);
		dadosPortador.put("DESCRICAO", descricao);
		dadosPortador.put("SIGLA", sigla);
		dadosPortador.put("TIPO", tipo);
		
		final PortadorBancoSql portadorSql = new PortadorBancoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = portadorSql.construirSqlStatement(dadosPortador);
		// Pega o argumento para o statement
		final String[] argumentoSql = portadorSql.argumentoStatement(dadosPortador);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//portadorSql.insertOrReplace(dadosPortador);
						portadorSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				portadorSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						portadorSql.update(dadosPortador, "ID_CFAPORTA = " + idPorta);
					}
				});
			} else {
				portadorSql.update(dadosPortador, "ID_CFAPORTA = " + idPorta);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						portadorSql.delete("ID_CFAPORTA = " + idPorta);
					}
				});
			} else {
				portadorSql.delete("ID_CFAPORTA = " + idPorta);
			}
		}
	} // Fim Portador
	
	private void importarRegistroProfissao(String linha){
		
		Scanner scannerProfissao = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerProfissao.next();
		final String idProfi = scannerProfissao.next();
		String dtAlt = scannerProfissao.next();
		String codigo = scannerProfissao.next();
		String descricao = scannerProfissao.next();
		String cbo = scannerProfissao.next();
		String descAtacVista = scannerProfissao.next();
		String descAtacPrazo = scannerProfissao.next();
		String descVareVista = scannerProfissao.next();
		String descVarePrazo = scannerProfissao.next();
		String descPromocao =  scannerProfissao.next();
		
		scannerProfissao = null;
		
		final ContentValues dadosProfissao = new ContentValues();
		dadosProfissao.put("ID_CFAPROFI", idProfi);
		dadosProfissao.put("DT_ALT", dtAlt);
		dadosProfissao.put("CODIGO", codigo);
		dadosProfissao.put("DESCRICAO", descricao);
		dadosProfissao.put("CBO", cbo);
		dadosProfissao.put("DESC_ATAC_VISTA", descAtacVista);
		dadosProfissao.put("DESC_ATAC_PRAZO", descAtacPrazo);
		dadosProfissao.put("DESC_VARE_VISTA", descVareVista);
		dadosProfissao.put("DESC_VARE_PRAZO", descVarePrazo);
		dadosProfissao.put("DESC_PROMOCAO", descPromocao);
		
		final ProfissaoSql profissaoSql = new ProfissaoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = profissaoSql.construirSqlStatement(dadosProfissao);
		// Pega o argumento para o statement
		final String[] argumentoSql = profissaoSql.argumentoStatement(dadosProfissao);
				
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//profissaoSql.insertOrReplace(dadosProfissao);
						profissaoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				profissaoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						profissaoSql.update(dadosProfissao, "ID_CFAPROFI = " + idProfi);
					}
				});
			} else {
				profissaoSql.update(dadosProfissao, "ID_CFAPROFI = " + idProfi);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						profissaoSql.delete("ID_CFAPROFI = " + idProfi);
					}
				});
			} else {
				profissaoSql.delete("ID_CFAPROFI = " + idProfi);
			}
		}
	}

	private void importarRegistroTipoCliente(String linha){
		
		Scanner scannerTipoCliente = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerTipoCliente.next();
		final String idTpCli = scannerTipoCliente.next();
		String dtAlt = scannerTipoCliente.next();
		String codigo = scannerTipoCliente.next();
		String descricao = scannerTipoCliente.next();
		String descAtacVista = scannerTipoCliente.next();
		String descAtacPrazo = scannerTipoCliente.next();
		String descVareVista = scannerTipoCliente.next();
		String descVarePrazo = scannerTipoCliente.next();
		String descPromocao = scannerTipoCliente.next();
		String vendeAtacVare = scannerTipoCliente.next();
		
		scannerTipoCliente = null;
		
		final ContentValues dadosTipoCliente = new ContentValues();
		dadosTipoCliente.put("ID_CFATPCLI", idTpCli);
		dadosTipoCliente.put("DT_ALT", dtAlt);
		dadosTipoCliente.put("CODIGO", codigo);
		dadosTipoCliente.put("DESCRICAO", descricao);
		dadosTipoCliente.put("DESC_ATAC_VISTA", descAtacVista);
		dadosTipoCliente.put("DESC_ATAC_PRAZO", descAtacPrazo);
		dadosTipoCliente.put("DESC_VARE_VISTA", descVareVista);
		dadosTipoCliente.put("DESC_VARE_PRAZO", descVarePrazo);
		dadosTipoCliente.put("DESC_PROMOCAO", descPromocao);
		dadosTipoCliente.put("VENDE_ATAC_VAREJO", vendeAtacVare);
		
		final TipoClienteSql tipoClienteSql = new TipoClienteSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = tipoClienteSql.construirSqlStatement(dadosTipoCliente);
		// Pega o argumento para o statement
		final String[] argumentoSql = tipoClienteSql.argumentoStatement(dadosTipoCliente);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//tipoClienteSql.insertOrReplace(dadosTipoCliente);
						tipoClienteSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				tipoClienteSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tipoClienteSql.update(dadosTipoCliente, "ID_CFATPCLI = " + idTpCli);
					}
				});
			} else {
				tipoClienteSql.update(dadosTipoCliente, "ID_CFATPCLI = " + idTpCli);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tipoClienteSql.delete("ID_CFATPCLI = " + idTpCli);
					}
				});
			} else {
				tipoClienteSql.delete("ID_CFATPCLI = " + idTpCli);
			}
		}
	} // Fim TipoCliente
	
	
	private void importarRegistroEstado(String linha){
		
		Scanner scannerEstado = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerEstado.next();
		final String idEstad = scannerEstado.next();
		String dtAlt = scannerEstado.next();
		String uf = scannerEstado.next();
		String descricao = scannerEstado.next();
		String icmsSai = scannerEstado.next();
		String tipoIpiSai = scannerEstado.next();
		String IpiSai = scannerEstado.next();
		String codIbge = scannerEstado.next();
		
		scannerEstado = null;
		
		final ContentValues dadosEstado = new ContentValues();
		dadosEstado.put("ID_CFAESTAD", idEstad);
		dadosEstado.put("DT_ALT", dtAlt);
		dadosEstado.put("UF", uf);
		dadosEstado.put("DESCRICAO", descricao);
		dadosEstado.put("ICMS_SAI", icmsSai);
		dadosEstado.put("TIPO_IPI_SAI", tipoIpiSai);
		dadosEstado.put("IPI_SAI", IpiSai);
		dadosEstado.put("COD_IBGE", codIbge);
		
		final EstadoSql estadoSql = new EstadoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = estadoSql.construirSqlStatement(dadosEstado);
		// Pega o argumento para o statement
		final String[] argumentoSql = estadoSql.argumentoStatement(dadosEstado);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						estadoSql.insertOrReplace(dadosEstado);
					}
				});
			} else {
				estadoSql.insertOrReplace(dadosEstado);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						estadoSql.update(dadosEstado, "ID_CFAESTAD = " + idEstad);
					}
				});
			} else {
				estadoSql.update(dadosEstado, "ID_CFAESTAD = " + idEstad);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						estadoSql.delete("ID_CFAESTAD = " + idEstad);
					}
				});
			} else {
				estadoSql.delete("ID_CFAESTAD = " + idEstad);
			}
		}
	} // Fim estado
	
	
	private void importarRegistroCidade(String linha){
		
		Scanner scannerCidade = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerCidade.next();
		final String idCidade = scannerCidade.next();
		String idEstad = scannerCidade.next();
		String dtAlt = scannerCidade.next();
		String descricao = scannerCidade.next();
		String codIbge = scannerCidade.next();
		
		scannerCidade.close();
		
		/*final String sql = "INSERT OR REPLACE INTO CFACIDAD (ID_CFACIDAD, ID_CFAESTAD, DT_ALT, DESCRICAO, COD_IBGE) VALUES (" +
							idEstad + ", " + idCidade + ", " + dtAlt + ", '" + descricao + "', " + codIbge + ")";*/
		
		final ContentValues dadosCidade = new ContentValues();
		dadosCidade.put("ID_CFACIDAD", idCidade);
		dadosCidade.put("ID_CFAESTAD", idEstad);
		dadosCidade.put("DT_ALT", dtAlt);
		dadosCidade.put("DESCRICAO", descricao.replace("'", " "));
		dadosCidade.put("COD_IBGE",codIbge);
		
		final CidadeSql cidadeSql = new CidadeSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = cidadeSql.construirSqlStatement(dadosCidade);
		// Pega o argumento para o statement
		final String[] argumentoSql = cidadeSql.argumentoStatement(dadosCidade);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//cidadeSql.insertOrReplace(dadosCidade);
						cidadeSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				cidadeSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cidadeSql.update(dadosCidade, "ID_CFACIDAD = " + idCidade);
					}
				});
			} else {
				cidadeSql.update(dadosCidade, "ID_CFACIDAD = " + idCidade);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cidadeSql.delete("ID_CFACIDAD = " + idCidade);
					}
				});
			} else {
				cidadeSql.delete("ID_CFACIDAD = " + idCidade);
			}
		}
	} // Fim Cidade
	
	
	private void importarRegistroEndereco(String linha){
		
		Scanner scannerEndereco = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerEndereco.next();
		final String idEnder = scannerEndereco.next();
		String idEstad = scannerEndereco.next();
		String idCidad = scannerEndereco.next();
		String idEmpre = scannerEndereco.next();
		String idClifo = scannerEndereco.next();
		String dtAlt = scannerEndereco.next();
		String tipo = scannerEndereco.next();
		String cep = scannerEndereco.next();
		String bairro = scannerEndereco.next();
		String logradouro = scannerEndereco.next();
		String numero = scannerEndereco.next();
		String complemento = scannerEndereco.next();
		String email = scannerEndereco.next();
		String letraCxPostal = scannerEndereco.next();
		String caixaPostal = scannerEndereco.next();
		
		scannerEndereco = null;
		
		final ContentValues dadosEndereco = new ContentValues();
		dadosEndereco.put("ID_CFAENDER", idEnder);
		dadosEndereco.put("ID_CFAESTAD", idEstad);
		dadosEndereco.put("ID_CFACIDAD", idCidad);
		dadosEndereco.put("ID_SMAEMPRE", idEmpre);
		dadosEndereco.put("ID_CFACLIFO", idClifo);
		dadosEndereco.put("DT_ALT", dtAlt);
		dadosEndereco.put("TIPO", tipo);
		dadosEndereco.put("CEP", cep);
		dadosEndereco.put("BAIRRO", bairro.replace("'", " "));
		dadosEndereco.put("LOGRADOURO", logradouro);
		dadosEndereco.put("NUMERO", numero);
		dadosEndereco.put("COMPLEMENTO", complemento);
		dadosEndereco.put("EMAIL", email);
		dadosEndereco.put("LETRA_CX_POSTAL", letraCxPostal);
		dadosEndereco.put("CAIXA_POSTAL", caixaPostal);
		
		final EnderecoSql enderecoSql = new EnderecoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = enderecoSql.construirSqlStatement(dadosEndereco);
		// Pega o argumento para o statement
		final String[] argumentoSql = enderecoSql.argumentoStatement(dadosEndereco);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//enderecoSql.insertOrReplace(dadosEndereco);
						enderecoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				enderecoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						enderecoSql.update(dadosEndereco, "ID_CFAENDER = " + idEnder);
					}
				});
			} else {
				enderecoSql.update(dadosEndereco, "ID_CFAENDER = " + idEnder);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						enderecoSql.delete("ID_CFAENDER = " + idEnder);
					}
				});
			} else {
				enderecoSql.delete("ID_CFAENDER = " + idEnder);
			}
		}
	} // FIm Endereco
	
	private void importarRegistroParametro(String linha){
		
		Scanner scannerParametro = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerParametro.next();
		final String idParam = scannerParametro.next();
		String idClifo = scannerParametro.next();
		String idEmpre = scannerParametro.next();
		String idClifoVende = scannerParametro.next();
		String idTpCob = scannerParametro.next();
		String idPorta = scannerParametro.next();
		String idTpDoc = scannerParametro.next();
		String idPlPgt = scannerParametro.next();
		String dtAlt = scannerParametro.next();
		String vendeAtrazado = scannerParametro.next();
		String limite = scannerParametro.next();
		String descAtacVista = scannerParametro.next();
		String descAtacPrazo = scannerParametro.next();
		String descVareVista = scannerParametro.next();
		String descVarePrazo = scannerParametro.next();
		String descPromocao = scannerParametro.next();
		String roteiro = scannerParametro.next();
		String frequencia = scannerParametro.next();
		String dtUltimaVisita = scannerParametro.next();
		String dtUltimoEnvio = scannerParametro.next();
		String dtUltimoRecebto = scannerParametro.next();
		String dtProximoContato = scannerParametro.next();
		String diasAtrazo = scannerParametro.next();
		String diasCarencia = scannerParametro.next();
		String jurosDiario = scannerParametro.next();
		String atacadoVarejo = scannerParametro.next();
		String vistaPrazo = scannerParametro.next();
		String faturaVlMin = scannerParametro.next();
		String parcelaAberto = scannerParametro.next();
		
		scannerParametro = null;
		
		final ContentValues dadosParametro = new ContentValues();
		dadosParametro.put("ID_CFAPARAM", idParam);
		dadosParametro.put("ID_CFACLIFO", idClifo);
		dadosParametro.put("ID_SMAEMPRE", idEmpre);
		dadosParametro.put("ID_CFACLIFO_VENDE", idClifoVende);
		dadosParametro.put("ID_CFATPCOB", idTpCob);
		dadosParametro.put("ID_CFAPORTA", idPorta);
		dadosParametro.put("ID_CFATPDOC", idTpDoc);
		dadosParametro.put("ID_AEAPLPGT", idPlPgt);
		dadosParametro.put("DT_ALT", dtAlt);
		dadosParametro.put("VENDE_ATRAZADO", vendeAtrazado);
		dadosParametro.put("LIMITE", limite);
		dadosParametro.put("DESC_ATAC_VISTA", descAtacVista);
		dadosParametro.put("DESC_ATAC_PRAZO", descAtacPrazo);
		dadosParametro.put("DESC_VARE_VISTA", descVareVista);
		dadosParametro.put("DESC_VARE_PRAZO", descVarePrazo);
		dadosParametro.put("DESC_PROMOCAO", descPromocao);
		dadosParametro.put("ROTEIRO", roteiro);
		dadosParametro.put("FREQUENCIA", frequencia);
		dadosParametro.put("DT_ULT_VISITA", dtUltimaVisita.replace("0000-00-00", ""));
		dadosParametro.put("DT_ULT_ENVIO", dtUltimoEnvio.replace("0000-00-00", ""));
		dadosParametro.put("DT_ULT_RECEBTO", dtUltimoRecebto.replace("0000-00-00", ""));
		dadosParametro.put("DT_PROXIMO_CONTATO", dtProximoContato.replace("0000-00-00", ""));
		dadosParametro.put("DIAS_ATRAZO", diasAtrazo);
		dadosParametro.put("DIAS_CARENCIA", diasCarencia);
		dadosParametro.put("JUROS_DIARIO", jurosDiario);
		dadosParametro.put("ATACADO_VAREJO", atacadoVarejo);
		dadosParametro.put("VISTA_PRAZO", vistaPrazo);
		dadosParametro.put("FATURA_VL_MIN", faturaVlMin);
		dadosParametro.put("PARCELA_EM_ABERTO", parcelaAberto);
		
		final ParametrosSql parametrosSql = new ParametrosSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = parametrosSql.construirSqlStatement(dadosParametro);
		// Pega o argumento para o statement
		final String[] argumentoSql = parametrosSql.argumentoStatement(dadosParametro);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//parametrosSql.insertOrReplace(dadosParametro);
						parametrosSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				parametrosSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						parametrosSql.update(dadosParametro, "ID_CFAPARAM = " + idParam);
					}
				});
			} else {
				parametrosSql.update(dadosParametro, "ID_CFAPARAM = " + idParam);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						parametrosSql.delete("ID_CFAPARAM = " + idParam);
					}
				});
			} else {
				parametrosSql.delete("ID_CFAPARAM = " + idParam);
			}
		}
	} // Fim Parametros

	
	private void importarRegistroClasse(String linha){
		
		Scanner scannerClasse = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerClasse.next();
		final String idClase = scannerClasse.next();
		String dtAlt = scannerClasse.next();
		String codigo = scannerClasse.next();
		String descricao = scannerClasse.next();
		
		final ContentValues dadosClasse = new ContentValues();
		dadosClasse.put("ID_AEACLASE", idClase);
		dadosClasse.put("DT_ALT", dtAlt);
		dadosClasse.put("CODIGO", codigo);
		dadosClasse.put("DESCRICAO", descricao);
		
		final ClasseSql classeSql = new ClasseSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = classeSql.construirSqlStatement(dadosClasse);
		// Pega o argumento para o statement
		final String[] argumentoSql = classeSql.argumentoStatement(dadosClasse);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//classeSql.insertOrReplace(dadosClasse);
						classeSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				classeSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						classeSql.update(dadosClasse, "ID_AEACLASE = " + idClase);
					}
				});
			} else {
				classeSql.update(dadosClasse, "ID_AEACLASE = " + idClase);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						classeSql.delete("ID_AEACLASE = " + idClase);
					}
				});
			} else {
				classeSql.delete("ID_AEACLASE = " + idClase);
			}
		}
	} // FIm classe
	
	
	private void importarRegistroUnidadeVenda(String linha){
		
		Scanner scannerUnVenda = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerUnVenda.next();
		final String idUnVen = scannerUnVenda.next();
		String dtAlt = scannerUnVenda.next();
		String sigla = scannerUnVenda.next();
		String descricao = scannerUnVenda.next();
		String decimais = scannerUnVenda.next();
		
		scannerUnVenda = null;
				
		final ContentValues dadosUnVenda = new ContentValues();
		
		dadosUnVenda.put("ID_AEAUNVEN", idUnVen);
		dadosUnVenda.put("DT_ALT", dtAlt);
		dadosUnVenda.put("SIGLA", sigla);
		dadosUnVenda.put("DESCRICAO_SINGULAR", descricao);
		dadosUnVenda.put("DECIMAIS", decimais);
		
		final UnidadeVendaSql unVendaSql = new UnidadeVendaSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = unVendaSql.construirSqlStatement(dadosUnVenda);
		// Pega o argumento para o statement
		final String[] argumentoSql = unVendaSql.argumentoStatement(dadosUnVenda);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//unVendaSql.insertOrReplace(dadosUnVenda);
						unVendaSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				unVendaSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						unVendaSql.update(dadosUnVenda, "ID_AEAUNVEN = " + idUnVen);
					}
				});
			} else {
				unVendaSql.update(dadosUnVenda, "ID_AEAUNVEN = " + idUnVen);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						unVendaSql.delete("ID_AEAUNVEN = " + idUnVen);
					}
				});
			} else {
				unVendaSql.delete("ID_AEAUNVEN = " + idUnVen);
			}
		}
	} // Fim UnidadeVenda
	
	
	private void importarRegistroEstoque(String linha){
		
		Scanner scannerEstoque = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerEstoque.next();
		final String idEstoq = scannerEstoque.next();
		String idPloja = scannerEstoque.next();
		String idLoces = scannerEstoque.next();
		String dtAlt = scannerEstoque.next();
		String estoque = scannerEstoque.next();
		String retido = scannerEstoque.next();
		String ativo = scannerEstoque.next();
		
		scannerEstoque = null;
		
		final ContentValues dadosEstoque = new ContentValues();
		dadosEstoque.put("ID_AEAESTOQ", idEstoq);
		dadosEstoque.put("ID_AEAPLOJA", idPloja);
		dadosEstoque.put("ID_AEALOCES", idLoces);
		dadosEstoque.put("DT_ALT", dtAlt);
		dadosEstoque.put("ESTOQUE", estoque);
		dadosEstoque.put("RETIDO", retido);
		dadosEstoque.put("ATIVO", ativo);
		
		final EstoqueSql estoqueSql = new EstoqueSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = estoqueSql.construirSqlStatement(dadosEstoque);
		// Pega o argumento para o statement
		final String[] argumentoSql = estoqueSql.argumentoStatement(dadosEstoque);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//estoqueSql.insertOrReplace(dadosEstoque);
						estoqueSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				estoqueSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						estoqueSql.update(dadosEstoque, "ID_AEAESTOQ = " + idEstoq);
					}
				});
			} else {
				estoqueSql.update(dadosEstoque, "ID_AEAESTOQ = " + idEstoq);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						estoqueSql.delete("ID_AEAESTOQ = " + idEstoq);
					}
				});
			} else {
				estoqueSql.delete("ID_AEAESTOQ = " + idEstoq);
			}
		}
	} // Fim Estoque
	
	private void importarRegistroOrcamento(String linha){
		
		Scanner scannerOrcamento = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerOrcamento.next();
		final String guid = scannerOrcamento.next();
		String idClifo = scannerOrcamento.next();
		String idEstado = scannerOrcamento.next();
		String idCidade = scannerOrcamento.next();
		String idRomaneio = scannerOrcamento.next();
		String idTipoDocumento = scannerOrcamento.next();
		String dataAlt = scannerOrcamento.next();
		String numero = scannerOrcamento.next();
		String valorFaturado = scannerOrcamento.next();
		String pessoaCliente = scannerOrcamento.next();
		String nomeCliente = scannerOrcamento.next();
		String ieRgCliente = scannerOrcamento.next();
		String cpfCgcCliente = scannerOrcamento.next();
		String enderecoCliente = scannerOrcamento.next();
		String bairroCliente = scannerOrcamento.next();
		String cepCliente = scannerOrcamento.next();
		String obs = scannerOrcamento.next();
		String tipoEntrega = scannerOrcamento.next();
		String statusRetorno = scannerOrcamento.next();
		
		scannerOrcamento = null;
		
		final ContentValues dadosOrcamento = new ContentValues();
		dadosOrcamento.put("GUID", guid); 
		dadosOrcamento.put("ID_CFACLIFO", idClifo);
		dadosOrcamento.put("ID_CFAESTAD", idEstado);
		dadosOrcamento.put("ID_CFACIDAD", idCidade);
		dadosOrcamento.put("ID_AEAROMAN", idRomaneio);
		dadosOrcamento.put("ID_CFATPDOC", idTipoDocumento);
		dadosOrcamento.put("DT_ALT", dataAlt);
		dadosOrcamento.put("NUMERO", numero);
		dadosOrcamento.put("VL_FATURADO", valorFaturado);
		dadosOrcamento.put("PESSOA_CLIENTE", pessoaCliente);
		dadosOrcamento.put("NOME_CLIENTE", nomeCliente);
		dadosOrcamento.put("IE_RG_CLIENTE", ieRgCliente);
		dadosOrcamento.put("CPF_CGC_CLIENTE", cpfCgcCliente);
		dadosOrcamento.put("ENDERECO_CLIENTE", enderecoCliente);
		dadosOrcamento.put("BAIRRO_CLIENTE", bairroCliente);
		dadosOrcamento.put("CEP_CLIENTE", cepCliente);
		dadosOrcamento.put("OBS", obs);
		dadosOrcamento.put("TIPO_ENTREGA", tipoEntrega);
		dadosOrcamento.put("STATUS_RETORNO", statusRetorno);
		
		final OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = orcamentoSql.construirSqlStatement(dadosOrcamento);
		// Pega o argumento para o statement
		final String[] argumentoSql = orcamentoSql.argumentoStatement(dadosOrcamento);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//gradeSql.insertOrReplace(dadosGrade);
						orcamentoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				orcamentoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						orcamentoSql.update(dadosOrcamento, "GUID = " + guid);
					}
				});
			} else {
				orcamentoSql.update(dadosOrcamento, "GUID = " + guid);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						orcamentoSql.delete("GUID = " + guid);
					}
				});
			} else {
				orcamentoSql.delete("GUID = " + guid);
			}
		}
	} // Fim importarRegistroOrcamento

	private void importarRegistroItemOrcamento(String linha){
		Scanner scannerOrcamento = new Scanner(linha).useDelimiter("\\|");

		String FINALIDADE = scannerOrcamento.next();
		final String guid = scannerOrcamento.next();
		String idOrcamento = scannerOrcamento.next();
		String idEstoque = scannerOrcamento.next();
		String idPlanoPagamento = scannerOrcamento.next();
		String idUnidadeVenda = scannerOrcamento.next();
		String dtAlt = scannerOrcamento.next();
		String quantidade = scannerOrcamento.next();
		String vlTabalaFaturado = scannerOrcamento.next();
		String vlLiquidoFaturado = scannerOrcamento.next();
		String complemento = scannerOrcamento.next();
		String seqDesconto = scannerOrcamento.next();
		String statusRetorno = scannerOrcamento.next();

		final ContentValues dadosItemOrcamento = new ContentValues();
		dadosItemOrcamento.put("GUID", guid);
		dadosItemOrcamento.put("ID_AEAORCAM", idOrcamento);
		dadosItemOrcamento.put("ID_AEAESTOQ", idEstoque);
		dadosItemOrcamento.put("ID_AEAPLPGT", idPlanoPagamento);
		dadosItemOrcamento.put("ID_AEAUNVEN", idUnidadeVenda);
		dadosItemOrcamento.put("DT_ALT", dtAlt);
		dadosItemOrcamento.put("QUANTIDADE_FATURADA", quantidade);
		dadosItemOrcamento.put("VL_TABELA_FATURADO", vlTabalaFaturado);
		dadosItemOrcamento.put("FC_LIQUIDO_FATURADO", vlLiquidoFaturado);
		dadosItemOrcamento.put("COMPLEMENTO", complemento);
		dadosItemOrcamento.put("SEQ_DESCONTO", seqDesconto);
		dadosItemOrcamento.put("STATUS_RETORNO", statusRetorno);

		final ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);

		// Pega o sql para passar para o statement
		final String sql = itemOrcamentoSql.construirSqlStatement(dadosItemOrcamento);
		// Pega o argumento para o statement
		final String[] argumentoSql = itemOrcamentoSql.argumentoStatement(dadosItemOrcamento);

		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//gradeSql.insertOrReplace(dadosGrade);
						itemOrcamentoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				itemOrcamentoSql.insertOrReplaceFast(sql, argumentoSql);
			}

			// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						itemOrcamentoSql.update(dadosItemOrcamento, "GUID = " + guid);
					}
				});
			} else {
				itemOrcamentoSql.update(dadosItemOrcamento, "GUID = " + guid);
			}

			// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						itemOrcamentoSql.delete("GUID = " + guid);
					}
				});
			} else {
				itemOrcamentoSql.delete("GUID = " + guid);
			}
		}
	}
	
	private void importarRegistroGrade(String linha){
		
		Scanner scannerGrade = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerGrade.next();
		final String idGrade = scannerGrade.next();
		String idTpGrd = scannerGrade.next();
		String dtAlt = scannerGrade.next();
		String descricao = scannerGrade.next();
		
		scannerGrade = null;
		
		final ContentValues dadosGrade = new ContentValues();
		dadosGrade.put("ID_AEAGRADE", idGrade);
		dadosGrade.put("ID_AEATPGRD", idTpGrd);
		dadosGrade.put("DT_ALT", dtAlt);
		dadosGrade.put("DESCRICAO", descricao);
		
		final GradeSql gradeSql = new GradeSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = gradeSql.construirSqlStatement(dadosGrade);
		// Pega o argumento para o statement
		final String[] argumentoSql = gradeSql.argumentoStatement(dadosGrade);
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//gradeSql.insertOrReplace(dadosGrade);
						gradeSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				gradeSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						gradeSql.update(dadosGrade, "ID_AEAGRADE = " + idGrade);
					}
				});
			} else {
				gradeSql.update(dadosGrade, "ID_AEAGRADE = " + idGrade);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						gradeSql.delete("ID_AEAGRADE = " + idGrade);
					}
				});
			} else {
				gradeSql.delete("ID_AEAGRADE = " + idGrade);
			}
		}
	} // FIm grade
	
	private void importarRegistroMarca(String linha){
		
		Scanner scannerMarca = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerMarca.next();
		final String idMarca = scannerMarca.next();
		String dtAlt = scannerMarca.next();
		String descricao = scannerMarca.next();
		
		scannerMarca = null;
		
		final ContentValues dadosMarca = new ContentValues();
		dadosMarca.put("ID_AEAMARCA", idMarca);
		dadosMarca.put("DT_ALT", dtAlt);
		dadosMarca.put("DESCRICAO", descricao);
		
		final MarcaSql marcaSql= new MarcaSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = marcaSql.construirSqlStatement(dadosMarca);
		// Pega o argumento para o statement
		final String[] argumentoSql = marcaSql.argumentoStatement(dadosMarca);
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//marcaSql.insertOrReplace(dadosMarca);
						marcaSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				marcaSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						marcaSql.update(dadosMarca, "ID_AEAMARCA = " + idMarca);
					}
				});
			} else {
				marcaSql.update(dadosMarca, "ID_AEAMARCA = " + idMarca);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						marcaSql.delete("ID_AEAMARCA = " + idMarca);
					}
				});
			} else {
				marcaSql.delete("ID_AEAMARCA = " + idMarca);
			}
		}
	} // FIm Marca
	
	
	private void importarRegistroProduto(String linha){
		
		Scanner scannerProduto = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerProduto.next();
		final String idProdu = scannerProduto.next();
		String idClase = scannerProduto.next();
		String idMarca = scannerProduto.next();
		String idUnVen = scannerProduto.next();
		String dtCad = scannerProduto.next();
		String dtAlt = scannerProduto.next();
		String descricao = scannerProduto.next();
		String descricaoAuxiliar = scannerProduto.next();
		String codigoEstrutural = scannerProduto.next();
		String referencia = scannerProduto.next();
		String codigoBarras = scannerProduto.next();
		String pesoLiquido = scannerProduto.next();
		String pesoBruto = scannerProduto.next();
		String ativo = scannerProduto.next();
		String tipo = scannerProduto.next();
		
		scannerProduto = null;
		
		final ContentValues dadosProduto = new ContentValues();
		dadosProduto.put("ID_AEAPRODU", idProdu);
		dadosProduto.put("ID_AEACLASE", idClase);
		dadosProduto.put("ID_AEAMARCA", idMarca);
		dadosProduto.put("ID_AEAUNVEN", idUnVen);
		dadosProduto.put("DT_CAD", dtCad);
		dadosProduto.put("DT_ALT", dtAlt);
		dadosProduto.put("DESCRICAO", descricao);
		dadosProduto.put("DESCRICAO_AUXILIAR", descricaoAuxiliar);
		dadosProduto.put("CODIGO_ESTRUTURAL", codigoEstrutural);
		dadosProduto.put("REFERENCIA", referencia);
		dadosProduto.put("CODIGO_BARRAS", codigoBarras);
		dadosProduto.put("PESO_LIQUIDO", pesoLiquido);
		dadosProduto.put("PESO_BRUTO", pesoBruto);
		dadosProduto.put("ATIVO", ativo);
		dadosProduto.put("TIPO", tipo);
		
		final ProdutoSql produtoSql = new ProdutoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = produtoSql.construirSqlStatement(dadosProduto);
		// Pega o argumento para o statement
		final String[] argumentoSql = produtoSql.argumentoStatement(dadosProduto);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//produtoSql.insertOrReplace(dadosProduto);
						produtoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				produtoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						produtoSql.update(dadosProduto, "ID_AEAPRODU = " + idProdu);
					}
				});
			} else {
				produtoSql.update(dadosProduto, "ID_AEAPRODU = " + idProdu);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						produtoSql.delete("ID_AEAPRODU = " + idProdu);
					}
				});
			} else {
				produtoSql.delete("ID_AEAPRODU = " + idProdu);
			}
		}
	} // Fim Produto
	
	
	private void importarRegistroEmbalagem(String linha){
		
		Scanner scannerEmbalagem = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerEmbalagem.next();
		final String idEmbal = scannerEmbalagem.next();
		String idProdu = scannerEmbalagem.next();
		String idUnven = scannerEmbalagem.next();
		String dtAlt = scannerEmbalagem.next();
		String principal = scannerEmbalagem.next();
		String descricao = scannerEmbalagem.next();
		String fatorConversao = scannerEmbalagem.next();
		String fatorPreco = scannerEmbalagem.next();
		String modulo = scannerEmbalagem.next();
		String decimais = scannerEmbalagem.next();
		String ativo = scannerEmbalagem.next();
		
		scannerEmbalagem = null;
		
		final ContentValues dadosEmbal = new ContentValues();
		dadosEmbal.put("ID_AEAEMBAL", idEmbal);
		dadosEmbal.put("ID_AEAPRODU", idProdu);
		dadosEmbal.put("ID_AEAUNVEN", idUnven);
		dadosEmbal.put("DT_ALT", dtAlt);
		dadosEmbal.put("PRINCIPAL", principal);
		dadosEmbal.put("DESCRICAO", descricao);
		dadosEmbal.put("FATOR_CONVERSAO", fatorConversao);
		dadosEmbal.put("FATOR_PRECO", fatorPreco);
		dadosEmbal.put("MODULO", modulo);
		dadosEmbal.put("DECIMAIS", decimais);
		dadosEmbal.put("ATIVO", ativo);
		
		final EmbalagemSql embalagemSql = new EmbalagemSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = embalagemSql.construirSqlStatement(dadosEmbal);
		// Pega o argumento para o statement
		final String[] argumentoSql = embalagemSql.argumentoStatement(dadosEmbal);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//embalagemSql.insertOrReplace(dadosEmbal);
						embalagemSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				embalagemSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						embalagemSql.update(dadosEmbal, "ID_AEAEMBAL = " + idEmbal);
					}
				});
			} else {
				embalagemSql.update(dadosEmbal, "ID_AEAEMBAL = " + idEmbal);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						embalagemSql.delete("ID_AEAEMBAL = " + idEmbal);
					}
				});
			} else {
				embalagemSql.delete("ID_AEAEMBAL = " + idEmbal);
			}
		}
	} // FIm embalagem
	
	private void importarRegistroPLoja(String linha){
		
		Scanner scannerPLoja = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerPLoja.next();
		final String idPLoja = scannerPLoja.next();
		String idEmpre = scannerPLoja.next();
		String idProdu = scannerPLoja.next();
		String idCodSt = scannerPLoja.next();
		String dtAlt = scannerPLoja.next();
		String estoqueF = scannerPLoja.next();
		String estoqueC = scannerPLoja.next();
		String retido = scannerPLoja.next();
		String pedido = scannerPLoja.next();
		String ativo = scannerPLoja.next();
		String dtEntradaD = scannerPLoja.next();
		String dtEntradaN = scannerPLoja.next();
		String ctReposicaoN = scannerPLoja.next();
		String ctCompletoN = scannerPLoja.next();
		String vendaAtac = scannerPLoja.next();
		String vendaVare = scannerPLoja.next();
		String promocaoAtac = scannerPLoja.next();
		String promocaoVare = scannerPLoja.next();
		String precoMinAtac = scannerPLoja.next();
		String precoMinVare = scannerPLoja.next();
		String precoMaxAtac = scannerPLoja.next();
		String precoMaxVare = scannerPLoja.next();
		
		scannerPLoja = null;
		
		final ContentValues dadosPLoja = new ContentValues();
		dadosPLoja.put("ID_AEAPLOJA", idPLoja);
		dadosPLoja.put("ID_SMAEMPRE", idEmpre);
		dadosPLoja.put("ID_AEAPRODU", idProdu);
		dadosPLoja.put("ID_AEACODST", idCodSt);
		dadosPLoja.put("DT_ALT", dtAlt);
		dadosPLoja.put("ESTOQUE_F", estoqueF);
		dadosPLoja.put("ESTOQUE_C", estoqueC);
		dadosPLoja.put("RETIDO", retido);
		dadosPLoja.put("PEDIDO", pedido);
		dadosPLoja.put("ATIVO", ativo);
		dadosPLoja.put("DT_ENTRADA_D", dtEntradaD.replace("0000-00-00", ""));
		dadosPLoja.put("DT_ENTRADA_N", dtEntradaN.replace("0000-00-00", ""));
		dadosPLoja.put("CT_REPOSICAO_N", ctReposicaoN);
		dadosPLoja.put("CT_COMPLETO_N", ctCompletoN);
		dadosPLoja.put("VENDA_ATAC", vendaAtac);
		dadosPLoja.put("VENDA_VARE", vendaVare);
		dadosPLoja.put("PROMOCAO_ATAC", promocaoAtac);
		dadosPLoja.put("PROMOCAO_VARE", promocaoVare);
		dadosPLoja.put("PRECO_MINIMO_ATAC", precoMinAtac);
		dadosPLoja.put("PRECO_MINIMO_VARE", precoMinVare);
		dadosPLoja.put("PRECO_MAXIMO_ATAC", precoMaxAtac);
		dadosPLoja.put("PRECO_MAXIMO_VARE", precoMaxVare);
		
		final ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = produtoLojaSql.construirSqlStatement(dadosPLoja);
		// Pega o argumento para o statement
		final String[] argumentoSql = produtoLojaSql.argumentoStatement(dadosPLoja);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//produtoLojaSql.insertOrReplace(dadosPLoja);
						produtoLojaSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				produtoLojaSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						produtoLojaSql.update(dadosPLoja, "ID_AEAPLOJA = " + idPLoja);
					}
				});
			} else {
				produtoLojaSql.update(dadosPLoja, "ID_AEAPLOJA = " + idPLoja);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						produtoLojaSql.delete("ID_AEAPLOJA = " + idPLoja);
					}
				});
			} else {
				produtoLojaSql.delete("ID_AEAPLOJA = " + idPLoja);
			}
		}
	} // Fim PLoja
	
	
	private void importarRegistroPlanoPgto(String linha){
		
		Scanner scannerPlanoPgto = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerPlanoPgto.next();
		final String idPlPgt = scannerPlanoPgto.next();
		String idEmpre = scannerPlanoPgto.next();
		String dtAlt = scannerPlanoPgto.next();
		String codigo = scannerPlanoPgto.next();
		String descricao = scannerPlanoPgto.next();
		String ativo = scannerPlanoPgto.next();
		String atacVarejo = scannerPlanoPgto.next();
		String vistaPrazo = scannerPlanoPgto.next();
		String percDescAtac = scannerPlanoPgto.next();
		String percDescVare = scannerPlanoPgto.next();
		String descPromocao = scannerPlanoPgto.next();
		String juroMedioAtac = scannerPlanoPgto.next();
		String juroMedioVare = scannerPlanoPgto.next();
		String diasMedio = scannerPlanoPgto.next();
		
		scannerPlanoPgto = null;
		
		final ContentValues dadosPlanoPgto = new ContentValues();
		dadosPlanoPgto.put("ID_AEAPLPGT", idPlPgt);
		dadosPlanoPgto.put("ID_SMAEMPRE", idEmpre);
		dadosPlanoPgto.put("DT_ALT", dtAlt);
		dadosPlanoPgto.put("CODIGO", codigo);
		dadosPlanoPgto.put("DESCRICAO", descricao);
		dadosPlanoPgto.put("ATIVO", ativo);
		dadosPlanoPgto.put("ATAC_VAREJO", atacVarejo);
		dadosPlanoPgto.put("VISTA_PRAZO", vistaPrazo);
		dadosPlanoPgto.put("PERC_DESC_ATAC", percDescAtac);
		dadosPlanoPgto.put("PERC_DESC_VARE", percDescVare);
		dadosPlanoPgto.put("DESC_PROMOCAO", descPromocao);
		dadosPlanoPgto.put("JURO_MEDIO_ATAC", juroMedioAtac);
		dadosPlanoPgto.put("JURO_MEDIO_VARE", juroMedioVare);
		dadosPlanoPgto.put("DIAS_MEDIOS", diasMedio);
		
		final PlanoPagamentoSql pagamentoSql = new PlanoPagamentoSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = pagamentoSql.construirSqlStatement(dadosPlanoPgto);
		// Pega o argumento para o statement
		final String[] argumentoSql = pagamentoSql.argumentoStatement(dadosPlanoPgto);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//pagamentoSql.insertOrReplace(dadosPlanoPgto);
						pagamentoSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				pagamentoSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						pagamentoSql.update(dadosPlanoPgto, "ID_AEAPLPGT = " + idPlPgt);
					}
				});
			} else {
				pagamentoSql.update(dadosPlanoPgto, "ID_AEAPLPGT = " + idPlPgt);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						pagamentoSql.delete("ID_AEAPLPGT = " + idPlPgt);
					}
				});
			} else {
				pagamentoSql.delete("ID_AEAPLPGT = " + idPlPgt);
			}
		}
	} // Fim PlanoPgto
	
	
	private void importarRegistroParcela(String linha){
		
		Scanner scannerParcela = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerParcela.next();
		final String idParce = scannerParcela.next();
		String idEmpre = scannerParcela.next();
		String idFatur = scannerParcela.next();
		String idClifo = scannerParcela.next();
		String idTpDoc = scannerParcela.next();
		String idTpCob = scannerParcela.next();
		String idPorta = scannerParcela.next();
		String dtAlta = scannerParcela.next();
		String tipo = scannerParcela.next();
		String dtEmissao = scannerParcela.next();
		String dtVencimento = scannerParcela.next();
		String dtBaixa = scannerParcela.next();
		String parcela = scannerParcela.next();
		String vlParcela = scannerParcela.next();
		String vlTotalPago = scannerParcela.next();
		String vlRestante = scannerParcela.next();
		String vlJurosDiario = scannerParcela.next();
		String percDesconto = scannerParcela.next();
		String sequencial = scannerParcela.next();
		String numero = scannerParcela.next();
		String obs = scannerParcela.next();
		
		scannerParcela = null;
		
		final ContentValues dadosParcela = new ContentValues();
		dadosParcela.put("ID_RPAPARCE", idParce);
		dadosParcela.put("ID_SMAEMPRE", idEmpre);
		dadosParcela.put("ID_RPAFATUR", idFatur);
		dadosParcela.put("ID_CFACLIFO", idClifo);
		dadosParcela.put("ID_CFATPDOC", idTpDoc);
		dadosParcela.put("ID_CFATPCOB", idTpCob);
		dadosParcela.put("ID_CFAPORTA", idPorta);
		dadosParcela.put("DT_ALT", dtAlta);
		dadosParcela.put("TIPO", tipo);
		dadosParcela.put("DT_EMISSAO", dtEmissao.replace("0000-00-00", ""));
		dadosParcela.put("DT_VENCIMENTO", dtVencimento.replace("0000-00-00", ""));
		dadosParcela.put("DT_BAIXA", dtBaixa.replace("0000-00-00", ""));
		dadosParcela.put("PARCELA", parcela);
		dadosParcela.put("VL_PARCELA", vlParcela);
		dadosParcela.put("FC_VL_TOTAL_PAGO", vlTotalPago);
		dadosParcela.put("FC_VL_RESTANTE", vlRestante);
		dadosParcela.put("VL_JUROS_DIARIO", vlJurosDiario);
		dadosParcela.put("PERC_DESCONTO", percDesconto);
		dadosParcela.put("SEQUENCIAL", sequencial);
		dadosParcela.put("NUMERO", numero);
		dadosParcela.put("OBS", obs);
		
		final ParcelaSql parcelaSql = new ParcelaSql(context);
		
		// Pega o sql para passar para o statement
		final String sql = parcelaSql.construirSqlStatement(dadosParcela);
		// Pega o argumento para o statement
		final String[] argumentoSql = parcelaSql.argumentoStatement(dadosParcela);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						//parcelaSql.insertOrReplace(dadosParcela);
						parcelaSql.insertOrReplaceFast(sql, argumentoSql);
					}
				});
			} else {
				parcelaSql.insertOrReplaceFast(sql, argumentoSql);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						parcelaSql.update(dadosParcela, "ID_RPAPARCE = " + idParce);
					}
				});
			} else {
				parcelaSql.update(dadosParcela, "ID_RPAPARCE = " + idParce);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						parcelaSql.delete("ID_RPAPARCE = " + idParce);
					}
				});
			} else {
				parcelaSql.delete("ID_RPAPARCE = " + idParce);
			}
		}
	} // FIm Parcela
	
	
	private void importarRegistroDtAtualizacao(String linha){
		
		Scanner scannerDtAtualizacao = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerDtAtualizacao.next();
		String dtAtualizacao = scannerDtAtualizacao.next();
		
		scannerDtAtualizacao = null;
		
		ContentValues dadosAtualizacao = new ContentValues();
		dadosAtualizacao.put("DT_ATUALIZACAO", dtAtualizacao);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		UsuarioSQL usuarioSQL = new UsuarioSQL(context);
		
		// Checa se a finalidade eh atualizar
		if(FINALIDADE.equalsIgnoreCase("U")){
			usuarioSQL.update(dadosAtualizacao, "ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));
			
		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			usuarioSQL.delete("ID_SMAEMPRE = " + "ID_USUA = " + funcoes.getValorXml("CodigoUsuario"));
		}
		dadosAtualizacao = null;
	}


	private void importarRegistroTipoCobranca(String linha){
		
		Scanner scannerTpCobranca = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerTpCobranca.next();
		final String idTpCob = scannerTpCobranca.next();
		String dtAlt = scannerTpCobranca.next();
		String codigo = scannerTpCobranca.next();
		String descricao = scannerTpCobranca.next();
		String sigla = scannerTpCobranca.next();
		
		scannerTpCobranca = null;
		
		final ContentValues dadosTpCob = new ContentValues();
		dadosTpCob.put("ID_CFATPCOB", idTpCob);
		dadosTpCob.put("DT_ALT", dtAlt);
		dadosTpCob.put("CODIGO", codigo);
		dadosTpCob.put("DESCRICAO", descricao);
		dadosTpCob.put("SIGLA", sigla);
		
		final CobrancaSql cobrancaSql = new CobrancaSql(context);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cobrancaSql.insertOrReplace(dadosTpCob);
					}
				});
			} else {
				cobrancaSql.insertOrReplace(dadosTpCob);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cobrancaSql.update(dadosTpCob, "ID_CFATPCOB = " + idTpCob);
					}
				});
			} else {
				cobrancaSql.update(dadosTpCob, "ID_CFATPCOB = " + idTpCob);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						cobrancaSql.delete("ID_CFATPCOB = " + idTpCob);
					}
				});
			} else {
				cobrancaSql.delete("ID_CFATPCOB = " + idTpCob);
			}
		}
	} // Fim TipoCobranca
	
	
	private void importarRegistroSituacaoTributaria(String linha){
		
		Scanner scannerSituacaoTrib = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerSituacaoTrib.next();
		final String idCodSt = scannerSituacaoTrib.next();
		String dtAlt = scannerSituacaoTrib.next();
		String codigo = scannerSituacaoTrib.next();
		String descricao = scannerSituacaoTrib.next();
		String tipo = scannerSituacaoTrib.next();
		String origrem = scannerSituacaoTrib.next();
		
		scannerSituacaoTrib = null;
		
		final ContentValues dadosSituacaoTrib = new ContentValues();
		
		dadosSituacaoTrib.put("ID_AEACODST", idCodSt);
		dadosSituacaoTrib.put("DT_ALT", dtAlt);
		dadosSituacaoTrib.put("CODIGO", codigo);
		dadosSituacaoTrib.put("DESCRICAO", descricao);
		dadosSituacaoTrib.put("TIPO", tipo);
		dadosSituacaoTrib.put("ORIGEM", origrem);
		
		final SituacaoTributariaSql tributariaSql = new SituacaoTributariaSql(context);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tributariaSql.insertOrReplace(dadosSituacaoTrib);
					}
				});
			} else {
				tributariaSql.insertOrReplace(dadosSituacaoTrib);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tributariaSql.update(dadosSituacaoTrib, "ID_AEACODST = " + idCodSt);
					}
				});
			} else {
				tributariaSql.update(dadosSituacaoTrib, "ID_AEACODST = " + idCodSt);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						tributariaSql.delete("ID_AEACODST = " + idCodSt);
					}
				});
			} else {
				tributariaSql.delete("ID_AEACODST = " + idCodSt);
			}
		}
	}

	
	private void importarRegistroLocacao(String linha){
		
		Scanner scannerLocacao = new Scanner(linha).useDelimiter("\\|");
		
		String FINALIDADE = scannerLocacao.next();
		final String idLoces = scannerLocacao.next();
		String idEmpre = scannerLocacao.next();
		String dtAlt = scannerLocacao.next();
		String codigo = scannerLocacao.next();
		String descricao = scannerLocacao.next();
		String ativo = scannerLocacao.next();
		String tipoVenda = scannerLocacao.next();
		
		scannerLocacao = null;
		
		final ContentValues dadosLocacao = new ContentValues();
		
		dadosLocacao.put("ID_AEALOCES", idLoces);
		dadosLocacao.put("ID_SMAEMPRE", idEmpre);
		dadosLocacao.put("DT_ALT", dtAlt);
		dadosLocacao.put("CODIGO", codigo);
		dadosLocacao.put("DESCRICAO", descricao);
		dadosLocacao.put("ATIVO", ativo);
		dadosLocacao.put("TIPO_VENDA", tipoVenda);
		
		final LocacaoSql locacaoSql = new LocacaoSql(context);
		
		// Checa se a finalidade eh inserir
		if(FINALIDADE.equalsIgnoreCase("I")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						locacaoSql.insertOrReplace(dadosLocacao);
					}
				});
			} else {
				locacaoSql.insertOrReplace(dadosLocacao);
			}

		// Checa se a finalidade eh atualizar
		} else if(FINALIDADE.equalsIgnoreCase("U")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						locacaoSql.update(dadosLocacao, "ID_AEALOCES = " + idLoces);
					}
				});
			} else {
				locacaoSql.update(dadosLocacao, "ID_AEALOCES = " + idLoces);
			}

		// Checa se a finalidade eh deletar
		} else if(FINALIDADE.equalsIgnoreCase("D")){
			if (telaChamou != TELA_RECEPTOR_ALARME) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						locacaoSql.delete("ID_AEALOCES = " + idLoces);
					}
				});
			} else {
				locacaoSql.delete("ID_AEALOCES = " + idLoces);
			}
		}
	}
}
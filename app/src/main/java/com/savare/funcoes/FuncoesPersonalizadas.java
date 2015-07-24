package com.savare.funcoes;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jasypt.util.text.BasicTextEncryptor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.savare.R;
import com.savare.banco.ConexaoTask;
import com.savare.configuracao.ServicosWeb;

public class FuncoesPersonalizadas {

	private static final int ERRO = 0;
	private static final int INFORMACAO = 1;
	private static final int RAPIDO = 2;
	private static final String CHAVE_UNICA = "117ff1e4cfcafb0370b3517042bf90c9";
	public static final String ENVIAR_ORCAMENTO_SAVARE = "ENVIAR_ORCAMENTO_SAVARE";
	public static final String RECEBER_DADOS_SAVARE = "RECEBER_DADOS_SAVARE";
	public static final int MILISEGUNDOS = 0,
							SEGUNDOS = 1,
							MINUTOS = 2,
							HORAS = 3,
							DIAS = 4;
	AlertDialog.Builder menssagem;
	private Context context;
	public static final String NAO_ENCONTRADO = "nao encontrado";
	private int orientacaoTela;

	
	public FuncoesPersonalizadas(Context context) {
		super();
		//menssagem = new AlertDialog.Builder(context);
		this.context = context;
	}
	
	/**
	 * Criptografa qualquer texto.
	 * @param textoPleno - Tem que ser passado uma String com o valor dentro para ser criptografado.
	 * @return
	 */
	public String criptografaSenha(String textoPleno)  {
        
		String textoCrip = criptyDescripty(0, textoPleno, null);
		
        return textoCrip;
    }
	
		
	/**
	 * Descriptografa qualquer texto.
	 * @param textoCriptografado - Tem que ser passado uma String com o valor dentro para ser descriptografado.
	 * @return
	 */
	public String descriptografaSenha(String textoCriptografado)  {
        
		String textoPleno = criptyDescripty(1, null, textoCriptografado);
		
        return textoPleno;
    }
	
	private String criptyDescripty(int tipo, String senhaPlena, String textoCriptografado){
		
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(CHAVE_UNICA);
		String myEncryptedText = textEncryptor.encrypt(senhaPlena);
		String plainText = textEncryptor.decrypt(textoCriptografado);
		
		// Checa se o tipo eh para criptografar
		if(tipo == 0){
			return myEncryptedText;
		} else if(tipo == 1){
			return plainText;
		} else {
			return "";
		}
	}
	
	
	/**
	 * Metodo para abrir uma caixa de dialogo de acordo com o comando escolhido.
	 * Tem que ser passado por parametro um {@link ContentValues}, dentro tem que conter
	 * as seguintes chaves(key) com os seguintes valores:
	 * <p>*** ERRO *** INFORMACAO *** MENSAGEM RAPIDA *** </P>
	 * <p>"comando" -> 0 => Erro, 1 => Informacao, 2 => Menssagem Rapida
	 * <p>"tela" 	-> Contem o nome da tela(activity ou view)</p>
	 * <p>"mensagem"	-> Descricao da mensagem a ser exibida</p>
	 * <p>*** ERRO *** </P>
	 * <p>"dados"	-> Dados que eh para ser manipulado(insert, update, delete, select)</p>
	 * <p>"usuario"	-> Nome do usuario logado</p>
	 * <p>"empresa"	-> Nome da empresa</p>
	 * <p>"email"	-> Email do usuario</p>
	 * 
	 * @param contentValues
	 */
	public void menssagem(ContentValues contentValues){
		
		if(contentValues.getAsInteger("comando") == ERRO){
		
			menssagemEnviarErro(contentValues);
		
		} else if (contentValues.getAsInteger("comando") == INFORMACAO) {
			
			menssagemInformacao(contentValues);
			
		} else if (contentValues.getAsInteger("comando") == RAPIDO){
			
			menssagemRapida(contentValues);
			
		} else {
			
			menssagemRapida(contentValues);
		}
	}
	
	/**
	 * Metodo para abrir uma caixa de dialogo com o botão Enviar e Voltar.
	 * Tem que ser passado por parametro um {@link ContentValues}
	 * 
	 * @param contentValues
	 */
	private void menssagemEnviarErro(final ContentValues contentValues){
		AlertDialog.Builder telaMenssagem = new AlertDialog.Builder(this.context);
		
		telaMenssagem.setTitle(contentValues.getAsString("tela"));
		telaMenssagem.setMessage(contentValues.getAsString("mensagem"));
		
		// Opcao do botao positivo(OK)
		telaMenssagem.setPositiveButton(R.string.enviar, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ConexaoTask conexaoTask = new ConexaoTask(context);
				
				List<NameValuePair> valores = new ArrayList<NameValuePair>();
				valores.add(new BasicNameValuePair("mensagem", contentValues.getAsString("mensagem")));
				valores.add(new BasicNameValuePair("tela", contentValues.getAsString("tela")));
				valores.add(new BasicNameValuePair("dados", contentValues.getAsString("dados")));
				valores.add(new BasicNameValuePair("usuario", contentValues.getAsString("usuario")));
				valores.add(new BasicNameValuePair("empresa", contentValues.getAsString("empresa")));
				valores.add(new BasicNameValuePair("email", contentValues.getAsString("email")));
				
				conexaoTask = new ConexaoTask(context);
				conexaoTask.setComando(ServicosWeb.ERRO);
				conexaoTask.execute(valores);
				
			}
		});
		
		// Opcao do botao negativo(cancelar)
		telaMenssagem.setNegativeButton(R.string.voltar, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
		});
			
		telaMenssagem.create();
		telaMenssagem.show();
	}
	
	/**
	 * Metodo para aparecer uma menssagem rapida,
	 * basta passar por paramento o texto da menssagem. \n
	 * mensagem = Texto que deve aparecer na mensagem
	 * 
	 * @param contentValues
	 */
	private void menssagemRapida(ContentValues contentValues){
		
		Toast.makeText(this.context, contentValues.getAsString("mensagem"), Toast.LENGTH_SHORT).show();
		
	}
	
	
	private void menssagemInformacao(ContentValues contentValues){
		AlertDialog.Builder telaMenssagem = new AlertDialog.Builder(this.context);
		
		telaMenssagem.setTitle(contentValues.getAsString("tela"));
		telaMenssagem.setMessage(contentValues.getAsString("mensagem"));	
		telaMenssagem.setNegativeButton("OK", null);
		telaMenssagem.show();
	}
	
	
	
	/**
	 * Funcao para arrendondar os valores apos a virgula e 
	 * retornar um valor em formato padrao.
	 * @param valor
	 * @return - Retorna uma String com tres casas decimais.
	 */
	public String arredondarValor(Double valor){
		String str = String.valueOf(valor);
		
		return valorFormatado(str);
	}
	/**
	 * Funcao para arrendondar os valores apos a virgula e 
	 * retornar um valor em formato padrao.
	 * @param valor
	 * @return - Retorna uma String com tres casas decimais.
	 */
	public String arredondarValor(String valor){
		return valorFormatado(valor);
	}
	/**
	 * Funcao para arrendondar os valores apos a virgula e 
	 * retornar um valor em formato padrao.
	 * @param valor
	 * @return - Retorna uma String com tres casas decimais.
	 */
	public String arredondarValor(int valor){
		String str = String.valueOf(valor);
		
		return valorFormatado(str);
	}
	/**
	 * Funcao para arrendondar os valores apos a virgula e 
	 * retornar um valor em formato padrao.
	 * @param valor
	 * @return - Retorna uma String com tres casas decimais.
	 */
	public String arredondarValor(long valor){
		String str = String.valueOf(valor);
		
		return valorFormatado(str);
	}
	
	
	private String valorFormatado(String valor){
		String retorno = null;
		try {
			//Converte a string em double
			double vlDouble = Double.parseDouble(valor);
			
			//Pega o valor e faz arredondamento com tres casas decimais
			BigDecimal valorFinal = new BigDecimal(vlDouble).setScale(3, BigDecimal.ROUND_HALF_EVEN);
			
			//Crica uma vareavel colo o local
			Locale localPtBr = new Locale("pt", "BR");
			NumberFormat formatarNumero = NumberFormat.getInstance(localPtBr);
			
			int qtdCasasDecimais;
			
			// Checa se tem algum parametro de casas decimais
			if(getValorXml("CasasDecimais") != null && getValorXml("CasasDecimais").length() > 0){
				qtdCasasDecimais = Integer.valueOf(getValorXml("CasasDecimais"));
			}else {
				qtdCasasDecimais = 3;
			}
			
			// Informa que os casas decimais eh com minimo de 3 casas
			formatarNumero.setMinimumFractionDigits(qtdCasasDecimais);
			formatarNumero.setMinimumIntegerDigits(1);
						
			retorno = formatarNumero.format(valorFinal.doubleValue());
			
		} catch (Exception e) {

			try {
				valor = valor.replace(".", "").replace(",", "");
				double vlDouble = Double.parseDouble(valor) / 1000;
				//Pega o valor e faz arredondamento com tres casas decimais
				BigDecimal valorFinal = new BigDecimal(vlDouble).setScale(3, BigDecimal.ROUND_HALF_EVEN);
				retorno = String.valueOf(valorFinal.doubleValue());
				
			} catch (Exception e2) {
				retorno = "0.000";
			}
			
		}
		return retorno;
	}
	
	
	/**
	 * Tirar a formatacao do padr�o brasileiro de numeros.
	 * Por exemplo: 
	 * R$ 1.025,35 retorna 1025.35
	 * 1.025,30 retorna 1025.3
	 * 
	 * @param valor
	 * @return
	 */
	public double desformatarValor(String valor){
		double valorDesformatado = 0;
		
		try {
			//Crica uma vareavel colo o local
			Locale localPtBr = new Locale("pt", "BR");
			NumberFormat formatarNumero = NumberFormat.getInstance(localPtBr);
			// Informa que os casas decimais eh com minimo de 3 casas
			formatarNumero.setMinimumFractionDigits(3);
			formatarNumero.setMinimumIntegerDigits(1);
			
			valorDesformatado = formatarNumero.parse(valor).doubleValue();
			
		} catch (Exception e) {
			e.getMessage();
		}
		return valorDesformatado;
	}
	
	
	/**
	 * Funcao para retorno um determinado valor em formato monet�rio.
	 * 
	 * @param s - CharSequence
	 * @return Retona uma @String
	 */
	public String mascaraMonetaria(CharSequence s){

		String str = s.toString();
		// Verifica se j� existe a m�scara no texto.
		boolean hasMask = ((str.indexOf("R$") > -1 || str.indexOf("$") > -1) && (str.indexOf(".") > -1 || str.indexOf(",") > -1));
		
		// Verificamos se existe m�scara
		if (hasMask) {
			// Retiramos a m�scara.
			str = str.replaceAll("[R$]", "").replaceAll("[,]", "").replaceAll("[.]", "");
		}
		// Pega o formato numerio configurado no aparelho
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		
		try {
			// Transformamos o n�mero que est� escrito no EditText em monet�rio.
			str = nf.format(Double.parseDouble(str) / 100);
		} catch (NumberFormatException e) {
			str = "";
		}
		
		return str;
	}
	

	/**
	 * Funcao para salvar um valor em um xml na pasta padrao da aplicacao.
	 * @param campo - Nome do campos que eh para ser salvo.
	 * @param texto - Valor do campo, o conteudo.
	 */
	public void setValorXml(String campo, String texto) {
		 
		// Cria ou abre.
		SharedPreferences prefs = this.context.getSharedPreferences("prefSavare_1", Context.MODE_PRIVATE);
 
		// Precisamos utilizar um editor para alterar Shared Preferences.
		Editor ed = prefs.edit();
 
		// salvando informações de acordo com o tipo
		ed.putString(campo, texto);
 
		// Grava efetivamente as alterações.
		ed.commit();
	} // Fim do salvarXml
	
	/**
	 * Funcao para pegar o valor de um determinado campo no arquivo XML.
	 * Este XML esta localizado na pasta padrão da aplicacao
	 * 
	 * @param campo - Nome do campos que esta salvo no arquivo XML
	 * @return - Retorna uma String com o valor do campos
	 */
	public String getValorXml(String campo) {
		// Acesso às Shared Preferences usando o nome definido.
		SharedPreferences prefs = this.context.getSharedPreferences("prefSavare_1", Context.MODE_PRIVATE);
 
		// Acesso às informações de acordo com o tipo.
		String texto = prefs.getString(campo, NAO_ENCONTRADO);
 
		// Formata um string com todo o conteúdo separado por linha.
		return texto;
	} // FIm do selecionaValorXml
	
	
	/**
	 * Funcao para criar uma pasta o diretorio raiz do aparelho.
	 * 
	 * @param nomeArquivo
	 * @return
	 */
	public boolean criarPasta(String nomeArquivo){
		boolean retorno = false;
		try {
			File diretorio = context.getDir(nomeArquivo, Context.MODE_WORLD_WRITEABLE);
			if( diretorio.mkdirs() ){
				retorno = true;
			}
			
		} catch (Exception e) {
			
			ContentValues dadosMensagem = new ContentValues();
			dadosMensagem.put("comando", 0);
			dadosMensagem.put("tela", "FuncoesPersonalizadas");
			dadosMensagem.put("mensagem", "Não foi possível criar a pasta. \n" + e.getMessage());
			dadosMensagem.put("dados", e.toString());
			dadosMensagem.put("usuario", getValorXml("Usuario"));
			dadosMensagem.put("empresa", getValorXml("Empresa"));
			dadosMensagem.put("email", getValorXml("Email"));
			
			menssagem(dadosMensagem);
		}
		
		return retorno;
	}
	
	
	public boolean existeConexaoInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo netInfo = connectivity.getActiveNetworkInfo();

			// Se n�o existe nenhum tipo de conex�o retorna false
			if (netInfo == null) {
				return false;
			}

			int netType = netInfo.getType();

			// Verifica se a conex�o � do tipo WiFi ou Mobile e
			// retorna true se estiver conectado ou false em
			// caso contr�rio
			if (netType == ConnectivityManager.TYPE_WIFI || netType == ConnectivityManager.TYPE_MOBILE) {
				return netInfo.isConnected();

			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	public void criarAlarmeEnviarOrcamento(){
		
		// Checa se o alarme nao foi criado
		boolean alarmeEnviarDesativado =  (PendingIntent.getBroadcast(context, 0, new Intent(ENVIAR_ORCAMENTO_SAVARE), PendingIntent.FLAG_NO_CREATE) == null);
		boolean alarmeReceberDesativado = (PendingIntent.getBroadcast(context, 0, new Intent(RECEBER_DADOS_SAVARE), PendingIntent.FLAG_NO_CREATE) == null);
		
		// Checa se esta configurado para enviar os orcamentos automaticos
		if( (getValorXml("EnviarAutomatico").equalsIgnoreCase("S")) || (getValorXml("EnviarAutomatico") == null) ){
		
			// Checa se o alarme de envio de orcamento esta desativado
			if(alarmeEnviarDesativado){
				Log.i("Script", "Novo alarme Enviar");
				
				// Cria a intent com identificacao do alarme
				Intent intent = new Intent(ENVIAR_ORCAMENTO_SAVARE);
				PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
				
				Calendar tempoInicio = Calendar.getInstance();
				// Pega a hora atual do sistema em milesegundo
				tempoInicio.setTimeInMillis(System.currentTimeMillis());
				// Adiciona mais alguns segundo para executar o alarme depois de alguns segundo que esta
				// Activity for abaerta
				tempoInicio.add(Calendar.SECOND, 3);
				// Cria um intervalo de quanto em quanto tempo o alarme vai repetir
				long intervalo = 1 * 1000; // 5 Minutos
				
				AlarmManager alarmeEnviarOrcamento = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarmeEnviarOrcamento.setRepeating(AlarmManager.RTC_WAKEUP, tempoInicio.getTimeInMillis(), intervalo, alarmIntent);
			}
		} else {
			// Checa se o alarme foi cria para enviar a desativacao
			if(!alarmeEnviarDesativado){
				Intent intentCancelar = new Intent(ENVIAR_ORCAMENTO_SAVARE);
				PendingIntent alarmeIntent = PendingIntent.getBroadcast(context, 0, intentCancelar, 0);
				
				AlarmManager alarme = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarme.cancel(alarmeIntent);
				Log.i("Script", "Desativado alarme Enviar");
			}
		}
		
		// Checa se esta configurado para receber dados automaticos
		if( (getValorXml("ReceberAutomatico").equalsIgnoreCase("S")) || (getValorXml("ReceberAutomatico") == null) ){
			
			// Checa se o alarme de recebimento de dados esta desativado
			if(alarmeReceberDesativado){
				Log.i("Script", "Novo alarme Receber");
				
				Intent intent = new Intent(RECEBER_DADOS_SAVARE);
				PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
				
				Calendar tempoInicio = Calendar.getInstance();
				// Pega a hora atual do sistema em milesegundo
				tempoInicio.setTimeInMillis(System.currentTimeMillis());
				// Adiciona mais alguns segundo para executar o alarme depois de alguns segundo que esta
				// Activity for abaerta
				tempoInicio.add(Calendar.SECOND, 3);
				
				//long intervalo = 300 * 1000;
				
				AlarmManager alarmeReceberOrcamento = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarmeReceberOrcamento.setRepeating(AlarmManager.RTC_WAKEUP, tempoInicio.getTimeInMillis(), 2 * 1000, alarmIntent);
			}
		} else {
			// Checa se o alarme foi criado para podermos desativalo
			if(!alarmeReceberDesativado){
				Log.i("Script", "Desativado alarme Receber");
				Intent intentCancelar = new Intent(RECEBER_DADOS_SAVARE);
				PendingIntent alarmeIntent = PendingIntent.getBroadcast(context, 0, intentCancelar, 0);
				
				AlarmManager alarme = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarme.cancel(alarmeIntent);
			}
		}
	}
	
	
	public void bloqueiaOrientacaoTela() {
		// Pega a orientacao atual da tela
		orientacaoTela = context.getResources().getConfiguration().orientation;

		// Checa em qual orientacao esta atualmente para bloquear
		if (orientacaoTela == Configuration.ORIENTATION_PORTRAIT) {
			
			((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	public void desbloqueiaOrientacaoTela() {
		if (orientacaoTela > 0) {
			((Activity) context).setRequestedOrientation(orientacaoTela);
		} else {
			((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	} // Fim desbloqueiaOrientacaoTela
	
	
	/**
	 * Formata data e a hora de acordo com o padrão brasileiro dd/MM/yyyy.
	 * Tem que ser passado por parametro o seguinte formato:
	 * yyyy-MM-dd hh:mm:ss
	 * 
	 * @param dataHora - yyyy-MM-dd hh:mm:ss
	 * @return - dd/MM/yyyy hh:mm:ss
	 */
	public String formataDataHora(String dataHora){
		String dataFormatada = "";
		
		try{
			if(dataHora != null){
				Scanner scannerParametro = new Scanner(dataHora.replace(" ", "-").replace(":", "-")).useDelimiter("\\-");
				int ano = scannerParametro.nextInt();
				int mes = scannerParametro.nextInt();
				int dia = scannerParametro.nextInt();
				int hora = scannerParametro.nextInt();
				int minuto = scannerParametro.nextInt();
				int segundo = scannerParametro.nextInt();
				
				// Instancia a classe de calendario
				Calendar calendario = Calendar.getInstance();
				calendario.set(ano, mes -1, dia, hora, minuto, segundo);
				// Cria um formato para data e hora
				DateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				// Salva a data e hora passada por parametro com o novo formato
				dataFormatada = dataFormat.format(calendario.getTime());
			}
		}catch(Exception e){
			String s = e.getMessage();
			int i = s.length();
		}
		return dataFormatada;
	}
	
	
	/**
	 * Formata apenas a data de acordo com o padr�o brasileiro dd/MM/yyyy.
	 * Tem que ser passado por parametro a data no seguinte formato:
	 * yyyy-MM-dd.
	 * 
	 * @param data - yyyy-MM-dd
	 * @return dd/MM/yyyy
	 */
	public String formataData(String data){
		String dataFormatada = "";
		
		if(data != null){
			Scanner scannerParametro = new Scanner(data).useDelimiter("\\-");
			int ano = scannerParametro.nextInt();
			int mes = scannerParametro.nextInt();
			int dia = scannerParametro.nextInt();
			
			Calendar calendario = Calendar.getInstance();
			calendario.set(ano, mes -1, dia);
			
			DateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");
			
			dataFormatada = dataFormat.format(calendario.getTime());
		}
		return dataFormatada;
	}
	
	
	/**
	 * Tira o formato brasileiro da data e hora.
	 * Tem que ser passado por paramento no formato brasileiro dd/MM/yyyy hh:mm:ss
	 * Retorna o seguinte formato:
	 * yyyy-MM-dd hh:mm:ss
	 *  
	 * @param dataHora dd/MM/yyyy hh:mm:ss (06/04/2015 08:32:15)
	 * @return - yyyy-MM-dd hh:mm:ss
	 */
	public String desformataDataHora(String dataHora){
		String dataFormatada = "";
		
		if(dataHora != null){
			Scanner scannerParametro = new Scanner(dataHora).useDelimiter("\\/");
			
			int dia = scannerParametro.nextInt();
			int mes = scannerParametro.nextInt();
			int ano = scannerParametro.nextInt();
			// Inseri um novo delimitador para pegar a hora, minuto e segundo
			scannerParametro.useDelimiter("\\:");
			int hora = scannerParametro.nextInt();
			int minuto = scannerParametro.nextInt();
			int segundo = scannerParametro.nextInt();
		
			// Instancia a classe de calendario
			Calendar calendario = Calendar.getInstance();
			calendario.set(ano, mes -1, dia, hora, minuto, segundo);
		}
		DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		dataFormatada = dataFormat.format(dataHora);
		
		return dataFormatada;
	}
	
	
	public String desformataData(String dataHora){
		String dataFormatada = "";
		
		if(dataHora != null){
			Scanner scannerParametro = new Scanner(dataHora).useDelimiter("\\/");
			
			int dia = scannerParametro.nextInt();
			int mes = scannerParametro.nextInt();
			int ano = scannerParametro.nextInt();
		
			// Instancia a classe de calendario
			Calendar calendario = Calendar.getInstance();
			calendario.set(ano, mes -1, dia);
		}
		
		DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		dataFormatada = dataFormat.format(dataHora);
		
		return dataFormatada;
	} // desformataData
	
	public static String unmask(String s) {
        return s.replaceAll("[.]", "").replaceAll("[-]", "")
                .replaceAll("[/]", "").replaceAll("[(]", "")
                .replaceAll("[)]", "");
    }
 
    public static TextWatcher insertMascara(final String mask, final EditText ediTxt) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";
 
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                String str = FuncoesPersonalizadas.unmask(s.toString());
                String mascara = "";
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }
                int i = 0;
                for (char m : mask.toCharArray()) {
                    if (m != '#' && str.length() > old.length()) {
                        mascara += m;
                        continue;
                    }
                    try {
                        mascara += str.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                ediTxt.setText(mascara);
                ediTxt.setSelection(mascara.length());
            }
 
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
 
            public void afterTextChanged(Editable s) {
            }
        };
    } // Fim insertMascara


	/**
	 *
	 * @param TIPO - MILISEGUNDOS, MINUTOS, HORAS, DIAS
	 * @param dataHoraInicial - dd/mm/yyyy HH:mm:ss
	 * @param dataHoraFinal - dd/mm/yyyy HH:mm:ss
	 * @return
	 */
	public String diferencaEntreDataHora(int TIPO, String dataHoraInicial, String dataHoraFinal){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");

		String retorno = "";
		try {
			Date date1 = sdf.parse(dataHoraInicial);
			Date date2 = sdf.parse(dataHoraFinal);
			long differenceMilliSeconds = date2.getTime() - date1.getTime();

			if (TIPO == MILISEGUNDOS){
				retorno = ""+differenceMilliSeconds;
			} else if (TIPO == SEGUNDOS){
				retorno = "" + (differenceMilliSeconds/1000);
			} else if (TIPO == MINUTOS){
				retorno = "" + (differenceMilliSeconds/1000/60);
			} else if (TIPO == HORAS) {
				retorno = "" + (differenceMilliSeconds/1000/60/60);
			} else if (TIPO == DIAS){
				retorno = "" + (differenceMilliSeconds/1000/60/60/24);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return retorno;
	} // Fim diferencaEntreDataHora

	public String tratamentoErroBancoDados(String erro){
		// Tratamento para erro de registro unico
		if(erro.toLowerCase().contains("code 2067")){
			erro = context.getResources().getString(R.string.erro_sqlite_code_2067) + "\n" + erro + "\n";
		}
		// Tratamento de erro para restrincoes em trigger
		if(erro.toLowerCase().contains("code 1811")){
			erro = context.getResources().getString(R.string.erro_sqlite_code_1811) + "\n" + erro + "\n";
		}
		// Tratamento de erro para campo obrigatorio (not null)
		if(erro.toLowerCase().contains("code 1299")){
			erro = context.getResources().getString(R.string.erro_sqlite_code_1299) + "\n" + erro + "\n";
		}

		return erro;
	}
} // Fecha classe

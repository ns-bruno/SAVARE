package com.savare.funcoes;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.banco.ConexaoTask;
import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.beans.EmpresaBeans;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.configuracao.ServicosWeb;
import com.savare.funcoes.rotinas.EmpresaRotinas;
import com.savare.funcoes.rotinas.receptor.ReceptorAlarmeEnviarOrcamentoBroadcastRotinas;
import com.savare.funcoes.rotinas.receptor.ReceptorAlarmeReceberDadosBroadcastRotinas;
import com.savare.sincronizacao.SavareAutenticadorService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jasypt.util.text.BasicTextEncryptor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.content.Context.ACCOUNT_SERVICE;

public class FuncoesPersonalizadas {

	private static final int ERRO = 0;
	private static final int INFORMACAO = 1;
	private static final int RAPIDO = 2;
	private static final String CHAVE_UNICA = "117ff1e4cfcafb0370b3517042bf90c9";
	private static final String TAG = "SAVARE";
	public static final String TAG_ENVIANDO_DADOS = "EnviandoDados",
							   TAG_ENVIAR_AUTOMATICO = "EnviarAutomatico",
							   TAG_ENVIAR_INSTANTANEAMENTE = "EnviarInstantaneamente",
							   TAG_RECEBER_AUTOMATICO = "ReceberAutomatico",
							   TAG_RECEBENDO_DADOS = "RecebendoDados",
							   TAG_IMAGEM_PRODUTO = "ImagemProduto",
							   TAG_EMPRESA_ATIVA = "EmpresaAtiva",
							   TAG_CODIGO_USUARIO = "CodigoUsuario",
							   TAG_TIPO_FUNCIONARIO = "TipoFuncionario",
							   TAG_USUARIO = "Usuario",
							   TAG_SENHA_USUARIO = "SenhaUsuario",
							   TAG_ABRIU_PRIMEIRA_VEZ = "AbriuAppPriveiraVez",
							   TAG_CNPJ_EMPRESA = "CnpjEmpresa",
							   TAG_CODIGO_EMPRESA = "CodigoEmpresa",
							   TAG_ID_CFACLIFO_VISTA = "ID_CFACLIFO_VISTA",
							   TAG_CHAVE_FUNCIONARIO = "ChaveFuncionario",
							   TAG_SALVA_PEDIDO_PDF = "SalvaPedidoPdf",
							   TAG_DESCRICAO_DISPOSITIVO = "DescricaoDispositivo",
							   TAG_UUID_DISPOSITIVO = "UuidDispositivo",
							   TAG_PESQUISA_PRODUTO_ESTOQUE = "PesquisaProdutoComEstoque";
	public static final String SIM = "S", NAO = "N";
	public static final String NOTIFICATION_FILE = "FileNotification";
	public static final int MILISEGUNDOS = 0,
							SEGUNDOS = 1,
							MINUTOS = 2,
							HORAS = 3,
							DIAS = 4;
	public static final String MASCARA_CPF = "###.###.###-##";
	public static final String MASCARA_CNPJ = "##.###.###/####-##";
	AlertDialog.Builder menssagem;
	private Context context;
	public static final String NAO_ENCONTRADO = "nao encontrado";
	private int orientacaoTela;
	// Cria uma notificacao para ser manipulado
	NotificationManager notificationManager;
	NotificationCompat.BigTextStyle bigTextStyle;
	NotificationCompat.Builder mBuilder;

	
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
		
		//SuperToast.create(context, contentValues.getAsString("mensagem"), SuperToast.Duration.LONG, Style.getStyle(Style.GRAY, SuperToast.Animations.POPUP)).show();
		SuperActivityToast.create(context, contentValues.getAsString("mensagem"), Style.DURATION_LONG)
				.setTextColor(Color.WHITE)
				.setColor(Color.GRAY)
				.setAnimations(Style.ANIMATIONS_POP)
				.show();
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
			//BigDecimal valorFinal = new BigDecimal(vlDouble).setScale(3, BigDecimal.ROUND_HALF_EVEN);
			BigDecimal valorFinal = new BigDecimal(vlDouble);

			//Crica uma vareavel colo o local
			Locale localPtBr = new Locale("pt", "BR");
			NumberFormat formatarNumero = NumberFormat.getInstance(localPtBr);
			
			int qtdCasasDecimais;
			
			// Checa se tem algum parametro de casas decimais
			if ((getValorXml("CasasDecimais") != null) && (getValorXml("CasasDecimais").length() > 0) && (!getValorXml("CasasDecimais").equalsIgnoreCase(NAO_ENCONTRADO))){
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
	 * Tirar a formatacao do padrao brasileiro de numeros.
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

			// Se nao existe nenhum tipo de conexao retorna false
			if (netInfo == null) {
				return false;
			}

			int netType = netInfo.getType();

			// Verifica se a conexao eh do tipo WiFi ou Mobile e
			// retorna true se estiver conectado ou false em
			// caso contrario
			if (netType == ConnectivityManager.TYPE_WIFI || netType == ConnectivityManager.TYPE_MOBILE) {
				return netInfo.isConnected();

			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void criarAlarmeEnviarAutomatico(Boolean ativarEnvioAutomatico){
		Intent intentAlarme = new Intent(context, ReceptorAlarmeEnviarOrcamentoBroadcastRotinas.class);
		intentAlarme.setAction(ReceptorAlarmeEnviarOrcamentoBroadcastRotinas.TAG_ENVIAR_ORCAMENTO_SAVARE);
        boolean alarmeEnviarDesativado =  (PendingIntent.getBroadcast(context, ReceptorAlarmeEnviarOrcamentoBroadcastRotinas.TAG_ID_ALARME_ENVIAR, intentAlarme, PendingIntent.FLAG_NO_CREATE) == null);

        // Checa se eh para criar o alarme de envio automatico
        if ((getValorXml(TAG_ENVIAR_AUTOMATICO).equalsIgnoreCase("S")) && ((getValorXml(TAG_ENVIAR_INSTANTANEAMENTE).equalsIgnoreCase("N")) || (getValorXml(TAG_ENVIAR_INSTANTANEAMENTE).equalsIgnoreCase(NAO_ENCONTRADO))) &&
                (ativarEnvioAutomatico)){
            // Checa se o alarme de envio de orcamento esta desativado
            if(alarmeEnviarDesativado){
                // Cria a intent com identificacao do alarme
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, ReceptorAlarmeEnviarOrcamentoBroadcastRotinas.TAG_ID_ALARME_ENVIAR, intentAlarme, 0);

                Calendar tempoInicio = Calendar.getInstance();
                // Pega a hora atual do sistema em milesegundo
                tempoInicio.setTimeInMillis(System.currentTimeMillis());
                // Adiciona mais alguns segundo para executar o alarme depois de alguns segundo que esta Activity for abaerta
                tempoInicio.add(Calendar.SECOND, 30);
                // Cria um intervalo de quanto em quanto tempo o alarme vai repetir
                long intervalo = 1200 * 1000; // 20 Minutos

                AlarmManager alarmeEnviarOrcamento = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmeEnviarOrcamento.setRepeating(AlarmManager.RTC_WAKEUP, tempoInicio.getTimeInMillis(), intervalo, alarmIntent);
            }
        } else {
        	if (alarmeEnviarDesativado == false) {
				PendingIntent alarmeIntent = PendingIntent.getBroadcast(context, ReceptorAlarmeEnviarOrcamentoBroadcastRotinas.TAG_ID_ALARME_ENVIAR, intentAlarme, PendingIntent.FLAG_CANCEL_CURRENT);

				AlarmManager alarme = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarme.cancel(alarmeIntent);
				alarmeIntent.cancel();
			}
        }
    }


    public void criarAlarmeReceberAutomatico(Boolean ativarRecebimentoAutomatico){
		Intent intentAlarme = new Intent(context, ReceptorAlarmeReceberDadosBroadcastRotinas.class);
		intentAlarme.setAction(ReceptorAlarmeReceberDadosBroadcastRotinas.TAG_RECEBER_DADOS_SAVARE);
        boolean alarmeReceberDesativado = (PendingIntent.getBroadcast(context, ReceptorAlarmeReceberDadosBroadcastRotinas.TAG_ID_ALARME_RECEBER, intentAlarme, PendingIntent.FLAG_NO_CREATE) == null);

        // Checa se eh para criar o alarme de envio automatico
        if ((getValorXml(TAG_RECEBER_AUTOMATICO).equalsIgnoreCase("S")) && (ativarRecebimentoAutomatico)){
            // Checa se o alarme de recebimento de dados esta desativado
            if(alarmeReceberDesativado){
                PendingIntent alarmPending = PendingIntent.getBroadcast(context, ReceptorAlarmeReceberDadosBroadcastRotinas.TAG_ID_ALARME_RECEBER, intentAlarme, 0);

                Calendar tempoInicio = Calendar.getInstance();
                // Pega a hora atual do sistema em milesegundo
                tempoInicio.setTimeInMillis(System.currentTimeMillis());
                // Adiciona mais alguns segundo para executar o alarme depois de alguns segundo que esta Activity for abaerta
                tempoInicio.add(Calendar.SECOND, 30);

                long intervalo = 3600 * 1000; // 60 Minutos

                AlarmManager alarmeReceberOrcamento = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmeReceberOrcamento.setRepeating(AlarmManager.RTC_WAKEUP, tempoInicio.getTimeInMillis(), intervalo, alarmPending);
            }
        } else {
        	if (alarmeReceberDesativado == false) {
				PendingIntent alarmeIntent = PendingIntent.getBroadcast(context, ReceptorAlarmeReceberDadosBroadcastRotinas.TAG_ID_ALARME_RECEBER, intentAlarme, PendingIntent.FLAG_CANCEL_CURRENT);

				AlarmManager alarme = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarme.cancel(alarmeIntent);
				alarmeIntent.cancel();
			}
        }
    }
	
	
	@SuppressLint("SourceLockedOrientationActivity")
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
				Scanner scannerParametro = new Scanner(dataHora.replace(" ", "-").replace(":", "-").replace(".", "-")).useDelimiter("\\-");
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
		
		if((data != null) && (data.contains("-"))){
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);

		String retorno = "0";
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

	/**
	 * Se o retorno for positivo a dataFinal eh maior que a dataInicial.
	 * dataFital - @dataInicial
	 * @param TIPO - MILISEGUNDOS, MINUTOS, HORAS, DIAS
	 * @param dataInicial - dd/mm/yyyy
	 * @param dataFinal - dd/mm/yyyy
	 * @return
	 */
	public String diferencaEntreData(int TIPO, String dataInicial, String dataFinal){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

		String retorno = "0";
		try {
			Date date1 = sdf.parse(dataInicial);
			Date date2 = sdf.parse(dataFinal);
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

	public String adicionaDataHora(int tipo, String dataHora, int quantidade){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);

		String retorno = "0";
		try {
			Date date1 = sdf.parse(dataHora);
			long differenceMilliSeconds = date1.getTime();

			if (tipo == MILISEGUNDOS){
				date1.setTime(differenceMilliSeconds + quantidade );
			} else if (tipo == SEGUNDOS){
				date1.setTime(differenceMilliSeconds + (quantidade * 1000));
			} else if (tipo == MINUTOS){
				date1.setTime(differenceMilliSeconds + (quantidade *1000 * 60));
			} else if (tipo == HORAS) {
				date1.setTime(differenceMilliSeconds + ( quantidade * 1000 * 60  * 60));
			} else if (tipo == DIAS){
				date1.setTime(differenceMilliSeconds + (1000 * 60 * 60 * 24));
			}
			retorno = date1.toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return retorno;
	}

	public String adicionaData(int tipo, String dataHora, int quantidade){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

		String retorno = "0";
		try {
			Date date1 = sdf.parse(dataHora);
			long differenceMilliSeconds = date1.getTime();

			if (tipo == MILISEGUNDOS){
				date1.setTime(differenceMilliSeconds + quantidade );
			} else if (tipo == SEGUNDOS){
				date1.setTime(differenceMilliSeconds + (quantidade * 1000));
			} else if (tipo == MINUTOS){
				date1.setTime(differenceMilliSeconds + (quantidade *1000 * 60));
			} else if (tipo == HORAS) {
				date1.setTime(differenceMilliSeconds + ( quantidade * 1000 * 60  * 60));
			} else if (tipo == DIAS){
				date1.setTime(differenceMilliSeconds + (1000 * 60 * 60 * 24));
			}
			retorno = sdf.format(date1);
			//retorno = date1.toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return retorno;
	}

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

		// Tratamento de erro para campo obrigatorio (not null)
		if(erro.toLowerCase().contains("no permission for insert")){
			erro = context.getResources().getString(R.string.erro_nao_tem_permissao_inserir) + "\n" + erro + "\n";
		}

		if ((erro.toLowerCase().contains("no such table")) || (erro.toLowerCase().contains("no such column"))){
			erro = context.getResources().getString(R.string.nao_existe_tabela_banco_dados)  + "\n" + erro + "\n" + context.getResources().getString(R.string.vamos_executar_processo_criar_tabelas);
			try {
				ConexaoBancoDeDados conexaoBancoDeDados = new ConexaoBancoDeDados(context, VersionUtils.getVersionCode(context));
				// Pega o banco de dados do SAVARE
				SQLiteDatabase bancoDados = conexaoBancoDeDados.abrirBanco();
				// Executa o onCreate para criar todas as tabelas do banco de dados
				conexaoBancoDeDados.onCreate(bancoDados);

			} catch (PackageManager.NameNotFoundException e) {
				erro = e.getMessage() + "\n" + erro;
			}
		}

		if (erro.toLowerCase().contains("timeoutexception")){
			erro = context.getResources().getString(R.string.demorou_demais_servidor_webservice_responter_internet_lenta) + "\n" + erro + "\n";
		}

		return erro;
	} // Fim tratamentoErroBancoDados

	/**
	 * Valida o CNPJ, retornando apenas se é um CNPJ válido ou não.
	 * Pode ser passado apenas numero ou com a formatação padrão do Brasil.
	 *
	 * @param CNPJ
	 * @return
	 */
	public boolean validaCNPJ(String CNPJ) {
		CNPJ = CNPJ.replace(".", "").replace("-","").replace("/", "");
		// considera-se erro CNPJ's formados por uma sequencia de numeros iguais
		if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111")
				|| CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333")
				|| CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555")
				|| CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777")
				|| CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999")
				|| (CNPJ.length() != 14)) {
			return (false);
		}

		char verificador13, verificador14;
		int soma, controle, resto, num, valores_defenidos;

		// "try" - protege o código para eventuais erros de conversao de tipo (int)
		try {
			// Calculo do 1o. Digito Verificador
			soma = 0;
			valores_defenidos = 2;
			for (controle = 11; controle >= 0; controle--) {
				// converte o i-ésimo caractere do CNPJ em um número:
				// por exemplo, transforma o caractere '0' no inteiro 0
				// (48 eh a posição de '0' na tabela ASCII)
				num = (int) (CNPJ.charAt(controle) - 48);
				soma = soma + (num * valores_defenidos);
				valores_defenidos = valores_defenidos + 1;
				if (valores_defenidos == 10) {
					valores_defenidos = 2;
				}
			}

			resto = soma % 11;
			if ((resto == 0) || (resto == 1)) {
				verificador13 = '0';
			} else {
				verificador13 = (char) ((11 - resto) + 48);
			}

			// Calculo do 2o. Digito Verificador
			soma = 0;
			valores_defenidos = 2;
			for (controle = 12; controle >= 0; controle--) {
				num = (int) (CNPJ.charAt(controle) - 48);
				soma = soma + (num * valores_defenidos);
				valores_defenidos = valores_defenidos + 1;
				if (valores_defenidos == 10) {
					valores_defenidos = 2;
				}
			}

			resto = soma % 11;
			if ((resto == 0) || (resto == 1)) {
				verificador14 = '0';
			} else {
				verificador14 = (char) ((11 - resto) + 48);
			}

			// Verifica se os dígitos calculados conferem com os dígitos informados.
			if ((verificador13 == CNPJ.charAt(12)) && (verificador14 == CNPJ.charAt(13))) {
				return (true);
			} else {
				return (false);
			}
		} catch (InputMismatchException erro) {
			return (false);
		}
	}

	/**
	 * Valida o CPF, retornando apenas se é um CPF válido ou não.
	 * Pode ser passado apenas numero ou com a formatação padrão do Brasil.
	 *
	 * @param CPF
	 * @return
	 */
	public boolean validaCPF(String CPF) {
		CPF = CPF.replace(".", "").replace("-","");
		// considera-se erro CPF's formados por uma sequencia de numeros iguais
		if (CPF.equals("00000000000") || CPF.equals("11111111111")
				|| CPF.equals("22222222222") || CPF.equals("33333333333")
				|| CPF.equals("44444444444") || CPF.equals("55555555555")
				|| CPF.equals("66666666666") || CPF.equals("77777777777")
				|| CPF.equals("88888888888") || CPF.equals("99999999999")
				|| (CPF.length() != 11)) {
			return (false);
		}

		char verificador10, verificador11;
		int soma, controle, resto, num, valores_defenidos;

		// "try" - protege o codigo para eventuais erros de conversao de tipo (int)
		try {
			// Calculo do 1o. Digito Verificador
			soma = 0;
			valores_defenidos = 10;
			for (controle = 0; controle < 9; controle++) {
				// converte o i-esimo caractere do CPF em um numero:
				// por exemplo, transforma o caractere '0' no inteiro 0
				// (48 eh a posicao de '0' na tabela ASCII)
				num = (int) (CPF.charAt(controle) - 48);
				soma = soma + (num * valores_defenidos);
				valores_defenidos = valores_defenidos - 1;
			}

			resto = 11 - (soma % 11);
			if ((resto == 10) || (resto == 11)) {
				verificador10 = '0';
			} else {
				verificador10 = (char) (resto + 48); // converte no respectivo caractere numerico
			}
			// Calculo do 2o. Digito Verificador
			soma = 0;
			valores_defenidos = 11;
			for (controle = 0; controle < 10; controle++) {
				num = (int) (CPF.charAt(controle) - 48);
				soma = soma + (num * valores_defenidos);
				valores_defenidos = valores_defenidos - 1;
			}

			resto = 11 - (soma % 11);
			if ((resto == 10) || (resto == 11)) {
				verificador11 = '0';
			} else {
				verificador11 = (char) (resto + 48);
			}

			// Verifica se os digitos calculados conferem com os digitos informados.
			if ((verificador10 == CPF.charAt(9)) && (verificador11 == CPF.charAt(10))) {
				return (true);
			} else {
				return (false);
			}
		} catch (InputMismatchException erro) {
			return (false);
		}
	} // Fim validaCPF

	/**
	 * Create an entry for this application in the system account list, if it isn't already there.
	 *
	 * @param context Context
	 */
	@TargetApi(Build.VERSION_CODES.FROYO)
	public void CreateSyncAccount(Context context) {
		boolean newAccount = false;
		boolean setupComplete = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.pref_setup_complete), false);

		FuncoesPersonalizadas f = new FuncoesPersonalizadas(context);

		Bundle dadosUsuario = new Bundle();
		dadosUsuario.putString("Usuario", f.getValorXml("Usuario"));
		dadosUsuario.putString("Email", f.getValorXml("Email"));

		// Create account, if it's missing. (Either first run, or user has deleted account.)
		Account account = new Account(f.getValorXml("Usuario"), context.getResources().getString(R.string.sync_account_type));

		AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

		if (accountManager.addAccountExplicitly(account, null, dadosUsuario)) {
			// Inform the system that this account supports sync
			ContentResolver.setIsSyncable(account, context.getResources().getString(R.string.content_authority), 1);
			// Inform the system that this account is eligible for auto sync when the network is up
			ContentResolver.setSyncAutomatically(account, context.getResources().getString(R.string.content_authority), true);
			// Recommend a schedule for automatic synchronization. The system may modify this based
			// on other scheduled syncs and network utilization.
			ContentResolver.addPeriodicSync(account, context.getResources().getString(R.string.content_authority), new Bundle(), 20);
			newAccount = true;
		}

		// Schedule an initial sync if we detect problems with either our account or our local
		// data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
		// the account list, so wee need to check both.)
		if (newAccount || !setupComplete) {
			TriggerRefresh(context);
			PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(context.getResources().getString(R.string.pref_setup_complete), true).commit();
		}

		/*if (ContentResolver.isSyncPending(account, context.getResources().getString(R.string.content_authority))  ||
			ContentResolver.isSyncActive(account, context.getResources().getString(R.string.content_authority))) {
			Log.i("SAVARE", "SyncPending, canceling - Tela FuncoesPersonalizadas");
			ContentResolver.cancelSync(account, context.getResources().getString(R.string.content_authority));
		}*/
		// To just enable the sync (not kick it off) call setSyncAutomatically on ContentResolver. An account is needed but it can be a dummy account.
		ContentResolver.setSyncAutomatically(account, context.getResources().getString(R.string.content_authority), true);
	}

	/**
	 * Helper method to trigger an immediate sync ("refresh").
	 *
	 * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
	 * means the user has pressed the "refresh" button.
	 *
	 * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
	 * preserve battery life. If you know new data is available (perhaps via a GCM notification),
	 * but the user is not actively waiting for that data, you should omit this flag; this will give
	 * the OS additional freedom in scheduling your sync request.
	 */
	public void TriggerRefresh(Context context) {

		Bundle b = new Bundle();
		// Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

		SavareAutenticadorService savareAutenticadorService = new SavareAutenticadorService();
		Account c = SavareAutenticadorService.GetAccount(context);

		ContentResolver.requestSync(c, context.getResources().getString(R.string.content_authority), b);
	}

	public boolean statusSincronizacaoPlano(){
		SavareAutenticadorService savareAutenticadorService = new SavareAutenticadorService();
		Account c = SavareAutenticadorService.GetAccount(context);

		if (ContentResolver.getIsSyncable(c, context.getResources().getString(R.string.content_authority)) > 0){
			return true;
		} else {
			return false;
		}
	}

	public void cancelarSincronizacaoSegundoPlano(){
		SavareAutenticadorService savareAutenticadorService = new SavareAutenticadorService();
		Account c = SavareAutenticadorService.GetAccount(context);

		ContentResolver.cancelSync(c, context.getResources().getString(R.string.content_authority));
	}

	public String geraGuid(int tamanho){
		if (tamanho > 0){
			return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, tamanho);
		}else {
			return  UUID.randomUUID().toString();
		}
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						String ip = Formatter.formatIpAddress(inetAddress.hashCode());
						//Log.i(TAG, "***** IP="+ ip);
						return ip;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}

	public boolean checaVersao(){
		boolean valido = true;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel mChannel = new NotificationChannel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL, NOTIFICATION_FILE, NotificationManager.IMPORTANCE_MIN);
            mChannel.setDescription(context.getResources().getString(R.string.importar_dados_recebidos));
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChannel);
        }
        // Create a BigTextStyle object.
        bigTextStyle = new NotificationCompat.BigTextStyle();

        mBuilder = new NotificationCompat.Builder(context, ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_smallicon)
                .setContentTitle(context.getResources().getString(R.string.versao_savare_desatualizada))
                .setStyle(bigTextStyle)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setSound(null, AudioManager.STREAM_VOICE_CALL)
                .setVibrate(new long[0])
                .setOnlyAlertOnce(true);
		try {
			EmpresaRotinas empresaRotinas = new EmpresaRotinas(context);

			EmpresaBeans empresaBeans = empresaRotinas.empresa(getValorXml(TAG_CODIGO_EMPRESA));

			if (empresaBeans != null){
				int versaoLocal = VersionUtils.getVersionCode(context);
				//int versaoWebservice = Integer.parseInt(objetoIndividual.getProperty("return").toString());

				// Checa se o SAVARE esta desatualizado
				if (versaoLocal < empresaBeans.getVersaoSavare()) {
					// Cria uma notificacao para ser manipulado
                    bigTextStyle.bigText(context.getResources().getString(R.string.savare_desatualizado_favor_atualize));
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR  + new Random().nextInt(100), mBuilder.build());

                    valido = false;

					// Checa se o SAVARE esta mais atualizado que o webservice
				}/* else if (versaoLocal > empresaBeans.getVersaoSavare()) {
					// Cria uma notificacao para ser manipulado
                    bigTextStyle.bigText(context.getResources().getString(R.string.savare_mais_atualizado_que_webservice));
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());

					valido = true;

					// Checa se o SAVARE esta na mesma versao que o webservice
				} else if (versaoLocal == empresaBeans.getVersaoSavare()) {
					valido = true;
				} */
			} else {
                bigTextStyle.bigText(context.getResources().getString(R.string.nao_retornou_dados_suficiente_para_continuar_comunicao_webservice));
                mBuilder.setStyle(bigTextStyle);
                notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
			}

		} catch (Exception e){
            bigTextStyle.bigText(context.getResources().getString(R.string.erro_validar_versao) + " \n " + e.getMessage());
            mBuilder.setStyle(bigTextStyle);
            notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_SINCRONIZAR + new Random().nextInt(100), mBuilder.build());
		}
		return valido;
	}


	public void fecharTecladoVirtual(){
		//criaListaDeProdutos(where, null, 1);
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		// Fecha o teclado
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public static boolean pingHost(String host, int port) {
        try {
            SocketAddress sockaddr = new InetSocketAddress(host, port);
            // Create an unbound socket
            Socket sock = new Socket();

            // This method will block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            sock.connect(sockaddr, 2000);

            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        } catch (Exception e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public static boolean pingWebserviceSisInfo(){
        return pingHost(ServicosWeb.IP_SERVIDOR_WEBSERVICE, 8080);
    }

    public void setUuidDispositivo(){
        if ( (Build.VERSION.SDK_INT >= 23)) {

        	if (ContextCompat.checkSelfPermission( context, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            	ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.READ_PHONE_STATE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS  }, PackageManager.PERMISSION_GRANTED );
			}
        }
		UUID deviceUuid = null;
		String androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

			SubscriptionManager subsManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

			List<SubscriptionInfo> subsList = subsManager.getActiveSubscriptionInfoList();

			if (subsList!=null) {
				for (SubscriptionInfo subsInfo : subsList) {
					if (subsInfo != null) {
						String simSerialNo  = subsInfo.getIccId();
						deviceUuid = new UUID(androidId.hashCode(), ((long) subsInfo.getIccId().hashCode() << 32) | subsInfo.getCardId());
					}
				}
			}
		} else {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceUuid = new UUID(androidId.hashCode(), ((long) tm.getDeviceId().hashCode() << 32) | tm.getSimSerialNumber().hashCode());
		}

		String nameAccount = null;

		Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (gmailPattern.matcher(account.name).matches()) {
				nameAccount = account.name;
			}
			if ( (nameAccount == null) && (account.type.equalsIgnoreCase("com.whatsapp")) ){
			    nameAccount = account.name;
            }
		}
		String descricao = (Build.VERSION.RELEASE + " - "+ Build.MODEL + ((nameAccount != null && !nameAccount.isEmpty()) ? " - " + nameAccount : "")).toUpperCase();

		setValorXml(TAG_UUID_DISPOSITIVO, deviceUuid.toString().toUpperCase());
		setValorXml(TAG_DESCRICAO_DISPOSITIVO, (descricao.length() > 40 ? descricao.substring(0, 39) : descricao));
	}

	public void setPermission(){
		if ( (Build.VERSION.SDK_INT >= 23)) {

			if (ContextCompat.checkSelfPermission( context, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.READ_PHONE_STATE  }, PackageManager.PERMISSION_GRANTED );
			}

			if (ContextCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.WRITE_EXTERNAL_STORAGE  }, PackageManager.PERMISSION_GRANTED );
			}

			if (ContextCompat.checkSelfPermission( context, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE  }, PackageManager.PERMISSION_GRANTED );
			}

			if (ContextCompat.checkSelfPermission( context, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.INTERNET  }, PackageManager.PERMISSION_GRANTED );
			}

			if (ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  }, PackageManager.PERMISSION_GRANTED );
			}

			if (ContextCompat.checkSelfPermission( context, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.CAMERA  }, PackageManager.PERMISSION_GRANTED );
			}
		}
	}

	public String getNomeVersaoAplicacao(){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
            //int versionCode = pInfo.versionCode;
            //Log.d("MyApp", "Version Name : "+version + "\n Version Code : "+versionCode);
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            //Log.d("MyApp", "PackageManager Catch : "+e.toString());
        }
        return "";
    }

    public Integer getNumeroVersaoAplicacao(){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
            //int versionCode = pInfo.versionCode;
            //Log.d("MyApp", "Version Name : "+version + "\n Version Code : "+versionCode);
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            //Log.d("MyApp", "PackageManager Catch : "+e.toString());
        }
        return 0;
    }

	public boolean hasPermissions(@NonNull String... permissions) {
		if (Build.VERSION.SDK_INT > 22) {
			for (String permission : permissions)
				if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(permission))
					return false;
		}
		return true;
	}

} // Fecha classe

package com.savare.banco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;

import com.savare.configuracao.ServicosWeb;

public class ConexaoHttpClient implements Runnable {
	
	public static final int HTTP_TIMEOUT = 30 * 1000;
	private static HttpClient httpClient;
	public Context context;

	
	private static HttpClient getHttpClient(){
		
		if(httpClient == null){
			// Instancia o httpClient
			httpClient = new DefaultHttpClient();
			final HttpParams httpParams = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, HTTP_TIMEOUT);
			ConnManagerParams.setTimeout(httpParams, HTTP_TIMEOUT);
		}
		
		return httpClient;
	}
	
	public ConexaoHttpClient(Context context) {
		this.context = context;
	}
	
	public ConexaoHttpClient() {
		
	}
	
	public static String executaHttpPost(ArrayList<NameValuePair> parametrosPost) throws Exception {
		BufferedReader bufferedReader = null;
		
		ServicosWeb servicosWeb = new ServicosWeb();
		try {
			HttpClient client = getHttpClient();
			HttpPost httpPost = new HttpPost(servicosWeb.URL_ERRO);
			// Armazena os valores a serem passados
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parametrosPost);
			
			// Excuta tudo o que esta na url
			httpPost.setEntity(formEntity);
			// Pega a resposta do servidor
			HttpResponse httpResponse = client.execute(httpPost);
			
			bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			StringBuffer stringBuffer = new StringBuffer("");
			String line = "";
			String LS = System.getProperty("line.separator"); // \s
			
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line + LS);
			}
			
			bufferedReader.close();

			String resultado = stringBuffer.toString();
			
			return resultado;
			
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}			
		//Videoaulas Neri Neitzke - mais de 4.000 videoaulas www.informaticon.com.br

	}
	
	public void postHttpBruto(ArrayList<NameValuePair> valores){
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://www.parceiraodistribuidora.com.br/server.php");
		
		try{
			/*ArrayList<NameValuePair> valores = new ArrayList<NameValuePair>();
			valores.add(new BasicNameValuePair("nome", nome));
			valores.add(new BasicNameValuePair("sobrenome", sobrenome));
			valores.add(new BasicNameValuePair("email", email));*/
			
			//httpPost.setEntity(new UrlEncodedFormEntity(valores));
			HttpResponse resposta = httpClient.execute(httpPost);
			
			String s = EntityUtils.toString(resposta.getEntity());
			int i = s.length();
		}
		catch(ClientProtocolException e){}
		catch(IOException e){}
	}

	
	public String postHttp(ArrayList<NameValuePair> valores){
		HttpClient httpClient = new DefaultHttpClient();
		
		HttpPost httpPost = new HttpPost(ServicosWeb.URL_ERRO);
		
		String answer = "";
		
		try{
			/*ArrayList<NameValuePair> valores = new ArrayList<NameValuePair>();
			valores.add(new BasicNameValuePair("method", wd.getMethod()));
			valores.add(new BasicNameValuePair("name", wd.getName()));
			valores.add(new BasicNameValuePair("email", wd.getEmail()));
			valores.add(new BasicNameValuePair("age", wd.getAge()+""));
			valores.add(new BasicNameValuePair("img-mime", wd.getImage().getMime()));
			valores.add(new BasicNameValuePair("img-image", wd.getImage().getBitmapBase64()));*/
			
			
			httpPost.setEntity(new UrlEncodedFormEntity(valores));
			HttpResponse resposta = httpClient.execute(httpPost);
			answer = EntityUtils.toString(resposta.getEntity());
		}
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		catch (ClientProtocolException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		return(answer);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}

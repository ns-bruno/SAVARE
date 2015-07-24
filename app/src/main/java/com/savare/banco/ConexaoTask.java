package com.savare.banco;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.savare.R;
import com.savare.configuracao.ServicosWeb;
import com.savare.funcoes.FuncoesPersonalizadas;

public class ConexaoTask extends AsyncTask<List<NameValuePair>, String, Boolean> {

	private Context context;
	private ProgressDialog dialog;
	private int comando = 0; // ERRO
	private FuncoesPersonalizadas funcoes;
	private String mensagemRetorno;
	
    
    /**
	 * @return the comando
	 */
	public int getComando() {
		return comando;
	}


	/**
	 * Salva o tipo de comando para ser executado.
	 * Exemplo:
	 * <p>ERRO</p>
	 * 
	 * @param comando the comando to set
	 */
	public void setComando(int comando) {
		this.comando = comando;
	}


	/**
     * Construtor com Context(activity)
     * @param context
     */
    public ConexaoTask(Context context){
    	super();
    	this.context = context;
    }
 
    
    public void postData(List<NameValuePair> valores) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(ServicosWeb.URL_ERRO);

		try {
			// Adiciona os valores que vai ser passado por parametro
			httppost.setEntity(new UrlEncodedFormEntity(valores));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			// Pega a mensagem de retorno
			this.mensagemRetorno = EntityUtils.toString(response.getEntity());
			
		} catch (ClientProtocolException e) {
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "ConexaoTask");
			mensagem.put("mensagem", e.getMessage());
			
			this.funcoes = new FuncoesPersonalizadas(context);
			this.funcoes.menssagem(mensagem);
			
		} catch (IOException e) {
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "ConexaoTask");
			mensagem.put("mensagem", e.getMessage());
			
			this.funcoes = new FuncoesPersonalizadas(context);
			this.funcoes.menssagem(mensagem);
		}
	}
    
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	// Cria um progressdialog
    	dialog = new ProgressDialog(this.context);
        // Set progressdialog title
    	dialog.setTitle("Enviando Dados");
        // Set progressdialog message
    	dialog.setMessage("Aguarde! Enviando...");
    	dialog.setIndeterminate(false);
        // Show progressdialog
    	dialog.show();
    }
	
    @Override
    protected Boolean doInBackground(List<NameValuePair>... valores) {
    	Boolean result = false;
    	
    	if(this.comando == ServicosWeb.ERRO){
    		
    		postData(valores[0]);
    		result = true;
    		
    	}else{
    		this.mensagemRetorno = String.valueOf(R.string.nao_possivel_enviar);
    	}
    	
    	    	
        return result;
    } // Fim do doInBackground
    
    @Override
    protected void onPostExecute(Boolean result) {
    	super.onPostExecute(result);
    	
    	dialog.dismiss();
    	
    	funcoes = new FuncoesPersonalizadas(this.context.getApplicationContext());
    	
    	ContentValues contentValues = new ContentValues();
    	contentValues.put("comando", 2);
    	contentValues.put("mensagem", mensagemRetorno);
		
    	funcoes.menssagem(contentValues);
    }
    
    


}

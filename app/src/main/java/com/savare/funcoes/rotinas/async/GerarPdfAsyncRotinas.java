package com.savare.funcoes.rotinas.async;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.rotinas.GerarPdfRotinas;

public class GerarPdfAsyncRotinas extends AsyncTask<String, String, String> {
	
	private ProgressDialog progress;
	private Context context;
	private OrcamentoBeans orcamento;
	private List<ItemOrcamentoBeans> listaItensOrcamento;
	private int tipoGerarPdf = -1;
	public static final int SIM = 1;
	public static final int NAO = 0;


	public GerarPdfAsyncRotinas(Context context) {
		this.context = context;
	}
	
	
	/**
	 * @return the tipoGerarPdf
	 */
	public int getTipoGerarPdf() {
		return tipoGerarPdf;
	}

	/**
	 * 0 = Com barra de progresso.
	 * 
	 * @param tipoGerarPdf the tipoGerarPdf to set
	 */
	public void setTipoGerarPdf(int tipoGerarPdf) {
		this.tipoGerarPdf = tipoGerarPdf;
	}

	/**
	 * @return the orcamento
	 */
	public OrcamentoBeans getOrcamento() {
		return orcamento;
	}

	/**
	 * @param orcamento the orcamento to set
	 */
	public void setOrcamento(OrcamentoBeans orcamento) {
		this.orcamento = orcamento;
	}

	/**
	 * @return the listaItensOrcamento
	 */
	public List<ItemOrcamentoBeans> getListaItensOrcamento() {
		return listaItensOrcamento;
	}

	/**
	 * @param listaItensOrcamento the listaItensOrcamento to set
	 */
	public void setListaItensOrcamento(List<ItemOrcamentoBeans> listaItensOrcamento) {
		this.listaItensOrcamento = listaItensOrcamento;
	}

	
	@Override
	protected void onPreExecute() {
		//super.onPreExecute();
		if(tipoGerarPdf == SIM){

			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
						//Cria novo um ProgressDialogo e exibe
						progress = new ProgressDialog(context);
						progress.setMessage("Aguarde, Gerando o PDF...");
						progress.show();
				}
			});
		}
	}
	
	
	
	@Override
	protected String doInBackground(String... params) {
		
		GerarPdfRotinas gerarPdfRotinas = new GerarPdfRotinas(context);
		gerarPdfRotinas.setListaItensOrcamento(listaItensOrcamento);
		gerarPdfRotinas.setOrcamento(orcamento);
		
		String s = gerarPdfRotinas.criaArquivoPdf();
		
		return s;
	}

	
	@Override
	protected void onPostExecute(String result) {
		//super.onPostExecute(result);
		if(tipoGerarPdf == SIM){
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					// Cancela progressDialogo
					progress.dismiss();
				}
			});
		}
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		//super.onProgressUpdate(values);
		/*if(tipoGerarPdf == SIM){
			//Atualiza mensagem
			progress.setMessage("Ainda esta sendo gerado o PDF, Aguarde Por Favor...");
		}*/
	}
}

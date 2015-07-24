package com.savare.funcoes.rotinas;

import android.content.Context;
import android.os.Environment;

import com.savare.beans.OrcamentoBeans;

public class GeraHtmlRotinas {

	private static Context context;
	private OrcamentoBeans orcamento;
	private static String PASTA = "SAVARE";
	private static String FILE = Environment.getExternalStorageDirectory().getPath() + "/" + PASTA;
	
	public GeraHtmlRotinas(Context context) {
		this.context = context;
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
	
	
	public void criarArquivoHtml(){
		String codigoHtml = "<!DOCTYPE html><html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
		
		
	}
	
}

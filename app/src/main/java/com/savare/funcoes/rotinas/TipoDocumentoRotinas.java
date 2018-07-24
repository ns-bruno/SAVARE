package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.R;
import com.savare.banco.funcoesSql.TipoDocumentoSql;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

public class TipoDocumentoRotinas extends Rotinas {

	public TipoDocumentoRotinas(Context context) {
		super(context);
	}
	
	
	/**
	 * Funcao para retornar todas os tipos de documentos cadastrado.
	 * 
	 * @param where
	 * @return
	 */
	public List<TipoDocumentoBeans> listaTipoDocumento(String where){
		List<TipoDocumentoBeans> listaTipoDocumento = null;
		
		// Instancia a classe de funcoes
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		if(where != null){
			where = " (" + where + ") AND (ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") ";
		} else {
			where = "(ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") ";
		}
		
		// Instancia a classe para manipular os dados do banco de dados
		TipoDocumentoSql tipoDocumentoSql = new TipoDocumentoSql(context);
		// Executa o sql e armazena os dados recuperados em um Cursor
		Cursor cursor = tipoDocumentoSql.query(where, "DESCRICAO");
		
		// Instancia a classe para salvar os dados que foi recuperando no banco
		listaTipoDocumento = new ArrayList<TipoDocumentoBeans>();
		
		if ( (cursor != null) && (cursor.getCount() > 0) ){

			TipoDocumentoBeans tipoDocumentoSelecione = new TipoDocumentoBeans();
			tipoDocumentoSelecione.setIdTipoDocumento(0);
			tipoDocumentoSelecione.setCodigoTipoDocumento(0);
			tipoDocumentoSelecione.setIdEmpresa(0);
			tipoDocumentoSelecione.setDescricaoTipoDocumento(context.getResources().getString(R.string.selecione_tipo_documento));
			tipoDocumentoSelecione.setSiglaTipoDocumento("Selecione");
			tipoDocumentoSelecione.setTipoVenda("0");

			listaTipoDocumento.add(tipoDocumentoSelecione);
			
			while(cursor.moveToNext()){
				// Pega os dados recuperado do banco de dados
				TipoDocumentoBeans tipoDocumento = new TipoDocumentoBeans();
				tipoDocumento.setIdTipoDocumento(cursor.getInt(cursor.getColumnIndex("ID_CFATPDOC")));
				tipoDocumento.setCodigoTipoDocumento(cursor.getInt(cursor.getColumnIndex("CODIGO")));
				tipoDocumento.setIdEmpresa(cursor.getInt(cursor.getColumnIndex("ID_SMAEMPRE")));
				tipoDocumento.setDescricaoTipoDocumento(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
				tipoDocumento.setSiglaTipoDocumento(cursor.getString(cursor.getColumnIndex("SIGLA")));
				tipoDocumento.setTipoVenda(cursor.getString(cursor.getColumnIndex("TIPO")));
				
				listaTipoDocumento.add(tipoDocumento);
			}
		}
		// Retorna uma lista de documentos
		return listaTipoDocumento;
	}
	
	
	

} // Fim da classe

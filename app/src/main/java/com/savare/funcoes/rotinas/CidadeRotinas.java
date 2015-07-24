package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.CidadeSql;
import com.savare.banco.funcoesSql.TipoDocumentoSql;
import com.savare.beans.CidadeBeans;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class CidadeRotinas extends Rotinas {

	public CidadeRotinas(Context context) {
		super(context);
	}
	
	public List<CidadeBeans> listaCidade(String where){
		List<CidadeBeans> listaCidades = null;
		
		// Instancia a classe de funcoes
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		if(where != null){
			where = " (" + where + ")";
		} 
		// Instancia a classe para manipular os dados do banco de dados
		CidadeSql cidadeSql = new CidadeSql(context);
		// Executa o sql e armazena os dados recuperados em um Cursor
		Cursor dadosCidade = cidadeSql.query(where);
		
		// Instancia a classe para salvar os dados que foi recuperando no banco
		listaCidades = new ArrayList<CidadeBeans>();
		
		if ( (dadosCidade != null) && (dadosCidade.getCount() > 0) ){
			
			while(dadosCidade.moveToNext()){
				// Pega os dados recuperado do banco de dados
				CidadeBeans cidade = new CidadeBeans();
				cidade.setIdCidade(dadosCidade.getInt(dadosCidade.getColumnIndex("ID_CFACIDAD")));
				cidade.setIdEstado(dadosCidade.getInt(dadosCidade.getColumnIndex("ID_CFAESTAD")));
				cidade.setDescricao(dadosCidade.getString(dadosCidade.getColumnIndex("DESCRICAO")));
				cidade.setCodigoIbge(dadosCidade.getInt(dadosCidade.getColumnIndex("COD_IBGE")));
				
				listaCidades.add(cidade);
			}
		}
		// Retorna uma lista de documentos
		return listaCidades;
	}

}

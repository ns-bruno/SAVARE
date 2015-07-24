package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.savare.banco.funcoesSql.RamoAtividadeSql;
import com.savare.beans.RamoAtividadeBeans;
import com.savare.funcoes.Rotinas;

public class RamoAtividadeRotinas extends Rotinas {

	public RamoAtividadeRotinas(Context context) {
		super(context);
	}
	
	public List<RamoAtividadeBeans> listaRamoAtividade(){
		List<RamoAtividadeBeans> listaRamoAtividade = new ArrayList<RamoAtividadeBeans>();
		try{
		RamoAtividadeSql ramoAtividadeSql = new RamoAtividadeSql(context);
		
		Cursor dadosRamo = ramoAtividadeSql.query(null, "DESCRICAO");
		
		if(dadosRamo != null && dadosRamo.getCount() > 0){
			
			while (dadosRamo.moveToNext()) {
				// Cria variavel para salvar os dados da atividade
				RamoAtividadeBeans ramoAtividadeBeans = new RamoAtividadeBeans();
				
				ramoAtividadeBeans.setIdRamoAtividade(dadosRamo.getInt(dadosRamo.getColumnIndexOrThrow("ID_CFAATIVI")));
				ramoAtividadeBeans.setCodigo(dadosRamo.getInt(dadosRamo.getColumnIndex("CODIGO")));
				ramoAtividadeBeans.setDescricaoRamoAtividade(dadosRamo.getString(dadosRamo.getColumnIndex("DESCRICAO")));
				ramoAtividadeBeans.setDescontoAtacadoPrazo(dadosRamo.getDouble(dadosRamo.getColumnIndex("DESC_ATAC_PRAZO")));
				ramoAtividadeBeans.setDescontoAtacadoVista(dadosRamo.getDouble(dadosRamo.getColumnIndex("DESC_ATAC_VISTA")));
				ramoAtividadeBeans.setDescontoVarejoPrazo(dadosRamo.getDouble(dadosRamo.getColumnIndex("DESC_VARE_PRAZO")));
				ramoAtividadeBeans.setDescontoVarejoVista(dadosRamo.getDouble(dadosRamo.getColumnIndex("DESC_VARE_VISTA")));
				if( (dadosRamo.getString(dadosRamo.getColumnIndex("DESC_PROMOCAO")) != null) && (!dadosRamo.getString(dadosRamo.getColumnIndex("DESC_PROMOCAO")).equals("")) ){
					ramoAtividadeBeans.setDescontoPromocao(dadosRamo.getString(dadosRamo.getColumnIndex("DESC_PROMOCAO")).charAt(0));
				}
				// adiciona o ramo em uma lista
				listaRamoAtividade.add(ramoAtividadeBeans);
			}
		}
		}catch(Exception e){
			
		}
		
		return listaRamoAtividade;
	}

}

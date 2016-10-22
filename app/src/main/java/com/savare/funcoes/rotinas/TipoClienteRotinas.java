package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.TipoClienteSql;
import com.savare.beans.TipoClienteBeans;
import com.savare.funcoes.Rotinas;

public class TipoClienteRotinas extends Rotinas {

	public TipoClienteRotinas(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	
	public List<TipoClienteBeans> listaTipoCliente(){
		List<TipoClienteBeans> listaTipoCliente = new ArrayList<TipoClienteBeans>();
		
		TipoClienteSql tipoClienteSql = new TipoClienteSql(context);
		
		Cursor dadosTipo = tipoClienteSql.query(null, "DESCRICAO");
		
		if(dadosTipo != null && dadosTipo.getCount() > 0){
			
			while (dadosTipo.moveToNext()) {
				TipoClienteBeans tipoClienteBeans = new TipoClienteBeans();
				
				tipoClienteBeans.setIdTipoCliente(dadosTipo.getInt(dadosTipo.getColumnIndex("ID_CFATPCLI")));
				tipoClienteBeans.setCodigoTipoCliente(dadosTipo.getInt(dadosTipo.getColumnIndex("CODIGO")));
				tipoClienteBeans.setDescricaoTipoCliente(dadosTipo.getString(dadosTipo.getColumnIndex("DESCRICAO")));
				tipoClienteBeans.setDescontoAtacadoPrazo(dadosTipo.getDouble(dadosTipo.getColumnIndex("DESC_ATAC_PRAZO")));
				tipoClienteBeans.setDescontoAtacadoVista(dadosTipo.getDouble(dadosTipo.getColumnIndex("DESC_ATAC_VISTA")));
				tipoClienteBeans.setDescontoVarejoPrazo(dadosTipo.getDouble(dadosTipo.getColumnIndex("DESC_VARE_PRAZO")));
				tipoClienteBeans.setDescontoVarejoVista(dadosTipo.getDouble(dadosTipo.getColumnIndex("DESC_VARE_VISTA")));
				if(dadosTipo.getString(dadosTipo.getColumnIndex("DESC_PROMOCAO")) != null){
					tipoClienteBeans.setDescontoPromocao(dadosTipo.getString(dadosTipo.getColumnIndex("DESC_PROMOCAO")));
				}
				if(dadosTipo.getString(dadosTipo.getColumnIndex("VENDE_ATAC_VAREJO")) != null){
					tipoClienteBeans.setVendeAtacadoVarejo(dadosTipo.getString(dadosTipo.getColumnIndex("VENDE_ATAC_VAREJO")));
				}
				listaTipoCliente.add(tipoClienteBeans);
			}
		}
		
		return listaTipoCliente;
	}
}

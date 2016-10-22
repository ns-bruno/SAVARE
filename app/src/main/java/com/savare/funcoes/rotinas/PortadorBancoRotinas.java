package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.PortadorBancoSql;
import com.savare.banco.funcoesSql.TipoClienteSql;
import com.savare.beans.PortadorBancoBeans;
import com.savare.beans.TipoClienteBeans;
import com.savare.funcoes.Rotinas;

public class PortadorBancoRotinas extends Rotinas {

	public PortadorBancoRotinas(Context context) {
		super(context);
	}
	
	public List<PortadorBancoBeans> listaPortadorBanco(){
		List<PortadorBancoBeans> listaPortadorBanco = new ArrayList<PortadorBancoBeans>();
		
		PortadorBancoSql portadorBancoSql = new PortadorBancoSql(context);
		
		Cursor dadosPortador = portadorBancoSql.query(null, "DESCRICAO");
		
		if(dadosPortador != null && dadosPortador.getCount() > 0){
			
			while (dadosPortador.moveToNext()) {
				PortadorBancoBeans portadorBancoBeans = new PortadorBancoBeans();
				
				portadorBancoBeans.setIdPortadorBanco(dadosPortador.getInt(dadosPortador.getColumnIndex("ID_CFAPORTA")));
				portadorBancoBeans.setCodigoPortadorBanco(dadosPortador.getInt(dadosPortador.getColumnIndex("CODIGO")));
				if(dadosPortador.getString(dadosPortador.getColumnIndex("DG")) != null){
					portadorBancoBeans.setDigitoPortador(dadosPortador.getString(dadosPortador.getColumnIndex("DG")));
				}
				portadorBancoBeans.setDescricaoPortador(dadosPortador.getString(dadosPortador.getColumnIndex("DESCRICAO")));
				portadorBancoBeans.setSiglaPortador(dadosPortador.getString(dadosPortador.getColumnIndex("SIGLA")));
				if(dadosPortador.getString(dadosPortador.getColumnIndex("TIPO")) != null){
					portadorBancoBeans.setTipo(dadosPortador.getString(dadosPortador.getColumnIndex("TIPO")));
				}
				listaPortadorBanco.add(portadorBancoBeans);
			}
		}
		
		return listaPortadorBanco;
	}

}

package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import com.savare.banco.funcoesSql.EstadoSql;
import com.savare.beans.EstadoBeans;
import com.savare.funcoes.Rotinas;

public class EstadoRotinas extends Rotinas {

	public EstadoRotinas(Context context) {
		super(context);
	}
	
	public List<EstadoBeans> listaEstados(){
		List<EstadoBeans> listaEstado = new ArrayList<EstadoBeans>();
		
		EstadoSql estadoSql = new EstadoSql(context);
		
		Cursor dadosEstado = estadoSql.query(null);
		
		if(dadosEstado != null && dadosEstado.getCount() > 0){
			
			while (dadosEstado.moveToNext()) {
				EstadoBeans estado = new EstadoBeans();
				
				estado.setIdEstado(dadosEstado.getInt(dadosEstado.getColumnIndex("ID_CFAESTAD")));
				estado.setCodigoEstado(dadosEstado.getInt(dadosEstado.getColumnIndex("COD_IBGE")));
				estado.setDescricaoEstado(dadosEstado.getString(dadosEstado.getColumnIndex("DESCRICAO")));
				estado.setSiglaEstado(dadosEstado.getString(dadosEstado.getColumnIndex("UF")));
				
				listaEstado.add(estado);
			}
		}
		
		return listaEstado;
	}

}

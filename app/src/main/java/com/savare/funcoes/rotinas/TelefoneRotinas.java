package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import com.savare.banco.funcoesSql.TelefoneSql;
import com.savare.beans.TelefoneBeans;
import com.savare.funcoes.Rotinas;

public class TelefoneRotinas extends Rotinas {

	public TelefoneRotinas(Context context) {
		super(context);
	}
	
	public List<TelefoneBeans> listaTelefone(String where){
		List<TelefoneBeans> listaTelefone = new ArrayList<TelefoneBeans>();
		
		TelefoneSql telefoneSql = new TelefoneSql(context);
		
		Cursor dadosTelefone = telefoneSql.query(where);
		
		if(dadosTelefone != null && dadosTelefone.getCount() > 0){
			
			while (dadosTelefone.moveToNext()) {
				
				TelefoneBeans telefone = new TelefoneBeans();
				
				telefone.setIdTelefone(dadosTelefone.getInt(dadosTelefone.getColumnIndex("ID_CFAFONES")));
				telefone.setIdPessoa(dadosTelefone.getInt(dadosTelefone.getColumnIndex("ID_CFACLIFO")));
				telefone.setDdd(dadosTelefone.getInt(dadosTelefone.getColumnIndex("DDD")));
				telefone.setTelefone(dadosTelefone.getString(dadosTelefone.getColumnIndex("FONE")));
				
				listaTelefone.add(telefone);
			}
		}
		
		return listaTelefone;
	}

}

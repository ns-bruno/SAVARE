package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.beans.EmpresaBeans;
import com.savare.funcoes.Rotinas;

public class EmpresaRotinas extends Rotinas {

	public EmpresaRotinas(Context context) {
		super(context);
	}

	
	public EmpresaBeans empresa(String idEmpresa){
		// Cria variavel para pegar os dados da empresa
		EmpresaBeans empresa = new EmpresaBeans();
		
		// Variavel para manipular os dados do banco
		EmpresaSql empresaSql = new EmpresaSql(context);
		
		Cursor cursor = empresaSql.query("ID_SMAEMPRE = " + idEmpresa);
		
		if( (cursor != null) && (cursor.getCount() > 0)){
			cursor.moveToFirst();
			
			empresa.setIdEmpresa(cursor.getInt(cursor.getColumnIndex("ID_SMAEMPRE")));
			empresa.setNomeRazao(cursor.getString(cursor.getColumnIndex("NOME_RAZAO")));
			empresa.setNomeFantasia(cursor.getString(cursor.getColumnIndex("NOME_FANTASIA")));
			empresa.setCpfCnpj(cursor.getString(cursor.getColumnIndex("CPF_CGC")));
			
		}
		return empresa;
	} // Fim empresa
}

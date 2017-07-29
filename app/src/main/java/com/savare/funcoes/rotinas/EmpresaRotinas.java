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
			empresa.setPeriodocrceditoAtacado(cursor.getString(cursor.getColumnIndex("PERIODO_CREDITO_ATACADO")));
			empresa.setPeriodocrceditoVarejo(cursor.getString(cursor.getColumnIndex("PERIODO_CREDITO_VAREJO")));
			empresa.setTitpoAcumuloCreditoAtacado(cursor.getString(cursor.getColumnIndex("TIPO_ACUMULO_CREDITO_ATACADO")));
			empresa.setTitpoAcumuloCreditoVarejo(cursor.getString(cursor.getColumnIndex("TIPO_ACUMULO_CREDITO_VAREJO")));
			empresa.setVersaoSavare(cursor.getInt(cursor.getColumnIndex("VERSAO_SAVARE")));

		} else {
			empresa = null;
		}
		return empresa;
	} // Fim empresa
}

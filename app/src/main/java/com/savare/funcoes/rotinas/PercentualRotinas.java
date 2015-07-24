package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.PercentualSql;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class PercentualRotinas extends Rotinas {

	public PercentualRotinas(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public double percentualMarkUpAtacado(String codigoUsuario){
		double percentual = -1;
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		String sql = "SELECT IFNULL(AEAPERCE.MARKUP_ATAC, 0) AS MARKUP_ATAC FROM AEAPERCE "
				   + "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) "
				   + "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) "
				   + "WHERE (CFACLIFO.CODIGO_USU = " + codigoUsuario + ") AND (CFACLIFO.USUARIO = '1') AND "
				   + "(AEAPERCE.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")";
		
		PercentualSql percentualSql = new PercentualSql(context);
		
		Cursor dados = percentualSql.sqlSelect(sql);
		
		if((dados != null) && (dados.getCount() > 0)){
			
			if(dados.moveToFirst()){
				percentual = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC"));
			}
		}
		
		return percentual;
	}

	
	public double percentualMarkUpVarejo(String codigoUsuario){
		double percentual = -1;
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		String sql = "SELECT IFNULL(AEAPERCE.MARKUP_VARE, 0) AS MARKUP_VARE FROM AEAPERCE "
				   + "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) "
				   + "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) "
				   + "WHERE (CFACLIFO.CODIGO_USU = " + codigoUsuario + ") AND (CFACLIFO.USUARIO = '1') AND "
				   + "(AEAPERCE.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")";
		
		PercentualSql percentualSql = new PercentualSql(context);
		
		Cursor dados = percentualSql.sqlSelect(sql);
		
		if((dados != null) && (dados.getCount() > 0)){
			dados.moveToFirst();
			
			percentual = dados.getDouble(dados.getColumnIndex("MARKUP_VARE"));
		}
		
		return percentual;
	}
}

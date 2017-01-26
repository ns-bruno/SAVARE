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

		int codigoEmpresa = (!funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? Integer.parseInt(funcoes.getValorXml("CodigoEmpresa")) : -1;

		String sql =  "SELECT IFNULL(AEAPERCE.MARKUP_ATAC, 0) AS MARKUP_ATAC FROM AEAPERCE \n"
					+ "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) \n"
					+ "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n"
					+ "WHERE (CFACLIFO.CODIGO_FUN = " + codigoUsuario + ") AND (CFACLIFO.FUNCIONARIO = '1') AND (CFACLIFO.ID_SMAEMPRE = " + codigoEmpresa + ")";
		
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

		int codigoEmpresa = (!funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? Integer.parseInt(funcoes.getValorXml("CodigoEmpresa")) : -1;

		String sql = "SELECT IFNULL(AEAPERCE.MARKUP_VARE, 0) AS MARKUP_VARE FROM AEAPERCE "
				+ "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) \n"
				+ "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n"
				+ "WHERE (CFACLIFO.CODIGO_FUN = " + codigoUsuario + ") AND (CFACLIFO.FUNCIONARIO = '1') AND (CFACLIFO.ID_SMAEMPRE = " + codigoEmpresa + ")";
		
		PercentualSql percentualSql = new PercentualSql(context);
		
		Cursor dados = percentualSql.sqlSelect(sql);
		
		if((dados != null) && (dados.getCount() > 0)){
			dados.moveToFirst();
			
			percentual = dados.getDouble(dados.getColumnIndex("MARKUP_VARE"));
		}
		
		return percentual;
	}
}

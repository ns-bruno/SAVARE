package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.LogSql;
import com.savare.beans.LogBeans;
import com.savare.funcoes.Rotinas;

public class LogRotinas extends Rotinas {

	public LogRotinas(Context context) {
		super(context);
	}

	
	public List<LogBeans> listaTabela(String where, String[] listaTabelaRequerida){
		
		List<LogBeans> listaTabela = new ArrayList<LogBeans>();
		
		LogSql logSql = new LogSql(context);
		
		String sql = "SELECT * FROM LOG ";
		
		if(where != null){
			sql += "WHERE (" + where + ") ";
		}
		
		if( (where != null) && (listaTabelaRequerida.length > 0)){
			
			sql += " AND (LOG.TABELA = '";
			
			int controle = 0;
			
			for (String tabela : listaTabelaRequerida) {
				controle ++;
				sql += tabela;
				
				if(controle < tabela.length()){
					sql += "' OR LOG.TABELA = '";
				} else {
					sql += "')";
				}
			}
		
		} else if(listaTabelaRequerida != null){
			sql += " WHERE (LOG.TABELA = '";
			
			int controle = 0;
			
			for (String tabela : listaTabelaRequerida) {
				controle ++;
				sql += tabela;
				
				if(controle < listaTabelaRequerida.length){
					sql += "' OR LOG.TABELA = '";
				} else {
					sql += "') ";
				}
			}
		}
		
		// Adiciona um agrupador
		sql += "GROUP BY TABELA ";
		
		Cursor dadosLog = logSql.sqlSelect(sql);
		
		if(dadosLog != null){
			// Move para o primeiro
			//dadosLog.moveToFirst();
			
			while (dadosLog.moveToNext()) {
				LogBeans log = new LogBeans();
				
				log.setIdLog(dadosLog.getInt(dadosLog.getColumnIndex("ID_LOG")));
				log.setIdTabela(dadosLog.getInt(dadosLog.getColumnIndex("ID_TABELA")));
				log.setTabela(dadosLog.getString(dadosLog.getColumnIndex("TABELA")));
				
				listaTabela.add(log);
			}
		}
		
		return listaTabela;
	}
	
	
	public List<LogBeans> listaLogPorTabela(String where){
		List<LogBeans> listaLog = new ArrayList<LogBeans>();
		
		LogSql logSql = new LogSql(context);
		
		String sql = "SELECT * FROM LOG ";
		
		if(where != null){
			sql += "WHERE (" + where + ") ";
		}
		
		sql += "ORDER BY DT_CAD ";
		
		Cursor dadosLog = logSql.sqlSelect(sql);
		
		if(dadosLog != null){
			while (dadosLog.moveToNext()) {
				LogBeans log = new LogBeans();
				
				log.setIdLog(dadosLog.getInt(dadosLog.getColumnIndex("ID_LOG")));
				log.setIdTabela(dadosLog.getInt(dadosLog.getColumnIndex("ID_TABELA")));
				log.setDataCadastro(dadosLog.getString(dadosLog.getColumnIndex("DT_CAD")));
				log.setTabela(dadosLog.getString(dadosLog.getColumnIndex("TABELA")));
				log.setOperacao(dadosLog.getString(dadosLog.getColumnIndex("OPERACAO")));
				log.setUsuario(dadosLog.getString(dadosLog.getColumnIndex("USUARIO")));
				log.setValores(dadosLog.getString(dadosLog.getColumnIndex("VALORES")));
				
				listaLog.add(log);
			}
		}
		
		return listaLog;
	}
}

package com.savare.funcoes.rotinas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.StatusSql;
import com.savare.beans.StatusBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

public class StatusRotinas extends Rotinas {

	public StatusRotinas(Context context) {
		super(context);
	}
	
	public StatusBeans statusCliente(String idPessoa){
		StatusBeans statusPessoa = new StatusBeans();
		
		try {
			
			String sql = "SELECT CFACLIFO.ID_CFACLIFO, CFASTATU.ID_CFASTATU, CFASTATU.CODIGO, CFASTATU.DESCRICAO, "
					   + "CFASTATU.BLOQUEIA, CFASTATU.PARCELA_EM_ABERTO, CFASTATU.VISTA_PRAZO "
					   + "FROM CFACLIFO "
					   + "LEFT OUTER JOIN CFASTATU CFASTATU ON(CFACLIFO.ID_CFASTATU = CFASTATU.ID_CFASTATU) "
					   + "WHERE CFACLIFO.ID_CFACLIFO = " + idPessoa;
			
			PessoaSql pessoaSql = new PessoaSql(context);
			
			Cursor cursor = pessoaSql.sqlSelect(sql);
			
			// Checa se retornou um registro
			if( (cursor != null) && (cursor.getCount() > 0)){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				statusPessoa.setIdStatus(cursor.getInt(cursor.getColumnIndex("ID_CFASTATU")));
				statusPessoa.setCodigo(cursor.getInt(cursor.getColumnIndex("CODIGO")));
				statusPessoa.setDescricao(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
				statusPessoa.setBloqueia(cursor.getString(cursor.getColumnIndex("BLOQUEIA")).charAt(0));
				statusPessoa.setParcelaEmAberto(cursor.getString(cursor.getColumnIndex("PARCELA_EM_ABERTO")).charAt(0));
				statusPessoa.setVistaPrazo(cursor.getString(cursor.getColumnIndex("VISTA_PRAZO")).charAt(0));
			}
			
		} catch (Exception e) {
			
		}
		
		return statusPessoa;
	} // Fim statusCliente


	public List<StatusBeans> listaStatus(String where){
		// Vareavel para salvar a lista de status
		List<StatusBeans> listaStatus = new ArrayList<StatusBeans>();

		StatusSql statusSql = new StatusSql(context);
		// Pega os dados no banco de dados
		Cursor status = statusSql.query(where, "CFASTATU.DESCRICAO, CFASTATU.MENSAGEM");
		// Checa se retornou algum dados do banco
		if ((status != null) && (status.getCount() > 0)){
			// Passa por todos os registros
			while (status.moveToNext()){
				StatusBeans statusPessoa = new StatusBeans();
				// Pega os dados do status
				statusPessoa.setIdStatus(status.getInt(status.getColumnIndex("ID_CFASTATU")));
				statusPessoa.setCodigo(status.getInt(status.getColumnIndex("CODIGO")));
				statusPessoa.setDescricao(status.getString(status.getColumnIndex("DESCRICAO")));
				statusPessoa.setMensagem(status.getString(status.getColumnIndex("MENSAGEM")));
				statusPessoa.setBloqueia(status.getString(status.getColumnIndex("BLOQUEIA")).charAt(0));
				statusPessoa.setParcelaEmAberto(status.getString(status.getColumnIndex("PARCELA_EM_ABERTO")).charAt(0));
				statusPessoa.setVistaPrazo(status.getString(status.getColumnIndex("VISTA_PRAZO")).charAt(0));
				// Adiciona o status na lista
				listaStatus.add(statusPessoa);
			}
		}
		return listaStatus;
	} // listaStatus
	

}

package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.EstoqueSql;
import com.savare.beans.EstoqueBeans;

public class EstoqueRotinas {
	
	private Context context;
	
	public EstoqueRotinas(Context context) {
		this.context = context;
	}
	

	public List<EstoqueBeans> listaEstoqueProduto(String idProduto, String where){
		
		String sql = "SELECT AEAESTOQ.ID_AEAESTOQ, AEAESTOQ.ID_AEAPLOJA, AEAESTOQ.ID_AEALOCES, AEAESTOQ.ESTOQUE, AEAESTOQ.RETIDO, AEAESTOQ.ATIVO "
				   + "FROM AEAESTOQ "
				   + "LEFT OUTER JOIN AEAPLOJA AEAPLOJA ON(AEAESTOQ.ID_AEAPLOJA = AEAPLOJA.ID_AEAPLOJA) "
				   + "LEFT OUTER JOIN AEAPRODU AEAPRODU ON(AEAPLOJA.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU) "
				   + "WHERE AEAPRODU.ID_AEAPRODU = " + idProduto;
		
		if(where != null && where.length() > 1){
			sql += " AND ( " + where + ")";
		}
		
		// Instancia a classe para pegar as informacoes do banco de dados
		EstoqueSql estoqueSql = new EstoqueSql(context);
		
		// Executa o sql e armazena os dados recuperados em um Cursor
		Cursor cursor = estoqueSql.sqlSelect(sql);
		
		// Cria uma lista de estoque para armazenar os valores retornado do banco
		List<EstoqueBeans> listaEstoque = new ArrayList<EstoqueBeans>();
		
		if(cursor != null && cursor.getCount() > 0){
			while (cursor.moveToNext()) {
				// Cria uma vareavel para pegar os dados do estoque
				EstoqueBeans estoque = new EstoqueBeans();
				
				// Salva os dados do estoque
				estoque.setIdEstoque(cursor.getInt(cursor.getColumnIndex("ID_AEAESTOQ")));
				estoque.setIdProdutoLoja(cursor.getInt(cursor.getColumnIndex("ID_AEAPLOJA")));
				estoque.setIdLocacao(cursor.getInt(cursor.getColumnIndex("ID_AEALOCES")));
				estoque.setEstoqueLocacao(cursor.getDouble(cursor.getColumnIndex("ESTOQUE")));
				estoque.setRetidoLocacao(cursor.getDouble(cursor.getColumnIndex("RETIDO")));
				estoque.setAtivo(cursor.getString(cursor.getColumnIndex("ATIVO")));
				
				// Adiciona na lista de estoque
				listaEstoque.add(estoque);
			}
		}
		return listaEstoque;
	} // Fim listaEstoqueProduto
}

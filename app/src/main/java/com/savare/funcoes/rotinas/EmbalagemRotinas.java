package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.beans.EmbalagemBeans;
import com.savare.beans.UnidadeVendaBeans;
import com.savare.funcoes.Rotinas;

public class EmbalagemRotinas extends Rotinas {

	public EmbalagemRotinas(Context context) {
		super(context);
	}
	
	
	/**
	 * Funcao para pegar todas as embalagens cadastradas em
	 * um determinado produto.
	 * 
	 * @param idProduto
	 * @return - Retorna um lista de embalagen do produto
	 */
	public List<EmbalagemBeans> selectEmbalagensProduto(String idProduto){
		
		//Monta o sql
		String sql = "SELECT AEAEMBAL.ID_AEAEMBAL, AEAEMBAL.ID_AEAPRODU, "
				   + "AEAEMBAL.ID_AEAUNVEN, AEAUNVEN.SIGLA, "
				   + "AEAEMBAL.PRINCIPAL, AEAEMBAL.DESCRICAO, AEAEMBAL.FATOR_CONVERSAO, "
				   + "AEAEMBAL.FATOR_PRECO, AEAEMBAL.MODULO, AEAEMBAL.DECIMAIS "
				   + "FROM AEAEMBAL "
				   + "LEFT OUTER JOIN AEAUNVEN AEAUNVEN "
				   + "ON(AEAEMBAL.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN)"
				   + "WHERE (ID_AEAPRODU = 1) AND (AEAEMBAL.ATIVO = '1')";
		
		// Instancia a classe para manipular os dados do banco de dados
		EmbalagemSql embalagemSql = new EmbalagemSql(context);
		// Executa o sql e armazena os dados recuperados em um Cursor
		Cursor cursor = embalagemSql.sqlSelect(sql);
		
		// Instancia a classe para salvar os dados que foi recuperando no banco
		List<EmbalagemBeans> listaEmbalagem = new ArrayList<EmbalagemBeans>();
		
		if(cursor.getCount() > 0){
			//cursor.moveToFirst();
			
			while(cursor.moveToNext()){
				// Pega os dados recuperado do banco de dados
				EmbalagemBeans embalagem = new EmbalagemBeans();
				embalagem.setIdEmbalagem(cursor.getInt(cursor.getColumnIndex("ID_AEAEMBAL")));
				embalagem.setDecimais(cursor.getInt(cursor.getColumnIndex("DECIMAIS")));
				embalagem.setDescricaoEmbalagem(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
				embalagem.setFatorConversao(cursor.getDouble(cursor.getColumnIndex("FATOR_CONVERSAO")));
				embalagem.setFatorPreco(cursor.getDouble(cursor.getColumnIndex("FATOR_PRECO")));
				embalagem.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
				embalagem.setIdUnidadeVenda(cursor.getInt(cursor.getColumnIndex("ID_AEAUNVEN")));
				embalagem.setModulo(cursor.getInt(cursor.getColumnIndex("MODULO")));
				
				UnidadeVendaBeans unidadeVenda = new UnidadeVendaBeans();
				unidadeVenda.setSiglaUnidadeVenda(cursor.getString(cursor.getColumnIndex("SIGLA")));
				
				embalagem.setUnidadeVendaEmbalagem(unidadeVenda);
				
				// Adiciona os dados do produto em uma lista
				listaEmbalagem.add(embalagem);
			} // Fim while
		}
		
		return listaEmbalagem;
	} // Fim selectEmbalagensProduto
	

}

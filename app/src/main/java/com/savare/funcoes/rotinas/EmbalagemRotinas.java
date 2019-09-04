package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.FuncoesSqlThread;
import com.savare.beans.AeaembalBeans;
import com.savare.beans.AeaproduBeans;
import com.savare.beans.AeaunvenBeans;
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
				   + "WHERE (ID_AEAPRODU = " + idProduto + ") AND (AEAEMBAL.ATIVO = '1') "
				   + "ORDER BY COALESCE(AEAEMBAL.PRINCIPAL, AEAUNVEN.SIGLA, AEAEMBAL.DESCRICAO)";

		// Instancia a classe para manipular os dados do banco de dados
		EmbalagemSql embalagemSql = new EmbalagemSql(context);
		// Executa o sql e armazena os dados recuperados em um Cursor
		Cursor cursor = embalagemSql.sqlSelect(sql);
		
		// Instancia a classe para salvar os dados que foi recuperando no banco
		List<EmbalagemBeans> listaEmbalagem = new ArrayList<EmbalagemBeans>();
		
		if( (cursor != null) && (cursor.getCount() > 0) ){
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


	public List<EmbalagemBeans> selectEmbalagensProdutoThread(String idProduto){

		//Monta o sql
		String sql = "SELECT AEAEMBAL.ID_AEAEMBAL, AEAEMBAL.ID_AEAPRODU, "
				+ "AEAEMBAL.ID_AEAUNVEN, AEAUNVEN.SIGLA, "
				+ "AEAEMBAL.PRINCIPAL, AEAEMBAL.DESCRICAO, AEAEMBAL.FATOR_CONVERSAO, "
				+ "AEAEMBAL.FATOR_PRECO, AEAEMBAL.MODULO, AEAEMBAL.DECIMAIS "
				+ "FROM AEAEMBAL "
				+ "LEFT OUTER JOIN AEAUNVEN AEAUNVEN "
				+ "ON(AEAEMBAL.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN)"
				+ "WHERE (ID_AEAPRODU = " + idProduto + ") AND (AEAEMBAL.ATIVO = '1') "
				+ "ORDER BY COALESCE(AEAEMBAL.PRINCIPAL, AEAUNVEN.SIGLA, AEAEMBAL.DESCRICAO)";

		// Instancia a classe para manipular os dados do banco de dados
		FuncoesSqlThread funcoesSqlThread = new FuncoesSqlThread(context);
		// Executa o sql e armazena os dados recuperados em um Cursor
		Cursor cursor = funcoesSqlThread.sqlSelectThread(sql);

		// Instancia a classe para salvar os dados que foi recuperando no banco
		List<EmbalagemBeans> listaEmbalagem = new ArrayList<EmbalagemBeans>();

		if( (cursor != null) && (cursor.getCount() > 0) ){
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
	}

	/**
	 * Pode ser passado somente o @idAeaembal ou pode passar
	 * idAeaprodu e idAeaunven, lembrando que é obrigatorio passa esses dois ou
	 * apenas o idAeambal.
	 *
	 * @param idAeaembal
	 * @param idAeaprodu
	 * @param idAeaunven
	 * @return
	 */
	public AeaembalBeans selectAeaembal(int idAeaembal, int idAeaprodu, int idAeaunven){
		AeaembalBeans aeaembal = null;
		try{
			StringBuffer where = new StringBuffer();
			if (idAeaembal != 0){
				where.append("AEAEMBAL.ID_AEAEMBAL = ").append(idAeaembal);
			} else {
				where.append("(AEAEMBAL.ID_AEAPRODU = ").append(idAeaprodu).append(")");
				where.append(" AND (AEAEMBAL.ID_AEAUNVEN = ").append(idAeaunven).append(")");
				//where.append(" AND (AEAEMBAL.ATIVO = '1')");
			}

			EmbalagemSql embalagemSql = new EmbalagemSql(context);
			Cursor cursorAeaembal = embalagemSql.query(where.toString(), "COALESCE(AEAEMBAL.PRINCIPAL, AEAEMBAL.ID_AEAEMBAL) ASC");

			if ((cursorAeaembal != null) && (cursorAeaembal.moveToFirst())){
				aeaembal = new AeaembalBeans();
				aeaembal.setIdAeaembal(cursorAeaembal.getInt(cursorAeaembal.getColumnIndex("ID_AEAEMBAL")));

				AeaproduBeans aeaprodu = new AeaproduBeans();
				aeaprodu.setIdAeaprodu(cursorAeaembal.getInt(cursorAeaembal.getColumnIndex("ID_AEAPRODU")));
				aeaembal.setAeaprodu(aeaprodu);

				AeaunvenBeans aeaunven = new AeaunvenBeans();
				aeaunven.setIdAeaunven(cursorAeaembal.getInt(cursorAeaembal.getColumnIndex("ID_AEAUNVEN")));
				aeaembal.setAeaunven(aeaunven);

				aeaembal.setDtAlt(cursorAeaembal.getString(cursorAeaembal.getColumnIndex("DT_ALT")));
				if (!cursorAeaembal.isNull(cursorAeaembal.getColumnIndex("PRINCIPAL"))) aeaembal.setPrincipal(cursorAeaembal.getString(cursorAeaembal.getColumnIndex("PRINCIPAL")));
				if (!cursorAeaembal.isNull(cursorAeaembal.getColumnIndex("DESCRICAO"))) aeaembal.setDescricao(cursorAeaembal.getString(cursorAeaembal.getColumnIndex("DESCRICAO")));
				aeaembal.setFatorConversao(cursorAeaembal.getDouble(cursorAeaembal.getColumnIndex("FATOR_CONVERSAO")));
				aeaembal.setFatorPreco(cursorAeaembal.getDouble(cursorAeaembal.getColumnIndex("FATOR_PRECO")));
				aeaembal.setModulo(cursorAeaembal.getInt(cursorAeaembal.getColumnIndex("MODULO")));
				aeaembal.setDecimais(cursorAeaembal.getInt(cursorAeaembal.getColumnIndex("DECIMAIS")));
				aeaembal.setAtivo(cursorAeaembal.getString(cursorAeaembal.getColumnIndex("ATIVO")));
			}
		} catch (Exception e){
			new MaterialDialog.Builder(context)
					.title("EmbalagemRotinas")
					.content(e.getMessage())
					.positiveText(android.R.string.ok)
					//.negativeText(R.string.disagree)
					.autoDismiss(true)
					.show();
		}
		return aeaembal;
	}

}

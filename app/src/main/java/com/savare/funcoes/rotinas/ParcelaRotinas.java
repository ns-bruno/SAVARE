package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.ProgressBar;

import com.savare.banco.funcoesSql.ParcelaSql;
import com.savare.beans.ParcelaBeans;
import com.savare.beans.TitulosListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class ParcelaRotinas extends Rotinas {

	public static final char TITULOS_EM_ABERTO = '0', TITULOS_BAIXADOS = '1', TITULOS_EM_ABERTO_VENCIDOS = '2';
	public static final char RECEBER = '0', PAGAR = '1';

	public ParcelaRotinas(Context context) {
		super(context);
		
	}

	/**
	 * 
	 * @param idPessoa
	 * @param tipoLitagem - 0 = Titulos em Aberto, 1 = Titulos Baixado, 2 = Titulos em Aberto Vencidos 
	 * @param pagarReceber - 0 = Receber, 1 = Pagar
	 * @return
	 */
	@SuppressWarnings("resource")
	public List<TitulosListaBeans> listaTitulos(String idPessoa, char tipoLitagem, char pagarReceber, String where, final ProgressBar progressBarStatus){
		
		List<TitulosListaBeans> listaTitulos = new ArrayList<TitulosListaBeans>();
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		try{
			String sql = "SELECT CFACLIFO.NOME_RAZAO, CFACLIFO.NOME_FANTASIA, RPAPARCE.ID_CFACLIFO, RPAPARCE.DT_EMISSAO, RPAPARCE.DT_VENCIMENTO, "
					   + "RPAPARCE.DT_BAIXA, RPAPARCE.VL_PARCELA, RPAPARCE.FC_VL_RESTANTE, RPAPARCE.PARCELA, RPAPARCE.SEQUENCIAL, "
					   + "RPAPARCE.NUMERO, CFATPDOC.DESCRICAO AS DESCRICAO_TPDOC, CFAPORTA.DESCRICAO AS DESCRICAO_PORTA, CFASTATU.DESCRICAO AS DESCRICAO_STATU, "
					   + "round(julianday('NOW', 'localtime') - julianday(RPAPARCE.DT_VENCIMENTO)) AS ATRAZADO "
					   + "FROM RPAPARCE "
					   + "LEFT OUTER JOIN CFACLIFO CFACLIFO "
					   + "ON(RPAPARCE.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) "
					   + "LEFT OUTER JOIN CFAENDER "
					   + "ON (CFAENDER.ID_CFAENDER = (SELECT CFAENDER.ID_CFAENDER FROM CFAENDER WHERE (CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) LIMIT 1)) "
					   + "LEFT OUTER JOIN CFATPDOC CFATPDOC "
					   + "ON((RPAPARCE.ID_CFATPDOC = CFATPDOC.ID_CFATPDOC) AND (CFATPDOC.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")) "
					   + "LEFT OUTER JOIN CFAPORTA CFAPORTA "
					   + "ON(RPAPARCE.ID_CFAPORTA = CFAPORTA.ID_CFAPORTA) "
					   + "LEFT OUTER JOIN CFASTATU CFASTATU "
					   + "ON(CFACLIFO.ID_CFASTATU = CFASTATU.ID_CFASTATU) ";
					   //+ " AND (RPAPARCE.DT_BAIXA IS NULL) AND (date(RPAPARCE.DT_VENCIMENTO) < date('now', 'localtime'))";
			
			String whereAuxiliar = criaWhereParcela(idPessoa, tipoLitagem, pagarReceber, where) +  " ORDER BY CFACLIFO.NOME_RAZAO, RPAPARCE.DT_VENCIMENTO, CFATPDOC.DESCRICAO, RPAPARCE.PARCELA";
			
			ParcelaSql parcelaSql = new ParcelaSql(context);
			
			Cursor cursor = parcelaSql.sqlSelect(sql + whereAuxiliar);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				// Move para o primeiro cursor
				//cursor.moveToFirst();
				final int totalLista = cursor.getCount();

				if (progressBarStatus != null){

					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							progressBarStatus.setIndeterminate(false);
							progressBarStatus.setMax(totalLista);
						}
					});
				}
				int controle = 0;
			 	while(cursor.moveToNext()) {

					final int finalControle = controle;
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							progressBarStatus.setProgress(finalControle);
						}
					});
			 		
			 		TitulosListaBeans titulos = new TitulosListaBeans();
			 		
			 		titulos.setIdPessoa(cursor.getInt(cursor.getColumnIndex("ID_CFACLIFO")));
					titulos.setNomeRazao(cursor.getString(cursor.getColumnIndex("NOME_RAZAO")));
					titulos.setNomeFantasia(cursor.getString(cursor.getColumnIndex("NOME_FANTASIA")));
					titulos.setDocumento(cursor.getString(cursor.getColumnIndex("DESCRICAO_TPDOC")));
					titulos.setPortadorBanco(cursor.getString(cursor.getColumnIndex("DESCRICAO_PORTA")));
					titulos.setStatus(cursor.getString(cursor.getColumnIndex("DESCRICAO_STATU")));
					titulos.setValorRestante(cursor.getDouble(cursor.getColumnIndex("FC_VL_RESTANTE")));
					titulos.setVencimento(funcoes.formataData(cursor.getString(cursor.getColumnIndex("DT_VENCIMENTO"))));
					if(cursor.getInt(cursor.getColumnIndex("ATRAZADO")) > 0){
						titulos.setAtrazado(true);
					}else {
						titulos.setAtrazado(false);
					}
					ParcelaBeans parcela = new ParcelaBeans();
					parcela.setDataEmissao(funcoes.formataData(cursor.getString(cursor.getColumnIndex("DT_EMISSAO"))));
					parcela.setDataBaixa(funcoes.formataData(cursor.getString(cursor.getColumnIndex("DT_BAIXA"))));
					parcela.setValorParcela(cursor.getDouble(cursor.getColumnIndex("VL_PARCELA")));
					parcela.setParcela(cursor.getInt(cursor.getColumnIndex("PARCELA")));
					parcela.setSequencial(cursor.getString(cursor.getColumnIndex("SEQUENCIAL")));
					parcela.setNumero(cursor.getString(cursor.getColumnIndex("NUMERO")));
					
					List<ParcelaBeans> listaParcela = new ArrayList<ParcelaBeans>();
					listaParcela.add(parcela);
					
					// Adiciona os dados da parcela no titulo
					titulos.setListaParcela(listaParcela);
					
					listaTitulos.add(titulos);

					controle ++;
				}
			}
				
		}catch(Exception e){
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "PessoaRotinas");
			mensagem.put("mensagem", "Os dados da pessoa esta incompleto.");
			
			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}
		
		
		return listaTitulos;
	}
	
	
	
	/**
	 * Funcao para retornar o valor total de titulos que um determinado cliente tem.
	 * Pode set titulos a receber ou a pagar.
	 * 
	 * @param idPessoa
	 * @param tipoListagem - 0 = Titulos em Aberto, 1 = Titulos Baixado, 2 = Titulos em Aberto Vencidos 
	 * @param pagarReceber - 0 = Receber, 1 = Pagar
	 * @return
	 */
	public double totalReceberPagarCliente(String idPessoa, char tipoListagem, char pagarReceber, String where){
		
		String sql = "SELECT SUM(RPAPARCE.FC_VL_RESTANTE) AS TOTAL FROM RPAPARCE "
				   + "LEFT OUTER JOIN CFACLIFO CFACLIFO "
				   + "ON(RPAPARCE.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) "
				   + "LEFT OUTER JOIN CFAENDER "
				   + "ON (CFAENDER.ID_CFAENDER = (SELECT CFAENDER.ID_CFAENDER FROM CFAENDER WHERE (CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) LIMIT 1)) "
				   + "LEFT OUTER JOIN CFASTATU CFASTATU "
				   + "ON(CFACLIFO.ID_CFASTATU = CFASTATU.ID_CFASTATU) "
				   + criaWhereParcela(idPessoa, tipoListagem, pagarReceber, where);
		
		ParcelaSql parcelaSql = new ParcelaSql(context);
		
		Cursor cursor = parcelaSql.sqlSelect(sql);
		// Se o cursor tiver algum valor entra no laco
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Move para o primeiro registro
			cursor.moveToFirst();
			return cursor.getDouble(cursor.getColumnIndex("TOTAL"));
		} else {
			return 0;
		}
	} // Fim totalContasReceberCliente
	
	
	
	/**
	 * Funcao para montar a clausula where de acordo com o passado por parametro.
	 * @param idPessoa
	 * @param tipoLitagem
	 * @param pagarReceber
	 * @return
	 */
	public String criaWhereParcela(String idPessoa, char tipoLitagem, char pagarReceber, String where){
		String whereAuxiliar = " WHERE (RPAPARCE.TIPO = "+ pagarReceber + ") ";
		
		if((where != null) && (where.length() > 0)){
			whereAuxiliar += " AND (" + where + ") ";
		}
		
		if( (idPessoa != null) && (idPessoa.length() > 0) ){
			
			whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.ID_CFACLIFO = " + idPessoa + ") ";
			
			// Titulos em aberto
			if(tipoLitagem == '0'){
				whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.DT_BAIXA IS NULL) ";
			
				// Titulos baixados
			} else if(tipoLitagem == '1'){
				whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.DT_BAIXA IS NOT NULL) ";
			
				// Titulos em aberto vencidos
			} else if(tipoLitagem == '2'){
				whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.DT_BAIXA IS NULL) AND (date(RPAPARCE.DT_VENCIMENTO) < date('now', 'localtime')) ";
			}
			
		} else {
			// Titulos em aberto
			if(tipoLitagem == '0'){
				whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.DT_BAIXA IS NULL) ";
			
				// Titulos baixados
			} else if(tipoLitagem == '1'){
				whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.DT_BAIXA IS NOT NULL) ";
			
				// Titulos em aberto vencidos
			} else if(tipoLitagem == '2'){
				whereAuxiliar = whereAuxiliar + " AND (RPAPARCE.DT_BAIXA IS NULL) AND (date(RPAPARCE.DT_VENCIMENTO) < date('now', 'localtime')) ";
			}
		}
		
		return whereAuxiliar;
	} // Fim


}

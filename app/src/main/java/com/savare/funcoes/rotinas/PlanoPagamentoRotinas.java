package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.savare.banco.funcoesSql.PlanoPagamentoSql;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class PlanoPagamentoRotinas extends Rotinas {

	public PlanoPagamentoRotinas(Context context) {
		super(context);
	}
	
	/**
	 * 
	 * @param where
	 * @param ordem
	 * @param tipoVenda 0 = Atacado, 1 = Varejo, 2 = Todos
	 * @return
	 */
	public List<PlanoPagamentoBeans> listaPlanoPagamento(String where, String ordem, String tipoVenda){
		
		PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);
		
		// Checa se a clausula where esta nula
		if( where != null ){
			// Checa se o tipoVenda esta nulo
			if ((tipoVenda != null) && (!tipoVenda.isEmpty())){
				where = where + " AND ( (ATAC_VAREJO = '2') OR (ATAC_VAREJO = '" + tipoVenda + "') )";
			}
			
		} else if((tipoVenda != null) && (!tipoVenda.isEmpty())){
			where = "( (ATAC_VAREJO = '2') OR (ATAC_VAREJO = '" + tipoVenda + "') )";
		}
		// Cria uma variavel para salvar todos os planos de pagamentos
		List<PlanoPagamentoBeans> listaPlanoPagamento = null;
		
		try{
			// Executa o sql e armazena os registro em um Cursor
			Cursor cursor = planoPagamentoSql.query(where, ordem);

			if((cursor != null) && (cursor.getCount() > 0)){

				listaPlanoPagamento = new ArrayList<PlanoPagamentoBeans>();

				PlanoPagamentoBeans planoPadrao = new PlanoPagamentoBeans();
				planoPadrao.setIdPlanoPagamento(0);
				planoPadrao.setCodigoPlanoPagamento(0);
				planoPadrao.setDescricaoPlanoPagamento("Selecione um Plano de Pagamento");
				planoPadrao.setVistaPrazo("9");

				// Adiciona o plano em uma lista
				listaPlanoPagamento.add(planoPadrao);

				while (cursor.moveToNext()) {
					// Cria uma variavel para pegar os dados de cada plano do banco de dados
					PlanoPagamentoBeans plano = new PlanoPagamentoBeans();
					plano.setIdPlanoPagamento(cursor.getInt(cursor.getColumnIndex("ID_AEAPLPGT")));
					plano.setIdEmpresa(cursor.getInt(cursor.getColumnIndex("ID_AEAPLPGT")));
					plano.setCodigoPlanoPagamento(cursor.getInt(cursor.getColumnIndex("CODIGO")));
					plano.setDescricaoPlanoPagamento(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
					if((cursor.getString(cursor.getColumnIndex("ATAC_VAREJO")) != null) && (cursor.getString(cursor.getColumnIndex("ATAC_VAREJO")).length() > 0)){
						plano.setAtacadoVarejo(cursor.getString(cursor.getColumnIndex("ATAC_VAREJO")));
					}
					String s = cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO"));
					if((cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO")) != null) && (cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO")).length() > 0)){
						plano.setDescontoPromocao(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO")));
					}
					if((cursor.getString(cursor.getColumnIndex("VISTA_PRAZO")) != null) && (cursor.getString(cursor.getColumnIndex("VISTA_PRAZO")).length() > 0)){
						plano.setVistaPrazo(cursor.getString(cursor.getColumnIndex("VISTA_PRAZO")));
					}
					plano.setDescontoAtacado(cursor.getDouble(cursor.getColumnIndex("PERC_DESC_ATAC")));
					plano.setDescontoVarejo(cursor.getDouble(cursor.getColumnIndex("PERC_DESC_VARE")));
					plano.setJurosAtacado(cursor.getDouble(cursor.getColumnIndex("JURO_MEDIO_ATAC")));
					plano.setJurosVarejo(cursor.getDouble(cursor.getColumnIndex("JURO_MEDIO_VARE")));
					
					// Adiciona o plano em uma lista
					listaPlanoPagamento.add(plano);
				}
			}else {
				final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
				// Cria uma variavem para inserir as propriedades da mensagem
				final ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "PlanoPagamentoRotinas");
				mensagem.put("mensagem", "Não existe Planos cadastrados");
				
				// Executa a mensagem passando por parametro as propriedades
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						funcoes.menssagem(mensagem);
					}
				});
			}
		}catch(SQLException e){
			final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			final ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "TipoDocumentoRotinas");
			mensagem.put("mensagem", "Não foi possível carregar os dados do Plano de Pagamento. \n" + e.getMessage());
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					funcoes.menssagem(mensagem);
				}
			});
		
		}catch (Exception e) {
			final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			final ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "TipoDocumentoRotinas");
			mensagem.put("mensagem", "Não foi possivel carregar os dados do Plano de Pagamento. \n" + e.getMessage());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(mensagem);
                }
            });
		}
		
		return listaPlanoPagamento;
	} // Fim listaPlanoPagamento
	
	
	
	public int posicaoPlanoPagamentoLista(List<PlanoPagamentoBeans> lista, String idOrcamento){
		int posicao = 0;
		int idPlanoPgto = 0;
		String atacadoVarejo = "";
		
		try {
			PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);
			
			String sql = "SELECT AEAITORC.ID_AEAPLPGT FROM AEAITORC WHERE AEAITORC.ID_AEAORCAM = " + idOrcamento + " ORDER BY AEAITORC.ID_AEAITORC DESC LIMIT 1";
			
			Cursor planoPagamentoOrcamento = planoPagamentoSql.sqlSelect(sql);
			// Checa se retornou algum registro
			if((planoPagamentoOrcamento != null) && (planoPagamentoOrcamento.getCount() > 0) ){
				planoPagamentoOrcamento.moveToFirst();
				idPlanoPgto = planoPagamentoOrcamento.getInt(planoPagamentoOrcamento.getColumnIndex("ID_AEAPLPGT"));
			} else {
				sql = "SELECT AEAORCAM.ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento;
				Cursor dados = planoPagamentoSql.sqlSelect(sql);

				if ((dados != null) && (dados.getCount() > 0)){
					dados.moveToFirst();
					atacadoVarejo = dados.getString(dados.getColumnIndex("ATAC_VAREJO"));
				}
				dados = null;
				// Verifica se foi retornado o tipo de venda do orcamento
				if ((atacadoVarejo != null) && (!atacadoVarejo.isEmpty())){
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
					sql = "SELECT SMAEMPRE.ID_AEAPLPGT_VARE, SMAEMPRE.ID_AEAPLPGT_ATAC FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = " + funcoes.getValorXml(funcoes.TAG_CODIGO_EMPRESA);
					dados = planoPagamentoSql.sqlSelect(sql);

					if ((dados != null) && (dados.getCount() > 0)) {
					    dados.moveToFirst();

                        // Checa se a venda eh atacado
                        if (atacadoVarejo.equalsIgnoreCase("0")) {
                            idPlanoPgto = dados.getInt(dados.getColumnIndex("ID_AEAPLPGT_ATAC"));
                        } else {
                            idPlanoPgto = dados.getInt(dados.getColumnIndex("ID_AEAPLPGT_VARE"));
                        }
                    }
				}
			}
			// Passa por toda a lista de documentos
			for(int i = 0; i < lista.size(); i++){
				// Checa se o id da lista eh igual ao id do banco de dados salvo no orcamento
				if(lista.get(i).getIdPlanoPagamento() == idPlanoPgto){
					posicao = i;
					return posicao;
				}
			}
			
		} catch (Exception e) {
			final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			final ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 0);
			mensagem.put("tela", "PlanoPagamentoRotinas");
			mensagem.put("mensagem", "Erro ao descobrir posicao do documento da lista. \n" + e.getMessage());
			mensagem.put("dados", "TipoDocumentoRotinas: " + e + " | " + lista);
			mensagem.put("usuario", funcoes.getValorXml("Usuario"));
			mensagem.put("usuario", funcoes.getValorXml("ChaveEmpresa"));
			mensagem.put("usuario", funcoes.getValorXml("Email"));
			// Executa a mensagem passando por parametro as propriedades
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(mensagem);
                }
            });
		}
		
		return posicao;
	}

}

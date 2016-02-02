package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.savare.R;
import com.savare.banco.funcoesSql.ItemOrcamentoSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.beans.CidadeBeans;
import com.savare.beans.DescricaoSimplesBeans;
import com.savare.beans.EnderecoBeans;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.ProdutoBeans;
import com.savare.beans.TotalMensal;
import com.savare.beans.UnidadeVendaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class OrcamentoRotinas extends Rotinas {
	
	public static final String ORCAMENTO = "O", 
			  				   PEDIDO_NAO_ENVIADO = "P", 
			  				   EXCLUIDO = "E", 
			  				   PEDIDO_ENVIADO = "N", 
			  				   PEDIDO_RETORNADO_BLOQUEADO ="RB", 
			  				   PEDIDO_RETORNADO_LIBERADO ="RL", 
			  				   PEDIDO_RETORNADO_EXCLUIDO ="RE",
			  				   PEDIDO_FATURADO = "F";
	public static final String ORDEM_DECRESCENTE = "D";
	public static final String ORDEM_CRESCENTE = "C";

	public OrcamentoRotinas(Context context) {
		super(context);
	}
	
	/**
	 * Cria o cabecalho do orcamento no banco de dados.
	 * Tem que ser passado os dados do cliente por um @ContentValues.
	 * \n
	 * @param dadosCliente
	 * @return
	 */
	public long insertOrcamento(ContentValues dadosCliente){
		
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		return orcamentoSql.insert(dadosCliente);
	}

	/**
	 * Busca todos os produtos do orcamento.
	 * @param where
	 * @param idOrcamento
	 * @return
	 */
	public List<ItemOrcamentoBeans> listaItemOrcamentoResumida(String where, String idOrcamento){
		
		String sql = "SELECT AEAITORC.ID_AEAITORC, AEAITORC.ID_AEAORCAM, AEAPRODU.DESCRICAO AS DESCRICAO_PRODU, AEAMARCA.DESCRICAO AS DESCRICAO_MARCA, "
				   + "AEAPRODU.ID_AEAPRODU, AEAPRODU.CODIGO_ESTRUTURAL, AEAITORC.QUANTIDADE, AEAITORC.FC_LIQUIDO_UN, (AEAITORC.VL_BRUTO / AEAITORC.QUANTIDADE) AS VL_BRUTO_UN, "
				   + "AEAITORC.VL_BRUTO, AEAITORC.VL_TABELA, AEAITORC.FC_LIQUIDO, AEAITORC.VL_DESCONTO, AEAITORC.FC_DESCONTO_UN, AEAITORC.COMPLEMENTO, "
				   + "AEAUNVEN.SIGLA SIGLA_EMBALAGEM "
				   + "FROM AEAITORC AEAITORC "
				   + "LEFT OUTER JOIN AEAUNVEN AEAUNVEN "
				   + "ON  (AEAITORC.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN) "
				   + "LEFT OUTER JOIN AEAESTOQ AEAESTOQ "
				   + "ON  (AEAITORC.ID_AEAESTOQ = AEAESTOQ.ID_AEAESTOQ) "
				   + "LEFT OUTER JOIN AEAPLOJA AEAPLOJA "
				   + "ON  (AEAESTOQ.ID_AEAPLOJA = AEAPLOJA.ID_AEAPLOJA) "
				   + "LEFT OUTER JOIN AEAPRODU AEAPRODU "
				   + "ON  (AEAITORC.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU) "
				   + "LEFT OUTER JOIN AEAMARCA AEAMARCA "
				   + "ON  (AEAMARCA.ID_AEAMARCA = AEAPRODU.ID_AEAMARCA) "
				   + "WHERE (AEAITORC.ID_AEAORCAM = " + idOrcamento + ") ";
		
		if(where != null){
			sql = sql + "AND (" + where + ") ";
		}
		// Adiciona a clausula de ordem ao sql
		sql = sql + "ORDER BY AEAITORC.SEQUENCIA ";
		
		List<ItemOrcamentoBeans> listaItemOrcamento = new ArrayList<ItemOrcamentoBeans>();
		
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		// Executa o sql e armazena os registro em um Cursor
		Cursor cursor = orcamentoSql.sqlSelect(sql);
		
		// Verifica se retornou algum registro
		if(cursor.getCount() > 0){
			// Move para o primeiro registro
			//cursor.moveToFirst();
			
			while (cursor.moveToNext()) {
				// Intancia a classe de Itens de orcamento para armazenar os dados de cada registro
				ItemOrcamentoBeans item = new ItemOrcamentoBeans();
				
				item.setIdItemOrcamento(cursor.getInt(cursor.getColumnIndex("ID_AEAITORC")));
				item.setIdOrcamento(cursor.getInt(cursor.getColumnIndex("ID_AEAORCAM")));
				item.setQuantidade(cursor.getDouble(cursor.getColumnIndex("QUANTIDADE")));
				item.setValorLiquidoUnitario(cursor.getDouble(cursor.getColumnIndex("FC_LIQUIDO_UN")));
				item.setValorLiquido(cursor.getDouble(cursor.getColumnIndex("FC_LIQUIDO")));
				item.setValorBrutoUnitario(cursor.getDouble(cursor.getColumnIndex("VL_BRUTO_UN")));
				item.setValorBruto(cursor.getDouble(cursor.getColumnIndex("VL_BRUTO")));
				item.setValorTabela(cursor.getDouble(cursor.getColumnIndex("VL_TABELA")));
				item.setValorDesconto(cursor.getDouble(cursor.getColumnIndex("VL_DESCONTO")));
				item.setValorDescontoUnitario(cursor.getDouble(cursor.getColumnIndex("FC_DESCONTO_UN")));
				item.setComplemento(cursor.getString(cursor.getColumnIndex("COMPLEMENTO")));
				
				// Instancia a classe de unidade de venda
				UnidadeVendaBeans unidadeVenda = new UnidadeVendaBeans();
				unidadeVenda.setSiglaUnidadeVenda(cursor.getString(cursor.getColumnIndex("SIGLA_EMBALAGEM")));
				item.setUnidadeVenda(unidadeVenda);
				
				
				// Intancia a classe de produto
				ProdutoBeans produto = new ProdutoBeans();
				produto.setDescricaoProduto(cursor.getString(cursor.getColumnIndex("DESCRICAO_PRODU")));
				produto.setDescricaoMarca(cursor.getString(cursor.getColumnIndex("DESCRICAO_MARCA")));
				produto.setCodigoEstrutural(cursor.getString(cursor.getColumnIndex("CODIGO_ESTRUTURAL")));
				produto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
				item.setProduto(produto);
				
				// Adiciona o item na lista
				listaItemOrcamento.add(item);
				
			} // Fim do while
			
		} /*else {
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "OrcamentoRotinas");
			mensagem.put("mensagem", "N�o existe registros cadastrados");
			
			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}*/
		
		return listaItemOrcamento;
	} // Fim da funcao listaItemOrcamentoResumida
	
	
	
	/**
	 * Cria uma lista apenas com os codigo dos produtos de um determinado orcamento.
	 * Tem que ser passado por parametro o id do orcamento.
	 * 
	 * @param where
	 * @param idOrcamento
	 * @return
	 */
	public List<String> listaItemOrcamentoApenasID(String where, String idOrcamento){
		// Cria uma lista para armazenar os codigos
		List<String> listaIDItem = new ArrayList<String>();
		
		try {
			String sql = "SELECT AEAITORC.ID_AEAPRODU "
					   + "FROM AEAITORC AEAITORC "
					   + "LEFT OUTER JOIN AEAPRODU AEAPRODU "
					   + "ON  (AEAITORC.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU) "
					   + "WHERE (AEAITORC.ID_AEAORCAM = " + idOrcamento + ") ";
			
			if(where != null){
				sql = sql + "AND (" + where + ") ";
			}
			// Adiciona a clausula de ordem ao sql
			sql = sql + "ORDER BY AEAITORC.SEQUENCIA ";
			
			OrcamentoSql orcamentoSql = new OrcamentoSql(context);
			// Executa o sql e armazena os registro em um Cursor
			Cursor cursor = orcamentoSql.sqlSelect(sql);
			
			// Verifica se retornou algum registro
			if((cursor != null) && (cursor.getCount() > 0)){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				for(int i = 0; cursor.getCount() > i; i++) {
					// Recebe o id do produto
					String item = cursor.getString(cursor.getColumnIndex("ID_AEAPRODU"));
					// Adiciona o item na lista
					listaIDItem.add(item);
					// Move para o proximo registro do banco de dados
					cursor.moveToNext();
					
				} // Fim do for
				
			}/* else {
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "OrcamentoRotinas");
				mensagem.put("mensagem", "Não existe registros cadastrados");
				
				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			}*/
			
		}catch(Exception e){
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 0);
			mensagem.put("tela", "OrcamentoRotinas");
			mensagem.put("mensagem", "Erro ao pegar a lista de itens do orcamento. \n" + e.getMessage());
			mensagem.put("dados", "OrcamentoRotinas" + e + " | " + listaIDItem);
			mensagem.put("usuario", funcoes.getValorXml("Usuario"));
			mensagem.put("usuario", funcoes.getValorXml("ChaveEmpresa"));
			mensagem.put("usuario", funcoes.getValorXml("Email"));
			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}
		
		return listaIDItem;
	} // Fim da funcao listaItemOrcamentoApenasID
	
	
	/**
	 * Retorno um lista de ids de orcamentos de acordo com o tipo
	 * de orcamento passado por paramentro.
	 * 
	 * @param tipo - O = Orcamento, P = Pedido, E = Excluido, N = Pedidos Enviados
	 * @param where
	 * @return
	 */
	public List<String> listaIdOrcamento(String tipo, String where, String tipoOrdem){
		// Cria uma lista para armazenar os codigos
		List<String> listaIdOrcamento = new ArrayList<String>();
		
		try {
			// Constroi um sql
			String sql = "SELECT AEAORCAM.ID_AEAORCAM "
					   + "FROM AEAORCAM "
					   + "LEFT OUTER JOIN CFAESTAD CFAESTAD ON(AEAORCAM.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD) "
					   + "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) "
					   + "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
			
			// Adiciona a clausula where
			if(where != null){
				sql = sql + " AND ( " + where + " )";
			}
			// Adiciona um ordem no sql
			sql = sql + " ORDER BY AEAORCAM.DT_CAD ";
			
			if((tipoOrdem != null) && (tipoOrdem.equals(ORDEM_DECRESCENTE))){
				sql += " DESC, AEAORCAM.ID_AEAORCAM DESC";
			} else {
				sql += " ASC, AEAORCAM.ID_AEAORCAM ASC";
			}
			
			OrcamentoSql orcamentoSql = new OrcamentoSql(context);
			// Executa o sql e armazena os registro em um Cursor
			Cursor cursor = orcamentoSql.sqlSelect(sql);
			
			// Verifica se retornou algum registro
			if((cursor != null) && (cursor.getCount() > 0)){
				// Passa por todos os registro
				while (cursor.moveToNext()) {
					// Pega o id do orcamento
					listaIdOrcamento.add(cursor.getString(cursor.getColumnIndex("ID_AEAORCAM")));
				}
			}
		} catch (Exception e) {
			Log.i("Script", e.getMessage());
		}
		
		return listaIdOrcamento;
	} // Fim da funcao listaItemOrcamentoApenasID

	
	/**
	 * Retorna uma lista de orcamento ou pedido, de acordo com o tipo passado por paramento.
	 * @param tipoOrdem é um parametro para ordernar o resultado, por padrao a ordem eh ASC (Crescente),
	 * para escolher a ordem decrescente para colocar como parametro a letra D outro usar a 
	 * variavel public {@value}ORDEM_DECRESCENTE.
	 * 
	 * @param listaTipo - 'O' = ORCAMENTO, 'P' = PEDIDO, 'E' = EXCLUIDO, 'N' = ENVIADOS
	 * @param where
	 * @param tipoOrdem
	 * @return
	 */
	public List<OrcamentoBeans> listaOrcamentoPedido(String[] listaTipo, String where, String tipoOrdem){
		// Cria uma vareavel para salvar uma lista
		List<OrcamentoBeans> listaOrcamento = new ArrayList<OrcamentoBeans>();
		
		try {
			// Constroi um sql
			String sql = "SELECT AEAORCAM.ID_AEAORCAM, AEAORCAM.DT_CAD, AEAORCAM.ID_CFACLIFO, AEAORCAM.NOME_CLIENTE, AEAORCAM.FC_VL_TOTAL, AEAORCAM.FC_VL_TOTAL_FATURADO, AEAORCAM.ATAC_VAREJO, "
					   + "AEAORCAM.VL_MERC_BRUTO, AEAORCAM.VL_TABELA, AEAORCAM.VL_TABELA_FATURADO, AEAORCAM.STATUS, CFAESTAD.UF, CFACIDAD.DESCRICAO, AEAORCAM.STATUS_RETORNO "
					   + "FROM AEAORCAM "
					   + "LEFT OUTER JOIN CFAESTAD CFAESTAD ON(AEAORCAM.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD) "
					   + "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) ";
					   //+ "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
			
			if(listaTipo.length > 0){
				// adiciona a plavra where no select
				 sql += " WHERE (AEAORCAM.STATUS = '";
				 
				 int controle = 0;
				 
				 // Passa por todos os tipos da lista
				 for (String tipo : listaTipo) {
					controle++;
					sql += tipo;
					// Checa se eh o ultimo da lista de tipos de pesquisa
					if(controle < listaTipo.length){
						// Adiciona um OR no select
						sql += "' OR AEAORCAM.STATUS = '";
					} else {
						sql += "') ";
					}
				}
			}
			
			/*// Checa se eh para listar todos os pedidos
			if(tipo.equalsIgnoreCase("TP")){
				sql += "WHERE (AEAORCAM.STATUS = 'P') OR (AEAORCAM.STATUS = 'N')";
			} else {
				sql += "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
			}*/
			
			// Adiciona a clausula where
			if(where != null){
				sql = sql + " AND ( " + where + " )";
			}
			// Adiciona um ordem no sql
			sql = sql + " ORDER BY AEAORCAM.DT_CAD ";
			
			if((tipoOrdem != null) && (tipoOrdem.equals(ORDEM_CRESCENTE))){
				sql += " ASC, AEAORCAM.ID_AEAORCAM ASC";
			} else {
				sql += " DESC, AEAORCAM.ID_AEAORCAM DESC";
			}
			
			OrcamentoSql orcamentoSql = new OrcamentoSql(context);
			// Executa o sql e armazena os registro em um Cursor
			Cursor cursor = orcamentoSql.sqlSelect(sql);
			
			// Verifica se retornou algum registro
			if((cursor != null) && (cursor.getCount() > 0)){
				FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
				
				// Passa por todos os registro
				while (cursor.moveToNext()) {
					OrcamentoBeans orcamento = new OrcamentoBeans();
					// Pega os dados do orcamento
					orcamento.setIdOrcamento(cursor.getInt(cursor.getColumnIndex("ID_AEAORCAM")));
					orcamento.setNomeRazao(cursor.getString(cursor.getColumnIndex("NOME_CLIENTE")));
					orcamento.setDataCadastro(funcoes.formataDataHora(cursor.getString(cursor.getColumnIndex("DT_CAD"))));
					orcamento.setTotalOrcamento(cursor.getDouble(cursor.getColumnIndex("FC_VL_TOTAL")));
					orcamento.setTotalOrcamentoFaturado(cursor.getDouble(cursor.getColumnIndex("FC_VL_TOTAL_FATURADO")));
					orcamento.setTotalOrcamentoBruto(cursor.getDouble(cursor.getColumnIndex("VL_MERC_BRUTO")));
					orcamento.setTotalTabela(cursor.getDouble(cursor.getColumnIndex("VL_TABELA")));
					orcamento.setTotalTabelaFaturado(cursor.getDouble(cursor.getColumnIndex("VL_TABELA_FATURADO")));
					orcamento.setTipoVenda(cursor.getString(cursor.getColumnIndex("ATAC_VAREJO")).charAt(0));
					orcamento.setStatus(cursor.getString(cursor.getColumnIndex("STATUS")));
					orcamento.setSiglaEstado(cursor.getString(cursor.getColumnIndex("UF")));
					orcamento.setCidade(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
					orcamento.setIdPessoa(cursor.getInt(cursor.getColumnIndex("ID_CFACLIFO")));
					orcamento.setStatusRetorno(cursor.getString(cursor.getColumnIndex("STATUS_RETORNO")));
					
					// Adiciona os dados do orcamento em uma lista
					listaOrcamento.add(orcamento);
				}
			}
		} catch (Exception e) {
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoRotinas");
			mensagem.put("mensagem", "Não foi possível pegar os dados dos orçamentos/pedidos. \n" + e.getMessage());
			
			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}
		
		return listaOrcamento;
	}


	public List<OrcamentoBeans> listaTotalVendaDiarioCliente(String idClifo, String where, String mes, String ano){
		List<OrcamentoBeans> lista = new ArrayList<OrcamentoBeans>();

		try {
			String sql = "SELECT AEAORCAM.ID_CFACLIFO, STRFTIME('%d', AEAORCAM.DT_CAD) AS DIA, AEAORCAM.DT_CAD, AEAORCAM.FC_VL_TOTAL, AEAORCAM.FC_VL_TOTAL_FATURADO " +
						 "FROM AEAORCAM " +
						 "WHERE (AEAORCAM.STATUS = 'P' OR AEAORCAM.STATUS = 'N') " +
						 " AND (AEAORCAM.ID_CFACLIFO = " + idClifo + ") ";

			if (mes != null){
				sql += " AND ((STRFTIME('%m', AEAORCAM.DT_CAD)) = '" + mes + "') ";
			} else {
				sql += " AND ((STRFTIME('%m', AEAORCAM.DT_CAD)) = strftime('%m','now'))";
			}

			if (ano != null){
				sql += " AND ((STRFTIME('%Y', AEAORCAM.DT_CAD)) = '" + ano + "') ";
			} else {
				sql += " AND ((STRFTIME('%Y', AEAORCAM.DT_CAD)) = strftime('%Y','now'))";
			}

			if (where != null){
				sql += " AND ( " + where + " ) ";
			}

			OrcamentoSql orcamentoSql = new OrcamentoSql(context);
			// Executa o sql e armazena os registro em um Cursor
			Cursor cursor = orcamentoSql.sqlSelect(sql);

			// Verifica se retornou algum registro
			if((cursor != null) && (cursor.getCount() > 0)){

				// Passa por todos os registro
				while (cursor.moveToNext()) {
					OrcamentoBeans orcamento = new OrcamentoBeans();
					orcamento.setIdPessoa(cursor.getInt(cursor.getColumnIndex("ID_CFACLIFO")));
					orcamento.setDataCadastro(cursor.getString(cursor.getColumnIndex("DIA")));
					orcamento.setTotalOrcamento(cursor.getDouble(cursor.getColumnIndex("FC_VL_TOTAL")));
					orcamento.setTotalOrcamentoFaturado(cursor.getDouble(cursor.getColumnIndex("FC_VL_TOTAL_FATURADO")));

					lista.add(orcamento);
				}
			}
		} catch (Exception e){
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoRotinas");
			mensagem.put("mensagem", context.getResources().getString(R.string.nao_conseguimo_carregar_dados_vendas) + e.getMessage());

			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}

		return lista;
	}
	
	
	public List<TotalMensal> listaTotalVendaMensalCliente(String[] listaTipo, String where, String tipoOrdem){
		// Cria uma vareavel para salvar uma lista
		List<TotalMensal> listaTotalMensal = new ArrayList<TotalMensal>();
		
		try {
			// Constroi um sql
			String sql = "SELECT SUM(AEAORCAM.FC_VL_TOTAL) AS TOTAL_VENDIDO, STRFTIME('%m/%Y', AEAORCAM.DT_CAD) AS MES_ANO, "
					   + "STRFTIME('%m', AEAORCAM.DT_CAD) AS MES, STRFTIME('%Y', AEAORCAM.DT_CAD) AS ANO "
					   + "FROM AEAORCAM ";
					   //+ "LEFT OUTER JOIN CFAESTAD CFAESTAD ON(AEAORCAM.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD) "
					   //+ "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) ";
					   //+ "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
			
			if(listaTipo.length > 0){
				// adiciona a plavra where no select
				 sql += " WHERE ( ";
				 
				 int controle = 0;
				 
				 // Passa por todos os tipos da lista
				 for (String tipo : listaTipo) {
					controle++;
					sql += "(AEAORCAM.STATUS = '" + tipo + "') ";
					// Checa se eh o ultimo da lista de tipos de pesquisa
					if(controle < listaTipo.length){
						// Adiciona um OR no select
						sql += " OR ";
					} else {
						sql += ") ";
					}
				}
			}
			
			/*// Checa se eh para listar todos os pedidos
			if(tipo.equalsIgnoreCase("TP")){
				sql += "WHERE (AEAORCAM.STATUS = 'P') OR (AEAORCAM.STATUS = 'N')";
			} else {
				sql += "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
			}*/
			
			// Adiciona a clausula where
			if(where != null){
				sql = sql + " AND ( " + where + " )";
			}
			// Adiciona um ordem no sql
			sql = sql + " GROUP BY STRFTIME('%m/%Y', AEAORCAM.DT_CAD), STRFTIME('%m', AEAORCAM.DT_CAD), STRFTIME('%Y', AEAORCAM.DT_CAD) ORDER BY AEAORCAM.DT_CAD ";
			
			if((tipoOrdem != null) && (tipoOrdem.equals(ORDEM_DECRESCENTE))){
				sql += " DESC ";
			} else {
				sql += " ASC ";
			}
			
			OrcamentoSql orcamentoSql = new OrcamentoSql(context);
			// Executa o sql e armazena os registro em um Cursor
			Cursor cursor = orcamentoSql.sqlSelect(sql);
			
			// Verifica se retornou algum registro
			if((cursor != null) && (cursor.getCount() > 0)){
				
				// Passa por todos os registro
				while (cursor.moveToNext()) {
					TotalMensal totalMensal = new TotalMensal();
					
					totalMensal.setAno(cursor.getString(cursor.getColumnIndex("ANO")));
					totalMensal.setMes(cursor.getString(cursor.getColumnIndex("MES")));
					totalMensal.setMesAno(cursor.getString(cursor.getColumnIndex("MES_ANO")));
					totalMensal.setTotal(cursor.getDouble(cursor.getColumnIndex("TOTAL_VENDIDO")));
					
					// Adiciona os dados do orcamento em uma lista
					listaTotalMensal.add(totalMensal);
				}
			}
		} catch (Exception e) {
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 1);
			mensagem.put("tela", "OrcamentoRotinas");
			mensagem.put("mensagem", "N�o foi poss�vel pegar o total mensal dos pedidos. \n" + e.getMessage());
			
			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}
		
		return listaTotalMensal;
	}
	
	/**
	 * Retorna uma lista de cidades dos orcamentos ou pedidos que
	 * estao no banco de dados.
	 * 
	 * @param tipo - 'O' = ORCAMENTO, 'P' = PEDIDO, 'E' = EXCLUIDO, 'N' = ENVIADOS
	 * @param where
	 * @return
	 */
	public List<DescricaoSimplesBeans> listaCidadeOrcamentoPedido(String tipo, String where){
		// Cria uma lista para retornar as cidades
		List<DescricaoSimplesBeans> lista = new ArrayList<DescricaoSimplesBeans>();
		
		
		String sql = "SELECT CFAESTAD.UF, CFACIDAD.DESCRICAO "
				   + "FROM AEAORCAM "
				   + "LEFT OUTER JOIN CFAESTAD CFAESTAD ON(AEAORCAM.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD) "
				   + "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) "
				   + "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
		
		/*// Checa se eh para listar todos os pedidos
		if(tipo.equalsIgnoreCase("TP")){
			sql += "WHERE (AEAORCAM.STATUS = 'P') OR (AEAORCAM.STATUS = 'N')";
		} else {
			sql += "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
		}*/
		
		// Adiciona a clausula where
		if(where != null){
			sql = sql + " AND ( " + where + " )";
		}
		// Adiciona um ordem no sql
		sql = sql + " GROUP BY CFAESTAD.UF, CFACIDAD.DESCRICAO ORDER BY CFAESTAD.UF, CFACIDAD.DESCRICAO ";
		
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		// Executa o sql e armazena os registro em um Cursor
		Cursor cursor = orcamentoSql.sqlSelect(sql);
		
		if((cursor != null) && (cursor.getCount() > 0)){
						
			while (cursor.moveToNext()) {
				// Instancia a classe para salvar o nome da cidade
				DescricaoSimplesBeans cidade = new DescricaoSimplesBeans();
				// Seta o texto principal com o nome da cidade
				cidade.setTextoPrincipal(cursor.getString(cursor.getColumnIndex("UF")) + " - " + cursor.getString(cursor.getColumnIndex("DESCRICAO")));
				// Adiciona a cidade em uma lista
				lista.add(cidade);
			}
			
		} else {
			lista.add(new DescricaoSimplesBeans("Nenhum valor encontrado"));
		}
		
		// Adiciona um valor padrao para selecionar todas as cidades
		lista.add(new DescricaoSimplesBeans("Todas as Cidades"));
		
		return lista;
	} // Fim listaCidadePessoa
	
	
	/**
	 * Retorna o status do Orcamento.
	 * 
	 * @param idOrcamento
	 * @return O = Orcamento, P = Pedido, E = Excluido
	 */
	public String statusOrcamento(String idOrcamento){
		String retorno = "";
		
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		Cursor cursor = orcamentoSql.query(" ID_AEAORCAM = " + idOrcamento);
		
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Move o cursor para o primeiro registro
			cursor.moveToFirst();
			
			retorno = cursor.getString(cursor.getColumnIndex("STATUS"));
		}
		return retorno;
	}
	
	/**
	 * Busca o codigo do cliente atraves do numero do orcamento.
	 * @param idOrcamento
	 * @return
	 */
	public String codigoClienteOrcamento(String idOrcamento){
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		// Executa o sql e armazena os registro em um Cursor
		Cursor cursor = orcamentoSql.query("ID_AEAORCAM = " + idOrcamento);
		
		// Verifica se retornou algum registro
		if(cursor != null && cursor.getCount() > 0){
			return cursor.getString(cursor.getColumnIndex("ID_CFACLIFO"));
		}else {
			return "0";
		}
		
	} // Fim codigoClienteOrcamento
	
	
	
	public long insertItemOrcamento(ContentValues dados){
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		// Inseri o item no orcamento
		long idItemOrcamento = itemOrcamentoSql.insert(dados);
		// Salva um backup na pasta
		
		return idItemOrcamento;
		
	} // Fim insertItemOrcamento
	
	
	
	
	public int updateItemOrcamento(ContentValues dados, String id){
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		// Atualiza o item no orcamento
		return itemOrcamentoSql.update(dados, " ID_AEAITORC = " + id);
	}
	
	
	
	public int updateOrcamento(ContentValues dados, String idOrcamento){
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		// Atualiza o item no orcamento
		return orcamentoSql.update(dados, " ID_AEAORCAM = " + idOrcamento);
	}
	
	
	/**
	 * Atualiza todos os planos de pagamentos dos produtos de um determinado orcamento.
	 * @param dados
	 * @param idOrcamento
	 * @return
	 */
	public int updatePlanoPagamentoItemOrcamento(ContentValues dados, String idOrcamento){
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		int i = itemOrcamentoSql.update(dados, " ID_AEAORCAM = " + idOrcamento);
		// Atualiza o item no orcamento
		return i;
	}
	
	
	
	/**
	 * Pega o maior sequencial de um determinado orcamento.
	 * 
	 * @param idOrcamento
	 * @return
	 */
	public int proximoSequencial(String idOrcamento){
		// Instacia a classe para manipular os itens do orcamento no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		// Pega os dados
		Cursor cursor = itemOrcamentoSql.sqlSelect("SELECT MAX(SEQUENCIA) AS SEQUENCIA FROM AEAITORC WHERE ID_AEAITORC = " + idOrcamento);
		
		if((cursor.getCount() > 0) && (cursor != null)){
			// Move o cursor para o primeiro registro
			cursor.moveToFirst();
			// Pega a sequencia retornada do banco de dados
			int sequencia = cursor.getInt(cursor.getColumnIndex("SEQUENCIA"));
			
			return sequencia + 1;
		}
		
		return 0;
	} // Fim da proximoSequencial

	
	
	/**
	 * Pega os dados apenas de um produto que esta dentro do orcamento.
	 *  
	 * @param idOrcamento
	 * @param idProduto
	 * @return
	 */
	public ItemOrcamentoBeans selectItemOrcamento(String idOrcamento, String idProduto){
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		Cursor cursor = itemOrcamentoSql.query("ID_AEAORCAM = " + idOrcamento + " AND ID_AEAPRODU = " + idProduto);
		
		// Move o cursor para o primeiro registro
		cursor.moveToFirst();
		
		ItemOrcamentoBeans itemOrcamentoBeans = new ItemOrcamentoBeans();
		// Preenche os dados recuperados do banco de dados
		itemOrcamentoBeans.setComplemento(cursor.getString(cursor.getColumnIndex("COMPLEMENTO")));
		itemOrcamentoBeans.setIdOrcamento(cursor.getInt(cursor.getColumnIndex("ID_AEAORCAM")));
		itemOrcamentoBeans.setIdItemOrcamento(cursor.getInt(cursor.getColumnIndex("ID_AEAITORC")));
		if(cursor.getString(cursor.getColumnIndex("PROMOCAO")) != null){
			itemOrcamentoBeans.setPromocao(cursor.getString(cursor.getColumnIndex("PROMOCAO")).charAt(0));
		}
		itemOrcamentoBeans.setQuantidade(cursor.getDouble(cursor.getColumnIndex("QUANTIDADE")));
		itemOrcamentoBeans.setSeguencia(cursor.getInt(cursor.getColumnIndex("SEQUENCIA")));
		itemOrcamentoBeans.setGuid(cursor.getString(cursor.getColumnIndex("GUID")));
		itemOrcamentoBeans.setValorLiquido(cursor.getDouble(cursor.getColumnIndex("FC_LIQUIDO")));
		itemOrcamentoBeans.setValorBruto(cursor.getDouble(cursor.getColumnIndex("VL_BRUTO")));
		itemOrcamentoBeans.setValorTabela(cursor.getDouble(cursor.getColumnIndex("VL_TABELA")));
		itemOrcamentoBeans.setValorDesconto(cursor.getDouble(cursor.getColumnIndex("VL_DESCONTO")));
		// Instancia a classe de rotinas de embalagem
		EmbalagemRotinas embalagemRotinas = new EmbalagemRotinas(context);
		// Instancia a classe de produto para salvar a embalagem dentro do produto
		ProdutoBeans produto = new ProdutoBeans();
		// Adiciona uma lista de embalagem a variavel produto
		produto.setListaEmbalagem(embalagemRotinas.selectEmbalagensProduto(idProduto));
		// Adiciona o produto ao item de orcamento
		itemOrcamentoBeans.setProduto(produto);
		
		return itemOrcamentoBeans;
	} // Fim selectItemOrcamento
	
	
	
	public String selectObservacaoOrcamento(String idOrcamento){
		String retorno = "";
		
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		Cursor cursor = orcamentoSql.query(" ID_AEAORCAM = " + idOrcamento);
		
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Move o cursor para o primeiro registro
			cursor.moveToFirst();
			
			retorno = cursor.getString(cursor.getColumnIndex("OBS"));
		}
		return retorno;
	} // Fim selectObservacaoOrcamento
	
	
	
	public PessoaBeans selectDadosClienteOrcamento(String idOrcamento){
		Cursor dadosRetorno = null;
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		String sql = "SELECT AEAORCAM.ID_AEAORCAM, CFACLIFO.CODIGO_CLI, CFACLIFO.NOME_RAZAO, CFACLIFO.NOME_FANTASIA, "
				   + "CFAENDER.LOGRADOURO, CFAENDER.NUMERO, CFAENDER.BAIRRO, CFAENDER.CEP, CFAENDER.COMPLEMENTO, "
				   + "CFAESTAD.UF, CFACIDAD.DESCRICAO AS DESCRICAO_CIDADE"
				   + "FROM AEAORCAM "
				   + "LEFT OUTER JOIN CFACLIFO CFACLIFO "
				   + "ON(AEAORCAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) "
				   + "LEFT OUTER JOIN CFAENDER CFAENDER "
				   + "ON(CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) "
				   + "LEFT OUTER JOIN CFACIDAD CFACIDAD "
				   + "ON(CFAENDER.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) "
				   + "LEFT OUTER JOIN CFAESTAD CFAESTAD "
				   + "ON(CFACIDAD.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD) "
				   + "WHERE (CFACLIFO.ID_CFACLIFO) AND (AEAORCAM.ID_AEAORCAM = " + idOrcamento + " )";
		
		dadosRetorno = orcamentoSql.query(sql);
		PessoaBeans pessoa = new PessoaBeans();
		// Checa se retornou algum registro
		if(dadosRetorno != null && dadosRetorno.getCount() > 0){
			
			pessoa.setCodigoCliente(dadosRetorno.getInt(dadosRetorno.getColumnIndex("CODIGO_CLI")));
			pessoa.setNomeRazao(dadosRetorno.getString(dadosRetorno.getColumnIndex("NOME_RAZAO")));
			pessoa.setNomeFantasia(dadosRetorno.getString(dadosRetorno.getColumnIndex("NOME_FATASIA")));
			
			EnderecoBeans endereco = new EnderecoBeans();
			endereco.setLogradouro(dadosRetorno.getString(dadosRetorno.getColumnIndex("LOGRADOURO")));
			endereco.setNumero(dadosRetorno.getString(dadosRetorno.getColumnIndex("NUMERO")));
			endereco.setBairro(dadosRetorno.getString(dadosRetorno.getColumnIndex("BAIRRO")));
			endereco.setCep(dadosRetorno.getString(dadosRetorno.getColumnIndex("CEP")));
			endereco.setComplemento(dadosRetorno.getString(dadosRetorno.getColumnIndex("COMPLEMENTO")));
			
			pessoa.setEnderecoPessoa(endereco);
			
			CidadeBeans cidade = new CidadeBeans();
			cidade.setDescricao(dadosRetorno.getString(dadosRetorno.getColumnIndex("DESCRICAO_CIDADE")));
			
			pessoa.setCidadePessoa(cidade);
			
		}
		
		return pessoa;
	} // Fim selectDadosClienteOrcamento

	
	/**
	 * Retorna todos os itens de um orcamento.
	 * 
	 * @param idOrcamento
	 * @return
	 */
	public Cursor selectItensOrcamento(String idOrcamento){
		Cursor dadosRetorno = null;
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		// Criar o sql para pegar os dados do banco
		String sql = "SELCET AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.DESCRICAO AS DESCRICAO_PRODU, AEAMARCA.DESCRICAO AS DESCRICAO_MARCA, "
				   + "AEAUNVEN.SIGLA, AEAEMBAL.DESCRICAO AS DESCRICAO_EMBAL, AEAITORC.QUANTIDADE, AEAITORC.FC_LIQUIDO_UN, AEAITORC.FC_LIQUIDO "
				   + "FROM AEAITORC "
				   + "LEFT OUTER JOIN AEAPRODU AEAPRODU "
				   + "ON(AEAITORC.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU) "
				   + "LEFT OUTER JOIN AEAMARCA AEAMARCA "
				   + "ON(AEAPRODU.ID_AEAMARCA = AEAMARCA.ID_AEAMARCA) "
				   + "LEFT OUTER JOIN AEAUNVEN AEAUNVEN "
				   + "ON(AEAPRODU.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN) "
				   + "LEFT OUTER JOIN AEAEMBAL AEAEMBAL "
				   + "ON(AEAEMBAL.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU) "
				   + "WHERE (AEAITORC.ID_AEAORCAM = " + idOrcamento + " ) ";
		
		dadosRetorno = orcamentoSql.query(sql);
		
		return dadosRetorno;
	} // Fim selectDadosClienteOrcamento

	
	
	public String totalOrcamentoLiquido(String idOrcamento){
		String valor = null;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		Cursor cursor = itemOrcamentoSql.sqlSelect("SELECT SUM(AEAITORC.FC_LIQUIDO) TOTAL_ORCAMENTO FROM AEAITORC WHERE AEAITORC.ID_AEAORCAM = " + idOrcamento);
		// Move para o primeiro registro
		cursor.moveToFirst();
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Pega o valor salvo no cursor
			valor = cursor.getString(cursor.getColumnIndex("TOTAL_ORCAMENTO"));
		} else {
			valor = "0";
		}
		// Intancia a classe para executar algumas funcoes especiais
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		return funcoes.arredondarValor(valor);
		
	} // Fim totalOrcamentoLiquido
	
	
	/**
	 * Funcao para pegar o total de orcamentos de uma lista,
	 * de acordo com o status do orcamento.
	 * 
	 * @param tipo - O = Orcamento, P = Pedido, E = Excluido
	 * @return
	 */
	public String totalListaOrcamentoLiquido(String tipo, String cidade, String where){
		String valor = null;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		String sql = ("SELECT SUM(AEAORCAM.FC_VL_TOTAL) AS FC_VL_TOTAL "
				    + "FROM AEAORCAM "
					+ "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) "
					+ "WHERE (AEAORCAM.STATUS = '" + tipo + "') ");
		
		/*// Checa se eh para listar todos os pedidos
		if(tipo.equalsIgnoreCase("TP")){
			sql += "WHERE (AEAORCAM.STATUS = 'P') OR (AEAORCAM.STATUS = 'N')";
		} else {
			sql += "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
		}*/
		
		if(cidade != null){
			sql = sql + " AND (CFACIDAD.DESCRICAO = '" + cidade +"' )";
		}
		
		if(where != null){
			sql += " AND (" + where + ")";
		}
		
		Cursor cursor = itemOrcamentoSql.sqlSelect(sql);
		
		// Move para o primeiro registro
		cursor.moveToFirst();
		// Checa se retornou algum registro
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Pega o valor salvo no cursor
			valor = cursor.getString(cursor.getColumnIndex("FC_VL_TOTAL"));
		} else {
			valor = "0";
		}
		// Intancia a classe para executar algumas funcoes especiais
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		return funcoes.arredondarValor(valor);
		
	} // Fim totalListaOrcamentoLiquido
	
	
	/**
	 * Funcao para pegar o total bruto(tabela) de orcamentos de uma lista,
	 * de acordo com o status do orcamento.
	 * 
	 * @param tipo - O = Orcamento, P = Pedido, E = Excluido
	 * @return
	 */
	public String totalListaOrcamentoBruto(String tipo, String cidade, String where){
		String valor = null;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		String sql = ("SELECT SUM(AEAORCAM.VL_MERC_BRUTO) AS VL_MERC_BRUTO "
				    + "FROM AEAORCAM "
					+ "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) "
					+ "WHERE (AEAORCAM.STATUS = '" + tipo + "') ");
		
		/*// Checa se eh para listar todos os pedidos
		if(tipo.equalsIgnoreCase("TP")){
			sql += "WHERE (AEAORCAM.STATUS = 'P') OR (AEAORCAM.STATUS = 'N')";
		} else {
			sql += "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
		}*/
		
		if(cidade != null){
			sql = sql + " AND (CFACIDAD.DESCRICAO = '" + cidade +"' )";
		}
		
		if(where != null){
			sql += " AND (" + where + ")";
		}
		
		Cursor cursor = itemOrcamentoSql.sqlSelect(sql);
		
		// Move para o primeiro registro
		cursor.moveToFirst();
		// Checa se retornou algum registro
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Pega o valor salvo no cursor
			valor = cursor.getString(cursor.getColumnIndex("VL_MERC_BRUTO"));
		} else {
			valor = "0";
		}
		// Intancia a classe para executar algumas funcoes especiais
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		return funcoes.arredondarValor(valor);
		
	} // Fim totalListaOrcamentoBruto
	
	
	/**
	 * Funcao para retornar a quantidade de orcamento presente em uma lista.
	 * 
	 * @param tipo - O = Orcamento, P = Pedido, E = Excluido, N = Pedidos Enviados
	 * @return
	 */
	public String quantidadeListaOrcamento(String tipo, String cidade, String where){
		String valor = null;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		// Monta o sql
		String sql = ("SELECT COUNT(*) AS QUANTIDADE_ORCAMENTO "
			        + "FROM AEAORCAM "
			        + "LEFT OUTER JOIN CFACIDAD CFACIDAD ON(AEAORCAM.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) "
			        + "WHERE (AEAORCAM.STATUS = '" + tipo + "') ");
		
		/*// Checa se eh para listar todos os pedidos
		if(tipo.equalsIgnoreCase("TP")){
			sql += "WHERE (AEAORCAM.STATUS = 'P') OR (AEAORCAM.STATUS = 'N')";
		} else {
			sql += "WHERE (AEAORCAM.STATUS = '" + tipo + "') ";
		}*/
		
		if(cidade != null){
			sql = sql + " AND (CFACIDAD.DESCRICAO = '" + cidade + "' )";
		}
		
		if(where != null){
			sql += " AND (" + where + ")";
		}
		
		// Executa o sql
		Cursor cursor = itemOrcamentoSql.sqlSelect(sql);
		
		// Move para o primeiro registro
		cursor.moveToFirst();
		// Checa se retornou algum registro
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Pega o valor salvo no cursor
			valor = cursor.getString(cursor.getColumnIndex("QUANTIDADE_ORCAMENTO"));
		} else {
			valor = "0";
		}
		// Intancia a classe para executar algumas funcoes especiais
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		return funcoes.arredondarValor(valor);
		
	} // Fim totalListaOrcamentoLiquido
	
	
	
	public String totalOrcamentoBruto(String idOrcamento){
		String valor = null;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		Cursor cursor = itemOrcamentoSql.sqlSelect("SELECT SUM(AEAITORC.VL_BRUTO) TOTAL_BRUTO FROM AEAITORC WHERE AEAITORC.ID_AEAORCAM = " + idOrcamento);
		// Move para o primeiro registro
		cursor.moveToFirst();
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Pega o valor salvo no cursor
			valor = cursor.getString(cursor.getColumnIndex("TOTAL_BRUTO"));
		} else {
			valor = "0";
		}
		// Intancia a classe para executar algumas funcoes especiais
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		return funcoes.arredondarValor(valor);
		
	} // Fim totalOrcamento
	
	
	
	/**
	 * Pega o valor em percentual de desconto de um determinado orcamento.
	 * 
	 * @param idOrcamento
	 * @return
	 */
	public String descontoPercentualOrcamento(String idOrcamento){
		double percentualDesconto = 0;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		Cursor cursor = itemOrcamentoSql.sqlSelect("SELECT SUM(AEAITORC.VL_BRUTO) TOTAL_BRUTO, SUM(AEAITORC.FC_LIQUIDO) TOTAL_LIQUIDO "
												 + "FROM AEAITORC WHERE AEAITORC.ID_AEAORCAM = " + idOrcamento);
		// Move para o primeiro registro
		cursor.moveToFirst();
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Pega o valor salvo no cursor
			double totalBruto = cursor.getDouble(cursor.getColumnIndex("TOTAL_BRUTO"));
			double totalLiquido = cursor.getDouble(cursor.getColumnIndex("TOTAL_LIQUIDO"));
			
			percentualDesconto = (((totalLiquido / totalBruto) * 100) - 100) * -1;
			
		} else {
			percentualDesconto = 0;
		}
		// Intancia a classe para executar algumas funcoes especiais
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		return funcoes.arredondarValor(percentualDesconto);
	} // Fim descontoPercentualOrcamento
	
	
	
	public boolean distribuiDescontoItemOrcamento(String idOrcamento, double totalLiquido, double totalBruto){
		boolean deuCerto = false;

		// Instancia a clase de funcoes personalizadas
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		// Checa se o desconto eh igual a zero
		if( (totalBruto - totalLiquido == 0) || (totalBruto == 0) ){
			
			ContentValues dadosProduto = new ContentValues();
			dadosProduto.put("VL_DESCONTO", 0);
			dadosProduto.put("FC_DESCONTO_UN", 0);

			ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
			// Atualiza os dados de um determinado orcamento
			if(itemOrcamentoSql.update(dadosProduto, "ID_AEAORCAM = " + idOrcamento) > 0 ){
				deuCerto = true;
			}
		} else {
			List<ItemOrcamentoBeans> listaItemOrcamento = new ArrayList<ItemOrcamentoBeans>();

			listaItemOrcamento = listaItemOrcamentoResumida(null, idOrcamento);
			// Checa se a lista foi preenchida com algum valor
			if( (listaItemOrcamento != null) && (listaItemOrcamento.size() > 0) ){
				// Pega o fator de desconto utilizado
				double fatorDesconto = (totalLiquido / totalBruto);

				int qtdAtualizado = 0;
				// Passa por todos os produtos para ratear o desconto
				for(int i = 0; i < listaItemOrcamento.size(); i++){

					double	vlLiquido = listaItemOrcamento.get(i).getValorBruto() * fatorDesconto,
							vlDesconto = listaItemOrcamento.get(i).getValorBruto() - vlLiquido;
					
					ContentValues dadosProduto = new ContentValues();
					dadosProduto.put("VL_DESCONTO", funcoes.desformatarValor(funcoes.arredondarValor(vlDesconto)));
					dadosProduto.put("FC_DESCONTO_UN", funcoes.desformatarValor(funcoes.arredondarValor(vlDesconto / listaItemOrcamento.get(i).getQuantidade())));
					dadosProduto.put("FC_LIQUIDO", funcoes.desformatarValor(funcoes.arredondarValor(vlLiquido)));
					dadosProduto.put("FC_LIQUIDO_UN", funcoes.desformatarValor(funcoes.arredondarValor(vlLiquido / listaItemOrcamento.get(i).getQuantidade())));
					
					ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
					// Atualiza os dados de um determinado orcamento
					if(itemOrcamentoSql.update(dadosProduto, "ID_AEAITORC = " + listaItemOrcamento.get(i).getIdItemOrcamento()) > 0){
						qtdAtualizado ++;
					}
				}
				// Checa se todos os produtos do orcamento foi atualizado
				if (qtdAtualizado == listaItemOrcamento.size()){
					deuCerto = true;
				}
			}
		}
		
		return deuCerto;
	}
	
	
	
	/**
	 * Retorna o id do tipo de documento de um orcamento em especifico.
	 * 
	 * @param idOrcamento
	 * @return
	 */
	public int idTipoDocumentoOrcamento(String idOrcamento){
		int idTipoDocumento = 0;
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		Cursor cursor = orcamentoSql.sqlSelect("SELECT AEAORCAM.ID_CFATPDOC FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento);
		
		// Move para o primeiro registro
		cursor.moveToFirst();
		if( (cursor != null) && (cursor.getCount() > 0) ){
			idTipoDocumento = cursor.getInt(cursor.getColumnIndex("ID_CFATPDOC"));
		}
		
		return idTipoDocumento;
	} // Fim idTipoDocumentoOrcamento
	
	
	
	/**
	 * Retorna o id do plano de pagamento de um orcamento em especifico.
	 * 
	 * @param idOrcamento
	 * @return
	 */
	public int idPlanoPagamentoOrcamento(String idOrcamento){
		int idPlanoPagamento = 0;
		// Instancia a classe para manipular a tabela no banco de dados
		ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
		
		Cursor cursor = itemOrcamentoSql.sqlSelect("SELECT AEAITORC.ID_AEAPLPGT FROM AEAITORC WHERE AEAITORC.ID_AEAORCAM = " + idOrcamento + " ORDER BY AEAITORC.ID_AEAPLPGT ASC LIMIT 1 ");
		
		// Move para o primeiro registro
		cursor.moveToFirst();
		if( (cursor != null) && (cursor.getCount() > 0) ){
			idPlanoPagamento = cursor.getInt(cursor.getColumnIndex("ID_AEAPLPGT"));
		}
		
		return idPlanoPagamento;
	} // Fim idTipoDocumentoOrcamento
	
	
	
	/**
	 * Pega a data de cadastro do orcamento.
	 * 
	 * @param idOrcamento
	 * @return
	 */
	public String dataCadastroOrcamento(String idOrcamento){
		String data = null;
		// Instancia a classe para manipular a tabela no banco de dados
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		String sql = ("SELECT DT_CAD "
				    + "FROM AEAORCAM "
					+ "WHERE (AEAORCAM.DT_CAD = " + idOrcamento + ") ");
		
		Cursor cursor = orcamentoSql.sqlSelect(sql);
		
		// Checa se retornou algum registro
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Move para o primeiro registro
			cursor.moveToFirst();
			// Pega o valor salvo no cursor
			data = cursor.getString(cursor.getColumnIndex("DT_CAD"));
		}
		
		return data;
	}
	
	
} // Fim da classe

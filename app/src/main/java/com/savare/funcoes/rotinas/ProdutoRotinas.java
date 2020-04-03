package com.savare.funcoes.rotinas;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.activity.material.designer.fragment.ProdutoListaMDFragment;
import com.savare.banco.funcoesSql.AreasSql;
import com.savare.banco.funcoesSql.ClasseSql;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.OrcamentoSql;
import com.savare.banco.funcoesSql.PlanoPagamentoSql;
import com.savare.banco.funcoesSql.ProdutoLojaSql;
import com.savare.banco.funcoesSql.ProdutoRecomendadoSql;
import com.savare.banco.funcoesSql.ProdutoSql;
import com.savare.banco.funcoesSql.UnidadeVendaSql;
import com.savare.banco.storedProcedure.CalculaPrecoSP;
import com.savare.beans.AeaclaseBeans;
import com.savare.beans.AeamarcaBeans;
import com.savare.beans.AeaplojaBeans;
import com.savare.beans.AeaproduBeans;
import com.savare.beans.AeaunvenBeans;
import com.savare.beans.AreaBeans;
import com.savare.beans.CidadeBeans;
import com.savare.beans.ClasseBeans;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.DescricaoSimplesBeans;
import com.savare.beans.EmbalagemBeans;
import com.savare.beans.ProdutoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.beans.UnidadeVendaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

public class ProdutoRotinas extends Rotinas {

	public static final String APENAS_PRODUTO_PROMOCAO = "APP";

	public ProdutoRotinas(Context context) {
		super(context);
	}
	
	/**
	 * Funcao para listar todas as classe de produtos, sem filtro.
	 * \n
	 * @return List<DescricaoSimplesBeans>
	 */
	public List<DescricaoSimplesBeans> listaClasse(){
		// Cria uma lista para retornar as cidades
		List<DescricaoSimplesBeans> lista = new ArrayList<DescricaoSimplesBeans>();
		
		String sql = "SELECT AEACLASE.DESCRICAO AS DESCRICAO_CLASE FROM AEACLASE ORDER BY AEACLASE.DESCRICAO";
		
		// Instancia a classe para manipular o banco de dados
		ClasseSql classeSql = new ClasseSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = classeSql.sqlSelect(sql);
		
		if((cursor != null) && (cursor.getCount() > 0)){
			
			lista.add(new DescricaoSimplesBeans("Nenhuma Classe Selecionada"));
						
			while (cursor.moveToNext()) {
				// Instancia a classe para salvar o nome da cidade
				DescricaoSimplesBeans cidade = new DescricaoSimplesBeans();
				// Seta o texto principal com o nome da cidade
				cidade.setTextoPrincipal(cursor.getString(cursor.getColumnIndex("DESCRICAO_CLASE")));
				// Adiciona a cidade em uma lista
				lista.add(cidade);
			}
			
		} else {
			lista.add(new DescricaoSimplesBeans("Nenhum valor encontrado"));
		}
		
		// Adiciona um valor padrao para selecionar todas as cidades
		lista.add(new DescricaoSimplesBeans("Todos os produtos"));
		
		return lista;
	} // Fim listaClasse

	public List<CidadeBeans> listaCidadesMaisVendidos(String where){
		// Cria uma lista para retornar as cidades
		List<CidadeBeans> listaCidade = new ArrayList<CidadeBeans>();

		String sql = "SELECT CFACIDAD.ID_CFACIDAD, CFACIDAD.ID_CFAESTAD, CFACIDAD.DESCRICAO AS DESCRICAO_CIDADE \n" +
					 "FROM AEAPRREC \n" +
					 "LEFT OUTER JOIN CFACIDAD CFACIDAD \n" +
					 "ON(AEAPRREC.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD) \n" +
					 "WHERE (AEAPRREC.ID_CFACIDAD IS NOT NULL) AND (AEAPRREC.ID_CFACIDAD <> '') ";

		if ((where != null) && (!where.isEmpty()) ){
			sql += "AND " + where;
		}
		// Instancia a classe para manipular o banco de dados
		ProdutoRecomendadoSql produtoRecomendadoSql = new ProdutoRecomendadoSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = produtoRecomendadoSql.sqlSelect(sql);

		if((cursor != null) && (cursor.getCount() > 0)){

			CidadeBeans cidade = new CidadeBeans();
			cidade.setDescricao(context.getResources().getString(R.string.selecione_uma_opcao));
			cidade.setIdCidade(0);

			listaCidade.add(cidade);

			while (cursor.moveToNext()) {
				// Instancia a classe para salvar o nome da cidade
				cidade = new CidadeBeans();
				// Seta o texto principal com o nome da cidade
				cidade.setIdCidade(cursor.getInt(cursor.getColumnIndex("ID_CFACIDAD")));
				cidade.setIdEstado(cursor.getInt(cursor.getColumnIndex("ID_CFAESDAD")));
				cidade.setDescricao(cursor.getString(cursor.getColumnIndex("DESCRICAO_CIDADE")));
				// Adiciona a cidade em uma lista
				listaCidade.add(cidade);
			}
			// Adiciona um valor padrao para selecionar todas as cidades
			cidade = new CidadeBeans();
			cidade.setDescricao(context.getResources().getString(R.string.todos));
			cidade.setIdCidade(0);

		} else {
			CidadeBeans cidade = new CidadeBeans();
			cidade.setDescricao(context.getResources().getString(R.string.nenhuma_opcao_encontrada));
			cidade.setIdCidade(0);

			listaCidade.add(cidade);
		}

		return listaCidade;
	} // Fim listaClasse

	public List<AreaBeans> listaAreaMaisVendidos(){
		// Cria uma lista para retornar as cidades
		List<AreaBeans> listaArea = new ArrayList<AreaBeans>();

		String sql = "SELECT CFAAREAS.ID_CFAAREAS, CFAAREAS.CODIGO, CFAAREAS.DESCRICAO AS DESCRICAO_AREA, CFAAREAS.DESC_PROMOCAO FROM AEAPRREC \n" +
					 "LEFT OUTER JOIN CFAAREAS CFAAREAS \n" +
					 "ON(AEAPRREC.ID_CFAAREAS = CFAAREAS.ID_CFAAREAS) " +
					 "WHERE (AEAPRREC.ID_CFAAREAS IS NOT NULL) ";

		// Instancia a classe para manipular o banco de dados
		AreasSql areasSql = new AreasSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = areasSql.sqlSelect(sql);

		if((cursor != null) && (cursor.getCount() > 0)){

			AreaBeans area = new AreaBeans();
			area.setDescricaoArea(context.getResources().getString(R.string.selecione_uma_opcao));
			area.setIdArea(0);

			listaArea.add(area);

			while (cursor.moveToNext()) {
				// Instancia a classe para salvar o nome da cidade
				area = new AreaBeans();
				// Seta o texto principal com o nome da cidade
				area.setIdArea(cursor.getInt(cursor.getColumnIndex("ID_CFAAREAS")));
				area.setDescricaoArea(cursor.getString(cursor.getColumnIndex("DESCRICAO_AREA")));
				area.setPromocao(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO")).charAt(0));
				// Adiciona a cidade em uma lista
				listaArea.add(area);
			}
			// Adiciona um valor padrao para selecionar todas as cidades
			area = new AreaBeans();
			area.setDescricaoArea(context.getResources().getString(R.string.todos));
			area.setIdArea(0);

		} else {
			AreaBeans area = new AreaBeans();
			area.setDescricaoArea(context.getResources().getString(R.string.nenhuma_opcao_encontrada));
			area.setIdArea(0);

			listaArea.add(area);
		}

		return listaArea;
	} // Fim listaClasse

	/**
	 * Lista os produtos mais vendidos. Existe algumas classificacoes de produtos mais vendidos,
	 * as classificao pode ser por:
	 * cidade(produtos mais vendidos de uma determinada cidade),
	 * Area (produtos mais vendidos de uma determianda area,
	 * Vendedor (Produtos mais venddidos de um determinado vendedor, que no caso o vendedor logado no app),
	 * Empresa (produtos mais vendidos da empresa selecionada pelo app),
	 * Corte (produtos cortados, as lista de produtos pode ser os produtos cortados de um determinado cliente,
	 * ou uma lista de produtos cortados de varios clientes).
	 *
	 * @param tipoTela
	 * @param filtro
	 * @param where
	 * @param group
	 * @param idOrcamento
	 * @return
	 */
	@Deprecated
	public List<ProdutoListaBeans> listaProdutoMaisVendido(int tipoTela, ContentValues filtro, String where, String group, String idOrcamento, final ProgressBar progresso, TextView textProgresso){
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		String idEmpresa = funcoes.getValorXml("CodigoEmpresa");
		String codigoVendedor = funcoes.getValorXml("CodigoUsuario");

		String sql =  "SELECT AEAPLOJA.ID_AEAPLOJA, AEAPRODU.ID_AEAPRODU, AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.REFERENCIA, "
					+ "AEAPRODU.DESCRICAO AS DESCRICAO_PRODU, AEAPRODU.TIPO, (JULIANDAY(DATE('NOW', 'LOCALTIME')) - JULIANDAY(AEAPRODU.DT_CAD)) AS DIAS_CADASTRO, "
					+ "AEAMARCA.DESCRICAO AS DESCRICAO_MARCA, AEAPRODU.ID_AEAUNVEN, AEAUNVEN.SIGLA, "
					+ "AEAPLOJA.VENDA_ATAC AS VENDA_ATAC_TABELA, AEAPLOJA.VENDA_VARE AS VENDA_VARE_TABELA, "

					+ "ROUND((AEAPLOJA.VENDA_ATAC + (AEAPLOJA.VENDA_ATAC * (IFNULL((SELECT AEAPERCE.MARKUP_ATAC FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) + "
					+ "((AEAPLOJA.VENDA_ATAC + (AEAPLOJA.VENDA_ATAC * (IFNULL((SELECT AEAPERCE.MARKUP_ATAC FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) * "
					+ "(IFNULL((SELECT AEAPERCE.MARKUP_ATAC FROM AEAPERCE LEFT OUTER JOIN CFAPARAM ON(AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) "
					+ "LEFT OUTER JOIN CFACLIFO ON(CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) WHERE CFACLIFO.CODIGO_FUN = " + codigoVendedor + "), 0)/100)), "
					+ "(SELECT SMAEMPRE.QTD_CASAS_DECIMAIS FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = " + idEmpresa + ")) AS VENDA_ATAC_FINAL, "

					+ "ROUND((AEAPLOJA.VENDA_VARE + (AEAPLOJA.VENDA_VARE * (IFNULL((SELECT AEAPERCE.MARKUP_VARE FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) + "
					+ "((AEAPLOJA.VENDA_VARE + (AEAPLOJA.VENDA_VARE * (IFNULL((SELECT AEAPERCE.MARKUP_VARE FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) * "
					+ "(IFNULL((SELECT AEAPERCE.MARKUP_VARE FROM AEAPERCE LEFT OUTER JOIN CFAPARAM ON(AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) "
					+ "LEFT OUTER JOIN CFACLIFO ON(CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) WHERE CFACLIFO.CODIGO_FUN = " + codigoVendedor + "), 0)/100)), "
					+ "(SELECT SMAEMPRE.QTD_CASAS_DECIMAIS FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = " + idEmpresa + ")) AS VENDA_VARE_FINAL, "

                    + "SMAEMPRE.QTD_DIAS_DESTACA_PRODUTO, "
					+ "AEAPLOJA.PROMOCAO_ATAC_VISTA, AEAPLOJA.PROMOCAO_VARE_VISTA,"
					+ "AEAPLOJA.PROMOCAO_ATAC_PRAZO, AEAPLOJA.PROMOCAO_VARE_PRAZO,"
					+ "AEAPLOJA.CT_REPOSICAO_N, AEAPLOJA.CT_COMPLETO_N, "
					+ "AEAPLOJA.ESTOQUE_F ESTOQUE_FISICO, AEAPLOJA.ESTOQUE_C ESTOQUE_CONTABIL, "
					+ "AEACLASE.CODIGO AS CODIGO_CLASE, AEACLASE.DESCRICAO AS DESCRICAO_CLASE, AEAPRODU.PESO_BRUTO, AEAPRODU.PESO_LIQUIDO \n"
					+ "FROM AEAPRREC AEAPRREC \n"
					+ "LEFT OUTER JOIN AEAPRODU AEAPRODU ON  (AEAPRODU.ID_AEAPRODU = AEAPRREC.ID_AEAPRODU) \n"
					+ "LEFT OUTER JOIN AEAPLOJA AEAPLOJA ON  (AEAPLOJA.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU) \n"
					+ "LEFT OUTER JOIN AEACLASE AEACLASE ON  (AEACLASE.ID_AEACLASE = AEAPRODU.ID_AEACLASE) \n"
					+ "LEFT OUTER JOIN AEAMARCA AEAMARCA ON  (AEAMARCA.ID_AEAMARCA = AEAPRODU.ID_AEAMARCA) \n"
					+ "LEFT OUTER JOIN AEAUNVEN AEAUNVEN ON  (AEAUNVEN.ID_AEAUNVEN = AEAPRODU.ID_AEAUNVEN) \n"
					+ "LEFT OUTER JOIN SMAEMPRE SMAEMPRE ON  (SMAEMPRE.ID_SMAEMPRE = AEAPLOJA.ID_SMAEMPRE) \n"
					+ "WHERE (AEAPRODU.ATIVO = '1') AND (AEAPRODU.DESCRICAO IS NOT NULL) ";

		// Adiciona a clausula where passada por parametro no sql
		if(where != null){
			sql = sql + " AND ( " + where +" ) ";
		}

		if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE){

			if ((filtro != null) &&
					(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE)) != null) &&
					(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE)).length() > 0)) {

				sql += " AND ( AEAPRREC.ID_CFACIDAD = " + filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE)) + " ) ";
			} else {
				sql += " AND ( AEAPRREC.ID_CFACIDAD IS NOT NULL ) ";
			}

		} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA){
			// Checa se foi enviado algum parametro
			if ((filtro != null) && (filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA)) != null) &&
					(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA)).length() > 0)) {

				sql += " AND ( AEAPRREC.ID_CFAAREAS = " + filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA)) + " ) ";
			} else {
				sql += " AND ( AEAPRREC.ID_CFAAREAS IS NOT NULL ) ";
			}

		} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_VENDEDOR){
			/*if ((filtro != null) && (filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_VENDEDOR)) != null) &&
					(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_VENDEDOR)).length() > 0)) {*/
			if ((codigoVendedor != null) && (codigoVendedor.length() > 0)){

				sql += " AND ( AEAPRREC.ID_CFACLIFO_VENDEDOR = " + codigoVendedor + " ) ";
			} else {
				sql += " AND ( AEAPRREC.ID_CFACLIFO_VENDEDOR IS NOT NULL ) ";
			}

		} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_EMPRESA){
			/*if ((filtro != null) && (filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_EMPRESA)) != null) &&
					(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_EMPRESA)).length() > 0)) {*/
			if ((idEmpresa != null) && (idEmpresa.length() > 0)){

				sql += " AND ( AEAPRREC.ID_SMAEMPRE = " + idEmpresa + " ) ";
			} else {
				sql += " AND ( AEAPRREC.ID_SMAEMPRE IS NOT NULL ) ";
			}

		} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM){
			// Checa se foi passado por paramentro
			if ((filtro != null) && (filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM)) != null) &&
					(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM)).length() > 0)) {

				sql += " AND ( AEAPRREC.ID_CFACLIFO = " + filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM)) + " ) ";

			} else {
				sql += " AND (AEAPRREC.ID_CFACLIFO IS NOT NULL) ";
			}
		}
		if (group != null){
			sql = sql + " " + group + " ";
		}
		// Adiciona a ordem no sql
		sql = sql + " ORDER BY AEAPRODU.DESCRICAO, AEAUNVEN.SIGLA, AEAMARCA.DESCRICAO ";

		// Cria uma lista para armazenar todas os produtos retornados do banco
		List<ProdutoListaBeans> listaProduto = new ArrayList<ProdutoListaBeans>();

		// Instancia a classe para manipular o banco de dados
		ProdutoSql produtoSql = new ProdutoSql(context);

		final Cursor cursor = produtoSql.sqlSelect(sql);

		// Se o cursor tiver algum valor entra no laco
		if(cursor.getCount() > 0){
			// Checa se tem alguma barra de progresso
			if (progresso != null){
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						progresso.setIndeterminate(false);
						progresso.setProgress(0);
						progresso.setMax(cursor.getCount());
					}
				});
			}
			try{
				int incremento = 0;
				while(cursor.moveToNext()){
					// Checa se tem alguma barra de progresso
					if (progresso != null) {

						incremento++;
						final int finalIncremento = incremento;
						((Activity) context).runOnUiThread(new Runnable() {
							public void run() {
								progresso.setProgress(finalIncremento);
							}
						});
					}
					// Preenche os dados do produto
					ProdutoListaBeans produtoLista = new ProdutoListaBeans();
					ProdutoBeans produto = new ProdutoBeans();
					produto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
					produto.setCodigoEstrutural(cursor.getString(cursor.getColumnIndex("CODIGO_ESTRUTURAL")));
					produto.setReferencia(cursor.getString(cursor.getColumnIndex("REFERENCIA")));
					produto.setDescricaoProduto(cursor.getString(cursor.getColumnIndex("DESCRICAO_PRODU")));
					produto.setDescricaoMarca(cursor.getString(cursor.getColumnIndex("DESCRICAO_MARCA")));
					produto.setPesoBruto(cursor.getDouble(cursor.getColumnIndex("PESO_BRUTO")));
					produto.setPesoLiquido(cursor.getDouble(cursor.getColumnIndex("PESO_LIQUIDO")));
					if(cursor.getString(cursor.getColumnIndex("TIPO")).length() > 0){
						produto.setTipoProduto(cursor.getString(cursor.getColumnIndex("TIPO")));
					}else{
						produto.setTipoProduto("P");
					}
					produto.setDiasCadastro(cursor.getInt(cursor.getColumnIndex("DIAS_CADASTRO")));

					final String descProduto = produto.getDescricaoProduto();

					// Pega a unidade de venda do produto
					UnidadeVendaBeans unidadeVenda = new UnidadeVendaBeans();
					unidadeVenda.setIdUnidadeVenda(cursor.getInt(cursor.getColumnIndex("ID_AEAUNVEN")));
					unidadeVenda.setSiglaUnidadeVenda(cursor.getString(cursor.getColumnIndex("SIGLA")));
					// Adiciiona a unidade de venda no produto
					produto.setUnidadeVendaProduto(unidadeVenda);

					// Pega a classe do produto
					ClasseBeans classe = new ClasseBeans();
					classe.setCodigoClasse(cursor.getInt(cursor.getColumnIndex("CODIGO_CLASE")));
					classe.setDescricaoClasse(cursor.getString(cursor.getColumnIndex("DESCRICAO_CLASE")));
					// Adiciona a classe no produto
					produto.setClasseProduto(classe);

					// Adiciona o produto a lista
					//produtoLista.setProduto(produto);

					produtoLista.setValorTabelaAtacado(cursor.getDouble(cursor.getColumnIndex("VENDA_ATAC_TABELA")));
					produtoLista.setValorTabelaVarejo(cursor.getDouble(cursor.getColumnIndex("VENDA_VARE_TABELA")));
					produtoLista.setValorUnitarioAtacado(cursor.getDouble(cursor.getColumnIndex("VENDA_ATAC_FINAL")));
					produtoLista.setValorUnitarioVarejo(cursor.getDouble(cursor.getColumnIndex("VENDA_VARE_FINAL")));
					produtoLista.setValorPromocaoAtacadoVista(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_ATAC_VISTA")));
					produtoLista.setValorPromocaoAtacadoPrazo(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_ATAC_PRAZO")));
					produtoLista.setValorPromocaoVarejoVista(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_VARE_VISTA")));
					produtoLista.setValorPromocaoVarejoPrazo(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_VARE_PRAZO")));
					produtoLista.setCustoReposicaoN(cursor.getDouble(cursor.getColumnIndex("CT_REPOSICAO_N")));
					produtoLista.setCustoCompleto(cursor.getDouble(cursor.getColumnIndex("CT_COMPLETO_N")));
					produtoLista.setEstoqueFisico(cursor.getDouble(cursor.getColumnIndex("ESTOQUE_FISICO")));
					produtoLista.setEstoqueContabil(cursor.getDouble(cursor.getColumnIndex("ESTOQUE_CONTABIL")));

					// Verifica se tem numero de orcamento para pesquisar
					if(idOrcamento != null){
						//Verifica se o produto esta dentro de um orcamento
						if( marcaProdutoJaComprados(String.valueOf(produto.getIdProduto()), idOrcamento) ){
							produtoLista.setEstaNoOrcamento('1');
						}
					}
                    int diasProdutoNovo = cursor.getInt(cursor.getColumnIndex("QTD_DIAS_DESTACA_PRODUTO"));

                    if((diasProdutoNovo > 0) && (diasProdutoNovo >= produto.getDiasCadastro()) && (produtoLista.getEstaNoOrcamento() != '1')){
                        produtoLista.setProdutoNovo(true);
                    }

					// Instancia a clesse de embalagens
					EmbalagemSql embalagemSql = new EmbalagemSql(context);
					Cursor cursorEmbalagem = embalagemSql.query("ID_AEAPRODU = " + produto.getIdProduto());

					// Verifica se retornou algum registro
					if(cursorEmbalagem.getCount() > 0){
						List<EmbalagemBeans> listaEmbalagem = new ArrayList<EmbalagemBeans>();
						// Enquanto tiver registro vai para frente
						while(cursorEmbalagem.moveToNext()){
							// Instancia a classe de embalagem
							EmbalagemBeans embalagem = new EmbalagemBeans();
							// Preenche os dados da embalagem
							embalagem.setIdEmbalagem(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("ID_AEAEMBAL")));
							embalagem.setIdUnidadeVenda(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("ID_AEAUNVEN")));
                            String ativo = "";
                            ativo = cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("ATIVO"));
							if(ativo.length() > 0){
								embalagem.setAtivo(ativo);
							}
							if((cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL")) != null) && (cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL")).length() > 0)){
								embalagem.setPrincipal(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL")));
							}
							embalagem.setDescricaoEmbalagem(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("DESCRICAO")));
							embalagem.setFatorConversao(cursorEmbalagem.getDouble(cursorEmbalagem.getColumnIndex("FATOR_CONVERSAO")));
							embalagem.setFatorPreco(cursorEmbalagem.getDouble(cursorEmbalagem.getColumnIndex("FATOR_PRECO")));
							embalagem.setModulo(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("MODULO")));
							embalagem.setDecimais(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("DECIMAIS")));

							// Instancia a classe de unidade de venda para manipular banco de dados
							UnidadeVendaSql unidadeVendaSql = new UnidadeVendaSql(context);
							Cursor cursorUnVenda = unidadeVendaSql.query("ID_AEAUNVEN = " + embalagem.getIdUnidadeVenda());
							// Verifica se retornou registro
							if(cursorUnVenda.getCount() > 0){
								// Move para o primeiro registro
								cursorUnVenda.moveToFirst();
								// Preenche os dados da unidade de venda
								unidadeVenda.setIdUnidadeVenda(cursorUnVenda.getInt(cursorUnVenda.getColumnIndex("ID_AEAUNVEN")));
								unidadeVenda.setSiglaUnidadeVenda(cursorUnVenda.getString(cursorUnVenda.getColumnIndex("SIGLA")));
								unidadeVenda.setDescricaoUnidadeVenda(cursorUnVenda.getString(cursorUnVenda.getColumnIndex("DESCRICAO_SINGULAR")));
								unidadeVenda.setCasasDecimais(cursorUnVenda.getInt(cursorUnVenda.getColumnIndex("DECIMAIS")));

								embalagem.setUnidadeVendaEmbalagem(unidadeVenda);
							}
							// Adiciona a embalagem a uma lista
							listaEmbalagem.add(embalagem);
						} // FIm do while
                        // Adiciona uma lista de embalagens no produto
                        produto.setListaEmbalagem(listaEmbalagem);

						// Adiciona o produto a lista
						produtoLista.setProduto(produto);
					}
					listaProduto.add(produtoLista);
				} // Fim primeiro while

			}catch(Exception e){
				// Armazena as informacoes para para serem exibidas e enviadas
				ContentValues contentValues = new ContentValues();
				contentValues.put("comando", 0);
				contentValues.put("tela", "ProdutoRotinas");
				contentValues.put("mensagem", "Erro ao carregar os dados do produto. \n" + e.getMessage());
				contentValues.put("dados", e.toString());
				// Pega os dados do usuario
				funcoes = new FuncoesPersonalizadas(context);
				contentValues.put("usuario", funcoes.getValorXml("Usuario"));
				contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
				contentValues.put("email", funcoes.getValorXml("Email"));
				// Exibe a mensagem
				funcoes.menssagem(contentValues);
			}
		}
		return listaProduto;
	}


	public List<AeaplojaBeans> listaProduto(String where, String idOrcamento, Integer tipoTela, ContentValues filtro, final ProgressBar progresso, final TextView textprogresso){
		// Instancia a vareavel para salvar a lista com os dados
		List<AeaplojaBeans> listAeaploja = new ArrayList<>();

		try {
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			String idEmpresa = (funcoes.getValorXml(funcoes.TAG_CODIGO_EMPRESA).equalsIgnoreCase(funcoes.NAO_ENCONTRADO) ? "0" : funcoes.getValorXml(funcoes.TAG_CODIGO_EMPRESA));

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT AEAPLOJA.ID_AEAPLOJA, AEAPLOJA.ID_AEAPRODU, AEAPLOJA.ID_SMAEMPRE, AEAPLOJA.VENDA_ATAC, AEAPLOJA.VENDA_VARE, ");
			sql.append("AEAPLOJA.CT_REPOSICAO_N, AEAPLOJA.CT_REAL_D, AEAPLOJA.CT_COMPLETO_N, ");
			sql.append("AEAPLOJA.ESTOQUE_F, AEAPLOJA.ESTOQUE_C, DATE('NOW') AS DATA_ATUAL, ");

			sql.append("AEAPRODU.ID_AEAPRODU, AEAPRODU.ID_AEAUNVEN, AEAPRODU.ID_AEACLASE, AEAPRODU.ID_AEAMARCA, ");
			sql.append("AEAPRODU.DT_CAD AS DT_CAD_AEAPRODU, AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.DESCRICAO AS DESCRICAO_AEAPRODU, AEAPRODU.DESCRICAO_AUXILIAR, AEAPRODU.REFERENCIA, ");
			sql.append("AEAPRODU.CODIGO_BARRAS, AEAPRODU.PESO_LIQUIDO, AEAPRODU.PESO_BRUTO, AEAPRODU.TIPO, ");

			sql.append("AEAMARCA.ID_AEAMARCA, AEAMARCA.DESCRICAO AS DESCRICAO_AEAMARCA, ");

			sql.append("AEAUNVEN.ID_AEAUNVEN, AEAUNVEN.SIGLA, AEAUNVEN.DESCRICAO_SINGULAR, AEAUNVEN.DECIMAIS ");

			sql.append("FROM AEAPLOJA AEAPLOJA \n");
			sql.append("LEFT OUTER JOIN AEAPRODU AEAPRODU ON  (AEAPRODU.ID_AEAPRODU = AEAPLOJA.ID_AEAPRODU) \n");
			sql.append("LEFT OUTER JOIN AEAUNVEN AEAUNVEN ON  (AEAUNVEN.ID_AEAUNVEN = AEAPRODU.ID_AEAUNVEN) \n");
			sql.append("LEFT OUTER JOIN AEAMARCA AEAMARCA ON  (AEAMARCA.ID_AEAMARCA = AEAPRODU.ID_AEAMARCA) \n");
			//sql.append("LEFT OUTER JOIN AEAEMBAL AEAEMBAL ON  (AEAEMBAL.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU AND AEAEMBAL.ID_AEAUNVEN = AEAPRODU.ID_AEAUNVEN) \n");
			sql.append("WHERE (AEAPLOJA.ATIVO = '1') AND (AEAPRODU.ATIVO = '1') AND (AEAPRODU.DESCRICAO IS NOT NULL) \n");
			// Checa se tem o id do orcamento
			if (idOrcamento != null && idOrcamento.length() > 0) {
				sql.append(" AND (AEAPLOJA.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento + ")) ");
			} else {
				sql.append(" AND (AEAPLOJA.ID_SMAEMPRE = " + idEmpresa + ")");
			}
			// Adiciona a clausula where passada por parametro no sql
			if (where != null) {
				sql.append(" AND ( " + where + " ) ");
			}
			// Checa se foi passado algum tipo de tela e o filtro
			if ( (tipoTela != null) ) {
				if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE) {

					if ((filtro != null) && (filtro.containsKey(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE)))) {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFACIDAD = ").append(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CIDADE))).append(") ) ");
					} else {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFACIDAD IS NOT NULL )) ");
					}
				} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA) {
					// Checa se foi enviado algum parametro
					if ((filtro != null) && (filtro.containsKey(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA)))) {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFAAREAS = ").append(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_AREA))).append(") ) ");
					} else {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFAAREAS IS NOT NULL )) ");
					}
				} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_VENDEDOR) {
					// Pega o codigo do vendedor salvo nos parametros do app
					String codigoVendedor = (funcoes.getValorXml(funcoes.TAG_CODIGO_USUARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO) ? "0" : funcoes.getValorXml(funcoes.TAG_CODIGO_USUARIO));
					// Checa se realmente deu certo de pegar o codigo do vendedor
					if ((codigoVendedor != null) && (codigoVendedor.length() > 0) && (!codigoVendedor.equalsIgnoreCase("0"))) {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFACLIFO_VENDEDOR = ").append(codigoVendedor).append(") ) ");
					} else {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFACLIFO_VENDEDOR IS NOT NULL )) ");
					}
				} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_EMPRESA) {
					if ((idEmpresa != null) && (idEmpresa.length() > 0) && (!idEmpresa.equalsIgnoreCase("0"))) {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_SMAEMPRE = ").append(idEmpresa).append(") ) ");
					} else {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_SMAEMPRE IS NOT NULL )) ");
					}
				} else if (tipoTela == ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM) {
					// Checa se foi passado por paramentro
					if ((filtro != null) && (filtro.containsKey(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM)))) {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFACLIFO = ").append(filtro.getAsString(String.valueOf(ProdutoListaMDFragment.TELA_MAIS_VENDIDOS_CORTES_CHEGARAM))).append(") ) ");
					} else {
						sql.append(" AND ( AEAPRODU.ID_AEAPRODU IN(SELECT AEAPRREC.ID_AEAPRODU FROM AEAPRREC WHERE AEAPRREC.ID_CFACLIFO IS NOT NULL )) ");
					}
				}
			}
			// Adiciona a ordem no sql
			sql.append(" ORDER BY AEAPRODU.DESCRICAO, AEAUNVEN.SIGLA, AEAMARCA.DESCRICAO ");

			ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

			final Cursor cursorAeaploja = produtoLojaSql.sqlSelect(sql.toString());

			if ((cursorAeaploja != null) && (cursorAeaploja.getCount() > 0)) {
				// Checa se tem alguma barra de progresso
				if (progresso != null) {
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							progresso.setIndeterminate(false);
							progresso.setProgress(0);
							progresso.setMax(cursorAeaploja.getCount());
						}
					});
				}
				if (textprogresso != null){
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							textprogresso.setText(R.string.achamos_alguma_coisa);
						}
					});
				}
				int incremento = 0;

				while (cursorAeaploja.moveToNext()) {
					// Verifica se tem algum progressbar de status
					if (progresso != null) {
						incremento++;
						final int finalIncremento = incremento;
						((Activity) context).runOnUiThread(new Runnable() {
							public void run() {
								progresso.setProgress(finalIncremento);
							}
						});
					}
					AeaplojaBeans aeaploja = new AeaplojaBeans();
					aeaploja.setIdAeaploja(cursorAeaploja.getInt(cursorAeaploja.getColumnIndex("ID_AEAPLOJA")));
					aeaploja.setDataAtual(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("DATA_ATUAL")));

					AeaproduBeans aeaprodu = new AeaproduBeans();
					aeaprodu.setIdAeaprodu(cursorAeaploja.getInt(cursorAeaploja.getColumnIndex("ID_AEAPRODU")));
					aeaprodu.setDtCad(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("DT_CAD_AEAPRODU")));
					aeaprodu.setCodigoEstrutural(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("CODIGO_ESTRUTURAL")));
					aeaprodu.setDescricao(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("DESCRICAO_AEAPRODU")));
					if (!cursorAeaploja.isNull(cursorAeaploja.getColumnIndex("DESCRICAO_AUXILIAR"))) aeaprodu.setDescricaoAuxiliar(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("DESCRICAO_AUXILIAR")));
					if (!cursorAeaploja.isNull(cursorAeaploja.getColumnIndex("REFERENCIA"))) aeaprodu.setReferencia(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("REFERENCIA")));
					if (!cursorAeaploja.isNull(cursorAeaploja.getColumnIndex("CODIGO_BARRAS"))) aeaprodu.setCodigoBarras(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("CODIGO_BARRAS")));
					if (!cursorAeaploja.isNull(cursorAeaploja.getColumnIndex("TIPO"))) aeaprodu.setTipo(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("TIPO")));
					aeaprodu.setPesoBruto(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("PESO_BRUTO")));
					aeaprodu.setPesoLiquido(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("PESO_LIQUIDO")));

					AeamarcaBeans aeamarca = new AeamarcaBeans();
					aeamarca.setIdAeamarca(cursorAeaploja.getInt(cursorAeaploja.getColumnIndex("ID_AEAMARCA")));
					aeamarca.setDescricao(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("DESCRICAO_AEAMARCA")));
					aeaprodu.setAeamarca(aeamarca);

					AeaunvenBeans aeaunven = new AeaunvenBeans();
					aeaunven.setIdAeaunven(cursorAeaploja.getInt(cursorAeaploja.getColumnIndex("ID_AEAUNVEN")));
					aeaunven.setSigla(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("SIGLA")));
					aeaunven.setDescricaosingular(cursorAeaploja.getString(cursorAeaploja.getColumnIndex("DESCRICAO_SINGULAR")));
					aeaprodu.setAeaunven(aeaunven);

					aeaploja.setAeaprodu(aeaprodu);
					aeaploja.setVendaAtac(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("VENDA_ATAC")));
					aeaploja.setVendaVare(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("VENDA_VARE")));
					aeaploja.setCtRealD(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("CT_REAL_D")));
					aeaploja.setCtReposicaoN(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("CT_REPOSICAO_N")));
					aeaploja.setCtCompletoN(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("CT_COMPLETO_N")));
					aeaploja.setEstoqueF(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("ESTOQUE_F")));
					aeaploja.setEstoqueC(cursorAeaploja.getDouble(cursorAeaploja.getColumnIndex("ESTOQUE_C")));

					listAeaploja.add(aeaploja);
				} // Fim do while
			}


		} catch (final Exception e){
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					new MaterialDialog.Builder(context)
							.title("ProdutoRotinas")
							.content("Erro ao carregar os dados do produto por loja. \n" + e.getMessage())
							.positiveText(R.string.button_ok)
							.show();
				}
			});
		}
		return listAeaploja;
	}


	@Deprecated
	public List<ProdutoListaBeans> listaProduto(String where, String group, String idOrcamento, final ProgressBar progresso, final TextView textProgresso, String todasEmbalagens, Integer idPlPgto, String calculaPreco){

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		// Instancia a classe para manipular o banco de dados
		ProdutoSql produtoSql = new ProdutoSql(context);

		String idEmpresa = funcoes.getValorXml("CodigoEmpresa");
		idEmpresa = (idEmpresa.equalsIgnoreCase(funcoes.NAO_ENCONTRADO) ? "0" : idEmpresa);
		String codigoVendedor = funcoes.getValorXml("CodigoUsuario");
		codigoVendedor = (codigoVendedor.equalsIgnoreCase(funcoes.NAO_ENCONTRADO) ? "0" : codigoVendedor);
		String idCliente = "0", atacadoVarejo = "1";

		StringBuilder sql = new StringBuilder();
        sql.append("SELECT AEAPLOJA.ID_AEAPLOJA, AEAPRODU.ID_AEAPRODU, AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.REFERENCIA, ");
		sql.append("AEAPRODU.DESCRICAO AS DESCRICAO_PRODU, AEAPRODU.TIPO, (JULIANDAY(DATE('NOW', 'LOCALTIME')) - JULIANDAY(AEAPRODU.DT_CAD)) AS DIAS_CADASTRO, ");
		sql.append("AEAMARCA.DESCRICAO AS DESCRICAO_MARCA, AEAPRODU.ID_AEAUNVEN, AEAUNVEN.SIGLA, ");
		sql.append("DATE('NOW') AS DATA_ATUAL, AEAPLOJA.VENDA_ATAC AS VENDA_ATAC_TABELA, AEAPLOJA.VENDA_VARE AS VENDA_VARE_TABELA, ");

				   /**+ "ROUND((AEAPLOJA.VENDA_ATAC + (AEAPLOJA.VENDA_ATAC * (IFNULL((SELECT AEAPERCE.MARKUP_ATAC FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) + "
				   + "((AEAPLOJA.VENDA_ATAC + (AEAPLOJA.VENDA_ATAC * (IFNULL((SELECT AEAPERCE.MARKUP_ATAC FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) * "
				   + "(IFNULL((SELECT AEAPERCE.MARKUP_ATAC FROM AEAPERCE LEFT OUTER JOIN CFAPARAM ON(AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) "
				   + "LEFT OUTER JOIN CFACLIFO ON(CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) WHERE CFACLIFO.CODIGO_FUN = " + codigoVendedor + "), 0)/100)), 3) AS VENDA_ATAC_FINAL, "

				   + "ROUND((AEAPLOJA.VENDA_VARE + (AEAPLOJA.VENDA_VARE * (IFNULL((SELECT AEAPERCE.MARKUP_VARE FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) + "
				   + "((AEAPLOJA.VENDA_VARE + (AEAPLOJA.VENDA_VARE * (IFNULL((SELECT AEAPERCE.MARKUP_VARE FROM AEAPERCE WHERE AEAPERCE.ID_SMAEMPRE = " + idEmpresa + "), 0)/100))) * "
				   + "(IFNULL((SELECT AEAPERCE.MARKUP_VARE FROM AEAPERCE LEFT OUTER JOIN CFAPARAM ON(AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) "
				   + "LEFT OUTER JOIN CFACLIFO ON(CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) WHERE CFACLIFO.CODIGO_FUN = " + codigoVendedor + "), 0)/100)), 3) AS VENDA_VARE_FINAL, " **/

        sql.append("SMAEMPRE.QTD_DIAS_DESTACA_PRODUTO, ");
		//sql.append("AEAPLOJA.PROMOCAO_ATAC_VISTA, AEAPLOJA.PROMOCAO_VARE_VISTA, ");
		//sql.append("AEAPLOJA.PROMOCAO_ATAC_PRAZO, AEAPLOJA.PROMOCAO_VARE_PRAZO, ");
		sql.append("AEAPLOJA.CT_REPOSICAO_N, AEAPLOJA.CT_COMPLETO_N, ");
		sql.append("AEAPLOJA.ESTOQUE_F ESTOQUE_FISICO, AEAPLOJA.ESTOQUE_C ESTOQUE_CONTABIL, \n");
		sql.append("AEACLASE.CODIGO AS CODIGO_CLASE, AEACLASE.DESCRICAO AS DESCRICAO_CLASE, AEAPRODU.PESO_BRUTO, AEAPRODU.PESO_LIQUIDO ");
        if (todasEmbalagens.equalsIgnoreCase(NAO)){
            sql.append(",AEAEMBAL.ID_AEAEMBAL, AEAEMBAL.ID_AEAPRODU, AEAEMBAL.ID_AEAUNVEN, AEAEMBAL.DT_ALT, AEAEMBAL.PRINCIPAL, AEAEMBAL.DESCRICAO, \n");
            sql.append("AEAEMBAL.FATOR_CONVERSAO, AEAEMBAL.FATOR_PRECO, AEAEMBAL.MODULO, AEAEMBAL.DECIMAIS, AEAEMBAL.ATIVO, \n");
            sql.append("AEAUNVEN_EMBAL.SIGLA AS SIGLA_UNVEN, AEAUNVEN_EMBAL.DESCRICAO_SINGULAR AS DESCRICAO_SINGULAR_UNVEN, AEAUNVEN_EMBAL.DECIMAIS AS DECIMAIS_UNVEN \n");
        }
        sql.append("FROM AEAPLOJA AEAPLOJA \n");
		sql.append("LEFT OUTER JOIN AEAPRODU AEAPRODU ON  (AEAPRODU.ID_AEAPRODU = AEAPLOJA.ID_AEAPRODU) \n");
		sql.append("LEFT OUTER JOIN AEACLASE AEACLASE ON  (AEACLASE.ID_AEACLASE = AEAPRODU.ID_AEACLASE) \n");
		sql.append("LEFT OUTER JOIN AEAMARCA AEAMARCA ON  (AEAMARCA.ID_AEAMARCA = AEAPRODU.ID_AEAMARCA) \n");
		sql.append("LEFT OUTER JOIN AEAUNVEN AEAUNVEN ON  (AEAUNVEN.ID_AEAUNVEN = AEAPRODU.ID_AEAUNVEN) \n");
		sql.append("LEFT OUTER JOIN SMAEMPRE SMAEMPRE ON  (SMAEMPRE.ID_SMAEMPRE = AEAPLOJA.ID_SMAEMPRE) \n");
        if (todasEmbalagens.equalsIgnoreCase(NAO)){
            sql.append("LEFT OUTER JOIN AEAEMBAL AEAEMBAL ON  (AEAEMBAL.ID_AEAPRODU = AEAPRODU.ID_AEAPRODU AND AEAEMBAL.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN) \n");
            sql.append("LEFT OUTER JOIN AEAUNVEN AEAUNVEN_EMBAL ON(AEAEMBAL.ID_AEAUNVEN = AEAUNVEN_EMBAL.ID_AEAUNVEN)");
        }
        sql.append("WHERE (AEAPRODU.ATIVO = '1') AND (AEAPRODU.DESCRICAO IS NOT NULL) \n");

		// Checa se tem o id do orcamento
		if (idOrcamento != null && idOrcamento.length() > 0){
			sql.append(" AND (AEAPLOJA.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = " + idOrcamento + ")) ");
		} else {
			sql.append(" AND (AEAPLOJA.ID_SMAEMPRE = " + idEmpresa + ")");
		}
		// Adiciona a clausula where passada por parametro no sql
		if(where != null){
			sql.append(" AND ( " + where +" ) ");
		}

		if ((calculaPreco != null) && (calculaPreco.equalsIgnoreCase(APENAS_PRODUTO_PROMOCAO))){

			StringBuilder sqlTbpro = new StringBuilder();
			sqlTbpro.append( "(SELECT AEATBPRO.ID_AEATBPRO \n" +
					"FROM AEATBPRO \n" +
					"WHERE (AEATBPRO.ATIVO = '1') \n" +
					"AND (AEATBPRO.DT_INICIO <= DATE('NOW') ) AND (AEATBPRO.DT_FIM >= DATE('NOW') ) \n" +
					"AND ((AEATBPRO.DIAS = '') OR (AEATBPRO.DIAS IS NULL) OR (AEATBPRO.DIAS LIKE '%' || (STRFTIME('%w', DATE('NOW') )) || '%')) \n" +
					"AND (" + idEmpresa + " IN (SELECT ID_SMAEMPRE FROM AEAEMTBP WHERE AEAEMTBP.ID_AEATBPRO = AEATBPRO.ID_AEATBPRO AND AEAEMTBP.ID_SMAEMPRE = " + idEmpresa + ")) )");

			StringBuilder sqlIttbp = new StringBuilder();
			sqlIttbp.append("SELECT ID_AEAPRODU, ID_AEAAGPPR, ID_AEAMARCA, ID_AEAFAMIL, ID_AEACLASE, ID_AEAGRUPO, ID_AEASGRUP \n");
			sqlIttbp.append("FROM AEAITTBP \n");
			sqlIttbp.append("WHERE (ID_AEATBPRO IN (" + sqlTbpro.toString() + ") ) ");

			Cursor cursorIttbp = produtoSql.sqlSelect(sqlIttbp.toString());

			if ((cursorIttbp != null) && (cursorIttbp.getCount() > 0)){
				// Cria variaveis temporarias para salvar os id's que estao na promocao
				StringBuilder idProdu = new StringBuilder();
				idProdu.append(" AEAPRODU.ID_AEAPRODU IN (");
				int countIdProdu = 0;
				StringBuilder idMarca = new StringBuilder();
				idMarca.append(" AEAPRODU.ID_AEAMARCA IN (");
				int countIdMarca = 0;
				StringBuilder idFamil = new StringBuilder();
				idFamil.append(" AEAPRODU.ID_AEAFAMIL IN (");
				int countIdFamil = 0;
				StringBuilder idClase = new StringBuilder();
				idClase.append(" AEAPRODU.ID_AEACLASE IN (");
				int countIdClase = 0;
				StringBuilder idGrupo = new StringBuilder();
				idGrupo.append(" AEAPRODU.ID_AEAGRUPO IN (");
				int countIdGrupo = 0;
				StringBuilder idSGrupo = new StringBuilder();
				idSGrupo.append(" AEAPRODU.ID_AEASGRUP IN (");
				int countIdSGrupo = 0;
				StringBuilder idAgrupProd = new StringBuilder();
				idAgrupProd.append(" (");
				int countIdAgrupProd = 0;

				while (cursorIttbp.moveToNext()){
					// Verifica se tem algum produto na promocao valida
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEAPRODU"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAPRODU")) > 0) ){
						idProdu.append( ((countIdProdu > 0) ? ", " : "" ) );
						idProdu.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAPRODU")));

						countIdProdu ++;
					}
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEAAGPPR"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAAGPPR")) > 0) ){
						idAgrupProd.append( ((countIdAgrupProd > 0) ? ", " : "" ) );
						idAgrupProd.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAAGPPR")));

						countIdAgrupProd ++;
					}
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEAMARCA"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAMARCA")) > 0) ){
						idMarca.append( ((countIdMarca > 0) ? ", " : "" ) );
						idMarca.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAMARCA")));

						countIdMarca ++;
					}
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEAFAMIL"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAFAMIL")) > 0) ){
						idFamil.append( ((countIdFamil > 0) ? ", " : "" ) );
						idFamil.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAFAMIL")));

						countIdFamil ++;
					}
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEACLASE"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEACLASE")) > 0) ){
						idClase.append( ((countIdClase > 0) ? ", " : "" ) );
						idClase.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEACLASE")));

						countIdClase ++;
					}
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEAGRUPO"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAGRUPO")) > 0) ){
						idGrupo.append( ((countIdGrupo > 0) ? ", " : "" ) );
						idGrupo.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEAGRUPO")));

						countIdGrupo ++;
					}
					if ( (!cursorIttbp.isNull(cursorIttbp.getColumnIndex("ID_AEASGRUP"))) && (cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEASGRUP")) > 0) ){
						idSGrupo.append( ((countIdSGrupo > 0) ? ", " : "" ) );
						idSGrupo.append(cursorIttbp.getInt(cursorIttbp.getColumnIndex("ID_AEASGRUP")));

						countIdSGrupo ++;
					}
				}
				idAgrupProd.append(")");

				if ( (countIdAgrupProd > 0) && (idAgrupProd.length() > 0) ){
					Cursor cursorAgruProd = produtoSql.sqlSelect("SELECT * FROM AEAITGPR WHERE AEAITGPR.ID_AEAAGPPR IN " + idAgrupProd.toString());

					if ((cursorAgruProd != null) && (cursorAgruProd.getCount() > 0)){

						while (cursorAgruProd.moveToNext()) {
							// Verifica se tem algum produto na promocao valida
							if ((!cursorAgruProd.isNull(cursorAgruProd.getColumnIndex("ID_AEAPRODU"))) && (cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAPRODU")) > 0)) {
								idProdu.append(((countIdProdu > 0) ? ", " : ""));
								idProdu.append(cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAPRODU")));

								countIdProdu++;
							}
							if ((!cursorAgruProd.isNull(cursorAgruProd.getColumnIndex("ID_AEAMARCA"))) && (cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAMARCA")) > 0)) {
								idMarca.append(((countIdMarca > 0) ? ", " : ""));
								idMarca.append(cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAMARCA")));

								countIdMarca++;
							}
							if ((!cursorAgruProd.isNull(cursorAgruProd.getColumnIndex("ID_AEAFAMIL"))) && (cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAFAMIL")) > 0)) {
								idFamil.append(((countIdFamil > 0) ? ", " : ""));
								idFamil.append(cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAFAMIL")));

								countIdFamil++;
							}
							if ((!cursorAgruProd.isNull(cursorAgruProd.getColumnIndex("ID_AEACLASE"))) && (cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEACLASE")) > 0)) {
								idClase.append(((countIdClase > 0) ? ", " : ""));
								idClase.append(cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEACLASE")));

								countIdClase++;
							}
							if ((!cursorAgruProd.isNull(cursorAgruProd.getColumnIndex("ID_AEAGRUPO"))) && (cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAGRUPO")) > 0)) {
								idGrupo.append(((countIdGrupo > 0) ? ", " : ""));
								idGrupo.append(cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEAGRUPO")));

								countIdGrupo++;
							}
							if ((!cursorAgruProd.isNull(cursorAgruProd.getColumnIndex("ID_AEASGRUP"))) && (cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEASGRUP")) > 0)) {
								idSGrupo.append(((countIdSGrupo > 0) ? ", " : ""));
								idSGrupo.append(cursorAgruProd.getInt(cursorAgruProd.getColumnIndex("ID_AEASGRUP")));

								countIdSGrupo++;
							}
						}
					}
				}
				idProdu.append(")");
				idMarca.append(")");
				idFamil.append(")");
				idClase.append(")");
				idGrupo.append(")");
				idSGrupo.append(")");

				if ( (countIdProdu + countIdMarca + countIdFamil + countIdClase + countIdGrupo + countIdSGrupo) > 0) {
					int i = 0;
					// Adiciona o where no sql principal
					sql.append(" AND ( ");

					if ((idProdu.length() > 0) && (countIdProdu > 0)){
						sql.append(" ( ").append(idProdu.toString()).append(" ) ");
						i++;
					}
					if ((idMarca.length() > 0) && (countIdMarca > 0)){
						sql.append((i > 0) ? " OR " : "");
						sql.append(" ( ").append(idMarca.toString()).append(" ) ");
						i++;
					}
					if ((idFamil.length() > 0) && (countIdFamil > 0)){
						sql.append((i > 0) ? " OR " : "");
						sql.append(" ( ").append(idFamil.toString()).append(" ) ");
						i++;
					}
					if ((idClase.length() > 0) && (countIdClase > 0)){
						sql.append((i > 0) ? " OR " : "");
						sql.append(" ( ").append(idClase.toString()).append(" ) ");
						i++;
					}
					if ((idGrupo.length() > 0) && (countIdGrupo > 0)){
						sql.append((i > 0) ? " OR " : "");
						sql.append(" ( ").append(idGrupo.toString()).append(" ) ");
						i++;
					}
					if ((idSGrupo.length() > 0) && (countIdSGrupo > 0)){
						sql.append((i > 0) ? " OR " : "");
						sql.append(" ( ").append(idSGrupo.toString()).append(" ) ");
						i++;
					}

					sql.append(" ) ");
				}
			}

		}
		if (group != null){
			sql.append(" ").append(group).append(" ");
		}
		// Adiciona a ordem no sql
		sql.append(" ORDER BY AEAPRODU.DESCRICAO, AEAUNVEN.SIGLA, AEAMARCA.DESCRICAO ");

		// Cria uma lista para armazenar todas os produtos retornados do banco
		List<ProdutoListaBeans> listaProduto = new ArrayList<ProdutoListaBeans>();

		final Cursor cursor = produtoSql.sqlSelect(sql.toString());

		// Se o cursor tiver algum valor entra no laco
		if( (cursor != null) && (cursor.getCount() > 0) ){
			// Checa se tem alguma barra de progresso
			if (progresso != null){
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						progresso.setIndeterminate(false);
						progresso.setProgress(0);
						progresso.setMax(cursor.getCount());
					}
				});
			}
			int incremento = 0;
			try{
				while(cursor.moveToNext()){
				    // Verifica se tem algum progressbar de status
					if (progresso != null) {
						incremento++;
						final int finalIncremento = incremento;
						((Activity) context).runOnUiThread(new Runnable() {
							public void run() {
								progresso.setProgress(finalIncremento);
							}
						});
					}
					String dataAtual = "";
					// Preenche os dados do produto
					ProdutoBeans produto = new ProdutoBeans();
					produto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
					produto.setCodigoEstrutural(cursor.getString(cursor.getColumnIndex("CODIGO_ESTRUTURAL")));
					produto.setReferencia(cursor.getString(cursor.getColumnIndex("REFERENCIA")));
					produto.setDescricaoProduto(cursor.getString(cursor.getColumnIndex("DESCRICAO_PRODU")));
					produto.setDescricaoMarca(cursor.getString(cursor.getColumnIndex("DESCRICAO_MARCA")));
					produto.setPesoBruto(cursor.getDouble(cursor.getColumnIndex("PESO_BRUTO")));
					produto.setPesoLiquido(cursor.getDouble(cursor.getColumnIndex("PESO_LIQUIDO")));
					dataAtual = cursor.getString(cursor.getColumnIndex("DATA_ATUAL"));
					String tipo = cursor.getString(cursor.getColumnIndex("TIPO"));
					if((tipo != null) && (tipo.length() > 0)){
						produto.setTipoProduto(tipo);
					}else{
						produto.setTipoProduto("P");
					}
					produto.setDiasCadastro(cursor.getInt(cursor.getColumnIndex("DIAS_CADASTRO")));

					// Pega a unidade de venda do produto
					UnidadeVendaBeans unidadeVenda = new UnidadeVendaBeans();
					unidadeVenda.setIdUnidadeVenda(cursor.getInt(cursor.getColumnIndex("ID_AEAUNVEN")));
					unidadeVenda.setSiglaUnidadeVenda(cursor.getString(cursor.getColumnIndex("SIGLA")));
					// Adiciiona a unidade de venda no produto
					produto.setUnidadeVendaProduto(unidadeVenda);

					// Pega a classe do produto
					ClasseBeans classe = new ClasseBeans();
					classe.setCodigoClasse(cursor.getInt(cursor.getColumnIndex("CODIGO_CLASE")));
					classe.setDescricaoClasse(cursor.getString(cursor.getColumnIndex("DESCRICAO_CLASE")));
					// Adiciona a classe no produto
					produto.setClasseProduto(classe);

					// Adiciona o produto a lista
                    ProdutoListaBeans produtoLista = new ProdutoListaBeans();
					produtoLista.setIdPLoja(cursor.getInt(cursor.getColumnIndex("ID_AEAPLOJA")));
					produtoLista.setValorTabelaAtacado(cursor.getDouble(cursor.getColumnIndex("VENDA_ATAC_TABELA")));
					produtoLista.setValorTabelaVarejo(cursor.getDouble(cursor.getColumnIndex("VENDA_VARE_TABELA")));
					//produtoLista.setValorUnitarioAtacado(cursor.getDouble(cursor.getColumnIndex("VENDA_ATAC_FINAL")));
					//produtoLista.setValorUnitarioVarejo(cursor.getDouble(cursor.getColumnIndex("VENDA_VARE_FINAL")));
					//produtoLista.setValorPromocaoAtacadoVista(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_ATAC_VISTA")));
					//produtoLista.setValorPromocaoAtacadoPrazo(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_ATAC_PRAZO")));
					//produtoLista.setValorPromocaoVarejoVista(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_VARE_VISTA")));
					//produtoLista.setValorPromocaoVarejoPrazo(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_VARE_PRAZO")));
					produtoLista.setCustoReposicaoN(cursor.getDouble(cursor.getColumnIndex("CT_REPOSICAO_N")));
					produtoLista.setCustoCompleto(cursor.getDouble(cursor.getColumnIndex("CT_COMPLETO_N")));
					produtoLista.setEstoqueFisico(cursor.getDouble(cursor.getColumnIndex("ESTOQUE_FISICO")));
					produtoLista.setEstoqueContabil(cursor.getDouble(cursor.getColumnIndex("ESTOQUE_CONTABIL")));

					// Verifica se tem numero de orcamento para pesquisar
					if((idOrcamento != null) && (idOrcamento.length() > 0)){
						OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);
						idCliente = orcamentoRotinas.codigoClienteOrcamento(idOrcamento);
						atacadoVarejo = orcamentoRotinas.atacadoVarejoOrcamento(idOrcamento);

						//Verifica se o produto esta dentro de um orcamento
						if( marcaProdutoJaComprados(String.valueOf(produto.getIdProduto()), idOrcamento) ){
							produtoLista.setEstaNoOrcamento('1');
						}
					}
					if((idPlPgto != null) && (idPlPgto == 0)){
						EmpresaSql empresaSql = new EmpresaSql(context);
						Cursor dadosPlpgt = empresaSql.query("ID_SMAEMPRE = " + idEmpresa);

						if (dadosPlpgt != null && dadosPlpgt.getCount() > 0){
							dadosPlpgt.moveToFirst();
							// Checa se eh Atacado
							if (atacadoVarejo.equalsIgnoreCase("0")){
								idPlPgto = dadosPlpgt.getInt(dadosPlpgt.getColumnIndex("ID_AEAPLPGT_ATAC"));
							// Checa se eh Varejo
							} else if (atacadoVarejo.equalsIgnoreCase("1")){
								idPlPgto = dadosPlpgt.getInt(dadosPlpgt.getColumnIndex("ID_AEAPLPGT_VARE"));
							}
						}
					}


                    int diasProdutoNovo = cursor.getInt(cursor.getColumnIndex("QTD_DIAS_DESTACA_PRODUTO"));

                    if((diasProdutoNovo > 0) && (diasProdutoNovo >= produto.getDiasCadastro()) && (produtoLista.getEstaNoOrcamento() != '1')){
                        produtoLista.setProdutoNovo(true);
                    }

                    // Cria uma vareavel para salvar as embalagens
                    List<EmbalagemBeans> listaEmbalagem = new ArrayList<EmbalagemBeans>();

                    if (todasEmbalagens.equalsIgnoreCase(NAO)){
                        // Instancia a classe de embalagem
                        EmbalagemBeans embalagem = new EmbalagemBeans();
                        // Preenche os dados da embalagem
                        embalagem.setIdEmbalagem(cursor.getInt(cursor.getColumnIndex("ID_AEAEMBAL")));
                        embalagem.setIdUnidadeVenda(cursor.getInt(cursor.getColumnIndex("ID_AEAUNVEN")));
                        String ativo = "";
                        ativo = cursor.getString(cursor.getColumnIndex("ATIVO"));
                        if((ativo != null) && (ativo.length() > 0)){
                            embalagem.setAtivo(ativo);
                        }
                        String principal = "";
                        principal = cursor.getString(cursor.getColumnIndex("PRINCIPAL"));
                        if((principal != null) && (principal.length() > 0)){
                            embalagem.setPrincipal(principal);
                        }
                        embalagem.setDescricaoEmbalagem(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
                        embalagem.setFatorConversao(cursor.getDouble(cursor.getColumnIndex("FATOR_CONVERSAO")));
                        embalagem.setFatorPreco(cursor.getDouble(cursor.getColumnIndex("FATOR_PRECO")));
                        embalagem.setModulo(cursor.getInt(cursor.getColumnIndex("MODULO")));
                        embalagem.setDecimais(cursor.getInt(cursor.getColumnIndex("DECIMAIS")));

                        // Instancia novamente a vareavel unidade de venda
                        unidadeVenda = new UnidadeVendaBeans();
                        // Preenche os dados da unidade de venda
                        unidadeVenda.setIdUnidadeVenda(cursor.getInt(cursor.getColumnIndex("ID_AEAUNVEN")));
                        unidadeVenda.setSiglaUnidadeVenda(cursor.getString(cursor.getColumnIndex("SIGLA_UNVEN")));
                        unidadeVenda.setDescricaoUnidadeVenda(cursor.getString(cursor.getColumnIndex("DESCRICAO_SINGULAR_UNVEN")));
                        unidadeVenda.setCasasDecimais(cursor.getInt(cursor.getColumnIndex("DECIMAIS_UNVEN")));

                        embalagem.setUnidadeVendaEmbalagem(unidadeVenda);

                        // Adiciona a embalagem a uma lista
                        listaEmbalagem.add(embalagem);

                        // Adiciona uma lista de embalagens no produto
                        produto.setListaEmbalagem(listaEmbalagem);
                    } else {
                        // Instancia a clesse de embalagens
                        EmbalagemSql embalagemSql = new EmbalagemSql(context);

                        Cursor cursorEmbalagem = embalagemSql.sqlSelect(
                                "SELECT AEAEMBAL.ID_AEAEMBAL, AEAEMBAL.ID_AEAPRODU, AEAEMBAL.ID_AEAUNVEN, AEAEMBAL.DT_ALT, AEAEMBAL.PRINCIPAL, AEAEMBAL.DESCRICAO, \n" +
                                        "AEAEMBAL.FATOR_CONVERSAO, AEAEMBAL.FATOR_PRECO, AEAEMBAL.MODULO, AEAEMBAL.DECIMAIS, AEAEMBAL.ATIVO, \n" +
                                        "AEAUNVEN.SIGLA AS SIGLA_UNVEN, AEAUNVEN.DESCRICAO_SINGULAR AS DESCRICAO_SINGULAR_UNVEN, AEAUNVEN.DECIMAIS AS DECIMAIS_UNVEN \n" +
                                        "FROM AEAEMBAL \n" +
                                        "LEFT OUTER JOIN AEAUNVEN ON(AEAEMBAL.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN) \n" +
                                        "WHERE (AEAEMBAL.ATIVO = '1') AND (AEAEMBAL.ID_AEAPRODU = " + produto.getIdProduto() + ") \n" +
                                        "ORDER BY COALESCE(AEAEMBAL.ATIVO)");

                        // Verifica se retornou algum registro
                        if ((cursorEmbalagem != null) && (cursorEmbalagem.getCount() > 0)) {
                            // Enquanto tiver registro vai para frente
                            while (cursorEmbalagem.moveToNext()) {
                                // Instancia a classe de embalagem
                                EmbalagemBeans embalagem = new EmbalagemBeans();
                                // Preenche os dados da embalagem
                                embalagem.setIdEmbalagem(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("ID_AEAEMBAL")));
                                embalagem.setIdUnidadeVenda(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("ID_AEAUNVEN")));
                                String ativo = "";
                                ativo = cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("ATIVO"));
                                if ((ativo != null) && (ativo.length() > 0)) {
                                    embalagem.setAtivo(ativo);
                                }
                                String principal = "";
                                principal = cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL"));
                                if ((principal != null) && (principal.length() > 0)) {
                                    embalagem.setPrincipal(principal);
                                }
                                embalagem.setDescricaoEmbalagem(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("DESCRICAO")));
                                embalagem.setFatorConversao(cursorEmbalagem.getDouble(cursorEmbalagem.getColumnIndex("FATOR_CONVERSAO")));
                                embalagem.setFatorPreco(cursorEmbalagem.getDouble(cursorEmbalagem.getColumnIndex("FATOR_PRECO")));
                                embalagem.setModulo(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("MODULO")));
                                embalagem.setDecimais(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("DECIMAIS")));
                                // Instancia novamente a vareavel unidade de venda
                                unidadeVenda = new UnidadeVendaBeans();
                                // Preenche os dados da unidade de venda
                                unidadeVenda.setIdUnidadeVenda(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("ID_AEAUNVEN")));
                                unidadeVenda.setSiglaUnidadeVenda(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("SIGLA_UNVEN")));
                                unidadeVenda.setDescricaoUnidadeVenda(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("DESCRICAO_SINGULAR_UNVEN")));
                                unidadeVenda.setCasasDecimais(cursorEmbalagem.getInt(cursorEmbalagem.getColumnIndex("DECIMAIS_UNVEN")));

                                embalagem.setUnidadeVendaEmbalagem(unidadeVenda);
                                // Adiciona a embalagem a uma lista
                                listaEmbalagem.add(embalagem);
                            } // FIm do while Embalagem
                            // Adiciona uma lista de embalagens no produto
                            produto.setListaEmbalagem(listaEmbalagem);
                        }
                    }

					// Adiciona o produto a lista
					produtoLista.setProduto(produto);

                    // Verifica se eh pra executar a procedure calcula preco
                    if( (calculaPreco != null) && (calculaPreco.equalsIgnoreCase(SIM)) ) {

                    	CalculaPrecoSP calculaPrecoSP = new CalculaPrecoSP(context, null, null);
						ContentValues retornoPreco = calculaPrecoSP.execute(produtoLista.getIdPLoja(),
								produtoLista.getProduto().getListaEmbalagem().get(0).getIdEmbalagem(),
								idPlPgto,
								Integer.parseInt(idCliente),
								Integer.parseInt(codigoVendedor),
								dataAtual,
								produtoLista.getValorTabelaAtacado(),
								produtoLista.getValorTabelaVarejo());

						if (retornoPreco != null) {
							produtoLista.setValorUnitarioAtacado(retornoPreco.getAsDouble(CalculaPrecoSP.KEY_PRECO_ATACADO));
							produtoLista.setValorUnitarioVarejo(retornoPreco.getAsDouble(CalculaPrecoSP.KEY_PRECO_VAREJO));
							produtoLista.setProdutoPromocaoAtacado(retornoPreco.getAsString(CalculaPrecoSP.KEY_PRODUTO_PROMOCAO_ATACADO));
							produtoLista.setProdutoPromocaoVarejo(retornoPreco.getAsString(CalculaPrecoSP.KEY_PRODUTO_PROMOCAO_VAREJO));
							produtoLista.setProdutoPromocaoServico(retornoPreco.getAsString(CalculaPrecoSP.KEY_PRODUTO_PROMOCAO_SERVICO));
						}
					} else if ((calculaPreco != null) && (calculaPreco.equalsIgnoreCase(APENAS_PRODUTO_PROMOCAO)) ){

                    	produtoLista.setValorUnitarioAtacado(produtoLista.getValorTabelaAtacado());
						produtoLista.setValorUnitarioVarejo(produtoLista.getValorTabelaVarejo());
						produtoLista.setProdutoPromocaoAtacado("1");
						produtoLista.setProdutoPromocaoVarejo("1");
						produtoLista.setProdutoPromocaoServico("1");

					} else {
						produtoLista.setValorUnitarioAtacado(produtoLista.getValorTabelaAtacado());
						produtoLista.setValorUnitarioVarejo(produtoLista.getValorTabelaVarejo());
						produtoLista.setProdutoPromocaoAtacado("0");
						produtoLista.setProdutoPromocaoVarejo("0");
						produtoLista.setProdutoPromocaoServico("0");

						StringBuilder sqlTemp = new StringBuilder();
						sqlTemp.append( "SELECT TIPO, ID_AEACLASE, ID_AEAGRUPO, ID_AEASGRUP, ID_AEAFAMIL, ID_AEAMARCA FROM AEAPRODU WHERE ID_AEAPRODU = " + produtoLista.getProduto().getIdProduto());

						Cursor dados = produtoSql.sqlSelect(sqlTemp.toString());

						if ((dados != null) && (dados.getCount() > 0)){
							dados.moveToFirst();

							String tipoProduto = dados.getString(dados.getColumnIndex("TIPO"));
							Integer idClasse = dados.getInt(dados.getColumnIndex("ID_AEACLASE"));
							Integer idGrupo = dados.getInt(dados.getColumnIndex("ID_AEAGRUPO"));
							Integer idSubGrupo = dados.getInt(dados.getColumnIndex("ID_AEASGRUP"));
							Integer idFamilia = dados.getInt(dados.getColumnIndex("ID_AEAFAMIL"));
							Integer idMarca = dados.getInt(dados.getColumnIndex("ID_AEAMARCA"));

							StringBuilder sqlTbpro = new StringBuilder();
							sqlTbpro.append( "(SELECT ID_AEATBPRO \n" +
									"FROM AEATBPRO \n" +
									"WHERE (ATIVO = '1') \n" +
									"AND (DT_INICIO <= '" + dataAtual + "') AND (DT_FIM >= '" + dataAtual + "') \n" +
									"AND ((DIAS = '') OR (DIAS IS NULL) OR (DIAS LIKE '%' || (STRFTIME('%w', '" + dataAtual + "')) || '%')) \n" +
									"AND (" + idEmpresa + " IN (SELECT ID_SMAEMPRE FROM AEAEMTBP WHERE AEAEMTBP.ID_AEATBPRO = AEATBPRO.ID_AEATBPRO AND AEAEMTBP.ID_SMAEMPRE = " + idEmpresa + ")) )");

							sqlTemp.setLength(0);
							sqlTemp.append( "SELECT ID_AEAITTBP, DESC_MERC_VISTA_ATAC, DESC_MERC_VISTA_VARE, DESC_MERC_PRAZO_ATAC, DESC_MERC_PRAZO_VARE, \n" +
									"DESC_SERV_VISTA, DESC_SERV_PRAZO, PRECO_VISTA_VARE, PRECO_VISTA_ATAC, PRECO_PRAZO_VARE, \n" +
									"PRECO_PRAZO_ATAC, PRECO_VISTA_SERV, PRECO_PRAZO_SERV \n" +
									"FROM AEAITTBP \n" +
									"WHERE (ID_AEATBPRO IN (" + sqlTbpro.toString() + ") ) AND \n" +
									"((" + produtoLista.getProduto().getIdProduto() + " = ID_AEAPRODU) OR (" + idMarca + " = ID_AEAMARCA) \n" +
									"OR (" + idSubGrupo + " = ID_AEASGRUP) OR (" + idGrupo + " = ID_AEAGRUPO) OR (" + idClasse + " = ID_AEACLASE) \n" +
									"OR (" + idFamilia + " = ID_AEAFAMIL) \n" +
									"OR ((ID_AEAAGPPR IS NOT NULL) AND (ID_AEAAGPPR IN " +
									"(SELECT ID_AEAAGPPR \n" +
									"FROM AEAITGPR \n" +
									"WHERE (" + produtoLista.getProduto().getIdProduto() + " = AEAITGPR.ID_AEAPRODU) OR (" + idMarca + " = AEAITGPR.ID_AEAMARCA) \n" +
									"OR (" + idSubGrupo + " = AEAITGPR.ID_AEASGRUP) OR (" + idGrupo + " = AEAITGPR.ID_AEAGRUPO) OR (" + idClasse + " = AEAITGPR.ID_AEACLASE) \n" +
									"OR (" + idFamilia + " = AEAITGPR.ID_AEAFAMIL))))) \n" +
									"ORDER BY COALESCE(ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_AEAAGPPR) LIMIT 1 ");

							Cursor dadosItemPromo = produtoSql.sqlSelect(sqlTemp.toString());
							if ((dadosItemPromo != null) && (dadosItemPromo.getCount() > 0)) {
								dadosItemPromo.moveToFirst();

								double somaAtacado = 0;
								double somaVarejo = 0;
								double somaServico = 0;

								somaAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_VISTA_ATAC")) + dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_PRAZO_ATAC")) +
											  dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_VISTA_ATAC")) + dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_PRAZO_ATAC"));

								somaVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_VISTA_VARE")) + dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_PRAZO_VARE")) +
											 dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_VISTA_VARE")) + dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_PRAZO_VARE"));

								somaServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_SERV_VISTA")) + dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_SERV_PRAZO")) +
											  dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_VISTA_SERV")) + dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_PRAZO_SERV"));

								if (somaAtacado != 0){
									produtoLista.setProdutoPromocaoAtacado("1");
								}
								if (somaVarejo != 0){
									produtoLista.setProdutoPromocaoVarejo("1");
								}
								if (somaServico != 0){
									produtoLista.setProdutoPromocaoServico("1");
								}
							}
						}
					}
					listaProduto.add(produtoLista);
				} // Fim primeiro while

			}catch(final Exception e){

                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("ProdutoRotinas")
                                .content("Erro ao carregar os dados do produto. \n" + e.getMessage())
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
			}

		} // Fim primeiro if

		return listaProduto;
	} // Fim listaProduto



	@Deprecated
	public boolean marcaProdutoJaComprados(String idProduto, String idOrcamento){
		// Vareavel para retornar se tem o produto no orcamento, sim ou nao
		boolean retorno = false;

		try{
			if(idOrcamento != null){
				// Instancia a classe para manipular o orcamento no banco de dados
				OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(this.context);
				// Cria uma simples lista
				List<String> listaIDItemOrcamento = new ArrayList<String>();
				// Inseri os dados na lista
				listaIDItemOrcamento = orcamentoRotinas.listaItemOrcamentoApenasID(null, idOrcamento);

				// Verifica se tem algum id na lista
				if(listaIDItemOrcamento.size() > 0){
					// Percorre toda a lista
					for(int i = 0; listaIDItemOrcamento.size() > i; i++){
						// verifica se o produto esta no orcamento
						if( (idProduto.equals(listaIDItemOrcamento.get(i))) ){
							retorno = true;
						}
					}
				}
			}

		}catch (Exception e){
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(this.context);
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 0);
			mensagem.put("tela", "ProdutoListaActivity");
			mensagem.put("mensagem", "Mensagem: " + e.getMessage() + "\n Dados: " + e);
			mensagem.put("dados", "ProdutoListaActivity: " + e.getMessage());
			mensagem.put("usuario", funcoes.getValorXml("Usuario"));
			mensagem.put("usuario", funcoes.getValorXml("ChaveEmpresa"));
			mensagem.put("usuario", funcoes.getValorXml("Email"));
			// Executa a mensagem passando por parametro as propriedades
			funcoes.menssagem(mensagem);
		}
		return retorno;
	} // Fim marcaProdutoJaComprados


	public int casasDecimaisProduto(String idEmbalagem, String idProduto){
		int casasDecimais = 0;

		EmbalagemSql embalagemSql = new EmbalagemSql(context);

		String sql = "SELECT DECIMAIS FROM AEAEMBAL WHERE AEAEMBAL.ID_AEAEMBAL = " + idEmbalagem + " AND AEAEMBAL.ID_AEAPRODU = " + idProduto;

		Cursor dados = embalagemSql.sqlSelect(sql);
		// Move para o primeiro registro
		dados.moveToFirst();

		casasDecimais = dados.getInt(dados.getColumnIndex("DECIMAIS"));

		return casasDecimais;
	}


	/**
	 * Retorna os dados completos de um unico produtos,
	 * de acordo com o id passado por parametro.
	 *
	 * @param idProduto
	 * @return
	 */
	public AeaproduBeans detalhesProduto(String idProduto){

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT AEAPRODU.ID_AEAPRODU, AEAPRODU.ID_AEAUNVEN, AEAPRODU.ID_AEACLASE, AEAPRODU.ID_AEAMARCA, ");
		sql.append("AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.DESCRICAO, AEAPRODU.DESCRICAO_AUXILIAR, AEAPRODU.REFERENCIA, ");
		sql.append("AEAPRODU.CODIGO_BARRAS, AEAPRODU.PESO_LIQUIDO, AEAPRODU.PESO_BRUTO, AEAPRODU.TIPO ");
		sql.append("FROM AEAPRODU ");
		sql.append("WHERE AEAPRODU.ID_AEAPRODU = ").append(idProduto);

		ProdutoSql produtoSql = new ProdutoSql(context);

		Cursor dadosProduto = produtoSql.sqlSelect(sql.toString());

		AeaproduBeans produto = null;

		// Checa se retornou algum registro do banco de dados
		if((dadosProduto != null) && (dadosProduto.getCount() > 0)){
			// Move o cursor para o primeiro registro
			dadosProduto.moveToFirst();
			produto = new AeaproduBeans();
			produto.setIdAeaprodu(dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAPRODU")));
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("ID_AEAMARCA"))) {
				AeamarcaBeans aeamarca = new AeamarcaBeans();
				aeamarca.setIdAeamarca(dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAMARCA")));
				produto.setAeamarca(aeamarca);
			}
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("ID_AEAUNVEN"))) {
				AeaunvenBeans aeaunven = new AeaunvenBeans();
				aeaunven.setIdAeaunven(dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAUNVEN")));
				produto.setAeaunven(aeaunven);
			}
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("ID_AEACLASE"))) {
				AeaclaseBeans aeaclase = new AeaclaseBeans();
				aeaclase.setIdAeaclase(dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEACLASE")));
				produto.setAeaclase(aeaclase);
			}
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("CODIGO_ESTRUTURAL"))) produto.setCodigoEstrutural(dadosProduto.getString(dadosProduto.getColumnIndex("CODIGO_ESTRUTURAL")));
			produto.setDescricao(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO")));
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("DESCRICAO_AUXILIAR"))) produto.setDescricaoAuxiliar(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO_AUXILIAR")));
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("REFERENCIA"))) produto.setReferencia(dadosProduto.getString(dadosProduto.getColumnIndex("REFERENCIA")));
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("CODIGO_BARRAS"))) produto.setCodigoBarras(dadosProduto.getString(dadosProduto.getColumnIndex("CODIGO_BARRAS")));
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("PESO_LIQUIDO"))) produto.setPesoLiquido(dadosProduto.getDouble(dadosProduto.getColumnIndex("PESO_LIQUIDO")));
			if (!dadosProduto.isNull(dadosProduto.getColumnIndex("PESO_BRUTO"))) produto.setPesoBruto(dadosProduto.getDouble(dadosProduto.getColumnIndex("PESO_BRUTO")));
			if( (dadosProduto.getString(dadosProduto.getColumnIndex("TIPO")) != null) && (dadosProduto.getString(dadosProduto.getColumnIndex("TIPO")).length() > 0)){
				produto.setTipo(dadosProduto.getString(dadosProduto.getColumnIndex("TIPO")));
			}
		}
		return produto;
	}// Fim detalhesProduto


	public List<DescricaoDublaBeans> listaDetalhesProduto(String idProduto){
		List<DescricaoDublaBeans> listaDetalhes = new ArrayList<DescricaoDublaBeans>();

		AeaproduBeans produtoBeans = detalhesProduto(idProduto);

		DescricaoDublaBeans descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getCodigoEstrutural());
		descricaoDupla.setTextoSecundario("Código Produto");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getDescricaoAuxiliar());
		descricaoDupla.setTextoSecundario("Descrição Auxiliar");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getReferencia());
		descricaoDupla.setTextoSecundario("Referência");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getCodigoBarras());
		descricaoDupla.setTextoSecundario("Código de Barras");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(""+ funcoes.arredondarValor(produtoBeans.getPesoLiquido()));
		descricaoDupla.setTextoSecundario("Peso Líquido");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(""+ funcoes.arredondarValor(produtoBeans.getPesoBruto()));
		descricaoDupla.setTextoSecundario("Peso Bruto");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		descricaoDupla = new DescricaoDublaBeans();
		//descricaoDupla.setTextoPrincipal(produtoBeans.getCodigoEstrutural());
		descricaoDupla.setTextoSecundario("Tipo");
		if(String.valueOf(produtoBeans.getTipo()) != null){

			if(produtoBeans.getTipo() == "0"){
				descricaoDupla.setTextoPrincipal("Produto");

			} else if(produtoBeans.getTipo() == "1"){
				descricaoDupla.setTextoPrincipal("Serviço");

			} else if(produtoBeans.getTipo() == "2"){
				descricaoDupla.setTextoPrincipal("Conjunto");

			} else if(produtoBeans.getTipo() == "3"){
				descricaoDupla.setTextoPrincipal("Grade");
			}
		}
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);

		AeamarcaRotinas aeamarcaRotinas = new AeamarcaRotinas(context);
		if (produtoBeans.getAeamarca() != null) {
			AeamarcaBeans aeamarca = aeamarcaRotinas.selectMarca(produtoBeans.getAeamarca().getIdAeamarca());
			if (aeamarca != null) {
				descricaoDupla = new DescricaoDublaBeans();
				descricaoDupla.setTextoPrincipal(aeamarca.getDescricao());
				descricaoDupla.setTextoSecundario("Marca");
				// Adiciona a lista
				listaDetalhes.add(descricaoDupla);
			}
		}

		AeaunvenRotinas aeaunvenRotinas = new AeaunvenRotinas(context);
		AeaunvenBeans aeaunven = aeaunvenRotinas.selectUnidadeVenda(produtoBeans.getAeaunven().getIdAeaunven());
		if (aeaunven != null){
			descricaoDupla = new DescricaoDublaBeans();
			descricaoDupla.setTextoPrincipal(aeaunven.getSigla() + " - " + aeaunven.getDescricaosingular());
			descricaoDupla.setTextoSecundario("Unidade de Venda");
			// Adiciona a lista
			listaDetalhes.add(descricaoDupla);
		}

		AeaclaseRotinas aeaclaseRotinas = new AeaclaseRotinas(context);
		if (produtoBeans.getAeaclase() != null) {
			AeaclaseBeans aeaclase = aeaclaseRotinas.selectClasse(produtoBeans.getAeaclase().getIdAeaclase());
			if (aeaclase != null) {
				descricaoDupla = new DescricaoDublaBeans();
				descricaoDupla.setTextoPrincipal(aeaclase.getDescricao());
				descricaoDupla.setTextoSecundario("Classe/Grupo");
				// Adiciona a lista
				listaDetalhes.add(descricaoDupla);
			}
		}


		EmbalagemRotinas embalagemRotinas = new EmbalagemRotinas(context);
		// Pega todas as embalagens do produto
		List<EmbalagemBeans> listaEmbalagem = embalagemRotinas.selectEmbalagensProduto(idProduto);

		if ( (listaEmbalagem != null) && (listaEmbalagem.size() > 0) ){

			for(int i = 0; i < listaEmbalagem.size(); i++){
				descricaoDupla = new DescricaoDublaBeans();
				descricaoDupla.setTextoPrincipal(listaEmbalagem.get(i).getUnidadeVendaEmbalagem().getSiglaUnidadeVenda() + " - " + (listaEmbalagem.get(i).getDescricaoEmbalagem() != null ? listaEmbalagem.get(i).getDescricaoEmbalagem() : ""));
				descricaoDupla.setTextoSecundario("Embalagem do Produto");
				// Adiciona a lista
				listaDetalhes.add(descricaoDupla);
			}
		}
		
		return listaDetalhes;
	}

} // FIm da classe

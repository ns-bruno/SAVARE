package com.savare.funcoes.rotinas;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.ClasseSql;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.ProdutoSql;
import com.savare.banco.funcoesSql.UnidadeVendaSql;
import com.savare.beans.ClasseBeans;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.DescricaoSimplesBeans;
import com.savare.beans.EmbalagemBeans;
import com.savare.beans.EmpresaBeans;
import com.savare.beans.ProdutoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.beans.UnidadeVendaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

public class ProdutoRotinas extends Rotinas {

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
	
	
	public List<ProdutoListaBeans> listaProduto(String where, String group, String idOrcamento){
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		String idEmpresa = funcoes.getValorXml("CodigoEmpresa");
		String codigoVendedor = funcoes.getValorXml("CodigoUsuario");
		
		String sql = "SELECT AEAPLOJA.ID_AEAPLOJA, AEAPRODU.ID_AEAPRODU, AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.REFERENCIA, "
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
				   
				   + "AEAPLOJA.PROMOCAO_ATAC, AEAPLOJA.PROMOCAO_VARE," 
				   + "AEAPLOJA.CT_REPOSICAO_N, AEAPLOJA.CT_COMPLETO_N, "
				   + "AEAPLOJA.ESTOQUE_F ESTOQUE_FISICO, AEAPLOJA.ESTOQUE_C ESTOQUE_CONTABIL, "
				   + "AEACLASE.CODIGO AS CODIGO_CLASE, AEACLASE.DESCRICAO AS DESCRICAO_CLASE, AEAPRODU.PESO_BRUTO, AEAPRODU.PESO_LIQUIDO "
				   + "FROM AEAPLOJA AEAPLOJA "
				   + "LEFT OUTER JOIN AEAPRODU AEAPRODU ON  (AEAPRODU.ID_AEAPRODU = AEAPLOJA.ID_AEAPRODU) "
				   + "LEFT OUTER JOIN AEACLASE AEACLASE ON  (AEACLASE.ID_AEACLASE = AEAPRODU.ID_AEACLASE) "
				   + "LEFT OUTER JOIN AEAMARCA AEAMARCA ON  (AEAMARCA.ID_AEAMARCA = AEAPRODU.ID_AEAMARCA) "
				   + "LEFT OUTER JOIN AEAUNVEN AEAUNVEN ON  (AEAUNVEN.ID_AEAUNVEN = AEAPRODU.ID_AEAUNVEN) "
				   + "WHERE (AEAPRODU.ATIVO = '1') AND (AEAPRODU.DESCRICAO IS NOT NULL) ";
		
		// Adiciona a clausula where passada por parametro no sql
		if(where != null){
			sql = sql + " AND ( " + where +" ) ";
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
		
		Cursor cursor = produtoSql.sqlSelect(sql);
		
		// Se o cursor tiver algum valor entra no laco
		if(cursor.getCount() > 0){
			// Move o foco para o primeiro registro que esta dentro do cursor
			//cursor.moveToFirst();
			try{
				while(cursor.moveToNext()){
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
						produto.setTipoProduto(cursor.getString(cursor.getColumnIndex("TIPO")).charAt(0));
					}else{
						produto.setTipoProduto('P');
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
					//produtoLista.setProduto(produto);
					
					produtoLista.setValorTabelaAtacado(cursor.getDouble(cursor.getColumnIndex("VENDA_ATAC_TABELA")));
					produtoLista.setValorTabelaVarejo(cursor.getDouble(cursor.getColumnIndex("VENDA_VARE_TABELA")));
					produtoLista.setValorUnitarioAtacado(cursor.getDouble(cursor.getColumnIndex("VENDA_ATAC_FINAL")));
					produtoLista.setValorUnitarioVarejo(cursor.getDouble(cursor.getColumnIndex("VENDA_VARE_FINAL")));
					produtoLista.setValorPromocaoAtacado(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_ATAC")));
					produtoLista.setValorPromocaoVarejo(cursor.getDouble(cursor.getColumnIndex("PROMOCAO_VARE")));
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
							if((cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("ATIVO")) != null) && (cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("ATIVO")).length() > 0)){
								embalagem.setAtivo(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("ATIVO")).charAt(0));
							}
							if((cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL")) != null) && (cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL")).length() > 0)){
								embalagem.setPrincipal(cursorEmbalagem.getString(cursorEmbalagem.getColumnIndex("PRINCIPAL")).charAt(0));
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
			
		} // Fim primeiro if
		
		return listaProduto;
	} // Fim listaProduto
	

	
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
	
	public int diasProdutoNovo(String idEmpresa){
		// Cria variavel para pegar os dados da empresa
		int quantidadeDias = 0;
		
		// Variavel para manipular os dados do banco
		EmpresaSql empresaSql = new EmpresaSql(context);
		
		Cursor cursor = empresaSql.query("ID_SMAEMPRE = " + idEmpresa);
		
		if( (cursor != null) && (cursor.getCount() > 0)){
			cursor.moveToFirst();
			
			quantidadeDias = cursor.getInt(cursor.getColumnIndex("QTD_DIAS_DESTACA_PRODUTO"));
			
		}
		return quantidadeDias;
	} // Fim empresa
	
	/**
	 * Retorna os dados completos de um unico produtos,
	 * de acordo com passado por parametro.
	 * 
	 * @param idProduto
	 * @return
	 */
	public ProdutoBeans detalhesProduto(String idProduto){
		String sql = "SELECT AEAPRODU.ID_AEAPRODU, AEAPRODU.CODIGO_ESTRUTURAL, AEAPRODU.DESCRICAO, AEAPRODU.DESCRICAO_AUXILIAR, AEAPRODU.REFERENCIA, "
				   + "AEAPRODU.CODIGO_BARRAS, AEAPRODU.PESO_LIQUIDO, AEAPRODU.PESO_BRUTO, AEAPRODU.TIPO, "
				   + "AEAMARCA.DESCRICAO AS DESCRICAO_MARCA, AEAUNVEN.ID_AEAUNVEN, AEAUNVEN.SIGLA, AEAUNVEN.DESCRICAO_SINGULAR, "
				   + "AEACLASE.CODIGO AS CODIGO_CLASE, AEACLASE.DESCRICAO AS DESCRICAO_CLASE "
				   + "FROM AEAPRODU "
				   + "LEFT OUTER JOIN AEACLASE ON(AEAPRODU.ID_AEACLASE = AEACLASE.ID_AEACLASE) "
				   + "LEFT OUTER JOIN AEAMARCA ON(AEAPRODU.ID_AEAMARCA = AEAMARCA.ID_AEAMARCA) "
				   + "LEFT OUTER JOIN AEAUNVEN ON(AEAPRODU.ID_AEAUNVEN = AEAUNVEN.ID_AEAUNVEN) "
				   + "WHERE AEAPRODU.ID_AEAPRODU = " + idProduto;
		
		ProdutoSql produtoSql = new ProdutoSql(context);
		
		Cursor dadosProduto = produtoSql.sqlSelect(sql);
		
		ProdutoBeans produto = new ProdutoBeans();
		
		// Checa se retornou algum registro do banco de dados
		if((dadosProduto != null) && (dadosProduto.getCount() > 0)){
			// Move o cursor para o primeiro registro
			dadosProduto.moveToFirst();
			
			produto.setIdProduto(dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAPRODU")));
			produto.setCodigoEstrutural(dadosProduto.getString(dadosProduto.getColumnIndex("CODIGO_ESTRUTURAL")));
			produto.setDescricaoProduto(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO")));
			produto.setDescricaoAuxiliar(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO_AUXILIAR")));
			produto.setReferencia(dadosProduto.getString(dadosProduto.getColumnIndex("REFERENCIA")));
			produto.setCodigoBarras(dadosProduto.getString(dadosProduto.getColumnIndex("CODIGO_BARRAS")));
			produto.setPesoLiquido(dadosProduto.getDouble(dadosProduto.getColumnIndex("PESO_LIQUIDO")));
			produto.setPesoBruto(dadosProduto.getDouble(dadosProduto.getColumnIndex("PESO_BRUTO")));
			if( (dadosProduto.getString(dadosProduto.getColumnIndex("TIPO")) != null) && (dadosProduto.getString(dadosProduto.getColumnIndex("TIPO")).length() > 0)){
				produto.setTipoProduto(dadosProduto.getString(dadosProduto.getColumnIndex("TIPO")).charAt(0));
			}
			produto.setDescricaoMarca(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO_MARCA")));
			
			UnidadeVendaBeans unidadeVenda = new UnidadeVendaBeans();
			unidadeVenda.setIdUnidadeVenda(dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAUNVEN")));
			unidadeVenda.setSiglaUnidadeVenda(dadosProduto.getString(dadosProduto.getColumnIndex("SIGLA")));
			unidadeVenda.setDescricaoUnidadeVenda(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO_SINGULAR")));
			produto.setUnidadeVendaProduto(unidadeVenda);
			
			// Pega a classe do produto
			ClasseBeans classe = new ClasseBeans();
			classe.setCodigoClasse(dadosProduto.getInt(dadosProduto.getColumnIndex("CODIGO_CLASE")));
			classe.setDescricaoClasse(dadosProduto.getString(dadosProduto.getColumnIndex("DESCRICAO_CLASE")));
			// Adiciona a classe no produto
			produto.setClasseProduto(classe);
		}
		return produto;
	}// Fim detalhesProduto
	
	public List<DescricaoDublaBeans> listaDetalhesProduto(String idProduto){
		List<DescricaoDublaBeans> listaDetalhes = new ArrayList<DescricaoDublaBeans>();
		
		ProdutoBeans produtoBeans = detalhesProduto(idProduto);
		
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
		
		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(""+produtoBeans.getPesoLiquido());
		descricaoDupla.setTextoSecundario("Peso Líquido");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);
		
		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(""+produtoBeans.getPesoBruto());
		descricaoDupla.setTextoSecundario("Peso Bruto");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);
		
		descricaoDupla = new DescricaoDublaBeans();
		//descricaoDupla.setTextoPrincipal(produtoBeans.getCodigoEstrutural());
		descricaoDupla.setTextoSecundario("Tipo");
		if(String.valueOf(produtoBeans.getTipoProduto()) != null){
			
			if(produtoBeans.getTipoProduto() == '0'){
				descricaoDupla.setTextoPrincipal("Produto");
			
			} else if(produtoBeans.getTipoProduto() == '1'){
				descricaoDupla.setTextoPrincipal("Serviço");
			
			} else if(produtoBeans.getTipoProduto() == '2'){
				descricaoDupla.setTextoPrincipal("Conjunto");
			
			} else if(produtoBeans.getTipoProduto() == '3'){
				descricaoDupla.setTextoPrincipal("Grade");
			}
		}
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);
		
		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getDescricaoMarca());
		descricaoDupla.setTextoSecundario("Marca");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);
		
		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getUnidadeVendaProduto().getSiglaUnidadeVenda() + " - " + produtoBeans.getUnidadeVendaProduto().getDescricaoUnidadeVenda());
		descricaoDupla.setTextoSecundario("Unidade de Venda");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);
		
		descricaoDupla = new DescricaoDublaBeans();
		descricaoDupla.setTextoPrincipal(produtoBeans.getClasseProduto().getDescricaoClasse());
		descricaoDupla.setTextoSecundario("Classe/Grupo");
		// Adiciona a lista
		listaDetalhes.add(descricaoDupla);
		
		return listaDetalhes;
	}
	
} // FIm da classe

package com.savare.funcoes.rotinas;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.ProgressBar;

import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.beans.CidadeBeans;
import com.savare.beans.DescricaoSimplesBeans;
import com.savare.beans.EnderecoBeans;
import com.savare.beans.EstadoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.PortadorBancoBeans;
import com.savare.beans.RamoAtividadeBeans;
import com.savare.beans.StatusBeans;
import com.savare.beans.TelefoneBeans;
import com.savare.beans.TipoClienteBeans;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

public class PessoaRotinas extends Rotinas {

	public static final String KEY_TIPO_CLIENTE = "cliente",
							   KEY_TIPO_FONECEDOR = "fornecedor",
							   KEY_TIPO_USUARIO = "usuario";

	public PessoaRotinas(Context context) {
		super(context);
	}
	
	/**
	 * 
	 * @param tipoPessoa
	 * @return
	 */
	public List<DescricaoSimplesBeans> listaCidadePessoa(String tipoPessoa){
		// Cria uma lista para retornar as cidades
		List<DescricaoSimplesBeans> lista = new ArrayList<DescricaoSimplesBeans>();

		String sql = "SELECT CFACIDAD.DESCRICAO AS DESCRICAO_CIDAD FROM CFAENDER "
				   + "LEFT OUTER JOIN CFACIDAD ON CFAENDER.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD "
				   + "LEFT OUTER JOIN CFACLIFO ON CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO "
				   + "WHERE (CFACIDAD.DESCRICAO IS NOT NULL) ";
		
		// Verifica qual opcao foi passado por parametro
		if(tipoPessoa.equalsIgnoreCase("CLIENTE")){
			sql = sql + "AND (CFACLIFO.CLIENTE = 1) GROUP BY CFACIDAD.DESCRICAO ";
		
		}else {
			sql = sql + "GROUP BY CFACIDAD.DESCRICAO ";
		}
		// Instancia a classe para manipular o banco de dados
		PessoaSql clienteSql = new PessoaSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = clienteSql.sqlSelect(sql);
		
		if((cursor != null) && (cursor.getCount() > 0)){
						
			while (cursor.moveToNext()) {
				// Instancia a classe para salvar o nome da cidade
				DescricaoSimplesBeans cidade = new DescricaoSimplesBeans();
				// Seta o texto principal com o nome da cidade
				cidade.setTextoPrincipal(cursor.getString(cursor.getColumnIndex("DESCRICAO_CIDAD")));
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

	public List<CidadeBeans> listaCidadeCliente(String tipoPessoa){
		// Cria uma lista para retornar as cidades
		List<CidadeBeans> lista = new ArrayList<CidadeBeans>();

		String sql = "SELECT CFACIDAD.ID_CFACIDAD, CFACIDAD.DESCRICAO AS DESCRICAO_CIDAD, CFAESTAD.UF FROM CFAENDER "
				+ "LEFT OUTER JOIN CFACIDAD ON CFAENDER.ID_CFACIDAD = CFACIDAD.ID_CFACIDAD "
				+ "LEFT OUTER JOIN CFAESTAD ON CFACIDAD.ID_CFAESTAD = CFAESTAD.ID_CFAESTAD "
				+ "LEFT OUTER JOIN CFACLIFO ON CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO "
				+ "WHERE (CFACIDAD.DESCRICAO IS NOT NULL) ";

		// Verifica qual opcao foi passado por parametro
		if(tipoPessoa.equalsIgnoreCase(KEY_TIPO_CLIENTE)){
			sql = sql + "AND (CFACLIFO.CLIENTE = 1) GROUP BY CFACIDAD.DESCRICAO ";

		}else {
			sql = sql + "GROUP BY CFACIDAD.DESCRICAO ";
		}
		// Instancia a classe para manipular o banco de dados
		PessoaSql clienteSql = new PessoaSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = clienteSql.sqlSelect(sql);

		if((cursor != null) && (cursor.getCount() > 0)){

			while (cursor.moveToNext()) {
				// Instancia a classe para salvar o nome da cidade
				CidadeBeans cidade = new CidadeBeans();
				cidade.setIdCidade(cursor.getInt(cursor.getColumnIndex("ID_CFACIDAD")));
				cidade.setDescricao(cursor.getString(cursor.getColumnIndex("DESCRICAO_CIDAD")));

				EstadoBeans estado = new EstadoBeans();
				estado.setSiglaEstado(cursor.getString(cursor.getColumnIndex("UF")));
				cidade.setEstado(estado);
				// Adiciona a cidade em uma lista
				lista.add(cidade);
			}

		} else {
			CidadeBeans cidade = new CidadeBeans();
			cidade.setIdCidade(0);
			cidade.setDescricao("Nenhum valor encontrado");
			lista.add(cidade);
		}

		// Adiciona um valor padrao para selecionar todas as cidades
		CidadeBeans cidade = new CidadeBeans();
		cidade.setIdCidade(0);
		cidade.setDescricao("Todas as Cidades");
		lista.add(cidade);

		return lista;
	} // Fim listaCidadePessoa
	
	
	/**
	 * 
	 * @param where
	 * @param tipoPessoa - KEY_TIPO_CLIENTE, KEY_TIPO_FONECEDOR, KEY_TIPO_USUARIO
	 * @return
	 */
	public List<PessoaBeans> listaPessoaResumido(String where, String tipoPessoa, final ProgressBar progresso) {

		String sql = "SELECT CFACLIFO.ID_CFACLIFO, CFACLIFO.CODIGO_CLI, CFACLIFO.CODIGO_FUN, CFACLIFO.CODIGO_USU, "
					+"CFACLIFO.CODIGO_TRA, CFACLIFO.CLIENTE, "
					+"CFACLIFO.NOME_RAZAO, CFACLIFO.NOME_FANTASIA, CFACLIFO.PESSOA, CFACLIFO.DT_ULT_COMPRA, CFACLIFO.STATUS_CADASTRO_NOVO, "
					+"CFASTATU.ID_CFASTATU, CFASTATU.DESCRICAO AS DESCRICAO_STATU, CFASTATU.BLOQUEIA, CFASTATU.PARCELA_EM_ABERTO, CFASTATU.VISTA_PRAZO, CPF_CNPJ, CFACLIFO.IE_RG, "
					+"CFACIDAD.ID_CFACIDAD, CFACIDAD.DESCRICAO AS DESCRICAO_CIDAD, CFAESTAD.UF, CFAESTAD.ID_CFAESTAD, "
					+"CFAENDER.TIPO, CFAENDER.CEP, CFAENDER.BAIRRO, CFAENDER.LOGRADOURO, CFAENDER.NUMERO, CFAENDER.COMPLEMENTO, CFAENDER.EMAIL "
					+"FROM CFACLIFO "
					+"LEFT OUTER JOIN CFASTATU ON (CFACLIFO.ID_CFASTATU = CFASTATU.ID_CFASTATU) "
					+"LEFT OUTER JOIN CFAENDER ON (CFAENDER.ID_CFAENDER = (SELECT CFAENDER.ID_CFAENDER FROM CFAENDER WHERE (CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) LIMIT 1)) "
					+"LEFT OUTER JOIN CFAESTAD ON (CFAESTAD.ID_CFAESTAD = CFAENDER.ID_CFAESTAD) "
					+"LEFT OUTER JOIN CFACIDAD ON (CFACIDAD.ID_CFACIDAD = CFAENDER.ID_CFACIDAD) ";
		
		// Verifica se eh para retornar apenas os clientes
		if(tipoPessoa.equalsIgnoreCase(KEY_TIPO_CLIENTE)){
			sql = sql + "WHERE (CFACLIFO.CLIENTE = 1) ";
		
		// Verifica se eh para retornar apenas os fornecedores
		} else if(tipoPessoa.equalsIgnoreCase(KEY_TIPO_FONECEDOR)){
			sql = sql + "WHERE (CFACLIFO.FORNECEDOR = 1) ";
		
		// Retorna todos os registro, nao importando se eh cliente ou nao
		} else if (tipoPessoa.equalsIgnoreCase(KEY_TIPO_USUARIO)){
			sql = sql + "WHERE (CFACLIFO.USUARIO = 1) ";
		}
		// Adiciona a clausula where passada por parametro no sql
		if(where != null){
			sql = sql + " AND ( " + where +" ) ";
		}
		// Adiciona a ordem no sql
		sql = sql + "ORDER BY CFACLIFO.NOME_RAZAO, CFACLIFO.CPF_CNPJ, CFACIDAD.DESCRICAO ";
		
		// Cria uma lista para armazenar todas as pessoas retornadas do banco
		List<PessoaBeans> listaPessoas = new ArrayList<PessoaBeans>();
		
		// Instancia a classe para manipular o banco de dados
		PessoaSql pessoaSql = new PessoaSql(context);
		
		final Cursor dadosPessoa = pessoaSql.sqlSelect(sql);
		// Se o cursor tiver algum valor entra no laco
		if (dadosPessoa != null && dadosPessoa.getCount() > 0){

			// Checa se tem alguma barra de progresso
			if (progresso != null){
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						progresso.setIndeterminate(false);
						progresso.setProgress(0);
						progresso.setMax(dadosPessoa.getCount());
					}
				});
			}

			// Cria a variavel para salvar os dados da pesso
			PessoaBeans pessoa;
			
			// Move o foco para o primeiro registro que esta dentro do cursor
			dadosPessoa.moveToFirst();
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			
			// Enquanto o cursor for para o proximo registro e entra no laco
			for(int controle = 0; controle < dadosPessoa.getCount(); controle++){

				if (progresso != null) {

					final int finalControle = controle;
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							progresso.setProgress(finalControle);
						}
					});
				}
				// Instancia a classe de pessoa para armazenar os valores do banco
				pessoa = new PessoaBeans();
				// Preenche os dados da pessoa
				pessoa.setIdPessoa(dadosPessoa.getInt(dadosPessoa.getColumnIndex("ID_CFACLIFO")));
				pessoa.setCodigoCliente(dadosPessoa.getInt(dadosPessoa.getColumnIndex("CODIGO_CLI")));
				pessoa.setCodigoFuncionario(dadosPessoa.getInt(dadosPessoa.getColumnIndex("CODIGO_FUN")));
				pessoa.setCodigoUsuario(dadosPessoa.getInt(dadosPessoa.getColumnIndex("CODIGO_USU")));
				pessoa.setCodigoTransportadora(dadosPessoa.getInt(dadosPessoa.getColumnIndex("CODIGO_TRA")));
				pessoa.setNomeRazao(dadosPessoa.getString(dadosPessoa.getColumnIndex("NOME_RAZAO")));
				pessoa.setNomeFantasia(dadosPessoa.getString(dadosPessoa.getColumnIndex("NOME_FANTASIA")));
				pessoa.setDataUltimaCompra(funcoes.formataData(dadosPessoa.getString(dadosPessoa.getColumnIndex("DT_ULT_COMPRA"))));
				pessoa.setCpfCnpj(dadosPessoa.getString(dadosPessoa.getColumnIndex("CPF_CNPJ")));
				pessoa.setIeRg(dadosPessoa.getString(dadosPessoa.getColumnIndex("IE_RG")));
				// Checa se a pessoa eh um cadastro novo
				if ( (!dadosPessoa.isNull(dadosPessoa.getColumnIndex("STATUS_CADASTRO_NOVO"))) && (dadosPessoa.getString(dadosPessoa.getColumnIndex("STATUS_CADASTRO_NOVO")).equalsIgnoreCase("N")) ){
					pessoa.setCadastroNovo(true);
				} else {
					pessoa.setCadastroNovo(false);
				}
				if ((dadosPessoa.getString(dadosPessoa.getColumnIndex("CLIENTE")) != null) && (dadosPessoa.getString(dadosPessoa.getColumnIndex("CLIENTE")).length() > 0)) {
					pessoa.setCliente(dadosPessoa.getString(dadosPessoa.getColumnIndex("CLIENTE")));
				}
				// Checa se retornou algum valor
				if ((dadosPessoa.getString(dadosPessoa.getColumnIndex("PESSOA")) != null) && (!dadosPessoa.getString(dadosPessoa.getColumnIndex("PESSOA")).equals(""))) {
					pessoa.setPessoa(dadosPessoa.getString(dadosPessoa.getColumnIndex("PESSOA")));
				}

				// Instancia a classe de cidade
				CidadeBeans cidade = new CidadeBeans();
				cidade.setDescricao(dadosPessoa.getString(dadosPessoa.getColumnIndex("DESCRICAO_CIDAD")));
				cidade.setIdCidade(dadosPessoa.getInt(dadosPessoa.getColumnIndex("ID_CFACIDAD")));
				// Adiciona a cidade na pessoa
				pessoa.setCidadePessoa(cidade);
				
				// Instancia a classe de estado
				EstadoBeans estado = new EstadoBeans();
				estado.setSiglaEstado(dadosPessoa.getString(dadosPessoa.getColumnIndex("UF")));
				estado.setCodigoEstado(dadosPessoa.getInt(dadosPessoa.getColumnIndex("ID_CFAESTAD")));
				// Adiciona o estado na pessoa
				pessoa.setEstadoPessoa(estado);
				
				//Instancia a classe de status
				StatusBeans status = new StatusBeans();
				status.setIdStatus(dadosPessoa.getInt(dadosPessoa.getColumnIndex("ID_CFASTATU")));
				status.setDescricao(dadosPessoa.getString(dadosPessoa.getColumnIndex("DESCRICAO_STATU")));
				// Checa se tem algum dados
				if ((dadosPessoa.getString(dadosPessoa.getColumnIndex("BLOQUEIA")) != null) && (!dadosPessoa.getString(dadosPessoa.getColumnIndex("BLOQUEIA")).equals(""))) {
					status.setBloqueia(dadosPessoa.getString(dadosPessoa.getColumnIndex("BLOQUEIA")));
				}
				if ((dadosPessoa.getString(dadosPessoa.getColumnIndex("PARCELA_EM_ABERTO")) != null) && (!dadosPessoa.getString(dadosPessoa.getColumnIndex("PARCELA_EM_ABERTO")).equals(""))) {
					status.setParcelaEmAberto(dadosPessoa.getString(dadosPessoa.getColumnIndex("PARCELA_EM_ABERTO")));
				}
				if ((dadosPessoa.getString(dadosPessoa.getColumnIndex("VISTA_PRAZO")) != null) && (!dadosPessoa.getString(dadosPessoa.getColumnIndex("VISTA_PRAZO")).equals(""))) {
					status.setVistaPrazo(dadosPessoa.getString(dadosPessoa.getColumnIndex("VISTA_PRAZO")));
				}
				// Adiciona o status na pessoa
				pessoa.setStatusPessoa(status);
				
				// Instancia a classe de endereco
				EnderecoBeans endereco = new EnderecoBeans();
				if ((dadosPessoa.getString(dadosPessoa.getColumnIndex("TIPO")) != null) && (dadosPessoa.getString(dadosPessoa.getColumnIndex("TIPO")).length() > 0)) {
					endereco.setTipoEndereco(dadosPessoa.getString(dadosPessoa.getColumnIndex("TIPO")));
				}
				endereco.setBairro(dadosPessoa.getString(dadosPessoa.getColumnIndex("BAIRRO")));
				endereco.setCep(dadosPessoa.getString(dadosPessoa.getColumnIndex("CEP")));
				endereco.setLogradouro(dadosPessoa.getString(dadosPessoa.getColumnIndex("LOGRADOURO")));
				endereco.setComplemento(dadosPessoa.getString(dadosPessoa.getColumnIndex("COMPLEMENTO")));
				endereco.setEmail(dadosPessoa.getString(dadosPessoa.getColumnIndex("EMAIL")));
				endereco.setNumero(dadosPessoa.getString(dadosPessoa.getColumnIndex("NUMERO")));
				// Adiciona o endereco na pessoa
				pessoa.setEnderecoPessoa(endereco);
				
				// passa para o proximo registro (pessoa)
				dadosPessoa.moveToNext();

				listaPessoas.add(pessoa);
				
			} // Fim do for
		} else {
			final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			// Cria uma variavem para inserir as propriedades da mensagem
			final ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "Rotinas");
			mensagem.put("mensagem", "Não existe registros cadastrados");
			
			// Executa a mensagem passando por parametro as propriedades
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					funcoes.menssagem(mensagem);
				}
			});

		}


		return listaPessoas;
	} // Fim listaPessoaResumido


	/**
	 *
	 * @param idPessoa
	 * @param tipoPessoa - KEY_TIPO_USUARIO, KEY_TIPO_FONECEDOR, KEY_TIPO_CLIENTE
	 * @return
	 */
	public PessoaBeans pessoaCompleta(String idPessoa, String tipoPessoa){
		// Cria uma lista para armazenar todas as pessoas retornadas do banco
		PessoaBeans dadosPessoaCompleto = new PessoaBeans();
		try{
			String where = "CFACLIFO.ID_CFACLIFO = " + idPessoa;
			
			dadosPessoaCompleto = listaPessoaResumido(where, tipoPessoa, null).get(0);
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			
			String sql = "SELECT CFACLIFO.ID_CFACLIFO, CFACLIFO.CAPITAL_SOCIAL, "
					   + "CFAATIVI.ID_CFAATIVI, CFAATIVI.CODIGO, CFAATIVI.DESCRICAO AS DESCRICAO_ATIVIDADE, CFAATIVI.DESC_ATAC_PRAZO AS DESC_ATAC_PRAZO_ATIVIDADE, "
					   + "CFAATIVI.DESC_ATAC_VISTA AS DESC_ATAC_VISTA_ATIVIDADE, CFAATIVI.DESC_VARE_PRAZO AS DESC_VARE_PRAZO_ATIVIDADE, CFAATIVI.DESC_VARE_VISTA AS DESC_VARE_VISTA_ATIVIDADE, "
					   + "CFAATIVI.DESC_PROMOCAO DESC_PROMOCAO_ATIVIDADE, CFATPCLI.ID_CFATPCLI, CFATPCLI.CODIGO AS CODIGO_TP_CLIENTE, CFATPCLI.DESCRICAO AS DESCRICAO_TP_CLIENTE, "
		     		   + "CFATPCLI.DESC_ATAC_PRAZO AS DESC_ATAC_PRAZO_TP_CLIENTE, CFATPCLI.DESC_ATAC_VISTA AS DESC_ATAC_VISTA_TP_CLIENTE, CFATPCLI.DESC_VARE_PRAZO AS DESC_VARE_PRAZO_TP_CLIENTE, "
					   + "CFATPCLI.DESC_VARE_VISTA AS DESC_VARE_VISTA_TP_CLIENTE, CFATPCLI.DESC_PROMOCAO AS DESC_PROMOCAO_TP_CLIENTE, CFATPCLI.VENDE_ATAC_VAREJO AS VENDE_ATAC_VAREJO_TP_CLIENTE "
					   + "FROM CFACLIFO "
					   + "LEFT OUTER JOIN CFAATIVI ON(CFACLIFO.ID_CFAATIVI = CFAATIVI.ID_CFAATIVI) "
					   + "LEFT OUTER JOIN CFATPCLI ON(CFACLIFO.ID_CFATPCLI = CFATPCLI.ID_CFATPCLI) "
					   + "WHERE (CFACLIFO.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ") AND (CFACLIFO.ID_CFACLIFO = " + idPessoa + ") ";
	
			// Verifica se eh para retornar apenas os clientes
			if(tipoPessoa.equalsIgnoreCase("cliente")){
				sql = sql + " AND (CFACLIFO.CLIENTE = 1) ";
			
			// Verifica se eh para retornar apenas os fornecedores
			} else if(tipoPessoa.equalsIgnoreCase("fornecedor")){
				sql = sql + " AND (CFACLIFO.FORNECEDOR = 1) ";

			// Verifica se eh para retornar apenas os dados do usuario
			} else if (tipoPessoa.equalsIgnoreCase(KEY_TIPO_USUARIO)){
				sql = sql + " AND (CFACLIFO.USUARIO = 1) ";
			}
			
			sql = sql + "ORDER BY CFAATIVI.ID_CFAATIVI, CFATPCLI.ID_CFATPCLI";
			
			PessoaSql pessoaSql = new PessoaSql(context);
			
			Cursor cursor = pessoaSql.sqlSelect(sql);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				dadosPessoaCompleto.setCapitalSocial(cursor.getDouble(cursor.getColumnIndex("CAPITAL_SOCIAL")));
				
				// Cria variavel para salvar os dados da atividade
				RamoAtividadeBeans ramoAtividadeBeans = new RamoAtividadeBeans();
				
				ramoAtividadeBeans.setIdRamoAtividade(cursor.getInt(cursor.getColumnIndexOrThrow("ID_CFAATIVI")));
				ramoAtividadeBeans.setCodigo(cursor.getInt(cursor.getColumnIndex("CODIGO")));
				ramoAtividadeBeans.setDescricaoRamoAtividade(cursor.getString(cursor.getColumnIndex("DESCRICAO_ATIVIDADE")));
				ramoAtividadeBeans.setDescontoAtacadoPrazo(cursor.getDouble(cursor.getColumnIndex("DESC_ATAC_PRAZO_ATIVIDADE")));
				ramoAtividadeBeans.setDescontoAtacadoVista(cursor.getDouble(cursor.getColumnIndex("DESC_ATAC_VISTA_ATIVIDADE")));
				ramoAtividadeBeans.setDescontoVarejoPrazo(cursor.getDouble(cursor.getColumnIndex("DESC_VARE_PRAZO_ATIVIDADE")));
				ramoAtividadeBeans.setDescontoVarejoVista(cursor.getDouble(cursor.getColumnIndex("DESC_VARE_VISTA_ATIVIDADE")));
				if( (cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_ATIVIDADE")) != null) && (!cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_ATIVIDADE")).equals("")) ){
					ramoAtividadeBeans.setDescontoPromocao(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_ATIVIDADE")).charAt(0));
				}
				
				dadosPessoaCompleto.setRamoAtividade(ramoAtividadeBeans);
				
				TipoClienteBeans tipoClienteBeans = new TipoClienteBeans();
				
				tipoClienteBeans.setIdTipoCliente(cursor.getInt(cursor.getColumnIndex("ID_CFATPCLI")));
				tipoClienteBeans.setCodigoTipoCliente(cursor.getInt(cursor.getColumnIndex("CODIGO_TP_CLIENTE")));
				tipoClienteBeans.setDescricaoTipoCliente(cursor.getString(cursor.getColumnIndex("DESCRICAO_TP_CLIENTE")));
				tipoClienteBeans.setDescontoAtacadoPrazo(cursor.getDouble(cursor.getColumnIndex("DESC_ATAC_PRAZO_TP_CLIENTE")));
				tipoClienteBeans.setDescontoAtacadoVista(cursor.getDouble(cursor.getColumnIndex("DESC_ATAC_VISTA_TP_CLIENTE")));
				tipoClienteBeans.setDescontoVarejoPrazo(cursor.getDouble(cursor.getColumnIndex("DESC_VARE_PRAZO_TP_CLIENTE")));
				tipoClienteBeans.setDescontoVarejoVista(cursor.getDouble(cursor.getColumnIndex("DESC_VARE_VISTA_TP_CLIENTE")));
				if(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_TP_CLIENTE")) != null){
					tipoClienteBeans.setDescontoPromocao(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_TP_CLIENTE")));
				}
				if(cursor.getString(cursor.getColumnIndex("VENDE_ATAC_VAREJO_TP_CLIENTE")) != null){
					tipoClienteBeans.setVendeAtacadoVarejo(cursor.getString(cursor.getColumnIndex("VENDE_ATAC_VAREJO_TP_CLIENTE")));
				}
				
				dadosPessoaCompleto.setTipoClientePessoa(tipoClienteBeans);
				
			}

			TelefoneRotinas telefoneRotinas = new TelefoneRotinas(context);
			// Pega a lista de telefones da pessoa
			List<TelefoneBeans> listaTele = telefoneRotinas.listaTelefone("CFAFONES.ID_CFACLIFO = " + idPessoa);
			// Checa se retornou a lista de telefone
			if (listaTele != null && listaTele.size() > 0) {
				// Preenche os dados de telefone da pessoa
				dadosPessoaCompleto.setListaTelefone(listaTele);
				// Limpa a lista
				listaTele.clear();
			}
			sql = "SELECT CFAPARAM.ID_CFACLIFO, CFAPARAM.ID_SMAEMPRE, CFAPARAM.LIMITE, CFAPARAM.DESC_ATAC_VISTA AS DESC_ATAC_VISTA_PARAM, CFAPARAM.DESC_ATAC_PRAZO AS DESC_ATAC_PRAZO_PARAM, "
				+ "CFAPARAM.DESC_VARE_VISTA AS DESC_VARE_VISTA_PARAM, CFAPARAM.DESC_VARE_PRAZO AS DESC_VARE_PRAZO_PARAM, CFAPARAM.DT_ULT_VISITA AS DT_ULT_VISITA_PARAM, "
				+ "CFATPDOC.ID_CFATPDOC, CFATPDOC.CODIGO AS CODIGO_TPDOC, CFATPDOC.DESCRICAO AS DESCRICAO_TPDOC, CFATPDOC.SIGLA AS SIGLA_TPDOC, CFATPDOC.TIPO AS TIPO_TPDOC, "
				+ "CFAPORTA.ID_CFAPORTA, CFAPORTA.CODIGO AS CODIGO_PORTA, CFAPORTA.DG, CFAPORTA.DESCRICAO AS DESCRICAO_PORTA, CFAPORTA.SIGLA AS SIGLA_PORTA, CFAPORTA.TIPO AS TIPO_PORTA, "
				+ "AEAPLPGT.ID_AEAPLPGT, AEAPLPGT.CODIGO AS CODIGO_PLPGT, AEAPLPGT.ID_SMAEMPRE AS ID_SMAEMPRE_PLPGT, AEAPLPGT.DESCRICAO AS DESCRICAO_PLPGT, AEAPLPGT.ATAC_VAREJO, AEAPLPGT.ATIVO AS ATIVO_PLPGT, "
				+ "AEAPLPGT.DESC_PROMOCAO DESC_PROMOCAO_PLPGT, AEAPLPGT.JURO_MEDIO_ATAC, AEAPLPGT.JURO_MEDIO_VARE, AEAPLPGT.PERC_DESC_ATAC, AEAPLPGT.PERC_DESC_VARE, AEAPLPGT.VISTA_PRAZO AS VISTA_PRAZO_PLPGT "
				+ "FROM CFAPARAM "
				+ "LEFT OUTER JOIN CFATPDOC ON((CFAPARAM.ID_CFATPDOC = CFATPDOC.ID_CFATPDOC) AND (CFATPDOC.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")) "
				+ "LEFT OUTER JOIN CFAPORTA ON(CFAPARAM.ID_CFAPORTA = CFAPORTA.ID_CFAPORTA) "
				+ "LEFT OUTER JOIN AEAPLPGT ON((CFAPARAM.ID_AEAPLPGT = AEAPLPGT.ID_AEAPLPGT) AND (AEAPLPGT.ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa") + ")) "
				+ "WHERE (CFAPARAM.ID_SMAEMPRE =  " + funcoes.getValorXml("CodigoEmpresa") + ") AND (CFAPARAM.ID_CFACLIFO = " + idPessoa + ") "
				+ "ORDER BY CFAPARAM.ID_CFACLIFO";
			
			cursor = pessoaSql.sqlSelect(sql);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				dadosPessoaCompleto.setLimiteCompra(cursor.getDouble(cursor.getColumnIndex("LIMITE")));
				dadosPessoaCompleto.setDescontoAtacadoVista(cursor.getDouble(cursor.getColumnIndex("DESC_ATAC_VISTA_PARAM")));
				dadosPessoaCompleto.setDescontoAtacadoPrazo(cursor.getDouble(cursor.getColumnIndex("DESC_ATAC_PRAZO_PARAM")));
				dadosPessoaCompleto.setDescontoVarejoVista(cursor.getDouble(cursor.getColumnIndex("DESC_VARE_VISTA_PARAM")));
				dadosPessoaCompleto.setDescontoVarejoPrazo(cursor.getDouble(cursor.getColumnIndex("DESC_VARE_PRAZO_PARAM")));
				dadosPessoaCompleto.setDataUltimaVisita(funcoes.formataDataHora(cursor.getString(cursor.getColumnIndex("DT_ULT_VISITA_PARAM"))));
				
				
				// Cria variavel para salvar os dados do tipo de documento
				TipoDocumentoBeans tipoDocumentoBeans = new TipoDocumentoBeans();
				tipoDocumentoBeans.setIdTipoDocumento(cursor.getInt(cursor.getColumnIndex("ID_CFATPDOC")));
				tipoDocumentoBeans.setIdEmpresa(cursor.getInt(cursor.getColumnIndex("ID_SMAEMPRE")));
				tipoDocumentoBeans.setCodigoTipoDocumento(cursor.getInt(cursor.getColumnIndex("CODIGO_TPDOC")));
				tipoDocumentoBeans.setDescricaoTipoDocumento(cursor.getString(cursor.getColumnIndex("DESCRICAO_TPDOC")));
				tipoDocumentoBeans.setSiglaTipoDocumento(cursor.getString(cursor.getColumnIndex("SIGLA_TPDOC")));
				
				// Adiciona aos dados do cliente
				dadosPessoaCompleto.setTipoDocumentoPessoa(tipoDocumentoBeans);
				
				// Cria variavel para salvar os dados do portador
				PortadorBancoBeans portadorBancoBeans = new PortadorBancoBeans();
				portadorBancoBeans.setIdPortadorBanco(cursor.getInt(cursor.getColumnIndex("ID_CFAPORTA")));
				portadorBancoBeans.setCodigoPortadorBanco(cursor.getInt(cursor.getColumnIndex("CODIGO_PORTA")));
				if(cursor.getString(cursor.getColumnIndex("DG")) != null){
					portadorBancoBeans.setDigitoPortador(cursor.getString(cursor.getColumnIndex("DG")));
				}
				portadorBancoBeans.setDescricaoPortador(cursor.getString(cursor.getColumnIndex("DESCRICAO_PORTA")));
				portadorBancoBeans.setSiglaPortador(cursor.getString(cursor.getColumnIndex("SIGLA_PORTA")));
				if(cursor.getString(cursor.getColumnIndex("TIPO_PORTA")) != null){
					portadorBancoBeans.setTipo(cursor.getString(cursor.getColumnIndex("TIPO_PORTA")));
				}
				
				// Adiciona aos dados do cliente
				dadosPessoaCompleto.setPortadorBancoPessoa(portadorBancoBeans);
				
				// Cria variavel para salvar os dados do portador
				PlanoPagamentoBeans planoPagamentoBeans = new PlanoPagamentoBeans();
				planoPagamentoBeans.setIdPlanoPagamento(cursor.getInt(cursor.getColumnIndex("ID_AEAPLPGT")));
				planoPagamentoBeans.setIdEmpresa(cursor.getInt(cursor.getColumnIndex("ID_SMAEMPRE_PLPGT")));
				planoPagamentoBeans.setCodigoPlanoPagamento(cursor.getInt(cursor.getColumnIndex("CODIGO_PLPGT")));
				planoPagamentoBeans.setDescricaoPlanoPagamento(cursor.getString(cursor.getColumnIndex("DESCRICAO_PLPGT")));
				planoPagamentoBeans.setAtacadoVarejo(cursor.getString(cursor.getColumnIndex("ATAC_VAREJO")));
				planoPagamentoBeans.setAtivo(cursor.getString(cursor.getColumnIndex("ATIVO_PLPGT")));
				if(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_PLPGT")) != null){
					planoPagamentoBeans.setDescontoPromocao(cursor.getString(cursor.getColumnIndex("DESC_PROMOCAO_PLPGT")));
				}
				planoPagamentoBeans.setJurosAtacado(cursor.getDouble(cursor.getColumnIndex("JURO_MEDIO_ATAC")));
				planoPagamentoBeans.setJurosVarejo(cursor.getDouble(cursor.getColumnIndex("JURO_MEDIO_VARE")));
				planoPagamentoBeans.setDescontoAtacado(cursor.getDouble(cursor.getColumnIndex("PERC_DESC_ATAC")));
				planoPagamentoBeans.setDescontoVarejo(cursor.getDouble(cursor.getColumnIndex("PERC_DESC_VARE")));
				planoPagamentoBeans.setVistaPrazo(cursor.getString(cursor.getColumnIndex("VISTA_PRAZO_PLPGT")));
				
				// Adiciona aos dados do cliente
				dadosPessoaCompleto.setPlanoPagamentoPessoa(planoPagamentoBeans);
				
			}

			sql = "SELECT SUM(RPAPARCE.FC_VL_RESTANTE) AS TOTAL_CREDITO FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = " + idPessoa + ") AND (RPAPARCE.TIPO = 1) "
				+ " AND (RPAPARCE.DT_BAIXA IS NULL)";
			
			cursor = pessoaSql.sqlSelect(sql);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				dadosPessoaCompleto.setCreditoAcumulado(cursor.getDouble(cursor.getColumnIndex("TOTAL_CREDITO")));
			}
			
			// Pega todos os titulos a receber em aberto
			sql = "SELECT SUM(RPAPARCE.FC_VL_RESTANTE) AS TOTAL_A_RECEBER FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = " + idPessoa + ") AND (RPAPARCE.TIPO = 0) "
				+ " AND (RPAPARCE.DT_BAIXA IS NULL)";
			
			cursor = pessoaSql.sqlSelect(sql);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				dadosPessoaCompleto.setTotalAPagar(cursor.getDouble(cursor.getColumnIndex("TOTAL_A_RECEBER")));
			}

			sql = "SELECT SUM(RPAPARCE.FC_VL_TOTAL_PAGO) AS TOTAL_PAGO FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = " + idPessoa + ") AND (RPAPARCE.TIPO = 0) "
				+ " AND (RPAPARCE.DT_BAIXA IS NOT NULL)";
			
			cursor = pessoaSql.sqlSelect(sql);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				dadosPessoaCompleto.setTotalPago(cursor.getDouble(cursor.getColumnIndex("TOTAL_PAGO")));
			}
			
			// Pega todos os titulos a receber vencidos
			sql = "SELECT SUM(RPAPARCE.FC_VL_RESTANTE) AS TOTAL_VENCIDO FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = " + idPessoa + ") AND (RPAPARCE.TIPO = 0) "
				+ " AND (RPAPARCE.DT_BAIXA IS NULL) AND (RPAPARCE.DT_VENCIMENTO < date('now', 'localtime'))";
			
			cursor = pessoaSql.sqlSelect(sql);
			
			// Se o cursor tiver algum valor entra no laco
			if( (cursor != null) && (cursor.getCount() > 0) ){
				// Move para o primeiro registro
				cursor.moveToFirst();
				
				dadosPessoaCompleto.setTotalVencido(cursor.getDouble(cursor.getColumnIndex("TOTAL_VENCIDO")));
			}
			
		}catch(Exception e){
			
			// Cria uma variavem para inserir as propriedades da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "PessoaRotinas");
			mensagem.put("mensagem", "Os dados da pessoa esta incompleto. \n" + e.getMessage());
			
			// Executa a mensagem passando por parametro as propriedades
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			funcoes.menssagem(mensagem);
		}
		
		return dadosPessoaCompleto;
	} // Fim pessoaCompleta


	public List<PessoaBeans> listaPessoaCompleta(String tipoPessoa, String where){
		// Lista dos dados completo da pessoa
		List<PessoaBeans> listaPessoa = new ArrayList<PessoaBeans>();

		String sql = "SELECT CFACLIFO.ID_CFACLIFO FROM CFACLIFO ";

		// Verifica se eh para retornar apenas os clientes
		if(tipoPessoa.equalsIgnoreCase(KEY_TIPO_CLIENTE)){
			sql = sql + "WHERE (CFACLIFO.CLIENTE = 1) ";

			// Verifica se eh para retornar apenas os fornecedores
		} else if(tipoPessoa.equalsIgnoreCase(KEY_TIPO_FONECEDOR)){
			sql = sql + " WHERE (CFACLIFO.FORNECEDOR = 1) ";

			// Retorna todos os registro, nao importando se eh cliente ou nao
		} else if (tipoPessoa.equalsIgnoreCase(KEY_TIPO_USUARIO)){
			sql = sql + " WHERE (CFACLIFO.USUARIO = 1) ";
		}

		if (where != null && where.length() > 1){
			sql += " AND ( " + where + ")";
		}

		PessoaSql pessoaSql = new PessoaSql(context);
		// Executa o sql
		Cursor dadosCliente = pessoaSql.sqlSelect(sql);

		// Checa se retornou alguma coisa do banco de dados
		if(dadosCliente != null && dadosCliente.getCount() > 0){
			// Move o cursor para o primeiro registro
			dadosCliente.moveToFirst();

			// Passa por todos os registro
			for (int i = 0; i < dadosCliente.getCount(); i++) {
				// Adiciona a pessoa a lista
				listaPessoa.add(pessoaCompleta(dadosCliente.getString(dadosCliente.getColumnIndex("ID_CFACLIFO")), tipoPessoa));
			}
		}
		return listaPessoa;
	} // Fim listaPessoaCompleta
	
	
	public String emailPessoa(String idPessoa){
		String sql = "SELECT CFAENDER.EMAIL FROM CFACLIFO "
				   + "LEFT OUTER JOIN CFAENDER ON (CFAENDER.ID_CFAENDER = (SELECT CFAENDER.ID_CFAENDER FROM CFAENDER WHERE (CFAENDER.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) LIMIT 1)) "
				   + "WHERE CFACLIFO.ID_CFACLIFO = " + idPessoa;
		
		// Instancia a classe para manipular o banco de dados
		PessoaSql clienteSql = new PessoaSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = clienteSql.sqlSelect(sql);
		
		String email = "";
		
		if((cursor != null) && (cursor.getCount() > 0)){
			cursor.moveToFirst();
			
			email = cursor.getString(cursor.getColumnIndex("EMAIL"));
		}
		return email;
	} // fIM emailPessoa


	public double quantidadeCadastroPessoaNovo(){
		double qtd = 0;
		String sql = "SELECT COUNT(*) AS QTD FROM CFACLIFO WHERE (CFACLIFO.STATUS_CADASTRO_NOVO = 'N') AND (CFACLIFO.ID_CFACLIFO < 0)";

		// Instancia a classe para manipular o banco de dados
		PessoaSql clienteSql = new PessoaSql(context);
		// Executa a funcao para retornar os registro do banco de dados
		Cursor cursor = clienteSql.sqlSelect(sql);

		if ((cursor != null) && (cursor.getCount() > 0)){
			// Move o cursor para o primeiro registro
			cursor.moveToFirst();

			qtd = cursor.getDouble(cursor.getColumnIndex("QTD"));
		}
		return qtd;
	} // Fim quantidadeCadastroPessoaNovo
}

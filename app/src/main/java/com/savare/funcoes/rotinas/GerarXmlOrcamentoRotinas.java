package com.savare.funcoes.rotinas;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;

import com.savare.funcoes.FuncoesPersonalizadas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GerarXmlOrcamentoRotinas {
	
	private Context context;
	private String idOrcamento;
	
	public GerarXmlOrcamentoRotinas(Context context) {
		this.context = context;
	}
	
	/**
	 * @return the idOrcamento
	 */
	public String getIdOrcamento() {
		return idOrcamento;
	}

	/**
	 * @param idOrcamento the idOrcamento to set
	 */
	public void setIdOrcamento(String idOrcamento) {
		this.idOrcamento = idOrcamento;
	}


	public String criarArquivoXml(){
		String localArquivoXml = "";
		// Instancia formato de classe
		DateFormat dataFormatada = new SimpleDateFormat("ddMMyyyy_HHmmss_SSSS");
		// Instancia com a data atual
    	Date data = new Date();
    	
		File pasta = new File(Environment.getExternalStorageDirectory() + "/SAVARE/XML");
		File arquivoXml;
	    try {
	    	// Checa se a pasta xml existe
	    	if (!pasta.exists()) {
	    	    pasta.mkdirs();
	    	}
	    	//String dataHoraSemSimbolos = dataFormatada.format(data);
	    	
	    	arquivoXml = new File(pasta, "/" + dataFormatada.format(data) + ".xml");
	    	
	    	Writer saida = new OutputStreamWriter(new FileOutputStream(arquivoXml));
	    	
	    	//XMLOutputter saidaXml = new XMLOutputter();
	    	
	    	//saidaXml.setFormat(Format.getCompactFormat().setEncoding("ISO-8859-1"));
	    	
	    	//saidaXml.output(gerarXml(), saida);
	    	
	    	if(arquivoXml.exists()){
	    		localArquivoXml = arquivoXml.getPath();
	    	}
	    
	    	/*}catch (ParserConfigurationException e) {
            e.printStackTrace();
        
	    } catch (TransformerException e) {
            e.printStackTrace();*/
	    }catch(Exception e){
	    	
	    	ContentValues dadosMensagem = new ContentValues();
			
	    	FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
	    	
	    	dadosMensagem.put("comando", 0);
			dadosMensagem.put("tela", "GerarXmlOrcamentoRotinas");
			dadosMensagem.put("mensagem", "Não foi possível criar o arquivo XML. \n" + e.getMessage());
			dadosMensagem.put("dados", e.toString());
			dadosMensagem.put("usuario", funcoes.getValorXml("Usuario"));
			dadosMensagem.put("empresa", funcoes.getValorXml("Empresa"));
			dadosMensagem.put("email", funcoes.getValorXml("Email"));
			
			funcoes.menssagem(dadosMensagem);
	    }
		return localArquivoXml;
	}
	
	
	/*private Document gerarXml(){
		OrcamentoSql orcamentoSql = new OrcamentoSql(context);
		
		Cursor dadosOrcamento = orcamentoSql.query("AEAORCAM.ID_AEAORCAM = " + idOrcamento);
		// Cria o elemento(tag) raiz
		Element tagOrcamentoProc = new Element("orcamentoProc");
		
		if(dadosOrcamento != null){
			// Move o cursor para o primeiro registro
			dadosOrcamento.moveToFirst();
			
			// Cria a tag(elemento) que vai ficar dentro da raiz (orcamentoProc)
			Element tagOrcamento = new Element("dadosOrcamento");
			// Cria a tag que vai dentro do dadosOrcamento
			Element tagIdentificacaoOrcamento = new Element("identificacaoOrcamento");
			
			// Cria os elementos que vai entro de identificacaoOrcamento
			Element idOrcam = new Element("idOrcam");
			// Inseri os dados entro do elemento
			idOrcam.setText(String.valueOf(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ID_AEAORCAM"))));
			// Adiciona o elemento a tag Pai
			tagIdentificacaoOrcamento.addContent(idOrcam);
			
			Element idEmpre = new Element("idEmpre");
			idEmpre.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ID_SMAEMPRE")));
			tagIdentificacaoOrcamento.addContent(idEmpre);
			
			Element idClifo = new Element("idClifo");
			idClifo.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ID_CFACLIFO")));
			tagIdentificacaoOrcamento.addContent(idClifo);
			
			Element idEstad = new Element("idEstad");
			idEstad.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ID_CFAESTAD")));
			tagIdentificacaoOrcamento.addContent(idEstad);
			
			Element idCidad = new Element("idCidad");
			idCidad.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ID_CFACIDAD")));
			tagIdentificacaoOrcamento.addContent(idCidad);
			
			Element idTpDoc = new Element("idTpDoc");
			idTpDoc.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ID_CFATPDOC")));
			tagIdentificacaoOrcamento.addContent(idTpDoc);
			
			Element guid = new Element("guid");
			guid.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("GUID")));
			tagIdentificacaoOrcamento.addContent(guid);
			
			Element dtCad = new Element("dtCad");
			dtCad.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("DT_CAD")));
			tagIdentificacaoOrcamento.addContent(dtCad);
			
			Element dtAlt = new Element("dtAlt");
			dtAlt.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("DT_ALT")));
			tagIdentificacaoOrcamento.addContent(dtAlt);
			
			Element vlMercBruto = new Element("vlMercBruto");
			vlMercBruto.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("VL_MERC_BRUTO")));
			tagIdentificacaoOrcamento.addContent(vlMercBruto);
			
			Element vlMercDesconto = new Element("vlMercDesconto");
			vlMercDesconto.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("VL_MERC_DESCONTO")));
			tagIdentificacaoOrcamento.addContent(vlMercDesconto);
			
			Element vlFrete = new Element("vlFrete");
			vlFrete.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("VL_FRETE")));
			tagIdentificacaoOrcamento.addContent(vlFrete);
			
			Element vlSeguro = new Element("vlSeguro");
			vlSeguro.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("VL_SEGURO")));
			tagIdentificacaoOrcamento.addContent(vlSeguro);
			
			Element vlOutros = new Element("vlOutros");
			vlOutros.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("VL_OUTROS")));
			tagIdentificacaoOrcamento.addContent(vlOutros);
			
			Element vlEncargosFinanceiros = new Element("vlEncargosFinanceiros");
			vlEncargosFinanceiros.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("VL_ENCARGOS_FINANCEIROS")));
			tagIdentificacaoOrcamento.addContent(vlEncargosFinanceiros);
			
			Element vlTotal = new Element("vlTotal");
			vlTotal.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("FC_VL_TOTAL")));
			tagIdentificacaoOrcamento.addContent(vlTotal);
			
			Element atacVarejo = new Element("atacVarejo");
			atacVarejo.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ATAC_VAREJO")));
			tagIdentificacaoOrcamento.addContent(atacVarejo);
			
			Element pessoaCliente = new Element("pessoaCliente");
			pessoaCliente.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("PESSOA_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(pessoaCliente);
			
			Element nomeCliente = new Element("nomeCliente");
			nomeCliente.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("NOME_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(nomeCliente);
			
			Element ieRg = new Element("ieRg");
			ieRg.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("IE_RG_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(ieRg);
			
			Element cpfCGC = new Element("cpfCGC");
			cpfCGC.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("CPF_CGC_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(cpfCGC);
			
			Element enderecoCliente = new Element("enderecoCliente");
			enderecoCliente.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ENDERECO_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(enderecoCliente);
			
			Element bairroCliente = new Element("bairroCliente");
			bairroCliente.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("BAIRRO_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(bairroCliente);
			
			Element cepCliente = new Element("cepCliente");
			cepCliente.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("CEP_CLIENTE")));
			tagIdentificacaoOrcamento.addContent(cepCliente);
			
			Element obs = new Element("obs");
			obs.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("OBS")));
			tagIdentificacaoOrcamento.addContent(obs);
			
			Element status = new Element("status");
			status.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("STATUS")));
			tagIdentificacaoOrcamento.addContent(status);
			
			Element tipoEntrega = new Element("tipoEntrega");
			tipoEntrega.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("TIPO_ENTREGA")));
			tagIdentificacaoOrcamento.addContent(tipoEntrega);
			
			tagOrcamento.addContent(tagIdentificacaoOrcamento);
			
			
			// Cria a tag que vai dentro do dadosOrcamento
			Element tagLocalPedido = new Element("localPedido");
			
			// Cria os elementos que vai entro de localPedido
			Element latitude = new Element("latitude");
			// Inseri os dados entro do elemento
			latitude.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("LATITUDE")));
			// Adiciona o elemento a tag Pai
			tagLocalPedido.addContent(latitude);
			
			// Cria os elementos que vai entro de localPedido
			Element longitude = new Element("longitude");
			// Inseri os dados entro do elemento
			longitude.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("LONGITUDE")));
			// Adiciona o elemento a tag Pai
			tagLocalPedido.addContent(longitude);
			
			// Cria os elementos que vai entro de localPedido
			Element altitude = new Element("altitude");
			// Inseri os dados entro do elemento
			altitude.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("ALTITUDE")));
			// Adiciona o elemento a tag Pai
			tagLocalPedido.addContent(altitude);
			
			// Cria os elementos que vai entro de localPedido
			Element horarioLocal = new Element("horarioLocalizacao");
			// Inseri os dados entro do elemento
			horarioLocal.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("HORARIO_LOCALIZACAO")));
			// Adiciona o elemento a tag Pai
			tagLocalPedido.addContent(horarioLocal);
			
			// Cria os elementos que vai entro de localPedido
			Element tipoLocalizacao = new Element("tipoLocalizacao");
			// Inseri os dados entro do elemento
			tipoLocalizacao.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("TIPO_LOCALIZACAO")));
			// Adiciona o elemento a tag Pai
			tagLocalPedido.addContent(tipoLocalizacao);
			
			// Cria os elementos que vai entro de localPedido
			Element precisao = new Element("precisao");
			// Inseri os dados entro do elemento
			precisao.setText(dadosOrcamento.getString(dadosOrcamento.getColumnIndex("PRECISAO")));
			// Adiciona o elemento a tag Pai
			tagLocalPedido.addContent(precisao);
			
			tagOrcamento.addContent(tagLocalPedido);
			
			
			ItemOrcamentoSql itemOrcamentoSql = new ItemOrcamentoSql(context);
			
			Cursor listaItemOrcamento = itemOrcamentoSql.query("AEAITORC.ID_AEAORCAM = " + idOrcamento, "AEAITORC.SEQUENCIA, AEAITORC.DT_CAD");
			
			// Verifica se retornou algum registro do banco
			if(listaItemOrcamento != null){
				// Move o cursor para o primeiro registro
				listaItemOrcamento.moveToFirst();
				
				// Passa por todos os registro
				for (int i = 0; i < listaItemOrcamento.getCount(); i++) {
					// Cria tab para salvar a lista dos produtos que tem no orcamento
					Element tagListaItemOrcamento = new Element("itemOrcamento");
					// Adiciona um atributo a tag Lista de Item do Orcamento
					tagListaItemOrcamento.setAttribute("sequencia", String.valueOf(i+1));
					
					Element idOrcamItem = new Element("idOrcamItem");
					idOrcamItem.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("ID_AEAORCAM")));
					tagListaItemOrcamento.addContent(idOrcamItem);
					
					Element idEstoq = new Element("idEstoq");
					idEstoq.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("ID_AEAESTOQ")));
					tagListaItemOrcamento.addContent(idEstoq);
					
					Element idProdu = new Element("idProdu");
					idProdu.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("ID_AEAPRODU")));
					tagListaItemOrcamento.addContent(idProdu);
					
					Element idPlPgt = new Element("idPlPgt");
					idPlPgt.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("ID_AEAPLPGT")));
					tagListaItemOrcamento.addContent(idPlPgt);
					
					Element idUnVen = new Element("idUnVen");
					idUnVen.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("ID_AEAUNVEN")));
					tagListaItemOrcamento.addContent(idUnVen);
					
					Element idClifoVendedorItem = new Element("idClifoVendedorItem");
					idClifoVendedorItem.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("ID_CFACLIFO_VENDEDOR")));
					tagListaItemOrcamento.addContent(idClifoVendedorItem);
					
					Element guidItem = new Element("guidItem");
					guidItem.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("GUID")));
					tagListaItemOrcamento.addContent(guidItem);
					
					Element dtCadItem = new Element("dtCadItem");
					dtCadItem.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("DT_CAD")));
					tagListaItemOrcamento.addContent(dtCadItem);
					
					Element quantidade = new Element("quantidade");
					quantidade.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("QUANTIDADE")));
					tagListaItemOrcamento.addContent(quantidade);
					
					Element vlTabela = new Element("vlTabela");
					vlTabela.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("VL_TABELA")));
					tagListaItemOrcamento.addContent(vlTabela);
					
					Element vlBruto = new Element("vlBruto");
					vlBruto.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("VL_BRUTO")));
					tagListaItemOrcamento.addContent(vlBruto);
					
					Element vlDeconto = new Element("vlDeconto");
					vlDeconto.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("VL_DESCONTO")));
					tagListaItemOrcamento.addContent(vlDeconto);
					
					Element totalLiquido = new Element("totalLiquido");
					totalLiquido.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("FC_LIQUIDO")));
					tagListaItemOrcamento.addContent(totalLiquido);
					
					Element complemento = new Element("complemento");
					complemento.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("COMPLEMENTO")));
					tagListaItemOrcamento.addContent(complemento);
					
					Element seqDesconto = new Element("seqDesconto");
					seqDesconto.setText(listaItemOrcamento.getString(listaItemOrcamento.getColumnIndex("SEQ_DESCONTO")));
					tagListaItemOrcamento.addContent(seqDesconto);
					
					tagOrcamento.addContent(tagListaItemOrcamento);
					
					listaItemOrcamento.moveToNext();
				} // Fim do for
			}
			
			tagOrcamentoProc.addContent(tagOrcamento);
			
			Element tagIdentificacaoEmpresa = new Element("identificacaoEmpresa");
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			
			Element chaveEmpresa = new Element("chaveEmpresa");
			chaveEmpresa.setText(funcoes.getValorXml("ChaveEmpresa"));
			tagIdentificacaoEmpresa.addContent(chaveEmpresa);
			
			Element codigoEmpresa = new Element("codigoEmpresa");
			codigoEmpresa.setText(funcoes.getValorXml("CodigoEmpresa"));
			tagIdentificacaoEmpresa.addContent(codigoEmpresa);
			
			Element usuario = new Element("usuario");
			usuario.setText(funcoes.getValorXml("Usuario"));
			tagIdentificacaoEmpresa.addContent(usuario);
			
			Element codigoUsuario = new Element("codigoUsuario");
			codigoUsuario.setText(funcoes.getValorXml("CodigoUsuario"));
			tagIdentificacaoEmpresa.addContent(codigoUsuario);
			
			tagOrcamentoProc.addContent(tagIdentificacaoEmpresa);
			
		}
		
		//Criamos um objeto Document
	    Document doc = new Document();
	    // Adiciona a tag raiz(root) no documento
	    doc.setRootElement(tagOrcamentoProc);
	    
	    return doc;
	}*/
}

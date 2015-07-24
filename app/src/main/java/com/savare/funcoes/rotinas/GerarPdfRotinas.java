package com.savare.funcoes.rotinas;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.savare.beans.EmpresaBeans;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;

public class GerarPdfRotinas {

	//private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
	//private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
	//private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	private Context context;
	private OrcamentoBeans orcamento;
	private List<ItemOrcamentoBeans> listaItensOrcamento;
	
	/**
	 * Construtor padrao.
	 * @param context
	 */
	public GerarPdfRotinas(Context context) {
		this.context = context;
	}
	
	
	public OrcamentoBeans getOrcamento() {
		return orcamento;
	}

	public void setOrcamento(OrcamentoBeans orcamento) {
		this.orcamento = orcamento;
	}

	public List<ItemOrcamentoBeans> getListaItensOrcamento() {
		return listaItensOrcamento;
	}

	public void setListaItensOrcamento(List<ItemOrcamentoBeans> listaItensOrcamento) {
		this.listaItensOrcamento = listaItensOrcamento;
	}



	public String criaArquivoPdf(){
		String localDocumento = "";
		
		Document documento = new Document();
		// Cria o documento tamanho A4
		documento = new Document(PageSize.A4);
		
		//document.addTitle("Orcamento/Pedido PDF");
		//document.addSubject("Documento feito pela app SAVARE");
		//document.addKeywords("Java, PDF, iText");
		//document.addAuthor(funcoes.getValorXml("CodigoUsuario") + " - " + funcoes.getValorXml("Usuario"));
		//document.addCreator("Lars Vogel");
		
		// Cria um formato de data
		DateFormat dataFormatada = new SimpleDateFormat("MM_yyyy");
		// Instancia com a data atual
		Date data = new Date();
	    		
		File camihnoDocumento;
		File pasta = new File(Environment.getExternalStorageDirectory() + "/SAVARE/PDF/" + dataFormatada.format(data));
	    try {
	    	
	    	if (!pasta.exists()) {
	    	    pasta.mkdirs();
	    	}
	          camihnoDocumento = new File(pasta, "/"+orcamento.getIdOrcamento() + "_" + orcamento.getNomeRazao().replace("/", "_").replace("[^a-zA-Z0-9 -]", "_").replace("-", "").replace(" ", "_") + ".pdf");
	          //FileOutputStream fos = new FileOutputStream(camihnoDocumento);
	          // associa a stream de saida ao
	          PdfWriter.getInstance(documento, new FileOutputStream(camihnoDocumento));
	          // Abre o documento
	          documento.open();
	          
	          documento.add(createTable(0));
	          
	          // Adiciona linhas em branco
	          ListItem linhasBranco = new ListItem(" ");
	          linhasBranco.add(" ");
	          documento.add(linhasBranco);
	          
	          // Adiciona os dados do orcamento
	          documento.add(createTable(2));
	          
	          // Adiciona linhas em branco
	          documento.add(linhasBranco);
	          
	          if(orcamento.getObservacao() != null){
		          // Adiciona a observacao
		          documento.add(createTable(4));
		          
		          // Adiciona linhas em branco
		          documento.add(linhasBranco);
	          }
	          
	          // Adiciona a lista de produtos
	          documento.add(createTable(1));
	          documento.add(createTable(3));
	          
	          // Fecha o documento
	          documento.close();
	          
	          if(camihnoDocumento.exists()){
	        	  localDocumento = camihnoDocumento.toString();
	          }
	      }catch (Exception e){
	    	  String s = e.getMessage();
	    	  s.length();
	      }
	      
	    return localDocumento;
	} // Fim criaArquivoPdf
	
	
	/**
	 * 
	 * @param tipoTabela - 0 = Dados da empresa e vendedor, 1 = Dados dos Produtos, 2 = Dados do Orcamento,
	 * 3 = Total Orcamento, 4 = Observacao
	 * @return
	 * @throws BadElementException
	 */
	 private PdfPTable createTable(int tipoTabela) throws BadElementException {
		 
		PdfPTable tabela = null;
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		//Dados do produtos
		 if(tipoTabela == 1){
			tabela = new PdfPTable(new float[] { 0.08f, 0.42f, 0.15f, 0.1f, 0.2f });
			// Percentagem da largura da pagina
			tabela.setWidthPercentage(100);
			// Muda a cor do fundo do cabecalho
			//tabela.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
	
	 	    PdfPCell c1 = new PdfPCell(new Phrase("Código", smallBold));
	 	    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
	 	    c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
	 	    tabela.addCell(c1);
	 	    
	 	    c1 = new PdfPCell(new Phrase("Descrição Produto", smallBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
	 	    c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
		    tabela.addCell(c1);
	
	 	    c1 = new PdfPCell(new Phrase("Qtde.", smallBold));
	 	    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
	 	    c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
	 	    tabela.addCell(c1);
	
	 	    c1 = new PdfPCell(new Phrase("Pr. de Venda", smallBold));
	 	    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
	 	    c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
	 	    tabela.addCell(c1);
	 	    
	 	    c1 = new PdfPCell(new Phrase("Total", smallBold));
		    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
	 	    c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
		    tabela.addCell(c1);
		    tabela.setHeaderRows(1);
	
	 	    for (int i = 0; i < listaItensOrcamento.size(); i++) {
	 	    	tabela.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
	 	    	tabela.addCell(listaItensOrcamento.get(i).getProduto().getCodigoEstrutural());
	 	    	tabela.addCell(listaItensOrcamento.get(i).getProduto().getDescricaoProduto() + "\n" + listaItensOrcamento.get(i).getComplemento());
	 	    	tabela.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
	 	    	tabela.addCell(funcoes.arredondarValor(listaItensOrcamento.get(i).getQuantidade()));
	 	    	tabela.addCell(funcoes.arredondarValor(listaItensOrcamento.get(i).getValorLiquidoUnitario()));
	 	    	tabela.addCell(funcoes.arredondarValor(listaItensOrcamento.get(i).getValorLiquido()));
			}
	 	    
	 	    // Dados da empresa e vendedor
		 } else if(tipoTabela == 0){
			// Instancia a tabela e a quantidade de colunas
			tabela = new PdfPTable(new float[] {1f});
			// Percentagem da largura da pagina
			tabela.setWidthPercentage(100);
			tabela.setHorizontalAlignment(Element.ALIGN_CENTER);
			
			// Variavel para pegar os dados da emrpesa no banco
			EmpresaRotinas empresaRotinas = new EmpresaRotinas(context);
			
			// Pega os dados da emrpesa
			EmpresaBeans empresa = empresaRotinas.empresa(funcoes.getValorXml("CodigoEmpresa"));
			
			// Nome da empresa
			PdfPCell c1 = new PdfPCell(new Phrase(empresa.getNomeFantasia()));
		 	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		 	tabela.addCell(c1);
		 	tabela.setHeaderRows(1);
		 	
		 	//tabela.addCell(empresa.getNomeFantasia());
		 	tabela.addCell("Vendedor: " + funcoes.getValorXml("Usuario"));
			
			// Dados do orcamento
		 } else if(tipoTabela == 2){
			// Instancia a tabela e a quantidade de colunas
			tabela = new PdfPTable(new float[] {0.5f, 0.5f});
			// Percentagem da largura da pagina
			tabela.setWidthPercentage(100);
			PessoaRotinas pessoaRotinas = new PessoaRotinas(context);
			PessoaBeans pessoa = pessoaRotinas.listaPessoaResumido("CFACLIFO.ID_CFACLIFO = " + orcamento.getIdPessoa(), "cliente").get(0);
			
			// Preenche as celulas em asequencia
			tabela.addCell("Orçamento/Pedido N�: " + orcamento.getIdOrcamento());
			tabela.addCell("Data Cadastro: " + orcamento.getDataCadastro());
			tabela.addCell("Razão Social: " + pessoa.getNomeRazao());
			tabela.addCell("Fantasia: " + pessoa.getNomeFantasia());
			tabela.addCell("CPF/CNPJ: " + pessoa.getCpfCnpj());
			tabela.addCell("I.E.: " + pessoa.getIeRg());
			tabela.addCell("Endereço: " + pessoa.getEnderecoPessoa().getLogradouro() + ", Nº " + pessoa.getEnderecoPessoa().getNumero());
			tabela.addCell("Bairro: " + pessoa.getEnderecoPessoa().getBairro());
			tabela.addCell("Cidade: " + pessoa.getCidadePessoa().getDescricao());
			tabela.addCell("Estado: " + pessoa.getEstadoPessoa().getSiglaEstado());
			
			// Numero do Orcamento
			PdfPCell c1 = new PdfPCell();
		 	c1.setPhrase(new Phrase("Nº " + orcamento.getIdOrcamento()));
		 	c1.setHorizontalAlignment(Element.ALIGN_LEFT);
		 	tabela.addCell(c1);
		 	
		 	// Total do orcamento
		 } else if(tipoTabela ==3){
			// Instancia a tabela e a quantidade de colunas
			tabela = new PdfPTable(new float[] {0.2f});
			// Percentagem da largura da pagina
			tabela.setWidthPercentage(100);
			tabela.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			tabela.addCell("Total Geral: " + funcoes.arredondarValor(orcamento.getTotalOrcamento()));
		 
			// Observacao
		 } else if(tipoTabela == 4){
			// Instancia a tabela e a quantidade de colunas
			tabela = new PdfPTable(new float[] {1f});
			// Percentagem da largura da pagina
			tabela.setWidthPercentage(100);
			tabela.getDefaultCell().setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
			tabela.addCell("Observação: " + orcamento.getObservacao());
		 }
		 
		// Alinha a tabela no documento
		tabela.setHorizontalAlignment(Element.ALIGN_CENTER);
		
 	    return tabela;

 	  }

}

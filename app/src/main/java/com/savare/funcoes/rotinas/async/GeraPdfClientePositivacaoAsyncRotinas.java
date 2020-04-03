package com.savare.funcoes.rotinas.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.savare.R;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.beans.CfapositBeans;
import com.savare.beans.EmpresaBeans;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.EmpresaRotinas;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GeraPdfClientePositivacaoAsyncRotinas extends AsyncTask<Void, Void, String> {
    private static Font empresaFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLDITALIC);
    private static Font contextoFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private ProgressDialog progress;
    private Context context;
    private List<CfapositBeans> listCfaposit;
    private List<PessoaBeans> listPessoa;
    private String dataInicial, dataFinal, nameFile;

    public GeraPdfClientePositivacaoAsyncRotinas(Context context) {
        this.context = context;
    }

    public void setListCfaposit(List<CfapositBeans> listCfaposit) {
        this.listCfaposit = listCfaposit;
    }

    public void setListPessoa(List<PessoaBeans> listPessoa) {
        this.listPessoa = listPessoa;
    }

    public void setDataInicial(String dataInicial) {
        this.dataInicial = dataInicial;
    }

    public void setDataFinal(String dataFinal) {
        this.dataFinal = dataFinal;
    }

    @Override
    protected void onPreExecute() {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                //Cria novo um ProgressDialogo e exibe
                progress = new ProgressDialog(context);
                progress.setMessage("Aguarde, Gerando o PDF...");
                progress.show();
            }
        });
        if ( ((dataInicial!= null) && (!dataInicial.isEmpty())) || ((dataFinal != null) && (!dataFinal.isEmpty())) ){
            nameFile = dataInicial.replace("/", "_").replace("-", "_") + "_ATE_" + dataFinal.replace("/", "_").replace("-", "_");
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        String localDocumento = "";

        Document documento = new Document();
        // Cria o documento tamanho A4
        documento = new Document(PageSize.A4);

        // Cria um formato de data
        DateFormat dataFormatada = new SimpleDateFormat("MM_yyyy");
        // Instancia com a data atual
        Date data = new Date();

        File camihnoDocumento;
        File pasta = new File(Environment.getExternalStorageDirectory() + "/SAVARE/PDF/POSITIVACAO/" + dataFormatada.format(data));
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
        try {
            if (!pasta.exists()) {
                pasta.mkdirs();
            }
            camihnoDocumento = new File(pasta, nameFile + ".pdf");

            // associa a stream de saida ao
            PdfWriter.getInstance(documento, new FileOutputStream(camihnoDocumento));
            // Abre o documento
            documento.open();

            // Dados da empresa e vendedor
            // Instancia a tabelaDadosEmpresa e a quantidade de colunas
            PdfPTable tabelaDadosEmpresa = new PdfPTable(new float[] {1f});
            // Percentagem da largura da pagina
            tabelaDadosEmpresa.setWidthPercentage(100);
            tabelaDadosEmpresa.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Variavel para pegar os dados da emrpesa no banco
            EmpresaRotinas empresaRotinas = new EmpresaRotinas(context);

            // Pega os dados da emrpesa
            EmpresaBeans empresa = empresaRotinas.empresa(funcoes.getValorXml("CodigoEmpresa"));

            if (empresa != null) {
                // Nome da empresa
                PdfPCell c1 = new PdfPCell(new Phrase(empresa.getNomeFantasia(), empresaFont));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabelaDadosEmpresa.addCell(c1);
                tabelaDadosEmpresa.setHeaderRows(1);
            }

            PessoaSql pessoaSql = new PessoaSql(context);
            Cursor dadosVendedor = pessoaSql.sqlSelect("SELECT CFACLIFO.NOME_RAZAO FROM CFACLIFO WHERE CFACLIFO.CODIGO_FUN = " + funcoes.getValorXml(funcoes.TAG_CODIGO_USUARIO));
            if (dadosVendedor != null && dadosVendedor.moveToFirst()){
                tabelaDadosEmpresa.addCell("Vendedor: " + dadosVendedor.getString(dadosVendedor.getColumnIndex("NOME_RAZAO")));
            } else {
                //tabelaDadosEmpresa.addCell(empresa.getNomeFantasia());
                tabelaDadosEmpresa.addCell("Vendedor: " + funcoes.getValorXml("Usuario"));
            }
            tabelaDadosEmpresa.addCell("De: " + funcoes.formataData(dataInicial) + " - Até: " + funcoes.formataData(dataFinal));
            // Adiciona a tabelaClientesPositivados no documento
            documento.add(tabelaDadosEmpresa);

            // Adiciona linhas em branco
            ListItem linhasBranco = new ListItem(" ");
            linhasBranco.add(" ");
            documento.add(linhasBranco);

            //PdfPCell tituloTabela = new PdfPCell(new Phrase("Clientes Positivados", empresaFont));
            //tituloTabela.setHorizontalAlignment(Element.ALIGN_CENTER);
            //tabelaDadosEmpresa.addCell(tituloTabela);
            //tabelaDadosEmpresa.setHeaderRows(1);
            documento.add(new ListItem("Clientes Positivados"));

            // Adicionar os clientes positivados
            // Adiciona o tamanho de cada coluna
            PdfPTable tabelaClientesPositivados = new PdfPTable(new float[] {0.15f, 0.42f, 0.42f, 0.25f, 0.20f, 0.25f, 0.40f });
            // Percentagem da largura da pagina
            tabelaClientesPositivados.setWidthPercentage(100);
            // Muda a cor do fundo do cabecalho
            //tabelaClientesPositivados.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);

            PdfPCell c1 = new PdfPCell(new Phrase("Código", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            c1 = new PdfPCell(new Phrase("Razão", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            c1 = new PdfPCell(new Phrase("Fantasia", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            c1 = new PdfPCell(new Phrase("CNPJ/CPF", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            c1 = new PdfPCell(new Phrase("Data Venda", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            c1 = new PdfPCell(new Phrase("Valor", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            c1 = new PdfPCell(new Phrase("Positivacao", smallBold));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabelaClientesPositivados.addCell(c1);

            tabelaClientesPositivados.setHeaderRows(1);

            for (int i = 0; i < listCfaposit.size(); i++) {
                tabelaClientesPositivados.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tabelaClientesPositivados.addCell(new Phrase(listCfaposit.get(i).getPessoaBeans().getCodigoCliente() + "", contextoFont));
                tabelaClientesPositivados.addCell(new Phrase(listCfaposit.get(i).getPessoaBeans().getNomeRazao(), contextoFont));
                tabelaClientesPositivados.addCell(new Phrase(listCfaposit.get(i).getPessoaBeans().getNomeFantasia(), contextoFont));
                tabelaClientesPositivados.addCell(new Phrase(listCfaposit.get(i).getPessoaBeans().getCpfCnpj(), contextoFont));
                tabelaClientesPositivados.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabelaClientesPositivados.addCell(new Phrase(funcoes.formataData(listCfaposit.get(i).getDataVisita()), contextoFont));
                tabelaClientesPositivados.addCell(new Phrase(funcoes.arredondarValor(listCfaposit.get(i).getValor()), contextoFont));
                if (listCfaposit.get(i).getStatus().equalsIgnoreCase("0")) {
                    tabelaClientesPositivados.addCell(new Phrase("Visitou e Comprou", contextoFont));
                } else if (listCfaposit.get(i).getStatus().equalsIgnoreCase("1")) {
                    tabelaClientesPositivados.addCell(new Phrase("Visitou, mas, Não Comprou", contextoFont));
                } else if (listCfaposit.get(i).getStatus().equalsIgnoreCase("2")) {
                    tabelaClientesPositivados.addCell(new Phrase("Não Estava", contextoFont));
                } else if (listCfaposit.get(i).getStatus().equalsIgnoreCase("3")) {
                    tabelaClientesPositivados.addCell(new Phrase("Pedido Feito Por Telefone", contextoFont));
                } else if (listCfaposit.get(i).getStatus().equalsIgnoreCase("4")) {
                    tabelaClientesPositivados.addCell(new Phrase("Pedido Feito Pelo Balcao/Loja", contextoFont));
                } else {
                    tabelaClientesPositivados.addCell(new Phrase("SEM STATUS!", contextoFont));
                }
            }
            // Alinha a tabela no documento
            tabelaClientesPositivados.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Adiciona a tabela no documento
            documento.add(tabelaClientesPositivados);

            // Adiciona linhas em branco
            documento.add(linhasBranco);

            if ((listPessoa != null) && (listPessoa.size() > 0)) {
                //PdfPCell tituloNaoPositivado = new PdfPCell(new Phrase("Clientes Não Positivados", empresaFont));
                //tituloNaoPositivado.setHorizontalAlignment(Element.ALIGN_CENTER);
                //tabelaDadosEmpresa.addCell(tituloNaoPositivado);
                //tabelaDadosEmpresa.setHeaderRows(1);

                documento.add(new ListItem("Clientes Não Positivados"));

                // Adicionar os clientes positivados
                // Adiciona o tamanho de cada coluna
                PdfPTable tabelaClientesRestantes = new PdfPTable(new float[]{0.15f, 0.42f, 0.42f, 0.25f, 0.42f});
                // Percentagem da largura da pagina
                tabelaClientesRestantes.setWidthPercentage(100);
                // Muda a cor do fundo do cabecalho
                //tabelaClientesPositivados.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);

                PdfPCell c2 = new PdfPCell(new Phrase("Código", smallBold));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabelaClientesRestantes.addCell(c2);

                c2 = new PdfPCell(new Phrase("Razão", smallBold));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabelaClientesRestantes.addCell(c2);

                c2 = new PdfPCell(new Phrase("Fantasia", smallBold));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabelaClientesRestantes.addCell(c2);

                c2 = new PdfPCell(new Phrase("CNPJ/CPF", smallBold));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabelaClientesRestantes.addCell(c2);

                c2 = new PdfPCell(new Phrase("Positivacao", smallBold));
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabelaClientesRestantes.addCell(c2);

                tabelaClientesRestantes.setHeaderRows(1);

                for (int i = 0; i < listPessoa.size(); i++) {
                    tabelaClientesRestantes.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    tabelaClientesRestantes.addCell(new Phrase(listPessoa.get(i).getCodigoCliente() + "", contextoFont));
                    tabelaClientesRestantes.addCell(new Phrase(listPessoa.get(i).getNomeRazao(), contextoFont));
                    tabelaClientesRestantes.addCell(new Phrase(listPessoa.get(i).getNomeFantasia(), contextoFont));
                    tabelaClientesRestantes.addCell(new Phrase(listPessoa.get(i).getCpfCnpj(), contextoFont));
                    tabelaClientesRestantes.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tabelaClientesRestantes.addCell(new Phrase("Sem Positivação", contextoFont));
                }
                // Alinha a tabela no documento
                tabelaClientesRestantes.setHorizontalAlignment(Element.ALIGN_CENTER);

                // Adiciona a tabela no documento
                documento.add(tabelaClientesRestantes);
            }

            // Fecha o documento
            documento.close();

            if(camihnoDocumento.exists()){
                localDocumento = camihnoDocumento.toString();
            }

        } catch (Exception e){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title(R.string.produtos)
                            .content(R.string.nao_achamos_nenhum_valor)
                            .positiveText(android.R.string.ok)
                            //.negativeText(R.string.disagree)
                            .autoDismiss(true)
                            .show();
                }
            });
        }
        return localDocumento;
    }

    @Override
    protected void onPostExecute(String aVoid) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                // Cancela progressDialogo
                progress.dismiss();
            }
        });
    }
}

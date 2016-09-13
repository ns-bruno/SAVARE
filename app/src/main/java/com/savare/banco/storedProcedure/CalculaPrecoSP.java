package com.savare.banco.storedProcedure;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.savare.R;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.MarcaSql;
import com.savare.banco.funcoesSql.PercentualSql;
import com.savare.banco.funcoesSql.ProdutoLojaSql;
import com.savare.banco.funcoesSql.ProdutoSql;
import com.savare.funcoes.FuncoesPersonalizadas;

/**
 * Created by Bruno Nogueira Silva on 06/09/2016.
 */
public class CalculaPrecoSP extends StoredProcedure {

    private double  precoAtacadoFinal = 0,
                    precoVarejoFinal = 0,
                    precoServicoFinal = 0;
    private double  precoAtacado = 0,
                    precoVarejo = 0,
                    precoServico = 0,
                    custoCompleto,
                    custoReposicao,
                    fatorConversao = 1,
                    fatorPreco = 1,
                    fatorVenda = 0,
                    markupAtacado = 0,
                    markupVarejo = 0;
    private int idProduto,
                idEmpresa,
                idClasse,
                idGrupo,
                idSubGrupo,
                idFamilia,
                idMarca,
                idFator;
    private String tipoProduto;

    public CalculaPrecoSP(Context context) {
        super(context);
    }

    public ContentValues execute(int idProdutoLoja,
                                 int idEmbalagem,
                                 int idPlanoPgto,
                                 int idCliente,
                                 int idVendedor,
                                 double precoVenda){
        // Cria avariavel de retorno
        ContentValues retorno = null;
        // Checa se foi passado por paramentro o id do produto por loja
        if (idProdutoLoja == 0){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "CalculaPrecoSP");
            contentValues.put("mensagem", context.getResources().getString(R.string.erro_id_produto_loja_nao_informado));
            contentValues.put("dados", context.toString());
            // Pega os dados do usuario
            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } else {
            // Conecta com o banco de dados
            bancoDados = conexaoBanco.abrirBanco();

            try {
                ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

                Cursor dados = produtoLojaSql.sqlSelect("SELECT ID_AEAPRODU, ID_SMAEMPRE, VENDA_VARE, VENDA_ATAC, CT_REPOSICAO_N, CT_COMPLETO_N FROM AEAPLOJA \n" +
                                                      "WHERE AEAPLOJA.ID_AEAPLOJA = " + idProdutoLoja);
                // Checa se retornou alguma coisa do banco de dados
                if ((dados != null) && (dados.getCount() > 0)){
                    // Move o cursor para o primeiro registro
                    dados.moveToFirst();

                    // Pega os dados buscado nos banco
                    idProduto = dados.getInt(dados.getColumnIndex("ID_AEAPRODU"));
                    idEmpresa = dados.getInt(dados.getColumnIndex("ID_SMAEMPRE"));
                    precoVarejo = dados.getDouble(dados.getColumnIndex("VENDA_VARE"));
                    precoAtacado = dados.getDouble(dados.getColumnIndex("VENDA_ATAC"));
                    custoReposicao = dados.getDouble(dados.getColumnIndex("CT_REPOSICAO_N"));
                    custoCompleto = dados.getDouble(dados.getColumnIndex("CT_COMPLETO_N"));
                }
                // Limpa a memoria
                produtoLojaSql = null; dados = null;

                EmbalagemSql embalagemSql = new EmbalagemSql(context);

                Cursor dadosEmbalagem = embalagemSql.sqlSelect("SELECT FATOR_CONVERSAO, FATOR_PRECO FROM AEAEMBAL WHERE ID_AEAEMBAL = " + idEmbalagem);
                // Checa se retornou alguma coisa do banco de dados
                if ((dadosEmbalagem != null) && (dadosEmbalagem.getCount() > 0)){
                    dadosEmbalagem.moveToFirst();

                    fatorConversao = dadosEmbalagem.getDouble(dadosEmbalagem.getColumnIndex("FATOR_CONVERSAO"));
                    fatorPreco = dadosEmbalagem.getDouble(dadosEmbalagem.getColumnIndex("FATOR_PRECO"));
                }
                // Libera memoria
                embalagemSql = null; dadosEmbalagem = null;

                precoAtacado = precoAtacado * fatorConversao * fatorPreco;
                precoVarejo = precoVarejo * fatorConversao * fatorPreco;

                if (precoAtacado == 0){precoAtacado = precoVenda;}
                if (precoVarejo == 0){precoVarejo = precoVenda;}

                ProdutoSql produtoSql = new ProdutoSql(context);

                Cursor dadosProduto = produtoSql.sqlSelect("SELECT TIPO, ID_AEACLASE, ID_AEAGRUPO, ID_AEASGRUP, ID_AEAFAMIL, ID_AEAMARCA FROM AEAPRODU \n" +
                                                           "WHERE ID_AEAPRODU = " + idProduto);

                if ((dadosProduto != null) && (dadosProduto.getCount() > 0)){
                    dadosProduto.moveToFirst();

                    tipoProduto = dadosProduto.getString(dadosProduto.getColumnIndex("TIPO"));
                    idClasse = dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEACLASE"));
                    idGrupo = dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAGRUPO"));
                    idSubGrupo = dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEASGRUP"));
                    idFamilia = dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAFAMIL"));
                    idMarca = dadosProduto.getInt(dadosProduto.getColumnIndex("ID_AEAMARCA"));
                }
                // Libera memoria
                dadosProduto = null;
                // Checa se o tipo de produto eh conjunto
                if (tipoProduto.equalsIgnoreCase("2")){

                // Checa se o tipo de produto eh produto
                } else if (tipoProduto.equalsIgnoreCase("1")){
                    precoServico = precoVarejo;

                    MarcaSql marcaSql = new MarcaSql(context);

                    dados = marcaSql.sqlSelect("SELECT FATOR_VENDA FROM AEAMARCA WHERE ID_AEAMARCA = " + idMarca);

                    // Checa se voltou alguma coisa
                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        fatorVenda = dados.getDouble(dados.getColumnIndex("FATOR_VENDA"));
                    }
                    // Checa se tem o fator de venda
                    if (fatorVenda == 0){fatorVenda = 1;}

                    // Libera memoria
                    dados = null;

                    // Cria variaveis temporarias
                    double makeUpAtac, makeUpVare, descontoMercadoriaVistaVarejo, descontoMercadoriaVistaAtacado,
                           descontoMercadoriaPrazoVarejo, descontoMercadoriaPrazoAtacado;
                    int idPercentualTabela, idFatorTemp = 0;

                    // Checa se foi passado por parametro o codigo do vendedor
                    if (idVendedor != 0){
                        PercentualSql percentualSql = new PercentualSql(context);

                        dados = percentualSql.sqlSelect("SELECT MARKUP_VARE, MARKUP_ATAC \n" +
                                "FROM AEAPERCE \n" +
                                "WHERE " +
                                "((ID_AEAPLOJA = " + idProdutoLoja + ") OR (ID_AEAPRODU = " + idProduto + ") OR \n" +
                                "(ID_AEASGRUP = " + idSubGrupo + ") OR (ID_AEAGRUPO = " + idGrupo + ") OR \n" +
                                "(ID_AEACLASE = " + idClasse + ") OR (ID_AEAFAMIL = " + idFamilia + ") OR \n" +
                                "(ID_AEAMARCA = " + idMarca + ")) AND (ID_CFAPARAM_VENDEDOR = " + idVendedor + ")");
                        if ((dados != null) && (dados.getCount() > 0)) {


                            dados.moveToFirst();
                            makeUpAtac = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC"));
                            makeUpVare = dados.getDouble(dados.getColumnIndex("MARKUP_VARE"));
                            // Passa por todos os registros
                            while (dados.moveToNext()) {
                                if (markupAtacado == 0) {markupAtacado = makeUpAtac;}
                                if (markupVarejo == 0) {markupVarejo = makeUpVare;}
                            }
                        }
                        dados = null;
                    }
                    PercentualSql percentualSql = new PercentualSql(context);
                    dados = percentualSql.sqlSelect("SELECT ID_AEATBPER_TABELA, ID_AEAFATOR, MARKUP_VARE, MARKUP_ATAC, " +
                                                    "DESC_MERC_VISTA_VARE, DESC_MERC_VISTA_ATAC, DESC_MERC_PRAZO_VARE, DESC_MERC_PRAZO_ATAC \n" +
                                                    "FROM AEAPERCE \n" +
                                                    "WHERE ((ID_AEAPLOJA = "+ idProdutoLoja +") OR (ID_AEAPRODU = " + idProduto + ") OR \n" +
                                                    "(ID_AEASGRUP = "+ idSubGrupo +") OR (ID_AEAGRUPO = "+ idGrupo +") OR \n" +
                                                    "(ID_AEACLASE = "+ idClasse +") OR (ID_AEAFAMIL = "+ idFamilia +") OR \n" +
                                                    "(ID_AEAMARCA = "+ idMarca +") OR (ID_SMAEMPRE = "+ idEmpresa +")) \n" +
                                                    "AND ((ID_CFAPARAM_VENDEDOR IS NULL) OR (ID_CFAPARAM_VENDEDOR = 0)) \n" +
                                                    "ORDER BY COALESCE(ID_AEAPLOJA, ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_SMAEMPRE) IS NULL");

                    if ((dados != null) && (dados.getCount() > 0)){
                        // Passa por todos os registro buscado no banco
                        while (dados.moveToNext()){
                            idPercentualTabela = dados.getInt(dados.getColumnIndex("ID_AEATBPER_TABELA"));
                            idFator = dados.getInt(dados.getColumnIndex("ID_AEAFATOR"));
                            makeUpAtac = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC"));
                            makeUpVare = dados.getDouble(dados.getColumnIndex("MARKUP_VARE"));
                            descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_MERC_VISTA_VARE"));
                            descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_MERC_VISTA_ATAC"));
                            descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_MERC_PRAZO_VARE"));
                            descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_MERC_PRAZO_ATAC"));

                            if ((idFator == 0) && (idFatorTemp != 0)){idFator = idFatorTemp;}
                            if (markupAtacado == 0) {markupAtacado = makeUpAtac;}
                            if (markupVarejo == 0) {markupVarejo = makeUpVare;}
                        }
                    }
                }

            } catch (Exception e) {

                final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "CalculaPrecoSP");
                contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.getMessage()));
                contentValues.put("dados", e.toString());
                // Pega os dados do usuario
                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                contentValues.put("email", funcoes.getValorXml("Email"));
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        funcoes.menssagem(contentValues);
                    }
                });
            } finally {
                conexaoBanco.fechar();
                bancoDados.close();
            }
        }
        return retorno;
    }
}

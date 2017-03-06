package com.savare.banco.storedProcedure;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.banco.funcoesSql.EmbalagemSql;
import com.savare.banco.funcoesSql.FatorSql;
import com.savare.banco.funcoesSql.MarcaSql;
import com.savare.banco.funcoesSql.PercentualSql;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.PlanoPagamentoSql;
import com.savare.banco.funcoesSql.PrecoSql;
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
                    custoReal,
                    custoMedio,
                    fatorConversao = 1,
                    fatorPreco = 1,
                    fatorVenda = 0,
                    markupAtacado = 0,
                    markupVarejo = 0,
                    jurosMedioAtacado = 0,
                    jurosMedioVarejo = 0,
                    jurosMedioServico = 0,
                    descontoMaximoPlanoAtacadoVista,
                    descontoMaximoPlanoAtacadoPrazo,
                    descontoMaximoPlanoVarejoVista,
                    descontoMaximoPlanoVarejoPrazo,
                    descontoMaximoPlanoServicoVista,
                    descontoMaximoPlanoServicoPrazo;
    private int idProduto,
                idEmpresa,
                idClasse,
                idGrupo,
                idSubGrupo,
                idFamilia,
                idMarca,
                idFator,
                diasBonusFator;
    private String  tipoProduto,
                    tipoBonusFator;

    public CalculaPrecoSP(Context context, ProgressBar progressBarStatus, TextView textStatus) {
        super(context, progressBarStatus, textStatus);
    }

    public ContentValues execute(int idProdutoLoja,
                                 int idEmbalagem,
                                 int idPlanoPgto,
                                 int idCliente,
                                 int idVendedor,
                                 String dataVenda,
                                 double precoVenda){
        // Cria avariavel de retorno
        ContentValues retorno = null;

        // Checa se foi passado por paramentro o id do produto por loja
        if (idProdutoLoja == 0){
            // Checo se o texto de status foi passado pro parametro
            if (textStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setVisibility(View.VISIBLE);
                        textStatus.setText(context.getResources().getString(R.string.erro_id_produto_loja_nao_informado));
                    }
                });
            }
            if (progressBarStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressBarStatus.setVisibility(View.VISIBLE);
                        progressBarStatus.setIndeterminate(true);
                    }
                });
            }

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

            if (textStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        textStatus.setVisibility(View.VISIBLE);
                        textStatus.setText(context.getResources().getString(R.string.calculando_preco_venda));
                    }
                });
            }
            if (progressBarStatus != null){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressBarStatus.setVisibility(View.VISIBLE);
                        progressBarStatus.setIndeterminate(true);
                    }
                });
            }

            // Conecta com o banco de dados
            bancoDados = conexaoBanco.abrirBanco();

            try {
                ProdutoLojaSql produtoLojaSql = new ProdutoLojaSql(context);

                Cursor dados = produtoLojaSql.sqlSelect("SELECT ID_AEAPRODU, ID_SMAEMPRE, VENDA_VARE, VENDA_ATAC, CT_REPOSICAO_N, CT_COMPLETO_N, CT_REAL_N, CT_MEDIO_N \n" +
                                                        "FROM AEAPLOJA \n" +
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
                    custoReal = dados.getDouble(dados.getColumnIndex("CT_REAL_N"));
                    custoMedio = dados.getDouble(dados.getColumnIndex("CT_MEDIO_N"));
                }
                // Limpa a memoria
                produtoLojaSql = null;
                dados = null;

                PrecoSql precoSql = new PrecoSql(context);

                Cursor dadosPreco = precoSql.sqlSelect("SELECT VENDA_VARE, VENDA_ATAC FROM AEAPRECO WHERE (ID_AEAPRODU = " + idProduto + ") AND (ID_AEAPLPGT = " + idPlanoPgto + ")");

                if ((dadosPreco != null) && (dadosPreco.getCount() > 0)){
                    dadosPreco.moveToFirst();

                    precoVarejo = dadosPreco.getDouble(dadosPreco.getColumnIndex("VENDA_VARE"));
                    precoAtacado = dadosPreco.getDouble(dadosPreco.getColumnIndex("VENDA_ATAC"));
                }

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

                dados = produtoSql.sqlSelect("SELECT TIPO, ID_AEACLASE, ID_AEAGRUPO, ID_AEASGRUP, ID_AEAFAMIL, ID_AEAMARCA FROM AEAPRODU \n" +
                                                           "WHERE ID_AEAPRODU = " + idProduto);

                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();

                    tipoProduto = dados.getString(dados.getColumnIndex("TIPO"));
                    idClasse = dados.getInt(dados.getColumnIndex("ID_AEACLASE"));
                    idGrupo = dados.getInt(dados.getColumnIndex("ID_AEAGRUPO"));
                    idSubGrupo = dados.getInt(dados.getColumnIndex("ID_AEASGRUP"));
                    idFamilia = dados.getInt(dados.getColumnIndex("ID_AEAFAMIL"));
                    idMarca = dados.getInt(dados.getColumnIndex("ID_AEAMARCA"));
                }
                // Libera memoria
                dados = null;
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
                    double descontoMercadoriaVistaVarejo = 0, descontoMercadoriaVistaAtacado = 0,
                           descontoMercadoriaPrazoVarejo = 0, descontoMercadoriaPrazoAtacado = 0;
                    int idPercentualTabela = 0, idFatorTemp = 0;

                    // Checa se foi passado por parametro o codigo do vendedor
                    if (idVendedor != 0){
                        PercentualSql percentualSql = new PercentualSql(context);

                        dados = percentualSql.sqlSelect("SELECT MARKUP_VARE, MARKUP_ATAC \n" +
                                                        "FROM AEAPERCE \n" +
                                                        "WHERE " +
                                                        "((ID_AEAPLOJA = " + idProdutoLoja + ") OR (ID_AEAPRODU = " + idProduto + ") OR \n" +
                                                        "(ID_AEASGRUP = " + idSubGrupo + ") OR (ID_AEAGRUPO = " + idGrupo + ") OR \n" +
                                                        "(ID_AEACLASE = " + idClasse + ") OR (ID_AEAFAMIL = " + idFamilia + ") OR \n" +
                                                        "(ID_AEAMARCA = " + idMarca + ")) AND (ID_CFAPARAM_VENDEDOR = " + idVendedor + ")" +
                                                        "ORDER BY COALESCE(ID_AEAPLOJA, ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_SMAEMPRE) \n");
                                                        /*"CASE WHEN ID_AEAPLOJA IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_AEAPRODU IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_AEASGRUP IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_AEAGRUPO IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_AEACLASE IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_AEAFAMIL IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_AEAMARCA IS NULL THEN 1 ELSE 0 END, \n" +
                                                        "CASE WHEN ID_SMAEMPRE IS NULL THEN 1 ELSE 0 END ");*/
                        if ((dados != null) && (dados.getCount() > 0)) {
                            // Passa por todos os registros
                            while (dados.moveToNext()) {
                                if (markupAtacado == 0) {markupAtacado = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC")); }
                                if (markupVarejo == 0) {markupVarejo = dados.getDouble(dados.getColumnIndex("MARKUP_VARE")); }
                            }
                        }
                        dados = null;
                        percentualSql = null;
                    }
                    PercentualSql percentualSql = new PercentualSql(context);
                    dados = percentualSql.sqlSelect("SELECT ID_AEATBPER_TABELA, ID_AEAFATOR, MARKUP_VARE, MARKUP_ATAC, " +
                                                    "DESC_MERC_VISTA_VARE, DESC_MERC_VISTA_ATAC, DESC_MERC_PRAZO_VARE, DESC_MERC_PRAZO_ATAC \n" +
                                                    "FROM AEAPERCE \n" +
                                                    "WHERE ((ID_AEAPLOJA = "+ idProdutoLoja +") OR (ID_AEAPRODU = " + idProduto + ") OR \n" +
                                                    "(ID_AEASGRUP = "+ idSubGrupo +") OR (ID_AEAGRUPO = "+ idGrupo +") OR \n" +
                                                    "(ID_AEACLASE = "+ idClasse +") OR (ID_AEAFAMIL = "+ idFamilia +") OR \n" +
                                                    "(ID_AEAMARCA = "+ idMarca +") OR (ID_SMAEMPRE = "+ idEmpresa +") " +
                                                    "((ID_AEAAGPPR IS NOT NULL) AND (ID_AEAAGPPR IN \n" +
                                                    "  (SELECT ID_AEAAGPPR \n" +
                                                    "   FROM AEAITGPR \n" +
                                                    "   WHERE ("+ idProduto +" = AEAITGPR.ID_AEAPRODU) OR ("+ idMarca +" = AEAITGPR.ID_AEAMARCA) \n" +
                                                    "   OR ("+ idSubGrupo +" = AEAITGPR.ID_AEASGRUP) OR ("+ idGrupo +" = AEAITGPR.ID_AEAGRUPO) " +
                                                    "   OR ("+ idClasse +" = AEAITGPR.ID_AEACLASE) \n" +
                                                    "   OR ("+ idFamilia +" = AEAITGPR.ID_AEAFAMIL))))) \n" +
                                                    "AND ((ID_CFAPARAM_VENDEDOR IS NULL) OR (ID_CFAPARAM_VENDEDOR = 0)) \n" +
                                                    "ORDER BY COALESCE(ID_AEAPLOJA, ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_AEAAGPPR, ID_SMAEMPRE)");

                    if ((dados != null) && (dados.getCount() > 0)){
                        // Passa por todos os registro buscado no banco
                        while (dados.moveToNext()){
                            idPercentualTabela = dados.getInt(dados.getColumnIndex("ID_AEATBPER_TABELA"));
                            idFatorTemp = dados.getInt(dados.getColumnIndex("ID_AEAFATOR"));
                            if ((idFator == 0) && (idFatorTemp != 0)){idFator = idFatorTemp;}
                            if (markupAtacado == 0) {markupAtacado = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC")); }
                            if (markupVarejo == 0) {markupVarejo = dados.getDouble(dados.getColumnIndex("MARKUP_VARE")); }
                            if (descontoMercadoriaVistaVarejo == 0) {descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_MERC_VISTA_VARE")); }
                            if (descontoMercadoriaVistaAtacado == 0) {descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_MERC_VISTA_ATAC")); }
                            if (descontoMercadoriaPrazoAtacado == 0) {descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_MERC_PRAZO_ATAC")); }
                            if (descontoMercadoriaPrazoVarejo == 0) {descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_MERC_PRAZO_VARE")); }

                            if (idPercentualTabela > 0){
                                Cursor dadosPercentualTemp = percentualSql.sqlSelect(
                                        "SELECT MARKUP_VARE, MARKUP_ATAC, DESC_MERC_VISTA_VARE, DESC_MERC_VISTA_ATAC, DESC_MERC_PRAZO_VARE, DESC_MERC_PRAZO_ATAC \n" +
                                        "FROM AEAPERCE \n" +
                                        "WHERE ID_AEAPERCE = (SELECT ID_AEAPERCE FROM AEAPERCE WHERE ID_AEATBPER = " + idPercentualTabela + ") ");

                                if ((dadosPercentualTemp != null) && (dadosPercentualTemp.getCount() > 0)){
                                    // Move para o primeiro registro
                                    dadosPercentualTemp.moveToFirst();

                                    if (markupAtacado == 0) {markupAtacado = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("MARKUP_ATAC")); }
                                    if (markupVarejo == 0) {markupVarejo = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("MARKUP_VARE")); }
                                    if (descontoMercadoriaVistaVarejo == 0) {descontoMercadoriaVistaVarejo = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_VISTA_VARE")); }
                                    if (descontoMercadoriaVistaAtacado == 0) {descontoMercadoriaVistaAtacado = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_VISTA_ATAC")); }
                                    if (descontoMercadoriaPrazoAtacado == 0) {descontoMercadoriaPrazoAtacado = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_PRAZO_ATAC")); }
                                    if (descontoMercadoriaPrazoVarejo == 0) {descontoMercadoriaPrazoVarejo = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_PRAZO_VARE")); }
                                }
                            }
                        }
                        dados = null;
                        percentualSql = null;
                    }
                    if (idFator == 0){
                        if (textStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    textStatus.setVisibility(View.VISIBLE);
                                    textStatus.setText(context.getResources().getString(R.string.erro_sem_fator_preco));
                                }
                            });
                        }
                        if (progressBarStatus != null){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressBarStatus.setVisibility(View.INVISIBLE);
                                    progressBarStatus.setIndeterminate(true);
                                }
                            });
                        }

                        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                        final ContentValues contentValues = new ContentValues();
                        contentValues.put("comando", 0);
                        contentValues.put("tela", "CalculaPrecoSP");
                        contentValues.put("mensagem", context.getResources().getString(R.string.erro_sem_fator_preco));
                        contentValues.put("dados", context.toString());

                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                funcoes.menssagem(contentValues);
                            }
                        });
                    } else {
                        if (markupVarejo != 0){ precoVarejo = precoVarejo * markupVarejo; }
                        if (markupAtacado != 0){ precoAtacado = precoAtacado * markupAtacado; }

                        precoVarejo = precoVarejo * fatorVenda;
                        precoAtacado = precoAtacado * fatorVenda;
                        precoServico = precoServico * fatorVenda;

                        FatorSql fatorSql = new FatorSql(context);

                        dados = fatorSql.sqlSelect(
                                "SELECT JURO_MEDIO_ATAC, JURO_MEDIO_VARE, JURO_MEDIO_SERV, DESC_MAX_PLANO_ATAC_VISTA, \n" +
                                "DESC_MAX_PLANO_ATAC_PRAZO, DESC_MAX_PLANO_VARE_VISTA, DESC_MAX_PLANO_VARE_PRAZO, \n" +
                                "DESC_MAX_PLANO_SERV_VISTA, DESC_MAX_PLANO_SERV_PRAZO, TIPO_BONUS, DIAS_BONUS \n" +
                                "FROM AEAFATOR \n" +
                                "WHERE ID_AEAFATOR = " + idFator);

                        if ((dados != null) && (dados.getCount() > 0)){
                            // Move o curso para o primeiro registro
                            dados.moveToFirst();

                            jurosMedioAtacado = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_ATAC"));
                            jurosMedioVarejo = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_VARE"));
                            jurosMedioServico = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_SERV"));
                            descontoMaximoPlanoAtacadoVista = dados.getDouble(dados.getColumnIndex("DESC_MAX_PLANO_ATAC_VISTA"));
                            descontoMaximoPlanoAtacadoPrazo = dados.getDouble(dados.getColumnIndex("DESC_MAX_PLANO_ATAC_PRAZO"));
                            descontoMaximoPlanoVarejoVista = dados.getDouble(dados.getColumnIndex("DESC_MAX_PLANO_VARE_VISTA"));
                            descontoMaximoPlanoVarejoPrazo = dados.getDouble(dados.getColumnIndex("DESC_MAX_PLANO_VARE_PRAZO"));
                            descontoMaximoPlanoServicoVista = dados.getDouble(dados.getColumnIndex("DESC_MAX_PLANO_SERV_VISTA"));
                            descontoMaximoPlanoServicoPrazo = dados.getDouble(dados.getColumnIndex("DESC_MAX_PLANO_SERV_PRAZO"));
                            tipoBonusFator = dados.getString(dados.getColumnIndex("TIPO_BONUS"));
                            diasBonusFator = dados.getInt(dados.getColumnIndex("DIAS_BONUS"));

                            if (    ((descontoMercadoriaVistaVarejo != 0) && (descontoMaximoPlanoVarejoVista == 0)) ||
                                    ((descontoMercadoriaVistaVarejo < descontoMaximoPlanoVarejoVista) && (descontoMercadoriaVistaVarejo != 0)) ){
                                descontoMaximoPlanoVarejoVista = descontoMercadoriaVistaVarejo;
                            }
                            if (    ((descontoMercadoriaPrazoVarejo != 0) && (descontoMaximoPlanoVarejoPrazo == 0)) ||
                                    ((descontoMercadoriaPrazoVarejo < descontoMaximoPlanoVarejoPrazo) && (descontoMercadoriaPrazoVarejo != 0)) ){
                                descontoMaximoPlanoVarejoPrazo = descontoMercadoriaPrazoVarejo;
                            }
                            if (    ((descontoMercadoriaVistaAtacado != 0) && (descontoMaximoPlanoAtacadoVista == 0)) ||
                                    ((descontoMercadoriaVistaAtacado < descontoMaximoPlanoAtacadoVista) && (descontoMercadoriaVistaAtacado != 0)) ){
                                descontoMaximoPlanoAtacadoVista = descontoMercadoriaVistaAtacado;
                            }
                            if (    ((descontoMercadoriaPrazoAtacado != 0) && (descontoMaximoPlanoAtacadoPrazo == 0)) ||
                                    ((descontoMercadoriaPrazoAtacado < descontoMaximoPlanoAtacadoPrazo) && (descontoMercadoriaPrazoAtacado != 0)) ){
                                descontoMaximoPlanoAtacadoPrazo = descontoMercadoriaPrazoAtacado;
                            }
                            descontoMercadoriaVistaVarejo = 0;
                            descontoMercadoriaPrazoVarejo = 0;
                            descontoMercadoriaVistaAtacado = 0;
                            descontoMercadoriaPrazoAtacado = 0;
                        }
                        dados = null;
                        fatorSql = null;

                        PlanoPagamentoSql planoPagamentoSql = new PlanoPagamentoSql(context);

                        String vistaPrazoPlanoPgto;
                        dados = planoPagamentoSql.sqlSelect(
                                "SELECT DESC_PROMOCAO, VISTA_PRAZO, PERC_DESC_ATAC, PERC_DESC_VARE, \n" +
                                "QTDE_PARCELAS1, DIAS_PARCELAS1, QTDE_PARCELAS2, DIAS_PARCELAS2, QTDE_PARCELAS3, DIAS_PARCELAS3, PERC_ENTRADA, \n" +
                                "DIAS_ENTRADA, ORIGEM_VALOR, JURO_MEDIO_ATAC, JURO_MEDIO_VARE, JURO_MEDIO_SERV, JURO_MEDIO_LOCAL \n" +
                                "FROM AEAPLPGT \n" +
                                "WHERE AEAPLPGT.ID_AEAPLPGT = " + idPlanoPgto);

                        if ((dados != null) && (dados.getCount() > 0)){
                            dados.moveToFirst();

                            vistaPrazoPlanoPgto = dados.getString(dados.getColumnIndex("VISTA_PRAZO"));
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

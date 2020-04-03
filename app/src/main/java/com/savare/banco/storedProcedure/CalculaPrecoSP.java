package com.savare.banco.storedProcedure;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Created by Bruno Nogueira Silva on 06/09/2016.
 */
public class CalculaPrecoSP extends StoredProcedure {

    public static final String  KEY_PRECO_VAREJO = "precoVarejo",
                                KEY_PRECO_ATACADO = "precoAtacado",
                                KEY_PRECO_SERVICO = "precoServico",
                                KEY_PRODUTO_PROMOCAO_ATACADO = "prodPromoAtac",
                                KEY_PRODUTO_PROMOCAO_VAREJO = "prodPromoVare",
                                KEY_PRODUTO_PROMOCAO_SERVICO = "prodPromoServ";
    private double  precoAtacado = 0,
                    precoVarejo = 0,
                    precoServico = 0,
                    vendaVarejoPreco = 0,
                    vendaAtacadoPreco = 0,
                    precoPromocaoVistaVarejo,
                    precoPromocaoVistaAtacado,
                    precoPromocaoVistaServico,
                    precoPromocaoPrazoVarejo,
                    precoPromocaoPrazoAtacado,
                    precoPromocaoPrazoServico,
                    custoReposicao,
                    custoReal,
                    custoMedio,
                    custoCompleto,
                    fatorConversao = 1,
                    fatorPreco = 1,
                    fatorVenda = 0,
                    markupAtacado = 0,
                    markupVarejo = 0,
                    valorMarkupAtacado1 = 0,
                    valorMarkupAtacado2 = 0,
                    valorMarkupAtacado3 = 0,
                    valorMarkupVarejo1 = 0,
                    valorMarkupVarejo2 = 0,
                    valorMarkupVarejo3 = 0,
                    markupEmpresaVarejo1 = 0,
                    markupEmpresaVarejo2 = 0,
                    markupEmpresaVarejo3 = 0,
                    markupEmpresaAtacado1 = 0,
                    markupEmpresaAtacado2 = 0,
                    markupEmpresaAtacado3 = 0,
                    jurosMedioPlPgtAtacado = 0,
                    jurosMedioPlPgtVarejo = 0,
                    jurosMedioPlPgtServico = 0,
                    jurosMedioFatorAtacado = 0,
                    jurosMedioFatorVarejo = 0,
                    jurosMedioFatorServico = 0,
                    descontoMaximoPlanoAtacadoVista,
                    descontoMaximoPlanoAtacadoPrazo,
                    descontoMaximoPlanoVarejoVista,
                    descontoMaximoPlanoVarejoPrazo,
                    descontoMaximoPlanoServicoVista,
                    descontoMaximoPlanoServicoPrazo,
                    descontoMercadoriaVistaVarejo,
                    descontoMercadoriaVistaAtacado,
                    descontoMercadoriaPrazoVarejo,
                    descontoMercadoriaPrazoAtacado,
                    descontoMercadoriaVistaServico,
                    descontoMercadoriaPrazoServico,
                    percentualDescontoAtacado,
                    percentualDescontoVarejo,
                    percentualDescontoServico,
                    percentualEntrada;
    private Integer
                idProduto,
                idEmpresa,
                idClasse,
                idGrupo,
                idSubGrupo,
                idFamilia,
                idMarca,
                idFator,
                idMoeda,
                idAtividade,
                idProfissao,
                idTipoCliente,
                idStatus,
                idArea,
                idPlanoPgtoEquivalente,
                idItemTabelaPromocao,
                idItemTabelaPromocaoExcesao,
                diasBonusFator,
                qtdeParcelas1,
                qtdeParcelas2,
                qtdeParcelas3,
                diasParcela1,
                diasParcela2,
                diasParcela3,
                diasEntrada;
    private String  tipoProduto,
                    tipoBonusFator,
                    dataReajusteAtacado,
                    dataReajusteVarejo,
                    produtoEmPromocaoAtacado,
                    produtoEmPromocaoVarejo,
                    produtoEmPromocaoServico,
                    descontoPromocao,
                    vistaPrazo,
                    origemValor,
                    usaJurosMedioLocal,
                    calculaJuros;
    private NotificationManager notificationManager;
    private NotificationCompat.BigTextStyle bigTextStyle;
    private NotificationCompat.Builder mBuilder;

    public CalculaPrecoSP(Context context, ProgressBar progressBarStatus, TextView textStatus) {
        super(context, progressBarStatus, textStatus);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel mChannel = new NotificationChannel(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL, FuncoesPersonalizadas.NOTIFICATION_FILE, NotificationManager.IMPORTANCE_MIN);
            mChannel.setDescription(context.getResources().getString(R.string.calculando_preco_venda));
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Create a BigTextStyle object.
        bigTextStyle = new NotificationCompat.BigTextStyle();

        mBuilder = new NotificationCompat.Builder(context, ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_smallicon)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentTitle(context.getResources().getString(R.string.calculando_preco_venda))
                //.setContentText(mActivity.getResources().getString(R.string.app_name))
                .setStyle(bigTextStyle)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setSound(null, 0)
                .setVibrate(new long[0])
                .setOnlyAlertOnce(true);
    }

    /**
     *
     * @param idProdutoLoja
     * @param idEmbalagem
     * @param idPlanoPgto
     * @param idCliente
     * @param codigoVendedor - é o mesmo CODIGO_FUN da tabela CFACLIFO
     * @param dataVenda
     * @param precoVendaAtacado
     * @param precoVendaVarejo
     * @return
     */
    public ContentValues execute(final int idProdutoLoja,
                                 int idEmbalagem,
                                 final int idPlanoPgto,
                                 int idCliente,
                                 int codigoVendedor,
                                 String dataVenda,
                                 double precoVendaAtacado,
                                 double precoVendaVarejo){
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
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("CalculaPrecoSP")
                            .content(context.getResources().getString(R.string.erro_id_produto_loja_nao_informado))
                            .positiveText(R.string.button_ok)
                            .show();
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
            StringBuilder sql = new StringBuilder();

            try {
                sql.setLength(0);
                sql.append("SELECT ID_AEAPRODU, ID_SMAEMPRE, VENDA_VARE, VENDA_ATAC, DT_REAJUSTE_VARE, DT_REAJUSTE_ATAC, CT_REPOSICAO_N, CT_COMPLETO_N, CT_REAL_N, CT_MEDIO_N \n");
                sql.append("FROM AEAPLOJA \n");
                sql.append("WHERE AEAPLOJA.ID_AEAPLOJA = " + idProdutoLoja);

                Cursor dados = bancoDados.rawQuery(sql.toString(), null);

                // Checa se retornou alguma coisa do banco de dados
                if ((dados != null) && (dados.getCount() > 0)){
                    // Move o cursor para o primeiro registro
                    dados.moveToFirst();

                    // Pega os dados buscado nos banco
                    idProduto = dados.getInt(dados.getColumnIndex("ID_AEAPRODU"));
                    idEmpresa = dados.getInt(dados.getColumnIndex("ID_SMAEMPRE"));
                    precoVarejo = dados.getDouble(dados.getColumnIndex("VENDA_VARE"));
                    precoAtacado = dados.getDouble(dados.getColumnIndex("VENDA_ATAC"));
                    dataReajusteVarejo = dados.getString(dados.getColumnIndex("DT_REAJUSTE_VARE"));
                    dataReajusteAtacado = dados.getString(dados.getColumnIndex("DT_REAJUSTE_ATAC"));
                    custoReposicao = dados.getDouble(dados.getColumnIndex("CT_REPOSICAO_N"));
                    custoCompleto = dados.getDouble(dados.getColumnIndex("CT_COMPLETO_N"));
                    custoReal = dados.getDouble(dados.getColumnIndex("CT_REAL_N"));
                    custoMedio = dados.getDouble(dados.getColumnIndex("CT_MEDIO_N"));
                }
                // Limpa a memoria
                dados = null;

                sql.setLength(0);
                sql.append("SELECT FORMA, CALCULA_JUROS, VENDA_VARE, VENDA_ATAC FROM AEAPRECO WHERE (ID_AEAPRODU = " + idProduto + ") AND (ID_AEAPLPGT = " + idPlanoPgto + ")");

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();
                    vistaPrazo = dados.getString(dados.getColumnIndex("FORMA"));
                    calculaJuros = dados.getString(dados.getColumnIndex("CALCULA_JUROS"));
                    vendaVarejoPreco = dados.getDouble(dados.getColumnIndex("VENDA_VARE"));
                    vendaAtacadoPreco = dados.getDouble(dados.getColumnIndex("VENDA_ATAC"));
                }
                // Limpa a memoria
                dados = null;

                sql.setLength(0);
                sql.append("SELECT FORMA, CALCULA_JUROS, VENDA_VARE, VENDA_ATAC FROM AEAPRECO WHERE (ID_AEAPRODU = " + idProduto + ") AND (ID_CFACLIFO = " + idCliente + ")");

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();
                    vistaPrazo = dados.getString(dados.getColumnIndex("FORMA"));
                    calculaJuros = dados.getString(dados.getColumnIndex("CALCULA_JUROS"));
                    vendaVarejoPreco = dados.getDouble(dados.getColumnIndex("VENDA_VARE"));
                    vendaAtacadoPreco = dados.getDouble(dados.getColumnIndex("VENDA_ATAC"));
                }
                // Limpa a memoria
                dados = null;

                sql.setLength(0);
                sql.append("SELECT FORMA, CALCULA_JUROS, VENDA_VARE, VENDA_ATAC FROM AEAPRECO WHERE (ID_AEAPRODU = " + idProduto + ") AND (ID_AEAPLPGT = " + idPlanoPgto + ") AND (ID_CFACLIFO = " + idCliente + ")");

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();
                    vistaPrazo = dados.getString(dados.getColumnIndex("FORMA"));
                    calculaJuros = dados.getString(dados.getColumnIndex("CALCULA_JUROS"));
                    vendaVarejoPreco = dados.getDouble(dados.getColumnIndex("VENDA_VARE"));
                    vendaAtacadoPreco = dados.getDouble(dados.getColumnIndex("VENDA_ATAC"));
                }
                // Limpa a memoria
                dados = null;

                if (vistaPrazo == null){
                    vistaPrazo = "";
                }
                String vistaPrazoPlanoEquivalente = "";

                if ( (vistaPrazo != null) && (vistaPrazo != "") && (!vistaPrazo.isEmpty()) ){
                    sql.setLength(0);
                    sql.append("SELECT ID_AEAPLPGT_EQUIVALENTE, VISTA_PRAZO FROM AEAPLPGT WHERE ID_AEAPLPGT = " + idPlanoPgto);

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();
                        idPlanoPgtoEquivalente = dados.getInt(dados.getColumnIndex("ID_AEAPLPGT_EQUIVALENTE"));
                        vistaPrazoPlanoEquivalente = dados.getString(dados.getColumnIndex("VISTA_PRAZO"));
                    }
                    dados = null;

                    if ((idPlanoPgtoEquivalente != null) && (idPlanoPgtoEquivalente != 0) ){
                        sql.setLength(0);
                        sql.append("SELECT VISTA_PRAZO FROM AEAPLPGT WHERE ID_AEAPLPGT = " + idPlanoPgtoEquivalente);

                        dados = bancoDados.rawQuery(sql.toString(), null);

                        if ((dados != null) && (dados.getCount() > 0)) {
                            dados.moveToFirst();

                            vistaPrazoPlanoEquivalente = dados.getString(dados.getColumnIndex("VISTA_PRAZO"));
                        }
                        dados = null;
                    }
                }
                // if ((:vn_aux1<>0) And (:vs_v_p=:vs_dep)) then vn_preco_vare=:vn_aux1;
                if ( (vendaVarejoPreco != 0) && (vistaPrazoPlanoEquivalente != "") && (vistaPrazo != "") && (vistaPrazo.equalsIgnoreCase(vistaPrazoPlanoEquivalente)) ){ precoVarejo = vendaVarejoPreco; }
                // if ((:vn_aux2<>0) And (:vs_v_p=:vs_dep)) then vn_preco_atac=:vn_aux2;
                if ( (vendaAtacadoPreco != 0) && ((!vistaPrazo.isEmpty()) && (!vistaPrazoPlanoEquivalente.isEmpty()) && (vistaPrazo.equalsIgnoreCase(vistaPrazoPlanoEquivalente))) ){ precoAtacado = vendaAtacadoPreco; }

                if ( (calculaJuros != null) && (calculaJuros.equalsIgnoreCase("0")) ) {
                    if (retorno == null){
                        retorno = new ContentValues();
                    }
                    retorno.put(KEY_PRECO_VAREJO, precoVarejo);
                    retorno.put(KEY_PRECO_ATACADO, precoAtacado);
                    retorno.put(KEY_PRECO_SERVICO, precoServico);
                    retorno.put(KEY_PRODUTO_PROMOCAO_ATACADO, produtoEmPromocaoAtacado);
                    retorno.put(KEY_PRODUTO_PROMOCAO_VAREJO, produtoEmPromocaoVarejo);
                    retorno.put(KEY_PRODUTO_PROMOCAO_SERVICO, produtoEmPromocaoServico);
                    return retorno;
                }

                sql.setLength(0);
                sql.append("SELECT FATOR_CONVERSAO, FATOR_PRECO FROM AEAEMBAL WHERE ID_AEAEMBAL = " + idEmbalagem);

                dados = bancoDados.rawQuery(sql.toString(), null);
                // Checa se retornou alguma coisa do banco de dados
                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();

                    fatorConversao = dados.getDouble(dados.getColumnIndex("FATOR_CONVERSAO"));
                    fatorPreco = dados.getDouble(dados.getColumnIndex("FATOR_PRECO"));
                }
                // Libera memoria
                dados = null;

                precoAtacado = precoAtacado * fatorConversao * fatorPreco;
                precoVarejo = precoVarejo * fatorConversao * fatorPreco;

                if (precoAtacado == 0){precoAtacado = precoVendaAtacado;}
                if (precoVarejo == 0){precoVarejo = precoVendaVarejo;}

                sql.setLength(0);
                sql.append( "SELECT TIPO, ID_AEACLASE, ID_AEAGRUPO, ID_AEASGRUP, ID_AEAFAMIL, ID_AEAMARCA FROM AEAPRODU WHERE ID_AEAPRODU = " + idProduto);

                dados = bancoDados.rawQuery(sql.toString(), null);

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
                    int controle = 0;

                    sql.setLength(0);
                    sql.append("SELECT QUANTIDADE, ID_AEAPRODU_ITEM FROM AEACONJT WHERE AEACONJT.ID_AEAPRODU = " + idProduto);

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)){

                        while(dados.moveToNext()) {

                            int idProdutoItem = dados.getInt(dados.getColumnIndex("ID_AEAPRODU_ITEM"));
                            double quantidadeItemCj = dados.getDouble(dados.getColumnIndex("QUANTIDADE"));
                            int idAeaplojaItemCj = 0;

                            sql.setLength(0);
                            sql.append("SELECT ID_AEAPLOJA FROM AEAPLOJA WHERE (ID_AEAPRODU = " + idProdutoItem + ") AND (ID_SMAEMPRE = " + idEmpresa + ")");

                            Cursor dadosConjunto = bancoDados.rawQuery(sql.toString(), null);

                            if ((dadosConjunto != null) && (dadosConjunto.getCount() > 0)) {
                                dadosConjunto.moveToFirst();

                                idAeaplojaItemCj = dadosConjunto.getInt(dadosConjunto.getColumnIndex("ID_AEAPLOJA"));

                                ContentValues retornoItemConj = execute(idAeaplojaItemCj, idEmbalagem, idPlanoPgto, idCliente, codigoVendedor, dataVenda, 0, 0);

                                if ((retornoItemConj != null) && (retornoItemConj.size() > 0)){
                                    precoVarejo = precoVarejo + (retornoItemConj.getAsDouble(KEY_PRECO_VAREJO) * quantidadeItemCj);
                                    precoAtacado = precoAtacado + (retornoItemConj.getAsDouble(KEY_PRECO_ATACADO) * quantidadeItemCj);
                                    precoServico = precoServico + (retornoItemConj.getAsDouble(KEY_PRECO_SERVICO) * quantidadeItemCj);
                                }
                            }
                        } // Fim while
                    }
                    // Libera memoria
                    dados = null;

                    if (retorno == null){
                        retorno = new ContentValues();
                    }
                    retorno.put(KEY_PRECO_VAREJO, precoVarejo);
                    retorno.put(KEY_PRECO_ATACADO, precoAtacado);
                    retorno.put(KEY_PRECO_SERVICO, precoServico);
                    retorno.put(KEY_PRODUTO_PROMOCAO_ATACADO, produtoEmPromocaoAtacado);
                    retorno.put(KEY_PRODUTO_PROMOCAO_VAREJO, produtoEmPromocaoVarejo);
                    retorno.put(KEY_PRODUTO_PROMOCAO_SERVICO, produtoEmPromocaoServico);
                    return retorno;

                // Checa se o tipo de produto eh servico
                } else if (tipoProduto.equalsIgnoreCase("1")) {
                    precoServico = precoVarejo;
                }
                dataReajusteAtacado = dataVenda;
                dataReajusteVarejo = dataVenda;
                idFator = 0;
                idMoeda = 0;
                markupAtacado = 0;
                markupVarejo = 0;
                descontoMercadoriaVistaAtacado = 0;
                descontoMercadoriaPrazoAtacado = 0;
                descontoMercadoriaVistaVarejo = 0;
                descontoMercadoriaPrazoVarejo = 0;
                descontoMercadoriaVistaServico = 0;
                descontoMercadoriaPrazoServico = 0;

                sql.setLength(0);
                sql.append("SELECT FATOR_VENDA FROM AEAMARCA WHERE ID_AEAMARCA = " + idMarca);

                dados = bancoDados.rawQuery(sql.toString(), null);

                // Checa se voltou alguma coisa
                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();

                    fatorVenda = dados.getDouble(dados.getColumnIndex("FATOR_VENDA"));
                }
                // Checa se tem o fator de venda
                if (fatorVenda == 0){fatorVenda = 1;}

                // Libera memoria
                dados = null;

                // Checa se foi passado por parametro o codigo do vendedor
                if (codigoVendedor != 0){
                    sql.setLength(0);
                    sql.append("SELECT MARKUP_VARE, MARKUP_ATAC \n");
                    sql.append("FROM AEAPERCE \n" );
                    sql.append("WHERE " );
                    sql.append("((ID_AEAPLOJA = " + idProdutoLoja + ") OR (ID_AEAPRODU = " + idProduto + ") OR \n" );
                    sql.append("(ID_AEASGRUP = " + idSubGrupo + ") OR (ID_AEAGRUPO = " + idGrupo + ") OR \n" );
                    sql.append("(ID_AEACLASE = " + idClasse + ") OR (ID_AEAFAMIL = " + idFamilia + ") OR \n" );
                    sql.append("(ID_AEAMARCA = " + idMarca + ")) AND (ID_CFAPARAM_VENDEDOR = (SELECT CFACLIFO.ID_CFACLIFO FROM CFACLIFO WHERE CFACLIFO.CODIGO_FUN = " + codigoVendedor + ") )" );
                    sql.append("ORDER BY COALESCE(ID_AEAPLOJA, ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_SMAEMPRE) \n");

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)) {
                        // Passa por todos os registros
                        while (dados.moveToNext()) {
                            if (markupAtacado == 0) {markupAtacado = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC")); }
                            if (markupVarejo == 0) {markupVarejo = dados.getDouble(dados.getColumnIndex("MARKUP_VARE")); }
                        }
                    }
                    dados = null;
                }
                sql.setLength(0);
                sql.append("SELECT ID_AEAPERCE, ID_AEATBPER_TABELA, ID_AEAFATOR, ID_CFAMOEDA, MARKUP_VARE, MARKUP_ATAC, ");
                sql.append("DESC_MERC_VISTA_VARE, DESC_MERC_VISTA_ATAC, DESC_MERC_PRAZO_VARE, DESC_MERC_PRAZO_ATAC, DESC_SERV_VISTA, DESC_SERV_PRAZO \n");
                sql.append("FROM AEAPERCE \n");
                sql.append("WHERE ((ID_AEAPLOJA = "+ idProdutoLoja +") OR (ID_AEAPRODU = " + idProduto + ") OR \n");
                sql.append("(ID_AEASGRUP = "+ idSubGrupo +") OR (ID_AEAGRUPO = "+ idGrupo +") OR \n");
                sql.append("(ID_AEACLASE = "+ idClasse +") OR (ID_AEAFAMIL = "+ idFamilia +") OR \n");
                sql.append("(ID_AEAMARCA = "+ idMarca +") OR (ID_SMAEMPRE = "+ idEmpresa +") OR \n");
                sql.append("((ID_AEAAGPPR IS NOT NULL) AND (ID_AEAAGPPR IN \n");
                sql.append("  (SELECT ID_AEAAGPPR \n");
                sql.append("   FROM AEAITGPR \n");
                sql.append("   WHERE ("+ idProduto +" = AEAITGPR.ID_AEAPRODU) OR ("+ idMarca +" = AEAITGPR.ID_AEAMARCA) \n");
                sql.append("   OR ("+ idSubGrupo +" = AEAITGPR.ID_AEASGRUP) OR ("+ idGrupo +" = AEAITGPR.ID_AEAGRUPO) ");
                sql.append("   OR ("+ idClasse +" = AEAITGPR.ID_AEACLASE) \n");
                sql.append("   OR ("+ idFamilia +" = AEAITGPR.ID_AEAFAMIL))))) \n");
                sql.append("AND ((ID_CFAPARAM_VENDEDOR IS NULL) OR (ID_CFAPARAM_VENDEDOR = 0)) \n");
                sql.append("ORDER BY COALESCE(ID_AEAPLOJA, ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_AEAAGPPR, ID_SMAEMPRE)");

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    // Passa por todos os registro buscado no banco
                    while (dados.moveToNext()){
                        Integer idPercentualTabela = dados.getInt(dados.getColumnIndex("ID_AEATBPER_TABELA"));
                        Integer idPercentual = dados.getInt(dados.getColumnIndex("ID_AEAPERCE"));
                        //idFatorTemp = dados.getInt(dados.getColumnIndex("ID_AEAFATOR"));
                        if ((idFator == 0) && (dados.getString(dados.getColumnIndex("ID_AEAFATOR")) != null) && (!dados.getString(dados.getColumnIndex("ID_AEAFATOR")).isEmpty()) && (dados.getInt(dados.getColumnIndex("ID_AEAFATOR")) != 0)){idFator = dados.getInt(dados.getColumnIndex("ID_AEAFATOR"));}
                        if ((idMoeda == 0) && (dados.getString(dados.getColumnIndex("ID_CFAMOEDA")) != null) && (!dados.getString(dados.getColumnIndex("ID_CFAMOEDA")).isEmpty()) && (dados.getInt(dados.getColumnIndex("ID_CFAMOEDA")) != 0)){idMoeda = dados.getInt(dados.getColumnIndex("ID_CFAMOEDA"));}
                        if (markupAtacado == 0) {markupAtacado = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC")); }
                        if (markupVarejo == 0) {markupVarejo = dados.getDouble(dados.getColumnIndex("MARKUP_VARE")); }
                        if (descontoMercadoriaVistaVarejo == 0) {descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_MERC_VISTA_VARE")); }
                        if (descontoMercadoriaVistaAtacado == 0) {descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_MERC_VISTA_ATAC")); }
                        if (descontoMercadoriaVistaServico == 0) {descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA")); }
                        if (descontoMercadoriaPrazoVarejo == 0) {descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_MERC_PRAZO_VARE")); }
                        if (descontoMercadoriaPrazoAtacado == 0) {descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_MERC_PRAZO_ATAC")); }
                        if (descontoMercadoriaPrazoServico == 0) {descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO")); }

                        if (dados.getInt(dados.getColumnIndex("ID_AEATBPER_TABELA")) > 0){

                            StringBuilder sqlTemp = new StringBuilder();
                            sqlTemp.append( "SELECT MARKUP_VARE, MARKUP_ATAC, DESC_MERC_VISTA_VARE, DESC_MERC_VISTA_ATAC, DESC_MERC_PRAZO_VARE, DESC_MERC_PRAZO_ATAC, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                                            "FROM AEAPERCE \n" +
                                            "WHERE ID_AEAPERCE = (SELECT ID_AEAPERCE FROM AEAPERCE WHERE ID_AEATBPER = " + dados.getInt(dados.getColumnIndex("ID_AEATBPER_TABELA")) + ") ");
                            Cursor dadosPercentualTemp = bancoDados.rawQuery(sqlTemp.toString(), null);

                            if ((dadosPercentualTemp != null) && (dadosPercentualTemp.getCount() > 0)){
                                // Move para o primeiro registro
                                dadosPercentualTemp.moveToFirst();

                                if (markupAtacado == 0) {markupAtacado = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("MARKUP_ATAC")); }
                                if (markupVarejo == 0) {markupVarejo = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("MARKUP_VARE")); }
                                if (descontoMercadoriaVistaVarejo == 0) {descontoMercadoriaVistaVarejo = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_VISTA_VARE")); }
                                if (descontoMercadoriaVistaAtacado == 0) {descontoMercadoriaVistaAtacado = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_VISTA_ATAC")); }
                                if (descontoMercadoriaVistaServico == 0) {descontoMercadoriaVistaServico = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_SERV_VISTA")); }
                                if (descontoMercadoriaPrazoAtacado == 0) {descontoMercadoriaPrazoAtacado = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_PRAZO_ATAC")); }
                                if (descontoMercadoriaPrazoVarejo == 0) {descontoMercadoriaPrazoVarejo = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_MERC_PRAZO_VARE")); }
                                if (descontoMercadoriaPrazoServico == 0) {descontoMercadoriaPrazoServico = dadosPercentualTemp.getDouble(dadosPercentualTemp.getColumnIndex("DESC_SERV_PRAZO")); }
                            }
                        }
                    }
                    dados = null;
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
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            new MaterialDialog.Builder(context)
                                    .title("CalculaPrecoSP")
                                    .content(context.getResources().getString(R.string.erro_sem_fator_preco))
                                    .positiveText(R.string.button_ok)
                                    .show();
                        }
                    });
                    return retorno;
                }
                if (idCliente != 0){
                    sql.setLength(0);
                    sql.append("SELECT ID_CFAATIVI, ID_CFAPROFI, ID_CFATPCLI, ID_CFASTATU, ID_CFAAREAS \n" +
                            "   FROM CFACLIFO \n" +
                            "   WHERE ID_CFACLIFO = " + idCliente);

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)) {
                        // Passa por todos os registros
                        dados.moveToNext();

                        idAtividade = dados.getInt(dados.getColumnIndex("ID_CFAATIVI"));
                        idProfissao = dados.getInt(dados.getColumnIndex("ID_CFAPROFI"));
                        idTipoCliente = dados.getInt(dados.getColumnIndex("ID_CFATPCLI"));
                        idStatus = dados.getInt(dados.getColumnIndex("ID_CFASTATU"));
                        idArea = dados.getInt(dados.getColumnIndex("ID_CFAAREAS"));
                    }
                }
                // Aplica o markup
                if (markupVarejo != 0){ precoVarejo = precoVarejo * markupVarejo; }
                if (markupAtacado != 0){ precoAtacado = precoAtacado * markupAtacado; }
                // Aplica o fator de venda
                precoVarejo = precoVarejo * fatorVenda;
                precoAtacado = precoAtacado * fatorVenda;
                precoServico = precoServico * fatorVenda;

                produtoEmPromocaoAtacado = "0";
                produtoEmPromocaoVarejo = "0";
                produtoEmPromocaoServico = "0";
                idPlanoPgtoEquivalente = 0;
                descontoPromocao = "1";
                vistaPrazo = "0";
                origemValor = "0";
                percentualDescontoAtacado = 0;
                percentualDescontoVarejo = 0;
                percentualDescontoServico = 0;

                dados = null;
                sql.setLength(0);
                sql.append( "SELECT JURO_MEDIO_ATAC, JURO_MEDIO_VARE, JURO_MEDIO_SERV, DESC_MAX_PLANO_ATAC_VISTA, \n" +
                            "DESC_MAX_PLANO_ATAC_PRAZO, DESC_MAX_PLANO_VARE_VISTA, DESC_MAX_PLANO_VARE_PRAZO, \n" +
                            "DESC_MAX_PLANO_SERV_VISTA, DESC_MAX_PLANO_SERV_PRAZO, TIPO_BONUS, DIAS_BONUS \n" +
                            "FROM AEAFATOR \n" +
                            "WHERE ID_AEAFATOR = " + idFator);

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    // Move o curso para o primeiro registro
                    dados.moveToFirst();

                    jurosMedioFatorAtacado = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_ATAC"));
                    jurosMedioFatorVarejo = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_VARE"));
                    jurosMedioFatorServico = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_SERV"));
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
                    if (    ((descontoMercadoriaVistaServico != 0) && (descontoMaximoPlanoAtacadoVista == 0)) ||
                            ((descontoMercadoriaVistaServico < descontoMaximoPlanoAtacadoVista) && (descontoMercadoriaVistaServico != 0)) ){
                        descontoMaximoPlanoServicoVista = descontoMercadoriaVistaServico;
                    }
                    if (    ((descontoMercadoriaPrazoServico != 0) && (descontoMaximoPlanoAtacadoPrazo == 0)) ||
                            ((descontoMercadoriaPrazoServico < descontoMaximoPlanoAtacadoPrazo) && (descontoMercadoriaPrazoServico != 0)) ){
                        descontoMaximoPlanoServicoPrazo = descontoMercadoriaPrazoServico;
                    }
                    descontoMercadoriaVistaVarejo = 0;
                    descontoMercadoriaPrazoVarejo = 0;
                    descontoMercadoriaVistaAtacado = 0;
                    descontoMercadoriaPrazoAtacado = 0;
                }
                dados = null;

                sql.setLength(0);
                sql.append( "SELECT ID_AEAPLPGT_EQUIVALENTE, DESC_PROMOCAO, VISTA_PRAZO, " +
                            "PERC_DESC_ATAC, PERC_DESC_VARE, PERC_DESC_SERV, " +
                            "QTDE_PARCELAS1, DIAS_PARCELAS1, QTDE_PARCELAS2, DIAS_PARCELAS2, QTDE_PARCELAS3, DIAS_PARCELAS3, PERC_ENTRADA, DIAS_ENTRADA, " +
                            "ORIGEM_VALOR, JURO_MEDIO_ATAC, JURO_MEDIO_VARE, JURO_MEDIO_SERV, JURO_MEDIO_LOCAL \n" +
                            "FROM AEAPLPGT \n" +
                            "WHERE AEAPLPGT.ID_AEAPLPGT = " + idPlanoPgto);

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();

                    descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                    vistaPrazo = dados.getString(dados.getColumnIndex("VISTA_PRAZO"));
                    percentualDescontoAtacado = dados.getDouble(dados.getColumnIndex("PERC_DESC_ATAC"));
                    percentualDescontoVarejo = dados.getDouble(dados.getColumnIndex("PERC_DESC_VARE"));
                    percentualDescontoServico = dados.getDouble(dados.getColumnIndex("PERC_DESC_SERV"));
                    qtdeParcelas1 = dados.getInt(dados.getColumnIndex("QTDE_PARCELAS1"));
                    diasParcela1 = dados.getInt(dados.getColumnIndex("DIAS_PARCELAS1"));
                    qtdeParcelas2 = dados.getInt(dados.getColumnIndex("QTDE_PARCELAS2"));
                    diasParcela2 = dados.getInt(dados.getColumnIndex("DIAS_PARCELAS2"));
                    qtdeParcelas3 = dados.getInt(dados.getColumnIndex("QTDE_PARCELAS3"));
                    diasParcela3 = dados.getInt(dados.getColumnIndex("DIAS_PARCELAS3"));
                    percentualEntrada = dados.getInt(dados.getColumnIndex("PERC_ENTRADA"));
                    diasEntrada = dados.getInt(dados.getColumnIndex("DIAS_ENTRADA"));
                    origemValor = dados.getString(dados.getColumnIndex("ORIGEM_VALOR"));
                    jurosMedioPlPgtAtacado = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_ATAC"));
                    jurosMedioPlPgtVarejo = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_VARE"));
                    jurosMedioPlPgtServico = dados.getDouble(dados.getColumnIndex("JURO_MEDIO_SERV"));
                    usaJurosMedioLocal = dados.getString(dados.getColumnIndex("JURO_MEDIO_LOCAL"));

                    if(dados.getInt(dados.getColumnIndex("ID_AEAPLPGT_EQUIVALENTE")) > 0){
                        StringBuilder sqlTemp = new StringBuilder();
                        sqlTemp.append( "SELECT DESC_PROMOCAO, VISTA_PRAZO, PERC_DESC_ATAC, PERC_DESC_VARE, PERC_DESC_SERV, " +
                                        "QTDE_PARCELAS1, DIAS_PARCELAS1, QTDE_PARCELAS2, DIAS_PARCELAS2, QTDE_PARCELAS3, DIAS_PARCELAS3, PERC_ENTRADA, DIAS_ENTRADA, " +
                                        "ORIGEM_VALOR, JURO_MEDIO_ATAC, JURO_MEDIO_VARE, JURO_MEDIO_SERV, JURO_MEDIO_LOCAL \n" +
                                        "FROM AEAPLPGT \n" +
                                        "WHERE AEAPLPGT.ID_AEAPLPGT = " + dados.getInt(dados.getColumnIndex("ID_AEAPLPGT_EQUIVALENTE")));

                        Cursor dadosPlPgto = bancoDados.rawQuery(sqlTemp.toString(), null);

                        if((dadosPlPgto != null) && (dadosPlPgto.getCount() > 0)){
                            // Move o cursor para o primeiro registro
                            dadosPlPgto.moveToFirst();

                            descontoPromocao = dadosPlPgto.getString(dadosPlPgto.getColumnIndex("DESC_PROMOCAO"));
                            vistaPrazo = dadosPlPgto.getString(dadosPlPgto.getColumnIndex("VISTA_PRAZO"));
                            percentualDescontoAtacado = dadosPlPgto.getDouble(dadosPlPgto.getColumnIndex("PERC_DESC_ATAC"));
                            percentualDescontoVarejo = dadosPlPgto.getDouble(dadosPlPgto.getColumnIndex("PERC_DESC_VARE"));
                            percentualDescontoServico = dadosPlPgto.getDouble(dadosPlPgto.getColumnIndex("PERC_DESC_SERV"));
                            qtdeParcelas1 = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("QTDE_PARCELAS1"));
                            diasParcela1 = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("DIAS_PARCELAS1"));
                            qtdeParcelas2 = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("QTDE_PARCELAS2"));
                            diasParcela2 = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("DIAS_PARCELAS2"));
                            qtdeParcelas3 = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("QTDE_PARCELAS3"));
                            diasParcela3 = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("DIAS_PARCELAS3"));
                            percentualEntrada = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("PERC_ENTRADA"));
                            diasEntrada = dadosPlPgto.getInt(dadosPlPgto.getColumnIndex("DIAS_ENTRADA"));
                            origemValor = dadosPlPgto.getString(dadosPlPgto.getColumnIndex("ORIGEM_VALOR"));
                            jurosMedioPlPgtAtacado = dadosPlPgto.getDouble(dadosPlPgto.getColumnIndex("JURO_MEDIO_ATAC"));
                            jurosMedioPlPgtVarejo = dadosPlPgto.getDouble(dadosPlPgto.getColumnIndex("JURO_MEDIO_VARE"));
                            jurosMedioPlPgtServico = dadosPlPgto.getDouble(dadosPlPgto.getColumnIndex("JURO_MEDIO_SERV"));
                            usaJurosMedioLocal = dadosPlPgto.getString(dadosPlPgto.getColumnIndex("JURO_MEDIO_LOCAL"));
                        }
                    }
                }
                // Limpa a variavel
                dados = null;
                // Verifica se a origem de preço eh diferente do Preço de Venda
                if (!origemValor.equalsIgnoreCase("0")){
                    // Custo Reposição
                    if (origemValor.equalsIgnoreCase("1")){
                        precoVarejo = custoReposicao * fatorConversao * fatorPreco;
                    // Custo Real
                    } else if (origemValor.equalsIgnoreCase("2")){
                        precoVarejo = custoReal * fatorConversao * fatorPreco;
                    // Custo Médio
                    } else if (origemValor.equalsIgnoreCase("3")){
                        precoVarejo = custoMedio * fatorConversao;
                    }
                    precoAtacado = precoVarejo;
                    precoServico = precoVarejo;
                    if (retorno == null){
                        retorno = new ContentValues();
                    }
                    retorno.put(KEY_PRECO_VAREJO, precoVarejo);
                    retorno.put(KEY_PRECO_ATACADO, precoAtacado);
                    retorno.put(KEY_PRECO_SERVICO, precoServico);
                    retorno.put(KEY_PRODUTO_PROMOCAO_ATACADO, produtoEmPromocaoAtacado);
                    retorno.put(KEY_PRODUTO_PROMOCAO_VAREJO, produtoEmPromocaoVarejo);
                    retorno.put(KEY_PRODUTO_PROMOCAO_SERVICO, produtoEmPromocaoServico);
                    return retorno;
                }
                Integer diasTemp = 0, diasSomados;
                Double diasMultiplicadoTemp;

                if (diasEntrada == null){
                    // Cria uma notificacao para ser manipulado
                    bigTextStyle.bigText(context.getResources().getString(R.string.falta_marcar_plano_pagamento) + "\n ID_AEAPLPGT = " + idPlanoPgto);
                    mBuilder.setStyle(bigTextStyle);
                    notificationManager.notify(ConfiguracoesInternas.IDENTIFICACAO_NOTIFICACAO_CALCULA_PRECO_SP, mBuilder.build());

                    if (retorno == null){
                        retorno = new ContentValues();
                    }
                    retorno.put(KEY_PRECO_VAREJO, precoVarejo);
                    retorno.put(KEY_PRECO_ATACADO, precoAtacado);
                    retorno.put(KEY_PRECO_SERVICO, precoServico);
                    retorno.put(KEY_PRODUTO_PROMOCAO_ATACADO, produtoEmPromocaoAtacado);
                    retorno.put(KEY_PRODUTO_PROMOCAO_VAREJO, produtoEmPromocaoVarejo);
                    retorno.put(KEY_PRODUTO_PROMOCAO_SERVICO, produtoEmPromocaoServico);
                    return retorno;
                }

                if (percentualEntrada != 0){diasTemp = diasEntrada;}
                diasMultiplicadoTemp = percentualEntrada * diasEntrada;
                if ((qtdeParcelas1 + qtdeParcelas2 + qtdeParcelas3) > 0){percentualEntrada = ((100 - percentualEntrada) / (qtdeParcelas1 + qtdeParcelas2 + qtdeParcelas3));}

                diasSomados = diasTemp;
                while (qtdeParcelas1 > 0){
                    diasMultiplicadoTemp = diasMultiplicadoTemp + ((diasTemp + (qtdeParcelas1 * diasParcela1)) * percentualEntrada);
                    diasSomados = diasSomados + (qtdeParcelas1 * diasParcela1);
                    qtdeParcelas1 = qtdeParcelas1 - 1;
                }
                diasTemp = diasSomados;

                while (qtdeParcelas2 > 0){
                    diasMultiplicadoTemp = diasMultiplicadoTemp + ((diasTemp + (qtdeParcelas2 * diasParcela2)) * percentualEntrada);
                    diasSomados = diasSomados + (qtdeParcelas2 * diasParcela2);
                    qtdeParcelas2 = qtdeParcelas2 - 1;
                }
                diasTemp = diasSomados;

                while (qtdeParcelas3 > 0){
                    diasMultiplicadoTemp = diasMultiplicadoTemp + ((diasTemp + (qtdeParcelas3 * diasParcela3)) * percentualEntrada);
                    diasSomados = diasSomados + (qtdeParcelas3 * diasParcela3);
                    qtdeParcelas3 = qtdeParcelas3 - 1;
                }
                diasMultiplicadoTemp = (diasMultiplicadoTemp / 100);

                sql.setLength(0);
                sql.append( "SELECT ID_AEATBPRO \n" +
                            "FROM AEATBPRO \n" +
                            "WHERE (ATIVO = '1') AND ((VISTA_PRAZO = " + vistaPrazo + ") OR (VISTA_PRAZO = '2')) \n" +
                            "AND (DT_INICIO <= '" + dataVenda + "') AND (DT_FIM >= '" + dataVenda + "') \n" +
                            "AND ((DIAS = '') OR (DIAS IS NULL) OR (DIAS LIKE '%' || (STRFTIME('%w', '" + dataVenda + "')) || '%')) \n" +
                            "AND (" + idEmpresa + " IN (SELECT ID_SMAEMPRE FROM AEAEMTBP WHERE AEAEMTBP.ID_AEATBPRO = AEATBPRO.ID_AEATBPRO AND AEAEMTBP.ID_SMAEMPRE = " + idEmpresa + "))");

                dados = bancoDados.rawQuery(sql.toString(), null);

                if ((dados != null) && (dados.getCount() > 0)){
                    while (dados.moveToNext()){

                        idItemTabelaPromocao = null;
                        idItemTabelaPromocaoExcesao = null;

                        StringBuilder sqlTemp = new StringBuilder();
                        sqlTemp.append( "SELECT ID_AEAITTBP, DESC_MERC_VISTA_ATAC, DESC_MERC_VISTA_VARE, DESC_MERC_PRAZO_ATAC, DESC_MERC_PRAZO_VARE, \n" +
                                        "DESC_SERV_VISTA, DESC_SERV_PRAZO, PRECO_VISTA_VARE, PRECO_VISTA_ATAC, PRECO_PRAZO_VARE, \n" +
                                        "PRECO_PRAZO_ATAC, PRECO_VISTA_SERV, PRECO_PRAZO_SERV \n" +
                                        "FROM AEAITTBP \n" +
                                        "WHERE (ID_AEATBPRO = " + dados.getInt(dados.getColumnIndex("ID_AEATBPRO")) + ") AND \n" +
                                        "((" + idProduto + " = ID_AEAPRODU) OR (" + idMarca + " = ID_AEAMARCA) \n" +
                                        "OR (" + idSubGrupo + " = ID_AEASGRUP) OR (" + idGrupo + " = ID_AEAGRUPO) OR (" + idClasse + " = ID_AEACLASE) \n" +
                                        "OR (" + idFamilia + " = ID_AEAFAMIL) \n" +
                                        "OR ((ID_AEAAGPPR IS NOT NULL) AND (ID_AEAAGPPR IN " +
                                        "(SELECT ID_AEAAGPPR \n" +
                                        "FROM AEAITGPR \n" +
                                        "WHERE (" + idProduto + " = AEAITGPR.ID_AEAPRODU) OR (" + idMarca + " = AEAITGPR.ID_AEAMARCA) \n" +
                                        "OR (" + idSubGrupo + " = AEAITGPR.ID_AEASGRUP) OR (" + idGrupo + " = AEAITGPR.ID_AEAGRUPO) OR (" + idClasse + " = AEAITGPR.ID_AEACLASE) \n" +
                                        "OR (" + idFamilia + " = AEAITGPR.ID_AEAFAMIL))))) \n" +
                                        "ORDER BY COALESCE(ID_AEAPRODU, ID_AEASGRUP, ID_AEAGRUPO, ID_AEACLASE, ID_AEAFAMIL, ID_AEAMARCA, ID_AEAAGPPR) LIMIT 1 ");

                        Cursor dadosItemPromo = bancoDados.rawQuery(sqlTemp.toString(), null);
                        if ((dadosItemPromo != null) && (dadosItemPromo.getCount() > 0)){
                            dadosItemPromo.moveToFirst();

                            idItemTabelaPromocao = dadosItemPromo.getInt(dadosItemPromo.getColumnIndex("ID_AEAITTBP"));
                            descontoMercadoriaVistaAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_VISTA_ATAC"));
                            descontoMercadoriaVistaVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_VISTA_VARE"));
                            descontoMercadoriaPrazoAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_PRAZO_ATAC"));
                            descontoMercadoriaPrazoVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_PRAZO_VARE"));
                            descontoMercadoriaVistaServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_SERV_VISTA"));
                            descontoMercadoriaPrazoServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_SERV_PRAZO"));
                            precoPromocaoVistaVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_VISTA_VARE"));
                            precoPromocaoVistaAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_VISTA_ATAC"));
                            precoPromocaoPrazoVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_PRAZO_VARE"));
                            precoPromocaoPrazoAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_PRAZO_ATAC"));
                            precoPromocaoVistaServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_VISTA_SERV"));
                            precoPromocaoPrazoServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("PRECO_PRAZO_SERV"));
                        }
                        dadosItemPromo = null;

                        // Verifica se retornou nulo
                        idItemTabelaPromocao = ((idItemTabelaPromocao == null) ? 0 : idItemTabelaPromocao);

                        if (idItemTabelaPromocao != 0){
                            sqlTemp.setLength(0);
                            sqlTemp.append( "SELECT ID_AEAEXTBP, ID_CFATPCLI, ID_CFAPROFI, ID_CFAAREAS, ID_CFASTATU, ID_CFAATIVI \n" +
                                            "FROM AEAEXTBP \n" +
                                            "WHERE (ID_AEAITTBP = " + idItemTabelaPromocao + ") AND ((ID_CFAPROFI = " + (idProfissao != null ? idProfissao : 0) + ") OR (ID_CFAATIVI = " + (idAtividade != null ? idAtividade : 0) + ") \n" +
                                            "OR (ID_CFATPCLI = " + (idTipoCliente != null ? idTipoCliente : 0) + ") OR (ID_CFAAREAS = " + (idArea != null ? idArea : 0) + ") OR (ID_CFASTATU = " + (idStatus != null ? idStatus : 0) + "))");

                            Cursor dadosExcao = bancoDados.rawQuery(sqlTemp.toString(), null);

                            if ((dadosExcao != null) && (dadosExcao.getCount() > 0)){
                                while (dadosExcao.moveToNext()){
                                    Integer somaTemp = 0;
                                    if (dadosExcao.getInt(dadosExcao.getColumnIndex("ID_CFATPCLI")) == idTipoCliente){ somaTemp += 1;}
                                    if (dadosExcao.getInt(dadosExcao.getColumnIndex("ID_CFAPROFI")) == idTipoCliente){ somaTemp += 1;}
                                    if (dadosExcao.getInt(dadosExcao.getColumnIndex("ID_CFAAREAS")) == idTipoCliente){ somaTemp += 1;}
                                    if (dadosExcao.getInt(dadosExcao.getColumnIndex("ID_CFASTATU")) == idTipoCliente){ somaTemp += 1;}
                                    if (dadosExcao.getInt(dadosExcao.getColumnIndex("ID_CFAATIVI")) == idTipoCliente){ somaTemp += 1;}
                                    if (somaTemp > 0){
                                        idItemTabelaPromocaoExcesao = dadosExcao.getInt(dadosExcao.getColumnIndex("ID_AEAEXTBP"));
                                    }
                                }
                            }
                            // Verifica se a excessao de promossao retornou alguma coisa
                            idItemTabelaPromocaoExcesao = ((idItemTabelaPromocaoExcesao == null) ? 0 : idItemTabelaPromocaoExcesao);

                            if (idItemTabelaPromocaoExcesao != 0){
                                sqlTemp.setLength(0);
                                sqlTemp.append( "SELECT DESC_MERC_VISTA_ATAC, DESC_MERC_VISTA_VARE, DESC_MERC_PRAZO_ATAC, DESC_MERC_PRAZO_VARE, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                                                "FROM AEAEXTBP \n" +
                                                "WHERE (ID_AEAEXTBP = " + idItemTabelaPromocaoExcesao + ")");

                                dadosItemPromo = bancoDados.rawQuery(sqlTemp.toString(), null);

                                if ((dadosItemPromo != null) && (dadosItemPromo.getCount() > 0)){
                                    dadosItemPromo.moveToFirst();

                                    descontoMercadoriaVistaAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_VISTA_ATAC"));
                                    descontoMercadoriaVistaVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_VISTA_VARE"));
                                    descontoMercadoriaPrazoAtacado = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_PRAZO_ATAC"));
                                    descontoMercadoriaPrazoVarejo = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_MERC_PRAZO_VARE"));
                                    descontoMercadoriaVistaServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_SERV_VISTA"));
                                    descontoMercadoriaPrazoServico = dadosItemPromo.getDouble(dadosItemPromo.getColumnIndex("DESC_SERV_PRAZO"));
                                }
                            }
                        }
                    } // Fim While
                }
                dados = null;

                if (vistaPrazo.equalsIgnoreCase("0")){
                    if (precoPromocaoVistaAtacado != 0){
                        precoAtacado = precoPromocaoVistaAtacado * fatorConversao * fatorPreco;
                        if (tipoProduto != "1"){ produtoEmPromocaoAtacado = "1";}
                    }
                    if (precoPromocaoVistaVarejo != 0){
                        precoVarejo = precoPromocaoVistaVarejo * fatorConversao * fatorPreco;
                        if (tipoProduto != "1"){ produtoEmPromocaoVarejo = "1";}
                    }
                    if (precoPromocaoVistaServico != 0){
                        precoAtacado = precoPromocaoVistaServico * fatorConversao * fatorPreco;
                        if (tipoProduto.equalsIgnoreCase("1")){ produtoEmPromocaoServico = "1";}
                    }
                } else {
                    if (precoPromocaoPrazoAtacado != 0){
                        precoAtacado = precoPromocaoPrazoAtacado * fatorConversao * fatorPreco;
                        if (tipoProduto != "1"){ produtoEmPromocaoAtacado = "1";}
                    }
                    if (precoPromocaoPrazoVarejo != 0){
                        precoVarejo = precoPromocaoPrazoVarejo * fatorConversao * fatorPreco;
                        if (tipoProduto != "1"){ produtoEmPromocaoVarejo = "1";}
                    }
                    if (precoPromocaoPrazoServico != 0){
                        precoAtacado = precoPromocaoPrazoServico * fatorConversao * fatorPreco;
                        if (tipoProduto.equalsIgnoreCase("1")){ produtoEmPromocaoServico = "1";}
                    }
                }
                if ((descontoMercadoriaVistaAtacado + descontoMercadoriaVistaVarejo + descontoMercadoriaPrazoAtacado + descontoMercadoriaPrazoVarejo + descontoMercadoriaVistaServico + descontoMercadoriaPrazoServico) > 0){
                    if (vistaPrazo.equalsIgnoreCase("0")){
                        if ((tipoProduto != "1") && (descontoMercadoriaVistaAtacado != 0)){ produtoEmPromocaoAtacado = "1"; }
                        if ((tipoProduto != "1") && (descontoMercadoriaVistaVarejo != 0)){ produtoEmPromocaoVarejo = "1"; }
                        if ((tipoProduto.equalsIgnoreCase("1")) && (descontoMercadoriaVistaServico != 0)){ produtoEmPromocaoServico = "1"; }
                        precoVarejo = precoVarejo * (1- (descontoMercadoriaVistaVarejo / 100));
                        precoAtacado = precoAtacado * (1- (descontoMercadoriaVistaAtacado / 100));
                        precoServico = precoServico * (1- (descontoMercadoriaVistaServico / 100));
                    } else {
                        if ((tipoProduto != "1") && (descontoMercadoriaPrazoAtacado != 0)){ produtoEmPromocaoAtacado = "1"; }
                        if ((tipoProduto != "1") && (descontoMercadoriaPrazoVarejo != 0)){ produtoEmPromocaoVarejo = "1"; }
                        if ((tipoProduto.equalsIgnoreCase("1")) && (descontoMercadoriaPrazoServico != 0)){ produtoEmPromocaoServico = "1"; }
                        precoVarejo = precoVarejo * (1- (descontoMercadoriaPrazoVarejo / 100));
                        precoAtacado = precoAtacado * (1- (descontoMercadoriaPrazoAtacado / 100));
                        precoServico = precoServico * (1- (descontoMercadoriaPrazoServico / 100));
                    }
                }
                ep_inc_desc_preco();

                sql.setLength(0);
                sql.append( "SELECT ID_CFAPARAM, DESC_PROMOCAO, DESC_ATAC_VISTA, DESC_ATAC_PRAZO, DESC_VARE_VISTA, DESC_VARE_PRAZO, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                            "FROM CFAPARAM \n" +
                            "WHERE (ID_CFACLIFO = " + idCliente + ") AND (ID_SMAEMPRE = " + idEmpresa + ")");

                dados = bancoDados.rawQuery(sql.toString(), null);
                if ( (dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();

                    if (dados.getInt(dados.getColumnIndex("ID_CFAPARAM")) > 0){
                        descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                        descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_VISTA"));
                        descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_PRAZO"));
                        descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_VISTA"));
                        descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_PRAZO"));
                        descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA"));
                        descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO"));

                        ep_inc_desc_preco();
                    }
                }
                // Limpa os dados
                dados = null;
                if (idAtividade != null && idAtividade != 0){
                    sql.setLength(0);
                    sql.append( "SELECT DESC_PROMOCAO, DESC_ATAC_VISTA, DESC_ATAC_PRAZO, DESC_VARE_VISTA, DESC_VARE_PRAZO, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                                "FROM CFAATIVI \n" +
                                "WHERE (ID_CFAATIVI = " + idAtividade + ")");

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ( (dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                        descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_VISTA"));
                        descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_PRAZO"));
                        descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_VISTA"));
                        descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_PRAZO"));
                        descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA"));
                        descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO"));

                        ep_inc_desc_preco();
                    }
                    // Limpa os dados
                    dados = null;
                }
                if (idProfissao != null && idProfissao != 0){
                    sql.setLength(0);
                    sql.append( "SELECT DESC_PROMOCAO, DESC_ATAC_VISTA, DESC_ATAC_PRAZO, DESC_VARE_VISTA, DESC_VARE_PRAZO, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                                "FROM CFAPROFI \n" +
                                "WHERE (ID_CFAPROFI = " + idProfissao + ")");

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ( (dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                        descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_VISTA"));
                        descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_PRAZO"));
                        descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_VISTA"));
                        descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_PRAZO"));
                        descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA"));
                        descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO"));

                        ep_inc_desc_preco();
                    }
                    // Limpa os dados
                    dados = null;
                }
                if (idTipoCliente != null && idTipoCliente != 0){
                    sql.setLength(0);
                    sql.append( "SELECT DESC_PROMOCAO, DESC_ATAC_VISTA, DESC_ATAC_PRAZO, DESC_VARE_VISTA, DESC_VARE_PRAZO, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                                "FROM CFATPCLI \n" +
                                "WHERE (ID_CFATPCLI = " + idTipoCliente + ")");

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ( (dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                        descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_VISTA"));
                        descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_PRAZO"));
                        descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_VISTA"));
                        descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_PRAZO"));
                        descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA"));
                        descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO"));

                        ep_inc_desc_preco();
                    }
                    // Limpa os dados
                    dados = null;
                }
                if (idStatus != null && idStatus != 0){
                    sql.setLength(0);
                    sql.append( "SELECT DESC_PROMOCAO, DESC_ATAC_VISTA, DESC_ATAC_PRAZO, DESC_VARE_VISTA, DESC_VARE_PRAZO, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                                "FROM CFASTATU \n" +
                                "WHERE (ID_CFASTATU = " + idStatus + ")");

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ( (dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                        descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_VISTA"));
                        descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_PRAZO"));
                        descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_VISTA"));
                        descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_PRAZO"));
                        descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA"));
                        descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO"));

                        ep_inc_desc_preco();
                    }
                    // Limpa os dados
                    dados = null;
                }
                if (idArea != null && idArea != 0){
                    sql.setLength(0);
                    sql.append( "SELECT DESC_PROMOCAO, DESC_ATAC_VISTA, DESC_ATAC_PRAZO, DESC_VARE_VISTA, DESC_VARE_PRAZO, DESC_SERV_VISTA, DESC_SERV_PRAZO \n" +
                            "FROM CFAAREAS \n" +
                            "WHERE (ID_CFAAREAS = " + idArea + ")");

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ( (dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        descontoPromocao = dados.getString(dados.getColumnIndex("DESC_PROMOCAO"));
                        descontoMercadoriaVistaAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_VISTA"));
                        descontoMercadoriaPrazoAtacado = dados.getDouble(dados.getColumnIndex("DESC_ATAC_PRAZO"));
                        descontoMercadoriaVistaVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_VISTA"));
                        descontoMercadoriaPrazoVarejo = dados.getDouble(dados.getColumnIndex("DESC_VARE_PRAZO"));
                        descontoMercadoriaVistaServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_VISTA"));
                        descontoMercadoriaPrazoServico = dados.getDouble(dados.getColumnIndex("DESC_SERV_PRAZO"));

                        ep_inc_desc_preco();
                    }
                    // Limpa os dados
                    dados = null;
                }
                sql.setLength(0);
                sql.append( "SELECT INDEXA, INDICE_VALOR, VAL_MARKUP_VARE1, VAL_MARKUP_VARE2, VAL_MARKUP_VARE3, VAL_MARKUP_ATAC1, VAL_MARKUP_ATAC2, VAL_MARKUP_ATAC3, " +
                            "MARKUP_VARE1, MARKUP_VARE2, MARKUP_VARE3, MARKUP_ATAC1, MARKUP_ATAC2, MARKUP_ATAC3 \n" +
                            "FROM SMAEMPRE \n" +
                            "WHERE ID_SMAEMPRE = " + idEmpresa);

                String indexaTemp = "";
                String indiceValorTemp = "";

                dados = bancoDados.rawQuery(sql.toString(), null);
                if ( (dados != null) && (dados.getCount() > 0)){
                    dados.moveToFirst();
                    indexaTemp = dados.getString(dados.getColumnIndex("INDEXA"));
                    indiceValorTemp = dados.getString(dados.getColumnIndex("INDICE_VALOR"));
                    valorMarkupVarejo1 = dados.getDouble(dados.getColumnIndex("VAL_MARKUP_VARE1"));
                    valorMarkupVarejo2 = dados.getDouble(dados.getColumnIndex("VAL_MARKUP_VARE2"));
                    valorMarkupVarejo3 = dados.getDouble(dados.getColumnIndex("VAL_MARKUP_VARE3"));
                    valorMarkupAtacado1 = dados.getDouble(dados.getColumnIndex("VAL_MARKUP_ATAC1"));
                    valorMarkupAtacado2 = dados.getDouble(dados.getColumnIndex("VAL_MARKUP_ATAC2"));
                    valorMarkupAtacado3 = dados.getDouble(dados.getColumnIndex("VAL_MARKUP_ATAC3"));
                    markupEmpresaVarejo1 = dados.getDouble(dados.getColumnIndex("MARKUP_VARE1"));
                    markupEmpresaVarejo2 = dados.getDouble(dados.getColumnIndex("MARKUP_VARE2"));
                    markupEmpresaVarejo3 = dados.getDouble(dados.getColumnIndex("MARKUP_VARE3"));
                    markupEmpresaAtacado1 = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC1"));
                    markupEmpresaAtacado2 = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC2"));
                    markupEmpresaAtacado3 = dados.getDouble(dados.getColumnIndex("MARKUP_ATAC3"));

                }
                // Limpa os dados
                dados = null;

                if ((idMoeda != null) && (idMoeda != 0) && (indexaTemp.equalsIgnoreCase("1"))){
                    double cotacao = 1;

                    if ((indiceValorTemp.equalsIgnoreCase("1"))){
                        sql.setLength(0);
                        sql.append( "SELECT VALOR \n" +
                                    "FROM CFACOTAC \n" +
                                    "WHERE CFACOTAC.ID_CFAMOEDA = " + idMoeda + " AND CFACOTAC.DATA = " + dataReajusteVarejo);
                        dados = bancoDados.rawQuery(sql.toString(), null);
                        if ((dados != null) && (dados.getCount() > 0)){
                            dados.moveToFirst();

                            cotacao = dados.getDouble(dados.getColumnIndex("VALOR"));
                        }
                        dados = null;
                        cotacao = (cotacao == 0 ? 1 : cotacao);
                        precoVarejo = precoVarejo / cotacao;
                        precoServico = precoServico / cotacao;

                        if (!dataReajusteAtacado.equalsIgnoreCase(dataReajusteVarejo)){
                            sql.setLength(0);
                            sql.append( "SELECT VALOR FROM CFACOTAC WHERE CFACOTAC.ID_CFAMOEDA = " + idMoeda + " AND CFACOTAC.DATA = " + dataReajusteAtacado);

                            dados = bancoDados.rawQuery(sql.toString(), null);
                            if ((dados != null) && (dados.getCount() > 0)){
                                dados.moveToFirst();

                                cotacao = dados.getDouble(dados.getColumnIndex("VALOR"));
                            }
                            dados = null;
                        }
                        cotacao = (cotacao == 0 ? 1 : cotacao);
                        precoAtacado = precoAtacado / cotacao;
                    }
                    cotacao = 1;
                    sql.setLength(0);
                    sql.append( "SELECT VALOR FROM CFACOTAC WHERE CFACOTAC.ID_CFAMOEDA = " + idMoeda + " AND CFACOTAC.DATA = " + dataReajusteAtacado);

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        cotacao = dados.getDouble(dados.getColumnIndex("VALOR"));
                    }
                    dados = null;
                    cotacao = (cotacao == 0 ? 1 : cotacao);
                    precoVarejo = precoVarejo * cotacao;
                    precoAtacado = precoAtacado * cotacao;
                    precoServico = precoServico * cotacao;
                }
                if (vistaPrazo.equalsIgnoreCase("0")){
                    jurosMedioFatorAtacado = 0;
                    jurosMedioFatorVarejo = 0;
                    jurosMedioFatorServico = 0;
                }
                if (usaJurosMedioLocal != null && usaJurosMedioLocal.contains("0")){jurosMedioFatorAtacado =  jurosMedioPlPgtAtacado;}
                if (usaJurosMedioLocal != null && usaJurosMedioLocal.contains("1")){jurosMedioFatorVarejo =  jurosMedioPlPgtVarejo;}
                if (usaJurosMedioLocal != null && usaJurosMedioLocal.contains("2")){jurosMedioFatorServico =  jurosMedioPlPgtServico;}

                // Verifica se retornou o tipo de bonus do banco pois o campo nao eh obrigatorio
                if ((tipoBonusFator != null) && (!tipoBonusFator.isEmpty())) {

                    if (((tipoBonusFator.equalsIgnoreCase("1")) || (tipoBonusFator.equalsIgnoreCase("2")))
                            && (diasBonusFator >= diasMultiplicadoTemp)) {
                        diasMultiplicadoTemp = 0.0;

                    } else if ((tipoBonusFator.equalsIgnoreCase("2")) && (diasBonusFator <= diasMultiplicadoTemp)) {
                        diasMultiplicadoTemp = (diasMultiplicadoTemp - diasBonusFator);
                    }
                }
                jurosMedioFatorAtacado = (jurosMedioFatorAtacado / 30 * diasMultiplicadoTemp);
                jurosMedioFatorVarejo = (jurosMedioFatorVarejo / 30 * diasMultiplicadoTemp);
                jurosMedioFatorServico = (jurosMedioFatorServico / 30 * diasMultiplicadoTemp);

                precoVarejo = precoVarejo * (1 + (jurosMedioFatorVarejo / 100));
                precoAtacado = precoAtacado * (1 + (jurosMedioFatorAtacado / 100));
                precoServico = precoServico * (1 + (jurosMedioFatorServico / 100));

                if (precoVarejo < valorMarkupVarejo1){precoVarejo = markupEmpresaVarejo1;}
                if (precoVarejo < valorMarkupVarejo2){precoVarejo = markupEmpresaVarejo2;}
                if (precoVarejo < valorMarkupVarejo3){precoVarejo = markupEmpresaVarejo3;}
                if (precoAtacado < valorMarkupAtacado1){precoAtacado = markupEmpresaAtacado1;}
                if (precoAtacado < valorMarkupAtacado2){precoAtacado = markupEmpresaAtacado2;}
                if (precoAtacado < valorMarkupAtacado3){precoAtacado = markupEmpresaAtacado3;}

                if (tipoProduto.equalsIgnoreCase("1")){
                    precoVarejo = 0;
                    precoAtacado = 0;
                } else {
                    precoServico = 0;
                }

                precoVarejo = round(precoVarejo, 3);
                precoAtacado = round(precoAtacado, 3);
                precoServico = round(precoServico, 3);
                if (retorno == null){
                    retorno = new ContentValues();
                }
                retorno.put(KEY_PRECO_VAREJO, precoVarejo);
                retorno.put(KEY_PRECO_ATACADO, precoAtacado);
                retorno.put(KEY_PRECO_SERVICO, precoServico);
                retorno.put(KEY_PRODUTO_PROMOCAO_ATACADO, produtoEmPromocaoAtacado);
                retorno.put(KEY_PRODUTO_PROMOCAO_VAREJO, produtoEmPromocaoVarejo);
                retorno.put(KEY_PRODUTO_PROMOCAO_SERVICO, produtoEmPromocaoServico);

            } catch (final Exception e) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(context)
                                .title("CalculaPrecoSP")
                                .content(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage() + "\n idAeaploja" + idProdutoLoja)
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                });
            } finally {
                conexaoBanco.fechar();
                bancoDados.close();
            }
        }
        return retorno;
    }

    private void ep_inc_desc_preco(){
        descontoPromocao = descontoPromocao == null ? "" : descontoPromocao;
        if (vistaPrazo.equalsIgnoreCase("0")){
            if ( ((produtoEmPromocaoAtacado.equalsIgnoreCase("0")) && ((descontoMaximoPlanoAtacadoVista == 0) || ((descontoMercadoriaVistaAtacado + percentualDescontoAtacado) <= descontoMaximoPlanoAtacadoVista)) )
                    || ((produtoEmPromocaoAtacado.equalsIgnoreCase("1")) && (descontoPromocao.equalsIgnoreCase("1")) ) ){
                precoAtacado = precoAtacado * (1- ((percentualDescontoAtacado) / 100));
            }
            if ( ((produtoEmPromocaoVarejo.equalsIgnoreCase("0")) && ((descontoMaximoPlanoVarejoVista == 0) || ((descontoMercadoriaVistaVarejo + percentualDescontoVarejo) <= descontoMaximoPlanoVarejoVista)) )
                    || ((produtoEmPromocaoVarejo.equalsIgnoreCase("1")) && (descontoPromocao.equalsIgnoreCase("1")) ) ){
                precoVarejo = precoVarejo * (1- ((percentualDescontoVarejo) / 100));
            }
            if ( ((produtoEmPromocaoServico.equalsIgnoreCase("0")) && ((descontoMaximoPlanoServicoVista == 0) || ((descontoMercadoriaVistaServico + percentualDescontoServico) <= descontoMaximoPlanoServicoVista)) )
                    || ((produtoEmPromocaoServico.equalsIgnoreCase("1")) && (descontoPromocao.equalsIgnoreCase("1")) ) ){
                precoServico = precoServico * (1- ((percentualDescontoServico ) / 100));
            }
        } else {
            if ( ((produtoEmPromocaoAtacado.equalsIgnoreCase("0")) && ((descontoMaximoPlanoAtacadoPrazo == 0) || ((descontoMercadoriaPrazoAtacado +percentualDescontoAtacado) <= descontoMaximoPlanoAtacadoPrazo)) )
                    || ((produtoEmPromocaoAtacado.equalsIgnoreCase("1")) && (descontoPromocao.equalsIgnoreCase("1")) ) ){
                precoAtacado = precoAtacado * (1- ((percentualDescontoAtacado) / 100));
            }
            if ( ((produtoEmPromocaoVarejo.equalsIgnoreCase("0")) && ((descontoMaximoPlanoVarejoPrazo == 0) || ((descontoMercadoriaPrazoVarejo + percentualDescontoVarejo) <= descontoMaximoPlanoVarejoPrazo)) )
                    || ((produtoEmPromocaoVarejo.equalsIgnoreCase("1")) && (descontoPromocao.equalsIgnoreCase("1")) ) ){
                precoVarejo = precoVarejo * (1- ((percentualDescontoVarejo) / 100));
            }
            if ( ((produtoEmPromocaoServico.equalsIgnoreCase("0")) && ((descontoMaximoPlanoServicoPrazo == 0) || ((descontoMercadoriaPrazoServico + percentualDescontoServico) <= descontoMaximoPlanoServicoPrazo)) )
                    || ((produtoEmPromocaoServico.equalsIgnoreCase("1")) && (descontoPromocao.equalsIgnoreCase("1")) ) ){
                precoServico = precoServico * (1- ((percentualDescontoServico) / 100));
            }
        }
    }

}

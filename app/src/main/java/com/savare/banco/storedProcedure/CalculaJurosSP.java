package com.savare.banco.storedProcedure;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;

public class CalculaJurosSP extends StoredProcedure {

    public static final String  KEY_JUROS = "keyJuros",
                                KEY_JUROS_PRORROGADO = "keyJurosProrrogado",
                                KEY_DESCONTO = "keyDesconto",
                                KEY_MULTA = "keyMulta",
                                KEY_TOTAL = "keyTotal";

    private String dtVencimento,
                    dtPagamento,
                    dtEmissao,
                    dtBase,
                    dtPrevisao,
                    tipoDocumento,
                    tipoPagarReceber,
                    prorrogado,
                    capitaliza,
                    capitalizaParam,
                    capitalizaEmpre,
                    antecipa;

    private double  valorJurosDiario,
                    valorRestante,
                    taxaDiaria,
                    percDescPromDia,
                    percDescDia,
                    percDescCartaoDeb,
                    percDescCartao1,
                    percDescCartao2,
                    percDescCartao3,
                    juros,
                    jurosDiario,
                    jurosDiarioParam,
                    jurosProrrogado,
                    desconto,
                    total,
                    auxDouble,
                    multa,
                    percMulta;

    private Integer parcelaFim1,
                    parcelaFim2,
                    parcelaFim3,
                    dias,
                    diasVendaVista,
                    diasCarencia,
                    meses,
                    idSmaempre,
                    idCfaclifo,
                    idCfaccred,
                    parcelaParametro;


    public CalculaJurosSP(Context context, ProgressBar progressBarStatus, TextView textStatus) {
        super(context, progressBarStatus, textStatus);
    }

    public ContentValues execute(Integer idRpaparce,
                                 double percentualJuros,
                                 double percentualDesconto,
                                 String capitaliza,
                                 String previsao){
        // Cria avariavel de retorno
        ContentValues retorno = null;

        // Variaveis que vao retornar dentro do ContentValues
        juros = 0;
        desconto = 0;
        percDescDia = 0;
        multa = 0;

        if (textStatus != null){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(context.getResources().getString(R.string.calculando_juros));
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
        try{
            sql.setLength(0);
            sql.append("SELECT RPAPARCE.TIPO AS PARCE_TIPO, RPAPARCE.ID_SMAEMPRE, RPAPARCE.DT_EMISSAO, RPAPARCE.DT_VENCIMENTO, RPAPARCE.DT_PAGAMENTO, RPAPARCE.VL_JUROS_PRORROG, RPAPARCE.VL_JUROS_DIARIO, RPAPARCE.PERC_MULTA, \n");
            sql.append("RPAPARCE.TAXA_DIARIA, RPAPARCE.CAPITALIZA, RPAPARCE.PERC_DESCONTO, RPAPARCE.FC_VL_RESTANTE_SEM_PRORROG, RPAPARCE.PRORROGADO, RPAPARCE.ID_CFACLIFO, RPAPARCE.ID_CFACCRED, \n");
            sql.append("CFACCRED.TAXA_DEB, CFACCRED.ANTECIPA, CFACCRED.PARCELA_FIM1, CFACCRED.TAXA1, CFACCRED.PARCELA_FIM2, CFACCRED.TAXA2, CFACCRED.PARCELA_FIM3, CFACCRED.TAXA3, CFATPDOC.TIPO AS TPDOC_TIPO \n");
            sql.append("FROM RPAPARCE \n");
            sql.append("LEFT OUTER JOIN CFACCRED ON (CFACCRED.ID_CFACCRED = RPAPARCE.ID_CFACCRED) \n");
            sql.append("LEFT OUTER JOIN CFATPDOC ON (CFATPDOC.ID_CFATPDOC = RPAPARCE.ID_CFATPDOC) \n");
            sql.append("WHERE RPAPARCE.ID_RPAPARCE = " + idRpaparce);

            Cursor dados = bancoDados.rawQuery(sql.toString(), null);

            // Checa se retornou alguma coisa do banco de dados
            if ((dados != null) && (dados.getCount() > 0)){
                // Move o cursor para o primeiro registro
                dados.moveToFirst();

                tipoPagarReceber = dados.getString(dados.getColumnIndex("PARCE_TIPO"));
                idSmaempre = dados.getInt(dados.getColumnIndex("ID_SMAEMPRE"));
                dtEmissao = dados.getString(dados.getColumnIndex("DT_EMISSAO"));
                dtVencimento = dados.getString(dados.getColumnIndex("DT_VENCIMENTO"));
                dtPagamento = dados.getString(dados.getColumnIndex("DT_PAGAMENTO"));
                juros = dados.getDouble(dados.getColumnIndex("VL_JUROS_PRORROG"));
                valorJurosDiario = dados.getDouble(dados.getColumnIndex("VL_JUROS_DIARIO"));
                percMulta = dados.getDouble(dados.getColumnIndex("PERC_MULTA"));
                taxaDiaria = dados.getDouble(dados.getColumnIndex("TAXA_DIARIA"));
                this.capitaliza = dados.getString(dados.getColumnIndex("CAPITALIZA"));
                percDescDia = dados.getDouble(dados.getColumnIndex("PERC_DESCONTO"));
                valorRestante = dados.getDouble(dados.getColumnIndex("FC_VL_RESTANTE_SEM_PRORROG"));
                prorrogado = dados.getString(dados.getColumnIndex("PRORROGADO"));
                idCfaclifo = dados.getInt(dados.getColumnIndex("ID_CFACLIFO"));
                idCfaccred = dados.getInt(dados.getColumnIndex("ID_CFACCRED"));
                percDescCartaoDeb = dados.getDouble(dados.getColumnIndex("TAXA_DEB"));
                antecipa = dados.getString(dados.getColumnIndex("ANTECIPA"));
                parcelaFim1 = dados.getInt(dados.getColumnIndex("PARCELA_FIM1"));
                percDescCartao1 = dados.getDouble(dados.getColumnIndex("TAXA1"));
                parcelaFim2 = dados.getInt(dados.getColumnIndex("PARCELA_FIM2"));
                percDescCartao2 = dados.getDouble(dados.getColumnIndex("TAXA2"));
                parcelaFim3 = dados.getInt(dados.getColumnIndex("PARCELA_FIM3"));
                percDescCartao3 = dados.getDouble(dados.getColumnIndex("TAXA3"));
                tipoDocumento = dados.getString(dados.getColumnIndex("TPDOC_TIPO"));
            }
            // Limpa a memoria
            dados = null;

            if ( (percDescCartaoDeb + percDescCartao1 + percDescCartao2 + percDescCartao3) == 0 ){
                // quando for retroativo
                if (idRpaparce < 0){
                    sql.setLength(0);
                    sql.append("SELECT SUM(VL_PAGO) AS VL_PAGO FROM RPALCPAR WHERE (ID_RPAPARCE = " + idRpaparce + ") AND (DT_MOVIMENTO > " + previsao + ")");

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        valorRestante = valorRestante + dados.getDouble(dados.getColumnIndex("VL_PAGO"));
                    }
                    // Limpa a memoria do cursor
                    dados = null;
                    dtPagamento = null;

                    sql.setLength(0);
                    sql.append("SELECT DT_MOVIMENTO FROM RPALCPAR WHERE (ID_RPAPARCE = ").append(idRpaparce).append(") AND (DT_MOVIMENTO <= ").append(previsao).append(") ORDER BY DT_MOVIMENTO DESC LIMIT 1");

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        dtPagamento = dados.getString(dados.getColumnIndex("DT_MOVIMENTO"));
                    }
                    // Limpa a memoria do cursor
                    dados = null;
                    dtBase = null;

                    sql.setLength(0);
                    sql.append("SELECT DT_VENCIMENTO_ANT FROM RPAPRORO WHERE (ID_RPAPARCE = ").append(idRpaparce).append(") AND (DT_MOVIMENTO >= ").append(previsao).append(") ORDER BY DT_MOVIMENTO ASC LIMIT 1");

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        dtBase = dados.getString(dados.getColumnIndex("DT_VENCIMENTO_ANT"));
                    }
                    // Limpa a memoria do cursor
                    dados = null;

                    if ( (dtBase != null) && (!dtBase.isEmpty()) ){
                        sql.setLength(0);
                        sql.append("SELECT SUM(VL_JUROS) AS VL_JUROS FROM RPAPRORO WHERE (ID_RPAPARCE = ").append(idRpaparce).append(") AND (DT_MOVIMENTO >= ").append(previsao).append(" )");

                        dados = bancoDados.rawQuery(sql.toString(), null);

                        if ((dados != null) && (dados.getCount() > 0)){
                            dados.moveToFirst();

                            juros = juros - dados.getDouble(dados.getColumnIndex("VL_JUROS"));
                        }
                        // Limpa a memoria do cursor
                        dados = null;

                        prorrogado = "1";
                        dtVencimento = dtBase;
                    } else {
                        sql.setLength(0);
                        sql.append("SELECT COUNT(*) AS QT FROM RPAPRORO WHERE (ID_RPAPARCE = ").append(idRpaparce).append(") AND (DT_MOVIMENTO < ").append(previsao).append(")");

                        dados = bancoDados.rawQuery(sql.toString(), null);

                        if ((dados != null) && (dados.getCount() > 0)){
                            dados.moveToFirst();

                           if (dados.getInt(dados.getColumnIndex("QT")) > 0){
                               prorrogado = "1";
                           } else {
                               prorrogado = "0";
                           }
                        }
                        dados = null;
                    }
                }
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                jurosProrrogado = round(juros, 2);
                dtBase = dtVencimento;

                // Se for positivo a diferenca entao a datafinal eh maior que data inicial ((:vd_dt_pagamento Is Not Null) And (:vd_dt_pagamento > :vd_dt_vencimento))
                if ((dtPagamento != null) && (Long.parseLong(funcoes.diferencaEntreData(funcoes.DIAS, dtPagamento, dtVencimento)) < 0) ){
                    dtBase = dtPagamento;
                }
                // :PREVISAO - :vd_dt_base
                dias = (Integer.parseInt(funcoes.diferencaEntreData(funcoes.DIAS, dtBase, previsao)) - 1); // Tira um dia

                if ( (tipoPagarReceber != null) && (tipoPagarReceber.equalsIgnoreCase("0"))){
                    sql.setLength(0);
                    sql.append("SELECT PERC_DESC_PGTO_ANT, JUROS_DIARIO, CAPITALIZA, DIAS_CARENCIA, DIAS_VENDA_VISTA, PERC_MULTA \n");
                    sql.append("FROM SMAEMPRE \n");
                    sql.append("WHERE ID_SMAEMPRE = ").append(idSmaempre);

                    dados = bancoDados.rawQuery(sql.toString(), null);

                    if ((dados != null) && (dados.getCount() > 0)){
                        dados.moveToFirst();

                        percDescDia = dados.getDouble(dados.getColumnIndex("PERC_DESC_PGTO_ANT"));
                        jurosDiario = dados.getDouble(dados.getColumnIndex("JUROS_DIARIO"));
                        capitalizaEmpre = dados.getString(dados.getColumnIndex("CAPITALIZA"));
                        diasCarencia = dados.getInt(dados.getColumnIndex("DIAS_CARENCIA"));
                        diasVendaVista = dados.getInt(dados.getColumnIndex("DIAS_VENDA_VISTA"));
                        auxDouble = dados.getInt(dados.getColumnIndex("PERC_MULTA"));

                    }
                    dados = null;
                }
                // Checa se ja tem percentual de multa
                if (percMulta == 0){
                    percMulta = auxDouble;
                }
                multa = valorRestante * (percMulta / 100);
                //auxDouble = 0;
                int diasCarenciaParam = 0;

                sql.setLength(0);
                sql.append("SELECT DIAS_CARENCIA, JUROS_DIARIO, CAPITALIZA ");
                sql.append("FROM CFAPARAM \n");
                sql.append("WHERE ID_CFACLIFO = ").append(idCfaclifo).append(" AND ID_SMAEMPRE = ").append(idSmaempre);

                dados = bancoDados.rawQuery(sql.toString(), null);
                if ((dados != null) && (dados.getCount() > 0)) {
                    dados.moveToFirst();

                    diasCarenciaParam = dados.getInt(dados.getColumnIndex("DIAS_CARENCIA"));
                    jurosDiarioParam = dados.getDouble(dados.getColumnIndex("JUROS_DIARIO"));
                    capitalizaParam = dados.getString(dados.getColumnIndex("CAPITALIZA"));
                }
                dados = null;
                if (diasCarenciaParam > 0){ diasCarencia = diasCarenciaParam; }

                if (percentualJuros != 0){
                    taxaDiaria = percentualJuros;
                }
                if (percentualDesconto != 0){
                    percDescPromDia = percentualDesconto;
                }
                if ((capitaliza != null) && (!capitaliza.isEmpty())){
                    this.capitaliza = capitaliza;
                }
                // Verfiica se dias venda vista esta nulo
                diasVendaVista = (diasVendaVista == null ? 0 : diasVendaVista);

                //((:vs_prorrogado = '1') Or (:vd_dt_pagamento Is Not Null) Or ((:vd_dt_vencimento - :vd_dt_emissao) <= iif(:vi_dias_venda_vista Is Null, 0, :vi_dias_venda_vista)))
                if ( ((prorrogado != null) && (prorrogado.equalsIgnoreCase("1"))) ||
                        ( (dtPagamento != null) && (!dtPagamento.isEmpty()) ) ||
                        (Integer.parseInt(funcoes.diferencaEntreData(funcoes.DIAS, dtEmissao, dtVencimento)) <= diasVendaVista) ){
                    diasCarencia = 0;
                }
                // Verifica se dais de carencia esta nulo
                diasCarencia = (diasCarencia == null ? 0 : diasCarencia);
                // Aumenta a data de previsao de pagamento
                dtPrevisao = funcoes.adicionaData(funcoes.DIAS, previsao, diasCarencia);
                // Verifica se prorrogado esta preenchido
                prorrogado = (prorrogado == null ? "" : prorrogado);

                double vn_aux1 = 0;
                //(PREVISAO < :vd_dt_vencimento) And (:vd_dt_pagamento Is Null) And (iif(:vs_prorrogado Is Null, '', :vs_prorrogado) <> '1'))
                if ( (Integer.parseInt(funcoes.diferencaEntreData(funcoes.DIAS, dtVencimento, previsao)) < 0) && (dtPagamento == null) && (prorrogado != "1") ){
                    desconto = valorRestante - (valorRestante * (1 - (percDescPromDia / 100.00)));
                    vn_aux1 = ((valorRestante - desconto) * (percDescDia / 100.00));
                    desconto = desconto + (round(vn_aux1, 2) * dias);

                //(:vd_dt_previsao > :vd_dt_base)
                } else if ( Integer.parseInt(funcoes.diferencaEntreData(funcoes.DIAS, dtBase, dtPrevisao)) > 0 ){
                    if (valorJurosDiario == 0){
                        if (vn_aux1 != 0){
                            taxaDiaria = vn_aux1; // Do titulo
                        }else if (jurosDiarioParam != 0){
                            taxaDiaria = jurosDiarioParam; // Do cliente
                        } else {
                            taxaDiaria = jurosDiario; // Da empresa
                        }
                        valorJurosDiario = valorRestante * (taxaDiaria / 100);
                    }
                    if (this.capitaliza == null){
                        if ( (capitalizaParam != null) && (!capitalizaParam.isEmpty()) ){
                            this.capitaliza = capitalizaParam;
                        } else {
                            this.capitaliza = capitalizaEmpre;
                        }
                    }
                    if (this.capitaliza != null && this.capitaliza.equalsIgnoreCase("1")){ // Diario
                        while (dias > 0){
                            juros = juros + round(valorJurosDiario, 2);
                            if (taxaDiaria != 0){
                                valorJurosDiario = (valorRestante + juros) * (taxaDiaria / 100.00);
                            }
                            dias = dias - 1;
                            juros = round(juros, 2);
                        }
                    } else if (this.capitaliza != null && this.capitaliza.equalsIgnoreCase("2")) { // Mensal
                        meses = dias / 30;
                        dias = dias - (meses * 30);
                        while (meses > 0){
                            juros = juros + (round(valorJurosDiario, 2) * 30);
                            if (taxaDiaria != 0){
                                valorJurosDiario = (valorRestante + juros) * (taxaDiaria / 100.00);
                            }
                            meses = meses - 1;
                            juros = round(juros, 2);
                        }
                        juros = juros + (round(valorJurosDiario, 2) * dias); // Restante dos dias
                    } else {
                        juros = juros + (round(valorJurosDiario, 2) * dias); // Nao Capitaliza
                    }
                }
            } else {
                juros = 0;
                if (tipoDocumento.equalsIgnoreCase("7")){
                    desconto = valorRestante * (percDescCartaoDeb / 100.00);
                } else {
                    sql.setLength(0);
                    sql.append("SELECT COUNT(*) AS QTD FROM RPAPARCE \n");
                    sql.append("LEFT OUTER JOIN CFATPDOC ON (CFATPDOC.ID_CFATPDOC = RPAPARCE.ID_CFATPDOC) \n");
                    sql.append("WHERE (RPAPARCE.ID_RPAPARCE = ").append(idRpaparce).append(") AND (RPAPARCE.ID_CFACCRED = ").append(idCfaccred).append(") AND (CFATPDOC.TIPO = ").append(tipoDocumento).append(")");

                    dados = bancoDados.rawQuery(sql.toString(), null);
                    if ((dados != null) && (dados.getCount() > 0)) {
                        dados.moveToFirst();
                        dias = dados.getInt(dados.getColumnIndex("QTD"));
                    }
                    dados = null;
                    meses = 1;
                    if (antecipa.equalsIgnoreCase("1")){
                        sql.setLength(0);
                        sql.append("SELECT COUNT(*) AS QTD FROM RPAPARCE \n");
                        sql.append("LEFT OUTER JOIN CFATPDOC ON (CFATPDOC.ID_CFATPDOC = RPAPARCE.ID_CFATPDOC) \n");
                        sql.append("WHERE (RPAPARCE.ID_RPAPARCE = ").append(idRpaparce)
                                .append(") AND (RPAPARCE.ID_CFACCRED = ").append(idCfaccred)
                                .append(") AND (CFATPDOC.TIPO = '").append(tipoDocumento)
                                .append("') AND (RPAPARCE.DT_VENCIMENTO <= ").append(dtVencimento).append(") ");

                        dados = bancoDados.rawQuery(sql.toString(), null);
                        if ((dados != null) && (dados.getCount() > 0)) {
                            dados.moveToFirst();
                            meses = dados.getInt(dados.getColumnIndex("QTD"));
                        }
                        dados = null;
                    }
                    if (dias <= parcelaFim1){
                        desconto = valorRestante * ((percDescCartao1 * meses) / 100.00);

                    } else if (dias <= parcelaFim2){
                        desconto = valorRestante * ((percDescCartao2 * meses) / 100.00);

                    } else if (dias <= parcelaFim3){
                        desconto = valorRestante * ((percDescCartao3 * meses) / 100.00);
                    }
                }
            }
            if (retorno == null){ retorno = new ContentValues(); }

            retorno.put(KEY_MULTA, round(multa, 2));
            retorno.put(KEY_JUROS, round(juros, 2));
            retorno.put(KEY_DESCONTO, round(desconto, 2));
            retorno.put(KEY_TOTAL, round(valorRestante + juros - desconto, 2));
            retorno.put(KEY_JUROS_PRORROGADO, round(jurosProrrogado, 2));


        } catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("CalculaJurosSP")
                            .content(context.getResources().getString(R.string.msg_error) + "\n" + e.getMessage())
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
        } finally {
            conexaoBanco.fechar();
            bancoDados.close();
        }
        return retorno;
    }
}

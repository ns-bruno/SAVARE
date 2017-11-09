package com.savare.funcoes.rotinas;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.ProgressBar;

import com.savare.banco.funcoesSql.CriticaOrcamentoSql;
import com.savare.beans.CriticaOrcamentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno on 27/02/2017.
 */

public class CriticaOrcamentoRotina extends Rotinas {

    public CriticaOrcamentoRotina(Context context) {
        super(context);
    }

    public long insertCriticaOrcamento(ContentValues dados){
        CriticaOrcamentoSql criticaOrcamentoSql = new CriticaOrcamentoSql(context);

        return criticaOrcamentoSql.insert(dados);
    }

    public List<CriticaOrcamentoBeans> listaCriticaOrcamento(String idOrcamento, final ProgressBar progressBarStatus){
        List<CriticaOrcamentoBeans> listaCriticaOrcamento = null;

        try{
            CriticaOrcamentoSql criticaOrcamentoSql = new CriticaOrcamentoSql(context);

            final Cursor dados = criticaOrcamentoSql.query("ID_AEAORCAM = " + idOrcamento, "DT_CAD DESC");

            if (dados != null && dados.getCount() > 0){

                if (progressBarStatus != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setIndeterminate(false);
                            progressBarStatus.setProgress(0);
                            progressBarStatus.setMax(dados.getCount());
                        }
                    });
                }
                listaCriticaOrcamento = new ArrayList<CriticaOrcamentoBeans>();

                int incrementoProgresso = 0;

                while (dados.moveToNext()){
                    CriticaOrcamentoBeans critica = new CriticaOrcamentoBeans();
                    critica.setIdCritica(dados.getInt(dados.getColumnIndex("ID_AEACRORC")));
                    critica.setIdOrcamento(dados.getInt(dados.getColumnIndex("ID_AEAORCAM")));
                    critica.setDataCadastro(dados.getString(dados.getColumnIndex("DT_CAD")));
                    critica.setStatus(dados.getString(dados.getColumnIndex("STATUS")));
                    critica.setCodigoRetornoWebservice(dados.getInt(dados.getColumnIndex("CODIGO_RETORNO_WEBSERVICE")));
                    critica.setRetornoWebservice(dados.getString(dados.getColumnIndex("RETORNO_WEBSERVICE")));

                    listaCriticaOrcamento.add(critica);

                    incrementoProgresso++;

                    // Incrementa a barra de progresso
                    if (progressBarStatus != null) {
                        final int finalIncrementoProgresso = incrementoProgresso;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setProgress(finalIncrementoProgresso);
                            }
                        });
                    }
                }
            }
        } catch (Exception e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Cria uma variavem para inserir as propriedades da mensagem
            final ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "CriticaOrcamentoRotina");
            mensagem.put("mensagem", "Não conseguimos buscar as críticas do orçamento " + idOrcamento + ". \n" + e.getMessage());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(mensagem);
                }
            });
        }

        return listaCriticaOrcamento;
    }


    public CriticaOrcamentoBeans criticaOrcamento(String idCritica, String whereParamero){
        CriticaOrcamentoBeans dadosCriticaOrcamento = null;

        try{
            CriticaOrcamentoSql criticaOrcamentoSql = new CriticaOrcamentoSql(context);

            String whereQuery = "(ID_AEACRORC = " + idCritica + ") ";

            if ((whereParamero != null) && (!whereParamero.isEmpty())){
                whereQuery += " AND (" + whereParamero + ")";
            }

            final Cursor dados = criticaOrcamentoSql.query(whereQuery, "DT_CAD");

            if (dados != null && dados.getCount() > 0){

                dadosCriticaOrcamento = new CriticaOrcamentoBeans();

                dados.moveToFirst();
                dadosCriticaOrcamento.setIdCritica(dados.getInt(dados.getColumnIndex("ID_AEACRORC")));
                dadosCriticaOrcamento.setIdOrcamento(dados.getInt(dados.getColumnIndex("ID_AEAORCAM")));
                dadosCriticaOrcamento.setDataCadastro(dados.getString(dados.getColumnIndex("DT_CAD")));
                dadosCriticaOrcamento.setStatus(dados.getString(dados.getColumnIndex("STATUS")));
                dadosCriticaOrcamento.setCodigoRetornoWebservice(dados.getInt(dados.getColumnIndex("CODIGO_RETORNO_WEBSERVICE")));
                dadosCriticaOrcamento.setRetornoWebservice(dados.getString(dados.getColumnIndex("RETORNO_WEBSERVICE")));
            }
        } catch (Exception e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
            // Cria uma variavem para inserir as propriedades da mensagem
            final ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "CriticaOrcamentoRotina");
            mensagem.put("mensagem", "Não conseguimos buscar a crítica " + idCritica + ". \n" + e.getMessage());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(mensagem);
                }
            });
        }

        return dadosCriticaOrcamento;
    }
}

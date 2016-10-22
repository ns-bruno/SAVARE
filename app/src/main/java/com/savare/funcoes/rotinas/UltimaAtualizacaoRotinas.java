package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.UltimaAtualizacaoSql;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;

/**
 * Created by Bruno Nogueira Silva on 09/08/2016.
 */
public class UltimaAtualizacaoRotinas extends Rotinas {

    public UltimaAtualizacaoRotinas(Context context) {
        super(context);
    }


    public ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacaoTabelas(String tabela){
        ArrayList<UltimaAtualizacaoBeans> listaAtualizacao = null;

        UltimaAtualizacaoSql atualizacaoSql = new UltimaAtualizacaoSql(context);

        String where = null;

        if ((tabela != null) && (!tabela.isEmpty())){
            where = "TABELA = '" + tabela + "'";
        }

        Cursor dados = atualizacaoSql.query(where);

        // Checa se retornou algum dados
        if ((dados != null) && (dados.getCount() > 0)){
            listaAtualizacao = new ArrayList<UltimaAtualizacaoBeans>();

            while (dados.moveToNext()){
                UltimaAtualizacaoBeans ultimaAtualizacao = new UltimaAtualizacaoBeans();

                ultimaAtualizacao.setIdUltimaAtualizacao(dados.getInt(dados.getColumnIndex("ID_ULTIMA_ATUALIZACAO_DISPOSITIVO")));
                ultimaAtualizacao.setIdDispositivo(dados.getString(dados.getColumnIndex("ID_DISPOSITIVO")));
                ultimaAtualizacao.setDataCad(dados.getString(dados.getColumnIndex("DT_CAD")));
                ultimaAtualizacao.setDataAlt(dados.getString(dados.getColumnIndex("DT_ALT")));
                ultimaAtualizacao.setTabela(dados.getString(dados.getColumnIndex("TABELA")));
                ultimaAtualizacao.setDataUltimaAtualizacao(dados.getString(dados.getColumnIndex("DATA_ULTIMA_ATUALIZACAO")));

                listaAtualizacao.add(ultimaAtualizacao);
            }
        }

        return listaAtualizacao;
    }
}

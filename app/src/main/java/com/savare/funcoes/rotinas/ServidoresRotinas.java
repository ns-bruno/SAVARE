package com.savare.funcoes.rotinas;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.funcoesSql.ServidoresSql;
import com.savare.beans.ServidoresBeans;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno on 14/11/2017.
 */

public class ServidoresRotinas extends Rotinas {

    public ServidoresRotinas(Context context) {
        super(context);
    }

    public List<ServidoresBeans> listaServidores(String where, String ordem, final ProgressBar progressBarStatus){
        List<ServidoresBeans> listaServidores = new ArrayList<ServidoresBeans>();
        try {
            if (progressBarStatus != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressBarStatus.setIndeterminate(true);
                        progressBarStatus.setVisibility(View.VISIBLE);
                    }
                });
            }
            ServidoresSql servidoresSql = new ServidoresSql(context);
            final Cursor dados = servidoresSql.query(where, ordem);

            if ((dados != null) && (dados.getCount() > 0)){

                int incrementoProgresso = 0;
                if (progressBarStatus != null) {
                    final int finalIncrementoProgresso = incrementoProgresso;
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setProgress(finalIncrementoProgresso);
                            progressBarStatus.setMax(dados.getCount());
                        }
                    });
                }
                while (dados.moveToNext()) {
                    if (progressBarStatus != null) {
                        final int finalIncrementoProgresso1 = incrementoProgresso;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                progressBarStatus.setProgress(finalIncrementoProgresso1);
                            }
                        });
                    }
                    ServidoresBeans servidor = new ServidoresBeans();
                    servidor.setIdServidores(dados.getInt(dados.getColumnIndex("ID_SERVIDORES")));
                    servidor.setNomeServidor(dados.getString(dados.getColumnIndex("NOME")));
                    servidor.setIpServidor(dados.getString(dados.getColumnIndex("IP_SERVIDOR")));
                    servidor.setPorta(dados.getInt(dados.getColumnIndex("PORTA")));

                    listaServidores.add(servidor);
                    incrementoProgresso ++;
                }
            }
        } catch (Exception e){
            new MaterialDialog.Builder(context)
                    .title("ServidoresRotinas")
                    .content(e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        }
            return listaServidores;
    } // Fim listaServidores

    public long insertServidor(ContentValues dados){
        // Instancia a classe para manipular a tabela no banco de dados
        ServidoresSql servidoresSql = new ServidoresSql(context);
        // Inseri o item no orcamento
        return servidoresSql.insert(dados);
    } // Fim insertServidor

    public int updateServidor(ContentValues dados, String idServidor){
        // Instancia a classe para manipular a tabela no banco de dados
        ServidoresSql orcamentoSql = new ServidoresSql(context);

        // Atualiza o item no orcamento
        return orcamentoSql.update(dados, " ID_SERVIDORES = " + idServidor);
    }

    public int deleteServidor(String idServidor){
        ServidoresSql servidoresSql = new ServidoresSql(context);

        return servidoresSql.delete("ID_SERVIDORES = " + idServidor);
    }
}

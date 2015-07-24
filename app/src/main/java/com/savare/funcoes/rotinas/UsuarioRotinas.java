package com.savare.funcoes.rotinas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.beans.StatusBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Faturamento on 02/07/2015.
 */
public class UsuarioRotinas extends Rotinas {

    public UsuarioRotinas(Context context) {
        super(context);
    }

    /**
     * Atualiza a data e hora do recebimento de dados.
     * Retorna verdadei ou falso, indicando se consiguiu atualizar a data de recebimento.
     *
     * @param dataHora - yyyy/MM/dd HH:mm:ss
     * @return
     */
    public boolean atualizaDataHoraRecebimento(String dataHora){
        boolean retorno = false;

        ContentValues dados = new ContentValues();

        if(dataHora == null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();


            dados.put("DT_ULTIMO_RECEBIMENTO", cal.getTime().toString());
        } else {
            dados.put("DT_ULTIMO_RECEBIMENTO", dataHora);
        }

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        UsuarioSQL usuarioSQL = new UsuarioSQL(context);

        if(usuarioSQL.update(dados, "ID_USUA = " + funcoes.getValorXml("CodigoUsuario")) > 0){
            retorno = true;
        }
        return retorno;
    } // Fim atualizaDataHoraRecebimento


    /**
     * Pega a data do ultimo envio de pedidos.
     * Essa data esta relacionada com a data de evnio automático.
     *
     * @param idUsuario
     * @return
     */
    public String dataUltimoEnvio(String idUsuario){
        String dtUltimoEnvio = "";

        try {
            String sql = "SELECT DT_ULTIMO_ENVIO FROM USUARIO_USUA WHERE ID_USUA = " + idUsuario;

            UsuarioSQL usuarioSQL = new UsuarioSQL(context);

            Cursor cursor = usuarioSQL.sqlSelect(sql);

            // Checa se retornou um registro
            if( (cursor != null) && (cursor.getCount() > 0)){
                // Move para o primeiro registro
                cursor.moveToFirst();

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                dtUltimoEnvio = funcoes.formataDataHora(cursor.getString(cursor.getColumnIndex("DT_ULTIMO_ENVIO")));
            }

        } catch (Exception e) {
            dtUltimoEnvio = e.getMessage();
        }

        return dtUltimoEnvio;
    } // Fim dataUltimoEnvio


    public String dataUltimoRecebimento(String idUsuario){
        String dtUltimoRecebimento = "";

        try {
            String sql = "SELECT DT_ULTIMO_RECEBIMENTO FROM USUARIO_USUA WHERE ID_USUA = " + idUsuario;

            UsuarioSQL usuarioSQL = new UsuarioSQL(context);

            Cursor cursor = usuarioSQL.sqlSelect(sql);

            // Checa se retornou um registro
            if( (cursor != null) && (cursor.getCount() > 0)){
                // Move para o primeiro registro
                cursor.moveToFirst();

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

                dtUltimoRecebimento = funcoes.formataDataHora(cursor.getString(cursor.getColumnIndex("DT_ULTIMO_RECEBIMENTO")));
            }

        } catch (Exception e) {
            dtUltimoRecebimento = e.getMessage();
        }

        return dtUltimoRecebimento;
    } // Fim dataUltimoEnvio
}

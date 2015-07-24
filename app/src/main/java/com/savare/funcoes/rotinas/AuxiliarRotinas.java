package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.AuxiliarSql;
import com.savare.beans.AuxiliarBeans;
import com.savare.funcoes.Rotinas;

/**
 * Created by Faturamento on 18/07/2015.
 */
public class AuxiliarRotinas extends Rotinas {

    public AuxiliarRotinas(Context context) {
        super(context);
    }

    public AuxiliarBeans getIdClienteTemporario(String where){
        AuxiliarBeans auxiliarBeans = new AuxiliarBeans();

        // Instancia a classe para manipular os dados do banco de dados
        AuxiliarSql auxiliarSql = new AuxiliarSql(context);

        Cursor dadosAuxiliar = auxiliarSql.query(where);

        if (dadosAuxiliar != null && dadosAuxiliar.getCount() > 0){
            dadosAuxiliar.moveToFirst();

            auxiliarBeans.setIdClienteTemporario(dadosAuxiliar.getInt(dadosAuxiliar.getColumnIndex("ID_CFACLIFO_TEMP")));
        }
        // RetornO
        return auxiliarBeans;
    } // Fim getIdClienteTemporario
}

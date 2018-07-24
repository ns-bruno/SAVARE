package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.ParametrosLocalSql;
import com.savare.beans.ParametrosLocalBeans;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

public class ParametrosLocalRotina extends Rotinas {

    public ParametrosLocalRotina(Context context) {
        super(context);
    }

    public List<ParametrosLocalBeans> listaParametrosLocal(String where){
        List<ParametrosLocalBeans> listaParametros = null;

        if(where != null){
            where = " (" + where + ")";
        }
        // Instancia a classe para manipular os dados do banco de dados
        ParametrosLocalSql parametrosLocalSql = new ParametrosLocalSql(context);
        // Executa o sql e armazena os dados recuperados em um Cursor
        Cursor dadosParam = parametrosLocalSql.query(where);

        // Instancia a classe para salvar os dados que foi recuperando no banco
        listaParametros = new ArrayList<ParametrosLocalBeans>();

        if ( (dadosParam != null) && (dadosParam.getCount() > 0) ){

            while(dadosParam.moveToNext()){
                // Pega os dados recuperado do banco de dados
                ParametrosLocalBeans param = new ParametrosLocalBeans();
                param.setIdParam(dadosParam.getInt(dadosParam.getColumnIndex("ID_PARAM")));
                param.setNomeParam(dadosParam.getString(dadosParam.getColumnIndex("NOME_PARAM")));
                param.setValorParam(dadosParam.getString(dadosParam.getColumnIndex("VALOR_PARAM")));

                listaParametros.add(param);
            }
        }
        // Retorna uma lista de documentos
        return listaParametros;
    }
}

package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.UnidadeVendaSql;
import com.savare.beans.AeaunvenBeans;
import com.savare.funcoes.Rotinas;

public class AeaunvenRotinas extends Rotinas {

    public AeaunvenRotinas(Context context) {
        super(context);
    }

    public AeaunvenBeans selectUnidadeVenda(int idAeaunven){
        UnidadeVendaSql unidadeVendaSql = new UnidadeVendaSql(context);

        Cursor cursorMarca = unidadeVendaSql.query("ID_AEAUNVEN = " + idAeaunven);
        AeaunvenBeans aeaunven = null;
        if ( (cursorMarca != null) && (cursorMarca.moveToFirst()) ){
            aeaunven = new AeaunvenBeans();
            aeaunven.setIdAeaunven(cursorMarca.getInt(cursorMarca.getColumnIndex("ID_AEAUNVEN")));
            aeaunven.setDtAlt(cursorMarca.getString(cursorMarca.getColumnIndex("DT_ALT")));
            aeaunven.setSigla(cursorMarca.getString(cursorMarca.getColumnIndex("SIGLA")));
            aeaunven.setDescricaosingular(cursorMarca.getString(cursorMarca.getColumnIndex("DESCRICAO_SINGULAR")));
            aeaunven.setDecimais(cursorMarca.getInt(cursorMarca.getColumnIndex("DECIMAIS")));
        }
        return aeaunven;
    }
}

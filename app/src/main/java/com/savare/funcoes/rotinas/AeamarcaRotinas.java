package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.MarcaSql;
import com.savare.beans.AeamarcaBeans;
import com.savare.funcoes.Rotinas;

public class AeamarcaRotinas extends Rotinas {

    public AeamarcaRotinas(Context context) {
        super(context);
    }

    public AeamarcaBeans selectMarca(int idAeamarca){
        MarcaSql marcaSql = new MarcaSql(context);

        Cursor cursorMarca = marcaSql.query("ID_AEAMARCA = " + idAeamarca);
        AeamarcaBeans aeamarca = null;
        if ( (cursorMarca != null) && (cursorMarca.moveToFirst()) ){
            aeamarca = new AeamarcaBeans();
            aeamarca.setIdAeamarca(cursorMarca.getInt(cursorMarca.getColumnIndex("ID_AEAMARCA")));
            aeamarca.setDtAlt(cursorMarca.getString(cursorMarca.getColumnIndex("DT_ALT")));
            aeamarca.setDescricao(cursorMarca.getString(cursorMarca.getColumnIndex("DESCRICAO")));
            if (!cursorMarca.isNull(cursorMarca.getColumnIndex("FATOR"))) aeamarca.setFatorVenda(cursorMarca.getDouble(cursorMarca.getColumnIndex("FATOR")));
        }
        return aeamarca;
    }
}

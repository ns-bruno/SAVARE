package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.ClasseSql;
import com.savare.beans.AeaclaseBeans;
import com.savare.funcoes.Rotinas;

public class AeaclaseRotinas extends Rotinas {

    public AeaclaseRotinas(Context context) {
        super(context);
    }

    public AeaclaseBeans selectClasse(int idAeaclase){
        ClasseSql classeSql = new ClasseSql(context);

        Cursor cursorClasse = classeSql.query("ID_AEACLASE = " + idAeaclase);
        AeaclaseBeans aeaclase = null;
        if ( (cursorClasse!= null) && (cursorClasse.moveToFirst())){
            aeaclase = new AeaclaseBeans();
            aeaclase.setIdAeaclase(cursorClasse.getInt(cursorClasse.getColumnIndex("ID_AEACLASE")));
            aeaclase.setDtAlt(cursorClasse.getString(cursorClasse.getColumnIndex("DT_ALT")));
            aeaclase.setCodigo(cursorClasse.getInt(cursorClasse.getColumnIndex("CODIGO")));
            aeaclase.setDescricao(cursorClasse.getString(cursorClasse.getColumnIndex("DESCRICAO")));
        }
        return aeaclase;
    }
}

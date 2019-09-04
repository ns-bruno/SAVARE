package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.FotosSql;
import com.savare.beans.CfafotosBeans;
import com.savare.funcoes.Rotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 12/01/2016.
 */
public class FotoRotinas extends Rotinas {

    public FotoRotinas(Context context) {
        super(context);
    }

    /**
     * Pega apenas uma imagem de um determinado produto.
     *
     * @param idProduto
     * @return
     */
    public CfafotosBeans fotoIdProtudo (String idProduto){

        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.query("(ID_AEAPRODU = " + idProduto + ") AND (FOTO IS NOT NULL)");
        CfafotosBeans fotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){
            // Instancia a classe para salvar a foto
            fotoProduto = new CfafotosBeans();

            // Move o cursor para o primeiro registro
            cursor.moveToFirst();

            // Pega a foto retornada do banco
            fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
            fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
            fotoProduto.setFoto(cursor.getBlob(cursor.getColumnIndex("FOTO")));
        }
        return fotoProduto;
    }

    public CfafotosBeans fotoIdFoto (String idFoto){

        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.query("ID_CFAFOTOS = " + idFoto);
        CfafotosBeans fotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){
            // Instancia a classe para salvar a foto
            fotoProduto = new CfafotosBeans();

            // Move o cursor para o primeiro registro
            cursor.moveToFirst();

            // Pega a foto retornada do banco
            fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
            fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
            fotoProduto.setFoto(cursor.getBlob(cursor.getColumnIndex("FOTO")));
        }
        return fotoProduto;
    }


    public List<CfafotosBeans> listaFotoProduto (String idProduto){

        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.query("ID_AEAPRODU = " + idProduto);
        List<CfafotosBeans> listaFotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){

            listaFotoProduto = new ArrayList<CfafotosBeans>();

            // Passa por todos os registro
            while (cursor.moveToNext()){
                // Instancia a classe para salvar a foto
                CfafotosBeans fotoProduto = new CfafotosBeans();
                // Pega a foto retornada do banco
                fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
                fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
                fotoProduto.setFoto(cursor.getBlob(cursor.getColumnIndex("FOTO")));

                listaFotoProduto.add(fotoProduto);
            }


        }
        return listaFotoProduto;
    }

    public List<CfafotosBeans> listaIdFotos(){
        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.sqlSelect("SELECT CFAFOTOS.ID_CFAFOTOS, CFAFOTOS.ID_AEAPRODU FROM CFAFOTOS WHERE CFAFOTOS.ID_AEAPRODU IS NOT NULL");
        List<CfafotosBeans> listaFotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){

            listaFotoProduto = new ArrayList<CfafotosBeans>();

            // Passa por todos os registro
            while (cursor.moveToNext()){
                // Instancia a classe para salvar a foto
                CfafotosBeans fotoProduto = new CfafotosBeans();
                // Pega a foto retornada do banco
                fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
                fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));

                listaFotoProduto.add(fotoProduto);
            }


        }
        return listaFotoProduto;
    }
}

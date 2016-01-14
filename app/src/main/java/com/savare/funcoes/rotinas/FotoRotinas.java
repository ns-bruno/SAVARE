package com.savare.funcoes.rotinas;

import android.content.Context;
import android.database.Cursor;

import com.savare.banco.funcoesSql.FotosSql;
import com.savare.beans.FotosBeans;
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
    public FotosBeans fotoIdProtudo (String idProduto){

        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.query("ID_AEAPRODU = " + idProduto);
        FotosBeans fotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){
            // Instancia a classe para salvar a foto
            fotoProduto = new FotosBeans();

            // Move o cursor para o primeiro registro
            cursor.moveToFirst();

            // Pega a foto retornada do banco
            fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
            fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
            fotoProduto.setFotos(cursor.getBlob(cursor.getColumnIndex("FOTO")));
        }
        return fotoProduto;
    }

    public FotosBeans fotoIdFoto (String idFoto){

        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.query("ID_CFAFOTOS = " + idFoto);
        FotosBeans fotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){
            // Instancia a classe para salvar a foto
            fotoProduto = new FotosBeans();

            // Move o cursor para o primeiro registro
            cursor.moveToFirst();

            // Pega a foto retornada do banco
            fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
            fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
            fotoProduto.setFotos(cursor.getBlob(cursor.getColumnIndex("FOTO")));
        }
        return fotoProduto;
    }


    public List<FotosBeans> listaFotoProduto (String idProduto){

        FotosSql fotosSql = new FotosSql(context);

        Cursor cursor = fotosSql.query("ID_AEAPRODU = " + idProduto);
        List<FotosBeans> listaFotoProduto = null;

        // Checa se retornou alguma coisa do banco de dados
        if ((cursor != null) && (cursor.getCount() > 0)){

            listaFotoProduto = new ArrayList<FotosBeans>();

            // Passa por todos os registro
            while (cursor.moveToNext()){
                // Instancia a classe para salvar a foto
                FotosBeans fotoProduto = new FotosBeans();
                // Pega a foto retornada do banco
                fotoProduto.setIdFotos(cursor.getInt(cursor.getColumnIndex("ID_CFAFOTOS")));
                fotoProduto.setIdProduto(cursor.getInt(cursor.getColumnIndex("ID_AEAPRODU")));
                fotoProduto.setFotos(cursor.getBlob(cursor.getColumnIndex("FOTO")));

                listaFotoProduto.add(fotoProduto);
            }


        }
        return listaFotoProduto;
    }
}

package com.savare.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Bruno Nogueira Silva on 12/01/2016.
 */
public class FotosBeans {

    private int idFotos, idCliente, idProduto;
    private byte[] fotos;

    public int getIdFotos() {
        return idFotos;
    }

    public void setIdFotos(int idFotos) {
        this.idFotos = idFotos;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public byte[] getFotos() {
        return fotos;
    }

    public void setFotos(byte[] fotos) {
        this.fotos = fotos;
    }

    public Bitmap getImagem(){
        // Transforma o binario em image
        Bitmap imagem = BitmapFactory.decodeByteArray(getFotos(), 0, getFotos().length);

        return imagem;
    }
}

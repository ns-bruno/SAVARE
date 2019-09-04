package com.savare.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Bruno Nogueira Silva on 12/01/2016.
 */
public class CfafotosBeans {

    private int idFotos, 
                idCliente, 
                idProduto;
    private byte[] foto;
    private String dtAlt;

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

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getDtAlt() {
        return dtAlt;
    }

    public void setDtAlt(String dtAlt) {
        this.dtAlt = dtAlt;
    }

    public Bitmap getImagem(){
        // Transforma o binario em image
        Bitmap imagem = BitmapFactory.decodeByteArray(getFoto(), 0, getFoto().length);

        return imagem;
    }
}

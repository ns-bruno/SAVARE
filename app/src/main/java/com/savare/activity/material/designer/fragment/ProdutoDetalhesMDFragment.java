package com.savare.activity.material.designer.fragment;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.savare.R;
import com.savare.beans.CfafotosBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.FotoRotinas;

/**
 * Created by Bruno Nogueira Silva on 13/01/2016.
 */
public class ProdutoDetalhesMDFragment extends Fragment {

    private View viewProduto;
    private ImageView imageProduto;
    public static final String KEY_ID_CFAFOTO = "ID_CFAFOTO";
    private String idFoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewProduto = inflater.inflate(R.layout.fragment_produto_detalhe_imagem_md, container, false);

        recuperarCampos();

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if(parametro != null){
            // Pega o id da foto passado por paramentro
            idFoto = ((parametro.getString(KEY_ID_CFAFOTO) != null) && (parametro.getString(KEY_ID_CFAFOTO).length() > 0)) ? parametro.getString(KEY_ID_CFAFOTO) : null;
        }

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

        // Checa se foi passado algum id por parametro e se esta configurado para visualizar as imagens dos produtos
        if ((idFoto != null) && (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase("S"))){

            // Inscancia a classe de rotinas das fotos/imagens
            FotoRotinas fotoRotinas = new FotoRotinas(getContext());
            // Pega a imagem especificada
            CfafotosBeans imagemBanco = fotoRotinas.fotoIdFoto(idFoto);

            // Checa se retonou alguma coisa
            if ((imagemBanco != null) && (imagemBanco.getFoto().length > 0)){
                try {

                    final Bitmap imagem = BitmapFactory.decodeByteArray(imagemBanco.getFoto(), 0, imagemBanco.getFoto().length);

                    // Mostra a foto do produto
                    imageProduto.setImageBitmap(imagem);

                } catch (Exception e){
                    funcoes = new FuncoesPersonalizadas(getContext());

                    // Armazena as informacoes para para serem exibidas e enviadas
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("comando", 0);
                    contentValues.put("tela", "ProdutoDetalhesMDFragment");
                    contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.getMessage()));
                    contentValues.put("dados", e.toString());
                    // Pega os dados do usuario
                    contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                    contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    contentValues.put("email", funcoes.getValorXml("Email"));

                    funcoes.menssagem(contentValues);
                }
            }
        }

        return viewProduto;
    } // Fim onCreateView



    private void recuperarCampos(){
        imageProduto = (ImageView) viewProduto.findViewById(R.id.fragment_produto_detalhe_imagem_md_imageView_foto);
    }
}

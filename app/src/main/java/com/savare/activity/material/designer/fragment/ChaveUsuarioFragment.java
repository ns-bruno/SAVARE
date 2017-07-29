package com.savare.activity.material.designer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;

import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

/**
 * Created by Bruno on 17/11/2016.
 */

public class ChaveUsuarioFragment extends SlideFragment {

    private EditText editTextDigitarChave;
    private Button buttonTenhoChave, buttonDigitarChave;
    private boolean escanearChave= true;

    public static ChaveUsuarioFragment newInstance() {
        return new ChaveUsuarioFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chave_usuario, container, false);

        editTextDigitarChave = (EditText) root.findViewById(R.id.fragment_chave_usuario_editText_digitar_chave);
        buttonDigitarChave = (Button) root.findViewById(R.id.fragment_chave_usuario_buttonDigitarChave);
        buttonTenhoChave = (Button) root.findViewById(R.id.fragment_chave_usuario_buttonTenhoChave);

        buttonTenhoChave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (escanearChave) {
                    new ZxingOrient(getActivity())
                            .setInfo(getResources().getString(R.string.escanear_codigo_barras))
                            .setVibration(true)
                            .setIcon(R.mipmap.ic_launcher)
                            .initiateScan();
                } else {
                    // Checa se a quantidade que foi digitada eh o tamanho certo
                    if (editTextDigitarChave.getText().length() >= 16) {
                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                        funcoes.setValorXml("ChaveFuncionario", editTextDigitarChave.getText().toString());

                        desativarCampos();

                        SuperToast.create(getContext(), getResources().getString(R.string.chave_salva_sucesso), SuperToast.Duration.SHORT, Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();

                    } else {
                        SuperToast.create(getContext(), getResources().getString(R.string.tamanho_chave_nao_permitido), SuperToast.Duration.SHORT, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                    }
                }
            }
        });

        buttonDigitarChave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escanearChave = !escanearChave;

                if (escanearChave){
                    editTextDigitarChave.setVisibility(View.GONE);

                    buttonDigitarChave.setText(R.string.digitar_chave);
                    buttonTenhoChave.setText(R.string.tenho_chave_licenca);
                } else {
                    // Mostra o campo para digitar a chave
                    editTextDigitarChave.setVisibility(View.VISIBLE);

                    buttonTenhoChave.setText(R.string.salvar);
                    buttonDigitarChave.setText(R.string.cancelar);
                }
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZxingOrientResult retornoEscanerCodigoBarra = ZxingOrient.parseActivityResult(requestCode, resultCode, data);

        if(retornoEscanerCodigoBarra != null) {
            // Checha se retornou algum dado
            if(retornoEscanerCodigoBarra.getContents() == null) {
                //Log.d("SAGA", "Cancelled scan - CadastroEmbalagemActivity");
                SuperToast.create(getContext(), getResources().getString(R.string.cancelado), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();

            } else {
                //Log.d("SAGA", "Scanned - CadastroEmbalagemActivity");

                // Pega a chave retornado pelo leitor de codigo de barras
                String chaveUsuario = retornoEscanerCodigoBarra.getContents();

                if (chaveUsuario.length() >= 16) {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    funcoes.setValorXml("ChaveFuncionario", chaveUsuario);

                    desativarCampos();
                } else {
                    SuperToast.create(getContext(), getResources().getString(R.string.tamanho_chave_nao_permitido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                }

                // Vai para o proximo slide
                nextSlide();
            }
        } else {
            // This is important, otherwise the retornoEscanerCodigoBarra will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void desativarCampos(){
        buttonDigitarChave.setEnabled(false);
        buttonTenhoChave.setEnabled(false);
        editTextDigitarChave.setEnabled(false);
    }
}

package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;

import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

/**
 * Created by Bruno on 17/11/2016.
 */

public class ChaveUsuarioFragment extends SlideFragment {

    private EditText editTextDigitarCnpj;
    private Button buttonTenhoCnpj, buttonDigitarCnpj;
    private boolean escanearCnpj = true;

    public static ChaveUsuarioFragment newInstance() {
        return new ChaveUsuarioFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chave_usuario, container, false);

        editTextDigitarCnpj = (EditText) root.findViewById(R.id.fragment_chave_usuario_editText_digitar_cnpj);
        buttonDigitarCnpj = (Button) root.findViewById(R.id.fragment_chave_usuario_buttonDigitarCnpj);
        buttonTenhoCnpj = (Button) root.findViewById(R.id.fragment_chave_usuario_buttonTenhoCnpj);

        buttonTenhoCnpj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (escanearCnpj) {
                    new ZxingOrient(getActivity())
                            .setInfo(getResources().getString(R.string.escanear_codigo_barras))
                            .setVibration(true)
                            .setIcon(R.mipmap.ic_launcher)
                            .initiateScan();
                } else {
                    salvarCnpjEmpresa();
                }
            }
        });

        buttonDigitarCnpj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escanearCnpj = !escanearCnpj;

                if (escanearCnpj){
                    editTextDigitarCnpj.setVisibility(View.GONE);

                    buttonDigitarCnpj.setText(R.string.digitar_cpj);
                    buttonTenhoCnpj.setText(R.string.tenho_cnpj);
                } else {
                    // Mostra o campo para digitar a chave
                    editTextDigitarCnpj.setVisibility(View.VISIBLE);

                    buttonTenhoCnpj.setText(R.string.salvar);
                    buttonDigitarCnpj.setText(R.string.cancelar);
                }
            }
        });

        editTextDigitarCnpj.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    salvarCnpjEmpresa();
                    return true;
                }
                return false;
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
                //SuperActivityToast.create(getActivity(), getResources().getString(R.string.cancelado), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                SuperActivityToast.create(getActivity(), getResources().getString(R.string.cancelado), Style.DURATION_LONG)
                        .setTextColor(Color.WHITE)
                        .setColor(Color.RED)
                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();

            } else {
                //Log.d("SAGA", "Scanned - CadastroEmbalagemActivity");

                // Pega a chave retornado pelo leitor de codigo de barras
                String cnpj = retornoEscanerCodigoBarra.getContents();

                if (cnpj.length() >= 11) {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    funcoes.setValorXml(funcoes.TAG_CNPJ_EMPRESA, cnpj);

                    desativarCampos();
                } else {
                    //SuperActivityToast.create(getActivity(), getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                    SuperActivityToast.create(getActivity(), getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), Style.DURATION_LONG)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
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
        buttonDigitarCnpj.setEnabled(false);
        buttonTenhoCnpj.setEnabled(false);
        editTextDigitarCnpj.setEnabled(false);
    }

    private void salvarCnpjEmpresa(){
        // Checa se a quantidade que foi digitada eh o tamanho certo
        if ( (editTextDigitarCnpj.getText().length() == 11) || (editTextDigitarCnpj.getText().length() == 14) ) {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

            String bkpEdittextCnpj = editTextDigitarCnpj.getText().toString();

            if (editTextDigitarCnpj.getText().length() == 11){
                editTextDigitarCnpj.addTextChangedListener(funcoes.insertMascara(funcoes.MASCARA_CPF, editTextDigitarCnpj));

            } else if (editTextDigitarCnpj.getText().length() == 14){
                editTextDigitarCnpj.addTextChangedListener(funcoes.insertMascara(funcoes.MASCARA_CNPJ, editTextDigitarCnpj));
            }
            editTextDigitarCnpj.setText(bkpEdittextCnpj);
            funcoes.setValorXml(funcoes.TAG_CNPJ_EMPRESA, editTextDigitarCnpj.getText().toString());

            desativarCampos();

            //SuperActivityToast.create(getActivity(), getResources().getString(R.string.cnpj_salva_sucesso), SuperToast.Duration.SHORT, Style.getStyle(Style.GREEN, SuperToast.Animations.FLYIN)).show();

            SuperActivityToast.create(getActivity(), getResources().getString(R.string.cnpj_salva_sucesso), Style.DURATION_SHORT)
                    .setTextColor(Color.WHITE)
                    .setColor(Color.GREEN)
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();

            //SuperActivityToast.create(getActivity(), getResources().getString(R.string.cnpj_salva_sucesso), SuperToast.Duration.SHORT, Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();

        } else {
            //SuperActivityToast.create(getActivity(), getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), SuperToast.Duration.SHORT, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
            SuperActivityToast.create(getActivity(), getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), Style.DURATION_SHORT)
                    .setTextColor(Color.WHITE)
                    .setColor(Color.RED)
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
    }
}

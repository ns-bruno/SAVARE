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
                        funcoes.setValorXml("CnpjEmpresa", editTextDigitarCnpj.getText().toString());

                        desativarCampos();

                        SuperToast.create(getContext(), getResources().getString(R.string.cnpj_salva_sucesso), SuperToast.Duration.SHORT, Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();

                    } else {
                        SuperToast.create(getContext(), getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), SuperToast.Duration.SHORT, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                    }
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
                String cnpj = retornoEscanerCodigoBarra.getContents();

                if (cnpj.length() >= 11) {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    funcoes.setValorXml("CnpjEmpresa", cnpj);

                    desativarCampos();
                } else {
                    SuperToast.create(getContext(), getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
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
}

package com.savare.activity.material.designer.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
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

    private EditText editTextDigitarChave;
    private Button buttonTenhoChave;

    public static ChaveUsuarioFragment newInstance() {
        return new ChaveUsuarioFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chave_usuario, container, false);

        editTextDigitarChave = (EditText) root.findViewById(R.id.fragment_chave_usuario_editText_digitar_chave);
        buttonTenhoChave = (Button) root.findViewById(R.id.fragment_chave_usuario_buttonTenhoChave);

        buttonTenhoChave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarChaveUsuario();
            }
        });

        editTextDigitarChave.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    salvarChaveUsuario();
                    return true;
                }
                return false;
            }
        });

        return root;
    }


    private void salvarChaveUsuario(){
        // Checa se a quantidade que foi digitada eh o tamanho certo
        if (editTextDigitarChave.getText().length() > 11)  {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

            funcoes.setValorXml(funcoes.TAG_UUID_DISPOSITIVO, editTextDigitarChave.getText().toString());

            SuperActivityToast.create(getActivity(), getResources().getString(R.string.chave_salva_sucesso), Style.DURATION_SHORT)
                    .setTextColor(Color.WHITE)
                    .setColor(Color.GREEN)
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();

            // Vai para o proximo slide
            nextSlide();

        } else {
            SuperActivityToast.create(getActivity(), getResources().getString(R.string.tamanho_chave), Style.DURATION_SHORT)
                    .setTextColor(Color.WHITE)
                    .setColor(Color.RED)
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
    }
}

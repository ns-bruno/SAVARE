package com.savare.activity.material.designer.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.savare.R;
import com.savare.activity.material.designer.RegistroChaveUsuarioMDActivity;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosWebserviceAsyncRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import java.util.regex.Pattern;

/**
 * Created by Bruno Nogueira Silva on 02/08/2016.
 */
public class LoginFragment extends SlideFragment {

    private EditText username;
    private EditText password;
    private Button login;
    private ProgressBar progressBarStatus;
    private Bundle parametros;

    public LoginFragment() {
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        username = (EditText) root.findViewById(R.id.fragmento_login_username);
        password = (EditText) root.findViewById(R.id.fragment_login_password);
        login = (Button) root.findViewById(R.id.fragment_login_login);
        progressBarStatus = (ProgressBar) root.findViewById(R.id.fragment_login_progressBar_status);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validarNomeUsuario()) {
                    username.setEnabled(false);
                    password.setEnabled(false);
                    login.setEnabled(false);
                    login.setText(R.string.aguarde_buscando_primeiros_dados);

                    final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    funcoes.setValorXml("Usuario", username.getText().toString());
                    funcoes.setValorXml("SenhaUsuario", funcoes.criptografaSenha(password.getText().toString()));

                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            progressBarStatus.setVisibility(View.VISIBLE);

                            ReceberDadosWebserviceAsyncRotinas receberDadosWebservice = new ReceberDadosWebserviceAsyncRotinas(
                                    new ReceberDadosWebserviceAsyncRotinas.OnTaskCompleted() {
                                        @Override
                                        public void onTaskCompleted() {
                                            // Instancia a classe de rotinas
                                            Rotinas rotinas = new Rotinas(getContext());

                                            // Verfifica se existe algum usuario cadastrado, ou
                                            if ((rotinas.existeUsuario() == true) && (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {

                                                login.setText(R.string.logado_sucesso);

                                                // Deixa a barra de progresso invisivel
                                                progressBarStatus.setVisibility(View.INVISIBLE);

                                            } else {
                                                new MaterialDialog.Builder(getContext())
                                                        .title(R.string.nao_conseguimos_atualizar_usuario)
                                                        .content("Não conseguimos receber os dados do usuário, por favor, tente novamente.")
                                                        .positiveText(R.string.button_ok)
                                                        .show();

                                                // Abilita novamente os campos e o botao
                                                username.setEnabled(true);
                                                password.setEnabled(true);
                                                login.setEnabled(true);
                                                login.setText(R.string.registrar);
                                                // Deixa a barra de progresso invisivel
                                                progressBarStatus.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    },
                                    getContext(),
                                    new String[]{WSSisinfoWebservice.FUNCTION_SELECT_USUARIO_USUA});

                            // Insiro um textView para mostra o status
                            receberDadosWebservice.setTextStatus(login);

                            // Executa o asynctask
                            receberDadosWebservice.execute();
                        }
                    });
                } else {
                    SuperToast.create(getContext(), getResources().getString(R.string.nome_login_invalido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    /*@Override
    public boolean canGoForward() {
        return loggedIn;
    }*/

    private boolean validarNomeUsuario(){
        boolean valido = true;

        if ((username.getText().toString().contains(" ")) || (!Pattern.compile("^[A-Z0-9]").matcher(username.getText().toString()).find()) ){
            valido = false;
        }

        return valido;
    }
}

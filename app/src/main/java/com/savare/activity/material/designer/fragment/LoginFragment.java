package com.savare.activity.material.designer.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.savare.R;
import com.savare.activity.CadastroUsuarioActivity;
import com.savare.activity.material.designer.RegistroChaveUsuarioMDActivity;
import com.savare.banco.funcoesSql.ParametrosLocalSql;
import com.savare.banco.funcoesSql.UsuarioSQL;
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
    private TextView textStatus;
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
        textStatus = (TextView) root.findViewById(R.id.fragment_login_text_status);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validarNomeUsuario()) {
                    username.setEnabled(false);
                    password.setEnabled(false);
                    login.setEnabled(false);
                    //login.setText(R.string.aguarde_buscando_primeiros_dados);

                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText(R.string.aguarde_buscando_primeiros_dados);

                    final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    funcoes.setValorXml(funcoes.TAG_USUARIO, username.getText().toString());
                    funcoes.setValorXml(funcoes.TAG_SENHA_USUARIO, funcoes.criptografaSenha(password.getText().toString()));

                    // Verifica se o CNPJ/CPF foi salvo
                    if (!funcoes.getValorXml(funcoes.TAG_CNPJ_EMPRESA).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)){
                        // Verifica se o usuario e a senha foi salvo
                        if ( (!funcoes.getValorXml(funcoes.TAG_USUARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) &&
                             (!funcoes.getValorXml(funcoes.TAG_SENHA_USUARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ){

                            ContentValues parametros = new ContentValues();
                            parametros.put("NOME_PARAM", funcoes.TAG_USUARIO);
                            parametros.put("VALOR_PARAM", funcoes.getValorXml(funcoes.TAG_USUARIO));

                            ParametrosLocalSql parametrosLocalSql = new ParametrosLocalSql(getContext());
                            if (parametrosLocalSql.insert(parametros) > 0) {

                                parametros.put("NOME_PARAM", funcoes.TAG_CNPJ_EMPRESA);
                                parametros.put("VALOR_PARAM", funcoes.getValorXml(funcoes.TAG_CNPJ_EMPRESA));
                                // Adiciona os parametros no banco de dados
                                // Checa se inserio no banco com sucesso pra poder proseguir
                                if (parametrosLocalSql.insert(parametros) > 0) {

                                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressBarStatus.setVisibility(View.VISIBLE);

                                            ReceberDadosWebserviceAsyncRotinas receberDadosWebservice = new ReceberDadosWebserviceAsyncRotinas(
                                                    new ReceberDadosWebserviceAsyncRotinas.OnTaskCompleted() {
                                                        @Override
                                                        public void onTaskCompleted() {
                                                            // Verfifica se a marcacao AbriuAppPriveiraVez esta como Nao pra saber se foi cadastrado o dispositivo com sucesso
                                                            if ((!funcoes.getValorXml("AbriuAppPriveiraVez").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) &&
                                                                    funcoes.getValorXml("AbriuAppPriveiraVez").equalsIgnoreCase("N")) {

                                                                //SuperActivityToast.create(getActivity(), getResources().getString(R.string.usuario_cadastrado_sucesso), SuperToast.Duration.LONG, Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();

                                                                SuperActivityToast.create(getActivity(), getResources().getString(R.string.usuario_cadastrado_sucesso), Style.DURATION_LONG)
                                                                        .setTextColor(Color.WHITE)
                                                                        .setColor(Color.GREEN)
                                                                        .setAnimations(Style.ANIMATIONS_POP)
                                                                        .show();

                                                                //login.setText(R.string.logado_sucesso);
                                                                textStatus.setText(R.string.logado_sucesso);

                                                                // Deixa a barra de progresso invisivel
                                                                progressBarStatus.setVisibility(View.INVISIBLE);

                                                            } else {
                                                                new MaterialDialog.Builder(getContext())
                                                                        .title(R.string.nao_conseguimos_atualizar_usuario)
                                                                        .content("Não conseguimos receber os dados do usuário, por favor, tente novamente.")
                                                                        .positiveText(R.string.button_ok)
                                                                        .show();

                                                                ((Activity) getContext()).runOnUiThread(new Runnable() {
                                                                    public void run() {
                                                                        textStatus.setText(R.string.nao_conseguimos_atualizar_usuario);
                                                                    }
                                                                });

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
                                                    null);

                                            // Insiro um textView para mostra o status
                                            receberDadosWebservice.setTextStatus(textStatus);
                                            // Executa o asynctask
                                            receberDadosWebservice.execute();
                                        }
                                    });
                                } else {
                                    // Abilita novamente os campos e o botao
                                    username.setEnabled(true);
                                    password.setEnabled(true);
                                    login.setEnabled(true);
                                    login.setText(R.string.registrar);

                                    new MaterialDialog.Builder(getContext())
                                            .title(R.string.registrar_voce_como_usuario)
                                            .content((R.string.nao_salvou_parametros_banco) + " - Parâmetro CNPJ Empresa")
                                            .positiveText(R.string.button_ok)
                                            .show();
                                }
                            } else {
                                // Abilita novamente os campos e o botao
                                username.setEnabled(true);
                                password.setEnabled(true);
                                login.setEnabled(true);
                                login.setText(R.string.registrar);

                                new MaterialDialog.Builder(getContext())
                                        .title(R.string.registrar_voce_como_usuario)
                                        .content((R.string.nao_salvou_parametros_banco) + " - Parametro Nome de Usuário")
                                        .positiveText(R.string.button_ok)
                                        .show();
                            }
                        }
                    } else {
                        // Abilita novamente os campos e o botao
                        username.setEnabled(true);
                        password.setEnabled(true);
                        login.setEnabled(true);
                        login.setText(R.string.registrar);

                        new MaterialDialog.Builder(getContext())
                                .title(R.string.registrar_voce_como_usuario)
                                .content(R.string.nao_achamos_cnpj)
                                .positiveText(R.string.button_ok)
                                .show();
                    }
                } else {
                    //SuperActivityToast.create(getActivity(), getResources().getString(R.string.nome_login_invalido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();

                    SuperActivityToast.create(getActivity(), getResources().getString(R.string.nome_login_invalido), Style.DURATION_LONG)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
            }
        });

        return root;
    }

    private boolean validarNomeUsuario(){
        boolean valido = true;

        if ((username.getText().toString().contains(" ")) || (!Pattern.compile("^[A-Z0-9]").matcher(username.getText().toString()).find()) ){
            valido = false;
        }

        return valido;
    }
}

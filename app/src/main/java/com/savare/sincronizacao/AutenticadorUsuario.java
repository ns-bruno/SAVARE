package com.savare.sincronizacao;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

import com.savare.R;
import com.savare.beans.UsuarioBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.UsuarioRotinas;

/**
 * Created by Bruno Nogueira Silva on 03/09/2015.
 */
public class AutenticadorUsuario extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";


    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private String mAuthTokenType;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        String nomeUsuario = "";
        String emailUsuario = "";
        String senhaUsuario = "";
        String chaveUsuario = "";

        UsuarioRotinas usuarioRotinas = new UsuarioRotinas(this);
        // Pega os dados do usuario
        UsuarioBeans usuario = usuarioRotinas.usuarioCompleto(null);
        // Checa se retornou algum usuario
        if (usuario != null){

            // Checa se tem o nome do usuario
            if ((!usuario.getNomeUsuario().equals("")) && (usuario.getNomeUsuario().length() > 0)){
                nomeUsuario = usuario.getNomeUsuario();
            } else {
                nomeUsuario = getResources().getString(R.string.nao_achamos_usuario);
            }

            // Checa se tem o endereco de email
            if ((!usuario.getEmail().equalsIgnoreCase("")) && (usuario.getEmail().length() > 5)){
                emailUsuario = usuario.getEmail();
            } else {
                emailUsuario = getResources().getString(R.string.nao_achamos_endereco_email);
            }

            // Checa se tem alguma senha salva
            if ((!usuario.getSenhaUsuario().equalsIgnoreCase("")) && (usuario.getSenhaUsuario().length() > 5)){
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(this);
                // Pega a senha sem descriptografada
                senhaUsuario = funcoes.descriptografaSenha(usuario.getSenhaUsuario());
            }

            if ((!usuario.getChave().equalsIgnoreCase("")) && (usuario.getChave().length() > 0)){
                chaveUsuario = usuario.getChave();

            } else {
              chaveUsuario = KEY_ERROR_MESSAGE;
            }

            mAccountManager = AccountManager.get(this);

            Account conta = new Account(nomeUsuario, chaveUsuario);

            mAccountManager.addAccountExplicitly(conta, senhaUsuario, null);

            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, nomeUsuario);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, chaveUsuario);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, chaveUsuario);
            this.setAccountAuthenticatorResult(intent.getExtras());
            this.setResult(RESULT_OK, intent);
            this.finish();

        }
    }
}

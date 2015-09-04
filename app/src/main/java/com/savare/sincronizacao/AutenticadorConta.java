package com.savare.sincronizacao;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.savare.R;
import com.savare.sincronizacao.AutenticadorUsuario;
import com.savare.sincronizacao.ConfiguracoesSincronizacao;

/**
 * Created by Bruno Nogueira Silva on 28/08/2015.
 */
public class AutenticadorConta extends AbstractAccountAuthenticator {

    private Context context;

    public AutenticadorConta(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent dadosConta = new Intent(context, AutenticadorUsuario.class);
        dadosConta.putExtra(context.getResources().getString(R.string.ACCOUNT_TYPE), accountType);
        dadosConta.putExtra(ConfiguracoesSincronizacao.ARG_AUTH_TYPE, authTokenType != null ? authTokenType : ConfiguracoesSincronizacao.ACCOUNT_TOKEN_TYPE);
        dadosConta.putExtra(ConfiguracoesSincronizacao.ARG_IS_ADDING_NEW_ACCOUNT, true);
        dadosConta.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, dadosConta);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

        AccountManager mAccountManager = AccountManager.get(context);
        String token = mAccountManager.peekAuthToken(account, authTokenType);

        if(!TextUtils.isEmpty(token)){
            //Log.i("Script", "AccountAuthenticator.getAuthToken() : if(!TextUtils.isEmpty(token))");
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            bundle.putString(AccountManager.KEY_AUTHTOKEN, token);
            return(bundle);
        }

        Intent it = new Intent(context, AutenticadorUsuario.class);
        it.putExtra(ConfiguracoesSincronizacao.ARG_ACCOUNT_TYPE, account.type);
        it.putExtra(ConfiguracoesSincronizacao.ARG_ACCOUNT_NAME, account.name);
        it.putExtra(ConfiguracoesSincronizacao.ARG_AUTH_TYPE, authTokenType);
        it.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, it);

        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

        throw new UnsupportedOperationException();

        /*Intent it = new Intent(context, PasswordActivity.class);
        it.putExtra(Constant.ARG_ACCOUNT_TYPE, account.type);
        it.putExtra(Constant.ARG_ACCOUNT_NAME, account.name);
        it.putExtra(Constant.ARG_AUTH_TYPE, authTokenType);
        it.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, it);*/
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}

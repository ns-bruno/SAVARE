package com.savare.activity.material.designer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.savare.R;
import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.beans.ParametrosLocalBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.VersionUtils;
import com.savare.funcoes.rotinas.ParametrosLocalRotina;

import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 11/12/2015.
 */
public class LoginMDActivity extends AppCompatActivity {

    // Variaveis globais
    private TextView textUsuario,
            textCodigoUsuario;
    private EditText editSenha;
    private Button buttonEntrar;
    private Toolbar toolbarLogin;
    public static final int REQUEST_APP_SETTINGS = 1;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_SYNC_STATS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_SYNC_SETTINGS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.INTERNET,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_SYNC_SETTINGS,
            Manifest.permission.VIBRATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_md);

        // Recupera o campo para manipular
        toolbarLogin = (Toolbar) findViewById(R.id.activity_login_md_toolbar_login);
        // Adiciona uma titulo para toolbar
        toolbarLogin.setTitle(this.getResources().getString(R.string.app_name));
        toolbarLogin.setTitleTextColor(getResources().getColor(R.color.branco));
        //toolbarInicio.setLogo(R.mipmap.ic_launcher);
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarLogin);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        recuperarDadosTela();

        buttonEntrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                entrarAplicacao();
            }
        });
    } // FIm onCreate

    /**
     * Executa toda vez que a activity eh aberta
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Instancia a classe de funcoes personalizadas
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);

            if (!camposObrigatorioPreenchido()) {
                // Abre a tela inicial do sistema
                Intent intent = new Intent(LoginMDActivity.this, RegistroChaveUsuarioMDActivity.class);
                startActivity(intent);
                return;
            } else {
                if (!funcoes.hasPermissions(REQUIRED_PERMISSIONS)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(LoginMDActivity.this, REQUIRED_PERMISSIONS, REQUEST_APP_SETTINGS);
                    }
                }
                if ( funcoes.getValorXml(funcoes.TAG_CNPJ_EMPRESA).equalsIgnoreCase(funcoes.NAO_ENCONTRADO) ){

                    textCodigoUsuario.setText(funcoes.getValorXml(funcoes.TAG_UUID_DISPOSITIVO));

                } else {

                    textCodigoUsuario.setText(funcoes.getValorXml(funcoes.TAG_CNPJ_EMPRESA));
                }
                textUsuario.setText(funcoes.getValorXml(funcoes.TAG_USUARIO));
            }

            // Executa uma classe pra atualizar o banco de dados
            //UpgradeBancoDadosAsyncRotinas upgradeBancoDadosAsyncRotinas = new UpgradeBancoDadosAsyncRotinas(LoginMDActivity.this, null, UpgradeBancoDadosAsyncRotinas.APENAS_TABELA_TEMP);
            //upgradeBancoDadosAsyncRotinas.execute();
        } catch (Exception e){

            new MaterialDialog.Builder(LoginMDActivity.this)
                    .title("LoginMDActivity")
                    .content(getResources().getString(R.string.msg_error) + " - " + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        }

        editSenha.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("SAVARE", "Prescionou a tecla enter apos digitar senha - enter_key_called");

                    entrarAplicacao();
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_APP_SETTINGS:
                if (grantResults.length > 0){

                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.login, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_login_cadastro_usuario:
                // Cria uma intent para salvar o local que eh para ser aberto
                Intent intent = new Intent(LoginMDActivity.this, ListaServidoresWebserviceMDActivity.class);
                //intent.putExtra("RECADASTRAR", true);
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Recupera os dados da tela da view
     * @return
     */
    private void recuperarDadosTela(){
        textUsuario = (TextView) findViewById(R.id.activity_login_md_text_usuario);
        textCodigoUsuario = (TextView) findViewById(R.id.activity_login_md_text_codigo_usuario);
        editSenha = (EditText) findViewById(R.id.activity_login_md_edit_senha);
        buttonEntrar = (Button) findViewById(R.id.activity_login_md_button_entrar);
    }

    private boolean camposObrigatorioPreenchido(){
        boolean retorno = false;
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);
            // Checa se tem algum campo obrigatorio vazio
            if ((funcoes.getValorXml(funcoes.TAG_USUARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                    (funcoes.getValorXml(funcoes.TAG_UUID_DISPOSITIVO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {

                retorno = false;

                ConexaoBancoDeDados conexaoBancoDeDados = new ConexaoBancoDeDados(LoginMDActivity.this, VersionUtils.getVersionCode(LoginMDActivity.this));

                if (conexaoBancoDeDados.existDataBase()){
                    ParametrosLocalRotina parametrosLocalRotina = new ParametrosLocalRotina(getApplicationContext());

                    List<ParametrosLocalBeans> listaParam = parametrosLocalRotina.listaParametrosLocal(null);

                    if ((listaParam != null) && (listaParam.size() > 0)) {
                        for (ParametrosLocalBeans param : listaParam) {
                            if ((param.getNomeParam().equalsIgnoreCase(funcoes.TAG_USUARIO))) {
                                funcoes.setValorXml(funcoes.TAG_USUARIO, param.getValorParam());
                            }
                            if (param.getNomeParam().equalsIgnoreCase(funcoes.TAG_UUID_DISPOSITIVO)) {
                                funcoes.setValorXml(funcoes.TAG_UUID_DISPOSITIVO, param.getValorParam());
                            }
                        }
                        if ((funcoes.getValorXml(funcoes.TAG_USUARIO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                                (funcoes.getValorXml(funcoes.TAG_UUID_DISPOSITIVO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {
                            retorno = false;
                        } else {
                            retorno = true;
                        }
                    }
                    conexaoBancoDeDados.fechar();
                }
            } else {
                retorno = true;
            }
        } catch (Exception e){
            new MaterialDialog.Builder(LoginMDActivity.this)
                    .title("LoginMDActivity")
                    .content(getResources().getString(R.string.msg_error) + " - " + e.getMessage())
                    .positiveText(R.string.button_ok)
                    .show();
        }
        return retorno;
    }

    private void entrarAplicacao(){
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

        // Checa se existe o codigo do usuario e o nome do usuario
        if ( (funcoes.getValorXml(funcoes.TAG_USUARIO).equalsIgnoreCase(textUsuario.getText().toString())) ){

            if ( (editSenha.getText().toString().equalsIgnoreCase(funcoes.descriptografaSenha(funcoes.getValorXml(funcoes.TAG_SENHA_USUARIO)))) ){
                Intent intent = new Intent(LoginMDActivity.this, InicioMDActivity.class);
                // Tira a acitivity da pilha e inicia uma nova
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                new MaterialDialog.Builder(LoginMDActivity.this)
                        .title("LoginMDActivity")
                        .content("Senha incorreta")
                        .positiveText(R.string.button_ok)
                        .show();
            }
        } else {
            new MaterialDialog.Builder(LoginMDActivity.this)
                    .title("LoginMDActivity")
                    .content("Usuário não existe")
                    .positiveText(R.string.button_ok)
                    .show();
        }
    }

}

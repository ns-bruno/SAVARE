package com.savare.activity.material.designer;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.CadastroUsuarioActivity;
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
    private static final int REQUEST_APP_SETTINGS = 1;
    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.READ_SYNC_STATS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_SYNC_SETTINGS,
            Manifest.permission.WRITE_SYNC_SETTINGS,
            Manifest.permission.VIBRATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_md);

       /* if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
            Toast.makeText(this, "Por favor selecione todas as permissões", Toast.LENGTH_LONG).show();
            goToSettings();
        }*/
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

        if (!hasPermissions(requiredPermissions)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(requiredPermissions, REQUEST_APP_SETTINGS);
            }
        }

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

            // Verifica se os dados do dispositivo estao salvos
            if (    (funcoes.getValorXml("UuidDispositivo").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                    (funcoes.getValorXml("DescricaoDispositivo").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ){
                funcoes.setUuidDispositivo();
            }

            if (!camposObrigatorioPreenchido()) {
                ConexaoBancoDeDados conexaoBancoDeDados = new ConexaoBancoDeDados(LoginMDActivity.this, VersionUtils.getVersionCode(LoginMDActivity.this));
                // Pega o banco de dados do SAVARE
                SQLiteDatabase bancoDados = conexaoBancoDeDados.abrirBanco();
                // Executa o onCreate para criar todas as tabelas do banco de dados
                conexaoBancoDeDados.onCreate(bancoDados);

                // Abre a tela inicial do sistema
                Intent intent = new Intent(LoginMDActivity.this, RegistroChaveUsuarioMDActivity.class);
                startActivity(intent);
                return;
            } else {

                textCodigoUsuario.setText(funcoes.getValorXml("CnpjEmpresa"));
                textUsuario.setText(funcoes.getValorXml("Usuario"));
            }

            //boolean enviaAutomatico = (funcoes.getValorXml("EnviarAutomatico").equalsIgnoreCase("S") ? true : false);
            //boolean receberAutomatico = (funcoes.getValorXml("ReceberAutomatico").equalsIgnoreCase("S") ? true : false);

            // Cria o alarme se nao existir para enviar e receber dados
            //funcoes.criarAlarmeEnviarReceberDadosAutomatico(enviaAutomatico, receberAutomatico);

        }catch (Exception e){
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);

            // Armazena as informacoes para para serem exibidas e enviadas
            ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "LoginActivity");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.getMessage()));
            contentValues.put("dados", LoginMDActivity.this.toString());
            // Pega os dados do usuario

            funcoes.menssagem(contentValues);
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.login, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_login_cadastro_usuario:
                // Cria uma intent para salvar o local que eh para ser aberto
                Intent intent = new Intent(LoginMDActivity.this, CadastroUsuarioActivity.class);
                intent.putExtra("RECADASTRAR", true);
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

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);
        // Checa se tem algum campo obrigatorio vazio
        if ( (funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("CnpjEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO))/* ||
                (funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("ChaveFuncionario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("ModoConexao").equalsIgnoreCase(funcoes.NAO_ENCONTRADO))*/ ){

            ParametrosLocalRotina parametrosLocalRotina = new ParametrosLocalRotina(getApplicationContext());

            List<ParametrosLocalBeans> listaParam = parametrosLocalRotina.listaParametrosLocal(null);

            if ( (listaParam != null) && (listaParam.size() > 0) ){
                for (ParametrosLocalBeans param : listaParam){
                    if ( (param.getNomeParam().equalsIgnoreCase(funcoes.TAG_USUARIO)) ){
                        funcoes.setValorXml(funcoes.TAG_USUARIO, param.getValorParam());
                    }
                    if (param.getNomeParam().equalsIgnoreCase(funcoes.TAG_CNPJ_EMPRESA)){
                        funcoes.setValorXml(funcoes.TAG_CNPJ_EMPRESA, param.getValorParam());
                    }
                }
                if ( (funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                        (funcoes.getValorXml("CnpjEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ){
                    retorno = false;
                } else {
                    retorno = true;
                }
            }
        } else {
            retorno = true;
        }
        return retorno;
    }

    private void entrarAplicacao(){
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

        // Checa se existe o codigo do usuario e o nome do usuario
        if ( (funcoes.getValorXml("Usuario").equalsIgnoreCase(textUsuario.getText().toString())) ){

            if ( (editSenha.getText().toString().equalsIgnoreCase(funcoes.descriptografaSenha(funcoes.getValorXml("SenhaUsuario")))) ){
                Intent intent = new Intent(LoginMDActivity.this, InicioMDActivity.class);
                // Tira a acitivity da pilha e inicia uma nova
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 1);
                mensagem.put("tela", "LoginActivitys");
                mensagem.put("mensagem", "Senha incorreta");

                funcoes.menssagem(mensagem);
            }
        } else {
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "LoginActivitys");
            mensagem.put("mensagem", "Usuário não existe");
            mensagem.put("dados", textCodigoUsuario.getText().toString() + textUsuario.getText().toString());

            funcoes.menssagem(mensagem);
        }
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        if (Build.VERSION.SDK_INT > 22) {
            for (String permission : permissions)
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                    return false;
        }
        return true;
    }
}

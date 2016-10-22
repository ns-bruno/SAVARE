package com.savare.activity.material.designer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import com.savare.banco.funcoesSql.UsuarioSQL;
import com.savare.banco.local.ConexaoBancoDeDados;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.Rotinas;
import com.savare.funcoes.VersionUtils;
import com.savare.funcoes.rotinas.async.ReceberDadosWebserviceAsyncRotinas;
import com.savare.webservice.WSSisinfoWebservice;

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
            }

            // Instancia a classe de funcoes personalizadas
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);
            // Funca o codigo do usuario no xml
            String codigoUsuario = funcoes.getValorXml("CodigoUsuario");

            // Instancia a classe de rotinas
            Rotinas rotinas = new Rotinas(LoginMDActivity.this);

            // Verfifica se existe algum usuario cadastrado, ou
            if ((rotinas.existeUsuario() == false) || (codigoUsuario.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))) {

                ConexaoBancoDeDados conexaoBancoDeDados = new ConexaoBancoDeDados(LoginMDActivity.this, VersionUtils.getVersionCode(LoginMDActivity.this));
                // Pega o banco de dados do SAVARE
                SQLiteDatabase bancoDados = conexaoBancoDeDados.abrirBanco();
                // Executa o onCreate para criar todas as tabelas do banco de dados
                conexaoBancoDeDados.onCreate(bancoDados);

                // Abre a tela inicial do sistema
                //Intent intent = new Intent(LoginMDActivity.this, CadastroUsuarioActivity.class);
                Intent intent = new Intent(LoginMDActivity.this, RegistroChaveUsuarioMDActivity.class);
                startActivity(intent);

            } else {
                textCodigoUsuario.setText(codigoUsuario);
                UsuarioSQL usuarioSQL = new UsuarioSQL(LoginMDActivity.this);
                // Pega os dados do usuario(vendedor)
                Cursor dadosUsuario = usuarioSQL.query("id_usua = " + codigoUsuario);

                if ((dadosUsuario != null) && (dadosUsuario.getCount() > 0)) {
                    // Move para o primeiro registro
                    dadosUsuario.moveToFirst();
                    // Preenche os campos com os dados do usuario(vendedor)
                    textUsuario.setText(dadosUsuario.getString(dadosUsuario.getColumnIndex("LOGIN_USUA")));
                }
            }
            // Cria o alarme se nao existir para enviar e receber dados
            funcoes.criarAlarmeEnviarReceberDadosAutomatico(true, true);

        }catch (Exception e){
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);

            // Armazena as informacoes para para serem exibidas e enviadas
            ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "LoginActivity");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.getMessage()));
            contentValues.put("dados", LoginMDActivity.this.toString());
            // Pega os dados do usuario

            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));

            funcoes.menssagem(contentValues);
        }

        editSenha.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("SAVARE", "enter_key_called");

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
        if ((funcoes.getValorXml("Usuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("ChaveUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                (funcoes.getValorXml("ModoConexao").equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
            retorno = false;

        } else {
            retorno = true;
        }
        return retorno;
    }

    private void entrarAplicacao(){
        Rotinas rotinas = new Rotinas(LoginMDActivity.this);

        // Checa se existe o codigo do usuario e o nome do usuario
        if (rotinas.checaUsuario(textCodigoUsuario.getText().toString(), textUsuario.getText().toString())) {

            // Checa se a senha esta certa
            try {
                if (rotinas.checaUsuarioESenha(textCodigoUsuario.getText().toString(), textUsuario.getText().toString(), editSenha.getText().toString()) == true) {
                    // Abre a tela inicial do sistema
                    //Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
                    Intent intent = new Intent(LoginMDActivity.this, InicioMDActivity.class);
                    // Tira a acitivity da pilha e inicia uma nova
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Mostra uma mensagem caso a senha esteja errada
                } else {
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 1);
                    mensagem.put("tela", "LoginActivitys");
                    mensagem.put("mensagem", "Senha incorreta");

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);
                    funcoes.menssagem(mensagem);
                }
            } catch (Exception e) {
                ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 1);
                mensagem.put("tela", "LoginActivitys");
                mensagem.put("mensagem", "Senha incorreta \n" + e.getMessage());
                mensagem.put("dados", e.toString());

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);
                funcoes.menssagem(mensagem);
            }

            // Mostra uma mensagem caso usuario esteja errado
        } else {
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "LoginActivitys");
            mensagem.put("mensagem", "Usuário não existe");
            mensagem.put("dados", textCodigoUsuario.getText().toString() + textUsuario.getText().toString());

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(LoginMDActivity.this);
            funcoes.menssagem(mensagem);
        }
    }
}

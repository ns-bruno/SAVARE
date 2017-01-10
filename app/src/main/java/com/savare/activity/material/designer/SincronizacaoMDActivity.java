package com.savare.activity.material.designer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ImportarDadosTxtRotinas;
import com.savare.funcoes.rotinas.UsuarioRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosFtpAsyncRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosWebserviceAsyncRotinas;
import com.savare.webservice.WSSisinfoWebservice;

/**
 * Created by Bruno Nogueira Silva on 18/04/2016.
 */
public class SincronizacaoMDActivity extends AppCompatActivity {

    private ProgressBar progressReceberDados,
            progressReceberDadosEmpresa,
            progressReceberDadosClientes,
            progressReceberDadosProdutos,
            progressReceberDadosTitulos;
    private TextView textDataUltimoEnvio,
            textDataUltimoRecebimento,
            textReceberDados,
            textReceberDadosEmpresa,
            textReceberDadosClientes,
            textReceberDadosProdutos,
            textReceberDadosTitulos;
    private Button buttonReceberDados,
            buttonReceberDadosEmpresa,
            buttonReceberDadosClientes,
            buttonReceberDadosProdutos,
            buttonReceberDadosTitulos;
    private String modoConexao = "";
    private Toolbar toolbarCabecalho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizacao_md);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoMDActivity.this);

        funcoes.bloqueiaOrientacaoTela();

        recuperaCampos();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Instancia a classe de rotinas da tabela usuario
        UsuarioRotinas usuarioRotinas = new UsuarioRotinas(SincronizacaoMDActivity.this);

        // Pega a data do ultimo envio de pedido
        textDataUltimoEnvio.setText(usuarioRotinas.dataUltimoEnvio(funcoes.getValorXml("CodigoUsuario")));
        // Pega a data do ultimo recebimento de dados
        textDataUltimoRecebimento.setText(usuarioRotinas.dataUltimoRecebimento(funcoes.getValorXml("CodigoUsuario")));

        buttonReceberDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoMDActivity.this, progressReceberDados, textReceberDados);
                //receberDadosFtpAsync.execute();

                ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(SincronizacaoMDActivity.this);
                receberDados.setProgressBarStatus(progressReceberDados);
                receberDados.setTextStatus(textReceberDados);
                receberDados.execute();
            }
        });

        buttonReceberDadosEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoMDActivity.this, progressReceberDadosEmpresa, textReceberDadosEmpresa);
                //receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_S);

                String[] tabelaRecebeDados = {  WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE};

                ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(SincronizacaoMDActivity.this);
                receberDados.setProgressBarStatus(progressReceberDadosEmpresa);
                receberDados.setTextStatus(textReceberDadosEmpresa);
                receberDados.setTabelaRecebeDados(tabelaRecebeDados);
                receberDados.execute();
            }
        });

        buttonReceberDadosClientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoMDActivity.this, progressReceberDadosClientes, textReceberDadosClientes);
                //receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_C);


                String[] tabelaRecebeDados = {  WSSisinfoWebservice.FUNCTION_SELECT_CFAAREAS,
                                                WSSisinfoWebservice.FUNCTION_SELECT_SMAEMPRE,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAATIVI,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFASTATU,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFATPDOC,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFACCRED,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAPORTA,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAPROFI,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFATPCLI,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFATPCOB,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAESTAD,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFACIDAD,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFACLIFO,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAENDER,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAPARAM,
                                                WSSisinfoWebservice.FUNCTION_SELECT_CFAFOTOS};

                ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(SincronizacaoMDActivity.this);
                receberDados.setProgressBarStatus(progressReceberDadosClientes);
                receberDados.setTextStatus(textReceberDadosClientes);
                receberDados.setTabelaRecebeDados(tabelaRecebeDados);
                receberDados.execute();
            }
        });

        buttonReceberDadosProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoMDActivity.this, progressReceberDadosProdutos, textReceberDadosProdutos);
                //receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_A);

                String[] tabelaRecebeDados = {  WSSisinfoWebservice.FUNCTION_SELECT_AEAPLPGT,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEACLASE,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAUNVEN,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAGRADE,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAMARCA,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEACODST,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAPRODU,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAEMBAL,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAPLOJA,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEALOCES,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAESTOQ,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAORCAM,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAITORC,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAPERCE,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAFATOR,
                                                WSSisinfoWebservice.FUNCTION_SELECT_AEAPRREC};

                ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(SincronizacaoMDActivity.this);
                receberDados.setProgressBarStatus(progressReceberDadosProdutos);
                receberDados.setTextStatus(textReceberDadosProdutos);
                receberDados.setTabelaRecebeDados(tabelaRecebeDados);
                receberDados.execute();
            }
        });

        buttonReceberDadosTitulos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReceberDadosFtpAsyncRotinas receberDadosFtpAsync = new ReceberDadosFtpAsyncRotinas(SincronizacaoMDActivity.this, progressReceberDadosTitulos, textReceberDadosTitulos);
                //receberDadosFtpAsync.execute(ImportarDadosTxtRotinas.BLOCO_R);

                String[] tabelaRecebeDados = {  WSSisinfoWebservice.FUNCTION_SELECT_RPAPARCE};

                ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(SincronizacaoMDActivity.this);
                receberDados.setProgressBarStatus(progressReceberDadosTitulos);
                receberDados.setTextStatus(textReceberDadosTitulos);
                receberDados.setTabelaRecebeDados(tabelaRecebeDados);
                receberDados.execute();
            }
        });
    } // Fim onCreate

    @Override
    protected void onResume() {
        super.onResume();

        UsuarioRotinas usuarioRotinas = new UsuarioRotinas(SincronizacaoMDActivity.this);
        // Pega o modo de sincronizacao dos dados. FTP ou SavareSyncAdapter
        modoConexao = usuarioRotinas.modoConexao();

        if (modoConexao.equalsIgnoreCase("S")){
            textDataUltimoEnvio.setVisibility(View.INVISIBLE);
            textDataUltimoRecebimento.setVisibility(View.INVISIBLE);
            textReceberDados.setVisibility(View.INVISIBLE);
            textReceberDadosEmpresa.setVisibility(View.INVISIBLE);
            textReceberDadosClientes.setVisibility(View.INVISIBLE);
            textReceberDadosProdutos.setVisibility(View.INVISIBLE);
            textReceberDadosTitulos.setVisibility(View.INVISIBLE);
            buttonReceberDados.setVisibility(View.INVISIBLE);
            buttonReceberDadosEmpresa.setVisibility(View.INVISIBLE);
            buttonReceberDadosClientes.setVisibility(View.INVISIBLE);
            buttonReceberDadosProdutos.setVisibility(View.INVISIBLE);
            buttonReceberDadosTitulos.setVisibility(View.INVISIBLE);
            progressReceberDados.setVisibility(View.INVISIBLE);
            progressReceberDadosEmpresa.setVisibility(View.INVISIBLE);
            progressReceberDadosClientes.setVisibility(View.INVISIBLE);
            progressReceberDadosProdutos.setVisibility(View.INVISIBLE);
            progressReceberDadosTitulos.setVisibility(View.INVISIBLE);
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoMDActivity.this);
        funcoes.desbloqueiaOrientacaoTela();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.sincronizacao_md, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Checo se o modo de conexao eh diferente de SavareSyncAdapter
        if (!modoConexao.equalsIgnoreCase("S")){
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;

            case  R.id.menu_sincronizacao_md_atualizar:

                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoMDActivity.this);
                funcoes.TriggerRefresh(SincronizacaoMDActivity.this);

                break;
            case R.id.menu_sincronizacao_md_desmarcar_transmissao_dados:
                FuncoesPersonalizadas funcoesExtra = new FuncoesPersonalizadas(SincronizacaoMDActivity.this);

                // Marca a aplicacao que nao esta mais recebendo dados
                funcoesExtra.setValorXml("RecebendoDados", "N");
                funcoesExtra.setValorXml("EnviandoDados", "N");
                funcoesExtra.cancelarSincronizacaoSegundoPlano();
                break;

            default:
                break;
        }
        return true;
    } // Fim do onOptionsItemSelectedK

    private void recuperaCampos(){
        textDataUltimoEnvio = (TextView) findViewById(R.id.activity_sincronizacao_md_text_data_ultimo_envio);
        textDataUltimoRecebimento = (TextView) findViewById(R.id.activity_sincronizacao_md_text_data_ultimo_recebimento);
        textReceberDados = (TextView) findViewById(R.id.activity_sincronizacao_md_textView_receber_dados);
        textReceberDadosEmpresa = (TextView) findViewById(R.id.activity_sincronizacao_md_textView_receber_dados_bloco_s);
        textReceberDadosClientes = (TextView) findViewById(R.id.activity_sincronizacao_md_textView_receber_dados_bloco_c);
        textReceberDadosProdutos = (TextView) findViewById(R.id.activity_sincronizacao_md_textView_receber_dados_bloco_a);
        textReceberDadosTitulos = (TextView) findViewById(R.id.activity_sincronizacao_md_textView_receber_dados_bloco_r);
        buttonReceberDados = (Button) findViewById(R.id.activity_sincronizacao_md_button_receber_dados);
        buttonReceberDadosEmpresa = (Button) findViewById(R.id.activity_sincronizacao_md_button_receber_dados_bloco_s);
        buttonReceberDadosClientes = (Button) findViewById(R.id.activity_sincronizacao_md_button_receber_dados_bloco_c);
        buttonReceberDadosProdutos = (Button) findViewById(R.id.activity_sincronizacao_md_button_receber_dados_bloco_a);
        buttonReceberDadosTitulos = (Button) findViewById(R.id.activity_sincronizacao_md_button_receber_dados_bloco_r);
        progressReceberDados = (ProgressBar) findViewById(R.id.activity_sincronizacao_md_progressBar_receber_dados);
        progressReceberDadosEmpresa = (ProgressBar) findViewById(R.id.activity_sincronizacao_md_progressBar_receber_dados_bloco_s);
        progressReceberDadosClientes = (ProgressBar) findViewById(R.id.activity_sincronizacao_md_progressBar_receber_dados_bloco_c);
        progressReceberDadosProdutos = (ProgressBar) findViewById(R.id.activity_sincronizacao_md_progressBar_receber_dados_bloco_a);
        progressReceberDadosTitulos = (ProgressBar) findViewById(R.id.activity_sincronizacao_md_progressBar_receber_dados_bloco_r);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_sincronizacao_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(getResources().getString(R.string.sincronizacao));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

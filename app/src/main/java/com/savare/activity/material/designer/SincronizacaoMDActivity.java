package com.savare.activity.material.designer;

import android.content.Intent;
import android.graphics.Color;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.beans.PessoaBeans;
import com.savare.beans.UltimaAtualizacaoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.UltimaAtualizacaoRotinas;
import com.savare.funcoes.rotinas.async.ReceberDadosWebserviceAsyncRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 18/04/2016.
 */
public class SincronizacaoMDActivity extends AppCompatActivity {

    private ProgressBar progressReceberDados;
    private TextView textDataUltimoEnvio,
            textDataUltimoRecebimento,
            textReceberDados,
            textStatus;
    private Button buttonReceberDados;
    private Toolbar toolbarCabecalho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizacao_md);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(SincronizacaoMDActivity.this);

        funcoes.bloqueiaOrientacaoTela();

        recuperaCampos();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(getApplicationContext());

        ArrayList<UltimaAtualizacaoBeans> listaUltimaAtualizacao = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas("AEAORCAM");

        if ((listaUltimaAtualizacao != null) && (listaUltimaAtualizacao.size() > 0)){
            textDataUltimoEnvio.setText(funcoes.formataDataHora(listaUltimaAtualizacao.get(0).getDataUltimaAtualizacao()));
        } else {
            textDataUltimoEnvio.setText("");
        }
        // Pega a data do ultimo recebimento de dados
        listaUltimaAtualizacao = ultimaAtualizacaoRotinas.listaUltimaAtualizacaoTabelas("SMAEMPRE");

        if ((listaUltimaAtualizacao != null) && (listaUltimaAtualizacao.size() > 0)){
            textDataUltimoRecebimento.setText(funcoes.formataDataHora(listaUltimaAtualizacao.get(0).getDataUltimaAtualizacao()));
        } else {
            textDataUltimoRecebimento.setText("");
        }

        buttonReceberDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReceberDadosWebserviceAsyncRotinas receberDados = new ReceberDadosWebserviceAsyncRotinas(SincronizacaoMDActivity.this);
                receberDados.setProgressBarStatus(progressReceberDados);
                receberDados.setTextStatus(textReceberDados);
                receberDados.setTextStatusErro(textStatus);
                receberDados.execute();
            }
        });


    } // Fim onCreate

    @Override
    protected void onResume() {
        super.onResume();

        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getApplicationContext());

        if(!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) {
            PessoaRotinas pessoaRotinas = new PessoaRotinas(getApplicationContext());
            // Pega os dados do usuario
            List<PessoaBeans> dadosUsuario = pessoaRotinas.listaPessoaResumido("CODIGO_FUN = " + funcoes.getValorXml("CodigoUsuario"), PessoaRotinas.KEY_TIPO_FUNCIONARIO, null);

            // Checa se retornou algum dados do usuario
            if (dadosUsuario != null && dadosUsuario.size() > 0) {

                if ((dadosUsuario.get(0).getAtivo() != null) && (dadosUsuario.get(0).getAtivo().equalsIgnoreCase("0"))){

                    (SincronizacaoMDActivity.this).runOnUiThread(new Runnable() {
                        public void run() {
                            new MaterialDialog.Builder(SincronizacaoMDActivity.this)
                                    .title("SincronizacaoMDActivity")
                                    .content(R.string.usuario_inativo)
                                    .positiveText(R.string.button_ok)
                                    .show();
                        }
                    });
                }
            }
        }

        UltimaAtualizacaoRotinas ultimaAtualizacaoRotinas = new UltimaAtualizacaoRotinas(getApplicationContext());

        if(ultimaAtualizacaoRotinas.muitoTempoSemSincronizacao()){
            (SincronizacaoMDActivity.this).runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(SincronizacaoMDActivity.this)
                            .title("SincronizacaoMDActivity")
                            .content("Tem mais de 10 dias sem fazer sincronização. Por favor realize uma sincronização para continuar usando o aplicativo.")
                            .positiveText(R.string.button_ok)
                            .show();
                }
            });
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
        menu.getItem(0).setVisible(false);
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

            case R.id.menu_sincronizacao_md_zerar_datas_sincronizacao:
                UltimaAtualizacaoRotinas atualizacaoRotinas = new UltimaAtualizacaoRotinas(SincronizacaoMDActivity.this);
                if (atualizacaoRotinas.apagarDatasSincronizacao() > 0){

                    SuperActivityToast.create(this, this.getResources().getString(R.string.zerado_sucesso), Style.DURATION_SHORT)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.GREEN)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
                break;
            case R.id.menu_sincronizacao_md_cadastro_servidor:
                // Cria uma intent para salvar o local que eh para ser aberto
                Intent intent = new Intent(SincronizacaoMDActivity.this, ListaServidoresWebserviceMDActivity.class);
                startActivity(intent);
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
        textStatus = (TextView) findViewById(R.id.activity_sincronizacao_md_textView_status);
        buttonReceberDados = (Button) findViewById(R.id.activity_sincronizacao_md_button_receber_dados);
        progressReceberDados = (ProgressBar) findViewById(R.id.activity_sincronizacao_md_progressBar_receber_dados);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_sincronizacao_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(getResources().getString(R.string.sincronizacao));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

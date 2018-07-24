package com.savare.activity.material.designer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.savare.R;
import com.savare.banco.funcoesSql.EmpresaSql;
import com.savare.banco.funcoesSql.PercentualSql;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.PercentualRotinas;

/**
 * Created by Faturamento on 18/04/2016.
 */
public class ConfiguracoesMDActivity extends AppCompatActivity {

    private Button buttonTamanhoFonte;
    private CheckBox checkEnviarAutomatico,
            checkReceberAutomatico;
    private Toolbar toolbarCabecalho;
    private EditText editQuantidadeCasasDecimais,
            editMarkUpAtacado,
            editMarkUpVarejo;
    double markUpAtacado;
    double markUpVarejo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_md);

        recuperarCampos();

        final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);

        // Checa o tamanho da fonte salvo
        if(funcoes.getValorXml("TamanhoFonte").equalsIgnoreCase("M")){
            buttonTamanhoFonte.setText("Tamanho do Texto: Médio");
            buttonTamanhoFonte.setTextSize(16);

        } else if(funcoes.getValorXml("TamanhoFonte").equalsIgnoreCase("G")){
            buttonTamanhoFonte.setText("Tamanho do Texto: Grande");
            buttonTamanhoFonte.setTextSize(21);

        } else {
            buttonTamanhoFonte.setText("Tamanho do Texto: Normal");
        }


        // Checa se esta pra enviar automatico
        if(funcoes.getValorXml("EnviarAutomatico").equalsIgnoreCase("S")){
            checkEnviarAutomatico.setChecked(true);
        }else {
            checkEnviarAutomatico.setChecked(false);
        }

        // Checa se esta pra enviar automatico
        if(funcoes.getValorXml("ReceberAutomatico").equalsIgnoreCase("S")){
            checkReceberAutomatico.setChecked(true);
        }else {
            checkReceberAutomatico.setChecked(false);
        }

        if(funcoes.getValorXml("CasasDecimais").length() == 1){
            editQuantidadeCasasDecimais.setText(funcoes.getValorXml("CasasDecimais"));
        }else{
            editQuantidadeCasasDecimais.setText("3");
        }

        PercentualRotinas percentualRotinas = new PercentualRotinas(ConfiguracoesMDActivity.this);

        markUpAtacado = percentualRotinas.percentualMarkUpAtacado(funcoes.getValorXml("CodigoUsuario"));
        markUpVarejo = percentualRotinas.percentualMarkUpVarejo(funcoes.getValorXml("CodigoUsuario"));

        if(markUpAtacado >= 0){
            //String s = ""+funcoes.arredondarValor(markUpAtacado);
            editMarkUpAtacado.setText(""+funcoes.arredondarValor(markUpAtacado));

        }else {
            editMarkUpAtacado.setText(funcoes.arredondarValor(0));
        }

        if(markUpVarejo >= 0){
            editMarkUpVarejo.setText(funcoes.arredondarValor(markUpVarejo));

        } else {
            editMarkUpVarejo.setText(funcoes.arredondarValor(0));
        }


        buttonTamanhoFonte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria um dialog para selecionar atacado ou varejo
                AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(ConfiguracoesMDActivity.this);
                // Atributo(variavel) para escolher o tipo da venda
                final String[] opcao = {"Normal", "Médio", "Grande"};
                // Preenche o dialogo com o titulo e as opcoes
                mensagemAtacadoVarejo.setTitle("Tamanho dos Textos").setItems(opcao, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Checa qual opcao foi selecionado
                        if(which == 0){
                            // Salva N para fontes normais
                            funcoes.setValorXml("TamanhoFonte", "N");
                            buttonTamanhoFonte.setText("Tamanho do Texto: Normal");
                            buttonTamanhoFonte.setTextSize(13);

                        } else if(which == 1){
                            // Salva M para fontes Medias
                            funcoes.setValorXml("TamanhoFonte", "M");
                            buttonTamanhoFonte.setText("Tamanho do Texto: Médio");
                            buttonTamanhoFonte.setTextSize(16);

                        } else if(which == 2){
                            // Salva G para fontes Grandes
                            funcoes.setValorXml("TamanhoFonte", "G");
                            buttonTamanhoFonte.setText("Tamanho do Texto: Grande");
                            buttonTamanhoFonte.setTextSize(21);
                        }

                    }});

                // Faz a mensagem (dialog) aparecer
                mensagemAtacadoVarejo.show();
            }
        });

        // Pega os clique do checkBom Enviar Automatico
        checkEnviarAutomatico.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FuncoesPersonalizadas funcoesP = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);

                if(checkEnviarAutomatico.isChecked()){
                    funcoesP.setValorXml("EnviarAutomatico", "S");
                }else {
                    funcoesP.setValorXml("EnviarAutomatico", "N");
                }

                funcoesP.criarAlarmeEnviarAutomatico(checkEnviarAutomatico.isChecked());
            }
        });

        // Pega os clique do checkBom Enviar Automatico
        checkReceberAutomatico.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FuncoesPersonalizadas funcoesP = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);

                if(checkReceberAutomatico.isChecked()){
                    funcoesP.setValorXml("ReceberAutomatico", "S");

                }else {
                    funcoesP.setValorXml("ReceberAutomatico", "N");
                }

                funcoesP.criarAlarmeReceberAutomatico(checkReceberAutomatico.isChecked());
            }
        });


        editMarkUpAtacado.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editMarkUpAtacado.setText("");
                return false;
            }
        });


        editMarkUpVarejo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editMarkUpVarejo.setText("");
                return false;
            }
        });

        editQuantidadeCasasDecimais.setSelection(editQuantidadeCasasDecimais.getText().length());
    } // Fim onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.configuracoes_md, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                finish();
                break;

            case R.id.menu_configuracoes_md_salvar:
                // Checa s efoi preenchido alguma coisa
                if(editMarkUpVarejo.getText().length() > 0){

                    if (markUpVarejo >= 0) {
                        PercentualSql percentualSql = new PercentualSql(ConfiguracoesMDActivity.this);

                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);
                        // Pega o codigo de usuario
                        int codigoUsuario = (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? Integer.parseInt(funcoes.getValorXml("CodigoUsuario")) : -1;
                        int codigoEmpresa = (!funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? Integer.parseInt(funcoes.getValorXml("CodigoEmpresa")) : -1;



                        if (codigoEmpresa > 0 && codigoUsuario > 0) {

                            String sqlUsuarioTemPerce =
                                      "SELECT COUNT(*) AS EXISTE_CONFIG FROM AEAPERCE "
                                    + "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) \n"
                                    + "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n"
                                    + "WHERE (CFACLIFO.CODIGO_FUN = " + codigoUsuario + ") AND (CFACLIFO.FUNCIONARIO = '1') AND (CFACLIFO.ID_SMAEMPRE = " + codigoEmpresa + ")";

                            Cursor dados = percentualSql.sqlSelect(sqlUsuarioTemPerce);

                            // Checa se retornou alguma informacao do banco de dados
                            if (dados != null && dados.getCount() > 0){
                                // Move para o primeiro cursor
                                dados.moveToFirst();
                                // Checa se existe algum parametro para este usuario
                                if (dados.getInt(dados.getColumnIndex("EXISTE_CONFIG")) > 0){

                                    String sql = "UPDATE AEAPERCE SET MARKUP_VARE = " + funcoes.desformatarValor(editMarkUpVarejo.getText().toString())
                                            + " WHERE ID_AEAPERCE = (SELECT AEAPERCE.ID_AEAPERCE FROM AEAPERCE "
                                            + "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) \n"
                                            + "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n"
                                            + "WHERE (CFACLIFO.CODIGO_FUN = " + codigoUsuario + ") AND (CFACLIFO.FUNCIONARIO = '1') AND (CFACLIFO.ID_SMAEMPRE = " + codigoEmpresa + "))";
                                    percentualSql.execSQL(sql);
                                }
                            }
                        }

                    } else {
                        ContentValues dadosMensagem = new ContentValues();

                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);

                        dadosMensagem.put("comando", 1);
                        dadosMensagem.put("tela", "ConfiguracaoesMDActivity - Varejo");
                        dadosMensagem.put("mensagem", getResources().getString(R.string.nao_existe_parametros_tabela_percentuais_usuario));

                        funcoes.menssagem(dadosMensagem);
                    }
                }

                // Checa se foi preenchido alguma coisa
                if(editMarkUpAtacado.getText().length() > 0){

                    if (markUpAtacado >= 0) {
                        PercentualSql percentualSql = new PercentualSql(ConfiguracoesMDActivity.this);

                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);
                        // Pega o codigo de usuario
                        int codigoUsuario = (!funcoes.getValorXml("CodigoUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? Integer.parseInt(funcoes.getValorXml("CodigoUsuario")) : -1;
                        int codigoEmpresa = (!funcoes.getValorXml("CodigoEmpresa").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ? Integer.parseInt(funcoes.getValorXml("CodigoEmpresa")) : -1;

                        if (codigoEmpresa > 0 && codigoUsuario > 0) {

                            String sqlUsuarioTemPerce =
                                    "SELECT COUNT(*) AS EXISTE_CONFIG FROM AEAPERCE "
                                            + "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) \n"
                                            + "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n"
                                            + "WHERE (CFACLIFO.CODIGO_FUN = " + codigoUsuario + ") AND (CFACLIFO.FUNCIONARIO = '1') AND (CFACLIFO.ID_SMAEMPRE = " + codigoEmpresa + ")";

                            Cursor dados = percentualSql.sqlSelect(sqlUsuarioTemPerce);

                            // Checa se retornou alguma informacao do banco de dados
                            if (dados != null && dados.getCount() > 0){
                                // Move para o primeiro cursor
                                dados.moveToFirst();
                                // Checa se existe algum parametro para este usuario
                                if (dados.getInt(dados.getColumnIndex("EXISTE_CONFIG")) > 0){

                                    String sql = "UPDATE AEAPERCE SET MARKUP_ATAC = " + funcoes.desformatarValor(editMarkUpAtacado.getText().toString())
                                            + " WHERE ID_AEAPERCE = (SELECT AEAPERCE.ID_AEAPERCE FROM AEAPERCE "
                                            + "LEFT OUTER JOIN CFAPARAM ON (AEAPERCE.ID_CFAPARAM_VENDEDOR = CFAPARAM.ID_CFAPARAM) \n"
                                            + "LEFT OUTER JOIN CFACLIFO ON (CFAPARAM.ID_CFACLIFO = CFACLIFO.ID_CFACLIFO) \n"
                                            + "WHERE (CFACLIFO.CODIGO_FUN = " + codigoUsuario + ") AND (CFACLIFO.FUNCIONARIO = '1') AND (CFACLIFO.ID_SMAEMPRE = " + codigoEmpresa + "))";
                                    percentualSql.execSQL(sql);
                                }
                            }
                        }
                    } else {
                        ContentValues dadosMensagem = new ContentValues();

                        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);

                        dadosMensagem.put("comando", 1);
                        dadosMensagem.put("tela", "ConfiguracaoesMDActivity - Atacado");
                        dadosMensagem.put("mensagem", getResources().getString(R.string.nao_existe_parametros_tabela_percentuais_usuario));

                        funcoes.menssagem(dadosMensagem);
                    }
                }

                if(editQuantidadeCasasDecimais.getText().length() > 0){
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ConfiguracoesMDActivity.this);
                    // Atucaliza o xml com a quantidade de casas decimais
                    funcoes.setValorXml("CasasDecimais", editQuantidadeCasasDecimais.getText().toString());

                    EmpresaSql empresaSql = new EmpresaSql(ConfiguracoesMDActivity.this);

                    ContentValues dados = new ContentValues();
                    dados.put("QTD_CASAS_DECIMAIS", editQuantidadeCasasDecimais.getText().toString());

                    empresaSql.update(dados, "ID_SMAEMPRE = " + funcoes.getValorXml("CodigoEmpresa"));
                }

                if ((markUpVarejo >= 0) && (markUpAtacado >= 0)) {
                    finish();
                }
                break;

            default:
                break;
        }
        return true;
    }

    private void recuperarCampos(){
        buttonTamanhoFonte = (Button) findViewById(R.id.activity_configuracoes_md_button_tamanho_fonte);
        checkEnviarAutomatico = (CheckBox) findViewById(R.id.activity_configuracoes_md_checkBox_enviar_automatico);
        checkReceberAutomatico = (CheckBox) findViewById(R.id.activity_configuracoes_md_checkBox_receber_automatico);
        editQuantidadeCasasDecimais = (EditText) findViewById(R.id.activity_configuracoes_md_edit_quantidade_casas_decimais);
        editMarkUpAtacado = (EditText) findViewById(R.id.activity_configuracoes_md_edit_percentual_mark_up_atacado);
        editMarkUpVarejo = (EditText) findViewById(R.id.activity_configuracoes_md_edit_percentual_mark_up_varejo);
        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_configuracoes_md_toolbar_cabecalho);
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        toolbarCabecalho.setTitle(getResources().getString(R.string.configuracoes));
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
        // Adiciona o botao voltar no toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

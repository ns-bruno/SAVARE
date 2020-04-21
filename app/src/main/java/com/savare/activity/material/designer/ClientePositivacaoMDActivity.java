package com.savare.activity.material.designer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.PositivacaoSql;
import com.savare.beans.CfapositBeans;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.CfapositRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.GeraPdfClientePositivacaoAsyncRotinas;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ClientePositivacaoMDActivity extends AppCompatActivity {
    public static String    TIPO_SEMANA = "S",
                            TIPO_MES = "M",
                            TIPO_PERSONALIZADO = "P";
    private String  dataInicial,
                    dataFinal;
    private ProgressBar progressBarStatus;
    private Toolbar toolbarInicio;
    private ListView listViewClientePositivacao;
    private ItemUniversalAdapter adapterClientePositivacao;
    private List<CfapositBeans> listaPositivacao;
    int anoInicialSelecinado = -1;
    int mesInicialSelecionado = -1;
    int diaInicialSelecionado = -1;
    int anoFinalSelecinado = -1;
    int mesFinalSelecionado = -1;
    int diaFinalSelecionado = -1;
    private String whereAuxiliar;
    private Dialog dialogPeriodo;
    private TextView textDataFinal;
    private TextView textDataInicial;
    private TextView textDataFiltro;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cliente_positivacao);

        recuperaCampos();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cliente_positivacao, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final CarregarListaTitulos carregarListaTitulos = new CarregarListaTitulos();
        Calendar now = Calendar.getInstance();
        int semanaAno = now.get(Calendar.WEEK_OF_YEAR);
        int mesAno = now.get(Calendar.MONTH);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_cliente_positivacao_semana_atual:
                semanaAno --;
                whereAuxiliar = "STRFTIME('%W', DATA_VISITA) = '" + ((semanaAno < 10) ? "0" + semanaAno : semanaAno) + "'";
                // Preenche a data que esta sendo pesquisada
                PreencheTextDataFiltro(TIPO_SEMANA, (semanaAno < 10) ? "0" + semanaAno : ""+semanaAno);
                carregarListaTitulos.execute();
                break;

            case R.id.menu_cliente_positivacao_semana_anterior:
                semanaAno = semanaAno -2;
                whereAuxiliar = "STRFTIME('%W', DATA_VISITA) = '" + ((semanaAno < 10) ? "0" + semanaAno : semanaAno) + "'";
                // Preenche a data que esta sendo pesquisada
                PreencheTextDataFiltro(TIPO_SEMANA, (semanaAno < 10) ? "0" + semanaAno : ""+semanaAno);
                carregarListaTitulos.execute();
                break;

            case R.id.menu_cliente_positivacao_mes_atual:
                mesAno ++;
                whereAuxiliar = "STRFTIME('%m', DATA_VISITA) = '" + ((mesAno < 10) ? "0" + mesAno : mesAno) + "'";
                // Preenche a data que esta sendo pesquisada
                PreencheTextDataFiltro(TIPO_MES, (mesAno < 10) ? "0" + mesAno : ""+mesAno);
                carregarListaTitulos.execute();
                break;

            case R.id.menu_cliente_positivacao_mes_anterior:
                whereAuxiliar = "STRFTIME('%m', DATA_VISITA) = '" + ((mesAno < 10) ? "0" + mesAno : mesAno) + "'";
                // Preenche a data que esta sendo pesquisada
                PreencheTextDataFiltro(TIPO_MES, (mesAno < 10) ? "0" + mesAno : ""+mesAno);
                carregarListaTitulos.execute();
                break;

            case R.id.menu_cliente_positivacao_intervalo_personalizado:
                dialogPeriodo = new Dialog(ClientePositivacaoMDActivity.this);
                // Seta o layout customizado para o dialog
                dialogPeriodo.setContentView(R.layout.layout_dialog_periodo_data);
                // Seta o titulo do dialog
                dialogPeriodo.setTitle("Período");
                dialogPeriodo.setCancelable(true);

                // Associa o campo do dialog customizado
                textDataInicial = (TextView) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_inicial);
                // Checa se ja existe alguma data selecionada
                if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
                    textDataInicial.setText(diaInicialSelecionado + "/" + mesInicialSelecionado + "/" + anoInicialSelecinado);
                }

                // Desailita a edicao do campo
                textDataInicial.setFocusable(false);
                // Pega os clique do campo
                textDataInicial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Process to get Current Date
                        final Calendar c = Calendar.getInstance();

                        if(anoInicialSelecinado < 0){
                            anoInicialSelecinado = c.get(Calendar.YEAR);
                        }
                        if(mesInicialSelecionado < 0){
                            mesInicialSelecionado = c.get(Calendar.MONTH);
                        }
                        if(diaInicialSelecionado < 0){
                            diaInicialSelecionado = c.get(Calendar.DAY_OF_MONTH);
                        }

                        // Launch Date Picker Dialog
                        DatePickerDialog dataDialog = new DatePickerDialog(ClientePositivacaoMDActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Preenche o campo com a data
                                textDataInicial.setText(dayOfMonth+"/" + (monthOfYear + 1) + "/"+year);
                                // Seta o calendario com a data selecionada
                                //c.set(year, monthOfYear + 1, dayOfMonth);

                                anoInicialSelecinado = year;
                                mesInicialSelecionado = (monthOfYear + 1);
                                diaInicialSelecionado = dayOfMonth;
                            }
                        }, anoInicialSelecinado, mesInicialSelecionado, diaInicialSelecionado);
                        dataDialog.show();
                    }
                });
                textDataFinal = (TextView) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_final);
                // Checa se ja existe alguma data selecionada
                if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
                    textDataFinal.setText(diaFinalSelecionado + "/" + mesFinalSelecionado + "/" + anoFinalSelecinado);
                }
                textDataFinal.setFocusable(false);
                textDataFinal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Process to get Current Date
                        final Calendar c = Calendar.getInstance();

                        if(anoFinalSelecinado < 0){
                            anoFinalSelecinado = c.get(Calendar.YEAR);
                        }
                        if(mesFinalSelecionado < 0){
                            mesFinalSelecionado = c.get(Calendar.MONTH);
                        }
                        if(diaFinalSelecionado < 0){
                            diaFinalSelecionado = c.get(Calendar.DAY_OF_MONTH);
                        }
                        // Launch Date Picker Dialog
                        DatePickerDialog dataDialog = new DatePickerDialog(ClientePositivacaoMDActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                textDataFinal.setText(dayOfMonth+"/"+ (monthOfYear + 1)+"/"+year);
                                //c.set(year, monthOfYear, dayOfMonth);
                                //dataFormatadaFinal.setCalendar(c);
                                anoFinalSelecinado = year;
                                mesFinalSelecionado = monthOfYear + 1;
                                diaFinalSelecionado = dayOfMonth;
                            }
                        }, anoInicialSelecinado, mesInicialSelecionado, diaInicialSelecionado);
                        dataDialog.show();
                    }
                });

                Button buttonCancelar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_cancelar);
                buttonCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Fecha o dialog com seletor de data
                        dialogPeriodo.dismiss();

                        anoInicialSelecinado = -1;
                        mesInicialSelecionado = -1;
                        diaInicialSelecionado = -1;

                        anoFinalSelecinado = -1;
                        mesFinalSelecionado = -1;
                        diaInicialSelecionado = -1;

                        whereAuxiliar = "";
                        textDataFiltro.setText("Sem Datas Para Filtrar");
                        listViewClientePositivacao.setAdapter(null);
                    }
                });


                Button buttonFiltrar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_filtrar);
                buttonFiltrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        whereAuxiliar = wherePeriodoData();
                        PreencheTextDataFiltro(TIPO_PERSONALIZADO, null);
                        carregarListaTitulos.execute();
                        dialogPeriodo.dismiss();
                    }
                });

                dialogPeriodo.show();
                break;

            case R.id.menu_cliente_positivacao_compartilhar:
                if ( (listaPositivacao != null) && (listaPositivacao.size() > 0) ) {
                    GeraPdfClientePositivacaoAsyncRotinas geraPdfPositivacaoAsync = new GeraPdfClientePositivacaoAsyncRotinas(ClientePositivacaoMDActivity.this);
                    geraPdfPositivacaoAsync.setListCfaposit(listaPositivacao);

                    List<PessoaBeans> listPessoa = new ArrayList<>();
                    PessoaRotinas pessoaRotinas = new PessoaRotinas(ClientePositivacaoMDActivity.this);
                    listPessoa = pessoaRotinas.listaPessoaResumido("CFACLIFO.ID_CFACLIFO NOT IN (SELECT CFAPOSIT.ID_CFACLIFO FROM CFAPOSIT WHERE (" + whereAuxiliar + ") GROUP BY CFAPOSIT.ID_CFACLIFO)",
                                                                    PessoaRotinas.KEY_TIPO_CLIENTE,
                                                            null);
                    geraPdfPositivacaoAsync.setListPessoa(listPessoa);
                    geraPdfPositivacaoAsync.setDataInicial(dataInicial);
                    geraPdfPositivacaoAsync.setDataFinal(dataFinal);
                    try {
                    String retornoCaminho = geraPdfPositivacaoAsync.execute().get();

                    File arquivo = new File(retornoCaminho);

                    if (arquivo.exists()) {
                        Uri positivacaoPdf = FileProvider.getUriForFile(ClientePositivacaoMDActivity.this, "com.savare.fileprovider", arquivo);
                        ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this).addStream(positivacaoPdf);

                        Intent intent = ShareCompat.IntentBuilder.from(this)
                                .setStream(positivacaoPdf) // uri from FileProvider
                                .setType("text/html")
                                .getIntent()
                                .setAction(Intent.ACTION_SEND) //Change if needed
                                .setDataAndType(positivacaoPdf, "application/pdf")
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(intent);
                    }
                    } catch (Exception ex) {
                        SuperActivityToast.create(this, getResources().getString(R.string.nao_possivel_compartilhar_arquivo) + " - " + ex.getMessage(), Style.DURATION_LONG)
                                .setTextColor(Color.WHITE)
                                .setColor(Color.RED)
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                    }
                } else {
                    ((ClientePositivacaoMDActivity.this)).runOnUiThread(new Runnable() {
                        public void run() {
                            new MaterialDialog.Builder(ClientePositivacaoMDActivity.this)
                                    .title(R.string.produtos)
                                    .content(R.string.nao_achamos_nenhum_valor_para_carregar_relatorio)
                                    .positiveText(android.R.string.ok)
                                    //.negativeText(R.string.disagree)
                                    .autoDismiss(true)
                                    .show();
                        }
                    });
                }
                break;

            default:
                break;
        }
        return true;
    }

    private void recuperaCampos(){
        textDataFiltro = (TextView) findViewById(R.id.activity_cliente_positivacao_text_datas);
        listViewClientePositivacao = (ListView) findViewById(R.id.activity_cliente_positivacao_listview_clientes);
        progressBarStatus = (ProgressBar) findViewById(R.id.activity_cliente_positivacao_progressBar_status);
        toolbarInicio = (Toolbar) findViewById(R.id.activity_cliente_positivacao_toolbar_cabecalho);
        // Adiciona uma titulo para toolbar
        toolbarInicio.setTitle(this.getResources().getString(R.string.positivacao));
        toolbarInicio.setTitleTextColor(getResources().getColor(R.color.branco));
        setSupportActionBar(toolbarInicio);
    }

    public class CarregarListaTitulos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Limpa a lista
            listViewClientePositivacao.setAdapter(null);

            CfapositRotinas cfapositRotinas = new CfapositRotinas(ClientePositivacaoMDActivity.this);
            listaPositivacao = new ArrayList<>();
            // Pega os dados de positivacao no banco de dados
            listaPositivacao = cfapositRotinas.listaClientePositivacao(whereAuxiliar);
            // Checa se voltou alguma coisa do banco de dados
            if ( (listaPositivacao != null) && (listaPositivacao.size() > 0) ) {
                adapterClientePositivacao = new ItemUniversalAdapter(ClientePositivacaoMDActivity.this, ItemUniversalAdapter.POSITIVACAO);
                adapterClientePositivacao.setListaCfaposit(listaPositivacao);
            } else {

                ((ClientePositivacaoMDActivity.this)).runOnUiThread(new Runnable() {
                    public void run() {
                        //
                        //textDataFiltro.setText(R.string.nao_achamos_nenhum_valor);

                        new MaterialDialog.Builder(ClientePositivacaoMDActivity.this)
                                .title(R.string.produtos)
                                .content(R.string.nao_achamos_nenhum_valor)
                                .positiveText(android.R.string.ok)
                                //.negativeText(R.string.disagree)
                                .autoDismiss(true)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (listaPositivacao != null && listaPositivacao.size() > 0) {
                listViewClientePositivacao.setAdapter(adapterClientePositivacao);
            }
            progressBarStatus.setVisibility(View.GONE);
        }
    }

    private String wherePeriodoData(){
        String where = null;

        // Formata a data
        DateFormat dataFormatadaInicial = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dataFormatadaFinal = new SimpleDateFormat("yyyy-MM-dd");

        // Checa se existe data inicial para pesquisar
        if((anoInicialSelecinado > 0) && (mesInicialSelecionado > 0) && (diaInicialSelecionado > 0)){
            Calendar c = Calendar.getInstance();

            c.set(anoInicialSelecinado, mesInicialSelecionado - 1, diaInicialSelecionado);

            // Preenche a dataFormatada com a data salva
            dataFormatadaInicial.setCalendar(c);

            where = " (DATA_VISITA >= '" + dataFormatadaInicial.format(c.getTime()) + "')";
        }
        // Checa se existe data final para pesquisar
        if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
            Calendar c = Calendar.getInstance();

            c.set(anoFinalSelecinado, mesFinalSelecionado - 1, diaFinalSelecionado);

            dataFormatadaFinal.setCalendar(c);

            if((where != null) && (where.length() > 0)){
                where += " AND (DATA_VISITA <= '" + dataFormatadaFinal.format(c.getTime()) + "')";
            } else {
                where = " (DATA_VISITA <= '" + dataFormatadaFinal.format(c.getTime()) + "')";
            }
        }
        return where;
    }

    /**
     *
     * @param tipo - S = SEMANA | M = MES | P = PERSONALIZADO
     * @param semanaAno
     */
    private void PreencheTextDataFiltro(String tipo, String semanaAno){
        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClientePositivacaoMDActivity.this);

        if (tipo.equalsIgnoreCase(TIPO_SEMANA)) {
            StringBuffer selectDateWeek = new StringBuffer();
            selectDateWeek.append("SELECT MAX(DATE(DATA_VISITA, 'WEEKDAY 0', '-7 DAY')) WEEKSTART, \n");
            selectDateWeek.append("MAX(DATE(DATA_VISITA, 'WEEKDAY 0', '-1 DAY')) WEEKEND ");
            selectDateWeek.append("FROM CFAPOSIT WHERE (STRFTIME('%W', DATA_VISITA) = '").append(semanaAno).append("')");

            PositivacaoSql positivacaoSql = new PositivacaoSql(ClientePositivacaoMDActivity.this);
            Cursor dados = positivacaoSql.sqlSelect(selectDateWeek.toString());
            if ((dados != null) && (dados.moveToFirst())) {
                dataInicial = dados.getString(dados.getColumnIndex("WEEKSTART"));
                dataFinal = dados.getString(dados.getColumnIndex("WEEKEND"));

                textDataFiltro.setText("De: " + funcoes.formataData(dados.getString(dados.getColumnIndex("WEEKSTART"))) + " - " +
                        "Até: " + funcoes.formataData(dados.getString(dados.getColumnIndex("WEEKEND"))));
            }
        } else if (tipo.equalsIgnoreCase(TIPO_MES)){
            StringBuffer selectDateWeek = new StringBuffer();
            selectDateWeek.append("SELECT DATE(DATA_VISITA, 'START OF MONTH') MONTHSTART, \n");
            selectDateWeek.append("DATE(DATA_VISITA, 'START OF MONTH', '1 MONTH', '-1 DAY') MONTHLAST ");
            selectDateWeek.append("FROM CFAPOSIT WHERE (STRFTIME('%m', DATA_VISITA) = '").append(semanaAno).append("')");

            PositivacaoSql positivacaoSql = new PositivacaoSql(ClientePositivacaoMDActivity.this);
            Cursor dados = positivacaoSql.sqlSelect(selectDateWeek.toString());
            if ((dados != null) && (dados.moveToFirst())) {
                dataInicial = dados.getString(dados.getColumnIndex("MONTHSTART"));
                dataFinal = dados.getString(dados.getColumnIndex("MONTHLAST"));

                textDataFiltro.setText("De: " + funcoes.formataData(dados.getString(dados.getColumnIndex("MONTHSTART"))) + " - " +
                        "Até: " + funcoes.formataData(dados.getString(dados.getColumnIndex("MONTHLAST"))));
            }
        } else if (tipo.equalsIgnoreCase(TIPO_PERSONALIZADO)){
            dataInicial = anoInicialSelecinado + "/" + mesInicialSelecionado + "/" + diaInicialSelecionado;
            dataFinal = anoFinalSelecinado + "/" + mesFinalSelecionado + "/" + diaFinalSelecionado;

            textDataFiltro.setText("De: " + diaInicialSelecionado + "/" + mesInicialSelecionado + "/" + anoInicialSelecinado + " - " +
                                   "Até: " + diaFinalSelecionado + "/" + mesFinalSelecionado + "/" + anoFinalSelecinado);
        }
    }
}

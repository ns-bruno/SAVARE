package com.savare.activity.material.designer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.adapter.ListaTitulosExpandableAdapter;
import com.savare.beans.TitulosListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.ParcelaRotinas;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 29/01/2016.
 */
public class ListaTitulosMDActivity extends AppCompatActivity{

    private ProgressBar progressBarStatus;
    private Toolbar toolbarInicio;
    private TextView textTotalAReceber,
            textTotalCredito;
    private TextView textDataFinal;
    private TextView textDataInicial;
    private Dialog dialogPeriodo;
    private ExpandableListView expandagleListaTitulos;
    private List<TitulosListaBeans> listaTitulos;
    private ListaTitulosExpandableAdapter adapterListaTitulos;
    private String idPessoa,
            whereFiltroAuxiliar;
    private char tipoListagem = '0'; // 0 = Titulos em Aberto, 1 = Titulos Baixado, 2 = Titulos em Aberto Vencidos
    private char pagarReceber = '0'; // 0 = Receber, 1 = Pagar
    int anoInicialSelecinado = -1;
    int mesInicialSelecionado = -1;
    int diaInicialSelecionado = -1;
    int anoFinalSelecinado = -1;
    int mesFinalSelecionado = -1;
    int diaFinalSelecionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_titulos_md);

        recuperaCampos();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {
            // Seta o campo codigo consumo total com o que foi passado por parametro
            idPessoa = intentParametro.getString("ID_CFACLIFO");

        } else {
            idPessoa = "";
        }

        CarregarListaTitulos carregarListaTitulos = new CarregarListaTitulos();
        carregarListaTitulos.execute();

        /*ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

        listaTitulos = new ArrayList<TitulosListaBeans>();
        listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, null);

        adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosMDActivity.this, listaTitulos);
        expandagleListaTitulos.setAdapter(adapterListaTitulos);*/

    }  // fim onCreate

    @Override
    protected void onResume() {
        super.onResume();

        /*ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaTitulosMDActivity.this);

        textTotalAReceber.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '0', whereFiltroAuxiliar)));
        textTotalCredito.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '1', whereFiltroAuxiliar)));*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();

        getMenuInflater().inflate(R.menu.lista_titulos, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView;
        final MenuItem itemMenuSearch = menu.findItem(R.id.menu_lista_titulos_search_pesquisar);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
            searchView = (SearchView) itemMenuSearch.getActionView();
        }
        else{
            searchView = (SearchView) MenuItemCompat.getActionView(itemMenuSearch);
        }

        /*if((idPessoa != null) && (idPessoa.length() < 0)){
            menu.getItem(R.menu.lista_titulos).setVisible(false);
        } */

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                query = query.replace(" ", "%");

                String where = "CFACLIFO.NOME_RAZAO LIKE '%" + query + "%' OR "
                             + "CFACLIFO.NOME_FANTASIA LIKE '%" + query + "%' OR "
                             + "CFACLIFO.CPF_CNPJ LIKE '%" + query + "%' OR "
                             + "CFAENDER.BAIRRO LIKE '%" + query + "%' OR "
                             + "RPAPARCE.DT_VENCIMENTO LIKE '%" + query + "%' OR "
                             + "RPAPARCE.VL_PARCELA LIKE '%" + query + "%' OR "
                             + "CFASTATU.DESCRICAO LIKE '%" + query + "%' ";
                // Pega a where e salva
                whereFiltroAuxiliar = where;

                ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

                listaTitulos = new ArrayList<TitulosListaBeans>();
                listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, where, progressBarStatus);

                // Atualiza o adapter com uma nova lista
                adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosMDActivity.this, listaTitulos);
                expandagleListaTitulos.setAdapter(adapterListaTitulos);

                onResume();

                return false;
            } // Fim do onQueryTextSubmit

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText.length() <= 0){
                    whereFiltroAuxiliar = "";
                }
                return false;
            } // Fim do onQueryTextChange
        }); // Fim do setOnQueryTextListener

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.menu_lista_titulos_vencidos:

                listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '2', pagarReceber, whereFiltroAuxiliar, progressBarStatus);
                adapterListaTitulos.setListaPessoasParent(listaTitulos);
                expandagleListaTitulos.setAdapter(adapterListaTitulos);
                onResume();
                break;

            case R.id.menu_lista_titulos_aberto:

                listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '0', pagarReceber, whereFiltroAuxiliar, progressBarStatus);
                adapterListaTitulos.setListaPessoasParent(listaTitulos);
                expandagleListaTitulos.setAdapter(adapterListaTitulos);
                onResume();
                break;

            case R.id.menu_lista_titulos_baixados:

                listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '1', '0', whereFiltroAuxiliar, progressBarStatus);
                adapterListaTitulos.setListaPessoasParent(listaTitulos);
                expandagleListaTitulos.setAdapter(adapterListaTitulos);
                onResume();
                break;

            case R.id.menu_lista_titulos_credito:

                listaTitulos = parcelaRotinas.listaTitulos(idPessoa, '0', '1', whereFiltroAuxiliar, progressBarStatus);
                adapterListaTitulos.setListaPessoasParent(listaTitulos);
                expandagleListaTitulos.setAdapter(adapterListaTitulos);
                onResume();
                break;

            case R.id.menu_lista_titulos_filtrar_periodo:

                dialogPeriodo = new Dialog(ListaTitulosMDActivity.this);
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
                        DatePickerDialog dataDialog = new DatePickerDialog(ListaTitulosMDActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                        DatePickerDialog dataDialog = new DatePickerDialog(ListaTitulosMDActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                        carregarListaTitulosPeriodo();
                    }
                });


                Button buttonFiltrar = (Button) dialogPeriodo.findViewById(R.id.layout_dialog_periodo_data_button_filtrar);
                buttonFiltrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        carregarListaTitulosPeriodo();
                        dialogPeriodo.dismiss();
                    }
                });

                dialogPeriodo.show();
                break;
            default:
                break;
        }
        return true;
    } // Fim

    public class CarregarListaTitulos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

            listaTitulos = new ArrayList<TitulosListaBeans>();
            listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, null, progressBarStatus);

            adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosMDActivity.this, listaTitulos);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            expandagleListaTitulos.setAdapter(adapterListaTitulos);

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaTitulosMDActivity.this);

            ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

            textTotalAReceber.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '0', whereFiltroAuxiliar)));
            textTotalCredito.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '1', whereFiltroAuxiliar)));

            progressBarStatus.setVisibility(View.GONE);
        }
    }

    private void recuperaCampos(){
        progressBarStatus = (ProgressBar) findViewById(R.id.activity_lista_titulos_mdprogressBar_status);
        textTotalAReceber = (TextView) findViewById(R.id.activity_lista_titulos_md_text_total_a_receber);
        textTotalCredito = (TextView) findViewById(R.id.activity_lista_titulos_md_text_credito);
        expandagleListaTitulos = (ExpandableListView) findViewById(R.id.activity_lista_titulos_md_expandable_lista_titulos);
        toolbarInicio = (Toolbar) findViewById(R.id.activity_lista_titulos_md_toolbar_cabecalho);
        // Adiciona uma titulo para toolbar
        toolbarInicio.setTitle(this.getResources().getString(R.string.titulos));
        toolbarInicio.setTitleTextColor(getResources().getColor(R.color.branco));
        setSupportActionBar(toolbarInicio);
    } // Fim do recuperaCampos


    /**
     * Pega o periodo que foi selecionado no menu.
     *
     * @return
     */
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

            where = " (DT_VENCIMENTO >= '" + dataFormatadaInicial.format(c.getTime()) + "')";
        }

        // Checa se existe data final para pesquisar
        if((anoFinalSelecinado > 0) && (mesFinalSelecionado > 0) && (diaFinalSelecionado > 0)){
            Calendar c = Calendar.getInstance();

            c.set(anoFinalSelecinado, mesFinalSelecionado - 1, diaFinalSelecionado);

            dataFormatadaFinal.setCalendar(c);

            if((where != null) && (where.length() > 0)){
                where += " AND (DT_VENCIMENTO <= '" + dataFormatadaFinal.format(c.getTime()) + "')";
            } else {
                where = " (DT_VENCIMENTO <= '" + dataFormatadaFinal.format(c.getTime()) + "')";
            }
        }
        return where;
    }


    /**
     * Carrega a lista completa dos or�amentos/pedidos de acordo com o
     * periodo selecionado e a cidade selecionada.
     *
     */
    private void carregarListaTitulosPeriodo(){

        String wherePeriodo = wherePeriodoData();

        ParcelaRotinas parcelaRotinas = new ParcelaRotinas(ListaTitulosMDActivity.this);

        listaTitulos = new ArrayList<TitulosListaBeans>();
        listaTitulos = parcelaRotinas.listaTitulos(idPessoa, tipoListagem, pagarReceber, wherePeriodo, progressBarStatus);
        // Seta o adapter com uma nova lista
        adapterListaTitulos = new ListaTitulosExpandableAdapter(ListaTitulosMDActivity.this, listaTitulos);
        expandagleListaTitulos.setAdapter(adapterListaTitulos);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ListaTitulosMDActivity.this);
        // Calcula os campos totais
        textTotalAReceber.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '0', wherePeriodo)));
        textTotalCredito.setText(funcoes.arredondarValor(parcelaRotinas.totalReceberPagarCliente(idPessoa, tipoListagem, '1', wherePeriodo)));
    }
}

package com.savare.activity.material.designer.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.savare.R;
import com.savare.beans.OrcamentoBeans;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bruno Nogueira Silva on 01/02/2016.
 */
public class ClienteDetalhesGraficoVendasMesMDFragment extends Fragment {

    private View viewGrafico;
    private String codigoCli,
            codigoFun,
            codigoUsu,
            codigoTra,
            idCliente;
    private Button buttonAnterior, buttonProximo;
    private TextView textViewMesVendas;
    private boolean clienteNovo = false;
    private BarChart barChartVendasMes;
    private LineChart lineChartNumeroVendasMes;
    private String mesPesquisar, anoPesquisar;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        viewGrafico = inflater.inflate(R.layout.fragment_cliente_detalhes_grafico_vendas_mes_md, container, false);

        // Ativa a opcao de menus para este fragment
        //setHasOptionsMenu(true);

        recuperarCamposTela();

        Bundle intentParametro = getArguments();

        if(intentParametro != null){

            // Pega os codigo internos da pessoa
            codigoCli = intentParametro.getString("CODIGO_CLI");
            codigoFun = intentParametro.getString("CODIGO_FUN");
            codigoTra = intentParametro.getString("CODIGO_TRA");
            codigoUsu = intentParametro.getString("CODIGO_USU");
            // Pega o id do cliente
            idCliente = intentParametro.getString("ID_CFACLIFO");

            if ((intentParametro.getString("CADASTRO_NOVO") != null) && (intentParametro.getString("CADASTRO_NOVO").equalsIgnoreCase("S"))){
                clienteNovo = true;
            }
        }
        Locale brasil = new Locale("pt", "BR");
        final Calendar c = Calendar.getInstance();
        final Date data = c.getTime();
        final SimpleDateFormat mesAtual = new SimpleDateFormat("MMMM", brasil);
        final SimpleDateFormat anoAtual = new SimpleDateFormat("yyyy");

        textViewMesVendas.setText(mesAtual.format(data).toUpperCase() + "/" + anoAtual.format(data));

        mesPesquisar = new SimpleDateFormat("MM").format(data);
        anoPesquisar = new SimpleDateFormat("yyyy").format(data);

        carregarGraficoValorVendasMes(mesPesquisar, anoPesquisar);

        buttonAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mes = Integer.parseInt(mesPesquisar);
                int ano = Integer.parseInt(anoPesquisar);

                if (mes == 1){
                    ano = ano - 1;
                    mes = 12;

                } else {
                    mes = mes - 1;
                }

                if (mes >=1 && mes <=9) {
                    mesPesquisar = "0" + String.valueOf(mes);
                }else {
                    mesPesquisar = String.valueOf(mes);
                }
                anoPesquisar = String.valueOf(ano);

                c.set(ano, mes-1, 1);

                Date data = c.getTime();

                textViewMesVendas.setText(mesAtual.format(data).toUpperCase() + "/" + anoAtual.format(data));

                // Carrega os graficos
                carregarGraficoValorVendasMes(mesPesquisar, anoPesquisar);
            }
        });

        buttonProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mes = Integer.parseInt(mesPesquisar);
                int ano = Integer.parseInt(anoPesquisar);

                if (mes == 12){
                    ano = ano + 1;
                    mes = 1;

                } else {
                    mes = mes + 1;
                }

                if (mes >=1 && mes <=9) {
                    mesPesquisar = "0" + String.valueOf(mes);
                }else {
                    mesPesquisar = String.valueOf(mes);
                }
                anoPesquisar = String.valueOf(ano);

                c.set(ano, mes-1, 1);

                Date data = c.getTime();

                textViewMesVendas.setText(mesAtual.format(data).toUpperCase() + "/" + anoAtual.format(data));

                // Carrega os graficos
                carregarGraficoValorVendasMes(mesPesquisar, anoPesquisar);
            }
        });

        return viewGrafico;
    } // Fim onCreateView


    private void recuperarCamposTela(){
        barChartVendasMes = (BarChart) viewGrafico.findViewById(R.id.fragment_cliente_detalhes_grafico_vendas_mes_md_barChart_vendas_mes);
        lineChartNumeroVendasMes = (LineChart) viewGrafico.findViewById(R.id.fragment_cliente_detalhes_grafico_vendas_mes_md_lineChart_numero_vendas_mes);
        buttonAnterior = (Button) viewGrafico.findViewById(R.id.fragment_cliente_detalhes_grafico_vendas_mes_md_button_anterior);
        buttonProximo = (Button) viewGrafico.findViewById(R.id.fragment_cliente_detalhes_grafico_vendas_mes_md_button_proximo);
        textViewMesVendas = (TextView) viewGrafico.findViewById(R.id.fragment_cliente_detalhes_grafico_vendas_mes_md_textView_mes_vendas);
    }

    private void carregarGraficoValorVendasMes(String mes, String ano){

        barChartVendasMes.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {

            }

            @Override
            public void onNothingSelected() {

            }
        });
        barChartVendasMes.setDrawBarShadow(false);
        barChartVendasMes.setDrawValueAboveBar(true);
        // Muda a cor do fundo
        barChartVendasMes.setBackgroundColor(getResources().getColor(R.color.branco));
        // Definir um texto de descrição que aparece no canto inferior direito do gráfico.
        barChartVendasMes.setDescription("");
        // Define o texto que deve aparecer se o gráfico se encontra vazio.
        barChartVendasMes.setNoDataTextDescription(getResources().getString(R.string.nao_achamos_nenhum_valor));

        barChartVendasMes.setPinchZoom(false);

        barChartVendasMes.setDrawGridBackground(false);

        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getContext());

        List<OrcamentoBeans> listaOrcamento;
        // Pega a lista de orcamento
        listaOrcamento = orcamentoRotinas.listaTotalVendaDiarioCliente(idCliente, null, mes, ano);

        if ((listaOrcamento != null) && (listaOrcamento.size() > 0)) {
            barChartVendasMes.setVisibility(View.VISIBLE);
            // Cria uma vareavel para pegar os dias que teve vendas do mes de vendas
            ArrayList<String> xValsDiasMesVenda = new ArrayList<String>();

            // Cria uma vareavel para pegar os valores de cada dia vendido
            ArrayList<BarEntry> yValsValorVendaDias = new ArrayList<BarEntry>();

            for (int i = 0; i < listaOrcamento.size(); i++) {
                // Adiciona o dia
                xValsDiasMesVenda.add(listaOrcamento.get(i).getDataCadastro());

                if (listaOrcamento.get(i).getTotalOrcamentoFaturado() > 0) {
                    yValsValorVendaDias.add(new BarEntry((float) listaOrcamento.get(i).getTotalOrcamentoFaturado(), i));
                } else {
                    yValsValorVendaDias.add(new BarEntry((float) listaOrcamento.get(i).getTotalOrcamento(), i));
                }
            }

            BarDataSet dados = new BarDataSet(yValsValorVendaDias, "Dias de Vendas No Mês " + textViewMesVendas.getText().toString());
            dados.setBarSpacePercent(35f);

            BarData barData = new BarData(xValsDiasMesVenda, dados);
            barChartVendasMes.setData(null);
            barChartVendasMes.setData(barData);
        } else {
            barChartVendasMes.setVisibility(View.INVISIBLE);
        }
    }
}

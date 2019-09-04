package com.savare.activity.material.designer.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.savare.R;
import com.savare.activity.material.designer.ListaTitulosMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.PessoaBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.PortadorBancoBeans;
import com.savare.beans.RamoAtividadeBeans;
import com.savare.beans.TipoClienteBeans;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.beans.TotalMensal;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ParcelaRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 01/02/2016.
 */
public class ClienteDetalhesDadosMDFragment extends Fragment {
    
    private View viewDados;
    private TextView textCodigoPessoa,
            textStatus,
            textRazaoSocial,
            textFantasia,
            textCnpjCpf,
            textInscricaoEstadual,
            textEstado,
            textCidade,
            textBairro,
            textEndereco,
            textNumero,
            textComplemento;
    private Spinner spinnerRamoAtividade,
            spinnerTipoCliente,
            spinnerTipoDocumento,
            spinnerPortadorBanco,
            spinnerPlanoPagamento;
    private EditText editCreditoAcumulado,
            editPontosAcumulados,
            editCapitalSocial,
            editLimiteCompra,
            editTotalVencido,
            editTotalPago,
            editTotalAPagar,
            editDescontoAtacadoVista,
            editDescontoAtacadoPrazo,
            editDescontoVarejoVista,
            editDescontoVarejoPrazo,
            editUltimaVisita;
    private LineChart graficoVendasPedidoMes;
    private ItemUniversalAdapter adapterRamoAtividade,
            adapterTipoCliente,
            adapterTipoDocumento,
            adapterPortadorBanco,
            adapterPlanoPagamento;
    private String codigoCli,
            codigoFun,
            codigoUsu,
            codigoTra,
            idCliente;
    private boolean clienteNovo = false;
    private boolean abertoTitulosPriveiraVez = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewDados = inflater.inflate(R.layout.fragment_cliente_detalhes_dados_md, container, false);

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

            // Checa se eh um cadastro novo feito na aplicacao
            if ((Integer.parseInt(intentParametro.getString("ID_CFACLIFO")) < 0) || (clienteNovo)){
                // Seta o campo codigo da pessoa com o que foi passado por parametro
                textCodigoPessoa.setText("*" + intentParametro.getString("CODIGO_CLI"));
                textCodigoPessoa.setTextColor(getResources().getColor(R.color.vermelho_escuro));
            }else {
                // Seta o campo codigo consumo total com o que foi passado por parametro
                textCodigoPessoa.setText(intentParametro.getString("CODIGO_CLI"));
            }
            carregarDadosPessoa();

        } else {
            textCodigoPessoa.setText("");
        }

        inativarCampos();

        return viewDados;
    } // Fim onCreate

    @Override
    public void onResume() {
        super.onResume();

        if(textCodigoPessoa.getText().length() > 0){
            // Carrega o grafico que mosta os totais vendidos para este cliente por mes
            carregarGraficoVendasPedidoMes();

            ParcelaRotinas parcelaRotinas = new ParcelaRotinas(getContext());

            if ((abertoTitulosPriveiraVez == false) && (parcelaRotinas.listaTitulos(idCliente, ParcelaRotinas.TITULOS_EM_ABERTO_VENCIDOS, ParcelaRotinas.RECEBER, null, null).size() > 0)) {

                abertoTitulosPriveiraVez = true;
                // Cria uma intent para abrir uma nova activity
                Intent intentTitulos = new Intent(getContext(), ListaTitulosMDActivity.class);
                intentTitulos.putExtra("ID_CFACLIFO", idCliente);
                startActivity(intentTitulos);
            }
        }
    }


    private void recuperarCamposTela(){
        textCodigoPessoa = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_codigo_pessoa);
        textStatus = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_status);
        textRazaoSocial = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_razao);
        textFantasia = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_fantasia);
        textCnpjCpf = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_cnpj_cpf);
        textInscricaoEstadual = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_inscricao_estadual);
        textEstado = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_estado);
        textCidade = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_cidade);
        textBairro = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_bairro);
        textEndereco = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_endereco);
        textNumero = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_numero);
        textComplemento = (TextView) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_text_complemento);
        spinnerRamoAtividade = (Spinner) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_spinner_ramo_atividade);
        spinnerTipoCliente = (Spinner) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_spinner_tipo_cliente);
        spinnerTipoDocumento = (Spinner) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_spinner_tipo_documento);
        spinnerPortadorBanco = (Spinner) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_spinner_portador_banco);
        spinnerPlanoPagamento = (Spinner) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_spinner_plano_pagamento);
        editCreditoAcumulado = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_credito_acumulado);
        editPontosAcumulados = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_pontos_acumulado);
        editCapitalSocial = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_capital_social);
        editLimiteCompra = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_limite_compras);
        editUltimaVisita = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_ultima_visita);
        editTotalVencido = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_total_vencido);
        editTotalPago = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_total_pago);
        editTotalAPagar = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_total_a_pagar);
        editDescontoAtacadoVista = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_desconto_atacado_vista);
        editDescontoAtacadoPrazo = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_desconto_atacado_prazo);
        editDescontoVarejoVista = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_desconto_varejo_vista);
        editDescontoVarejoPrazo = (EditText) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_editText_desconto_varejo_prazo);
        graficoVendasPedidoMes = (LineChart) viewDados.findViewById(R.id.fragment_cliente_detalhes_dados_md_lineChart_grafico_vendas_pedido_mes);

    }

    private void carregarDadosPessoa(){
        // Instancia a classe de rotinas
        PessoaRotinas pessoaRotinas = new PessoaRotinas(getContext());
        // Pega os dados da pessoa de acordo com o ID
        PessoaBeans pessoa = pessoaRotinas.pessoaCompleta(idCliente, "cliente");
        //PessoaBeans pessoa = pessoaRotinas.listaPessoaResumido("CFACLIFO.ID_CFACLIFO = " + textCodigoPessoa.getText().toString(), "cliente").get(0);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());

        textRazaoSocial.setText(pessoa.getNomeRazao());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(pessoa.getNomeRazao());
        textFantasia.setText(pessoa.getNomeFantasia());
        textCnpjCpf.setText(pessoa.getCpfCnpj());
        textInscricaoEstadual.setText(pessoa.getIeRg());
        textStatus.setText(pessoa.getStatusPessoa().getDescricao());
        textEstado.setText(pessoa.getEstadoPessoa().getSiglaEstado());
        textCidade.setText(pessoa.getCidadePessoa().getDescricao());
        textBairro.setText(pessoa.getEnderecoPessoa().getBairro());
        textEndereco.setText(pessoa.getEnderecoPessoa().getLogradouro());
        textNumero.setText(pessoa.getEnderecoPessoa().getNumero());
        textComplemento.setText(pessoa.getEnderecoPessoa().getComplemento());

        if((pessoa.getDataUltimaVisita() != null) && (pessoa.getDataUltimaVisita().length() < 1)){
            editUltimaVisita.setText("Não Tem Visita");
        } else {
            editUltimaVisita.setText("" + pessoa.getDataUltimaVisita());
        }

        editLimiteCompra.setText(funcoes.arredondarValor(pessoa.getLimiteCompra()));
        editDescontoAtacadoVista.setText(funcoes.arredondarValor(pessoa.getDescontoAtacadoVista()));
        editDescontoAtacadoPrazo.setText(funcoes.arredondarValor(pessoa.getDescontoAtacadoPrazo()));
        editDescontoVarejoVista.setText(funcoes.arredondarValor(pessoa.getDescontoVarejoVista()));
        editDescontoVarejoPrazo.setText(funcoes.arredondarValor(pessoa.getDescontoVarejoPrazo()));
        editCreditoAcumulado.setText(funcoes.arredondarValor(pessoa.getCreditoAcumulado()));
        editTotalAPagar.setText(funcoes.arredondarValor(pessoa.getTotalAPagar()));
        editCapitalSocial.setText(funcoes.arredondarValor(pessoa.getCapitalSocial()));
        editTotalPago.setText(funcoes.arredondarValor(pessoa.getTotalPago()));
        editTotalVencido.setText(funcoes.arredondarValor(pessoa.getTotalVencido()));

        if(pessoa.getTotalVencido() > 0){
            editTotalVencido.setTextColor(Color.RED);
            // Mensagem para avisar ao usuario que existe titulo vencido
            SuperActivityToast.create(getActivity(), getResources().getString(R.string.existe_titulos_vencidos), Style.DURATION_LONG)
                    .setTextColor(Color.WHITE)
                    .setColor(Color.RED)
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }

        // Checa se retornou algum valor
        if( (pessoa.getRamoAtividade() == null) || (pessoa.getRamoAtividade().getIdRamoAtividade() < 1) ){
            spinnerRamoAtividade.setVisibility(View.GONE);

        } else {
            List<RamoAtividadeBeans> listaRamoAtividade = new ArrayList<RamoAtividadeBeans>();
            listaRamoAtividade.add(pessoa.getRamoAtividade());
            // Intancia a classe do adapter
            adapterRamoAtividade = new ItemUniversalAdapter(getContext(), 7);
            // Preenche o adapter com uma lista de atividade
            adapterRamoAtividade.setListaRamoAtividade(listaRamoAtividade);
            spinnerRamoAtividade.setAdapter(adapterRamoAtividade);
        }

        // Checa se retornou algum valor
        if( (pessoa.getTipoClientePessoa() == null) || (pessoa.getTipoClientePessoa().getIdTipoCliente() < 1) ){
            spinnerTipoCliente.setVisibility(View.GONE);
        } else {
            List<TipoClienteBeans> listaTipoCliente = new ArrayList<TipoClienteBeans>();
            listaTipoCliente.add(pessoa.getTipoClientePessoa());
            // Intancia a classe do adapter
            adapterTipoCliente = new ItemUniversalAdapter(getContext(), 8);
            // Preenche o adapter com uma lista de tipos de cliente
            adapterTipoCliente.setListaTipoCliente(listaTipoCliente);
            spinnerTipoCliente.setAdapter(adapterTipoCliente);
        }

        // Checa se retornou algum valor
        if( (pessoa.getTipoDocumentoPessoa() == null) || (pessoa.getTipoDocumentoPessoa().getIdTipoDocumento() < 1) ){
            spinnerRamoAtividade.setVisibility(View.GONE);

        } else {
            List<TipoDocumentoBeans> listaTipoDocumentoBeans = new ArrayList<TipoDocumentoBeans>();
            listaTipoDocumentoBeans.add(pessoa.getTipoDocumentoPessoa());
            // Intancia a classe do adapter
            adapterTipoDocumento = new ItemUniversalAdapter(getContext(), 3);
            // Preenche o adapter com uma lista de documentos
            adapterTipoDocumento.setListaTipoDocumento(listaTipoDocumentoBeans);
            spinnerTipoDocumento.setAdapter(adapterTipoDocumento);
        }

        // Checa se retornou algum valor
        if( (pessoa.getPortadorBancoPessoa() == null) || (pessoa.getPortadorBancoPessoa().getIdPortadorBanco() < 1) ){
            spinnerPortadorBanco.setVisibility(View.GONE);

        } else {
            List<PortadorBancoBeans> listaPortadorBanco = new ArrayList<PortadorBancoBeans>();
            listaPortadorBanco.add(pessoa.getPortadorBancoPessoa());
            // Instancia a classe do adapter
            adapterPortadorBanco = new ItemUniversalAdapter(getContext(), 9);
            // Preenche o adapter com uma lista portadores
            adapterPortadorBanco.setListaPortadorBanco(listaPortadorBanco);
            spinnerPortadorBanco.setAdapter(adapterPortadorBanco);
        }

        // Checa se retornou algum valor
        if( (pessoa.getPlanoPagamentoPessoa() == null) || (pessoa.getPlanoPagamentoPessoa().getIdPlanoPagamento() < 1) ){
            spinnerPlanoPagamento.setVisibility(View.GONE);

        }else {
            List<PlanoPagamentoBeans> listaPlanoPagamento = new ArrayList<PlanoPagamentoBeans>();
            listaPlanoPagamento.add(pessoa.getPlanoPagamentoPessoa());
            // Intancia a classe do adapter
            adapterPlanoPagamento = new ItemUniversalAdapter(getContext(), 4);
            // Preenche o adapter com uma lista de planos de pagamento
            adapterPlanoPagamento.setListaPlanoPagamento(listaPlanoPagamento);
            spinnerPlanoPagamento.setAdapter(adapterPlanoPagamento);
        }

        // Verifica se o campo bloqueia eh NAO(0) e  o campo PARCELA EM ABERTO eh VENDE(1)
        if((pessoa.getStatusPessoa().getBloqueia() == "0" ) && (pessoa.getStatusPessoa().getParcelaEmAberto() == "1")){
            // Muda a cor da View
            textStatus.setTextColor(getResources().getColor(R.color.verde_escuro));

            // Verifica se o campo bloqueia eh SIM(1) e  o campo PARCELA EM ABERTO eh diferente de VENDE(1)
        } else if((pessoa.getStatusPessoa().getBloqueia() == "1") && (pessoa.getStatusPessoa().getParcelaEmAberto() != "1")){
            // Muda a cor da View para vermelho
            textStatus.setTextColor(getResources().getColor(R.color.vermelho_escuro));

        } else {
            // Muda a cor da View
            textStatus.setTextColor(getResources().getColor(R.color.amarelo));
        }
    } // Fim da funcao carregarDadosPessoa


    private void inativarCampos(){
        spinnerRamoAtividade.setEnabled(false);
        spinnerTipoCliente.setEnabled(false);
        spinnerTipoDocumento.setEnabled(false);
        spinnerPortadorBanco.setEnabled(false);
        spinnerPlanoPagamento.setEnabled(false);
        editCreditoAcumulado.setEnabled(false);
        editPontosAcumulados.setEnabled(false);
        editCapitalSocial.setEnabled(false);
        editLimiteCompra.setEnabled(false);
        editUltimaVisita.setEnabled(false);
        editTotalVencido.setEnabled(false);
        editTotalPago.setEnabled(false);
        editTotalAPagar.setEnabled(false);
        editDescontoAtacadoVista.setEnabled(false);
        editDescontoAtacadoPrazo.setEnabled(false);
        editDescontoVarejoVista.setEnabled(false);
        editDescontoVarejoPrazo.setEnabled(false);
    }

    private void carregarGraficoVendasPedidoMes(){

        OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getContext());

        // Pega uma lista dos totais de venda por mes
        List<TotalMensal> listaTotalVendasCliente = orcamentoRotinas.listaTotalVendaMensalCliente(new String[]{OrcamentoRotinas.PEDIDO_ENVIADO,
                                                                                                    OrcamentoRotinas.PEDIDO_NAO_ENVIADO,
                                                                                                    OrcamentoRotinas.PEDIDO_FATURADO,
                                                                                                    OrcamentoRotinas.PEDIDO_RETORNADO_BLOQUEADO,
                                                                                                    OrcamentoRotinas.PEDIDO_RETORNADO_LIBERADO},

                "AEAORCAM.ID_CFACLIFO = " + idCliente,

                OrcamentoRotinas.ORDEM_CRESCENTE);
        if(listaTotalVendasCliente.size() > 0){

            // Cria a variavel para pegar os dados (total) mensais
            ArrayList<Entry> vendas = new ArrayList<Entry>();
            ArrayList<String> mes = new ArrayList<String>();

            // Adiciona os totais na variavel padrao de dados do grafico
            for (int i = 0; i < listaTotalVendasCliente.size(); i++) {
                vendas.add(new Entry((float) listaTotalVendasCliente.get(i).getTotal(), i));
                mes.add((listaTotalVendasCliente.get(i).getMesAno() != null && listaTotalVendasCliente.get(i).getMesAno().length() > 0) ?
                        listaTotalVendasCliente.get(i).getMesAno() : new String("Sem Mês e Ano"));
            }
            // Cria a linha do grafico
            LineDataSet dadosLinhaGraficoTotalVendaMensal = new LineDataSet(vendas, "Vendas Mensal do cliente " + textRazaoSocial.getText().toString());
            dadosLinhaGraficoTotalVendaMensal.setLineWidth(2.5f);
            dadosLinhaGraficoTotalVendaMensal.setCircleSize(4.5f);
            dadosLinhaGraficoTotalVendaMensal.setHighLightColor(Color.rgb(244, 117, 117));
            dadosLinhaGraficoTotalVendaMensal.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
            dadosLinhaGraficoTotalVendaMensal.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0]);
            dadosLinhaGraficoTotalVendaMensal.setDrawValues(true);

            // Crio a lista e insiro os dados
            LineData linhaDados = new LineData(mes, dadosLinhaGraficoTotalVendaMensal);

            graficoVendasPedidoMes.setData(linhaDados);
            //graficoVendasPedidoMes.animateXY(2000, 2000);
            graficoVendasPedidoMes.invalidate();

            graficoVendasPedidoMes.setOnChartGestureListener(new OnChartGestureListener() {
                @Override
                public void onChartLongPressed(MotionEvent motionEvent) {

                }

                @Override
                public void onChartDoubleTapped(MotionEvent motionEvent) {

                }

                @Override
                public void onChartSingleTapped(MotionEvent motionEvent) {

                }

                @Override
                public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

                }

                @Override
                public void onChartScale(MotionEvent motionEvent, float v, float v1) {

                }
            });
            graficoVendasPedidoMes.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry entry, int i, Highlight highlight) {

                }

                @Override
                public void onNothingSelected() {

                }
            });

            // Muda a cor do fundo do grafico
            graficoVendasPedidoMes.setBackgroundColor(getResources().getColor(R.color.branco));
            // Definir um texto de descrição que aparece no canto inferior direito do gráfico.
            graficoVendasPedidoMes.setDescription(getResources().getString(R.string.historico_valores_vendas_mensais));
            // Define o texto que deve aparecer se o gráfico se encontra vazio.
            graficoVendasPedidoMes.setNoDataTextDescription("Não foi realizado nenhum pedido.");

            //graficoVendasPedidoMes.setHighlightEnabled(true);
            //graficoVendasPedidoMes.setTouchEnabled(true);
            //graficoVendasPedidoMes.setHighlightIndicatorEnabled(true);
        }

    } // fim carregarGrafico
}

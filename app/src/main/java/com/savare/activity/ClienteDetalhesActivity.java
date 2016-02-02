package com.savare.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.ChartInterface;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.savare.R;
import com.savare.activity.fragment.OrcamentoTabulacaoFragment;
import com.savare.activity.material.designer.ListaTitulosMDActivity;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.PortadorBancoBeans;
import com.savare.beans.RamoAtividadeBeans;
import com.savare.beans.TipoClienteBeans;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.beans.TitulosListaBeans;
import com.savare.beans.TotalMensal;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.LocalizacaoFuncoes;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.async.EnviarCadastroClienteFtpAsyncRotinas;

public class ClienteDetalhesActivity extends Activity implements OnChartGestureListener, OnChartValueSelectedListener {
	
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
	FuncoesPersonalizadas funcoes;
	private String codigoCli,
				   codigoFun,
				   codigoUsu,
				   codigoTra,
				   idCliente;
	private boolean clienteNovo = false;
	
	private ItemUniversalAdapter adapterRamoAtividade,
								 adapterTipoCliente,
								 adapterTipoDocumento,
								 adapterPortadorBanco,
								 adapterPlanoPagamento;
	private LineChart graficoVendasPedidoMes;
	private boolean abertoTitulosPriveiraVez = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cliente_detalhes);
		
		// Ativa a action bar com o simbolo de voltar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		recuperarCamposTela();
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle intentParametro = getIntent().getExtras();
		// Checa se foi passado algum parametro
		if (intentParametro != null) {
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
		
		if(textCodigoPessoa.getText().length() > 0){
			inativarCampos();
		}
	} // Fim onCreate
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(textCodigoPessoa.getText().length() > 0){
			// Carrega o grafico que mosta os totais vendidos para este cliente por mes
			carregarGraficoVendasPedidoMes();

			if (abertoTitulosPriveiraVez == false) {

				abertoTitulosPriveiraVez = true;
				// Cria uma intent para abrir uma nova activity
				Intent intentTitulos = new Intent(ClienteDetalhesActivity.this, ListaTitulosMDActivity.class);
				intentTitulos.putExtra("ID_CFACLIFO", textCodigoPessoa.getText().toString());
				startActivity(intentTitulos);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.cliente_detalhes, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			Intent intent = new Intent(ClienteDetalhesActivity.this, ClienteListaActivity.class);
			// Tira esta activity da pilha para nÃ£o voltar para ela
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
			
		case R.id.menu_cliente_detalhes_ocamento:
			
			//final LocalizacaoFuncoes localizacao = new LocalizacaoFuncoes(ClienteDetalhesActivity.this);
			
			// Instancia a classe de funcoes sql para pessoa
			PessoaRotinas pessoaRotinas = new PessoaRotinas(ClienteDetalhesActivity.this);
			// Pega os dados de uma pessoa especifica
			final PessoaBeans pessoa = pessoaRotinas.listaPessoaResumido("CODIGO_CLI = " + codigoCli, PessoaRotinas.KEY_TIPO_CLIENTE).get(0);
			
			// Cria um dialog para selecionar atacado ou varejo
			AlertDialog.Builder mensagemAtacadoVarejo = new AlertDialog.Builder(ClienteDetalhesActivity.this);
			// Atributo(variavel) para escolher o tipo da venda
			final String[] opcao = {"Atacado", "Varejo"};
			// Preenche o dialogo com o titulo e as opcoes
			mensagemAtacadoVarejo.setTitle("Atacado ou Varejo").setItems(opcao, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try{
					// Instancia a classe de funcoes
					funcoes = new FuncoesPersonalizadas(ClienteDetalhesActivity.this);
					
					// Preenche o ContentValues com os dados da pessoa
					ContentValues dadosCliente = new ContentValues();
					dadosCliente.put("ID_CFACLIFO", pessoa.getIdPessoa());
					dadosCliente.put("ID_CFAESTAD", pessoa.getEstadoPessoa().getCodigoEstado());
					dadosCliente.put("ID_CFACIDAD", pessoa.getCidadePessoa().getIdCidade());
					dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
					dadosCliente.put("GUID", UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16));
					dadosCliente.put("ATAC_VAREJO", which);
					dadosCliente.put("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
					dadosCliente.put("NOME_CLIENTE", pessoa.getNomeRazao());
					dadosCliente.put("IE_RG_CLIENTE", pessoa.getIeRg());
					dadosCliente.put("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
					dadosCliente.put("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
					dadosCliente.put("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
					dadosCliente.put("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
					/*dadosCliente.put("LATITUDE", localizacao.getLatitude());
					dadosCliente.put("LONGITUDE", localizacao.getLongitude());
					dadosCliente.put("ALTITUDE", localizacao.getAltitude());
					dadosCliente.put("HORARIO_LOCALIZACAO", localizacao.getHorarioLocalizacao());
					dadosCliente.put("TIPO_LOCALIZACAO", localizacao.getTipoLocalizacao());
					dadosCliente.put("PRECISAO", localizacao.getPrecisao());*/
					
					OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ClienteDetalhesActivity.this);
					
					// Cria um novo orcamento no banco de dados
					long numeroOracmento = orcamentoRotinas.insertOrcamento(dadosCliente);
					
					// Verifica se retornou algum numero
					if(numeroOracmento > 0){
						
						Bundle bundle = new Bundle();
						bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_ORCAMENTO, String.valueOf(numeroOracmento));
						bundle.putString(OrcamentoTabulacaoFragment.KEY_NOME_RAZAO, pessoa.getNomeRazao());
						bundle.putString(OrcamentoTabulacaoFragment.KEY_ID_PESSOA, String.valueOf(pessoa.getIdPessoa()));
						bundle.putString(OrcamentoTabulacaoFragment.KEY_ATACADO_VAREJO, String.valueOf(which));
						bundle.putString("AV", "0");
						
						Intent i = new Intent(ClienteDetalhesActivity.this, OrcamentoTabulacaoFragment.class);
						i.putExtras(bundle);
						
						// Abre outra tela
						startActivity(i);
						
					} else {
						// Dados da mensagem
						ContentValues mensagem = new ContentValues();
						mensagem.put("comando", 2);
						mensagem.put("tela", "CleinteDetalhesActivity");
						mensagem.put("mensagem", "Não foi possível criar orçamento");
						
						FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteDetalhesActivity.this);
						funcoes.menssagem(mensagem);
					}
				}catch(Exception e){
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 1);
					mensagem.put("tela", "CleinteDetalhesActivity");
					mensagem.put("mensagem", "Não conseguimos pegar todos os dados da pessoa.");
					
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteDetalhesActivity.this);
					funcoes.menssagem(mensagem);
				}
				
			}});
			// Faz a mensagem (dialog) aparecer
			mensagemAtacadoVarejo.show();
			break;
			
		case R.id.menu_cliente_detalhes_titulos_cliente:
			// Cria uma intent para abrir uma nova activity
			Intent intentTitulos = new Intent(ClienteDetalhesActivity.this, ListaTitulosMDActivity.class);
			intentTitulos.putExtra("ID_CFACLIFO", textCodigoPessoa.getText().toString());
			
			startActivity(intentTitulos);
			break;

		case R.id.menu_cliente_detalhes_enviar_cadastro:

			String where = " (CFACLIFO.ID_CFACLIFO == " + idCliente + ")";

			List<PessoaBeans> listaPessoasCadastro = new ArrayList<PessoaBeans>();

			PessoaRotinas pessoaRotinasCad = new PessoaRotinas(ClienteDetalhesActivity.this);

			// Pega a lista de pessoas a serem enviadas os dados
			listaPessoasCadastro = pessoaRotinasCad.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);
			// Checa se retornou alguma lista
			if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0) {
				EnviarCadastroClienteFtpAsyncRotinas enviarCadastro = new EnviarCadastroClienteFtpAsyncRotinas(ClienteDetalhesActivity.this, EnviarCadastroClienteFtpAsyncRotinas.TELA_CLIENTE_DETALHES);
				// Executa o envio do cadastro em segundo plano
				enviarCadastro.execute(listaPessoasCadastro);
			}
			break;

		default:
			break;
		}
			return true;
	} // Fim do onOptionsItemSelected


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (Integer.parseInt(idCliente) > 0) {
			// Desabilita o menu de enviar dados do cliente
			menu.getItem(2).setVisible(false);
		}
		return true;
	}

	private void recuperarCamposTela(){
		textCodigoPessoa = (TextView) findViewById(R.id.activity_cliente_detalhes_text_codigo_pessoa);
		textStatus = (TextView) findViewById(R.id.activity_cliente_detalhes_text_status);
		textRazaoSocial = (TextView) findViewById(R.id.activity_cliente_detalhes_text_razao);
		textFantasia = (TextView) findViewById(R.id.activity_cliente_detalhes_text_fantasia);
		textCnpjCpf = (TextView) findViewById(R.id.activity_cliente_detalhes_text_cnpj_cpf);
		textInscricaoEstadual = (TextView) findViewById(R.id.activity_cliente_detalhes_text_inscricao_estadual);
		textEstado = (TextView) findViewById(R.id.activity_cliente_detalhes_text_estado);
		textCidade = (TextView) findViewById(R.id.activity_cliente_detalhes_text_cidade);
		textBairro = (TextView) findViewById(R.id.activity_cliente_detalhes_text_bairro);
		textEndereco = (TextView) findViewById(R.id.activity_cliente_detalhes_text_endereco);
		textNumero = (TextView) findViewById(R.id.activity_cliente_detalhes_text_numero);
		textComplemento = (TextView) findViewById(R.id.activity_cliente_detalhes_text_complemento);
		spinnerRamoAtividade = (Spinner) findViewById(R.id.activity_cliente_detalhes_spinner_ramo_atividade);
		spinnerTipoCliente = (Spinner) findViewById(R.id.activity_cliente_detalhes_spinner_tipo_cliente);
		spinnerTipoDocumento = (Spinner) findViewById(R.id.activity_cliente_detalhes_spinner_tipo_documento);
		spinnerPortadorBanco = (Spinner) findViewById(R.id.activity_cliente_detalhes_spinner_portador_banco);
		spinnerPlanoPagamento = (Spinner) findViewById(R.id.activity_cliente_detalhes_spinner_plano_pagamento);
		editCreditoAcumulado = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_credito_acumulado);
		editPontosAcumulados = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_pontos_acumulado);
		editCapitalSocial = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_capital_social);
		editLimiteCompra = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_limite_compras);
		editUltimaVisita = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_ultima_visita);
		editTotalVencido = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_total_vencido);
		editTotalPago = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_total_pago);
		editTotalAPagar = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_total_a_pagar);
		editDescontoAtacadoVista = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_desconto_atacado_vista);
		editDescontoAtacadoPrazo = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_desconto_atacado_prazo);
		editDescontoVarejoVista = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_desconto_varejo_vista);
		editDescontoVarejoPrazo = (EditText) findViewById(R.id.activity_cliente_detalhes_editText_desconto_varejo_prazo);
		graficoVendasPedidoMes = (LineChart) findViewById(R.id.activity_cliente_detalhes_lineChart_grafico_vendas_pedido_mes);
		
	}
	
	private void carregarDadosPessoa(){
		// Instancia a classe de rotinas
		PessoaRotinas pessoaRotinas = new PessoaRotinas(ClienteDetalhesActivity.this);
		// Pega os dados da pessoa de acordo com o ID
		PessoaBeans pessoa = pessoaRotinas.pessoaCompleta(idCliente, "cliente");
		//PessoaBeans pessoa = pessoaRotinas.listaPessoaResumido("CFACLIFO.ID_CFACLIFO = " + textCodigoPessoa.getText().toString(), "cliente").get(0);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteDetalhesActivity.this);
		
		textRazaoSocial.setText(pessoa.getNomeRazao());
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
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 2);
			mensagem.put("tela", "CleinteDetalhesActivity");
			mensagem.put("mensagem", "Existe titulos vencidos.");
			
			funcoes.menssagem(mensagem);
		}
		
		// Checa se retornou algum valor
		if( (pessoa.getRamoAtividade() == null) || (pessoa.getRamoAtividade().getIdRamoAtividade() < 1) ){
			spinnerRamoAtividade.setVisibility(View.GONE);
		
		} else {
			List<RamoAtividadeBeans> listaRamoAtividade = new ArrayList<RamoAtividadeBeans>();
			listaRamoAtividade.add(pessoa.getRamoAtividade());
			// Intancia a classe do adapter
			adapterRamoAtividade = new ItemUniversalAdapter(ClienteDetalhesActivity.this, 7);
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
			adapterTipoCliente = new ItemUniversalAdapter(ClienteDetalhesActivity.this, 8);
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
			adapterTipoDocumento = new ItemUniversalAdapter(ClienteDetalhesActivity.this, 3);
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
			adapterPortadorBanco = new ItemUniversalAdapter(ClienteDetalhesActivity.this, 9);
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
			adapterPlanoPagamento = new ItemUniversalAdapter(ClienteDetalhesActivity.this, 4);
			// Preenche o adapter com uma lista de planos de pagamento
			adapterPlanoPagamento.setListaPlanoPagamento(listaPlanoPagamento);
			spinnerPlanoPagamento.setAdapter(adapterPlanoPagamento);
		}		
		
		// Verifica se o campo bloqueia eh NAO(0) e  o campo PARCELA EM ABERTO eh VENDE(1)
		if((pessoa.getStatusPessoa().getBloqueia() == '0' ) && (pessoa.getStatusPessoa().getParcelaEmAberto() == '1')){
			// Muda a cor da View
			textStatus.setTextColor(getResources().getColor(R.color.verde_escuro));
			
		// Verifica se o campo bloqueia eh SIM(1) e  o campo PARCELA EM ABERTO eh diferente de VENDE(1)
		} else if((pessoa.getStatusPessoa().getBloqueia() == '1') && (pessoa.getStatusPessoa().getParcelaEmAberto() != '1')){
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
		
		OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ClienteDetalhesActivity.this);
		
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
	
	        graficoVendasPedidoMes.setOnChartGestureListener(this);
	        graficoVendasPedidoMes.setOnChartValueSelectedListener(this);
	        
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

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
		// TODO Auto-generated method stub
		
	}

}

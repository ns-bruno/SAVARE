package com.savare.activity.material.designer.fragment;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.EnderecoSql;
import com.savare.banco.funcoesSql.ParametrosSql;
import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.beans.CidadeBeans;
import com.savare.beans.EstadoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.PortadorBancoBeans;
import com.savare.beans.RamoAtividadeBeans;
import com.savare.beans.StatusBeans;
import com.savare.beans.TipoClienteBeans;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.CidadeRotinas;
import com.savare.funcoes.rotinas.EstadoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;
import com.savare.funcoes.rotinas.PlanoPagamentoRotinas;
import com.savare.funcoes.rotinas.PortadorBancoRotinas;
import com.savare.funcoes.rotinas.RamoAtividadeRotinas;
import com.savare.funcoes.rotinas.StatusRotinas;
import com.savare.funcoes.rotinas.TipoClienteRotinas;
import com.savare.funcoes.rotinas.TipoDocumentoRotinas;
import com.savare.funcoes.rotinas.async.EnviarDadosWebserviceAsyncRotinas;
import com.savare.webservice.WSSisinfoWebservice;

import java.util.ArrayList;
import java.util.List;

public class ClienteCadastroDadosMDFragment extends Fragment {

	View viewDados;
	private TextView textCodigoPessoa,
	 				 textStatus;
	private Spinner spinnerRamoAtividade,
					spinnerStatus,
					spinnerTipoCliente,
					spinnerTipoDocumento,
					spinnerPortadorBanco,
					spinnerPlanoPagamento,
					spinnerEstado,
					spinnerCidade;
	private EditText editRazaoSocial,
					 editFantasia,
					 editCnpjCpf,
					 editInscricaoEstadual,
					 editCep,
					 editBairro,
					 editEndereco,
					 editNumero,
					 editComplemento,
					 editEmail,
					 editCapitalSocial,
					 editLimiteCompra,
					 editDescontoAtacadoVista,
					 editDescontoAtacadoPrazo,
					 editDescontoVarejoVista,
					 editDescontoVarejoPrazo;
	private ItemUniversalAdapter adapterRamoAtividade,
								 adapterStatus,
								 adapterTipoCliente,
								 adapterTipoDocumento,
								 adapterPortadorBanco,
								 adapterPlanoPagamento,
								 adapterEstado,
								 adapterCidade;
	private RadioGroup radioGroupFisicaJuridica;
	private RadioButton radioButtonFisica,
						radioButtonJuridica;
	private TextWatcher cpfMask;
    private TextWatcher cnpjMask;
	private String idPessoaTemporario,
				   cadastradoSucesso = "N";
	private ProgressBar progressBarStatus;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewDados = inflater.inflate(R.layout.fragment_cliente_cadastro_dados_basico_md, container, false);

		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle parametro = getArguments();

		if(parametro != null){
			idPessoaTemporario = parametro.getString(ClienteCadastroTelefoneMDFragment.KEY_ID_PESSOA);
		}

		// Ativa a opcao de menus para este fragment
		setHasOptionsMenu(true);

		recuperarCamposTela();
		
		carregarDadosListas();
		
		// Ativa a opcao de menus para este fragment
		setHasOptionsMenu(true);
		
		// Armazene seus TextWatcher para posterior uso
        cpfMask = FuncoesPersonalizadas.insertMascara("###.###.###-##", editCnpjCpf);
        editCnpjCpf.addTextChangedListener(cpfMask);
        
        cnpjMask = FuncoesPersonalizadas.insertMascara("##.###.###/####-##", editCnpjCpf);
        
        radioGroupFisicaJuridica.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.fragment_cliente_cadastro_dados_basico_radioButton_juridica){
					editCnpjCpf.removeTextChangedListener(cpfMask);
					editCnpjCpf.addTextChangedListener(cnpjMask);
				} else {
					editCnpjCpf.removeTextChangedListener(cnpjMask);
					editCnpjCpf.addTextChangedListener(cpfMask);
				}
			}
		});
        
        spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				
				CidadeRotinas cidadeRotinas = new CidadeRotinas(getActivity());
				List<CidadeBeans> listaCidade = new ArrayList<CidadeBeans>();
				
				EstadoBeans estado = (EstadoBeans) spinnerEstado.getSelectedItem();
				
				listaCidade = cidadeRotinas.listaCidade("ID_CFAESTAD = " + estado.getIdEstado());
				
				if(listaCidade != null && listaCidade.size() > 0){
					adapterCidade = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.CIDADE);
					adapterCidade.setListaCidade(listaCidade);
					spinnerCidade.setAdapter(adapterCidade);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return viewDados;
	} // Fim onCreateView
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.cliente_cadastro_dados_fragment, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menu_cliente_cadastro_dados_fragemnt_salvar:
			salvar();
			break;

			case R.id.menu_cliente_cadastro_dados_fragemnt_enviar:

				EnviarDadosWebserviceAsyncRotinas enviarDados = new EnviarDadosWebserviceAsyncRotinas(getContext());
				enviarDados.setTabelaEnviarDados(new String[]{WSSisinfoWebservice.FUNCTION_SISINFOWEB_JSON_INSERT_CFACLIFO});
				enviarDados.setProgressBarStatus(progressBarStatus);
				enviarDados.setTextStatus(textStatus);
				// Checa se foi passado algum parametro
				if ( (cadastradoSucesso.equalsIgnoreCase("S")) && (idPessoaTemporario != null) ){
					// Especifica uma pessoa
					enviarDados.setIdPessoaTemporario(idPessoaTemporario);
				}
				enviarDados.execute();

				/*PessoaRotinas pessoaRotinas = new PessoaRotinas(getActivity());

				List<PessoaBeans> listaPessoasCadastro = new ArrayList<PessoaBeans>();
				// Pega a lista de pessoa a serem enviadas os dados
				listaPessoasCadastro = pessoaRotinas.listaPessoaCompleta(PessoaRotinas.KEY_TIPO_CLIENTE, where);
				// Checa se retornou alguma lista
				if (listaPessoasCadastro != null && listaPessoasCadastro.size() > 0){
					EnviarCadastroClienteFtpAsyncRotinas enviarCadastro = new EnviarCadastroClienteFtpAsyncRotinas(getActivity(), EnviarCadastroClienteFtpAsyncRotinas.TELA_CLIENTE_CADASTRO_DADOS_FRAGMENT);
					// Executa o envio do cadastro em segundo plano
					enviarCadastro.execute(listaPessoasCadastro);
				} else {
					// Dados da mensagem
					ContentValues mensagem = new ContentValues();
					mensagem.put("comando", 0);
					mensagem.put("tela", "ClienteCadastroDadosMDFragment");
					mensagem.put("mensagem", "Não achamos nenhum cadastro.");
					// Instancia a classe  de funcoes para mostra a mensagem
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
					funcoes.menssagem(mensagem);
				}*/

				break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void recuperarCamposTela(){
		textCodigoPessoa = (TextView) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_codigo_pessoa);
		textStatus = (TextView) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_status);
		editRazaoSocial = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_razao);
		editFantasia = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_fantasia);
		editCnpjCpf = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_cnpj_cpf);
		editInscricaoEstadual = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_inscricao_estadual);
		spinnerEstado = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_estado);
		spinnerCidade = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_cidade);
		editCep = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_cep);
		editBairro = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_bairro);
		editEndereco = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_endereco);
		editNumero = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_numero);
		editComplemento = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_complemento);
		editEmail = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_text_email);
		spinnerRamoAtividade = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_ramo_atividade);
		spinnerTipoCliente = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_tipo_cliente);
		spinnerStatus = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_status);
		spinnerTipoDocumento = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_tipo_documento);
		spinnerPortadorBanco = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_portador_banco);
		spinnerPlanoPagamento = (Spinner) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_spinner_plano_pagamento);
		editCapitalSocial = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_editText_capital_social);
		editLimiteCompra = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_editText_limite_compras);
		editDescontoAtacadoVista = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_editText_desconto_atacado_vista);
		editDescontoAtacadoPrazo = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_editText_desconto_atacado_prazo);
		editDescontoVarejoVista = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_editText_desconto_varejo_vista);
		editDescontoVarejoPrazo = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_editText_desconto_varejo_prazo);
		radioGroupFisicaJuridica = (RadioGroup) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_radioGroup_fisica_juridica);
		radioButtonFisica = (RadioButton) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_radioButton_fisica);
		radioButtonJuridica = (RadioButton) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_radioButton_juridica);
		progressBarStatus = (ProgressBar) viewDados.findViewById(R.id.fragment_cliente_cadastro_dados_basico_md_progressBar_status);
	}
	
	private void carregarDadosListas(){
		try{
			StatusRotinas statusRotinas = new StatusRotinas(getActivity());
			List<StatusBeans> listaStatus = new ArrayList<StatusBeans>();
			listaStatus = statusRotinas.listaStatus(null);

			if (listaStatus != null && listaStatus.size() > 0){
				adapterStatus = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.STATUS);
				adapterStatus.setListaStatus(listaStatus);
				spinnerStatus.setAdapter(adapterStatus);
			}

			EstadoRotinas estadoRotinas = new EstadoRotinas(getActivity());
			List<EstadoBeans> listaEstado = new ArrayList<EstadoBeans>();
			listaEstado = estadoRotinas.listaEstados();
			
			if(listaEstado != null && listaEstado.size() > 0){
				adapterEstado = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.ESTADO);
				adapterEstado.setListaEstado(listaEstado);
				spinnerEstado.setAdapter(adapterEstado);
			}
			
			
			RamoAtividadeRotinas ramoAtividadeRotinas = new RamoAtividadeRotinas(getActivity());
			List<RamoAtividadeBeans> listaRamoAtividade = new ArrayList<RamoAtividadeBeans>();
			listaRamoAtividade = ramoAtividadeRotinas.listaRamoAtividade();
			
			if(listaRamoAtividade != null && listaRamoAtividade.size() > 0){
				// Intancia a classe do adapter
				adapterRamoAtividade = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.RAMO_ATIVIDADE);
				// Preenche o adapter com uma lista de atividade
				adapterRamoAtividade.setListaRamoAtividade(listaRamoAtividade);
				spinnerRamoAtividade.setAdapter(adapterRamoAtividade);
			}
			
			TipoClienteRotinas tipoClienteRotinas = new TipoClienteRotinas(getActivity());
			
			List<TipoClienteBeans> listaTipoCliente = new ArrayList<TipoClienteBeans>();
			listaTipoCliente = tipoClienteRotinas.listaTipoCliente();
			
			if(listaTipoCliente != null && listaTipoCliente.size() > 0){
				// Intancia a classe do adapter
				adapterTipoCliente = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.TIPO_CLIENTE);
				// Preenche o adapter com uma lista de tipos de cliente
				adapterTipoCliente.setListaTipoCliente(listaTipoCliente);
				spinnerTipoCliente.setAdapter(adapterTipoCliente);
			}
			
			
			TipoDocumentoRotinas tipoDocumentoRotinas = new TipoDocumentoRotinas(getActivity());
	
			List<TipoDocumentoBeans> listaTipoDocumentoBeans = new ArrayList<TipoDocumentoBeans>();
			listaTipoDocumentoBeans = tipoDocumentoRotinas.listaTipoDocumento(null);
			
			if(listaTipoDocumentoBeans != null && listaTipoDocumentoBeans.size() > 0){
				// Intancia a classe do adapter
				adapterTipoDocumento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.TIPO_DOCUMENTO);
				// Preenche o adapter com uma lista de documentos
				adapterTipoDocumento.setListaTipoDocumento(listaTipoDocumentoBeans);
				spinnerTipoDocumento.setAdapter(adapterTipoDocumento);
			}
			
			PortadorBancoRotinas portadorBancoRotinas = new PortadorBancoRotinas(getActivity());
			
			List<PortadorBancoBeans> listaPortadorBanco = new ArrayList<PortadorBancoBeans>();
			listaPortadorBanco = portadorBancoRotinas.listaPortadorBanco();
			
			if(listaPortadorBanco != null && listaPortadorBanco.size() > 0){
				// Instancia a classe do adapter
				adapterPortadorBanco = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.PORTADOR_BANCO);
				// Preenche o adapter com uma lista portadores
				adapterPortadorBanco.setListaPortadorBanco(listaPortadorBanco);
				spinnerPortadorBanco.setAdapter(adapterPortadorBanco);
			}
			
			
			PlanoPagamentoRotinas planoPagamentoRotinas = new PlanoPagamentoRotinas(getActivity());
			
			List<PlanoPagamentoBeans> listaPlanoPagamento = new ArrayList<PlanoPagamentoBeans>();
			listaPlanoPagamento = planoPagamentoRotinas.listaPlanoPagamento("ATIVO = '1' AND ENVIA_PALM = '1'", "DESCRICAO", null);
			
			if(listaPlanoPagamento != null && listaPlanoPagamento.size() > 0){
				// Intancia a classe do adapter
				adapterPlanoPagamento = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.PLANO_PAGAMENTO);
				// Preenche o adapter com uma lista de planos de pagamento
				adapterPlanoPagamento.setListaPlanoPagamento(listaPlanoPagamento);
				spinnerPlanoPagamento.setAdapter(adapterPlanoPagamento);
			}
			
		} catch(Exception e){
			// Dados da mensagem
			ContentValues mensagem = new ContentValues();
			mensagem.put("comando", 0);
			mensagem.put("tela", "ClienteCadastroDadosMDFragment");
			mensagem.put("mensagem", "Não foi possível carregar os dados necessários. \n" + e.getMessage());
			// Instancia a classe  de funcoes para mostra a mensagem
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
			funcoes.menssagem(mensagem);
		}
		
				
	} // Fim da funcao carregarDadosPessoa

	private void salvar(){
		boolean insertsSucesso = true;
		// Valida os dados
		if (validarDados()) {

			ContentValues dadosCliente = new ContentValues();

			RamoAtividadeBeans atividade = (RamoAtividadeBeans) spinnerRamoAtividade.getSelectedItem();
			TipoClienteBeans tipoCliente = (TipoClienteBeans) spinnerTipoCliente.getSelectedItem();
			TipoDocumentoBeans documento = (TipoDocumentoBeans) spinnerTipoDocumento.getSelectedItem();
			PortadorBancoBeans portador = (PortadorBancoBeans) spinnerPortadorBanco.getSelectedItem();
			PlanoPagamentoBeans planoPagamento = (PlanoPagamentoBeans) spinnerPlanoPagamento.getSelectedItem();
			EstadoBeans estado = (EstadoBeans) spinnerEstado.getSelectedItem();
			CidadeBeans cidade = (CidadeBeans) spinnerCidade.getSelectedItem();

			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

			dadosCliente.put("ID_CFACLIFO", idPessoaTemporario);
			dadosCliente.put("ID_CFAATIVI", atividade.getIdRamoAtividade());
			dadosCliente.put("ID_CFATPCLI", tipoCliente.getIdTipoCliente());
			dadosCliente.put("NOME_RAZAO", editRazaoSocial.getText().toString());
			dadosCliente.put("NOME_FANTASIA", editFantasia.getText().toString());
			dadosCliente.put("CPF_CNPJ", editCnpjCpf.getText().toString());
			dadosCliente.put("IE_RG", editInscricaoEstadual.getText().toString());
			dadosCliente.put("CAPITAL_SOCIAL", editCapitalSocial.getText().toString());
			dadosCliente.put("CLIENTE", "1");
			dadosCliente.put("CODIGO_CLI" , idPessoaTemporario);
			dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
			dadosCliente.put("STATUS_CADASTRO_NOVO", "N");

			if (radioGroupFisicaJuridica.getCheckedRadioButtonId() == radioButtonFisica.getId()){
				dadosCliente.put("PESSOA", "0");
			} else {
				dadosCliente.put("PESSOA", "1");
			}

			PessoaSql pessoaSql = new PessoaSql(getActivity());
			long idPessoa = pessoaSql.insertOrReplace(dadosCliente);

			if (idPessoa < 0) {

				textCodigoPessoa.setText("" + idPessoaTemporario);
				ContentValues dadosEndereco = new ContentValues();
				dadosEndereco.put("ID_CFAENDER", idPessoaTemporario);
				dadosEndereco.put("ID_CFACLIFO", idPessoaTemporario);
				dadosEndereco.put("ID_SMAEMPRE", funcoes.getValorXml(FuncoesPersonalizadas.TAG_CODIGO_EMPRESA));
				dadosEndereco.put("ID_CFAESTAD", estado.getIdEstado());
				dadosEndereco.put("ID_CFACIDAD", cidade.getIdCidade());
				if (radioButtonJuridica.isChecked()) {
					dadosEndereco.put("TIPO", "1");
				} else {
					dadosEndereco.put("TIPO", "0");
				}
				dadosEndereco.put("CEP", editCep.getText().toString());
				dadosEndereco.put("BAIRRO", editBairro.getText().toString());
				dadosEndereco.put("LOGRADOURO", editEndereco.getText().toString());
				dadosEndereco.put("NUMERO", editNumero.getText().toString());
				dadosEndereco.put("COMPLEMENTO", editComplemento.getText().toString());
				dadosEndereco.put("EMAIL", editEmail.getText().toString());

				EnderecoSql enderecoSql = new EnderecoSql(getActivity());
				if (enderecoSql.insertOrReplace(dadosEndereco) >= -1){
					insertsSucesso = false;
				}
				// Instancia a classe para pegar os dados do usuario
				PessoaRotinas pessoaRotinas = new PessoaRotinas(getActivity());

				// Pega os dados do usuario
				List<PessoaBeans> listaPessoas = new ArrayList<PessoaBeans>();
				listaPessoas = pessoaRotinas.listaPessoaResumido("CFACLIFO.CODIGO_FUN = " + funcoes.getValorXml(FuncoesPersonalizadas.TAG_CODIGO_USUARIO), PessoaRotinas.KEY_TIPO_FUNCIONARIO, null);

				PessoaBeans dadosUsuario = null;

				if (listaPessoas.size() > 0) {
					dadosUsuario = listaPessoas.get(0);
				}

				ContentValues dadosParametro = new ContentValues();
				dadosParametro.put("ID_CFAPARAM", idPessoaTemporario);
				dadosParametro.put("ID_CFACLIFO", idPessoaTemporario);
				dadosParametro.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
				dadosParametro.put("ID_CFAPORTA", portador.getIdPortadorBanco());
				dadosParametro.put("ID_CFATPDOC", documento.getIdTipoDocumento());
				dadosParametro.put("ID_AEAPLPGT", planoPagamento.getIdPlanoPagamento());
				dadosParametro.put("ID_CFACLIFO_VENDE", (dadosUsuario != null) ? dadosUsuario.getIdPessoa() : -1);
				dadosParametro.put("LIMITE", editLimiteCompra.getText().toString());
				dadosParametro.put("DESC_ATAC_VISTA", editDescontoAtacadoVista.getText().toString());
				dadosParametro.put("DESC_ATAC_PRAZO", editDescontoAtacadoPrazo.getText().toString());
				dadosParametro.put("DESC_VARE_VISTA", editDescontoVarejoVista.getText().toString());
				dadosParametro.put("DESC_VARE_PRAZO", editDescontoVarejoPrazo.getText().toString());

				ParametrosSql parametrosSql = new ParametrosSql(getActivity());
				if (parametrosSql.insertOrReplace(dadosParametro) >= -1 ){
					insertsSucesso = false;
				}
			} else {
				insertsSucesso = false;
			}
			if (insertsSucesso){
				cadastradoSucesso = "S";

				SuperActivityToast.create(getContext(), getContext().getResources().getString(R.string.cadastro_sucesso), Style.DURATION_VERY_SHORT)
						.setTextColor(Color.WHITE)
						.setColor(Color.GREEN)
						.setAnimations(Style.ANIMATIONS_POP)
						.show();
			} else {
				SuperActivityToast.create(getContext(), getContext().getResources().getString(R.string.nao_foi_possivel_cadastrar), Style.DURATION_VERY_SHORT)
						.setTextColor(Color.WHITE)
						.setColor(Color.RED)
						.setAnimations(Style.ANIMATIONS_POP)
						.show();
			}
		}
	} // Fim salvar

	private boolean validarDados(){
		boolean retorno = false;

		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
		// Checa se eh um cnpj
		if ((editCnpjCpf.getText().toString().replace(".", "").replace("-", "").replace("/", "")).length() == 14){
			// Checa se o cnpj eh valido
			if (!funcoes.validaCNPJ(editCnpjCpf.getText().toString())){

				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "ClienteCadastroDadosMDFragment");
				mensagem.put("mensagem", "CNPJ Inválido.");

				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			} else {
				retorno = true;
			}
			// Checa se eh um CPF
		} else if ((editCnpjCpf.getText().toString().replace(".", "").replace("-", "").replace("/", "")).length() == 11){
			// Checa se eh um cpf valido
			if (!funcoes.validaCPF(editCnpjCpf.getText().toString())){
				// Cria uma variavem para inserir as propriedades da mensagem
				ContentValues mensagem = new ContentValues();
				mensagem.put("comando", 2);
				mensagem.put("tela", "ClienteCadastroDadosMDFragment");
				mensagem.put("mensagem", "CPF Inválido.");

				// Executa a mensagem passando por parametro as propriedades
				funcoes.menssagem(mensagem);
			} else {
				retorno = true;
			}
		}
		return retorno;
	}
}

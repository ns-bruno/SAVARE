package com.savare.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.material.designer.ListaOrcamentoPedidoMDActivity;
import com.savare.activity.material.designer.ProdutoDetalhesMDActivity;
import com.savare.beans.AreaBeans;
import com.savare.beans.CidadeBeans;
import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.EmbalagemBeans;
import com.savare.beans.EstadoBeans;
import com.savare.beans.EstoqueBeans;
import com.savare.beans.ItemOrcamentoBeans;
import com.savare.beans.LogBeans;
import com.savare.beans.OrcamentoBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.PlanoPagamentoBeans;
import com.savare.beans.PortadorBancoBeans;
import com.savare.beans.ProdutoListaBeans;
import com.savare.beans.RamoAtividadeBeans;
import com.savare.beans.StatusBeans;
import com.savare.beans.TelefoneBeans;
import com.savare.beans.TipoClienteBeans;
import com.savare.beans.TipoDocumentoBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.ProdutoRotinas;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemUniversalAdapter extends BaseAdapter implements Filterable, OnItemClickListener {

	public static final int ITEM_ORCAMENTO = 0, 
							PRODUTO = 1, 
							RATEIO_ITEM_ORCAMENTO = 2, 
							TIPO_DOCUMENTO = 3, 
							PLANO_PAGAMENTO = 4, 
							LISTA_ORCAMENTO_PEDIDO = 5, 
							RATEIO_ORCAMENTO = 6, 
							RAMO_ATIVIDADE = 7,
							TIPO_CLIENTE = 8, 
							PORTADOR_BANCO = 9,
							ESTOQUE = 10,
							DETALHES_PRODUTOS = 11,
							EMBALAGEM = 12,
							PLANO_PAGAMENTO_ORCAMENTO = 13,
							TABELA_LOG = 14,
							LOG = 15,
							ESTADO = 16,
							CIDADE = 17,
							TELEFONE = 18,
							STATUS = 19,
							AREA = 20,
							CIDADE_DARK = 21,
							CLIENTE = 22,
							HISTORICO_PRECO_ITEM_ORCAMENTO = 23;
	private Context context;
	private int tipoItem, diasProdutoNovo;
	private int campoAtualProduto = -1;
	private String atacadoVarejo, idOrcamento, ATACADO = "0";
	private List<ItemOrcamentoBeans> listaItemOrcamento; // Lista de produtos dentro do orcamento
	private List<ProdutoListaBeans> listaProduto; // Lista de produtos para venda
	private List<TipoDocumentoBeans> listaTipoDocumento;
	private List<PlanoPagamentoBeans> listaPlanoPagamento;
	private List<OrcamentoBeans> listaOrcamentoPedido;
	private List<RamoAtividadeBeans> listaRamoAtividade;
	private List<TipoClienteBeans> listaTipoCliente;
	private List<PortadorBancoBeans> listaPortadorBanco;
	private List<EstoqueBeans> listaEstoque;
	private List<DescricaoDublaBeans> listaDetalhesProduto;
	private List<EmbalagemBeans> listaEmbalagem;
	private List<LogBeans> listaLog,
						   listaTabelaLog;
	private List<EstadoBeans> listaEstado;
	private List<CidadeBeans> listaCidade;
	private List<TelefoneBeans> listaTelefone;
	private List<StatusBeans> listaStatus;
	private List<AreaBeans> listaArea;
	private List<PessoaBeans> listaPessoa;
	private FuncoesPersonalizadas funcoes;
	
	/**
	 * 
	 * @param context
	 * @param tipoItem - 0 = ITEM ORCAMENTO, 1 = PRODUTO, 2 = RATEIO ITEM ORCAMENTO, 
	 * 3 = TIPO_DOCUMENTO, 4 = PLANO DE PAGAMENTO, 5 = LISTA_ORCAMENTO_PEDIDO, 6 = RATEIO_ORCAMENTO, 
	 * 7 = RAMO_ATIVIDADE, 8 = TIPO_CLIENTE, 9 = PORTADOR_BANCO
	 */
	public ItemUniversalAdapter(Context context, int tipoItem) {
		this.context = context;
		this.tipoItem = tipoItem;
	}
	
	/**
	 * Construtor para passar por parametro a lista de itens do orcamento.
	 * 
	 * @param context
	 * @param tipoItem - 0 = ITEM ORCAMENTO, 1 = PRODUTO, 2 = RATEIO ITEM ORCAMENTO, 
	 * 3 = TIPO_DOCUMENTO, 4 = PLANO DE PAGAMENTO, 5 = LISTA_ORCAMENTO_PEDIDO, 6 = RATEIO_ORCAMENTO, 
	 * 7 = RAMO_ATIVIDADE, 8 = TIPO_CLIENTE, 9 = PORTADOR_BANCO
	 * @param listaItemOrcamento - List<ItemOrcamentoBeans>
	 */
	public ItemUniversalAdapter(Context context, int tipoItem, List<ItemOrcamentoBeans> listaItemOrcamento) {
		this.context = context;
		this.tipoItem = tipoItem;
		this.listaItemOrcamento = listaItemOrcamento;
		
	}

	
	
	/**
	 * @return the listaEstoque
	 */
	public List<EstoqueBeans> getListaEstoque() {
		return listaEstoque;
	}

	/**
	 * @param listaEstoque the listaEstoque to set
	 */
	public void setListaEstoque(List<EstoqueBeans> listaEstoque) {
		this.listaEstoque = listaEstoque;
	}

	/**
	 * @return the listaOrcamentoPedido
	 */
	public List<OrcamentoBeans> getListaOrcamentoPedido() {
		return listaOrcamentoPedido;
	}

	/**
	 * @return the listaPlanoPagamento
	 */
	public List<PlanoPagamentoBeans> getListaPlanoPagamento() {
		return listaPlanoPagamento;
	}

	/**
	 * @param listaPlanoPagamento the listaPlanoPagamento to set
	 */
	public void setListaPlanoPagamento(List<PlanoPagamentoBeans> listaPlanoPagamento) {
		this.listaPlanoPagamento = listaPlanoPagamento;
	}

	/**
	 * @return the listaTipoDocumento
	 */
	public List<TipoDocumentoBeans> getListaTipoDocumento() {
		return listaTipoDocumento;
	}

	/**
	 * @param listaTipoDocumento the listaTipoDocumento to set
	 */
	public void setListaTipoDocumento(List<TipoDocumentoBeans> listaTipoDocumento) {
		this.listaTipoDocumento = listaTipoDocumento;
	}

	/**
	 * Pega a lista de produto que tem dentro do orcamento.
	 * 
	 * @return uma lista de produtos que esta salva dentro de um determinado orcamento
	 */
	public List<ItemOrcamentoBeans> getListaItemOrcamento() {
		return listaItemOrcamento;
	}

	/**
	 * Seta a lista de produtos dentro do orcamento.
	 * 
	 * @param listaItemOrcamento a lista de produtos que esta em um determinado orcamento
	 */
	public void setListaItemOrcamento(List<ItemOrcamentoBeans> listaItemOrcamento) {
		this.listaItemOrcamento = listaItemOrcamento;
	}

	/**
	 * @return lista de produto geral para venda. Todos os produtos.
	 */
	public List<ProdutoListaBeans> getListaProduto() {
		return listaProduto;
	}

	/**
	 * @param listaProduto the listaProduto to set
	 */
	public void setListaProduto(List<ProdutoListaBeans> listaProduto) {
		this.listaProduto = listaProduto;
	}
	
	
	/**
	 * @return the listaOrcamento
	 */
	public List<OrcamentoBeans> getListaOrcamentoPediso() {
		return listaOrcamentoPedido;
	}

	/**
	 * @param listaOrcamentoPedido the listaOrcamento to set
	 */
	public void setListaOrcamentoPedido(List<OrcamentoBeans> listaOrcamentoPedido) {
		this.listaOrcamentoPedido = listaOrcamentoPedido;
	}

	/**
	 * @return the atacadoVarejo
	 */
	public String getAtacadoVarejo() {
		return atacadoVarejo;
	}

	/**
	 * @param atacadoVarejo the atacadoVarejo to set
	 */
	public void setAtacadoVarejo(String atacadoVarejo) {
		this.atacadoVarejo = atacadoVarejo;
	}
	

	/**
	 * @return the idOrcamento
	 */
	public String getIdOrcamento() {
		return idOrcamento;
	}

	/**
	 * @param idOrcamento the idOrcamento to set
	 */
	public void setIdOrcamento(String idOrcamento) {
		this.idOrcamento = idOrcamento;
	}
	

	public int getTipoItem() {
		return tipoItem;
	}

	public void setTipoItem(int tipoItem) {
		this.tipoItem = tipoItem;
	}

	/**
	 * @return the listaRamoAtividade
	 */
	public List<RamoAtividadeBeans> getListaRamoAtividade() {
		return listaRamoAtividade;
	}
	
	/**
	 * @return the listaTipoCliente
	 */
	public List<TipoClienteBeans> getListaTipoCliente() {
		return listaTipoCliente;
	}

	/**
	 * @param listaTipoCliente the listaTipoCliente to set
	 */
	public void setListaTipoCliente(List<TipoClienteBeans> listaTipoCliente) {
		this.listaTipoCliente = listaTipoCliente;
	}

	/**
	 * @return the listaPortadorBanco
	 */
	public List<PortadorBancoBeans> getListaPortadorBanco() {
		return listaPortadorBanco;
	}

	/**
	 * @param listaPortadorBanco the listaPortadorBanco to set
	 */
	public void setListaPortadorBanco(List<PortadorBancoBeans> listaPortadorBanco) {
		this.listaPortadorBanco = listaPortadorBanco;
	}

	/**
	 * @param listaRamoAtividade the listaRamoAtividade to set
	 */
	public void setListaRamoAtividade(List<RamoAtividadeBeans> listaRamoAtividade) {
		this.listaRamoAtividade = listaRamoAtividade;
	}

	
	/**
	 * @return the listaDetalhesProduto
	 */
	public List<DescricaoDublaBeans> getListaDetalhesProduto() {
		return listaDetalhesProduto;
	}

	/**
	 * @param listaDetalhesProduto the listaDetalhesProduto to set
	 */
	public void setListaDetalhesProduto(
			List<DescricaoDublaBeans> listaDetalhesProduto) {
		this.listaDetalhesProduto = listaDetalhesProduto;
	}
	
	/**
	 * @return the listaEmbalagem
	 */
	public List<EmbalagemBeans> getListaEmbalagem() {
		return listaEmbalagem;
	}

	/**
	 * @param listaEmbalagem the listaEmbalagem to set
	 */
	public void setListaEmbalagem(List<EmbalagemBeans> listaEmbalagem) {
		this.listaEmbalagem = listaEmbalagem;
	}

	/**
	 * @return the listaLog
	 */
	public List<LogBeans> getListaLog() {
		return listaLog;
	}

	/**
	 * @param listaLog the listaLog to set
	 */
	public void setListaLog(List<LogBeans> listaLog) {
		this.listaLog = listaLog;
	}

	/**
	 * @return the listaTabelaLog
	 */
	public List<LogBeans> getListaTabelaLog() {
		return listaTabelaLog;
	}

	/**
	 * @param listaTabelaLog the listaTabelaLog to set
	 */
	public void setListaTabelaLog(List<LogBeans> listaTabelaLog) {
		this.listaTabelaLog = listaTabelaLog;
	}
	
	/**
	 * @return the listaEstado
	 */
	public List<EstadoBeans> getListaEstado() {
		return listaEstado;
	}

	/**
	 * @param listaEstado the listaEstado to set
	 */
	public void setListaEstado(List<EstadoBeans> listaEstado) {
		this.listaEstado = listaEstado;
	}

	/**
	 * @return the listaCidade
	 */
	public List<CidadeBeans> getListaCidade() {
		return listaCidade;
	}

	/**
	 * @param listaCidade the listaCidade to set
	 */
	public void setListaCidade(List<CidadeBeans> listaCidade) {
		this.listaCidade = listaCidade;
	}
	
	/**
	 * @return the listaTelefone
	 */
	public List<TelefoneBeans> getListaTelefone() {
		return listaTelefone;
	}

	/**
	 * @param listaTelefone the listaTelefone to set
	 */
	public void setListaTelefone(List<TelefoneBeans> listaTelefone) {
		this.listaTelefone = listaTelefone;
	}

	public List<StatusBeans> getListaStatus() {
		return listaStatus;
	}

	public void setListaStatus(List<StatusBeans> listaStatus) {
		this.listaStatus = listaStatus;
	}

	public List<AreaBeans> getListaArea() {
		return listaArea;
	}

	public void setListaArea(List<AreaBeans> listaArea) {
		this.listaArea = listaArea;
	}

	public List<PessoaBeans> getListaPessoa() {
		return listaPessoa;
	}

	public void setListaPessoa(List<PessoaBeans> listaPessoa) {
		this.listaPessoa = listaPessoa;
	}

	@Override
	public int getCount() {
		// Verifica o tipo de item
		if((this.tipoItem == ITEM_ORCAMENTO) || (this.tipoItem == RATEIO_ITEM_ORCAMENTO) || (this.tipoItem == HISTORICO_PRECO_ITEM_ORCAMENTO)){
			// Retorna a quantidade de item de orcamento da lista
			return listaItemOrcamento.size();
			
		} else if(this.tipoItem == PRODUTO){
			// Retorna a quantidade de produto da lista
			return listaProduto.size();
			
		} else if(this.tipoItem == TIPO_DOCUMENTO){
			// Retorna a quantidade de documentos da lista
			return listaTipoDocumento.size();
			
		} else if( (this.tipoItem == PLANO_PAGAMENTO) || (this.tipoItem == PLANO_PAGAMENTO_ORCAMENTO) ){
			
			return this.listaPlanoPagamento.size();
			
		} else if((this.tipoItem == LISTA_ORCAMENTO_PEDIDO) || (this.tipoItem == RATEIO_ORCAMENTO)){
			// Retorna a quantidade de orscamento da lista
			return this.listaOrcamentoPedido.size();
		} else if(this.tipoItem == RAMO_ATIVIDADE){
			
			return this.listaRamoAtividade.size();
			
		} else if(this.tipoItem == TIPO_CLIENTE){
			
			return this.listaTipoCliente.size();
			
		} else if(this.tipoItem == PORTADOR_BANCO){
			
			return this.listaPortadorBanco.size();
			
		} else if(this.tipoItem == ESTOQUE){
			
			return this.listaEstoque.size();
		
		} else if(this.tipoItem == DETALHES_PRODUTOS){
			
			return this.listaDetalhesProduto.size();
		
		} else if(this.tipoItem == EMBALAGEM){
			
			return this.listaEmbalagem.size();
		
		} else if(this.tipoItem == TABELA_LOG){
			
			return this.listaTabelaLog.size();
		
		} else if(this.tipoItem == LOG){
			
			return this.listaLog.size();
		
		} else if(this.tipoItem == ESTADO){
			
			return this.listaEstado.size();
			
		} else if(this.tipoItem == CIDADE || this.tipoItem == CIDADE_DARK){
			
			return this.listaCidade.size();
			
		} else if(this.tipoItem == TELEFONE){
			
			return this.listaTelefone.size();
			
		} else if (this.tipoItem == STATUS) {

			return  this.listaStatus.size();

		} else if (this.tipoItem == AREA){

			return this.listaArea.size();
		} else if (this.tipoItem == CLIENTE){

			return this.listaPessoa.size();
		} else{
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		// Verifica o tipo de item
		if((this.tipoItem == ITEM_ORCAMENTO) || (this.tipoItem == RATEIO_ITEM_ORCAMENTO) || (this.tipoItem == HISTORICO_PRECO_ITEM_ORCAMENTO)){
			// Retorna um item do orcamento
			return listaItemOrcamento.get(position);
			
		} else if(this.tipoItem == PRODUTO){
			// Retorna um produto
			return listaProduto.get(position);
			
		} else if(this.tipoItem == TIPO_DOCUMENTO){
			// Retorna um tipo de documento
			return listaTipoDocumento.get(position);
		
		} else if( (this.tipoItem == PLANO_PAGAMENTO) || (this.tipoItem == PLANO_PAGAMENTO_ORCAMENTO) ){
			// Retorna um plano de pagamento
			return listaPlanoPagamento.get(position);
		
		} else if((this.tipoItem == LISTA_ORCAMENTO_PEDIDO) || (this.tipoItem == RATEIO_ORCAMENTO)){
			// Retona um item da lista de orcamento/pedido
			return listaOrcamentoPedido.get(position);
			
		} else if(this.tipoItem == RAMO_ATIVIDADE){
			
			return this.listaRamoAtividade.get(position);
			
		} else if(this.tipoItem == TIPO_CLIENTE){
			
			return this.listaTipoCliente.get(position);
			
		} else if(this.tipoItem == PORTADOR_BANCO){
			
			return this.listaPortadorBanco.get(position);
			
		} else if(this.tipoItem == ESTOQUE){
			
			return this.listaEstoque.get(position);
			
		} else if(this.tipoItem == DETALHES_PRODUTOS){
			
			return this.listaDetalhesProduto.get(position);
			
		} else if(this.tipoItem == EMBALAGEM){
			
			return this.listaEmbalagem.get(position);
			
		} else if(this.tipoItem == TABELA_LOG){
			
			return this.listaTabelaLog.get(position);
			
		} else if(this.tipoItem == LOG){
			
			return this.listaLog.get(position);
			
		} else if(this.tipoItem == ESTADO){
			
			return this.listaEstado.get(position);
			
		} else if(this.tipoItem == CIDADE || this.tipoItem == CIDADE_DARK){
			
			return this.listaCidade.get(position);
			
		}  else if(this.tipoItem == TELEFONE){
			
			return this.listaTelefone.get(position);
			
		} else if(this.tipoItem == STATUS){

			return this.listaStatus.get(position);

		} else if(this.tipoItem == AREA){

			return this.listaArea.get(position);

		} else if(this.tipoItem == CLIENTE){

			return this.listaPessoa.get(position);

		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		
		if(this.tipoItem == PRODUTO){
			return this.listaProduto.get(position).getProduto().getIdProduto();
		
		}else if(this.tipoItem == TIPO_DOCUMENTO){
			return this.listaTipoDocumento.get(position).getIdTipoDocumento();
			
		} else if( (this.tipoItem == PLANO_PAGAMENTO) || (this.tipoItem == PLANO_PAGAMENTO_ORCAMENTO) ){
			return this.listaPlanoPagamento.get(position).getIdPlanoPagamento();
			
		} else if((this.tipoItem == LISTA_ORCAMENTO_PEDIDO) || (this.tipoItem == RATEIO_ORCAMENTO)){
			return this.listaOrcamentoPedido.get(position).getIdOrcamento();
			
		} else if(this.tipoItem == RAMO_ATIVIDADE){
			
			return this.listaRamoAtividade.get(position).getIdRamoAtividade();
			
		} else if(this.tipoItem == TIPO_CLIENTE){
			
			return this.listaTipoCliente.get(position).getIdTipoCliente();
			
		} else if(this.tipoItem == PORTADOR_BANCO){
			
			return this.listaPortadorBanco.get(position).getIdPortadorBanco();
			
		} else if(this.tipoItem == PORTADOR_BANCO){
			
			return this.listaEstoque.get(position).getIdEstoque();
			
		} else if(this.tipoItem == DETALHES_PRODUTOS){
			
			return position;
			
		} else if(this.tipoItem == EMBALAGEM){
			
			return this.listaEmbalagem.get(position).getIdEmbalagem();
			
		}  else if(this.tipoItem == TABELA_LOG){
			
			return this.listaTabelaLog.get(position).getIdLog();
			
		} else if(this.tipoItem == LOG){
			
			return this.listaLog.get(position).getIdLog();
			
		} else if(this.tipoItem == ESTADO){
			
			return this.listaEstado.get(position).getIdEstado();
			
		} else if(this.tipoItem == CIDADE || this.tipoItem == CIDADE_DARK){
			
			return this.listaCidade.get(position).getIdCidade();
			
		} else if(this.tipoItem == TELEFONE){
			
			return this.listaTelefone.get(position).getIdTelefone();
			
		} else if(this.tipoItem == STATUS){

			return this.listaStatus.get(position).getIdStatus();

		} else if (this.tipoItem == AREA){

			return this.listaArea.get(position).getIdArea();
		} else if (this.tipoItem == CLIENTE){

			return this.listaPessoa.get(position).getIdPessoa();
		} else {
			return position;
		}
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = convertView;
		
		/*if (view == null) {*/
			/*
			 * Recupera o servico LayoutInflater que eh o servidor que ira
			 * transformar o nosso layout layout_pessoa em uma View
			 */
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			/*
			 * Converte nosso layout em uma view
			 */
			view = inflater.inflate(R.layout.layout_item_universal, null);
			
		//} // Fim do if (view == null)
		
		/**
		 * Recupero os compoentes que estao dentro do layout_item_universal
		 */
		TextView textDescricao = (TextView) view.findViewById(R.id.layout_item_universal_text_descricao);
		TextView textAbaixoDescricaoEsqueda = (TextView) view.findViewById(R.id.layout_item_universal_text_abaixo_descricao_esquerda);
		TextView textAbaixoDescricaoDireita = (TextView) view.findViewById(R.id.layout_item_universal_text_abaixo_descricao_direita);
		TextView textBottonEsquerdo = (TextView) view.findViewById(R.id.layout_item_universal_text_botton_esquerdo);
		TextView textBottonEsquerdoDois = (TextView) view.findViewById(R.id.layout_item_universal_text_botton_esquerdo_dois);
		TextView textBottonDireito = (TextView) view.findViewById(R.id.layout_item_universal_text_botton_direito);
		View viewTopo = (View) view.findViewById(R.id.layout_item_universal_view_topo);
		View viewRodape = (View) view.findViewById(R.id.layout_item_universal_view_rodape);
		ImageView imageOpcao = (ImageView) view.findViewById(R.id.layout_item_universal_imageView_opcao);
		CircleImageView imageCirclePrincipal = (CircleImageView) view.findViewById(R.id.layout_item_universal_profile_image);
		
		funcoes = new FuncoesPersonalizadas(context);
		
		// Checa se o tamanho da fonte eh grante
		if(funcoes.getValorXml("TamanhoFonte").equalsIgnoreCase("G")){
		
			textDescricao.setTextAppearance(context, R.style.textoGrante);
			textAbaixoDescricaoEsqueda.setTextAppearance(context, R.style.textoGrante);
			textAbaixoDescricaoDireita.setTextAppearance(context, R.style.textoGrante);
			textBottonEsquerdo.setTextAppearance(context, R.style.textoGrante);
			textBottonEsquerdoDois.setTextAppearance(context, R.style.textoGrante);
			textBottonDireito.setTextAppearance(context, R.style.textoGrante);
			
		// Checa se o tamanho da fonte eh grante
		} else if(funcoes.getValorXml("TamanhoFonte").equalsIgnoreCase("M")){
			textDescricao.setTextAppearance(context, R.style.textoMendio);
			textAbaixoDescricaoEsqueda.setTextAppearance(context, R.style.textoMendio);
			textAbaixoDescricaoDireita.setTextAppearance(context, R.style.textoMendio);
			textBottonEsquerdo.setTextAppearance(context, R.style.textoMendio);
			textBottonEsquerdoDois.setTextAppearance(context, R.style.textoMendio);
			textBottonDireito.setTextAppearance(context, R.style.textoMendio);
		}
		
		// Verifica se o tipo de item universal eh para ITENS DO ORCAMENTO
		if( (this.tipoItem == ITEM_ORCAMENTO) || (this.tipoItem == HISTORICO_PRECO_ITEM_ORCAMENTO) ){
			/**
			 * Recupera dentro da lista de pessoas, uma pessoa(nome) especifica de acordo com a
			 * posicao passada no parametro do getView
			 */
			ItemOrcamentoBeans item = listaItemOrcamento.get(position);
			// Instancia a classe de funcoes para serem usadas
			//funcoes = new FuncoesPersonalizadas(context);
			
			textDescricao.setText(item.getProduto().getDescricaoProduto() + " - " + item.getProduto().getDescricaoMarca() +
								( ( (this.tipoItem == HISTORICO_PRECO_ITEM_ORCAMENTO) && (item.getDataCadastro() != null) ) ? " (" + funcoes.formataDataHora(item.getDataCadastro()) + ")" : "" ));
			textAbaixoDescricaoEsqueda.setText("Código: " + item.getProduto().getCodigoEstrutural());
			textAbaixoDescricaoDireita.setText("Qtd.: " + (funcoes.arredondarValor(item.getQuantidade())));
			textBottonEsquerdo.setText("Unitário: " + funcoes.arredondarValor(item.getValorLiquidoUnitario()));
			textBottonEsquerdoDois.setText(" | " + item.getUnidadeVenda().getSiglaUnidadeVenda());
			textBottonDireito.setText("Total: " + funcoes.arredondarValor(String.valueOf(item.getValorLiquido())));

			// Checa se este item foi conferido ou ao menos faturado
			if ( (item.getStatusRetorno() != null) && (item.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.ITEM_NAO_CONFERIDO)) ){
				textDescricao.setPaintFlags(textDescricao.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoEsqueda.setPaintFlags(textAbaixoDescricaoEsqueda.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoDireita.setPaintFlags(textAbaixoDescricaoDireita.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdo.setPaintFlags(textBottonEsquerdo.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdoDois.setPaintFlags(textBottonEsquerdoDois.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonDireito.setPaintFlags(textBottonDireito.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

			}

			if ((item.getStatusRetorno() != null) && (item.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_FATURADO))){
				textAbaixoDescricaoDireita.setText("Qtd. Fat.: " + (funcoes.arredondarValor(item.getQuantidadeFaturada())));
				textBottonDireito.setText("T. Fat.: " + funcoes.arredondarValor(String.valueOf(item.getValorLiquidoFaturado())));
				textBottonEsquerdo.setText("Unit. Fat.: " + funcoes.arredondarValor(item.getValorLiquidoFaturado() / item.getQuantidadeFaturada()));
			}

			viewTopo.setVisibility(View.INVISIBLE);
			viewRodape.setVisibility(View.INVISIBLE);
			
			if(item.isTagSelectContext()){
				view.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
				textDescricao.setTypeface(null, Typeface.BOLD);
				textAbaixoDescricaoEsqueda.setTypeface(null, Typeface.BOLD);
				textAbaixoDescricaoDireita.setTypeface(null, Typeface.BOLD);
				textBottonDireito.setTypeface(null, Typeface.BOLD);
				textBottonEsquerdo.setTypeface(null, Typeface.BOLD);
				textBottonEsquerdoDois.setTypeface(null, Typeface.BOLD);
				
			} else {
				view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
				textDescricao.setTypeface(null, Typeface.NORMAL);
				textAbaixoDescricaoEsqueda.setTypeface(null, Typeface.NORMAL);
				textAbaixoDescricaoDireita.setTypeface(null, Typeface.NORMAL);
				textBottonDireito.setTypeface(null, Typeface.NORMAL);
				textBottonEsquerdo.setTypeface(null, Typeface.NORMAL);
				textBottonEsquerdoDois.setTypeface(null, Typeface.NORMAL);
			}
			
			// Verifica se o tipo eH produto
		} else if(this.tipoItem == PRODUTO){
			/**
			 * Recupera dentro da lista de produtos, um produto especifico de acordo com a
			 * posicao passada no parametro do getView
			 */
			ProdutoListaBeans produto = listaProduto.get(position);
			
			this.funcoes = new FuncoesPersonalizadas(context);
			
			textDescricao.setText(produto.getProduto().getDescricaoProduto() + " - " + produto.getProduto().getDescricaoMarca());
			textAbaixoDescricaoEsqueda.setText("Cód. " + produto.getProduto().getCodigoEstrutural());
			textAbaixoDescricaoDireita.setText("Ref. " + produto.getProduto().getReferencia());
			textBottonEsquerdoDois.setText(produto.getProduto().getUnidadeVendaProduto().getSiglaUnidadeVenda());
			textBottonDireito.setText("Est.: " + this.funcoes.arredondarValor(produto.getEstoqueFisico()));
			// Checa se tem alguma imagem salva no produto
			if ( (funcoes.getValorXml("ImagemProduto").equalsIgnoreCase("S")) && (produto.getProduto().getImagemProduto() != null) && (produto.getProduto().getImagemProduto().getFotos() != null)){
				// Torna o campo da imagem do produto visivel
				imageCirclePrincipal.setVisibility(View.VISIBLE);
				// Pega a imagem que esta no banco de dados
				imageCirclePrincipal.setImageBitmap(produto.getProduto().getImagemProduto().getImagem());
			}
			// Verifica se o estoque eh menor ou igual a que zero
			if(produto.getEstoqueFisico() < 1){
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.vermelho));
				textBottonDireito.setTag(produto.getEstoqueFisico());
			}
			
			// Verifica se o tipo da venda eh atacado ou varejo
			if(atacadoVarejo.equalsIgnoreCase(ATACADO)){
				// Verifica se o produto esta na promocao
				if(produto.getValorPromocaoAtacado() > 0){
					// Seta o preco da promocao atacado
					textBottonEsquerdo.setText("R$ " + this.funcoes.arredondarValor(produto.getValorPromocaoAtacado()));
					// Muda a cor da view para amarelo
					viewTopo.setBackgroundColor(context.getResources().getColor(R.color.amarelo));
				}else{
					// Seta o preco normal
					textBottonEsquerdo.setText("R$ " + this.funcoes.arredondarValor(produto.getValorUnitarioAtacado()));
					// Torna a vivew Invisivel, mas ocupa o mesmo espaco
					viewTopo.setVisibility(View.INVISIBLE);
					//viewTopo.setBackgroundColor(context.getResources().getColor(R.color.branco));
				}
			
			}else{
				// Verifica se o produto esta na promocao
				if(produto.getValorPromocaoVarejo() > 0){
					// Seta o preco de promocao varejo
					textBottonEsquerdo.setText("R$ " + this.funcoes.arredondarValor(String.valueOf(produto.getValorPromocaoVarejo())));
					// Muda a cor da view para amarelo (promocao)
					viewTopo.setBackgroundColor(context.getResources().getColor(R.color.amarelo));
				}else{
					// Seta o preco normal de varejo
					textBottonEsquerdo.setText("R$ " + this.funcoes.arredondarValor(String.valueOf(produto.getValorUnitarioVarejo())));
					// Torna a vivew Invisivel, mas ocupa o mesmo espaco
					viewTopo.setVisibility(View.INVISIBLE);
				}
			}
			// Verifica se tem estoque contabil
			if(produto.getEstoqueContabil() < 1){
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
				viewRodape.setTag(produto.getEstoqueContabil());
			}else{
				viewRodape.setVisibility(View.INVISIBLE);
			}
			
			
			// Muda a cor do texto da listView se o produto ja estiver no orcamento
			if(produto.getEstaNoOrcamento() == '1'){
				// muda a cor dos texto do produto
				textDescricao.setTextColor(this.context.getResources().getColor(R.color.verde));
				textAbaixoDescricaoDireita.setTextColor(this.context.getResources().getColor(R.color.verde));
				textAbaixoDescricaoEsqueda.setTextColor(this.context.getResources().getColor(R.color.verde));
				textBottonDireito.setTextColor(this.context.getResources().getColor(R.color.verde));
				textBottonEsquerdo.setTextColor(this.context.getResources().getColor(R.color.verde));
				textBottonEsquerdoDois.setTextColor(this.context.getResources().getColor(R.color.verde));
			}
			

			ProdutoRotinas produtoRotinas = new ProdutoRotinas(context);
			
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			
			//diasProdutoNovo = produtoRotinas.diasProdutoNovo(funcoes.getValorXml("CodigoEmpresa"));
			
			// Checa se eh um produto novo
			if(produto.isProdutoNovo()){
				// Muda o fundo do view
				view.setBackgroundColor(context.getResources().getColor(R.color.azul_medio_200));
				
				// muda a cor dos texto do produto
				textDescricao.setTextColor(this.context.getResources().getColor(R.color.branco));
				textAbaixoDescricaoDireita.setTextColor(this.context.getResources().getColor(R.color.branco));
				textAbaixoDescricaoEsqueda.setTextColor(this.context.getResources().getColor(R.color.branco));
				textBottonDireito.setTextColor(this.context.getResources().getColor(R.color.branco));
				textBottonEsquerdo.setTextColor(this.context.getResources().getColor(R.color.branco));
				textBottonEsquerdoDois.setTextColor(this.context.getResources().getColor(R.color.branco));
				
				// Deixa o texto em negrito
				textDescricao.setTypeface(null, Typeface.BOLD);
				textAbaixoDescricaoEsqueda.setTypeface(null, Typeface.BOLD);
				textAbaixoDescricaoDireita.setTypeface(null, Typeface.BOLD);
				textBottonDireito.setTypeface(null, Typeface.BOLD);
				textBottonEsquerdo.setTypeface(null, Typeface.BOLD);
				textBottonEsquerdoDois.setTypeface(null, Typeface.BOLD);
			}
			// Visualiza o botão de opcao

			imageOpcao.setVisibility(View.VISIBLE);
			
		} else if(this.tipoItem == RATEIO_ITEM_ORCAMENTO){
			/**
			 * Recupera dentro da lista de pessoas, uma pessoa(nome) especifica de acordo com a
			 * posicao passada no parametro do getView
			 */
			ItemOrcamentoBeans item = listaItemOrcamento.get(position);
			
			textDescricao.setText(item.getProduto().getDescricaoProduto() + " - " + item.getProduto().getDescricaoMarca());
			textAbaixoDescricaoEsqueda.setText("Código: " + item.getProduto().getCodigoEstrutural());
			textAbaixoDescricaoDireita.setText("Qtd.: " + String.valueOf(item.getQuantidade()));
			// Instancia a classe de funcoes
			this.funcoes = new FuncoesPersonalizadas(context);
			
			textBottonEsquerdo.setText("Tab.: " + funcoes.arredondarValor(item.getValorTabela()) +
									   " - Ve.: " + funcoes.arredondarValor(item.getValorLiquido()));
			textBottonEsquerdoDois.setText(" | " + item.getUnidadeVenda().getSiglaUnidadeVenda());
			// Calcula a diferenca de preco entre o preco de tabela e o de venda
			double diferenca = (item.getValorTabela() - item.getValorLiquido());
			
			textBottonDireito.setText("Dife.: " + funcoes.arredondarValor(String.valueOf(diferenca * -1)));

			// Checa se este item foi conferido ou ao menos faturado
			if ((item.getStatusRetorno() != null) && (item.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.ITEM_NAO_CONFERIDO))){
				textDescricao.setPaintFlags(textDescricao.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoEsqueda.setPaintFlags(textAbaixoDescricaoEsqueda.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoDireita.setPaintFlags(textAbaixoDescricaoDireita.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdo.setPaintFlags(textBottonEsquerdo.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdoDois.setPaintFlags(textBottonEsquerdoDois.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonDireito.setPaintFlags(textBottonDireito.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

			}

			if ((item.getStatusRetorno() != null) && ((item.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_FATURADO)) || (item.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.ITEM_CONFERIDO)))){
				textAbaixoDescricaoDireita.setText("Qtd.: " + String.valueOf(item.getQuantidadeFaturada()));
				textBottonEsquerdo.setText("Tab.: " + funcoes.arredondarValor(item.getValorTabelaFaturado()) +
				  						   " - Ve.: " + funcoes.arredondarValor(item.getValorLiquidoFaturado()));
				// Calcula a diferenca de preco entre o preco de tabela e o de venda
				textBottonDireito.setText("Dife.: " + funcoes.arredondarValor(String.valueOf((item.getValorTabelaFaturado() - item.getValorLiquidoFaturado()) * -1)));
			}

			// Muda a cor da View de acordo com o resultado da diferenca
			if(diferenca < 0){
				//viewTopo.setVisibility(View.VISIBLE);
				//viewTopo.setBackgroundColor(context.getResources().getColor(R.color.verde_escuro));
				
				viewRodape.setVisibility(View.VISIBLE);
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.verde_escuro));
			// Caso o valor vendido seja maior que o valor da tabela
			} else if(diferenca > 0){
				//viewTopo.setVisibility(View.VISIBLE);
				//viewTopo.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
				
				viewRodape.setVisibility(View.VISIBLE);
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
			
			} else {
				viewRodape.setVisibility(View.INVISIBLE);
			}
			viewTopo.setVisibility(View.INVISIBLE);
			
			
		} else if(this.tipoItem == TIPO_DOCUMENTO){
			/**
			 * Recupera dentro da lista de tipos de documentos, um documento especifico de acordo com a
			 * posicao passada no parametro do getView
			 */
			TipoDocumentoBeans tipoDocumento = listaTipoDocumento.get(position);
			
			textDescricao.setText(tipoDocumento.getDescricaoTipoDocumento());
			textAbaixoDescricaoEsqueda.setText(""+tipoDocumento.getCodigoTipoDocumento());
			textAbaixoDescricaoDireita.setText(tipoDocumento.getSiglaTipoDocumento());
			
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
		
		} else if(this.tipoItem == PLANO_PAGAMENTO){
			/**
			 * Recupera dentro da lista de plano de pagamento apenas um plano de acordo com a
			 * posicao passada no parametro do getView.
			 */
			PlanoPagamentoBeans planoPagamento = listaPlanoPagamento.get(position);
			
			textDescricao.setText(planoPagamento.getDescricaoPlanoPagamento());
			textAbaixoDescricaoEsqueda.setText("" + planoPagamento.getCodigoPlanoPagamento());
			
			if(planoPagamento.getVista_prazo() == '0'){
				textAbaixoDescricaoDireita.setText("A Vista");
			} else if(planoPagamento.getVista_prazo() == '1'){
				textAbaixoDescricaoDireita.setText("A Prazo");
			} else {
				textAbaixoDescricaoDireita.setText("");
			}
			
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			
		} else if(this.tipoItem == PLANO_PAGAMENTO_ORCAMENTO){
			
			PlanoPagamentoBeans planoPagamento = listaPlanoPagamento.get(position);
			
			textDescricao.setText(funcoes.arredondarValor(planoPagamento.getPrecoProduto()));
			textDescricao.setTextAppearance(context, R.style.textoGrante);
			textDescricao.setTypeface(null, Typeface.BOLD);
			textAbaixoDescricaoEsqueda.setText(planoPagamento.getDescricaoPlanoPagamento());
			
			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			
		} else if(this.tipoItem == LISTA_ORCAMENTO_PEDIDO){
			/**
			 * Recupera dentro da lista de orcamento/pedido apenas um orcamento.
			 */
			OrcamentoBeans orcamento = listaOrcamentoPedido.get(position);
			
			textDescricao.setText(orcamento.getNomeRazao()); 
			textAbaixoDescricaoEsqueda.setText("Nº " +  orcamento.getIdOrcamento());
			textAbaixoDescricaoDireita.setText(orcamento.getSiglaEstado() + " - " + orcamento.getCidade());
			// Checa se o pedido ja teve retorno de faturado da empresa
			if ((orcamento.getStatusRetorno() != null) && ((orcamento.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_RETORNADO_LIBERADO)) ||
					(orcamento.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_FATURADO)) )){
				// Mostra o valor faturado de retorno
				textBottonDireito.setText(funcoes.arredondarValor(orcamento.getTotalOrcamentoFaturado()));

				textDescricao.setPaintFlags(textDescricao.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
				textAbaixoDescricaoEsqueda.setPaintFlags(textAbaixoDescricaoEsqueda.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
				textAbaixoDescricaoDireita.setPaintFlags(textAbaixoDescricaoDireita.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
				textBottonEsquerdo.setPaintFlags(textBottonEsquerdo.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
				textBottonEsquerdoDois.setPaintFlags(textBottonEsquerdoDois.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
				textBottonDireito.setPaintFlags(textBottonDireito.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

			} else {
				textBottonDireito.setText(funcoes.arredondarValor(orcamento.getTotalOrcamento()));
			}

			if(orcamento.getTipoVenda() == '0'){
				textBottonEsquerdo.setText("A");
				
			}else if(orcamento.getTipoVenda() == '1'){
				textBottonEsquerdo.setText("V");
			}
			textBottonEsquerdoDois.setVisibility(View.VISIBLE);
			textBottonEsquerdoDois.setText(" | " + orcamento.getDataCadastro());

			if ((orcamento.getStatusRetorno() != null) &&
					((orcamento.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_RETORNADO_EXCLUIDO)) ||
							(orcamento.getStatusRetorno().equalsIgnoreCase(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_RETORNADO_BLOQUEADO)))){
				// Rista no meio de todas as descricoes
				textDescricao.setPaintFlags(textDescricao.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoEsqueda.setPaintFlags(textAbaixoDescricaoEsqueda.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoDireita.setPaintFlags(textAbaixoDescricaoDireita.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdo.setPaintFlags(textBottonEsquerdo.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdoDois.setPaintFlags(textBottonEsquerdoDois.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonDireito.setPaintFlags(textBottonDireito.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
			}
			
			viewRodape.setVisibility(View.INVISIBLE);
			viewTopo.setVisibility(View.INVISIBLE);
			
			if((orcamento.getStatusRetorno() != null) && (orcamento.getStatusRetorno().equals(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_RETORNADO_BLOQUEADO))){
				viewTopo.setVisibility(View.VISIBLE);
				viewTopo.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
				
			} else if((orcamento.getStatusRetorno() != null) && (orcamento.getStatusRetorno().equals(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_RETORNADO_LIBERADO))){
				viewTopo.setVisibility(View.VISIBLE);
				viewTopo.setBackgroundColor(context.getResources().getColor(R.color.verde_escuro));
				
			} else if((orcamento.getStatusRetorno() != null) && (orcamento.getStatusRetorno().equals(ListaOrcamentoPedidoMDActivity.TIPO_PEDIDO_RETORNADO_EXCLUIDO))){
				textDescricao.setPaintFlags(textDescricao.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoEsqueda.setPaintFlags(textAbaixoDescricaoEsqueda.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				textAbaixoDescricaoDireita.setPaintFlags(textAbaixoDescricaoDireita.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonDireito.setPaintFlags(textBottonDireito.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdo.setPaintFlags(textBottonEsquerdo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				textBottonEsquerdoDois.setPaintFlags(textBottonEsquerdoDois.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}
			
			
			// Checa se eh um pedido normal sem ter sido enviado para o servidor FTP
			if(orcamento.getStatus().equalsIgnoreCase("P")){
				textDescricao.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				
			// Checa se eh um pedido enviado para o servidor FTP
			} else if(orcamento.getStatus().equalsIgnoreCase("N")){
				textDescricao.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.verde_escuro));
			
			// Padrao
			} else {
				textDescricao.setTextColor(context.getResources().getColor(R.color.preto));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.preto));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.preto));
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.preto));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.preto));
				textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.preto));
			}
			
			// Checa se foi selecionado o orcamento
			if(orcamento.isTagSelectContext()){
				view.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
				textDescricao.setTypeface(null, Typeface.BOLD);
				textAbaixoDescricaoEsqueda.setTypeface(null, Typeface.BOLD);
				textAbaixoDescricaoDireita.setTypeface(null, Typeface.BOLD);
				textBottonDireito.setTypeface(null, Typeface.BOLD);
				textBottonEsquerdo.setTypeface(null, Typeface.BOLD);
				textBottonEsquerdoDois.setTypeface(null, Typeface.BOLD);
				
			} else {
				view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
				textDescricao.setTypeface(null, Typeface.NORMAL);
				textAbaixoDescricaoEsqueda.setTypeface(null, Typeface.NORMAL);
				textAbaixoDescricaoDireita.setTypeface(null, Typeface.NORMAL);
				textBottonDireito.setTypeface(null, Typeface.NORMAL);
				textBottonEsquerdo.setTypeface(null, Typeface.NORMAL);
				textBottonEsquerdoDois.setTypeface(null, Typeface.NORMAL);
			}
			
		} else if(this.tipoItem == RATEIO_ORCAMENTO){
			/**
			 * Recupera dentro da lista orcamento, um orcamento especifico de acordo com a
			 * posicao passada no parametro do getView
			 */
			OrcamentoBeans orcamento = listaOrcamentoPedido.get(position);
			
			textDescricao.setText(orcamento.getNomeRazao());
			textAbaixoDescricaoEsqueda.setText("Nº " +  orcamento.getIdOrcamento());
			textAbaixoDescricaoDireita.setText(orcamento.getSiglaEstado() + " - " + orcamento.getCidade());

			// Instancia a classe de funcoes universal
			funcoes = new FuncoesPersonalizadas(context);

			// Instancia a classe para manipular dados de orcamento
			OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(context);

			double totalTabela = 0,
				   totalLiquido = 0;
			// Checa se o pedido ja foi faturado e retornado
			if (orcamento.getTotalOrcamentoFaturado() > 0){
				totalLiquido = orcamento.getTotalOrcamentoFaturado();

			} else {
				totalTabela = orcamento.getTotalTabela(); //Double.parseDouble(orcamentoRotinas.totalOrcamentoBruto(String.valueOf(orcamento.getIdOrcamento())).replace(".", "").replace(",", "")) / 1000,
			}
			// Checa se o pedido ja foi faturado e retornado
			if (orcamento.getTotalTabelaFaturado() > 0){
				totalTabela = orcamento.getTotalTabelaFaturado();

			} else{
				totalLiquido = orcamento.getTotalOrcamento(); //Double.parseDouble(orcamentoRotinas.totalOrcamentoLiquido(String.valueOf(orcamento.getIdOrcamento())).replace(".", "").replace(",", "")) / 1000;
			}

			textBottonEsquerdo.setText("Tab.: " + funcoes.arredondarValor(totalTabela) +
									   " - Ven.: " + funcoes.arredondarValor(totalLiquido));
			
			textBottonEsquerdoDois.setVisibility(View.INVISIBLE);
			
			// Calcula a diferenca de preco entre o preco de tabela e o de venda
			double diferenca = (totalTabela - totalLiquido);
			

			
			textBottonDireito.setText("Dif.: " + funcoes.arredondarValor(String.valueOf(diferenca * -1)));
			// Muda a cor da View de acordo com o resultado da diferenca
			if(diferenca < 0){
				
				viewRodape.setVisibility(View.VISIBLE);
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.verde_escuro));
			// Caso o valor vendido seja maior que o valor da tabela
			} else if(diferenca > 0){
				//viewTopo.setVisibility(View.VISIBLE);
				//viewTopo.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
				
				viewRodape.setVisibility(View.VISIBLE);
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
			
			} else {
				viewRodape.setVisibility(View.INVISIBLE);
			}
			
			viewTopo.setVisibility(View.INVISIBLE);
			
			// Checa se eh um pedido normal sem ter sido enviado para o servidor FTP
			if(orcamento.getStatus().equalsIgnoreCase("P")){
				textDescricao.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.lilas_escuro));
				
			// Checa se eh um pedido enviado para o servidor FTP
			} else if(orcamento.getStatus().equalsIgnoreCase("N")){
				textDescricao.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.verde_escuro));
			
			// Padrao
			} else {
				textDescricao.setTextColor(context.getResources().getColor(R.color.preto));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.preto));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.preto));
				textBottonDireito.setTextColor(context.getResources().getColor(R.color.preto));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.preto));
				textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.preto));
			}
		
		} else if(this.tipoItem == RAMO_ATIVIDADE){
			/**
			 * Recupera dentro da lista de plano de pagamento apenas um plano de acordo com a
			 * posicao passada no parametro do getView.
			 */
			RamoAtividadeBeans ramoAtividade = listaRamoAtividade.get(position);
			
			textDescricao.setText(ramoAtividade.getCodigo() + " - " + ramoAtividade.getDescricaoRamoAtividade());
			//textAbaixoDescricaoEsqueda.setText("" + ramoAtividade.getCodigo());
			
			//textAbaixoDescricaoEsqueda.setText("D.A.V.: " + ramoAtividade.getDescontoAtacadoVista() + " - D.A.P.: " + ramoAtividade.getDescontoAtacadoPrazo());
			//textAbaixoDescricaoDireita.setText("D.V.V.: " + ramoAtividade.getDescontoVarejoVista() + " - D.V.P.: " + ramoAtividade.getDescontoVarejoPrazo());

			textAbaixoDescricaoEsqueda.setVisibility(View.INVISIBLE);
			textAbaixoDescricaoDireita.setVisibility(View.INVISIBLE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			
		} else if(this.tipoItem == TIPO_CLIENTE){
			/**
			 * Recupera dentro da lista de plano de pagamento apenas um plano de acordo com a
			 * posicao passada no parametro do getView.
			 */
			TipoClienteBeans tipoCLiente = listaTipoCliente.get(position);
						
			textDescricao.setText(tipoCLiente.getCodigoTipoCliente() + " - " + tipoCLiente.getDescricaoTipoCliente());
			//textAbaixoDescricaoEsqueda.setText("" + ramoAtividade.getCodigo());
			
			//textAbaixoDescricaoEsqueda.setText("D.A.V.: " + tipoCLiente.getDescontoAtacadoVista() + " - D.A.P.: " + tipoCLiente.getDescontoAtacadoPrazo());
			//textAbaixoDescricaoDireita.setText("D.V.V.: " + tipoCLiente.getDescontoVarejoVista() + " - D.V.P.: " + tipoCLiente.getDescontoVarejoPrazo());
			
			if(tipoCLiente.getVendeAtacadoVarejo() == '0'){
				textBottonEsquerdo.setText("Atacado");
				
			}else if(tipoCLiente.getVendeAtacadoVarejo() == '1'){
				textBottonEsquerdo.setText("Varejo");
			
			} else if(tipoCLiente.getVendeAtacadoVarejo() == '2'){
				textBottonEsquerdo.setText("Atacado e Varejo");
			}

			textAbaixoDescricaoEsqueda.setVisibility(View.INVISIBLE);
			textAbaixoDescricaoDireita.setVisibility(View.INVISIBLE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
		
		} else if(this.tipoItem == PORTADOR_BANCO){
			/**
			 * 
			 */
			PortadorBancoBeans portadorBanco = listaPortadorBanco.get(position);
						
			textDescricao.setText(portadorBanco.getCodigoPortadorBanco() + " - " + portadorBanco.getDescricaoPortador());
			//textAbaixoDescricaoEsqueda.setText("" + ramoAtividade.getCodigo());
			
			textAbaixoDescricaoEsqueda.setText("Sigla: " + portadorBanco.getSiglaPortador());
			
			if(portadorBanco.getTipo() == '0'){
				textAbaixoDescricaoDireita.setText("Banco");
			
			} else if(portadorBanco.getTipo() == '1'){
				textAbaixoDescricaoDireita.setText("Carteira");
			}
						
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
		
		} else if(this.tipoItem == ESTOQUE){
			EstoqueBeans estoque = listaEstoque.get(position);
			
			
			textDescricao.setText("Estoque: " + funcoes.arredondarValor(estoque.getEstoqueLocacao()));
			textAbaixoDescricaoEsqueda.setText("Retido: " + funcoes.arredondarValor(estoque.getRetidoLocacao()));
			//textAbaixoDescricaoDireita.setText("Código: " + estoque.getIdEstoque());
			// Checa se tem estoque positivo
			if(estoque.getEstoqueLocacao() <= 0){
				viewTopo.setBackgroundColor(context.getResources().getColor(R.color.vermelho));
			}else {
				viewTopo.setVisibility(View.INVISIBLE);
			}
			
			// Deixa o restante dos campos invisiveis e sem ocupar o espaco do layout
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			textAbaixoDescricaoDireita.setVisibility(View.GONE);
		
		} else if(this.tipoItem == DETALHES_PRODUTOS){
			
			DescricaoDublaBeans detalhes = new DescricaoDublaBeans();
			detalhes = listaDetalhesProduto.get(position);
			
			textDescricao.setText(detalhes.getTextoPrincipal());
			textDescricao.setTypeface(null, Typeface.BOLD);
			textAbaixoDescricaoEsqueda.setText(detalhes.getTextoSecundario());
			
			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
		
		} else if(this.tipoItem == EMBALAGEM){
			
			EmbalagemBeans embalagem = listaEmbalagem.get(position);
			
			textDescricao.setText("Embalagem: " + embalagem.getUnidadeVendaEmbalagem().getSiglaUnidadeVenda());
			textAbaixoDescricaoEsqueda.setText("Desc.: " + embalagem.getDescricaoEmbalagem());
			
			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			
		} else if(this.tipoItem == TABELA_LOG){
			LogBeans log = listaTabelaLog.get(position);
			
			if(log.getTabela().equalsIgnoreCase("AEAORCAM")){
				textDescricao.setText("Orçamento/Pedido");
			
			} else if(log.getTabela().equalsIgnoreCase("AEAITORC")){
				textDescricao.setText("Produtos Orçamento/Pedido");
			} else {
				textDescricao.setText("Origem Desconhecida");
			}
			
			
			textAbaixoDescricaoEsqueda.setVisibility(View.GONE);
			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
		
		} else if(this.tipoItem == LOG){
			
			LogBeans log = listaLog.get(position);
			
			textDescricao.setText(log.getValores());
			textAbaixoDescricaoEsqueda.setText(log.getUsuario());
			
			if(log.getOperacao().equalsIgnoreCase("I")){
				textAbaixoDescricaoDireita.setText("Novo");
				
				textDescricao.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.verde_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.verde_escuro));
			
			} else if(log.getOperacao().equalsIgnoreCase("U")){
				textAbaixoDescricaoDireita.setText("Atualiza��o");
				
				textDescricao.setTextColor(context.getResources().getColor(R.color.azul_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.azul_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.azul_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.azul_escuro));
			
			} else if(log.getOperacao().equalsIgnoreCase("D")){
				textAbaixoDescricaoDireita.setText("Deletado");

				textDescricao.setTextColor(context.getResources().getColor(R.color.vermelho_escuro));
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.vermelho_escuro));
				textAbaixoDescricaoDireita.setTextColor(context.getResources().getColor(R.color.vermelho_escuro));
				textBottonEsquerdo.setTextColor(context.getResources().getColor(R.color.vermelho_escuro));
				
			} else {
				textAbaixoDescricaoDireita.setText("");
			}
			
			textBottonEsquerdo.setText(funcoes.formataDataHora(log.getDataCadastro()));
			
			
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			
		} else if(this.tipoItem == ESTADO){
			EstadoBeans estado = listaEstado.get(position);
			
			textDescricao.setText(estado.getSiglaEstado() + " - " + estado.getDescricaoEstado());
			
			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textAbaixoDescricaoEsqueda.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.INVISIBLE);
			viewTopo.setVisibility(View.INVISIBLE);
			
		} else if(this.tipoItem == CIDADE){
				CidadeBeans cidade = listaCidade.get(position);

				textDescricao.setText(cidade.getDescricao());

				if ((cidade.getEstado() != null) && (cidade.getEstado().getSiglaEstado() != null)){
					textAbaixoDescricaoDireita.setText(cidade.getEstado().getSiglaEstado());

				} else if (cidade.getIdCidade() >= 0){
					textAbaixoDescricaoDireita.setText("Cod.: " + cidade.getIdCidade());

				} else {
					textAbaixoDescricaoDireita.setVisibility(View.GONE);
				}

				textAbaixoDescricaoEsqueda.setVisibility(View.GONE);
				textBottonDireito.setVisibility(View.GONE);
				textBottonEsquerdo.setVisibility(View.GONE);
				textBottonEsquerdoDois.setVisibility(View.GONE);
				viewRodape.setVisibility(View.INVISIBLE);
				viewTopo.setVisibility(View.INVISIBLE);

		} else if(this.tipoItem == CIDADE_DARK){
			CidadeBeans cidade = listaCidade.get(position);

			textDescricao.setText(cidade.getDescricao());
			textDescricao.setTextColor(context.getResources().getColor(R.color.branco));

			//view.setBackgroundColor(context.getResources().getColor(R.color.preto));

			if ((cidade.getEstado() != null) && (cidade.getEstado().getSiglaEstado() != null)){
				textAbaixoDescricaoEsqueda.setText(cidade.getEstado().getSiglaEstado());
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.branco));

			} else if (cidade.getIdCidade() >= 0){
				textAbaixoDescricaoEsqueda.setText("Cod.: " + cidade.getIdCidade());
				textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.branco));

			} else {
				textAbaixoDescricaoEsqueda.setVisibility(View.GONE);
			}

			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.INVISIBLE);
			viewTopo.setVisibility(View.INVISIBLE);

		} else if(this.tipoItem == TELEFONE){
			TelefoneBeans telefone = listaTelefone.get(position);

			textDescricao.setText("(" + telefone.getDdd() + ") " + telefone.getTelefone());

			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textAbaixoDescricaoEsqueda.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			viewRodape.setVisibility(View.INVISIBLE);
			viewTopo.setVisibility(View.INVISIBLE);

		} else if (this.tipoItem == STATUS) {
			StatusBeans status = listaStatus.get(position);

			textDescricao.setText(status.getDescricao());
			textAbaixoDescricaoEsqueda.setText(""+status.getCodigo());
			textAbaixoDescricaoDireita.setVisibility(View.INVISIBLE);
			textBottonEsquerdo.setText(status.getMensagem());
			textBottonEsquerdoDois.setVisibility(View.INVISIBLE);
			textBottonDireito.setVisibility(View.INVISIBLE);
			viewRodape.setVisibility(View.INVISIBLE);
			viewTopo.setVisibility(View.INVISIBLE);

		} else if (this.tipoItem == AREA) {
			AreaBeans area = listaArea.get(position);

			textDescricao.setText(area.getDescricaoArea());
			textAbaixoDescricaoEsqueda.setText("Código: "+area.getCodigo());

			textAbaixoDescricaoDireita.setVisibility(View.GONE);
			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);

		} else if (this.tipoItem == CLIENTE) {
			PessoaBeans pessoa = listaPessoa.get(position);

			textDescricao.setText(pessoa.getCodigoCliente() + " - " + pessoa.getNomeRazao());
			textDescricao.setTextColor(context.getResources().getColor(R.color.verde));
			textAbaixoDescricaoEsqueda.setText(pessoa.getNomeFantasia());
			textAbaixoDescricaoEsqueda.setTextColor(context.getResources().getColor(R.color.azul_medio_200));

			textAbaixoDescricaoDireita.setVisibility(View.GONE);

			textBottonEsquerdo.setText(pessoa.getCidadePessoa().getDescricao() + " - ");
			textBottonEsquerdoDois.setText(pessoa.getEnderecoPessoa().getBairro());
			textBottonDireito.setText(pessoa.getCpfCnpj());

			// Verifica se o campo bloqueia eh NAO(0) e  o campo PARCELA EM ABERTO eh VENDE(1)
			if((pessoa.getStatusPessoa().getBloqueia() == "0" ) && (pessoa.getStatusPessoa().getParcelaEmAberto() == "1")){
				// Muda a cor da View
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.verde_escuro));

				// Verifica se o campo bloqueia eh SIM(1) e  o campo PARCELA EM ABERTO eh diferente de VENDE(1)
			} else if((pessoa.getStatusPessoa().getBloqueia() == "1") && (pessoa.getStatusPessoa().getParcelaEmAberto() != "1")){
				// Muda a cor da View para vermelho
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
				//textStatus.setTypeface(null, Typeface.BOLD_ITALIC);

			} else {
				// Muda a cor da View
				viewRodape.setBackgroundColor(context.getResources().getColor(R.color.amarelo));
				//textStatus.setTypeface(null, Typeface.BOLD);
			}

			textBottonEsquerdo.setVisibility(View.GONE);
			textBottonEsquerdoDois.setVisibility(View.GONE);
			textBottonDireito.setVisibility(View.GONE);
			viewRodape.setVisibility(View.GONE);
			viewTopo.setVisibility(View.GONE);
		}
		// Pega a posicao atual
		final int posicao = position;
		
		// Checa se quem esta chamando este adapter eh a lista de produtos
		if(this.tipoItem == PRODUTO){
		
			imageOpcao.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Checa se eh uma lista de produtos
					if (tipoItem == PRODUTO) {
						// Mostra um menu popup
						showPopup(v, posicao);
					}
				}
			});
		}
		

		return view;
		
	} // Fim getView
	
	
	public void showPopup(View v, final int posicao) {
	    PopupMenu popup = new PopupMenu(context, v);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.produto_lista_context, popup.getMenu());
	    popup.show();
	    
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				
				switch (item.getItemId()) {
				
				case R.id.menu_produto_lista_context_detalhes_produto:
					
					long idProduto = getItemId(posicao);
					
					Intent intent = new Intent(context, ProdutoDetalhesMDActivity.class);
					//intent.putExtra(ProdutoDetalhesActivity.KEY_ID_PRODUTO, String.valueOf(idProduto));
					intent.putExtra(ProdutoDetalhesMDActivity.KEY_ID_PRODUTO, String.valueOf(idProduto));

					context.startActivity(intent);
					break;

				default:
					break;
				}
				
				
				return true;
			}
		});
	}
	
	
	
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int p = position;
		View view2 = view;
		
	}
	
	public void remove(int posicao) {
		// Checa se o tipo do adapter eh item de orcamento
		if( (this.tipoItem == ITEM_ORCAMENTO) || (this.tipoItem == RATEIO_ITEM_ORCAMENTO)){
			listaItemOrcamento.remove(posicao);
			
		// Checa se o tipo de adapter eh de lista de produto	
		} else if(this.tipoItem == PRODUTO){
			listaProduto.remove(posicao);
		}
	}
	

	@SuppressLint("DefaultLocale")
	@Override
	public Filter getFilter() {
		Filter filtro = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				
				FilterResults filtroResultado = new FilterResults();
				
				if (constraint == null || constraint.length() == 0) {
					// Verifica se o filtro pertence ao item do orcamento
					if(tipoItem == ITEM_ORCAMENTO){
						// Se nao tiver nada para filtrar entao etorna a lista completa
						filtroResultado.values = listaItemOrcamento;
						filtroResultado.count = listaItemOrcamento.size();
					}
					// Verifica se o filtro pertence a lista de produtos
					if(tipoItem == PRODUTO){
						// Se nao tiver nada para filtrar entao etorna a lista completa
						filtroResultado.values = listaProduto;
						filtroResultado.count = listaProduto.size();
					}
					
					return filtroResultado;
					
				} else{
					
					if(tipoItem == ITEM_ORCAMENTO){
						// Cria uma lista auxiliar para armazenar os itens filtrados
						List<ItemOrcamentoBeans> auxItemOrcamento = new ArrayList<ItemOrcamentoBeans>();
						
						for (ItemOrcamentoBeans p : listaItemOrcamento){
							if(p.getProduto().getDescricaoProduto().toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   p.getProduto().getCodigoEstrutural().toUpperCase().contains(constraint.toString().toUpperCase()) || 
							   String.valueOf(p.getQuantidade()).toUpperCase().contains(constraint.toString().toUpperCase()) || 
							   String.valueOf(p.getUnidadeVenda()).toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   p.getUnidadeVenda().getSiglaUnidadeVenda().toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   String.valueOf(p.getValorLiquido()).toUpperCase().contains(constraint.toString().toUpperCase())){
								//Adiciona a pessoa em uma nova lista
								auxItemOrcamento.add(p);
							}
						} //Fim do for
						
						filtroResultado.values = auxItemOrcamento;
						filtroResultado.count = auxItemOrcamento.size();
					}
					
					// Verifica se o filtro pertence a lista de produtos
					if(tipoItem == PRODUTO){
						// Cria uma lista auxiliar para armazenar os itens filtrados
						List<ProdutoListaBeans> auxIListaProduto = new ArrayList<ProdutoListaBeans>();
						
						for (ProdutoListaBeans p : listaProduto){
							if(p.getProduto().getDescricaoProduto().toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   p.getProduto().getCodigoEstrutural().toUpperCase().contains(constraint.toString().toUpperCase()) || 
							   p.getProduto().getReferencia().toUpperCase().contains(constraint.toString().toUpperCase()) || 
							   String.valueOf(p.getValorUnitarioAtacado()).toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   String.valueOf(p.getValorUnitarioVarejo()).toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   p.getProduto().getDescricaoMarca().toUpperCase().contains(constraint.toString().toUpperCase()) ||
							   p.getProduto().getUnidadeVendaProduto().getSiglaUnidadeVenda().toUpperCase().contains(constraint.toString().toUpperCase())){
								//Adiciona a pessoa em uma nova lista
								auxIListaProduto.add(p);
							}
						} //Fim do for
						
						filtroResultado.values = auxIListaProduto;
						filtroResultado.count = auxIListaProduto.size();
					}
					
			    }
				
				return filtroResultado;
			} //FIm do performFiltering
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults resultadoFiltro) {
				
				//Temos que informar a nova lista
				if (resultadoFiltro.count == 0)
			        
					//Notifica os ouvintes
					notifyDataSetInvalidated();
			    
				else {
					// Verifica o tipo de item universal que esta sendo chamado
					if(tipoItem == ITEM_ORCAMENTO){
						// Preencho a lista(listaPessoas) do adapter com o novo valor
						listaItemOrcamento = (List<ItemOrcamentoBeans>) resultadoFiltro.values;
					}
					
					// Verifica o tipo de item universal que esta sendo chamado
					if(tipoItem == PRODUTO){
						// Preencho a lista(listaPessoas) do adapter com o novo valor
						listaProduto = (List<ProdutoListaBeans>) resultadoFiltro.values;
					}
			        
			        //Notifica ovites apos a lista ter novos valores
			        notifyDataSetChanged();
			    }
				
			} //Fim do publishResults
			
			
		}; 
		notifyDataSetChanged();
		return filtro;
	} // Fim getFilter


}

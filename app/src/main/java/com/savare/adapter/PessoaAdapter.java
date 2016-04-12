package com.savare.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.savare.R;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;

public class PessoaAdapter extends BaseAdapter implements Filterable, OnItemClickListener {
	
	private Context context;
	private List<PessoaBeans> listaPessoas;
	private int tipoPessoa;
	public static final int KEY_CLIENTE = 0, KEY_FORNECEDOR = 1, KEY_USUARIO = 2, KEY_REPRESENTANTE = 3, KEY_CONCORRENTE = 4, KEY_TRANSPORTADORA = 5;
	//private final List<Integer> pessoasSelecionadas = new ArrayList<Integer>(); // Para armazenar os itens selecionados

	/**
	 * Construtor padrao.
	 * @param context
	 * @param listaPessoas
	 * @param tipoPessoa - 0 = CLIENTE, 1 = FORNECEDOR, 2 = USUARIO, 3 = REPRESENTANTE, 4 = CONCORRENTE, 5 = TRANSPORTADORA
	 */
	public PessoaAdapter(Context context, List<PessoaBeans> listaPessoas, int tipoPessoa) {
		this.context = context;
		this.listaPessoas = listaPessoas;
		this.tipoPessoa = tipoPessoa;
	}
	
		
	/**
	 * @return the listaPessoas
	 */
	public List<PessoaBeans> getListaPessoas() {
		return listaPessoas;
	}

	/**
	 * @param listaPessoas the listaPessoas to set
	 */
	public void setListaPessoas(List<PessoaBeans> listaPessoas) {
		this.listaPessoas = listaPessoas;
	}

	/**
	 * Retorna a quantidade de pessoas contidos na lista.
	 * Importante retornar o tamanho do registro a ser exibido, 
	 * pois esse metodo eh chamado pelo {@link ListView} para que o mesmo saiba quantos registro tem dentro do adaptador
	 */
	@Override
	public int getCount() {
		return this.listaPessoas.size();
	}

	@Override
	public Object getItem(int posicao) {
		return this.listaPessoas.get(posicao);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int posicao, View convertView, ViewGroup parent) {
		
		View view = convertView;
		
		//if (view == null) {
			/*
			 * Recupera o servico LayoutInflater que eh o servidor que ira
			 * transformar o nosso layout layout_pessoa em uma View
			 */
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			/*
			 * Converte nosso layout em uma view
			 */
			view = inflater.inflate(R.layout.layout_pessoa, null);
		//}
		
		/**
		 * Recupero os compoentes que est�o dentro do lista_item_pessoa
		 */
		TextView textRazao = (TextView) view.findViewById(R.id.layout_pessoa_text_razao_social);
		TextView textFantasia = (TextView) view.findViewById(R.id.layout_pessoa_text_fantasia);
		TextView textCidade = (TextView) view.findViewById(R.id.layout_pessoa_text_cidade);
		TextView textStatus = (TextView) view.findViewById(R.id.layout_pessoa_text_status);
		TextView textUltimaVenda = (TextView) view.findViewById(R.id.layout_pessoa_text_ultima_venda);
		TextView textNumero = (TextView) view.findViewById(R.id.layout_pessoa_text_numero_qualquer);
		View viewStatus = (View) view.findViewById(R.id.layout_pessoa_view_status);
		ImageView imagePessoa = (ImageView) view.findViewById(R.id.layout_pessoa_image_foto);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		// Checa se o tamanho da fonte eh grante
		if(funcoes.getValorXml("TamanhoFonte").equalsIgnoreCase("G")){
			textRazao.setTextAppearance(context, R.style.textoGrante);
			textFantasia.setTextAppearance(context, R.style.textoGrante);
			textCidade.setTextAppearance(context, R.style.textoGrante);
			textStatus.setTextAppearance(context, R.style.textoGrante);
			textUltimaVenda.setTextAppearance(context, R.style.textoGrante);
			textNumero.setTextAppearance(context, R.style.textoGrante);
		
		} else if(funcoes.getValorXml("TamanhoFonte").equalsIgnoreCase("M")){
			textRazao.setTextAppearance(context, R.style.textoMendio);
			textFantasia.setTextAppearance(context, R.style.textoMendio);
			textCidade.setTextAppearance(context, R.style.textoMendio);
			textStatus.setTextAppearance(context, R.style.textoMendio);
			textUltimaVenda.setTextAppearance(context, R.style.textoMendio);
			textNumero.setTextAppearance(context, R.style.textoMendio);
		}

		if (!funcoes.getValorXml("ImagemPessoa").equalsIgnoreCase("S")){
			imagePessoa.setVisibility(View.GONE);
		}
		/**
		 * Recupera dentro da lista de pessoas, uma pessoa(nome) especifica de acordo com a
		 * posicao passada no parametro do getView
		 */
		PessoaBeans pessoa = listaPessoas.get(posicao);
		
		// Faz verificacao para saber qual tipo de pessoa eh pra retornar
		// 0 = CLIENTE, 1 = FORNECEDOR, 2 = USUARIO, 3 = REPRESENTANTE, 4 = CONCORRENTE, 5 = TRANSPORTADORA
		// Verifica se eh cliente  
		if(this.tipoPessoa == 0){
			textRazao.setText(pessoa.getCodigoCliente() + " - " + pessoa.getNomeRazao());
			
			// Verifica se eh fornecedor
		} else if(this.tipoPessoa == 2){
			textRazao.setText(pessoa.getCodigoUsuario() + " - " + pessoa.getNomeRazao());
			
			// Verifica se eh representante
		} else if(this.tipoPessoa == 5){
			textRazao.setText(pessoa.getCodigoTransportadora() + " - " + pessoa.getNomeRazao());
			
		} else {
			textRazao.setText(pessoa.getNomeRazao());
		}
		
		// Seta os campos de acordo com o que esta na lista de pessoa
		textFantasia.setText(pessoa.getNomeFantasia());
		textCidade.setText(pessoa.getCidadePessoa().getDescricao() + " - " + pessoa.getEnderecoPessoa().getBairro());
		textStatus.setText(pessoa.getStatusPessoa().getDescricao());
		textUltimaVenda.setText(pessoa.getDataUltimaCompra());
		textNumero.setText(pessoa.getCpfCnpj());
		
		// Verifica se o campo bloqueia eh NAO(0) e  o campo PARCELA EM ABERTO eh VENDE(1)
		if((pessoa.getStatusPessoa().getBloqueia() == '0' ) && (pessoa.getStatusPessoa().getParcelaEmAberto() == '1')){
			// Muda a cor da View
			viewStatus.setBackgroundColor(context.getResources().getColor(R.color.verde_escuro));
			
		// Verifica se o campo bloqueia eh SIM(1) e  o campo PARCELA EM ABERTO eh diferente de VENDE(1)
		} else if((pessoa.getStatusPessoa().getBloqueia() == '1') && (pessoa.getStatusPessoa().getParcelaEmAberto() != '1')){
			// Muda a cor da View para vermelho
			viewStatus.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
			textStatus.setTypeface(null, Typeface.BOLD_ITALIC);
			
		} else {
			// Muda a cor da View
			viewStatus.setBackgroundColor(context.getResources().getColor(R.color.amarelo));
			textStatus.setTypeface(null, Typeface.BOLD);
		}
		
		// Retorna uma view personalizada
		return view;
	}
	
	

	
	@Override
	public Filter getFilter() {
		Filter filtro = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				
				FilterResults filtroResultado = new FilterResults();
				
				if (constraint == null || constraint.length() == 0) {
			    
					// Se nao tiver nada para filtrar entao etorna a lista completa
					filtroResultado.values = listaPessoas;
					filtroResultado.count = listaPessoas.size();
					return filtroResultado;
				} else{
			    	
					List<PessoaBeans> auxPessoas = new ArrayList<PessoaBeans>();
					
					for (PessoaBeans p : listaPessoas){
						if(p.getNomeRazao().toUpperCase().contains(constraint.toString().toUpperCase()) ||
						   p.getNomeFantasia().toUpperCase().contains(constraint.toString().toUpperCase()) || 
						   p.getCidadePessoa().getDescricao().toUpperCase().contains(constraint.toString().toUpperCase()) || 
						   p.getStatusPessoa().getDescricao().toUpperCase().contains(constraint.toString().toUpperCase()) ||
						   p.getEnderecoPessoa().getBairro().toUpperCase().contains(constraint.toString().toUpperCase()) ||
						   p.getCpfCnpj().toUpperCase().contains(constraint.toString().toUpperCase())){
							//Adiciona a pessoa em uma nova lista
							auxPessoas.add(p);
						}
					} //Fim do for
					
					filtroResultado.values = auxPessoas;
					filtroResultado.count = auxPessoas.size();
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
					//Preencho a lista(listaPessoas) do adapter com o novo valor
			        listaPessoas = (List<PessoaBeans>) resultadoFiltro.values;
			        
			        //Notifica ovites apos a lista ter novos valores
			        notifyDataSetChanged();
			    }
				
			} //Fim do publishResults
			
			
		}; 
		notifyDataSetChanged();
		return filtro;
	} // FIm do getFilter



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	} 
	
	

}

package com.savare.adapter;

import java.util.List;

import com.savare.R;
import com.savare.beans.ParcelaBeans;
import com.savare.beans.PessoaBeans;
import com.savare.beans.StatusBeans;
import com.savare.beans.TitulosListaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.StatusRotinas;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ListaTitulosExpandableAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private List<TitulosListaBeans> listaTitulosParent;
	private LayoutInflater inflater;
	
	public ListaTitulosExpandableAdapter(Context context, List<TitulosListaBeans> listaTitulos) {
		this.context = context;
		this.listaTitulosParent = listaTitulos;
		this.inflater = LayoutInflater.from(context);
	}

	/**
	 * @return the listaPessoasParent
	 */
	public List<TitulosListaBeans> getListaPessoasParent() {
		return listaTitulosParent;
	}

	/**
	 * @param listaTitulosParent the listaPessoasParent to set
	 */
	public void setListaPessoasParent(List<TitulosListaBeans> listaTitulosParent) {
		this.listaTitulosParent = listaTitulosParent;
	}


	@Override
	public int getGroupCount() {
		return this.listaTitulosParent.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.listaTitulosParent.get(groupPosition).getListaParcela().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listaTitulosParent.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return this.listaTitulosParent.get(groupPosition).getListaParcela().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return this.listaTitulosParent.get(groupPosition).getIdPessoa();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return this.listaTitulosParent.get(groupPosition).getListaParcela().get(childPosition).getIdParcela();
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.layout_item_universal, parent, false);
		}
		
		/**
		 * Recupero os compoentes que estao dentro do layout_item_universal
		 */
		TextView textDescricao = (TextView) convertView.findViewById(R.id.layout_item_universal_text_descricao);
		TextView textAbaixoDescricaoEsqueda = (TextView) convertView.findViewById(R.id.layout_item_universal_text_abaixo_descricao_esquerda);
		TextView textAbaixoDescricaoDireita = (TextView) convertView.findViewById(R.id.layout_item_universal_text_abaixo_descricao_direita);
		TextView textBottonEsquerdo = (TextView) convertView.findViewById(R.id.layout_item_universal_text_botton_esquerdo);
		TextView textBottonEsquerdoDois = (TextView) convertView.findViewById(R.id.layout_item_universal_text_botton_esquerdo_dois);
		TextView textBottonDireito = (TextView) convertView.findViewById(R.id.layout_item_universal_text_botton_direito);
		View viewTopo = (View) convertView.findViewById(R.id.layout_item_universal_view_topo);
		View viewRodape = (View) convertView.findViewById(R.id.layout_item_universal_view_rodape);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		textDescricao.setText(listaTitulosParent.get(groupPosition).getNomeRazao());
		textAbaixoDescricaoEsqueda.setText("Vencimento: " + listaTitulosParent.get(groupPosition).getVencimento());
		textAbaixoDescricaoDireita.setText("Vl. Rest. " + funcoes.arredondarValor(listaTitulosParent.get(groupPosition).getValorRestante()));
		textBottonEsquerdo.setText(listaTitulosParent.get(groupPosition).getDocumento() + " / " + listaTitulosParent.get(groupPosition).getPortadorBanco());
		
		textBottonEsquerdoDois.setVisibility(View.GONE);
		textBottonDireito.setVisibility(View.GONE);
		
		if(listaTitulosParent.get(groupPosition).isAtrazado()){
			viewTopo.setVisibility(View.VISIBLE);
			viewTopo.setBackgroundColor(context.getResources().getColor(R.color.vermelho_escuro));
		} else {
			viewTopo.setVisibility(View.INVISIBLE);
		}
		
		viewRodape.setVisibility(View.INVISIBLE);
		
		textAbaixoDescricaoEsqueda.setPadding(25, 0, 0, 0);
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.layout_item_universal, parent, false);
		}
		
		/**
		 * Recupero os compoentes que estao dentro do layout_item_universal
		 */
		TextView textDescricao = (TextView) convertView.findViewById(R.id.layout_item_universal_text_descricao);
		TextView textAbaixoDescricaoEsqueda = (TextView) convertView.findViewById(R.id.layout_item_universal_text_abaixo_descricao_esquerda);
		TextView textAbaixoDescricaoDireita = (TextView) convertView.findViewById(R.id.layout_item_universal_text_abaixo_descricao_direita);
		TextView textBottonEsquerdo = (TextView) convertView.findViewById(R.id.layout_item_universal_text_botton_esquerdo);
		TextView textBottonEsquerdoDois = (TextView) convertView.findViewById(R.id.layout_item_universal_text_botton_esquerdo_dois);
		TextView textBottonDireito = (TextView) convertView.findViewById(R.id.layout_item_universal_text_botton_direito);
		View viewTopo = (View) convertView.findViewById(R.id.layout_item_universal_view_topo);
		View viewRodape = (View) convertView.findViewById(R.id.layout_item_universal_view_rodape);
		
		ParcelaBeans parcela = listaTitulosParent.get(groupPosition).getListaParcela().get(childPosition);
		
		FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
		
		textDescricao.setText("Sequencial: " + parcela.getSequencial());
		textAbaixoDescricaoEsqueda.setText("Emissão: " + parcela.getDataEmissao());
		textAbaixoDescricaoDireita.setText("Vl. Parcela: " + funcoes.arredondarValor(parcela.getValorParcela()));
		textBottonEsquerdo.setText("Parcela: " + parcela.getParcela());
		textBottonDireito.setText("Nosso N.: " +  ((parcela.getNumero() != null) ? parcela.getNumero() : ""));
		
		StatusRotinas statusRotinas = new StatusRotinas(context);
		StatusBeans status = statusRotinas.statusCliente(String.valueOf(listaTitulosParent.get(groupPosition).getIdPessoa()));
		
		textBottonEsquerdoDois.setText(status.getDescricao());
		
		// Verifica se o campo bloqueia eh NAO(0) e  o campo PARCELA EM ABERTO eh VENDE(1)
		if((status.getBloqueia() == "0" ) && (status.getParcelaEmAberto() == "1")){
			// Muda a cor da View
			textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.verde_escuro));
			
		// Verifica se o campo bloqueia eh SIM(1) e  o campo PARCELA EM ABERTO eh diferente de VENDE(1)
		} else if((status.getBloqueia() == "1") && (status.getParcelaEmAberto() != "1")){
			// Muda a cor da View para vermelho
			textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.vermelho_escuro));
			
		} else {
			// Muda a cor da View
			textBottonEsquerdoDois.setTextColor(context.getResources().getColor(R.color.amarelo));
		}
		
		viewTopo.setVisibility(View.INVISIBLE);
		viewRodape.setVisibility(View.INVISIBLE);
		
		return convertView;
	} // Fim getChildView

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		super.registerDataSetObserver(observer);
	}

}

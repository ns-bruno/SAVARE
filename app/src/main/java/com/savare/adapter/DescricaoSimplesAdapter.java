package com.savare.adapter;

import java.util.List;

import com.savare.beans.DescricaoDublaBeans;
import com.savare.beans.DescricaoSimplesBeans;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DescricaoSimplesAdapter extends BaseAdapter {

	private Context context;
	private List<DescricaoSimplesBeans> lista;
	
	public DescricaoSimplesAdapter(Context context, List<DescricaoSimplesBeans> lista) {
		this.context = context;
		this.lista = lista;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lista.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lista.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	/**
	 * @return the lista
	 */
	public List<DescricaoSimplesBeans> getLista() {
		return lista;
	}

	/**
	 * @param lista the lista to set
	 */
	public void setLista(List<DescricaoSimplesBeans> lista) {
		this.lista = lista;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Prepara a view para ser retornada.
        View view = convertView;
        
        if (view == null) {
			
			// Recupera o servico LayoutInflater que eh o servidor que ira
			// transformar o nosso layout item_pessoa em uma View
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			
			// Converte nosso layout em uma view
			view = inflater.inflate(android.R.layout.simple_spinner_item, null);
		}

        //EstadoDao entry = listaEstado.get(position);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        
        DescricaoSimplesBeans descricaoSimples = (DescricaoSimplesBeans) lista.get(position);
        	                
        text1.setText(descricaoSimples.getTextoPrincipal());
        
        return view;
	}

}

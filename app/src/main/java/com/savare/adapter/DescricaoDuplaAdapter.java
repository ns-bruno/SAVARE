package com.savare.adapter;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.savare.beans.DescricaoDublaBeans;

public class DescricaoDuplaAdapter extends BaseAdapter {
	
	private Context context;
	private List<DescricaoDublaBeans> lista;
	
	public DescricaoDuplaAdapter(Context context, List<DescricaoDublaBeans> lista) {
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Prepara a view para ser retornada.
        View view = convertView;
        
        if (view == null) {
			
			// Recupera o servico LayoutInflater que eh o servidor que ira
			// transformar o nosso layout item_pessoa em uma View
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			
			// Converte nosso layout em uma view
			view = inflater.inflate(android.R.layout.simple_list_item_2, null);
		}

        //EstadoDao entry = listaEstado.get(position);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        text1.setGravity(Gravity.RIGHT);
        
        DescricaoDublaBeans descricaoDupla = (DescricaoDublaBeans) lista.get(position);
        	                
        text1.setText(descricaoDupla.getTextoPrincipal());
        text2.setText(descricaoDupla.getTextoSecundario());
        
        return view;
	}

}

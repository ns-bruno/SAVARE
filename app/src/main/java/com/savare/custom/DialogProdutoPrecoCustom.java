package com.savare.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.savare.R;

public class DialogProdutoPrecoCustom extends Dialog {
	
	private int tipoEvento;
	private TextView textUnitarioTabela,
					 textEmbalagem,
					 textQuantidade;
	private EditText editUnitarioVenda,
					 editTotal;
	private Button buttonZero,
				   buttonUm,
				   buttonDois,
				   buttonTres,
				   buttonQuatro,
				   buttonCinco,
				   buttonSeis,
				   buttonSete,
				   buttonOito,
				   buttonNove,
				   buttonVirgula,
				   buttonClean,
				   buttonBackspace,
				   buttonEnter;

	public DialogProdutoPrecoCustom(Context context) {
		super(context);
		recuperaCampos();
	}
	
	public DialogProdutoPrecoCustom(Context context, int tipoEvento){
		super(context);
		this.tipoEvento = tipoEvento;
		recuperaCampos();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		textQuantidade.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				textQuantidade.setSelected(true);
				
			}
		});
	}
	
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.layout_dialog_produto_preco_custom);
	}
	
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle("Formação do Preço");
	}
	
	private void recuperaCampos(){
		textUnitarioTabela = (TextView) findViewById(R.id.layout_dialog_produto_preco_custom_text_unitario_tabela);
		textEmbalagem = (TextView) findViewById(R.id.layout_dialog_produto_preco_custom_text_embalagem);
		textQuantidade = (EditText) findViewById(R.id.layout_dialog_produto_preco_custom_text_quantidade);
		editUnitarioVenda = (EditText) findViewById(R.id.layout_dialog_produto_preco_custom_edit_unitario_Liquido_venda);
		editTotal = (EditText) findViewById(R.id.layout_dialog_produto_preco_custom_edit_total);
		buttonZero = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_zero);
		buttonUm = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_um);
		buttonDois = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_dois);
		buttonTres = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_tres);
		buttonQuatro = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_quatro);
		buttonCinco = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_cinco);
		buttonSeis = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_seis);
		buttonSete = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_sete);
		buttonOito = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_oito);
		buttonNove = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_nove);
		buttonVirgula = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_virgula);
		buttonClean = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_clean);
		buttonBackspace = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_backspace);
		buttonEnter = (Button) findViewById(R.id.layout_dialog_produto_preco_custom_button_ok);
	}

}

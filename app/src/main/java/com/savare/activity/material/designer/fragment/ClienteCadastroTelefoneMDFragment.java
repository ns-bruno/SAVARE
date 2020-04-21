package com.savare.activity.material.designer.fragment;

import java.util.ArrayList;
import java.util.List;

import com.savare.R;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.banco.funcoesSql.TelefoneSql;
import com.savare.beans.TelefoneBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.TelefoneRotinas;

import android.content.ContentValues;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ClienteCadastroTelefoneMDFragment extends Fragment {
	
	private View viewDados;
	private ListView listViewTelefones;
	private ItemUniversalAdapter adapterTelefone;
	private EditText editTelefone,
					 editDdd;
	private Button buttonAdicionar;
	private TextWatcher telefoneMask;
	private String idPessoaTemporario;
	public static final String KEY_ID_PESSOA = "ID_PESSOA";
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewDados = inflater.inflate(R.layout.fragment_cliente_cadastro_telefone_md, container, false);
		
		/**
		 * Pega valores passados por parametro de outra Activity
		 */
		Bundle parametro = getArguments();
		
		if(parametro != null){
			idPessoaTemporario = parametro.getString(KEY_ID_PESSOA);
		}
		
		recuperarCamposTela();
		
		carragaListaTelefones();
		
		// Armazene seus TextWatcher para posterior uso
        telefoneMask = FuncoesPersonalizadas.insertMascara("####-####", editTelefone);
        editTelefone.addTextChangedListener(telefoneMask);
        
        buttonAdicionar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (idPessoaTemporario.length() > 0) {
					ContentValues telefone = new ContentValues();
					telefone.put("ID_CFAFONES", idPessoaTemporario);
					telefone.put("ID_CFACLIFO", idPessoaTemporario);
					telefone.put("DDD", editDdd.getText().toString());
					telefone.put("FONE", editTelefone.getText().toString());

					TelefoneSql telefoneSql = new TelefoneSql(getActivity());
					if (telefoneSql.insertOrReplace(telefone) > 0) {

						carragaListaTelefones();

						editDdd.setText("");
						editTelefone.setText("");
					}
				} else {
					// Armazena as informacoes para para serem exibidas e enviadas
					ContentValues contentValues = new ContentValues();
					contentValues.put("comando", 0);
					contentValues.put("tela", "ClienteCadastroTelefoneMDFragment");
					contentValues.put("mensagem", "Por Favor, primeiro salve os dados do cliente para depois salvar o telefone.");
					contentValues.put("dados", "Por Favor, primeiro salve os dados do cliente para depois salvar o telefone.");
					// Pega os dados do usuario
					FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
					//contentValues.put("usuario", funcoes.getValorXml("Usuario"));
					//contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
					//contentValues.put("email", funcoes.getValorXml("Email"));

					funcoes.menssagem(contentValues);
				}
			}
		});
		
		return viewDados;
	}
	
	
	private void recuperarCamposTela(){
		listViewTelefones = (ListView) viewDados.findViewById(R.id.fragment_cliente_cadastro_telefone_md_listView_lista_telefone);
		editTelefone = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_telefone_md_edit_telefone);
		editDdd = (EditText) viewDados.findViewById(R.id.fragment_cliente_cadastro_telefone_md_edit_ddd);
		buttonAdicionar = (Button) viewDados.findViewById(R.id.fragment_cliente_cadastro_telefone_md_button_adicionar);
	}
	
	
	private void carragaListaTelefones(){
		TelefoneRotinas telefoneRotinas = new TelefoneRotinas(getActivity());
		
		List<TelefoneBeans> listaTelefone = new ArrayList<TelefoneBeans>();
		listaTelefone = telefoneRotinas.listaTelefone("ID_CFACLIFO = " + idPessoaTemporario);
		
		if(listaTelefone != null && listaTelefone.size() > 0){
			adapterTelefone = new ItemUniversalAdapter(getActivity(), ItemUniversalAdapter.TELEFONE);
			
			adapterTelefone.setListaTelefone(listaTelefone);
			listViewTelefones.setAdapter(adapterTelefone);
		}
	}
}

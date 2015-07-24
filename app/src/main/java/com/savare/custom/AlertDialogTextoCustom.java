package com.savare.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.savare.R;

public class AlertDialogTextoCustom extends AlertDialog {

	private EditText editTexto;
	
	/**
	 * @return the editTexto
	 */
	public String getEditTexto() {
		return editTexto.getText().toString();
	}

	/**
	 * @param editTexto the editTexto to set
	 */
	public void setEditTexto(String editTexto) {
		this.editTexto.setText(editTexto);
	}
	
	public AlertDialogTextoCustom(Context context) {
		super(context);
		recuperarCampos();
	}
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*setButton(DialogInterface.BUTTON_POSITIVE, "OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});*/
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		
		
		
	}
	
	@Override
	public void setContentView(View view) {
		super.setContentView(R.layout.layout_dialog_texto);
	}

	
	
	@Override
	public void setTitle(CharSequence title) {
		if(title.equals(null)){
			super.setTitle("Texto Longo");
		}else{
			super.setTitle(title);
		}
	}
	
	@Override
	public void setMessage(CharSequence message) {
		if(message.equals(null)){
			super.setMessage("");
		}else{
			super.setMessage(message);
		}
	}
	
	
	private void recuperarCampos(){
		editTexto = (EditText) findViewById(R.id.layout_dialog_texto_edit_texo);
	}
	
	


}

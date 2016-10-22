package com.savare.funcoes;

import java.util.Random;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import com.savare.banco.funcoesSql.UsuarioSQL;

public class Rotinas {
	
	protected Context context;
	public static final String SIM = "S", NAO = "N";
	
	public Rotinas(Context context) {
		this.context = context;
	}
	
	
	
	public boolean existeUsuario(){
		boolean resultado = false;
		
		UsuarioSQL usuarioSQL = new UsuarioSQL(context);
		Cursor cursor = usuarioSQL.query(null);
		
		if((cursor != null) && (cursor.getCount() > 0)){
			resultado = true;
		}
		
		return resultado;
	} // Fim existeUsuario
	
	
	public boolean checaUsuario(String codigoUsuario, String usuario){
		boolean resultado = false;
		
		UsuarioSQL usuarioSQL = new UsuarioSQL(context);
		Cursor cursor = usuarioSQL.query("id_usua = " + codigoUsuario + " and login_usua = '" + usuario +"' ");
		
		if(cursor.getCount() > 0){
			resultado = true;
		}
		
		return resultado;
	}
	
	/**
	 * Funcao para checar se o usuario e a senha estao corretos.
	 * @param usuario
	 * @param senha
	 * @return - Retorna verdadeiro se existir o usuario e a senha
	 * @throws Exception
	 */
	public boolean checaUsuarioESenha(String codigoUsuario, String usuario, String senha) throws Exception{
		boolean resultado = false;
		
		if(checaUsuario(codigoUsuario, usuario)){
			// Instancia a classe de funcoes para chamar alguma funcao especifica
			FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
			
			// Instancia a classe de UsuarioSQL para manipular os dados no banco de dados
			UsuarioSQL usuarioSQL = new UsuarioSQL(context);
			Cursor cursor = usuarioSQL.query("ID_USUA = " + codigoUsuario);
			cursor.moveToFirst();
			
			String senhaDescrip = funcoes.descriptografaSenha(cursor.getString(cursor.getColumnIndex("SENHA_USUA")));
			
			if(senhaDescrip.equals(senha)){
				resultado = true;
			}
		}
		
		return resultado;
	} // Fim do checaUsuarioESenha
	
	
	/**
	 * Gera uma identificação unica.
	 * @return
	 */
	public String gerarGuid(){

		return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
	}
	
	
	
} // Fim da classe

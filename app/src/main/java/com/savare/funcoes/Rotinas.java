package com.savare.funcoes;

import java.util.Random;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;

import com.savare.banco.funcoesSql.PessoaSql;
import com.savare.banco.funcoesSql.UsuarioSQL;

public class Rotinas {
	
	protected Context context;
	public static final String SIM = "S", NAO = "N";
	
	public Rotinas(Context context) {
		this.context = context;
	}
	
	
	
	public boolean existeUsuario(){
		boolean resultado = false;
		
		//PessoaSql pessoaSql = new PessoaSql(context);
		//Cursor cursor = pessoaSql.query("(CODIGO_FUN > 0) OR (FUNCIONARIO = '1')");

		UsuarioSQL usuarioSQL = new UsuarioSQL(context);
		Cursor cursor = usuarioSQL.query(null);
		
		if((cursor != null) && (cursor.getCount() > 0)){
			resultado = true;
		}
		
		return resultado;
	} // Fim existeUsuario
	
	
	public boolean checaUsuario(String codigoUsuario, String usuario){
		boolean resultado = false;
		
		//PessoaSql pessoaSql = new PessoaSql(context);
		//Cursor cursor = pessoaSql.query("CODIGO_FUN = " + codigoUsuario + " AND NOME_RAZAO = '" + usuario +"' ");

		UsuarioSQL usuarioSQL = new UsuarioSQL(context);
		Cursor cursor = usuarioSQL.query("LOGIN_USUA = '" + usuario + "'");
		
		if((cursor != null) && (cursor.getCount() > 0)){
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

			if (!funcoes.getValorXml("SenhaUsuario").equalsIgnoreCase(FuncoesPersonalizadas.NAO_ENCONTRADO)) {
				String senhaDescrip = funcoes.descriptografaSenha(funcoes.getValorXml("SenhaUsuario"));

				if (senhaDescrip.equals(senha)) {
					resultado = true;
				}
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

package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class EmbalagemBeans implements Parcelable{
	
	private UnidadeVendaBeans unidadeVendaEmbalagem;
	
	private int idEmbalagem,
				idUnidadeVenda,
				idProduto,
				modulo,
				decimais;
	
	private String principal,
				 ativo;
	
	private String descricaoEmbalagem, dataAlteracao;
	
	private double fatorConversao,
				   fatorPreco;
	
	public EmbalagemBeans(Parcel dados) {
		idEmbalagem = dados.readInt();
		idProduto = dados.readInt();
		modulo = dados.readInt();
		decimais = dados.readInt();
		principal = dados.readString();
		ativo = dados.readString();
		descricaoEmbalagem = dados.readString();
		fatorConversao = dados.readDouble();
		fatorPreco = dados.readDouble();
		unidadeVendaEmbalagem = (UnidadeVendaBeans) dados.readValue(UnidadeVendaBeans.class.getClassLoader());
	}
	
	public EmbalagemBeans() {
		
	}

	/**
	 * @return the unidadeVendaEmbalagem
	 */
	public UnidadeVendaBeans getUnidadeVendaEmbalagem() {
		return unidadeVendaEmbalagem;
	}

	/**
	 * @param unidadeVendaEmbalagem the unidadeVendaEmbalagem to set
	 */
	public void setUnidadeVendaEmbalagem(UnidadeVendaBeans unidadeVendaEmbalagem) {
		this.unidadeVendaEmbalagem = unidadeVendaEmbalagem;
	}

	/**
	 * @return the idEmbalagem
	 */
	public int getIdEmbalagem() {
		return idEmbalagem;
	}

	/**
	 * @param idEmbalagem the idEmbalagem to set
	 */
	public void setIdEmbalagem(int idEmbalagem) {
		this.idEmbalagem = idEmbalagem;
	}

	/**
	 * @return the idProduto
	 */
	public int getIdProduto() {
		return idProduto;
	}

	/**
	 * @param idProduto the idProduto to set
	 */
	public void setIdProduto(int idProduto) {
		this.idProduto = idProduto;
	}

	/**
	 * @return the modulo
	 */
	public int getModulo() {
		return modulo;
	}

	/**
	 * @param modulo the modulo to set
	 */
	public void setModulo(int modulo) {
		this.modulo = modulo;
	}

	/**
	 * @return the decimais
	 */
	public int getDecimais() {
		return decimais;
	}

	/**
	 * @param decimais the decimais to set
	 */
	public void setDecimais(int decimais) {
		this.decimais = decimais;
	}

	/**
	 * @return the principal
	 */
	public String getPrincipal() {
		return principal;
	}

	/**
	 * @param principal the principal to set
	 */
	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	/**
	 * @return the ativo
	 */
	public String getAtivo() {
		return ativo;
	}

	/**
	 * @param ativo the ativo to set
	 */
	public void setAtivo(String ativo) {
		this.ativo = ativo;
	}

	/**
	 * @return the descricaoEmbalagem
	 */
	public String getDescricaoEmbalagem() {
		return descricaoEmbalagem;
	}

	/**
	 * @param descricaoEmbalagem the descricaoEmbalagem to set
	 */
	public void setDescricaoEmbalagem(String descricaoEmbalagem) {
		this.descricaoEmbalagem = descricaoEmbalagem;
	}

	/**
	 * @return the fatorConversao
	 */
	public double getFatorConversao() {
		return fatorConversao;
	}

	/**
	 * @param fatorConversao the fatorConversao to set
	 */
	public void setFatorConversao(double fatorConversao) {
		this.fatorConversao = fatorConversao;
	}

	/**
	 * @return the fatorPreco
	 */
	public double getFatorPreco() {
		return fatorPreco;
	}

	/**
	 * @param fatorPreco the fatorPreco to set
	 */
	public void setFatorPreco(double fatorPreco) {
		this.fatorPreco = fatorPreco;
	}

	/**
	 * @return the idUnidadeVenda
	 */
	public int getIdUnidadeVenda() {
		return idUnidadeVenda;
	}

	/**
	 * @param idUnidadeVenda the idUnidadeVenda to set
	 */
	public void setIdUnidadeVenda(int idUnidadeVenda) {
		this.idUnidadeVenda = idUnidadeVenda;
	}

	public String getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(idEmbalagem);
		dest.writeInt(idProduto);
		dest.writeInt(modulo);
		dest.writeInt(decimais);
		dest.writeString(String.valueOf(principal));
		dest.writeString(String.valueOf(ativo));
		dest.writeString(descricaoEmbalagem);
		dest.writeDouble(fatorConversao);
		dest.writeDouble(fatorPreco);
		dest.writeValue(unidadeVendaEmbalagem);
	}
	
	public static final Parcelable.Creator<EmbalagemBeans> CREATOR = new Creator<EmbalagemBeans>() {

		@Override
		public EmbalagemBeans createFromParcel(Parcel source) {
			return new EmbalagemBeans(source);
		}

		@Override
		public EmbalagemBeans[] newArray(int size) {
			return new EmbalagemBeans[size];
		}
		
	};

				

}

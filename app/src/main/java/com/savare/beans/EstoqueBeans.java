package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class EstoqueBeans implements Parcelable  {

    private int idEstoque,
    			idProdutoLoja,
    			idLocacao;
	private String guid,
				   dataAlteracao;
	private double estoqueLocacao,
				   retidoLocacao;
	private String ativo;
	
	public EstoqueBeans(Parcel dados) {
		this.idEstoque = dados.readInt();
		this.idProdutoLoja = dados.readInt();
		this.idLocacao = dados.readInt();
		this.guid = dados.readString();
		this.estoqueLocacao = dados.readDouble();
		this.retidoLocacao = dados.readDouble();
		this.ativo = dados.readString();
	}
	
	public EstoqueBeans() {
		
	}
	
	/**
	 * @return the idEstoque
	 */
	public int getIdEstoque() {
		return idEstoque;
	}
	/**
	 * @param idEstoque the idEstoque to set
	 */
	public void setIdEstoque(int idEstoque) {
		this.idEstoque = idEstoque;
	}
	/**
	 * @return the idProdutoLoja
	 */
	public int getIdProdutoLoja() {
		return idProdutoLoja;
	}
	/**
	 * @param idProdutoLoja the idProdutoLoja to set
	 */
	public void setIdProdutoLoja(int idProdutoLoja) {
		this.idProdutoLoja = idProdutoLoja;
	}
	/**
	 * @return the idLocacao
	 */
	public int getIdLocacao() {
		return idLocacao;
	}
	/**
	 * @param idLocacao the idLocacao to set
	 */
	public void setIdLocacao(int idLocacao) {
		this.idLocacao = idLocacao;
	}
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return the dataAlteracao
	 */
	public String getDataAlteracao() {
		return dataAlteracao;
	}
	/**
	 * @param dataAlteracao the dataAlteracao to set
	 */
	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
	/**
	 * @return the estoqueLocacao
	 */
	public double getEstoqueLocacao() {
		return estoqueLocacao;
	}
	/**
	 * @param estoqueLocacao the estoqueLocacao to set
	 */
	public void setEstoqueLocacao(double estoqueLocacao) {
		this.estoqueLocacao = estoqueLocacao;
	}
	/**
	 * @return the retidoLocacao
	 */
	public double getRetidoLocacao() {
		return retidoLocacao;
	}
	/**
	 * @param retidoLocacao the retidoLocacao to set
	 */
	public void setRetidoLocacao(double retidoLocacao) {
		this.retidoLocacao = retidoLocacao;
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
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.idEstoque);
		dest.writeInt(this.idProdutoLoja);
		dest.writeInt(this.idLocacao);
		dest.writeString(this.guid);
		dest.writeDouble(this.estoqueLocacao);
		dest.writeDouble(this.retidoLocacao);
		dest.writeString(String.valueOf(this.ativo));
	}
	
	public static final Parcelable.Creator<EstoqueBeans> CREATOR = new Creator<EstoqueBeans>() {

		@Override
		public EstoqueBeans createFromParcel(Parcel source) {
			return new EstoqueBeans(source);
		}

		@Override
		public EstoqueBeans[] newArray(int size) {
			return new EstoqueBeans[size];
		}
		
	};
	
	
	
	

}

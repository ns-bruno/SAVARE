package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class UnidadeVendaBeans implements Parcelable {
	
	private int idUnidadeVenda, casasDecimais;
	private String dataAlteracao,
				   siglaUnidadeVenda,
				   descricaoUnidadeVenda;
	
	public UnidadeVendaBeans(Parcel dados) {
		this.idUnidadeVenda = dados.readInt();
		this.casasDecimais = dados.readInt();
		this.siglaUnidadeVenda = dados.readString();
		this.descricaoUnidadeVenda = dados.readString();
	}
	
	public UnidadeVendaBeans() {
		
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
	/**
	 * @return the casasDecimais
	 */
	public int getCasasDecimais() {
		return casasDecimais;
	}
	/**
	 * @param casasDecimais the casasDecimais to set
	 */
	public void setCasasDecimais(int casasDecimais) {
		this.casasDecimais = casasDecimais;
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
	 * @return the siglaUnidadeVenda
	 */
	public String getSiglaUnidadeVenda() {
		return siglaUnidadeVenda;
	}
	/**
	 * @param siglaUnidadeVenda the siglaUnidadeVenda to set
	 */
	public void setSiglaUnidadeVenda(String siglaUnidadeVenda) {
		this.siglaUnidadeVenda = siglaUnidadeVenda;
	}
	/**
	 * @return the descricaoUnidadeVenda
	 */
	public String getDescricaoUnidadeVenda() {
		return descricaoUnidadeVenda;
	}
	/**
	 * @param descricaoUnidadeVenda the descricaoUnidadeVenda to set
	 */
	public void setDescricaoUnidadeVenda(String descricaoUnidadeVenda) {
		this.descricaoUnidadeVenda = descricaoUnidadeVenda;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		 dest.writeInt(idUnidadeVenda);
		 dest.writeInt(casasDecimais);
		 dest.writeString(siglaUnidadeVenda);
		 dest.writeString(descricaoUnidadeVenda);
	}
	
	public static final Parcelable.Creator<UnidadeVendaBeans> CREATOR = new Creator<UnidadeVendaBeans>() {

		@Override
		public UnidadeVendaBeans createFromParcel(Parcel source) {
			return new UnidadeVendaBeans(source);
		}

		@Override
		public UnidadeVendaBeans[] newArray(int size) {
			return new UnidadeVendaBeans[size];
		}
		
	};
	
	

}

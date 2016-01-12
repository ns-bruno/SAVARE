package com.savare.beans;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ProdutoBeans implements Parcelable {
	
	private UnidadeVendaBeans unidadeVendaProduto;
	private ClasseBeans classeProduto;
	private List<EmbalagemBeans> listaEmbalagem;
	
	private int idProduto,
				diasCadastro;
	private String descricaoProduto,
				   codigoEstrutural,
				   codigoBarras,
				   referencia,
				   descricaoMarca,
				   descricaoAuxiliar;
	private double pesoBruto,
				   pesoLiquido;
	private char tipoProduto;

	private FotosBeans imagemProduto;
	
	public ProdutoBeans(Parcel dados) {
		this.idProduto = dados.readInt();
		this.codigoEstrutural = dados.readString();
	    this.descricaoProduto = dados.readString();
	    this.descricaoMarca = dados.readString();
	    this.unidadeVendaProduto = (UnidadeVendaBeans) dados.readValue(UnidadeVendaBeans.class.getClassLoader());
	    List<EmbalagemBeans> embalagem = new ArrayList<EmbalagemBeans>();
	    dados.readList(embalagem, EmbalagemBeans.class.getClassLoader());
	    this.listaEmbalagem = embalagem;
	}
	
	public ProdutoBeans() {
		
	}
	
	
	/**
	 * @return the diasCadastro
	 */
	public int getDiasCadastro() {
		return diasCadastro;
	}

	/**
	 * @param diasCadastro the diasCadastro to set
	 */
	public void setDiasCadastro(int diasCadastro) {
		this.diasCadastro = diasCadastro;
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
	 * @return the descricaoProduto
	 */
	public String getDescricaoProduto() {
		return descricaoProduto;
	}
	/**
	 * @param descricaoProduto the descricaoProduto to set
	 */
	public void setDescricaoProduto(String descricaoProduto) {
		this.descricaoProduto = descricaoProduto;
	}
	/**
	 * @return the codigoEstrutural
	 */
	public String getCodigoEstrutural() {
		return codigoEstrutural;
	}
	/**
	 * @param codigoEstrutural the codigoEstrutural to set
	 */
	public void setCodigoEstrutural(String codigoEstrutural) {
		this.codigoEstrutural = codigoEstrutural;
	}
	/**
	 * @return the descricaoMarca
	 */
	public String getDescricaoMarca() {
		return descricaoMarca;
	}
	/**
	 * @param marca the descricaoMarca to set
	 */
	public void setDescricaoMarca(String marca) {
		this.descricaoMarca = marca;
	}
	/**
	 * @return the unidadeVendaProduto
	 */
	public UnidadeVendaBeans getUnidadeVendaProduto() {
		return unidadeVendaProduto;
	}
	/**
	 * @param unidadeVendaProduto the unidadeVendaProduto to set
	 */
	public void setUnidadeVendaProduto(UnidadeVendaBeans unidadeVendaProduto) {
		this.unidadeVendaProduto = unidadeVendaProduto;
	}
	/**
	 * @return the classeProduto
	 */
	public ClasseBeans getClasseProduto() {
		return classeProduto;
	}
	/**
	 * @param classeProduto the classeProduto to set
	 */
	public void setClasseProduto(ClasseBeans classeProduto) {
		this.classeProduto = classeProduto;
	}
	/**
	 * @return the listaEmbalagem
	 */
	public List<EmbalagemBeans> getListaEmbalagem() {
		return listaEmbalagem;
	}
	/**
	 * @param listaEmbalagem the listaEmbalagem to set
	 */
	public void setListaEmbalagem(List<EmbalagemBeans> listaEmbalagem) {
		this.listaEmbalagem = listaEmbalagem;
	}
	/**
	 * @return the referencia
	 */
	public String getReferencia() {
		return referencia;
	}
	/**
	 * @param referencia the referencia to set
	 */
	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}
	/**
	 * @return the pesoBruto
	 */
	public double getPesoBruto() {
		return pesoBruto;
	}
	/**
	 * @param pesoBruto the pesoBruto to set
	 */
	public void setPesoBruto(double pesoBruto) {
		this.pesoBruto = pesoBruto;
	}
	/**
	 * @return the pesoLiquido
	 */
	public double getPesoLiquido() {
		return pesoLiquido;
	}
	/**
	 * @param pesoLiquido the pesoLiquido to set
	 */
	public void setPesoLiquido(double pesoLiquido) {
		this.pesoLiquido = pesoLiquido;
	}
	
	
	/**
	 * @return the codigoBarras
	 */
	public String getCodigoBarras() {
		return codigoBarras;
	}

	/**
	 * @param codigoBarras the codigoBarras to set
	 */
	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public FotosBeans getImagemProduto() {
		return imagemProduto;
	}

	public void setImagemProduto(FotosBeans imagemProduto) {
		this.imagemProduto = imagemProduto;
	}

	/**
	 * @return the descricaoAuxiliar
	 */
	public String getDescricaoAuxiliar() {
		return descricaoAuxiliar;
	}

	/**
	 * @param descricaoAuxiliar the descricaoAuxiliar to set
	 */
	public void setDescricaoAuxiliar(String descricaoAuxiliar) {
		this.descricaoAuxiliar = descricaoAuxiliar;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(idProduto);
		dest.writeString(codigoEstrutural);
	    dest.writeString(descricaoProduto);
	    dest.writeString(descricaoMarca);
	    dest.writeValue(unidadeVendaProduto);
	    dest.writeList(listaEmbalagem);
	}
	
	public static final Parcelable.Creator<ProdutoBeans> CREATOR = new Creator<ProdutoBeans>() {
		@Override
		public ProdutoBeans[] newArray(int size) {
			return new ProdutoBeans[size];
		}

		@Override
		public ProdutoBeans createFromParcel(Parcel source) {
			return new ProdutoBeans(source);
		}
		
		
	};

	/**
	 * @return the tipoProduto
	 */
	public char getTipoProduto() {
		return tipoProduto;
	}

	/**
	 * @param tipoProduto the tipoProduto to set
	 */
	public void setTipoProduto(char tipoProduto) {
		this.tipoProduto = tipoProduto;
	}
	
}

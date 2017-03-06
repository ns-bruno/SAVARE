package com.savare.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class PlanoPagamentoBeans implements Parcelable{
	
	private int idPlanoPagamento,
				idEmpresa,
				codigoPlanoPagamento, diasMedios;
	private String descricaoPlanoPagamento, dataAlteracao;
	private String ativo,
				 origemValor,
				 atacadoVarejo,
				 vistaPrazo,
				 descontoPromocao;
	private double descontoAtacado,
				   descontoVarejo,
				   jurosAtacado,
				   jurosVarejo,
				   precoProduto;
	
	public PlanoPagamentoBeans(Parcel dados) {
		this.idPlanoPagamento = dados.readInt();
		this.idEmpresa = dados.readInt();
		this.codigoPlanoPagamento = dados.readInt();
		this.descricaoPlanoPagamento = dados.readString();
		this.atacadoVarejo = dados.readString();
		this.descontoPromocao = dados.readString();
		this.descontoAtacado = dados.readDouble();
		this.descontoVarejo = dados.readDouble();
		this.jurosAtacado = dados.readDouble();
		this.jurosVarejo = dados.readDouble();
	}
	
	public PlanoPagamentoBeans() {
		
	}
	

	/**
	 * @return the precoProduto
	 */
	public double getPrecoProduto() {
		return precoProduto;
	}

	/**
	 * @param precoProduto the precoProduto to set
	 */
	public void setPrecoProduto(double precoProduto) {
		this.precoProduto = precoProduto;
	}

	/**
	 * @return the idPlanoPagamento
	 */
	public int getIdPlanoPagamento() {
		return idPlanoPagamento;
	}

	/**
	 * @param idPlanoPagamento the idPlanoPagamento to set
	 */
	public void setIdPlanoPagamento(int idPlanoPagamento) {
		this.idPlanoPagamento = idPlanoPagamento;
	}

	/**
	 * @return the idEmpresa
	 */
	public int getIdEmpresa() {
		return idEmpresa;
	}

	/**
	 * @param idEmpresa the idEmpresa to set
	 */
	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	/**
	 * @return the codigoPlanoPagamento
	 */
	public int getCodigoPlanoPagamento() {
		return codigoPlanoPagamento;
	}

	/**
	 * @param codigoPlanoPagamento the codigoPlanoPagamento to set
	 */
	public void setCodigoPlanoPagamento(int codigoPlanoPagamento) {
		this.codigoPlanoPagamento = codigoPlanoPagamento;
	}

	/**
	 * @return the descricaoPlanoPagamento
	 */
	public String getDescricaoPlanoPagamento() {
		return descricaoPlanoPagamento;
	}

	/**
	 * @param descricaoPlanoPagamento the descricaoPlanoPagamento to set
	 */
	public void setDescricaoPlanoPagamento(String descricaoPlanoPagamento) {
		this.descricaoPlanoPagamento = descricaoPlanoPagamento;
	}

	/**
	 * @return the ativo
	 */
	public String getAtivo() {
		return ativo;
	}

	public String getOrigemValor() {
		return origemValor;
	}

	public void setOrigemValor(String origemValor) {
		this.origemValor = origemValor;
	}

	/**
	 * @param ativo the ativo to set
	 */
	public void setAtivo(String ativo) {
		this.ativo = ativo;
	}

	/**
	 * @return the atacadoVarejo
	 */
	public String getAtacadoVarejo() {
		return atacadoVarejo;
	}

	/**
	 * @param atacadoVarejo the atacadoVarejo to set
	 */
	public void setAtacadoVarejo(String atacadoVarejo) {
		this.atacadoVarejo = atacadoVarejo;
	}
	

	/**
	 * @return the vistaPrazo
	 */
	public String getVistaPrazo() {
		return vistaPrazo;
	}

	/**
	 * @param vistaPrazo the vistaPrazo to set
	 */
	public void setVistaPrazo(String vistaPrazo) {
		this.vistaPrazo = vistaPrazo;
	}

	/**
	 * @return the descontoPromocao
	 */
	public String getDescontoPromocao() {
		return descontoPromocao;
	}

	/**
	 * @param descontoPromocao the descontoPromocao to set
	 */
	public void setDescontoPromocao(String descontoPromocao) {
		this.descontoPromocao = descontoPromocao;
	}

	/**
	 * @return the descontoAtacado
	 */
	public double getDescontoAtacado() {
		return descontoAtacado;
	}

	/**
	 * @param descontoAtacado the descontoAtacado to set
	 */
	public void setDescontoAtacado(double descontoAtacado) {
		this.descontoAtacado = descontoAtacado;
	}

	/**
	 * @return the descontoVarejo
	 */
	public double getDescontoVarejo() {
		return descontoVarejo;
	}

	/**
	 * @param descontoVarejo the descontoVarejo to set
	 */
	public void setDescontoVarejo(double descontoVarejo) {
		this.descontoVarejo = descontoVarejo;
	}

	/**
	 * @return the jurosAtacado
	 */
	public double getJurosAtacado() {
		return jurosAtacado;
	}

	/**
	 * @param jurosAtacado the jurosAtacado to set
	 */
	public void setJurosAtacado(double jurosAtacado) {
		this.jurosAtacado = jurosAtacado;
	}

	/**
	 * @return the jurosVarejo
	 */
	public double getJurosVarejo() {
		return jurosVarejo;
	}

	/**
	 * @param jurosVarejo the jurosVarejo to set
	 */
	public void setJurosVarejo(double jurosVarejo) {
		this.jurosVarejo = jurosVarejo;
	}

	public String getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(String dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	public int getDiasMedios() {
		return diasMedios;
	}

	public void setDiasMedios(int diasMedios) {
		this.diasMedios = diasMedios;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(idPlanoPagamento);
		dest.writeInt(idEmpresa);
		dest.writeInt(codigoPlanoPagamento);
		dest.writeString(descricaoPlanoPagamento);
		dest.writeString(String.valueOf(atacadoVarejo));
		dest.writeDouble(descontoAtacado);
		dest.writeDouble(descontoVarejo);
		dest.writeDouble(jurosAtacado);
		dest.writeDouble(jurosVarejo);
	}
	
	public static final Parcelable.Creator<PlanoPagamentoBeans> CREATOR = new Creator<PlanoPagamentoBeans>() {

		@Override
		public PlanoPagamentoBeans createFromParcel(Parcel source) {
			return new PlanoPagamentoBeans(source);
		}

		@Override
		public PlanoPagamentoBeans[] newArray(int size) {
			return new PlanoPagamentoBeans[size];
		}
		
	};

	
}

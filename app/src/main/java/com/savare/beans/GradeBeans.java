package com.savare.beans;

/**
 * Created by Bruno Nogueira Silva on 15/08/2016.
 */
public class GradeBeans {

    private int idGrade, idTipoGrade;
    private String dataAlteracao, descricaoGrade;

    public int getIdGrade() {
        return idGrade;
    }

    public void setIdGrade(int idGrade) {
        this.idGrade = idGrade;
    }

    public int getIdTipoGrade() {
        return idTipoGrade;
    }

    public void setIdTipoGrade(int idTipoGrade) {
        this.idTipoGrade = idTipoGrade;
    }

    public String getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(String dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getDescricaoGrade() {
        return descricaoGrade;
    }

    public void setDescricaoGrade(String descricaoGrade) {
        this.descricaoGrade = descricaoGrade;
    }
}

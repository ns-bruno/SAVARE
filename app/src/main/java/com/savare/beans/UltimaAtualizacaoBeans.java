package com.savare.beans;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by Bruno Nogueira Silva on 09/08/2016.
 */
public class UltimaAtualizacaoBeans implements KvmSerializable {
    private int idUltimaAtualizacao;
    private String idDispositivo, dataCad, dataAlt, tabela, dataUltimaAtualizacao;

    public int getIdUltimaAtualizacao() {
        return idUltimaAtualizacao;
    }

    public void setIdUltimaAtualizacao(int idUltimaAtualizacao) {
        this.idUltimaAtualizacao = idUltimaAtualizacao;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public String getDataCad() {
        return dataCad;
    }

    public void setDataCad(String dataCad) {
        this.dataCad = dataCad;
    }

    public String getDataAlt() {
        return dataAlt;
    }

    public void setDataAlt(String dataAlt) {
        this.dataAlt = dataAlt;
    }

    public String getTabela() {
        return tabela;
    }

    public void setTabela(String tabela) {
        this.tabela = tabela;
    }

    public String getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(String dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    @Override
    public Object getProperty(int i) {

        switch (i){
            case 0:
                return this.idUltimaAtualizacao;

            case 1:
                return this.idDispositivo;

            case 2:
                return this.dataCad;

            case 3:
                return this.dataAlt;

            case 4:
                return this.tabela;

            case 5:
                return this.dataUltimaAtualizacao;

            default:
                break;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 6;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i){
            case 0:
                this.idUltimaAtualizacao = Integer.parseInt(o.toString());
                break;
            case 1:
                this.idDispositivo = o.toString();
                break;
            case 2:
                this.dataCad = o.toString();
                break;

            case 3:
                this.dataAlt = o.toString();
                break;

            case 4:
                this.tabela = o.toString();
                break;

            case 5:
                this.dataUltimaAtualizacao = o.toString();
                break;

            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i){
            case 0:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idUltimaAtualizacao";
                break;

            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "idDispositivo";
                break;

            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "dataCad";
                break;

            case 3:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "dataAlt";
                break;

            case 4:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "tabela";
                break;

            case 5:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "dataUltimaAtualizacao";
                break;

            default:
                break;
        }
    }
}

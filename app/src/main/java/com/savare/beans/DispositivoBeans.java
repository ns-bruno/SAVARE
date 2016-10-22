package com.savare.beans;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 *
 * @author Bruno Nogueira Silva
 */
public class DispositivoBeans implements KvmSerializable {
    
    private String idDispositivo, chaveUsuario, nomeDispositivo, sistemaOperacionalDispositivo, dataUltimoAcesso,
            numeroSerialDispositivo, marcaDispositivo, operadoraDispositivo, ipHost;

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public String getChaveUsuario() {
        return chaveUsuario;
    }

    public void setChaveUsuario(String chaveUsuario) {
        this.chaveUsuario = chaveUsuario;
    }

    public String getNomeDispositivo() {
        return nomeDispositivo;
    }

    public void setNomeDispositivo(String nomeDispositivo) {
        this.nomeDispositivo = nomeDispositivo;
    }

    public String getSistemaOperacionalDispositivo() {
        return sistemaOperacionalDispositivo;
    }

    public void setSistemaOperacionalDispositivo(String sistemaOperacionalDispositivo) {
        this.sistemaOperacionalDispositivo = sistemaOperacionalDispositivo;
    }

    public String getDataUltimoAcesso() {
        return dataUltimoAcesso;
    }

    public void setDataUltimoAcesso(String dataUltimoAcesso) {
        this.dataUltimoAcesso = dataUltimoAcesso;
    }

    public String getNumeroSerialDispositivo() {
        return numeroSerialDispositivo;
    }

    public void setNumeroSerialDispositivo(String numeroSerialDispositivo) {
        this.numeroSerialDispositivo = numeroSerialDispositivo;
    }

    public String getMarcaDispositivo() {
        return marcaDispositivo;
    }

    public void setMarcaDispositivo(String marcaDispositivo) {
        this.marcaDispositivo = marcaDispositivo;
    }

    public String getOperadoraDispositivo() {
        return operadoraDispositivo;
    }

    public void setOperadoraDispositivo(String operadoraDispositivo) {
        this.operadoraDispositivo = operadoraDispositivo;
    }

    public String getIpHost() {
        return ipHost;
    }

    public void setIpHost(String ipHost) {
        this.ipHost = ipHost;
    }

    @Override
    public Object getProperty(int i) {
        switch (i){

            case 0 :
                return this.idDispositivo;

            case 1 :
                return this.chaveUsuario;

            case 2:
                return this.nomeDispositivo;

            case 3:
                return this.sistemaOperacionalDispositivo;

            case 4:
                return this.dataUltimoAcesso;

            case 5:
                return this.numeroSerialDispositivo;

            case 6:
                return this.marcaDispositivo;

            case 7:
                return this.operadoraDispositivo;

            case 8:
                return this.ipHost;

            default:
                break;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 9;
    }

    @Override
    public void setProperty(int i, Object o) {

        switch (i){
            case 0:
                this.idDispositivo = o.toString();
                break;

            case 1:
                this.chaveUsuario = o.toString();
                break;

            case 2:
                this.nomeDispositivo = o.toString();
                break;

            case 3:
                this.sistemaOperacionalDispositivo = o.toString();
                break;

            case 4:
                this.dataUltimoAcesso = o.toString();
                break;

            case 5:
                this.numeroSerialDispositivo = o.toString();
                break;

            case 6:
                this.marcaDispositivo = o.toString();
                break;

            case 7:
                this.operadoraDispositivo = o.toString();
                break;

            case 8:
                this.ipHost = o.toString();
                break;

            default:
                break;
        }

    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {

        switch (i){
            case 0:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "idDispositivo";
                break;

            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "chaveUsuario";
                break;

            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "nomeDispositivo";
                break;

            case 3:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "sistemaOperacionalDispositivo";
                break;

            case 4:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "dataUltimoAcesso";
                break;

            case 5:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "numeroSerialDispositivo";
                break;

            case 6:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "marcaDispositivo";
                break;

            case 7:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "operadoraDispositivo";
                break;

            case 8:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "ipHost";
                break;

            default:
                break;
        }
    }
}

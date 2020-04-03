package com.savare.webservice;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.savare.R;
import com.savare.beans.DispositivoBeans;
import com.savare.beans.RetornoWebServiceBeans;
import com.savare.beans.ServidoresBeans;
import com.savare.beans.SmadispoBeans;
import com.savare.configuracao.ServicosWeb;
import com.savare.funcoes.FuncoesPersonalizadas;

import org.apache.http.client.HttpClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by Bruno Nogueira Silva on 23/06/2016.
 */
public class WSSisinfoWebservice {

    private Context context;
    private DispositivoBeans dispositivoBeans;
    private SmadispoBeans smadispoBeans;
    public static final String FUNCTION_SELECT_USUARIO_USUA = "selectUsuario";
    public static final String FUNCTION_CADASTRAR_DISPOSITIVO = "cadastrarDispositivo";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAAREAS = "/Cfaareas";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_SMAEMPRE = "/Smaempre";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_SMADISPO = "/Smadispo";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAATIVI = "/Cfaativi";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFASTATU = "/Cfastatu";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFATPDOC = "/Cfatpdoc";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFACCRED = "/Cfaccred";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAPORTA = "/Cfaporta";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAPROFI = "/Cfaprofi";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCLI = "/Cfatpcli";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFATPCOB = "/Cfatpcob";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAESTAD = "/Cfaestad";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFACIDAD = "/Cfacidad";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO = "/Cfaclifo";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFACOTAC = "/Cfacotac";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFACLIFO_ADMIN = "/Cfaclifo/Admin";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFABAIRO = "/Cfabairo";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER = "/Cfaender";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAENDER_CUSTOM = "/CfaenderCustom";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAPARAM = "/Cfaparam";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_CFAFOTOS = "/Cfafotos";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLPGT = "/Aeaplpgt";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEACLASE = "/Aeaclase";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEACONJT = "/Aeaconjt";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAUNVEN = "/Aeaunven";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAGRADE = "/Aeagrade";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAMARCA = "/Aeamarca";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEACODST = "/Aeacodst";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRODU = "/Aeaprodu";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRECO = "/Aeapreco";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMBAL = "/Aeaembal";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAPLOJA = "/Aeaploja";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEALOCES = "/Aealoces";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAESTOQ = "/Aeaestoq";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAORCAM = "/Aeaorcam";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAITGPR = "/Aeaitgpr";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAITORC = "/Aeaitorc";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEASAIDA = "/Aeasaida";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAPERCE = "/Aeaperce";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAFATOR = "/Aeafator";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAPRREC = "/Aeaprrec";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEATBPRO = "/Aeatbpro";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAITTBP = "/Aeaittbp";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAEXTBP = "/Aeaextbp";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_AEAEMTBP = "/Aeaemtbp";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_RPAPARCE = "/Rpaparce";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_RPALCPAR = "/Rpalcpar";
    public static final String FUNCTION_SISINFOWEB_JSON_SELECT_SMAVERPR = "/Smaverpr";
    public static final String FUNCTION_INSERT_AEAORCAM = "insertOrcamento";
    public static final String FUNCTION_SISINFOWEB_JSON_INSERT_AEAORCAM = "/Aeaorcam";
    public static final String FUNCTION_SISINFOWEB_JSON_INSERT_CFACLIFO = "/Cfaclifo";
    public static final String FUNCTION_INSERT_AEAITORC = "insertItemOrcamento";
    public static final String FUNCTION_UPDATE_STATUS_AEAORCAM = "updateStatusOrcamento";
    public static final String FUNCTION_CHECK_SEND_AEAORCAM = "checkSendOrcamento";
    public static final String METODO_GET = "GET";
    public static final String METODO_PUT = "PUT";
    public static final String METODO_POST = "POST";
    public static final String KEY_OBJECT_STATUS_RETORNO = "statusRetorno";
    public static final String KEY_OBJECT_OBJECT_RETORNO = "object";
    public static final String KEY_OBJECT_PAGE_RETORNO = "page";
    public static final String KEY_ELEMENT_CODIGO_RETORNO = "codigoRetorno";
    public static final String KEY_ELEMENT_MENSAGEM_RETORNO = "mensagemRetorno";
    public static final String KEY_ELEMENT_EXTRA_RETORNO = "extra";
    public static final String KEY_ELEMENT_TOTAL_PAGES_RETORNO = "totalPages";
    public static final String KEY_ELEMENT_PAGE_NUMBER_RETORNO = "pageNumber";
    public static final String KEY_ELEMENT_TOTAL_ELEMENTS = "totalElements";

    public WSSisinfoWebservice(Context context) {
        this.context = context;
    }


    @Deprecated
    public Vector<SoapObject> executarSelectWebservice(String sql, String funcao, List<PropertyInfo> listaPropriedadesExtra){

        Vector<SoapObject> retorno = null;

        try {
            SoapObject soap = new SoapObject(ServicosWeb.WS_NAME_SPACE, funcao);

            PropertyInfo propertyDispositivo = new PropertyInfo();
            propertyDispositivo.setName("dispositivo");
            propertyDispositivo.setValue(dispositivoBeans);
            propertyDispositivo.setType(dispositivoBeans.getClass());
            soap.addProperty(propertyDispositivo);

            // Checa se o sql esta vazio
            if ((sql != null) && (!sql.isEmpty())) {
                PropertyInfo propertySql = new PropertyInfo();
                propertySql.setName("sql");
                propertySql.setValue(sql);
                propertySql.setType(sql.getClass());
                soap.addProperty(propertySql);
            }

            if (listaPropriedadesExtra != null) {
                // Pega todas propriedades passado por paramentros
                for (PropertyInfo propriedadInfo : listaPropriedadesExtra) {
                    soap.addProperty(propriedadInfo);
                }
            }

            // Definicao da versao do protocolo do webservice, a forma de como os dados serao enviados
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            // Adiciona os objetos/propriedades
            envelope.setOutputSoapObject(soap);
            // Mapea a esta classe
            envelope.addMapping(ServicosWeb.WS_NAME_SPACE, WSSisinfoWebservice.class.getSimpleName(), WSSisinfoWebservice.class);

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            String ipServidor = ServicosWeb.IP_SERVIDOR_WEBSERVICE;

            // Checa se retornou algum endereco de ip do servidor
            if ((ipServidor == null) || (ipServidor.length() <= 1) || (ipServidor.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
                ipServidor = "localhost";
                //ipServidor = "172.16.0.4";
            }
            String enderecoWebService = "http://" + ipServidor + ":8080/" + ServicosWeb.WS_ENDERECO_WEBSERVICE;

            HttpTransportSE httpTransporte = new HttpTransportSE(enderecoWebService, 100000);

            // Requisicao dos dados
            httpTransporte.call(funcao, envelope);

            if ((envelope.getResponse() != null) && (!((SoapObject) envelope.bodyIn).toString().contains("faultactor: 'null' detail: null"))) {
                // Instancia a classe de vetor para salvar a lista
                retorno = new Vector<SoapObject>();

                SoapObject objetoPropriedade = (SoapObject) envelope.bodyIn;

                // Checa se retornou apena um registro na lista
                if (objetoPropriedade.getPropertyCount() == 1){
                    retorno.add(objetoPropriedade);

                } else {
                    for (SoapObject objeto : (Vector<SoapObject>) envelope.getResponse()) {
                        retorno.add(objeto);
                    }
                }

            } else {
                return null;
            }
            return retorno;

        } catch (IOException e) {

            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());
            // Pega os dados do usuario

            //contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            //contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            //contentValues.put("email", funcoes.getValorXml("Email"));

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } catch (XmlPullParserException e) {

            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());
            // Pega os dados do usuario

            //contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            //contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            //contentValues.put("email", funcoes.getValorXml("Email"));

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        }
        return null;
    }

    /**
     * Substituido pelo metodo @executarWebserviceJson;
     * @param sql
     * @param funcao
     * @param metodo
     * @param parametros - Os dados a serem passados ja tem que ser passado no formato Json
     * @return
     */
    @Deprecated
    public String executarSelectWebserviceJson(String sql, String funcao, String metodo, String parametros, String parametrosUrl){
        String retorno = null;
        HttpURLConnection conexaoHttp = null;
        try {
            String ipServidor = ServicosWeb.IP_SERVIDOR_WEBSERVICE;

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Checa se retornou algum endereco de ip do servidor
            if ((ipServidor == null) || (ipServidor.length() <= 1) || (ipServidor.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
                ipServidor = "localhost";
            }
            //String enderecoWebService = "http://" + ipServidor + ((!ServicosWeb.PORTA_JSON.isEmpty()) ? (":" + ServicosWeb.PORTA_JSON + "/") : "/" ) + ServicosWeb.WS_ENDERECO_WEBSERVICE_JSON + funcao;
            String enderecoWebService = "http://" + ipServidor + ((!ServicosWeb.PORTA_JSON.isEmpty()) ? (":" + ServicosWeb.PORTA_JSON + "/") : "/" ) + ServicosWeb.WS_ENDERECO_SISINFOWEB + funcao;

            if ((metodo.equalsIgnoreCase(METODO_GET)) || (metodo.equalsIgnoreCase(METODO_POST))){
                Gson gson = new Gson();

                smadispoBeans = new SmadispoBeans();
                smadispoBeans.setIdentificacao(funcoes.getValorXml("UuidDispositivo"));

                // Adiciona o dipositivo em formado json e codificado
                //enderecoWebService += "/" + URLEncoder.encode(gson.toJson(dispositivoBeans), "UTF-8");
                enderecoWebService += "?dispositivo=" + URLEncoder.encode(gson.toJson(smadispoBeans), "UTF-8");

                if ((metodo.equalsIgnoreCase(METODO_GET)) && (parametros != null) && (!parametros.isEmpty()) && (parametros.contains("&"))){
                    //enderecoWebService += "/" + URLEncoder.encode(parametros, "UTF-8");
                    enderecoWebService += URLEncoder.encode(parametros, "UTF-8").replace("%26", "&").replace("%3D", "=");
                }
                if ((metodo.equalsIgnoreCase(METODO_GET)) && (sql != null) && (!sql.isEmpty())){
                    //enderecoWebService += "/" + URLEncoder.encode(parametros, "UTF-8");
                    enderecoWebService += "&sqlQuery= " + URLEncoder.encode(sql, "UTF-8");
                }
                if((parametrosUrl) != null && (!parametrosUrl.isEmpty()) && (parametrosUrl.contains("&"))){
                    enderecoWebService += URLEncoder.encode(parametrosUrl, "UTF-8").replace("%26", "&").replace("%3D", "=");
                }
            }
            URL urlWebservice = new URL(enderecoWebService);

            conexaoHttp = (HttpURLConnection) urlWebservice.openConnection();
            // Indeca o metodo da conexao (GET, PUT, POST, DEL)
            conexaoHttp.setRequestMethod(metodo);
            conexaoHttp.setRequestProperty("Accept", "application/json");
            conexaoHttp.setRequestProperty("Content-Type", "application/json");
            conexaoHttp.setConnectTimeout(100000);
            conexaoHttp.setReadTimeout(100000);

            if ((metodo.equalsIgnoreCase(METODO_POST)) && (parametros != null) && (!parametros.isEmpty())){
                // // Define que a conexão pode enviar informacoes e obte-las de volta
                conexaoHttp.setDoOutput(true);
                conexaoHttp.setDoInput(true);

                DataOutputStream dadosEnvio = new DataOutputStream(conexaoHttp.getOutputStream());
                // Salva o Json para envio
                dadosEnvio.writeBytes(parametros);
                dadosEnvio.flush();
            }
            conexaoHttp.connect();

            // Pega o codigo de retorno da comunicacao
            int codigoResp = conexaoHttp.getResponseCode();
            // Checa o codigo de retorno, se foi com sucesso
            if (codigoResp == HttpURLConnection.HTTP_OK) {

                //InputStream inputStream = new BufferedInputStream(conexaoHttp.getInputStream());
                BufferedReader buffeLeitura = new BufferedReader(new InputStreamReader(conexaoHttp.getInputStream()));
                StringBuilder dadosWebservice = new StringBuilder();

                String inputLine;
                while ((inputLine = buffeLeitura.readLine()) != null) {
                    dadosWebservice.append(inputLine + "\n");
                }
                buffeLeitura.close();

                return dadosWebservice.toString();
            }

            if (codigoResp == 404){
                // Armazena as informacoes para para serem exibidas e enviadas
                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "WSSisInfoWebservice");
                contentValues.put("mensagem", "Erro: 404. \nEndereço dos dados está errado. Tente novamente mais tarde, caso persista o erro entre em contato com suporte SAVARE. \n" +
                                              "Função: " + funcao + " \n Metodo: " + metodo);
                contentValues.put("dados", conexaoHttp.toString());

                final FuncoesPersonalizadas finalFuncoes = funcoes;
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        finalFuncoes.menssagem(contentValues);
                    }
                });
            }

            if (codigoResp == 500){
                funcoes = new FuncoesPersonalizadas(context);

                // Armazena as informacoes para para serem exibidas e enviadas
                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "WSSisInfoWebservice");
                contentValues.put("mensagem", "Erro: 500. \nAconteceu um erro do servidor em nuvem (Webservice). Tente novamente mais tarde, caso persista o erro entre em contato com suporte SAVARE");
                contentValues.put("dados", conexaoHttp.toString());

                final FuncoesPersonalizadas finalFuncoes = funcoes;
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        finalFuncoes.menssagem(contentValues);
                    }
                });
            }
        } catch (MalformedURLException e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } catch (ProtocolException e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } catch (IOException e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } catch (Exception e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } finally {
            if (conexaoHttp != null){
                // Desconecta com o webservice
                conexaoHttp.disconnect();
            }
        }
        return retorno;
    }


    public String executarWebserviceJson(ServidoresBeans servidor, String sql, String funcao, String metodo, String parametros, String parametrosUrl){
        String retorno = null;
        HttpURLConnection conexaoHttp = null;
        try {
            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Checa se retornou algum endereco de ip do servidor
            if ( (servidor == null) || ((servidor != null) && (servidor.getIpServidor().isEmpty()) && (servidor.getPorta() < 0)) ){
                servidor = new ServidoresBeans();
                servidor.setIpServidor(ServicosWeb.IP_SERVIDOR_WEBSERVICE);
                servidor.setPorta(Integer.valueOf(ServicosWeb.PORTA_JSON));
            }
            String enderecoWebService = "http://" + servidor.getIpServidor() + ":" + servidor.getPorta() + "/" + ServicosWeb.WS_ENDERECO_SISINFOWEB + funcao;

            Gson gson = new Gson();
            if (    (funcoes.getValorXml(funcoes.TAG_UUID_DISPOSITIVO).equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) ||
                    (funcoes.getValorXml(funcoes.TAG_DESCRICAO_DISPOSITIVO).equalsIgnoreCase(funcoes.TAG_DESCRICAO_DISPOSITIVO)) ){
                funcoes.setUuidDispositivo();
            }
            smadispoBeans = new SmadispoBeans();
            smadispoBeans.setIdentificacao(funcoes.getValorXml(funcoes.TAG_UUID_DISPOSITIVO));
            smadispoBeans.setDescricao(funcoes.getValorXml(funcoes.TAG_DESCRICAO_DISPOSITIVO));

            // Adiciona o dipositivo em formado json e codificado
            enderecoWebService += "?dispositivo=" + URLEncoder.encode(gson.toJson(smadispoBeans), "UTF-8");

            if((parametrosUrl) != null && (!parametrosUrl.isEmpty()) && (parametrosUrl.contains("&"))){
                enderecoWebService += URLEncoder.encode(parametrosUrl, "UTF-8").replace("%26", "&").replace("%3D", "=");
            }
            if ((metodo.equalsIgnoreCase(METODO_GET)) && (parametros != null) && (!parametros.isEmpty()) && (parametros.contains("&"))){
                //enderecoWebService += "/" + URLEncoder.encode(parametros, "UTF-8");
                enderecoWebService += URLEncoder.encode(parametros, "UTF-8").replace("%26", "&").replace("%3D", "=");
            }
            if ((metodo.equalsIgnoreCase(METODO_GET)) && (sql != null) && (!sql.isEmpty())){

                enderecoWebService += "&sqlQuery= " + URLEncoder.encode(sql, "UTF-8");
            }
            URL urlWebservice = new URL(enderecoWebService);

            conexaoHttp = (HttpURLConnection) urlWebservice.openConnection();
            // Indeca o metodo da conexao (GET, PUT, POST, DEL)
            conexaoHttp.setRequestMethod(metodo);
            conexaoHttp.setRequestProperty("Accept", "application/json");
            conexaoHttp.setRequestProperty("Content-Type", "application/json");
            conexaoHttp.setConnectTimeout(150000);
            conexaoHttp.setReadTimeout(150000);

            if ((metodo.equalsIgnoreCase(METODO_POST)) && (parametros != null) && (!parametros.isEmpty())){
                // Define que a conexão pode enviar informacoes e obte-las de volta
                conexaoHttp.setDoOutput(true);
                conexaoHttp.setDoInput(true);

                DataOutputStream dadosEnvio = new DataOutputStream(conexaoHttp.getOutputStream());
                // Salva o Json para envio
                dadosEnvio.writeBytes(parametros);
                dadosEnvio.flush();
            }
            conexaoHttp.connect();

            // Pega o codigo de retorno da comunicacao
            int codigoResp = conexaoHttp.getResponseCode();
            // Checa o codigo de retorno, se foi com sucesso
            if (codigoResp == HttpURLConnection.HTTP_OK) {

                //InputStream inputStream = new BufferedInputStream(conexaoHttp.getInputStream());
                BufferedReader buffeLeitura = new BufferedReader(new InputStreamReader(conexaoHttp.getInputStream()));
                StringBuilder dadosWebservice = new StringBuilder();

                String inputLine;
                while ((inputLine = buffeLeitura.readLine()) != null) {
                    dadosWebservice.append(inputLine + "\n");
                }
                buffeLeitura.close();

                return dadosWebservice.toString();
            }

            if (codigoResp == 404){
                // Armazena as informacoes para para serem exibidas e enviadas
                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "WSSisInfoWebservice");
                contentValues.put("mensagem", "Erro: 404. \nEndereço dos dados está errado. Tente novamente mais tarde, caso persista o erro entre em contato com suporte SAVARE. \n" +
                        "Função: " + funcao + " \n Metodo: " + metodo);
                contentValues.put("dados", conexaoHttp.toString());

                final FuncoesPersonalizadas finalFuncoes = funcoes;
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        finalFuncoes.menssagem(contentValues);
                    }
                });
            }

            if (codigoResp == 500){
                funcoes = new FuncoesPersonalizadas(context);

                // Armazena as informacoes para para serem exibidas e enviadas
                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "WSSisInfoWebservice");
                contentValues.put("mensagem", "Erro: 500. \nAconteceu um erro do servidor em Webservice. Tente novamente mais tarde, caso persista o erro entre em contato com suporte SAVARE");
                contentValues.put("dados", conexaoHttp.toString());

                final FuncoesPersonalizadas finalFuncoes = funcoes;
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        finalFuncoes.menssagem(contentValues);
                    }
                });
            }
        } catch (MalformedURLException e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } catch (ProtocolException e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } catch (final IOException e){
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    //funcoes.menssagem(contentValues);
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
                    new MaterialDialog.Builder(context)
                            .title("WSSisInfoWebservice")
                            .content(funcoes.tratamentoErroBancoDados(e.toString()))
                            .positiveText(context.getResources().getText(R.string.button_ok))
                            .show();

                }
            });
        } catch (Exception e){
            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });
        } finally {
            if (conexaoHttp != null){
                // Desconecta com o webservice
                conexaoHttp.disconnect();
            }
        }
        return retorno;
    }

}

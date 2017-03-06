package com.savare.webservice;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.savare.beans.DispositivoBeans;
import com.savare.beans.RetornoWebServiceBeans;
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
import java.util.Vector;

/**
 * Created by Bruno Nogueira Silva on 23/06/2016.
 */
public class WSSisinfoWebservice {

    private Context context;
    private DispositivoBeans dispositivoBeans;
    public static final String FUNCTION_SELECT_USUARIO_USUA = "selectUsuario";
    public static final String FUNCTION_JSON_SELECT_USUARIO_USUA = "/savare/selectUsuario";
    public static final String FUNCTION_SELECT_VERSAO_SAVARE = "selectVersaoSavare";
    public static final String FUNCTION_JSON_SELECT_VERSAO_SAVARE = "/savare/selectVersaoSavare";
    public static final String FUNCTION_SELECT_ULTIMA_ATUALIZACAO = "selectUltimaAtualizacao";
    public static final String FUNCTION_INSERT_ULTIMA_ATUALIZACAO = "insertUltimaAtualizacao";
    public static final String FUNCTION_SELECT_CFAAREAS = "selectAreas";
    public static final String FUNCTION_JSON_SELECT_CFAAREAS = "/clienteFornecedor/selectAreas";
    public static final String FUNCTION_SELECT_SMAEMPRE = "selectEmpresa";
    public static final String FUNCTION_JSON_SELECT_SMAEMPRE = "/sistema/selectEmpresa";
    public static final String FUNCTION_SELECT_CFAATIVI = "selectRamoAtividade";
    public static final String FUNCTION_JSON_SELECT_CFAATIVI = "/clienteFornecedor/selectRamoAtividade";
    public static final String FUNCTION_SELECT_CFASTATU = "selectStatusClifo";
    public static final String FUNCTION_JSON_SELECT_CFASTATU = "/clienteFornecedor/selectStatusClifo";
    public static final String FUNCTION_SELECT_CFATPDOC = "selectTipoDocumento";
    public static final String FUNCTION_JSON_SELECT_CFATPDOC = "/clienteFornecedor/selectTipoDocumento";
    public static final String FUNCTION_SELECT_CFACCRED = "selectCartaoCredito";
    public static final String FUNCTION_JSON_SELECT_CFACCRED = "/clienteFornecedor/selectCartaoCredito";
    public static final String FUNCTION_SELECT_CFAPORTA = "selectPortador";
    public static final String FUNCTION_JSON_SELECT_CFAPORTA = "/clienteFornecedor/selectPortador";
    public static final String FUNCTION_SELECT_CFAPROFI = "selectProfissao";
    public static final String FUNCTION_JSON_SELECT_CFAPROFI = "/clienteFornecedor/selectProfissao";
    public static final String FUNCTION_SELECT_CFATPCLI = "selectTipoCliente";
    public static final String FUNCTION_JSON_SELECT_CFATPCLI = "/clienteFornecedor/selectTipoCliente";
    public static final String FUNCTION_SELECT_CFATPCOB = "selectTipoCobranca";
    public static final String FUNCTION_JSON_SELECT_CFATPCOB = "/clienteFornecedor/selectTipoCobranca";
    public static final String FUNCTION_SELECT_CFAESTAD = "selectEstado";
    public static final String FUNCTION_JSON_SELECT_CFAESTAD = "/clienteFornecedor/selectEstado";
    public static final String FUNCTION_SELECT_CFACIDAD = "selectCidade";
    public static final String FUNCTION_JSON_SELECT_CFACIDAD = "/clienteFornecedor/selectCidade";
    public static final String FUNCTION_SELECT_CFACLIFO = "selectClienteFornecedor";
    public static final String FUNCTION_JSON_SELECT_CFACLIFO = "/clienteFornecedor/selectClienteFornecedor";
    public static final String FUNCTION_SELECT_CFAENDER = "selectEndereco";
    public static final String FUNCTION_JSON_SELECT_CFAENDER = "/clienteFornecedor/selectEndereco";
    public static final String FUNCTION_SELECT_CFAPARAM = "selectParametro";
    public static final String FUNCTION_JSON_SELECT_CFAPARAM = "/clienteFornecedor/selectParametro";
    public static final String FUNCTION_SELECT_CFAFOTOS = "selectFotos";
    public static final String FUNCTION_JSON_SELECT_CFAFOTOS = "clienteFornecedor/selectFotos";
    public static final String FUNCTION_SELECT_AEAPLPGT = "selectPlanoPagamento";
    public static final String FUNCTION_JSON_SELECT_AEAPLPGT = "/estoque/selectPlanoPagamento";
    public static final String FUNCTION_SELECT_AEACLASE = "selectClasseProdutos";
    public static final String FUNCTION_JSON_SELECT_AEACLASE = "/estoque/selectClasseProdutos";
    public static final String FUNCTION_SELECT_AEAUNVEN = "selectUnidadeVenda";
    public static final String FUNCTION_JSON_SELECT_AEAUNVEN = "/estoque/selectUnidadeVenda";
    public static final String FUNCTION_SELECT_AEAGRADE = "selectGrade";
    public static final String FUNCTION_JSON_SELECT_AEAGRADE = "/estoque/selectGrade";
    public static final String FUNCTION_SELECT_AEAMARCA = "selectMarca";
    public static final String FUNCTION_JSON_SELECT_AEAMARCA = "/estoque/selectMarca";
    public static final String FUNCTION_SELECT_AEACODST = "selectCodigoSituacaoTributaria";
    public static final String FUNCTION_JSON_SELECT_AEACODST = "/estoque/selectCodigoSituacaoTributaria";
    public static final String FUNCTION_SELECT_AEAPRODU = "selectProduto";
    public static final String FUNCTION_JSON_SELECT_AEAPRODU = "/estoque/selectProduto";
    public static final String FUNCTION_SELECT_AEAPRECO = "selectPreco";
    public static final String FUNCTION_JSON_SELECT_AEAPRECO = "/estoque/selectPreco";
    public static final String FUNCTION_SELECT_AEAEMBAL = "selectEmbalagemProduto";
    public static final String FUNCTION_JSON_SELECT_AEAEMBAL = "/estoque/selectEmbalagemProduto";
    public static final String FUNCTION_SELECT_AEAPLOJA = "selectProdutoPorLoja";
    public static final String FUNCTION_JSON_SELECT_AEAPLOJA = "/estoque/selectProdutoPorLoja";
    public static final String FUNCTION_SELECT_AEALOCES = "selectLocalEstoque";
    public static final String FUNCTION_JSON_SELECT_AEALOCES = "/estoque/selectLocalEstoque";
    public static final String FUNCTION_SELECT_AEAESTOQ = "selectEstoque";
    public static final String FUNCTION_JSON_SELECT_AEAESTOQ = "/estoque/selectEstoque";
    public static final String FUNCTION_SELECT_AEAORCAM = "selectOrcamento";
    public static final String FUNCTION_JSON_SELECT_AEAORCAM = "/orcamento/selectOrcamento";
    public static final String FUNCTION_SELECT_AEAITORC = "selectItemOrcamento";
    public static final String FUNCTION_SELECT_AEAPERCE = "selectPercentual";
    public static final String FUNCTION_JSON_SELECT_AEAPERCE = "/estoque/selectPercentual";
    public static final String FUNCTION_SELECT_AEAFATOR = "selectFator";
    public static final String FUNCTION_JSON_SELECT_AEAFATOR = "/estoque/selectFator";
    public static final String FUNCTION_SELECT_AEAPRREC = "selectProdutoRecomendado";
    public static final String FUNCTION_JSON_SELECT_AEAPRREC = "/estoque/selectProdutoRecomendado";
    public static final String FUNCTION_SELECT_RPAPARCE = "selectParcelas";
    public static final String FUNCTION_JSON_SELECT_RPAPARCE = "/receberPagar/selectParcelas";
    public static final String FUNCTION_INSERT_AEAORCAM = "insertOrcamento";
    public static final String FUNCTION_JSON_INSERT_AEAORCAM = "/orcamento/insertOrcamento";
    public static final String FUNCTION_INSERT_AEAITORC = "insertItemOrcamento";
    public static final String FUNCTION_UPDATE_STATUS_AEAORCAM = "updateStatusOrcamento";
    public static final String FUNCTION_CHECK_SEND_AEAORCAM = "checkSendOrcamento";
    public static final String METODO_GET = "GET";
    public static final String METODO_PUT = "PUT";
    public static final String METODO_POST = "POST";
    public static final String KEY_OBJECT_STATUS_RETORNO = "statusRetorno";
    public static final String KEY_OBJECT_OBJECT_RETORNO = "object";
    public static final String KEY_ELEMENT_CODIGO_RETORNO = "codigoRetorno";
    public static final String KEY_ELEMENT_MENSAGEM_RETORNO = "mensagemRetorno";
    public static final String KEY_ELEMENT_EXTRA_RETORNO = "extra";

    public WSSisinfoWebservice(Context context) {
        this.context = context;

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

        if ((!funcoes.getValorXml("ChaveUsuario").equalsIgnoreCase(funcoes.NAO_ENCONTRADO)) && (funcoes.getValorXml("ChaveUsuario").length() >= 36)) {

            // Instancia uma classe para pegar os dados do dispositivo
            dispositivoBeans = new DispositivoBeans();
            dispositivoBeans.setChaveUsuario(funcoes.getValorXml("ChaveUsuario"));
            dispositivoBeans.setNomeDispositivo(android.os.Build.MODEL + " - "+ android.os.Build.PRODUCT);
            dispositivoBeans.setSistemaOperacionalDispositivo(""+android.os.Build.VERSION.SDK_INT);
            dispositivoBeans.setNumeroSerialDispositivo(Build.SERIAL.replace("unknown", ""));
            dispositivoBeans.setMarcaDispositivo(android.os.Build.MANUFACTURER);
            dispositivoBeans.setIpHost(funcoes.getLocalIpAddress());

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            dispositivoBeans.setIdDispositivo(telephonyManager.getDeviceId());
            dispositivoBeans.setOperadoraDispositivo(telephonyManager.getSimOperatorName());
        }

    }

    /**
     *
     * @param listaPropriedades
     * @param funcao
     * @return
     */
    public RetornoWebServiceBeans executarWebservice(List<PropertyInfo> listaPropriedades, String funcao){
        RetornoWebServiceBeans retorno = new RetornoWebServiceBeans();
        try {
            SoapObject soap = new SoapObject(ServicosWeb.WS_NAME_SPACE, funcao);

            PropertyInfo propertyDispositivo = new PropertyInfo();
            propertyDispositivo.setName("dispositivo");
            propertyDispositivo.setValue(dispositivoBeans);
            propertyDispositivo.setType(dispositivoBeans.getClass());
            soap.addProperty(propertyDispositivo);

            if (listaPropriedades != null) {
                // Pega todas propriedades passado por paramentros
                for (PropertyInfo propriedadInfo : listaPropriedades) {
                    soap.addProperty(propriedadInfo);
                }
            }
            // Definicao da versao do protocolo do webservice, a forma de como os dados serao enviados
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            // Adiciona os objetos/propriedades
            envelope.setOutputSoapObject(soap);

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            String ipServidor = funcoes.getValorXml("IPServidorWebservice");

            // Checa se retornou algum endereco de ip do servidor
            if ((ipServidor == null) || (ipServidor.length() <= 1) || (ipServidor.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
                ipServidor = "localhost";
                //ipServidor = "172.16.0.4";
            }
            String enderecoWebService = "http://" + ipServidor + ":8080/" + ServicosWeb.WS_ENDERECO_WEBSERVICE;

            HttpTransportSE httpTransporte = new HttpTransportSE(enderecoWebService, 50000);

            // Requisicao dos dados
            httpTransporte.call(funcao, envelope);

            SoapObject response;

            if (envelope.getResponse() != null) {
                response = (SoapObject) envelope.getResponse();
                retorno.setCodigoRetorno(Integer.parseInt(response.getPropertyAsString("codigoRetorno")));
                retorno.setMensagemRetorno(response.getPropertyAsString("mensagemRetorno"));
                retorno.setExtra(response.getProperty("extra"));
            } else {
                return null;
            }
            return retorno;

        } catch (IOException e) {
            //e.printStackTrace();

            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());
            // Pega os dados do usuario

            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });

        } catch (XmlPullParserException e) {
            //e.printStackTrace();

            final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Armazena as informacoes para para serem exibidas e enviadas
            final ContentValues contentValues = new ContentValues();
            contentValues.put("comando", 0);
            contentValues.put("tela", "WSSisInfoWebservice");
            contentValues.put("mensagem", funcoes.tratamentoErroBancoDados(e.toString()));
            contentValues.put("dados", e.toString());
            // Pega os dados do usuario

            contentValues.put("usuario", funcoes.getValorXml("Usuario"));
            contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
            contentValues.put("email", funcoes.getValorXml("Email"));

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    funcoes.menssagem(contentValues);
                }
            });

        }
        return null;
    } // Fim executarWebservice

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
     *
     * @param sql
     * @param funcao
     * @param metodo
     * @param dadosJson - Os dados a serem passados ja tem que ser passado no formato Json
     * @return
     */
    public String executarSelectWebserviceJson(String sql, String funcao, String metodo, String dadosJson){
        String retorno = null;
        HttpURLConnection conexaoHttp = null;
        try {
            String ipServidor = ServicosWeb.IP_SERVIDOR_WEBSERVICE;

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);

            // Checa se retornou algum endereco de ip do servidor
            if ((ipServidor == null) || (ipServidor.length() <= 1) || (ipServidor.equalsIgnoreCase(funcoes.NAO_ENCONTRADO))){
                ipServidor = "localhost";
            }
            String enderecoWebService = "http://" + ipServidor + ((!ServicosWeb.PORTA_JSON.isEmpty()) ? (":" + ServicosWeb.PORTA_JSON + "/") : "/" ) + ServicosWeb.WS_ENDERECO_WEBSERVICE_JSON + funcao;

            if ((metodo.equalsIgnoreCase(METODO_GET)) || (metodo.equalsIgnoreCase(METODO_POST))){
                Gson gson = new Gson();
                // Adiciona o dipositivo em formado json e codificado
                enderecoWebService += "/" + URLEncoder.encode(gson.toJson(dispositivoBeans), "UTF-8");

                if ((metodo.equalsIgnoreCase(METODO_GET)) && (dadosJson != null) && (!dadosJson.isEmpty())){
                    enderecoWebService += "/" + URLEncoder.encode(dadosJson, "UTF-8");
                }
            }
            URL urlWebservice = new URL(enderecoWebService);

            conexaoHttp = (HttpURLConnection) urlWebservice.openConnection();
            // Indeca o metodo da conexao (GET, PUT, POST, DEL)
            conexaoHttp.setRequestMethod(metodo);
            conexaoHttp.setRequestProperty("Accept", "application/json");
            conexaoHttp.setRequestProperty("Content-Type", "application/json");
            //conexaoHttp.setRequestProperty("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.6,en;q=0.4");
            conexaoHttp.setConnectTimeout(100000);
            conexaoHttp.setReadTimeout(100000);

            if ((metodo.equalsIgnoreCase(METODO_POST)) && (dadosJson != null) && (!dadosJson.isEmpty())){
                // // Define que a conexão pode enviar informacoes e obte-las de volta
                conexaoHttp.setDoOutput(true);
                conexaoHttp.setDoInput(true);

                DataOutputStream dadosEnvio = new DataOutputStream(conexaoHttp.getOutputStream());
                // Salva o Json para envio
                dadosEnvio.writeBytes(dadosJson);
                dadosEnvio.flush();
            }
            conexaoHttp.connect();

            // Pega o codigo de retorno da comunicacao
            int codigoResp = conexaoHttp.getResponseCode();
            // Checa o codigo de retorno, se foi com sucesso
            if (codigoResp == 200) {

                //InputStream inputStream = new BufferedInputStream(conexaoHttp.getInputStream());
                BufferedReader buffeLeitura = new BufferedReader(new InputStreamReader(conexaoHttp.getInputStream()));
                StringBuilder dadosWebservice = new StringBuilder();

                String inputLine;
                while ((inputLine = buffeLeitura.readLine()) != null) {
                    dadosWebservice.append(inputLine + "\n");
                }
                buffeLeitura.close();

                retorno = dadosWebservice.toString();
            }

            if (codigoResp == 404){
                // Armazena as informacoes para para serem exibidas e enviadas
                final ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "WSSisInfoWebservice");
                contentValues.put("mensagem", "Erro: 404. \nEndereço dos dados está errado. Tente novamente mais tarde, caso persista o erro entre em contato com suporte SAVARE");
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
}

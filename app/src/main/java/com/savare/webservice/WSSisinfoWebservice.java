package com.savare.webservice;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.savare.beans.DispositivoBeans;
import com.savare.beans.RetornoWebServiceBeans;
import com.savare.configuracao.ServicosWeb;
import com.savare.funcoes.FuncoesPersonalizadas;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * Created by Bruno Nogueira Silva on 23/06/2016.
 */
public class WSSisinfoWebservice {

    private Context context;
    private DispositivoBeans dispositivoBeans;
    public static final String FUNCTION_SELECT_USUARIO_USUA = "selectUsuario";
    public static final String FUNCTION_SELECT_VERSAO_SAVARE = "selectVersaoSavare";
    public static final String FUNCTION_SELECT_ULTIMA_ATUALIZACAO = "selectUltimaAtualizacao";
    public static final String FUNCTION_INSERT_ULTIMA_ATUALIZACAO = "insertUltimaAtualizacao";
    public static final String FUNCTION_SELECT_CFAAREAS = "selectAreas";
    public static final String FUNCTION_SELECT_SMAEMPRE = "selectEmpresa";
    public static final String FUNCTION_SELECT_CFAATIVI = "selectRamoAtividade";
    public static final String FUNCTION_SELECT_CFASTATU = "selectStatusClifo";
    public static final String FUNCTION_SELECT_CFATPDOC = "selectTipoDocumento";
    public static final String FUNCTION_SELECT_CFACCRED = "selectCartaoCredito";
    public static final String FUNCTION_SELECT_CFAPORTA = "selectPortador";
    public static final String FUNCTION_SELECT_CFAPROFI = "selectProfissao";
    public static final String FUNCTION_SELECT_CFATPCLI = "selectTipoCliente";
    public static final String FUNCTION_SELECT_CFATPCOB = "selectTipoCobranca";
    public static final String FUNCTION_SELECT_CFAESTAD = "selectEstado";
    public static final String FUNCTION_SELECT_CFACIDAD = "selectCidade";
    public static final String FUNCTION_SELECT_CFACLIFO = "selectClienteFornecedor";
    public static final String FUNCTION_SELECT_CFAENDER = "selectEndereco";
    public static final String FUNCTION_SELECT_CFAPARAM = "selectParametro";
    public static final String FUNCTION_SELECT_CFAFOTOS = "selectFotos";
    public static final String FUNCTION_SELECT_AEAPLPGT = "selectPlanoPagamento";
    public static final String FUNCTION_SELECT_AEACLASE = "selectClasseProdutos";
    public static final String FUNCTION_SELECT_AEAUNVEN = "selectUnidadeVenda";
    public static final String FUNCTION_SELECT_AEAGRADE = "selectGrade";
    public static final String FUNCTION_SELECT_AEAMARCA = "selectMarca";
    public static final String FUNCTION_SELECT_AEACODST = "selectCodigoSituacaoTributaria";
    public static final String FUNCTION_SELECT_AEAPRODU = "selectProduto";
    public static final String FUNCTION_SELECT_AEAEMBAL = "selectEmbalagemProduto";
    public static final String FUNCTION_SELECT_AEAPLOJA = "selectProdutoPorLoja";
    public static final String FUNCTION_SELECT_AEALOCES = "selectLocalEstoque";
    public static final String FUNCTION_SELECT_AEAESTOQ = "selectEstoque";
    public static final String FUNCTION_SELECT_AEAORCAM = "selectOrcamento";
    public static final String FUNCTION_SELECT_AEAITORC = "selectItemOrcamento";
    public static final String FUNCTION_SELECT_AEAPERCE = "selectPercentual";
    public static final String FUNCTION_SELECT_AEAFATOR = "selectFator";
    public static final String FUNCTION_SELECT_AEAPRREC = "selectProdutoRecomendado";
    public static final String FUNCTION_SELECT_RPAPARCE = "selectParcelas";
    public static final String FUNCTION_INSERT_AEAORCAM = "insertOrcamento";
    public static final String FUNCTION_INSERT_AEAITORC = "insertItemOrcamento";

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

            if (envelope.getResponse() != null) {
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

}

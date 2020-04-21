package com.savare.activity.material.designer.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savare.R;
import com.savare.activity.material.designer.OrcamentoTabFragmentMDActivity;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;

/**
 * Created by Bruno Nogueira Silva on 12/04/2016.
 */
public class OrcamentoDescontoMDFragment extends Fragment {

    private View viewOrcamentoDesconto;
    private TextView textCodigoOrcamento,
            textTotalBruto,
            textValorDesconto,
            textAtacadoVarejo;
    private EditText editDescontoPercentual,
            editTotalLiquido;
    private ProgressBar progressBarStatus;
    private Toolbar toolbarRodape;
    private String tipoOrcamentoPedido;
    private double totalBruto = 0,
            totalLiquidoAuxiliar = 0,
            descontoPercentual = 0,
            totalBrutoAuxiliar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewOrcamentoDesconto = inflater.inflate(R.layout.fragment_orcamento_desconto_md, container, false);

        recuperaCamposTela();
        /**
         * Pega valores passados por parametro de outra Activity
         */
        Bundle parametro = getArguments();

        if (parametro != null) {
            // Seta o campo codigo consumo total com o que foi passado por parametro
            textCodigoOrcamento.setText(parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ID_ORCAMENTO));
            //codigoPessoa = parametro.getString("ID_CFACLIFO");
            //razaoSocial = parametro.getString("NOME_RAZAO");
            if(parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO).equals("0")){
                textAtacadoVarejo.setText("Atacado");

            }else if (parametro.getString(OrcamentoTabFragmentMDActivity.KEY_ATACADO_VAREJO).equals("1")){
                textAtacadoVarejo.setText("Varejo");
            }

        } else {
            // Dados da mensagem
            ContentValues mensagem = new ContentValues();
            mensagem.put("comando", 1);
            mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
            mensagem.put("mensagem", "Não foi carregar os dados do orçamento.\n");

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
            funcoes.menssagem(mensagem);
        }

        // Executa quando existe foco no campo
        editDescontoPercentual.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editDescontoPercentual.setText("");
                return false;
            }
        });


        // Executa toda vez que eh digitado alguma coisa no campo
        editDescontoPercentual.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    // Checa se o campo esta com foco
                    if(editDescontoPercentual.isFocused()){

                        calculaTodosCampos(editDescontoPercentual.getId());

                    }
                } catch (Exception e) {
                    // Instancia a classe da mensagem
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 0);
                    mensagem.put("tela", "OrcamentoDescontoMDActivity");
                    mensagem.put("mensagem", "Erro grave ao executar o addTextChangedListener do editDescontoPercentual. \n"
                            + e.getMessage() +"\n"
                            + getActivity().getResources().getString(R.string.favor_entrar_contato_responsavel_ti));
                    mensagem.put("dados", e.getMessage());
                    mensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    mensagem.put("email", funcoes.getValorXml("Email"));
                    // Montra a mensagem
                    funcoes.menssagem(mensagem);
                } // Fim catch
            } // onTextChanged

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,	int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }); // addTextChangedListener

        editDescontoPercentual.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    editTotalLiquido.setSelection(editTotalLiquido.getText().length());
                }

                return false;
            }
        });


        editTotalLiquido.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTotalLiquido.setText("");
                return false;
            }
        });
        editTotalLiquido.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (editTotalLiquido.isFocused()) {
                        // Calcula os totais de acordo com o digitado
                        calculaTodosCampos(editTotalLiquido.getId());
                    }

                } catch (Exception e) {
                    // Instancia a classe da mensagem
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

                    // Dados da mensagem
                    ContentValues mensagem = new ContentValues();
                    mensagem.put("comando", 0);
                    mensagem.put("tela", "OrcamentoDescontoMDActivity");
                    mensagem.put("mensagem", "Erro grave ao executar o addTextChangedListener do editTotalLiquido. \n"
                            + e.getMessage() + "\n"
                            + getActivity().getResources().getString(R.string.favor_entrar_contato_responsavel_ti));
                    mensagem.put("dados", e.getMessage());
                    mensagem.put("usuario", funcoes.getValorXml("Usuario"));
                    mensagem.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                    mensagem.put("email", funcoes.getValorXml("Email"));
                    // Montra a mensagem
                    funcoes.menssagem(mensagem);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTotalLiquido.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    //InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    // Fecha o teclado
                    //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getContext());
                    funcoes.fecharTecladoVirtual();
                }
                return false;
            }
        });


        toolbarRodape.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){

                    case R.id.menu_orcamento_desconto_fragment_md_salvar:
                        // Checa se eh um orcamento
                        if (tipoOrcamentoPedido.equals("O")){

                            SalvarDescontoOrcamento salvarDescontoOrcamento = new SalvarDescontoOrcamento(textCodigoOrcamento.getText().toString());
                            salvarDescontoOrcamento.execute();

                        } else {
                            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                            // Cria uma variavem para inserir as propriedades da mensagem
                            ContentValues mensagem = new ContentValues();
                            mensagem.put("comando", 2);
                            mensagem.put("tela", "OrcamentoDescontoFragment");
                            mensagem.put("mensagem", getActivity().getResources().getString(R.string.nao_orcamento) + "\n" +
                                    getActivity().getResources().getString(R.string.nao_possivel_inserir_alterar_orcamento));
                            // Executa a mensagem passando por parametro as propriedades
                            funcoes.menssagem(mensagem);
                        }
                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        return viewOrcamentoDesconto;
    } // Fim do onCreate

    @Override
    public void onResume() {
        super.onResume();

        CarregarDescontoOrcamento carregarDescontoOrcamento = new CarregarDescontoOrcamento(textCodigoOrcamento.getText().toString());
        carregarDescontoOrcamento.execute();
    }

    /**
     * Acossia os campos da view com as variaves criadas para manipula-las.
     *
     */
    private void recuperaCamposTela() {
        textCodigoOrcamento = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_text_codigo_orcamento);
        textTotalBruto = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_text_total_bruto);
        textValorDesconto = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_text_valor_desconto);
        textAtacadoVarejo = (TextView) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_text_atacado_varejo);
        editDescontoPercentual = (EditText) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_edit_desconto);
        editTotalLiquido = (EditText) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_edit_total_liquido);
        progressBarStatus = (ProgressBar) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_progressBar_status);
        toolbarRodape = (Toolbar) viewOrcamentoDesconto.findViewById(R.id.fragment_orcamento_desconto_md_toolbar_rodape);
        toolbarRodape.inflateMenu(R.menu.orcamento_desconto_fragment_md);
    }


    private void calculaTodosCampos(int campoChamada){
        double percentualDesconto = 0,
                totalLiquido = 0;

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

        // Checa se tem algum valor no percentual de desconto
        if((editDescontoPercentual != null) && (editDescontoPercentual.getText().length() > 0)){
            percentualDesconto = funcoes.desformatarValor(editDescontoPercentual.getText().toString());
        }

        // Checa se tem algum valor no campo de total liquido
        if((editTotalLiquido != null) && (editTotalLiquido.getText().length() > 0)){
            totalLiquido = funcoes.desformatarValor(editTotalLiquido.getText().toString());
        }

        // Checa se o campo que chamou esta funcao foi o campo editDescontoPercentual
        if(campoChamada == editDescontoPercentual.getId()){
            // Calcula o total liquido
            totalLiquido = totalBrutoAuxiliar - (totalBrutoAuxiliar * (percentualDesconto / 100));

            // Armazena o total liquido em uma vareavel auxiliar
            this.totalLiquidoAuxiliar = totalLiquido;

            // Seta o valor do campo total liquido com o valor calculao com o desconto
            editTotalLiquido.setText(funcoes.arredondarValor(totalLiquido));
            textValorDesconto.setText(funcoes.arredondarValor(totalBrutoAuxiliar - totalLiquido));
        }

        // Checa se o campo que chomou esta funcao foi o campo editTotalLiquido
        if(campoChamada == editTotalLiquido.getId()){
            // Calcula o percentual de desconto utilizado
            percentualDesconto = (((totalLiquido / totalBrutoAuxiliar) * 100) - 100) * -1;

            this.totalLiquidoAuxiliar = totalLiquido;

            editDescontoPercentual.setText(funcoes.arredondarValor(percentualDesconto));
            textValorDesconto.setText(funcoes.arredondarValor(totalBrutoAuxiliar - totalLiquido));
        }

    }


    public class CarregarDescontoOrcamento extends AsyncTask<Void, Void, Void> {

        private String codigoOrcamento = null;

        public CarregarDescontoOrcamento(String codigoOrcamento) {
            this.codigoOrcamento = codigoOrcamento;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Instancia a classe de rotinas
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

            // Pega o valor total bruto do orcamento
            totalBruto = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoBruto(codigoOrcamento));
            totalBrutoAuxiliar = totalBruto;

            // Pega o percentual de desconto aplicado no total do orcamento
            descontoPercentual = funcoes.desformatarValor(orcamentoRotinas.descontoPercentualOrcamento(codigoOrcamento));

            // Pega o valor total liquido
            totalLiquidoAuxiliar = funcoes.desformatarValor(orcamentoRotinas.totalOrcamentoLiquido(codigoOrcamento));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());

            // Mostra o total bruto na tela
            textTotalBruto.setText(funcoes.arredondarValor(totalBruto));

            // Mostra o percentual de desconto na tela
            editDescontoPercentual.setText(funcoes.arredondarValor(descontoPercentual));

            // Move o cursor para o final do campo
            editDescontoPercentual.setSelection(editDescontoPercentual.getText().length());

            // Montra o total liquido na tela
            editTotalLiquido.setText(funcoes.arredondarValor(totalLiquidoAuxiliar));

            // Instancia a classe de rotinas
            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

            // Pega o status do orcamento
            tipoOrcamentoPedido = orcamentoRotinas.statusOrcamento(textCodigoOrcamento.getText().toString());

            progressBarStatus.setVisibility(View.GONE);
        }
    }


    public class SalvarDescontoOrcamento extends AsyncTask<Void, Void, Void> {
        private String idOrcamento;
        private ProgressDialog progress;

        public SalvarDescontoOrcamento(String idOrcamento) {
            this.idOrcamento = idOrcamento;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setIndeterminate(true);

            //Cria novo um ProgressDialogo e exibe
            progress = new ProgressDialog(getActivity());
            progress.setMessage(getContext().getResources().getString(R.string.aguarde_distribuir_desconto_item_orcamento));
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(getActivity());

            if(orcamentoRotinas.distribuiDescontoItemOrcamento(idOrcamento, totalLiquidoAuxiliar, totalBrutoAuxiliar)){

                final ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 2);
                mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
                mensagem.put("mensagem", "Desconto Distribuido com sucesso.");

                final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        funcoes.menssagem(mensagem);
                    }
                });
                // Executa o onResume (todos os campos de valores)
                onResume();

            } else {
                final ContentValues mensagem = new ContentValues();
                mensagem.put("comando", 2);
                mensagem.put("tela", "OrcamentoPlanoPagamentoActivity");
                mensagem.put("mensagem", "Não foi possível salvar os desconto e o plano de pagamento");

                final FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(getActivity());
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        funcoes.menssagem(mensagem);
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressBarStatus.setVisibility(View.GONE);
            progress.dismiss();
        }
    }
}

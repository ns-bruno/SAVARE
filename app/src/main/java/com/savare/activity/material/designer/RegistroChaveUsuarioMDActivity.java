package com.savare.activity.material.designer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;
import com.savare.R;
import com.savare.activity.material.designer.fragment.LoginFragment;
import com.savare.activity.material.designer.fragment.RecebeDadosWebserviceFragment;
import com.savare.configuracao.ConfiguracoesInternas;
import com.savare.funcoes.FuncoesPersonalizadas;

import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

/**
 * Created by Bruno Nogueira Silva on 02/08/2016.
 */
public class RegistroChaveUsuarioMDActivity extends IntroActivity {

    boolean scrollable = true,
            skipEnabled = true,
            finishEnabled = true,
            showBack = false,
            showNext = true,
            getStartedEnabled = false;
    private String chaveUsuario = "";
    public static final String KEY_CHAVE_USUARIO = "keyChaveusuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         /* Enable/disable fullscreen */
        setFullscreen(true);

        super.onCreate(savedInstanceState);

        setButtonBackFunction(skipEnabled ? BUTTON_BACK_FUNCTION_SKIP : BUTTON_BACK_FUNCTION_BACK);
        setButtonNextFunction(finishEnabled ? BUTTON_NEXT_FUNCTION_NEXT_FINISH : BUTTON_NEXT_FUNCTION_NEXT);
        setButtonBackVisible(showBack);
        setButtonCtaLabel(R.string.proximo);
        setButtonNextVisible(showNext);
        setButtonCtaVisible(getStartedEnabled);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.primeiros_passos_iniciar_savare)
                .description(R.string.primeiro_passo)
                .image(R.drawable.ic_launcher)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.md_indigo_500)
                .scrollable(scrollable)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.registrar_voce_como_usuario)
                .description(R.string.orientacao_registrar_usuario)
                //.image(R.drawable.ic_account_key)
                .background(R.color.md_red_A700)
                .backgroundDark(R.color.md_red_A400)
                .scrollable(scrollable)
                .buttonCtaLabel(R.string.tenho_chave_licenca)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new ZxingOrient(RegistroChaveUsuarioMDActivity.this)
                                .setInfo(getResources().getString(R.string.escanear_codigo_barras))
                                .setVibration(true)
                                .setIcon(R.mipmap.ic_launcher)
                                .initiateScan();

                        //nextSlide();
                    }
                })
                .build());

        Slide loginSlide = new FragmentSlide.Builder()
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.md_indigo_500)
                .fragment(LoginFragment.newInstance())
                .build();
        addSlide(loginSlide);

        /*addSlide(new FragmentSlide.Builder()
                .background(R.color.color_custom_fragment_2)
                .backgroundDark(R.color.color_dark_custom_fragment_2)
                .fragment(R.layout.fragment_custom, R.style.AppThemeDark)
                .build()); */

        addSlide(new SimpleSlide.Builder()
                .title(R.string.terminamos_primeiros_passos)
                .description(R.string.pronto_para_iniciar_savare)
                .image(R.drawable.ic_launcher)
                .background(R.color.md_deep_purple_A700)
                .backgroundDark(R.color.md_purple_A400)
                .scrollable(scrollable)
                .build());

        Slide receberDadosSlide = new FragmentSlide.Builder()
                .background(R.color.md_deep_orange_900)
                .backgroundDark(R.color.md_deep_orange_500)
                .fragment(RecebeDadosWebserviceFragment.newInstance())
                .build();
        addSlide(receberDadosSlide);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(RegistroChaveUsuarioMDActivity.this);
        funcoes.setValorXml("ChaveUsuario", ConfiguracoesInternas.CHAVE_USUARIO_PUBLICO_WEBSERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZxingOrientResult retornoEscanerCodigoBarra = ZxingOrient.parseActivityResult(requestCode, resultCode, data);

        if(retornoEscanerCodigoBarra != null) {
            // Checha se retornou algum dado
            if(retornoEscanerCodigoBarra.getContents() == null) {
                //Log.d("SAGA", "Cancelled scan - CadastroEmbalagemActivity");
                SuperToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.cancelado), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();

            } else {
                //Log.d("SAGA", "Scanned - CadastroEmbalagemActivity");

                // Pega a chave retornado pelo leitor de codigo de barras
                chaveUsuario = retornoEscanerCodigoBarra.getContents();

                if (chaveUsuario.length() >= 36) {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(RegistroChaveUsuarioMDActivity.this);
                    funcoes.setValorXml("ChaveUsuario", chaveUsuario);
                } else {
                    SuperToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.tamanho_chave_nao_permitido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                }

                // Vai para o proximo slide
                nextSlide();
            }
        } else {
            // This is important, otherwise the retornoEscanerCodigoBarra will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

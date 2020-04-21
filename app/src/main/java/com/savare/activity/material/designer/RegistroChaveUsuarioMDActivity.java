package com.savare.activity.material.designer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.github.clans.fab.FloatingActionButton;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;
import com.savare.R;
import com.savare.activity.material.designer.fragment.ChaveUsuarioFragment;
import com.savare.activity.material.designer.fragment.ListaServidoresWebserviceMDFragment;
import com.savare.activity.material.designer.fragment.LoginFragment;
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
    private FloatingActionButton itemMenuNovoServidor;
    private String chaveUsuario = "";
    private String cnpjEmpresa = "";
    public static final String KEY_CHAVE_USUARIO = "keyChaveusuario";
    public static final String KEY_CNPJ_EMPRESA = "keyCnpjEmpresa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         /* Enable/disable fullscreen */
        setFullscreen(true);


        setButtonBackFunction(skipEnabled ? BUTTON_BACK_FUNCTION_SKIP : BUTTON_BACK_FUNCTION_BACK);
        setButtonNextFunction(finishEnabled ? BUTTON_NEXT_FUNCTION_NEXT_FINISH : BUTTON_NEXT_FUNCTION_NEXT);
        setButtonBackVisible(showBack);
        setButtonCtaLabel(R.string.proximo);
        setButtonNextVisible(showNext);
        setButtonCtaVisible(getStartedEnabled);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(RegistroChaveUsuarioMDActivity.this);

        funcoes.bloqueiaOrientacaoTela();

        addSlide(new SimpleSlide.Builder()
                .title(R.string.primeiros_passos_iniciar_savare)
                .description(R.string.primeiro_passo)
                .image(R.mipmap.ic_launcher)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.md_indigo_500)
                .scrollable(scrollable)
                .build());

        Slide chaveSlide = new FragmentSlide.Builder()
                .background(R.color.md_red_A700)
                .backgroundDark(R.color.md_red_A400)
                .fragment(ChaveUsuarioFragment.newInstance())
                .build();
        addSlide(chaveSlide);

        Slide servidoresSlide = new FragmentSlide.Builder()
                .background(R.color.md_indigo_500)
                .backgroundDark(R.color.md_indigo_500)
                .fragment(ListaServidoresWebserviceMDFragment.newInstance())
                .build();
        addSlide(servidoresSlide);


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
                .image(R.mipmap.ic_launcher)
                .background(R.color.md_deep_purple_A700)
                .backgroundDark(R.color.md_purple_A400)
                .scrollable(scrollable)
                .build());

        // Verifica se esta liberados as permicoes necessarias
        if (!funcoes.hasPermissions(LoginMDActivity.REQUIRED_PERMISSIONS)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(RegistroChaveUsuarioMDActivity.this, LoginMDActivity.REQUIRED_PERMISSIONS, LoginMDActivity.REQUEST_APP_SETTINGS);
            }
        }

    } // Fim onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZxingOrientResult retornoEscanerCodigoBarra = ZxingOrient.parseActivityResult(requestCode, resultCode, data);

        if(retornoEscanerCodigoBarra != null) {
            // Checha se retornou algum dado
            if(retornoEscanerCodigoBarra.getContents() == null) {
                //Log.d("SAGA", "Cancelled scan - CadastroEmbalagemActivity");
                //SuperToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.cancelado), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();

                SuperActivityToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.cancelado), Style.DURATION_LONG)
                        .setTextColor(Color.WHITE)
                        .setColor(Color.RED)
                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();

            } else {
                //Log.d("SAGA", "Scanned - CadastroEmbalagemActivity");

                // Pega a chave retornado pelo leitor de codigo de barras
                String cnpj = retornoEscanerCodigoBarra.getContents();

                if (cnpj.length() >= 11) {
                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(RegistroChaveUsuarioMDActivity.this);
                    funcoes.setValorXml(funcoes.TAG_CNPJ_EMPRESA, cnpj);

                    //SuperToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.cnpj_salva_sucesso), SuperToast.Duration.LONG, Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();
                    SuperActivityToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.cnpj_salva_sucesso), Style.DURATION_LONG)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                } else {
                    //SuperToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
                    SuperActivityToast.create(RegistroChaveUsuarioMDActivity.this, getResources().getString(R.string.tamanho_cnpj_cpf_nao_permitido), Style.DURATION_LONG)
                            .setTextColor(Color.WHITE)
                            .setColor(Color.RED)
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }

                // Vai para o proximo slide
                nextSlide();
            }
        } else {
            // This is important, otherwise the retornoEscanerCodigoBarra will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(RegistroChaveUsuarioMDActivity.this);
        funcoes.desbloqueiaOrientacaoTela();
    }
}

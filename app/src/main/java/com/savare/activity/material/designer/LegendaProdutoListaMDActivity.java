package com.savare.activity.material.designer;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.savare.R;

public class LegendaProdutoListaMDActivity extends IntroActivity {

    boolean scrollable = true,
            skipEnabled = true,
            finishEnabled = true,
            showBack = true,
            showNext = true,
            getStartedEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setFullscreen(false);

        setButtonBackFunction(skipEnabled ? BUTTON_BACK_FUNCTION_SKIP : BUTTON_BACK_FUNCTION_BACK);
        setButtonNextFunction(finishEnabled ? BUTTON_NEXT_FUNCTION_NEXT_FINISH : BUTTON_NEXT_FUNCTION_NEXT);
        setButtonBackVisible(showBack);
        setButtonCtaLabel(R.string.proximo);
        setButtonNextVisible(showNext);
        setButtonCtaVisible(getStartedEnabled);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);

        // Produto em promocao (sale)
        addSlide(new SimpleSlide.Builder()
                .title(R.string.produto_promocao)
                .description(R.string.legenda_promocao)
                .image(R.mipmap.sale_example)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.amarelo)
                .scrollable(scrollable)
                .build());

        // Produto ja adicionado no orcamento
        addSlide(new SimpleSlide.Builder()
                .title(R.string.produto_adicionado_orcamento)
                .description(R.string.legenda_produto_adicionado_orcamento)
                .image(R.mipmap.check_verde)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.verde)
                .scrollable(scrollable)
                .build());

        // Produto sem estoque contabil
        addSlide(new SimpleSlide.Builder()
                .title(R.string.sem_estoque_contabil)
                .description(R.string.legenda_produto_sem_estoque_contabil)
                .image(R.mipmap.not_stock_accounting)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.vermelho_escuro)
                .scrollable(scrollable)
                .build());

        // Produto novo
        addSlide(new SimpleSlide.Builder()
                .title(R.string.produto_novo)
                .description(R.string.legenda_produto_novo)
                .image(R.mipmap.new_product)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.azul_medio_500)
                .scrollable(scrollable)
                .build());

        // Produto sem estoque
        addSlide(new SimpleSlide.Builder()
                .title(R.string.produto_sem_estoque)
                .description(R.string.legenda_produto_sem_estoque)
                .image(R.mipmap.not_stock)
                .background(R.color.md_indigo_900)
                .backgroundDark(R.color.vermelho_escuro)
                .scrollable(scrollable)
                .build());
    }
}

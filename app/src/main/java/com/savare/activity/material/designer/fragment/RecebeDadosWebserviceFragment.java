package com.savare.activity.material.designer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.savare.R;
import com.savare.funcoes.rotinas.async.ReceberDadosWebserviceAsyncRotinas;

/**
 * Created by Bruno Nogueira Silva on 08/09/2016.
 */
public class RecebeDadosWebserviceFragment extends SlideFragment {

    private ProgressBar progressBarStatus;
    private TextView textStatus;

    public RecebeDadosWebserviceFragment(){

    }

    public static RecebeDadosWebserviceFragment newInstance() {
        return new RecebeDadosWebserviceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_recebe_dados_webservice, container, false);

        progressBarStatus = (ProgressBar) root.findViewById(R.id.fragment_recebe_dados_webservice_progressBar_status);
        textStatus = (TextView) root.findViewById(R.id.fragment_recebe_dados_webservice_text_status);

        ReceberDadosWebserviceAsyncRotinas receberDadosWebservice = new ReceberDadosWebserviceAsyncRotinas(new ReceberDadosWebserviceAsyncRotinas.OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {

            }
        },
        getContext());

        receberDadosWebservice.setProgressBarStatus(progressBarStatus);
        receberDadosWebservice.setTextStatus(textStatus);
        receberDadosWebservice.execute();

        return root;
    }
}

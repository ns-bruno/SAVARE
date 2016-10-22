package com.savare.funcoes.rotinas.receptor;

import com.savare.R;
import com.savare.activity.InicioActivity;
import com.savare.activity.LoginActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ReceptorNotificarAparelhoRotinas extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String ticker = intent.getExtras().getString("TICKER");
		String titulo = intent.getExtras().getString("TITULO");
		String mensagem = intent.getExtras().getString("MENSAGEM");
		
		if((ticker == null) || (ticker.length() <= 0)){
			ticker = "Notificação SAVARE";
		}
		
		if((titulo == null) || (titulo.length() <= 0)){
			titulo = "Nova Notificação do SAVARE";
		}
		
		if((mensagem == null) || (mensagem.length() <= 0)){
			mensagem = "O SAVARE executaou mais uma tarefa em segundo plano";
		}
		
		gerarNotificacao(context, intent, ticker, titulo, mensagem);
	}
	
	
	
	public void gerarNotificacao(Context context, Intent intent, CharSequence ticker, CharSequence titulo, CharSequence descricao){
		intent.setClass(context, InicioActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent p = PendingIntent.getActivity(context, 0, intent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setTicker(ticker);
		builder.setContentTitle(titulo);
		builder.setContentText(descricao);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
		builder.setContentIntent(p);
		
		Notification n = builder.build();
		n.vibrate = new long[]{150, 300, 150, 600};
		n.flags = Notification.FLAG_AUTO_CANCEL;
		nm.notify(R.mipmap.ic_launcher, n);
		
		try{
			Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone toque = RingtoneManager.getRingtone(context, som);
			toque.play();
		}
		catch(Exception e){
			Log.i("Script", e.getMessage());
		}
	}

}

package com.savare.funcoes;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocalizacaoFuncoes {

	private Context context;
	private double latitude,
				   longitude,
				   altitude;
	private long horarioLocalizacao;
	private String tipoLocalizacao;
	private float precisao;
	private boolean gpsAtivo = false,
					networkAtivo = false;
	private Timer tempo;
	LocationManager locationManager;
	Location localizacao;
	
	
	public LocalizacaoFuncoes(Context context) {
		this.context = context;
		pegarLocalizacao();
	}
	
	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the horarioLocalizacao
	 */
	public long getHorarioLocalizacao() {
		return horarioLocalizacao;
	}

	/**
	 * @param horarioLocalizacao the horarioLocalizacao to set
	 */
	public void setHorarioLocalizacao(long horarioLocalizacao) {
		this.horarioLocalizacao = horarioLocalizacao;
	}

	/**
	 * @return the tipoLocalizacao
	 */
	public String getTipoLocalizacao() {
		return tipoLocalizacao;
	}

	/**
	 * @param tipoLocalizacao the tipoLocalizacao to set
	 */
	public void setTipoLocalizacao(String tipoLocalizacao) {
		this.tipoLocalizacao = tipoLocalizacao;
	}

	/**
	 * @return the precisao
	 */
	public float getPrecisao() {
		return precisao;
	}

	/**
	 * @param precisao the precisao to set
	 */
	public void setPrecisao(float precisao) {
		this.precisao = precisao;
	}

	/**
	 * @return the gpsAtivo
	 */
	public boolean isGpsAtivo() {
		return gpsAtivo;
	}

	/**
	 * @param gpsAtivo the gpsAtivo to set
	 */
	public void setGpsAtivo(boolean gpsAtivo) {
		this.gpsAtivo = gpsAtivo;
	}

	/**
	 * @return the networkAtivo
	 */
	public boolean isNetworkAtivo() {
		return networkAtivo;
	}

	/**
	 * @param networkAtivo the networkAtivo to set
	 */
	public void setNetworkAtivo(boolean networkAtivo) {
		this.networkAtivo = networkAtivo;
	}

	
	public Location pegarLocalizacao(){
		locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		
		try {
			gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			networkAtivo = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception e) {
			
		}
		
		/*if((!gpsAtivo) && (!networkAtivo)){
			return false;
		}*/
		
		if(gpsAtivo){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
			
			if(locationManager != null){
				localizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
				// Checa se retornou alguma localizacao
				if(localizacao != null){
					setLatitude(localizacao.getLatitude());
					setLongitude(localizacao.getLongitude());
					setAltitude(localizacao.getAltitude());
					setHorarioLocalizacao(localizacao.getTime());
					setTipoLocalizacao(localizacao.getProvider());
					setPrecisao(localizacao.getAccuracy());
				}
			}
		}
		
		if(networkAtivo){
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
			
			if(locationManager != null){
				localizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
				// Checa se retornou alguma localizacao
				if(localizacao != null){
					setLatitude(localizacao.getLatitude());
					setLongitude(localizacao.getLongitude());
					setAltitude(localizacao.getAltitude());
					setHorarioLocalizacao(localizacao.getTime());
					setTipoLocalizacao(localizacao.getProvider());
					setPrecisao(localizacao.getAccuracy());
				}
			}
		}
		
		/*locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER , new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// Checa se retornou alguma localizacao
				if(location != null){
					setLatitude(location.getLatitude());
					setLongitude(location.getLongitude());
					setAltitude(location.getAltitude());
					setHorarioLocalizacao(location.getTime());
					setTipoLocalizacao(location.getProvider());
					setPrecisao(location.getAccuracy());
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				SuperToast.create(getContext(), getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.FLYIN)).show();
			}
			@Override
			public void onProviderEnabled(String provider) {
				SuperToast.create(getContext(), getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.FLYIN)).show();
			}
			@Override
			public void onProviderDisabled(String provider) {
				SuperToast.create(getContext(), getResources().getString(R.string.opcao_positivacao_nao_valida_para_esta_tela), SuperToast.Duration.LONG, Style.getStyle(Style.RED, SuperToast.Animations.FLYIN)).show();
			}
			
		}, null);*/
		
		
		return localizacao;
	}

	
	LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
        	
        	//tempo.cancel();
        	
        	// Checa se retornou alguma localizacao
			if(location != null){
				setLatitude(location.getLatitude());
				setLongitude(location.getLongitude());
				setAltitude(location.getAltitude());
				setHorarioLocalizacao(location.getTime());
				setTipoLocalizacao(location.getProvider());
				setPrecisao(location.getAccuracy());
			}
			
			locationManager.removeUpdates(locationListenerGps);
			locationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
	
    
    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            
        	//tempo.cancel();
            
        	// Checa se retornou alguma localizacao
			if(location != null){
				setLatitude(location.getLatitude());
				setLongitude(location.getLongitude());
				setAltitude(location.getAltitude());
				setHorarioLocalizacao(location.getTime());
				setTipoLocalizacao(location.getProvider());
				setPrecisao(location.getAccuracy());
			}
        	//locationResult.gotLocation(location);
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
	
    /**
     * Pega os valores da ultima localizacao.
     * 
     */
    public void getUltimaLocalizacao(){
    	Location localizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	if(localizacao != null){
    		setLatitude(localizacao.getLatitude());
			setLongitude(localizacao.getLongitude());
			setAltitude(localizacao.getAltitude());
			setHorarioLocalizacao(localizacao.getTime());
			setTipoLocalizacao(localizacao.getProvider());
			setPrecisao(localizacao.getAccuracy());
    	}
    	locationManager.removeUpdates(locationListenerGps);
    	locationManager.removeUpdates(locationListenerNetwork);
    }
	/*class GetLastLocation extends TimerTask {
	    @Override
	    public void run() {
	         locationManager.removeUpdates(locationListenerGps);
	         locationManager.removeUpdates(locationListenerNetwork);

	         Location net_loc = null, gps_loc = null;
	         if(gpsAtivo)
	             gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	         if(networkAtivo)
	             net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

	         //se tiver os dois valores, usar o mais atualizado
	         if(gps_loc!=null && net_loc!=null){
	             if(gps_loc.getTime()>net_loc.getTime())
	                 locationResult.gotLocation(gps_loc);
	             else
	                 locationResult.gotLocation(net_loc);
	             return;
	         }

	         if(gps_loc!=null){
	             locationResult.gotLocation(gps_loc);
	             return;
	         }
	         if(net_loc!=null){
	             locationResult.gotLocation(net_loc);
	             return;
	         }
	         locationResult.gotLocation(null);
	    }
	} // Fim GetLastLocation
*/}



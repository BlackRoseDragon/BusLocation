package mashup.com.buslocation;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String idSocket = "";
    private String idUser = "";
    private String userName = "";
    private double latitud = 0.0;
    private double longitud = 0.0;
    private Marker marcador;
    private double distancia = 0.0;

    public Usuario(String idUser, String userName) {
        this.idUser = idUser;
        this.userName = userName;
    }

    public String getIdSocket() {
        return idSocket;
    }

    public void setIdSocket(String idSocket) {
        this.idSocket = idSocket;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Marker getMarcador() {
        return marcador;
    }

    public void setMarcadorMapa(GoogleMap mMap) {
        this.marcador = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(this.latitud, this.longitud))
                .title(this.userName)
                .snippet("Mis Coordenadas: " + this.latitud + ", " + this.longitud));
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(GoogleMap mMap, LatLng distanciaMarcador) {
        this.distancia = SphericalUtil.computeDistanceBetween(distanciaMarcador, new LatLng(this.latitud, this.longitud));
        this.marcador = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(this.latitud, this.longitud))
                .title(this.userName)
                .snippet("Distancia: " + this.distancia + " en Metros."));
    }
}
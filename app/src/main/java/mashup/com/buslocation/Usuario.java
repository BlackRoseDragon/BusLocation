package mashup.com.buslocation;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Usuario {
    private String idUsuario = "";
    private double latitud = 0.0;
    private double longitud = 0.0;
    private Marker marcador;

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
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

    public void setMarcador(Marker marcador) {
        this.marcador = marcador;
    }

    public void setPosicion(double latitud, double longitud) {
        marcador.setPosition(new LatLng(latitud, longitud));
        marcador.setSnippet(idUsuario + ": " + latitud + ", " + longitud);
    }
}
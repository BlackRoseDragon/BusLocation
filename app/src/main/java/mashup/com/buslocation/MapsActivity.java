package mashup.com.buslocation;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static mashup.com.buslocation.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Marker marcador;

    private LinkedList<Usuario> listaUsuarios = new LinkedList();

    private TextView textview_coordenadas;

    private Button gps_button;

    private ToggleButton bluetooth_toggle_button;

    private BluetoothAdapter adaptadorBluetooth;

    private LocationManager locationManager;

    private LocationListener locationListener;

    private static final UUID ESTIMOTE_PROXIMITY_UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("rid", ESTIMOTE_PROXIMITY_UUID, null, null);

    BeaconManager beaconManager;

    public void BeaconManager() {
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, final List beacons) {
                //Log.d(TAG, "Ranged beacons: " + beacons);
                Toast.makeText(getApplicationContext(), "Ranged beacons: " + beacons.size(), Toast.LENGTH_LONG).show();
            }
        });

        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override public void onNearablesDiscovered(List nearables) {
                //Log.d(TAG, "Discovered nearables: " + nearables);
                Toast.makeText(getApplicationContext(), "Discovered nearables: " + nearables.size(), Toast.LENGTH_LONG).show();
            }
        });

        beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override public void onEddystonesFound(List eddystones) {
                //Log.d(TAG, "Nearby eddystones: " + eddystones);
                Toast.makeText(getApplicationContext(), "Nearby eddystones: " + eddystones.size(), Toast.LENGTH_LONG).show();
            }
        });
        connectToService();
    }

    public void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {

                // Beacons ranging.
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);

                // Nearable discovery.
                beaconManager.startNearableDiscovery();

                // Eddystone scanning.
                beaconManager.startEddystoneScanning();
            }
        });
    }

    private Socket socket;
    {
        try {
            //socket = IO.socket("http://192.168.1.68:3000/");
            socket = IO.socket("http://buslocation-itoaxacaedu.rhcloud.com/");
        }
        catch(URISyntaxException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        socket.on("recibirCoordenadas", recibirCoordenadas);
        socket.connect();

        textview_coordenadas = (TextView) findViewById(R.id.textview_coordenadas);

        gps_button = (Button) findViewById(R.id.gps_button);

        bluetooth_toggle_button = (ToggleButton) findViewById(R.id.bluetooth_toggle_button);

        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (adaptadorBluetooth == null) {
            bluetooth_toggle_button.setClickable(false);
        }
        estadoBluetooth();
        bluetooth_toggle_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
                }
                else {
                    adaptadorBluetooth.disable();
                }
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //marcador.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                socket.emit("enviarCoordenadas", location.getLatitude() + "," + location.getLongitude());
                textview_coordenadas.setText(location.getLatitude() + ", " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarPermisos();
            }
        });
        BeaconManager();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        marcador = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(37.7750, 122.4183))
                .title("Titulo.")
                .snippet("Descripcion."));
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void verificarPermisos() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
            }
        }
        else {
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "Iniciando el servicio GPS.", Toast.LENGTH_LONG).show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    public void estadoBluetooth() {
        if (adaptadorBluetooth.isEnabled()) {
            bluetooth_toggle_button.setChecked(true);
        } else {
            bluetooth_toggle_button.setChecked(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        estadoBluetooth();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //beaconManager.stopEddystoneScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        beaconManager.disconnect();
    }

    private Emitter.Listener recibirCoordenadas = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String idUsuario;
                    String latitud;
                    String longitud;
                    //Toast.makeText(getApplicationContext(), "Hola Mundo.", Toast.LENGTH_LONG).show();
                    try {
                        idUsuario = data.getString("idUsuario");
                        latitud = data.getString("latitud");
                        longitud = data.getString("longitud");
                        if(listaUsuarios.size() == 0) {
                            Usuario usuario = new Usuario();
                            usuario.setIdUsuario(idUsuario);
                            usuario.setLatitud(Double.parseDouble(latitud));
                            usuario.setLongitud(Double.parseDouble(longitud));
                            usuario.setMarcador(
                                    mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(usuario.getLatitud(), usuario.getLongitud()))
                                    .title(idUsuario)
                                    .snippet(idUsuario + ": " + latitud + ", " + longitud)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))));
                            listaUsuarios.add(usuario);
                        }
                        else {
                            for(int i = 0; i < listaUsuarios.size(); i++) {
                                Usuario usuario = listaUsuarios.get(i);
                                if(usuario.getIdUsuario().equals(idUsuario)) {
                                    usuario.setLatitud(Double.parseDouble(latitud));
                                    usuario.setLongitud(Double.parseDouble(longitud));
                                    usuario.setPosicion(Double.parseDouble(latitud), Double.parseDouble(longitud));
                                    return;
                                }
                            }
                            Usuario usuario = new Usuario();
                            usuario.setIdUsuario(idUsuario);
                            usuario.setLatitud(Double.parseDouble(latitud));
                            usuario.setLongitud(Double.parseDouble(longitud));
                            usuario.setMarcador(
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(usuario.getLatitud(), usuario.getLongitud()))
                                            .title(idUsuario)
                                            .snippet(idUsuario + ": " + latitud + ", " + longitud)));
                            listaUsuarios.add(usuario);
                        }
                    }
                    catch(JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    /*public int setIcono(String tipo) {
        if(tipo.equals("autobus")) {
            return R.drawable.bus;
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case 1: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Los permisos fueron otorgados.", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Los permisos fueron denegados.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
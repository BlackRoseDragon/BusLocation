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

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static mashup.com.buslocation.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Marker marcador;

    private TextView textview_coordenadas;

    private Button gps_button;

    private ToggleButton bluetooth_toggle_button;

    private BluetoothAdapter adaptadorBluetooth;

    private LocationManager locationManager;

    private LocationListener locationListener;

    private Socket socket;
    {
        try {
            socket = IO.socket("http://192.168.1.68:3000/");
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
                marcador.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                socket.emit("enviarCoordenadas", location.getLatitude() + ", " + location.getLongitude());
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
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    private Emitter.Listener recibirCoordenadas = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        Toast.makeText(getApplicationContext(), username + ": " + message, Toast.LENGTH_LONG).show();
                    }
                    catch(JSONException e) {
                        return;
                    }

                    // add the message to view
                    // addMessage(username, message);
                }
            });
        }
    };

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
package mashup.com.buslocation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.twitter.sdk.android.Twitter;

public class MainActivity extends AppCompatActivity {

    private Button map_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!verificarSesion()) {
            goLoginScreen();
        }

        map_button = (Button) findViewById(R.id.map_button);
        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goMapScreen();
            }
        });
    }

    public boolean verificarSesion() {
        if(Twitter.getInstance().core.getSessionManager().getActiveSession() != null) {
            return true;
        }
        if(AccessToken.getCurrentAccessToken() != null) {
            return true;
        }
        return false;
    }

    public void cerrarSesion() {
        if(Twitter.getInstance().core.getSessionManager().getActiveSession() != null) {
            Twitter.getInstance().core.getSessionManager().clearActiveSession();
        }
        if(AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        goLoginScreen();
    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) {
        cerrarSesion();
    }

    public void goMapScreen() {
        Intent intent = new Intent(getApplication(), MapsActivity.class);
        startActivity(intent);
    }
}
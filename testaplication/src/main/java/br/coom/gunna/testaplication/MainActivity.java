package br.coom.gunna.testaplication;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import br.gunna.pagarmeandroid.pagarme.PagarMeAndroid;
import br.gunna.pagarmeandroid.pagarme.PagarMeRequest;
import br.gunna.pagarmeandroid.pagarme.PagarMeResponse;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        PagarMeAndroid.initialize("ek_test_yCRumKDVQgqFkjlQojnTSHgWw7UI5g");
        PagarMeAndroid.getsInstance()
                .number("5117810304197289")
                .cvv("483")
                .expirationDate("05/2023")
                .holderName("Tiago")
                .generateCardHash(new PagarMeAndroid.PagarMeListener() {
                    @Override
                    public void onSuccess(PagarMeRequest pagarMeRequest, PagarMeResponse pagarMeResponse, String s) {
                        Log.e("CardHash", s);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

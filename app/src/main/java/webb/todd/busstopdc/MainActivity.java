package webb.todd.busstopdc;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView( R.layout.activity_main );
        setContentView( R.layout.activity_maps );


        startActivity( new Intent(this, MapsActivity.class ) );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        boolean success = super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate( R.menu.menu, menu );
        menu.add( R.string.say_hi_label );
        //menu.add( R.string.settings_label );
        return success;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        if( item.getItemId() == R.id.app_bar_search ) {
            startActivity( new Intent(this, MapsActivity.class ) );
        }
        else if( item.getItemId() == R.id.app_bar_settings ) {
            startActivity(new Intent(this, webb.todd.busstopdc.SettingsActivity.class));
        }
        else {
            Toast.makeText(getApplicationContext(), "Hi There!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}

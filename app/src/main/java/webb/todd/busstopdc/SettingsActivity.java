package webb.todd.busstopdc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_settings );

        ToggleButton soundToggle = (ToggleButton) findViewById( R.id.soundToggleButton );
        soundToggle.setSelected( getSharedPreferences( "busstopdc", MODE_PRIVATE ).getBoolean(
                "sound", true) );

        soundToggle.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick( View view ) {
                // set main setting
                final boolean soundOn = ((ToggleButton)view).isChecked();
                getSharedPreferences( "busstopdc", MODE_PRIVATE ).edit().putBoolean(
                        "sound", soundOn ).commit();
            }
        });
    }
}

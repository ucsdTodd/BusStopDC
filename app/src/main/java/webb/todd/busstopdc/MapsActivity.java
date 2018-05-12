package webb.todd.busstopdc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    boolean inUpdate = false;

    boolean musicOn = false;

    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSharedPreferences("busstopdc", MODE_PRIVATE).getBoolean("sound", true)) {
            if( player == null ){
                player = MediaPlayer.create( this, R.raw.wheels_on_bus );
            }
            player.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.stop();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady( GoogleMap googleMap ) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if( inUpdate ){
                    return;
                }
                inUpdate = true;
                final CameraPosition position = mMap.getCameraPosition();
                GetBusStops stopGetter = new GetBusStops( position.target.latitude,
                        position.target.longitude );
                stopGetter.execute();
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng natnMall = new LatLng(38.89, -77.022 );
        mMap.addMarker( new MarkerOptions().position(natnMall).title( "National Mall" ) );
        Log.i( "Bus", "Zooming to National Mall...");
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom( natnMall, 15 ) );
    }

    void updateMap( JSONObject jsonBusData ){
        mMap.clear();
        try{
            JSONArray stops = jsonBusData.getJSONArray( "Stops" );
            int numStops = stops.length();
            Log.i( "Bus", "Stops:" +stops.length() );
            for( int i = 0; i < numStops; i++ ){
                mMap.addMarker( getBusStopMarker( stops.getJSONObject( i ) ) );
            }
        }
        catch( final Exception xx ){

        }
        finally{
            inUpdate = false;
        }

    }

    MarkerOptions getBusStopMarker( JSONObject busStopData ){
        try {
            //Bitmap bitmap = BitmapFactory.decodeFile( "Wreck.png" );
            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(), R.drawable.bus_stop),
                    128, 128, true);
            BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(bitmap);
            double lat = busStopData.getDouble("Lat");
            double lon = busStopData.getDouble("Lon");
            String label = busStopData.getString( "Name" );
            StringBuilder routesText = new StringBuilder( "Routes ");
            JSONArray routeArray = busStopData.getJSONArray( "Routes" );
            int numRoutes = routeArray.length();
            for( int i = 0; i < numRoutes; i++ ){
                routesText.append( routeArray.getString( i ) );
                routesText.append(" ");
            }
            return new MarkerOptions().icon(bd).position(new LatLng(lat, lon)
            ).title(label).snippet( routesText.toString() );
        }
        catch ( final Exception xx ){
            return null;
        }
    }

    private class GetBusStops extends AsyncTask<URL, Void, JSONObject> {

        double lat;
        double lon;

        GetBusStops( final double latitude, final double longitude ){
            lat = latitude;
            lon = longitude;

        }

        @Override
        protected JSONObject doInBackground(URL... urls) {
            // ignore args for now, we have what we need
            URL query = null;
            HttpURLConnection conxtion = null;
            try{
                query = new URL( getQuery() );
                conxtion = (HttpURLConnection) query.openConnection();
                int response = conxtion.getResponseCode();
                //System.out.println( "Response code is " +response);
                Log.i( "Bus", "Response code is " +response );
                if( response == HttpURLConnection.HTTP_OK ){
                    final StringBuilder text = new StringBuilder();
                    try(BufferedReader reader = new BufferedReader(
                            new InputStreamReader( conxtion.getInputStream()))){
                        String line;
                        while( (line = reader.readLine()) != null ){
                            text.append( line );
                        }
                    }
                    Log.i( "Bus", text.toString() );
                    return new JSONObject( text.toString() );
                }
                return null;
            }
            catch( Exception x ){
                Log.e( "Bus", x.getMessage(), x );
                return null;
            }
            finally {
                if( conxtion != null ) {
                    conxtion.disconnect();
                }
            }
        }

        protected String getQuery(){
            return WMATA_BUS_STOPS_URL +"api_key=" + WMATA_DEMO_KEY +"&Lat="
                    +lat +"&Lon=" +lon +"&Radius=500";
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            updateMap( jsonObject );
        }
    }
    static final String WMATA_DEMO_KEY = "e13626d03d8e4c03ac07f95541b3091b";
    static final String WMATA_BUS_STOPS_URL = "https://api.wmata.com/Bus.svc/json/jStops?";

}

package com.nicog.idra.Interface.VistaFuente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.Entities.Incident;
import com.nicog.idra.Interface.MainActivity;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;

public class VistaFuente extends AppCompatActivity {
    private TextView tituloFuente;
    private ImageView imageFuente;
    private TextView metersTextView;
    private TextView createdBy;
    private TextView descripcionTextView;
    private TextView mTextView;
    private Button[] starsImages;
    private Button accpetPetitionButton;
    private Button denyPetitionButton;
    private Button reportIncidentButton;

    private Fuente fuente;
    private int distancia;
    public Service service;

    GoogleMap gMap;

    InterstitialAd mInterstitialAd;

    private boolean fuenteTerminado = false;
    private boolean mapTerminado = false;

    public static int vistaFuente = 1;
    public static int vistaAddFuente = 2;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_fuente);

        service = new Service();

        if(!service.userIsLogged()){
            returnToLogin();
        }

        fuente = (Fuente) getIntent().getExtras().getSerializable("fuente");
        distancia = getIntent().getIntExtra("metros", -1);
        mode = getIntent().getIntExtra("mode", vistaFuente);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFuente);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;

                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mapTerminado = true;
                updateUI();
            }
        });

        findViewById(R.id.mapFuente).setMinimumHeight(150);

        starsImages = new Button[]{findViewById(R.id.star1), findViewById(R.id.star2), findViewById(R.id.star3),
                findViewById(R.id.star4), findViewById(R.id.star5)};
        tituloFuente = findViewById(R.id.tituloFuente);
        imageFuente = findViewById(R.id.imageFuente);
        metersTextView = findViewById(R.id.metersTextView);
        createdBy = findViewById(R.id.createdByTextView);
        descripcionTextView = findViewById(R.id.descripcionTextView);
        mTextView = findViewById(R.id.mVistaFuenteTextView);
        denyPetitionButton = findViewById(R.id.denyPetitionButton);
        accpetPetitionButton = findViewById(R.id.acceptPetitionButton);
        reportIncidentButton = findViewById(R.id.reportIncidentButton);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5821282725257897/7378140761");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();

        if(appLinkData != null){                            //si el usuario viene del link
            String fuenteId = appLinkData.getLastPathSegment();
            service.getFuente(fuenteId, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    fuente = Fuente.fromDocumentSnapshot(documentSnapshot);
                    fuenteTerminado = true;
                    updateUI();
                }
            });
        }else if(fuente != null){
            fuenteTerminado = true;
            updateUI();
        }
    }

    private void updateUI(){
        if(!fuenteTerminado || !mapTerminado) return;
        tituloFuente.setText(fuente.getTitulo());

        if(distancia < 0){
            mTextView.setVisibility(View.INVISIBLE);
        }else{
            metersTextView.setText(String.valueOf(distancia));
        }

        if(fuente.getCreador() == null || fuente.getCreador().equals("")){
            createdBy.setText(getText(R.string.Anonymous));
        }else{
            service.getNickname(fuente.getCreador(), new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    createdBy.setText((String) documentSnapshot.get("nickname"));
                }
            });
        }

        if(fuente.getFoto() == null || fuente.getFoto().equals("")){
            imageFuente.setImageResource(R.drawable.ejemplofuente);
            imageFuente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reportarIncidencia(Incident.fotografia);
                }
            });
        }else{
            StorageReference reference = null;
            if(mode == vistaFuente){
                reference = service.imagenesFuentes;
            }else if(mode == vistaAddFuente){
                reference = service.peticionesImagenes;
            }

            service.getFotoFuente(fuente, new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageFuente.setImageBitmap(bmp);
                }
            }, reference.child(fuente.getFoto()));
        }

        service.getRatings(fuente, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Double avg = documentSnapshot.getDouble("avg");
                if(avg != null) setRatingUI(avg);
            }
        });

        String descr = fuente.getDescripcion();
        if(descr != null && !descr.equals("")){
            descripcionTextView.setText(descr);
        }else{
            descripcionTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reportarIncidencia(Incident.descripcion);
                }
            });
        }

        setButtons();
        setStarsImages();
        setMap();
    }

    private void setButtons(){
        switch(mode){
            case 1:
                reportIncidentButton.setVisibility(View.VISIBLE);
                break;
            case 2:
                denyPetitionButton.setVisibility(View.VISIBLE);
                accpetPetitionButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setMap(){
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fuente.getLatLng(), 15));

        Bitmap aux = BitmapFactory.decodeResource(getResources(), R.drawable.markermap);
        Bitmap icon = Bitmap.createScaledBitmap(aux, 80,125,false);

        MarkerOptions mops = new MarkerOptions();
        mops.position(fuente.getLatLng());
        mops.icon(BitmapDescriptorFactory.fromBitmap(icon));
        gMap.addMarker(mops);
    }

    private void setRatingUI(double rating){
        for(int i = 0; i < rating && i <= 5; i++){
            if(rating >= i + 1){ starsImages[i].setBackgroundResource(R.drawable.full_star); }
            else if(rating >= i + 0.5){ starsImages[i].setBackgroundResource(R.drawable.half_star); }
        }
    }

    private void setStarsImages(){
        if(mode != vistaFuente){return;}
        for(int i = 1; i <= starsImages.length; i++){
            final int x = i;
            starsImages[i-1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    service.addRating(fuente, x);
                    for(int y = 0; y < starsImages.length; y++){
                        if(x >= y+1){
                            starsImages[y].setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.blue5));
                            starsImages[y].setBackgroundResource(R.drawable.full_star);
                        }else{
                            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                            Log.i("data", "y:" + y + ", night:" + currentNightMode);
                            if(currentNightMode == Configuration.UI_MODE_NIGHT_NO){
                                starsImages[y].setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.dark));
                            }else if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
                                starsImages[y].setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.white));

                            }
                            starsImages[y].setBackgroundResource(R.drawable.empty_star);
                        }
                    }
                }
            });
        }
    }

    private void returnToLogin(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void share(View v){
        if(mode != vistaFuente){return;}
        String id = fuente.getId();
        String url = "fuente.idrapp.es/" + id;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    public void reportarIncidenciaClick(View v){
        reportarIncidencia(-1);
    }

    private void reportarIncidencia(int type){
        Intent i = new Intent(this, ReportIncident.class);
        if(type >= 0) i.putExtra("type", type);
        i.putExtra("fuente", fuente);
        startActivity(i);
    }

    private OnSuccessListener successListener = new OnSuccessListener() {
        @Override
        public void onSuccess(Object o) {
            finish();
        }
    };

    private OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(VistaFuente.this, R.string.errorTryAgainLater, Toast.LENGTH_LONG).show();
        }
    };

    public void acceptPetition(View v){
        service.acceptPetition(fuente, successListener, failureListener);
    }

    public void denyPetition(View v){
        service.denyPetition(fuente, successListener, failureListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }
}
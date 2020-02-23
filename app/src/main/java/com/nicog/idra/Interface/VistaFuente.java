package com.nicog.idra.Interface;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;

import java.util.Map;

public class VistaFuente extends AppCompatActivity {
    private TextView tituloFuente;
    private ImageView imageFuente;
    private TextView metersTextView;
    private TextView createdBy;
    private TextView descripcionTextView;
    private TextView mTextView;
    private ImageView[] starsImages;

    private Fuente fuente;
    public Service service;

    GoogleMap gMap;

    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_fuente);

        service = new Service();

        String fuenteId = getIntent().getStringExtra("fuenteID");
        final Integer distancia = getIntent().getIntExtra("metros", -1);

        service.getFuente(fuenteId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Fuente f = Fuente.fromDocumentSnapshot(documentSnapshot);
                fuente = f;
                updateUI(f, distancia);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFuente);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;

                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });

        findViewById(R.id.mapFuente).setMinimumHeight(150);

        starsImages = new ImageView[]{findViewById(R.id.star1), findViewById(R.id.star2), findViewById(R.id.star3),
                                        findViewById(R.id.star4), findViewById(R.id.star5)};
        tituloFuente = findViewById(R.id.tituloFuente);
        imageFuente = findViewById(R.id.imageFuente);
        metersTextView = findViewById(R.id.metersTextView);
        createdBy = findViewById(R.id.createdByTextView);
        descripcionTextView = findViewById(R.id.descripcionTextView);
        mTextView = findViewById(R.id.mVistaFuenteTextView);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5821282725257897/7378140761");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    private void updateUI(Fuente fuente, Integer distancia){
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
        }else{
            service.getFotoFuente(fuente, new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageFuente.setImageBitmap(bmp);
                }
            });
        }

        service.getRatings(fuente, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> ratings = documentSnapshot.getData();
                double media = 0;
                if(ratings == null){return;}
                for(Map.Entry<String, Object> entry : ratings.entrySet()){
                    media += (long) entry.getValue();
                }

                media /= (double) ratings.size();

                setRatingUI(media);
            }
        });

        String descr = fuente.getDescripcion();
        if(descr != null && !descr.equals("")){
            descripcionTextView.setText(descr);
        }

        setStarsImages();
        setMap();
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
            if(rating >= i + 1){ starsImages[i].setImageResource(R.drawable.full_star); }
            else if(rating >= i + 0.5){ starsImages[i].setImageResource(R.drawable.half_star); }
        }
    }

    private void setStarsImages(){
        for(int i = 1; i <= starsImages.length; i++){
            final int x = i;
            starsImages[i-1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    service.addRating(fuente, x);
                    for(int y = 0; y < starsImages.length; y++){
                        if(x >= y+1){
                            starsImages[y].setColorFilter(getColor(R.color.blue5));
                            starsImages[y].setImageResource(R.drawable.full_star);
                        }else{
                            starsImages[y].setColorFilter(null);
                            starsImages[y].setImageResource(R.drawable.empty_star);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }
}
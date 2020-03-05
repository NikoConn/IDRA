package com.nicog.idra.Interface;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.Interface.VistaFuente.VistaFuente;
import com.nicog.idra.Interface.addFuente.addFuente;
import com.nicog.idra.Interface.user.User;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;

    private FirebaseUser user;

    private LinearLayout fontListLayout;
    private ImageView userImageButton;

    private HashMap<Marker, Fuente> markerFuenteHashMap;
    private List<Fuente> fuenteList;
    //private List<Integer> cuadrantesList;

    private Service service;

    private double lastZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //set servicio
        service = new Service();

        //set views
        fontListLayout = findViewById(R.id.fontListLinearLayout);
        userImageButton = findViewById(R.id.userImageButton);

        //set list and map
        markerFuenteHashMap = new HashMap<>();
        fuenteList = new ArrayList<>();
        //cuadrantesList = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //poner ubicacion actual
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        updateLocation(false);

        Picasso.get().load(service.getUserPhotoUrl()).into(userImageButton);
    }

    //OnClick locate button
    public void locate(View v){
        updateLocation(true);
    }

    //OnClick User photo
    public void openUser(View v){
        Intent i = new Intent(this, User.class);
        startActivity(i);
    }

    //OnClick add button
    public void addFuente(View v){
        Intent i = new Intent(this, addFuente.class);
        startActivity(i);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMarkerClickListener(markerClickListener);
        mMap.setOnCameraIdleListener(cameraMoveListener);
    }

    //CameraMove
    private GoogleMap.OnCameraIdleListener cameraMoveListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            CameraPosition position = mMap.getCameraPosition();
            LatLng latLng = position.target;

            Log.i("camera", String.valueOf(position.zoom));

            if (position.zoom < 15) {
                if(lastZoom > 15){
                    mMap.clear();
                }
                service.getCuadrante(latLng, addCuadranteToUI);
            } else {
                if(lastZoom < 15){
                    mMap.clear();
                }
                service.getFuetnesNearOf(latLng, addFuenteToUI);
            }
            lastZoom = position.zoom;
        }
    };

    OnSuccessListener<DocumentSnapshot> addCuadranteToUI = new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            if(!documentSnapshot.exists()){return;}
            Bitmap aux = BitmapFactory.decodeResource(getResources(), R.drawable.markermap);
            Bitmap icon = Bitmap.createScaledBitmap(aux, 80,125,false);

            LatLng latLng = service.getCentroCuadrante(Integer.valueOf(documentSnapshot.getId()));

            MarkerOptions mops = new MarkerOptions();
            mops.position(latLng);
            mops.icon(BitmapDescriptorFactory.fromBitmap(icon));
            mMap.addMarker(mops);
        }
    };

    OnSuccessListener<DocumentSnapshot> addFuenteToUI = new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            Fuente fuente = Fuente.fromDocumentSnapshot(documentSnapshot);

            if(!fuenteList.contains(fuente)){
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                int distance = (int) Math.floor(service.getDistance(fuente.getLatLng(), currentLatLng));
                addFuenteToList(fuente, distance);
                fuenteList.add(fuente);
            }

            addFuenteToMap(fuente);
        }
    };

    //Onclick one source
    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Fuente fuente = markerFuenteHashMap.get(marker);
            if(fuente != null){
                openFuente(fuente);
            }else{
                LatLng position = marker.getPosition();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
            }
            return true;
        }
    };

    //Same
    private void openFuente(Fuente fuente){
        Intent i = new Intent(this, VistaFuente.class);
        i.putExtra("fuente", fuente);

        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        int distance = (int) Math.floor(service.getDistance(fuente.getLatLng(), currentLatLng));

        i.putExtra("metros", distance);
        startActivity(i);
    }

    public void updateLocation(final boolean animateCamera) {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if(animateCamera){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    }else{
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    }
                }
            }
        });
    }

    private void addFuenteToMap(Fuente fuente) {
        Bitmap aux = BitmapFactory.decodeResource(getResources(), R.drawable.markermap);
        Bitmap icon = Bitmap.createScaledBitmap(aux, 80,125,false);
        //Add to map
        MarkerOptions mops = new MarkerOptions();
        mops.position(fuente.getLatLng());
        mops.icon(BitmapDescriptorFactory.fromBitmap(icon));

        Marker marker = mMap.addMarker(mops);
        markerFuenteHashMap.put(marker, fuente);
    }

    private void addFuenteToList(final Fuente fuente, int distance) {
        //Add to list
        LayoutInflater li = LayoutInflater.from(this);
        View theview = li.inflate(R.layout.listelementfuentes, null);

        TextView titulo = theview.findViewById(R.id.tituloFuenteElem);
        final ImageView imagenFuente = theview.findViewById(R.id.imageFuenteElem);
        TextView meters = theview.findViewById(R.id.metersElement);
        CardView card = theview.findViewById(R.id.cardViewElement);
        ConstraintLayout constraintLayout = theview.findViewById(R.id.layoutElement);
        LinearLayout linearLayout = theview.findViewById(R.id.linearlayoutElement);

        meters.setText(String.valueOf(distance));
        titulo.setText(fuente.getTitulo());
        if (!fuente.getFoto().equals("") && fuente.getFoto() != null) {
            service.getFotoFuente(fuente, new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imagenFuente.setImageBitmap(bmp);
                }
            }, service.imagenesFuentes);
        } else {
            constraintLayout.removeView(card);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            constraintSet.connect(R.id.tituloFuenteElem, ConstraintSet.START, R.id.layoutElement, ConstraintSet.START, 0);
            constraintSet.connect(R.id.linearlayoutElement, ConstraintSet.START, R.id.layoutElement, ConstraintSet.START, 0);

            constraintSet.applyTo(constraintLayout);
        }


        fontListLayout.addView(theview, getElementIndex(distance));
        theview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFuente(fuente);
            }
        });
    }


    //get order of the fuente gonna put in list
    private int getElementIndex(int meters) {
        for (int i = 3; i < fontListLayout.getChildCount(); i++) {
            View view = fontListLayout.getChildAt(i);
            CharSequence aux = ((TextView) (view.findViewById(R.id.metersElement))).getText();
            int otherMeters = Integer.parseInt(aux.toString());
            if (otherMeters > meters) {
                return i;
            }
        }
        return fontListLayout.getChildCount();
    }
}

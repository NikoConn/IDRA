package com.nicog.idra.Interface.addFuente;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class addFuente extends AppCompatActivity implements OnMapReadyCallback {
    private EditText nameEditText;
    private EditText descriptionEditText;
    private TextView addPhotoTextView;
    private GoogleMap mMap;
    private ImageView marker;

    private LatLng latLng;
    Bitmap selectedImage;

    private Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fuente);

        nameEditText = findViewById(R.id.nameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        addPhotoTextView = findViewById(R.id.addPhotoTextView);
        marker = findViewById(R.id.addFuenteMarker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.locationMapView);
        mapFragment.getMapAsync(this);

        service = new Service();
    }

    public void addPhoto(View v){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getText(R.string.SelectPhoto));
        String[] pictureDialogItems = { getText(R.string.Gallery).toString(), getText(R.string.Camera).toString()};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, 2);
    }
    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 756);

        }else{
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 3);
        }
    }

    public void addLocation(){
        Intent i = new Intent(this, addLocation.class);
        startActivityForResult(i, 1);
    }

    public void cancel(View v){
        finish();
    }

    public void create(View v){
        String name = nameEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        LatLng ubicacion = latLng;

        if(name.equals("")){
            Toast.makeText(this, getText(R.string.nameRequired), Toast.LENGTH_SHORT).show();
        }else if(ubicacion == null){
            Toast.makeText(this, getText(R.string.locationRequired), Toast.LENGTH_SHORT).show();
        }else{
            Fuente fuente = new Fuente("", name, description, "", FirebaseAuth.getInstance().getUid(), latLng.latitude, latLng.longitude);
            service.addPetition(fuente, selectedImage, new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Toast.makeText(addFuente.this, getText(R.string.thanksPetition), Toast.LENGTH_LONG).show();
                    addFuente.this.finish();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(addFuente.this, getText(R.string.errorTryAgainLater), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addLocation();
            }
        });
    }

    public void setLatLng(LatLng latLng){
        this.latLng = latLng;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        marker.setImageResource(R.drawable.markermap);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && reqCode == 2) {  //result de la foto
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                addPhotoTextView.setText(getText(R.string.photoAdded));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }else if(resultCode == RESULT_OK && reqCode == 3) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            selectedImage = photo;
            addPhotoTextView.setText(getText(R.string.photoAdded));
        }else if(reqCode == 1 && resultCode == RESULT_OK){ //result del mapa
            LatLng latLng = data.getExtras().getParcelable("latLng");
            setLatLng(latLng);
        }else if(resultCode == RESULT_OK && reqCode == 756){  //result de permisos de camara
            takePhotoFromCamera();
        }
    }
}

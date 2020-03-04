package com.nicog.idra.Interface.VistaFuente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.Entities.Incident;
import com.nicog.idra.Interface.user.qrReader;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ReportIncident extends AppCompatActivity {
    private TextView tituloFuenteTextView;
    private Spinner incidentTypeSpinner;
    private LinearLayout container;
    private Button sendButton;

    private String[] incidentTypes;

    private EditText otherEditText;
    private TextView problemTextView;
    private ImageView photoImageView;

    private Fuente fuente;
    private int type;

    private Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        container = findViewById(R.id.containerViewIncident);
        incidentTypeSpinner = findViewById(R.id.typeOfProblemSpinner);
        tituloFuenteTextView = findViewById(R.id.tituloIncidencia);
        sendButton = findViewById(R.id.sendButton);

        this.fuente = (Fuente) getIntent().getExtras().get("fuente");
        type = getIntent().getExtras().getInt("type", -1);

        tituloFuenteTextView.setText(fuente.getTitulo());

        initSpinner();

        switch (type){
            case 0:
                incidentTypeSpinner.setSelection(1);
                setNoFoto();
                break;
            case 1:
                incidentTypeSpinner.setSelection(2);
                setNoDescription();
                break;
        }
    }

    private void initSpinner(){
        incidentTypes = new String[] {"", getText(R.string.incorrectPhoto).toString(),
                getText(R.string.incorrectDescription).toString(),
                getText(R.string.NonExistentSource).toString(),
                getText(R.string.Other).toString()};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, incidentTypes){
            public View getView(int position, View convertView,ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextSize(16);
                return v;
            }

            public View getDropDownView(int position, View convertView,ViewGroup parent) {
                View v = super.getDropDownView(position, convertView,parent);
                ((TextView) v).setGravity(Gravity.CENTER);

                return v;
            }
        };

        incidentTypeSpinner.setAdapter(adapter);
        incidentTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setContentOnClick(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setContentOnClick(-2);
            }
        });
    }

    private void setContentOnClick(int index){
        container.removeAllViews();
        switch(index){
            case 1:
                setNoFoto();
                break;
            case 2:
                setNoDescription();
                break;
            case 3:
                this.type = Incident.inexistente;
                break;
            case 4:
                setOther();
                break;
        }
        if(index >= 1) sendButton.setVisibility(View.VISIBLE);
        else sendButton.setVisibility(View.INVISIBLE);
    }

    private void setNoFoto(){
        problemTextView = new TextView(this);
        problemTextView.setText(getText(R.string.addPhoto));
        problemTextView.setGravity(Gravity.CENTER);
        problemTextView.setTextAppearance(R.style.DefaultFont);

        Button addPhotoButton = new Button(this);
        addPhotoButton.setBackground(getDrawable(R.drawable.add_button));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addPhotoButton.setLayoutParams(params);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhoto();
            }
        });

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(500, 250);
        photoImageView = new ImageView(this);
        photoImageView.setLayoutParams(params2);

        if(photo != null){
            photoImageView.setImageBitmap(photo);
        }

        container.addView(problemTextView);
        container.addView(addPhotoButton);
        container.addView(photoImageView);

        this.type = Incident.fotografia;
    }

    private void setNoDescription(){
        TextView tv = new TextView(this);
        tv.setText(getText(R.string.description));
        tv.setGravity(Gravity.CENTER);
        tv.setTextAppearance(R.style.DefaultFont);

        otherEditText = new EditText(this);
        otherEditText.setSingleLine(false);
        if(fuente != null) otherEditText.setText(fuente.getDescripcion());

        container.addView(tv);
        container.addView(otherEditText);

        otherEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        this.type = Incident.descripcion;
    }

    private void setOther(){
        TextView tv = new TextView(this);
        tv.setText(getText(R.string.ProblemDescription));
        tv.setGravity(Gravity.CENTER);
        tv.setTextAppearance(R.style.DefaultFont);

        otherEditText = new EditText(this);
        otherEditText.setSingleLine(false);

        container.addView(tv);
        container.addView(otherEditText);

        otherEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        this.type = Incident.otro;
    }

    public void addPhoto(){
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && reqCode == 2) {  //result de la foto
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap b = BitmapFactory.decodeStream(imageStream);
                photoAdded(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }else if(resultCode == RESULT_OK && reqCode == 3) {
            Bitmap b = (Bitmap) data.getExtras().get("data");
            photoAdded(b);
        }else if(resultCode == RESULT_OK && reqCode == 756){
            takePhotoFromCamera();
        }
    }

    private void photoAdded(Bitmap bitmap){
        photo = bitmap;
        photoImageView.setImageBitmap(photo);
        problemTextView.setText(getText(R.string.photoAdded));
    }

    private OnSuccessListener successListener = new OnSuccessListener() {
        @Override
        public void onSuccess(Object o) {
            Toast.makeText(ReportIncident.this, getText(R.string.thanksIncident), Toast.LENGTH_LONG).show();
            finish();
        }
    };

    private OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(ReportIncident.this, getText(R.string.errorTryAgainLater), Toast.LENGTH_LONG).show();
            sendButton.setEnabled(true);
        }
    };

    private void crearIncidencia(int tipo, String texto){
        Service s = new Service();

        Incident i = new Incident(tipo, texto, s.getUserUid(), fuente.getId());
        s.addIncident(i, successListener, failureListener);
    }

    private void crearIncideciaFoto(){
        Service s = new Service();
        s.addIncidentPhoto(photo, fuente.getId(), successListener, failureListener);
    }

    public void aceptarClick(View v){
        if( (type == Incident.otro || type == Incident.descripcion) && otherEditText.getText().toString().equals("")
                || type == Incident.fotografia && this.photo == null){
            Toast.makeText(this, getText(R.string.fillFields), Toast.LENGTH_SHORT).show();
            return;
        }
        sendButton.setEnabled(false);
        switch(type){
            case 1:
            case 3:
                crearIncidencia(type, otherEditText.getText().toString());
                break;
            case 2:
                crearIncidencia(type, "");
                break;
            case 0:
                crearIncideciaFoto();
                break;
        }
    }

    public void cancelClick(){
        finish();
    }
}

package com.nicog.idra.Interface.mod;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.Entities.Incident;
import com.nicog.idra.Interface.VistaFuente.VistaFuente;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;

import org.w3c.dom.Document;

import java.util.List;

public class ModMenu extends AppCompatActivity {
    private LinearLayout contentLinearLayout;
    private Service service;

    private RadioButton waterSourceRadioButton;
    private RadioButton incidentsRadioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_menu);

        this.service = new Service();

        contentLinearLayout = findViewById(R.id.contentScrollView);

        waterSourceRadioButton = findViewById(R.id.waterSoruceRadioButton);
        incidentsRadioButton = findViewById(R.id.incidentsRadioButton);
    }

    public void addWaterSourceClick(View v){
        service.getPetitions(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                setListaPeticiones(queryDocumentSnapshots.getDocuments());
            }
        });
    }

    private void setListaPeticiones(List<DocumentSnapshot> documentSnapshots){
        contentLinearLayout.removeAllViews();
        for(DocumentSnapshot ds : documentSnapshots){
            Fuente f = Fuente.fromDocumentSnapshot(ds);
            addToList(f);
        }
    }

    private void setListaIncidentes(List<DocumentSnapshot> documentSnapshots){
        for(DocumentSnapshot ds : documentSnapshots){

        }
    }

    private void addToList(final Fuente f){
        TextView tv = getTextView(f.getId());
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPeticion(f);
            }
        });
        contentLinearLayout.addView(tv);
    }

    private void addToList(Incident i){

    }

    private TextView getTextView(String id){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,40,0,0);
        TextView tv = new TextView(this);
        tv.setText(id);
        tv.setTextSize(20);
        tv.setLayoutParams(params);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return tv;
    }

    private void openPeticion(Fuente f){
        Intent i = new Intent(this, VistaFuente.class);
        i.putExtra("fuente", f);
        i.putExtra("mode", VistaFuente.vistaAddFuente);
        startActivity(i);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(waterSourceRadioButton.isChecked()){
            addWaterSourceClick(null);
        }else if(incidentsRadioButton.isChecked()){

        }
    }
}

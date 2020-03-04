package com.nicog.idra.Interface.mod;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nicog.idra.Entities.Fuente;
import com.nicog.idra.Interface.VistaFuente.VistaFuente;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;

import org.w3c.dom.Document;

import java.util.List;

public class ModMenu extends AppCompatActivity {
    private LinearLayout contentLinearLayout;
    private Service service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_menu);

        this.service = new Service();

        contentLinearLayout = findViewById(R.id.contentScrollView);
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
        for(final DocumentSnapshot ds : documentSnapshots){
            Log.i("data", ds.toString());
            TextView tv = new TextView(this);
            tv.setText(ds.getId());
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPeticion(Fuente.fromDocumentSnapshot(ds));
                }
            });
            contentLinearLayout.addView(tv);
        }
    }

    private void openPeticion(Fuente f){
        Intent i = new Intent(this, VistaFuente.class);
        i.putExtra("fuente", f);
        startActivity(i);
    }
}

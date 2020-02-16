package com.nicog.idra.logic;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nicog.idra.Entities.Fuente;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Service {
    private CollectionReference fuentesReference;
    private CollectionReference cuadrantesReference;
    private CollectionReference nicknamesReference;
    private CollectionReference ratingsReference;
    private CollectionReference petitionsReference;
    private StorageReference imagenesFuentes;
    private StorageReference peticionesImagenes;

    private FirebaseUser user;

    public Service(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        nicknamesReference = db.collection("nicknames");
        ratingsReference = db.collection("ratings");
        petitionsReference = db.collection("petitions");
        cuadrantesReference = db.collection("cuadrantes");
        fuentesReference = db.collection("fuentes");

        peticionesImagenes = FirebaseStorage.getInstance().getReference().child("fotos_peticiones");
        imagenesFuentes = FirebaseStorage.getInstance().getReference().child("fotos_fuentes");

        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    public void getFuetnesNearOf(LatLng latLng, final OnSuccessListener<DocumentSnapshot> callback){
        int cuadrante = getCuadrante(latLng.latitude, latLng.longitude);

        cuadrantesReference.document(String.valueOf(cuadrante)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map result = documentSnapshot.getData();
                if(result == null){return;}
                Set<String> keySet = result.keySet();
                for(String key : keySet){
                    getFuente(key, callback);
                }
            }
        });
    }

    public void getCuadrante(LatLng latLng, OnSuccessListener<DocumentSnapshot> ds){
        int cuadrante = getCuadrante(latLng.latitude, latLng.longitude);

        cuadrantesReference.document(String.valueOf(cuadrante)).get().addOnSuccessListener(ds);
    }

    public void getFuente(String id, OnSuccessListener<DocumentSnapshot> callback){
        fuentesReference.document(id).get().addOnSuccessListener(callback);
    }

    /*public void addFuenteToDB(final Fuente fuente) {
        double lat = fuente.getLatLng().latitude;
        double lon = fuente.getLatLng().longitude;
        final int cuadrante = getCuadrante(lat, lon);

        Log.i("Cuadrante", String.valueOf(cuadrante));
        fuentesReference.add(fuente).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                DocumentReference dr = task.getResult();

                HashMap<String, Object> aux = new HashMap<>();
                aux.put(dr.getId(), dr);
                cuadrantesReference.document(String.valueOf(cuadrante)).set(aux, SetOptions.merge());
            }
        });
    }*/

    public void getUserNickname(OnSuccessListener<DocumentSnapshot> successListener){
        String uid = user.getUid();
        nicknamesReference.document(uid).get().addOnSuccessListener(successListener);
    }

    public void getNickname(String uid, OnSuccessListener<DocumentSnapshot> successListener){
        nicknamesReference.document(uid).get().addOnSuccessListener(successListener);
    }

    public void addPetition(Fuente fuente, final Bitmap bitmap, final OnSuccessListener successListener , final OnFailureListener failureListener){
        final HashMap<String, Object> aux = new HashMap<>();
        aux.put("creador", fuente.getCreador());
        aux.put("latLng", fuente.getLatLng());
        aux.put("titulo", fuente.getTitulo());
        aux.put("descripcion", fuente.getDescripcion());

        if(bitmap == null){
            aux.put("foto", "");
            Task<DocumentReference> task = petitionsReference.add(aux);
            task.addOnSuccessListener(successListener).addOnFailureListener(failureListener);
        }else{
            Task<DocumentReference> task = petitionsReference.add(aux);
            task.addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    DocumentReference dr = task.getResult();

                    String id = dr.getId();
                    aux.put("foto", id);
                    petitionsReference.document(id).update(aux);

                    uploadFoto(id, bitmap, successListener, failureListener);
                }
            }).addOnFailureListener(failureListener);
        }
    }

    private void uploadFoto(String id, Bitmap bitmap, OnSuccessListener successListener, OnFailureListener failureListener){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();

        UploadTask uploadTask = peticionesImagenes.child(id).putBytes(data);

        uploadTask.addOnSuccessListener(successListener).addOnFailureListener(failureListener);
    }

    public void addRating(Fuente fuente, int rating){
        String uid = user.getUid();
        HashMap<String, Long> data = new HashMap<>();
        data.put(uid, (long) rating);
        ratingsReference.document(fuente.getId()).set(data, SetOptions.merge());
    }

    public void getRatings(Fuente fuente, OnSuccessListener<DocumentSnapshot> ds){
        ratingsReference.document(fuente.getId()).get().addOnSuccessListener(ds);
    }

    public void getFotoFuente(Fuente fuente, OnSuccessListener<byte[]> successListener){
        if(fuente.getFoto() != null && !fuente.getFoto().equals("")){
            StorageReference reference = imagenesFuentes.child(fuente.getFoto());
            reference.getBytes(1024*1024).addOnSuccessListener(successListener);
        }
    }

    public double getDistance(LatLng latLng1, LatLng latLng2){
        double lat1 = latLng1.latitude;
        double lon1 = latLng1.longitude;

        double lat2 = latLng2.latitude;
        double lon2 = latLng2.longitude;

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    private int getCuadrante(double lat, double lon) {
        double lCuadrantes = 0.01;
        int nFilas = (int) (180 / lCuadrantes);

        int nLatitud = (int) Math.floor(lat / lCuadrantes);
        int nLongitud = (int) (Math.floor(lon / lCuadrantes) * nFilas);

        return (int) Math.floor(nLatitud + nLongitud) + 162009000;
    }
}

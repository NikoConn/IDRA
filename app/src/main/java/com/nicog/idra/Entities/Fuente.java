package com.nicog.idra.Entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class Fuente implements java.io.Serializable {
    private String id;
    private transient LatLng latLng;
    private String titulo;
    private String descripcion;
    private String foto;
    private String creador;

    public Fuente(String id, String titulo, String descripcion, String foto, String creador, double lat, double lon) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.foto = foto;
        this.latLng = new LatLng(lat, lon);
        this.creador = creador;
    }

    public Fuente() {
    }

    public static Fuente fromDocumentSnapshot(DocumentSnapshot documentSnapshot){
        String id = documentSnapshot.getId();
        String foto = (String) documentSnapshot.get("foto");
        Map<String, Double> map = (Map<String, Double>) documentSnapshot.get("latLng");
        LatLng latLng = new LatLng(map.get("latitude"), map.get("longitude"));
        String titulo = (String) documentSnapshot.get("titulo");
        String descripcion = (String) documentSnapshot.get("descripcion");
        String creador = (String) documentSnapshot.get("creador");

        Fuente fuente = new Fuente(id, titulo, descripcion, foto, creador, latLng.latitude, latLng.longitude);

        return fuente;
    }

    public String getId() {
        return this.id;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public LatLng getLatLng() {
        return this.latLng;
    }

    public String getFoto() {
        return this.foto;
    }

    public String getCreador() {
        return this.creador;
    }

    public String getDescripcion(){
        return this.descripcion;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(latLng.latitude);
        out.writeDouble(latLng.longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        latLng = new LatLng(in.readDouble(), in.readDouble());
    }

    @Override
    public boolean equals(Object o){
        return o instanceof Fuente && ((Fuente) o).id.equals(this.id);
    }

}

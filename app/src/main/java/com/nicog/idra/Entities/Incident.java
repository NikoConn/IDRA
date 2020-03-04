package com.nicog.idra.Entities;

public class Incident {
    public static int fotografia = 0;
    public static int descripcion = 1;
    public static int inexistente = 2;
    public static int otro = 3;

    private int type;
    private String description;
    private String userUid;
    private String fuenteId;

    public Incident(){}

    public Incident(int type, String description, String userUid, String fuenteId){
        this.type = type;
        this.description = description;
        this.userUid = userUid;
        this.fuenteId = fuenteId;
    }

    public String getFuenteId() {
        return fuenteId;
    }

    public void setFuenteId(String fuenteId) {
        this.fuenteId = fuenteId;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

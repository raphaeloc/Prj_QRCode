package com.example.prj_qrcode;

public class Model {

    private String id;
    private String desconto;

    public Model() {
        this.id = "-1";
        this.desconto = "-1";
    }


    public Model(String id, String desconto) {
        this.id = id;
        this.desconto = desconto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesconto() {
        return desconto;
    }

    public void setDesconto(String desconto) {
        this.desconto = desconto;
    }
}
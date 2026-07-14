package com.Utc.RutaExpress.DTO;


public class loginValidar {
    private String nombre;
    private String password;

    public loginValidar(){}
        public String getnombre() {
        return nombre;
    }

    public void setnombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

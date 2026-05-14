package com.example.farmaventa.modelo;

import javafx.beans.property.*;

public class Persona {

    private final SimpleIntegerProperty id        = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombre    = new SimpleStringProperty();
    private final SimpleStringProperty  apellido  = new SimpleStringProperty();
    private final SimpleStringProperty  genero    = new SimpleStringProperty();
    private final SimpleStringProperty  telefono  = new SimpleStringProperty();
    private final SimpleStringProperty  correo    = new SimpleStringProperty();
    private final SimpleStringProperty  direccion = new SimpleStringProperty(); // ← nueva

    public Persona() {}

    public Persona(int id, String nombre, String apellido, String genero,
                   String telefono, String correo, String direccion) {
        this.id.set(id);
        this.nombre.set(nombre     != null ? nombre    : "");
        this.apellido.set(apellido != null ? apellido  : "");
        this.genero.set(genero     != null ? genero    : "");
        this.telefono.set(telefono != null ? telefono  : "");
        this.correo.set(correo     != null ? correo    : "");
        this.direccion.set(direccion != null ? direccion : "");
    }

    public SimpleIntegerProperty idProperty()       { return id; }
    public SimpleStringProperty  nombreProperty()   { return nombre; }
    public SimpleStringProperty  apellidoProperty() { return apellido; }
    public SimpleStringProperty  generoProperty()   { return genero; }
    public SimpleStringProperty  telefonoProperty() { return telefono; }
    public SimpleStringProperty  correoProperty()   { return correo; }
    public SimpleStringProperty  direccionProperty(){ return direccion; }

    public int    getId()              { return id.get(); }
    public void   setId(int v)         { id.set(v); }
    public String getNombre()          { return nombre.get(); }
    public void   setNombre(String v)  { nombre.set(v); }
    public String getApellido()        { return apellido.get(); }
    public void   setApellido(String v){ apellido.set(v); }
    public String getGenero()          { return genero.get(); }
    public void   setGenero(String v)  { genero.set(v); }
    public String getTelefono()        { return telefono.get(); }
    public void   setTelefono(String v){ telefono.set(v); }
    public String getCorreo()          { return correo.get(); }
    public void   setCorreo(String v)  { correo.set(v); }
    public String getDireccion()       { return direccion.get(); }
    public void   setDireccion(String v){ direccion.set(v); }
}
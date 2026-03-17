package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Mapea TBL_PERSONA + (JOIN TBL_CLIENTE o TBL_EMPLEADO + TBL_CARGO)
 */
public class Persona {

    private final SimpleIntegerProperty idPersona = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombre    = new SimpleStringProperty();
    private final SimpleStringProperty  apellido  = new SimpleStringProperty();
    private final SimpleStringProperty  tipo      = new SimpleStringProperty(); // "Cliente" o "Empleado"
    private final SimpleStringProperty  telefono  = new SimpleStringProperty();
    private final SimpleStringProperty  email     = new SimpleStringProperty();
    private final SimpleStringProperty  genero    = new SimpleStringProperty();
    private final SimpleStringProperty  cargo     = new SimpleStringProperty(); // de TBL_CARGO.nombre

    public Persona() {}

    public Persona(int idPersona, String nombre, String apellido, String tipo,
                   String telefono, String email, String genero, String cargo) {
        this.idPersona.set(idPersona);
        this.nombre.set(nombre != null ? nombre : "");
        this.apellido.set(apellido != null ? apellido : "");
        this.tipo.set(tipo != null ? tipo : "");
        this.telefono.set(telefono != null ? telefono : "");
        this.email.set(email != null ? email : "");
        this.genero.set(genero != null ? genero : "");
        this.cargo.set(cargo != null ? cargo : "");
    }

    public SimpleIntegerProperty idPersonaProperty() { return idPersona; }
    public SimpleStringProperty  nombreProperty()    { return nombre; }
    public SimpleStringProperty  apellidoProperty()  { return apellido; }
    public SimpleStringProperty  tipoProperty()      { return tipo; }
    public SimpleStringProperty  telefonoProperty()  { return telefono; }
    public SimpleStringProperty  emailProperty()     { return email; }
    public SimpleStringProperty  generoProperty()    { return genero; }
    public SimpleStringProperty  cargoProperty()     { return cargo; }

    public int    getIdPersona()        { return idPersona.get(); }
    public void   setIdPersona(int v)   { idPersona.set(v); }
    public String getNombre()           { return nombre.get(); }
    public void   setNombre(String v)   { nombre.set(v); }
    public String getApellido()         { return apellido.get(); }
    public void   setApellido(String v) { apellido.set(v); }
    public String getTipo()             { return tipo.get(); }
    public void   setTipo(String v)     { tipo.set(v); }
    public String getTelefono()         { return telefono.get(); }
    public void   setTelefono(String v) { telefono.set(v); }
    public String getEmail()            { return email.get(); }
    public void   setEmail(String v)    { email.set(v); }
    public String getGenero()           { return genero.get(); }
    public void   setGenero(String v)   { genero.set(v); }
    public String getCargo()            { return cargo.get(); }
    public void   setCargo(String v)    { cargo.set(v); }
}
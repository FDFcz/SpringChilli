package com.example.Structures;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String userName;
    private final int id;
    private List<Teracota> ownedTeracotas = new ArrayList<>();
    public User(int id, String userName) {
        this.id = id;
        this.userName = userName;
    }
    public String getUserName() {return userName;}
    public int getId() {return id;}
    public String getName(){return userName;}

    public void addTeracota(Teracota t) {ownedTeracotas.add(t);}
    public void removeTeracota(Teracota t) {ownedTeracotas.remove(t);}
    public List<Teracota> getOwnedTeracotas() {return ownedTeracotas;}

}

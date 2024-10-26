package com.example.Structures;

import com.example.Controlers.ChiliPeperApplication;

import java.sql.Date;

public class Teracota
{
    public static enum PlantTypes {
        Jalapenos,
        Poblano,
        Habareno
    }
    private int id;
    private String name;
    private Date plantedAt= null;
    private Plant plant;

    private int actualTemp;
    private int actualHumidity;
    private boolean actualLight;

    public Teracota(int id, String name, PlantTypes type,Date plantedAt)
    {
        this.id = id;
        this.name = name;
        this.plantedAt = plantedAt;
        plant = ChiliPeperApplication.getPlant(type.ordinal());
    }
    public Teracota(int id, String name, PlantTypes type)
    {
        this.id = id;
        this.name = name;
        plant = ChiliPeperApplication.getPlant(type.ordinal());
    }
    public Teracota(String name, PlantTypes type)
    {
        this.id = -1;
        this.name = name;
        plant = ChiliPeperApplication.getPlant(type.ordinal());
    }

    public int getId(){return id;}
    public int getPlantID(){return plant.getPlantTypes().ordinal();}
    public String getName() {return name;}
    public Date getPlantedAt() {return plantedAt;}
    public int getGrowDays(){return plant.getGrowDays();}
    public Plant getPlant() {return plant;}

    public void setActualTemp(int temp){actualTemp = temp;}
    public void setActualHumidity(int humidity){actualHumidity = humidity;}
    public void setActualLight(boolean light){actualLight = light;}

    public float getActualTemp() {return actualTemp;}
    public Boolean getActuallight() {return actualLight;}
    public float getActualHumidity() {return actualHumidity;}
}

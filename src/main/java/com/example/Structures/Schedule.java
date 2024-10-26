package com.example.Structures;

public class Schedule {
    private int id;
    private float temperature;
    private boolean light;
    private int humidity;
    public Schedule(int id, float temperature, boolean light, int humidity)
    {
        this.id = id;
        this.temperature = temperature;
        this.light = light;
        this.humidity = humidity;
    }
    public int getId() {return id;}
    public float getTemperature() {return temperature;}
    public boolean getLight() {return light;}
    public int getHumidity() {return humidity;}
}

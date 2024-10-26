package com.example.Structures;

import com.example.Controlers.ChiliPeperApplication;

public class Cron {
    private int id;
    private int tracota;
    private Schedule schedule;
    private int startTime,endTime;

    public Cron(int startTime, int endTime) {
        this.id = -1;

        this.schedule = null;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public Cron(int id, Schedule schedule, int startTime, int endTime) {
        this.id = id;

        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public Cron(int id, int terracotaID, Schedule schedule, int startTime, int endTime) {
        this.id = id;
        this.tracota = terracotaID;
        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public Cron(int id,int terracotaID, int scheduleID, int startTime, int endTime)
    {
        this.id = id;
        this.tracota = terracotaID;
        this.schedule = ChiliPeperApplication.getSchedule(scheduleID);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {return id;}
    public Schedule getSchedule() {return schedule;}
    public int getStartTime() {return startTime;}
    public int getEndTime() {return endTime;}
}

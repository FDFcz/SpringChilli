package com.example.Controlers;

import com.example.PLC.UDPSender;
import com.example.Structures.Cron;
import com.example.Structures.PLC;
import com.example.Structures.Schedule;
import com.example.Structures.Teracota;

import java.time.LocalTime;

public class TeraccotaControler {

    private Cron actualCron;
    private Teracota teracota;
    private PLC plc;
    private UDPSender sender;

    public TeraccotaControler(Teracota teracota)
    {
        this.teracota = teracota;
        plc = ChiliPeperApplication.getPLC(teracota.getId());
        sender = new UDPSender(plc.getIp(),8888);
        updateCron(LocalTime.now().getHour());
    }

    public byte[] updateData()
    {
        try {
            byte offset = 0;
            byte[] data = new byte[4];
            Schedule actualschedule = actualCron.getSchedule();
            data[0] = (byte) actualschedule.getTemperature();
            data[1] = (byte) actualschedule.getHumidity();
            data[2] = (byte) ((actualschedule.getLight()) ? 0 : 1);
            data[3] = offset;

            data = sender.sentData(data);
            if(data==null)
            {
                System.out.println("PLC didnt reponded");
                return null;
            }
            teracota.setActualTemp(data[0]);
            teracota.setActualHumidity(data[1]);
            if(data[2]==0) teracota.setActualLight(false);else teracota.setActualLight(true);
            return data;
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }
    public void updateCron(int hour)
    {
        actualCron = ChiliPeperApplication.getActiveCronForTeracota(teracota.getId(),hour);
    }
    public Teracota getTeracota(){return teracota;}
}

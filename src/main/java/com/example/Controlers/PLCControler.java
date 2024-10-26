package com.example.Controlers;

import com.example.Structures.Teracota;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class PLCControler
{
    private HashMap<Integer,TeraccotaControler> teraccotaControlers = new HashMap<>();
    private List<Integer> keysToRemove = new ArrayList<>();
    private List<Teracota> teracotasToAdd= new ArrayList<>();
    private Thread PLCMesagingThread;

    public PLCControler()
    {
        Teracota[] teracotas = ChiliPeperApplication.getAllTeracotas();
        for (Teracota teracota : teracotas) teraccotaControlers.put(teracota.getId(),new TeraccotaControler(teracota));
        PLCMesagingThread = new Thread(() -> {loop();});
        PLCMesagingThread.start();

    }
    public void addTeracota(Teracota teracota)
    {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        teracotasToAdd.add(teracota);
        lock.unlock();
    }
    public void removeTeracota(int teracotaID)
    {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        keysToRemove.add(teracotaID);
        lock.unlock();
    }
    public void updateCron(int teracotaID)
    {
        teraccotaControlers.get(teracotaID).updateCron(LocalTime.now().getHour());
    }
    public Teracota getActualValues(int teracotaID)
    {
        if(teraccotaControlers.get(teracotaID) == null)
        {
            return ChiliPeperApplication.getTeracotaFromDatabese(teracotaID);
        }
        return teraccotaControlers.get(teracotaID).getTeracota();
    }
    private void loop()
    {
        while (true)
        {
            ReentrantLock lock = new ReentrantLock();
            lock.lock();
            for(Teracota teracota : teracotasToAdd)
            {
                teraccotaControlers.put(teracota.getId(),new TeraccotaControler(teracota));
            }
            teracotasToAdd.clear();
            lock.unlock();

            teraccotaControlers.forEach((k,v)-> {
                v.updateData();
                try {
                    Thread.sleep(2000);
                }
                catch (ConcurrentModificationException e)
                {
                    System.out.println("ERROR");
                }
                catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
            });
            lock.lock();
            for(int i=0;i<keysToRemove.size();i++)
            {
                teraccotaControlers.remove(keysToRemove.get(i));
            }
            keysToRemove.clear();
            lock.unlock();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package com.example.PLC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class UDPSender {
    InetAddress destination;
    int port;

    public UDPSender(InetAddress destination, int port) {
        this.destination = destination;
        this.port = port;
    }
    public UDPSender(String destination, int port)  {
        try {
            this.destination = InetAddress.getByName(destination);
        }
        catch (UnknownHostException e) {
            System.out.println("IP address is not valid");
        }
        this.port = port;
    }

    public byte[] sentData(byte[] data) throws SocketException {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, destination, port);
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
            System.out.println(InetAddress.getLocalHost().getHostAddress() + " -> " + data[0] + " TO " + destination.getHostAddress() + ":" + port);
            return waitForResponse(datagramSocket);
        }catch (Exception e) {System.out.println(e);}
        return null;
    }
    private byte[] waitForResponse(DatagramSocket serverSocket) throws SocketException {
        DatagramPacket receivePacket = new DatagramPacket(new byte[4], 4);
        byte[] newParameters = null;
        try {

            serverSocket.setSoTimeout(2000);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("RECEIVED: " + sentence);
            newParameters = receivePacket.getData();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            serverSocket.close();
            System.out.println("TIMEOUT");
            return null;
        }
        serverSocket.close();
        return newParameters;
    }
}

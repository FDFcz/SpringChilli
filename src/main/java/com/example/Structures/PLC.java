package com.example.Structures;

public class PLC {
    private String ip;
    private boolean online;
    //private int port;
    private int offset;
    public PLC(String ip, int offset) {this.ip = ip;this.offset = offset;}

    public String getIp() {return ip;}
}

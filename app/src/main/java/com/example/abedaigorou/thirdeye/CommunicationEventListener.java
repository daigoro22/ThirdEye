package com.example.abedaigorou.thirdeye;

interface CommunicationEventListener
{
    void onConnect(String mes);
    void onConnected(String mes);
    void onDiconnect(String mes);
    byte[] onSend();
    void onRead(byte[] getter);
}

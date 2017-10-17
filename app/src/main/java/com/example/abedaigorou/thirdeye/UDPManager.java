package com.example.abedaigorou.thirdeye;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by abedaigorou on 2017/07/13.
 */

public class UDPManager
{
    private DatagramPacket senderDatagram,getterDatagram,senderDatagram_sub;
    private DatagramSocket SenderUdpSocket,GetterUdpSocket;
    private String address;
    private int port;
    private CommunicationEventListener listener;
    private NODE_TYPE nodeType;

    private Handler sendHandler,readHandler;
    private boolean run=true;

    private int bufferSize;
    private byte[] sender;
    private byte[] senderBuffer;
    private byte[] senderPacketBuffer;
    private byte[] byteSender;
    //private byte[] senderPacket;
    private byte[] getterBuffer;
    private byte[] getter;
    private byte[] getterPacketBuffer;
    private byte[] simpleGetterBuffer;
    private byte[] simpleGetter;
    //private byte[] getterPacket;
    private int senderOffset=0,getterOffset=0;
    private int packetSize;
    private final byte[] sync={(byte)127,(byte)68,(byte)99,(byte)16};
    private byte[] syncBuf=new byte[4];
    private final int sendDelay=0;
    private final int lastSendDelay=10;
    private int senderPacketNum,getterPacketNum;
    private int maxIndexNum,getterIndexNum=0;
    private boolean isDataDevided;
    private String TAG="UDPManager";


    enum NODE_TYPE{
        TCPSERVER("TCPサーバー"),
        TCPCLIENT("TCPクライアント"),
        UDPSERVER("UDPサーバー"),
        UDPCLIENT("UDPクライアント"),
        BLUETOOTHSERVER("BLUETOOTHサーバー"),
        BLUETOOTHCLIENT("BLUETOOTHクライアント");

        public String name;
        NODE_TYPE(String name){
            this.name=name;
        }
    }

    public UDPManager(CommunicationEventListener listener,int bufferSize,int packetSize){
        this.listener=listener;
        setBufferAndPacketSize(bufferSize,packetSize);
        //getterPacket=new byte[packetSize];
    }

    public void setBufferAndPacketSize(int bufferSize,int packetSize){
        this.bufferSize=bufferSize;
        sender=new byte[bufferSize];
        senderBuffer=new byte[bufferSize];
        byteSender=new byte[2];
        if(bufferSize>packetSize){//データ分割するかどうか
            this.packetSize=packetSize;
            isDataDevided=true;
        }
        else{
            packetSize=bufferSize;
            isDataDevided=false;
        }
        senderPacketBuffer=new byte[packetSize];
        //senderPacket=new byte[packetSize];

        getterBuffer=new byte[bufferSize];
        getter=new byte[bufferSize];
        getterPacketBuffer=new byte[packetSize];
        simpleGetter=new byte[2];
        simpleGetterBuffer=new byte[2];
        maxIndexNum=bufferSize/packetSize;
    }

    public void Connect(String host,int port,boolean isServer){
        nodeType=isServer?NODE_TYPE.UDPSERVER:NODE_TYPE.UDPCLIENT;

        run=true;
        this.port=port;
        this.address=host;

        listener.onConnect(nodeType.name);
        //Getter
        try {
            GetterUdpSocket=new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        getterDatagram=new DatagramPacket(getterBuffer,packetSize);

        //Setter
        InetAddress address=null;
        try {
            address=InetAddress.getByName(this.address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            SenderUdpSocket=new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        senderDatagram=new DatagramPacket(sender,packetSize,address,port);
        try {
            senderDatagram_sub=new DatagramPacket(sender,packetSize,InetAddress.getByName("192.168.1.2"),port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        listener.onConnected(nodeType.name);

        if(isServer){
            try {
                GetterUdpSocket.setReceiveBufferSize(packetSize);
                GetterUdpSocket.setSendBufferSize(packetSize);
                SenderUdpSocket.setReceiveBufferSize(2);
                SenderUdpSocket.setSendBufferSize(2);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            Send();
            Read();
        }else{
            try {
                GetterUdpSocket.setReceiveBufferSize(2);
                GetterUdpSocket.setSendBufferSize(2);
                SenderUdpSocket.setReceiveBufferSize(packetSize);
                SenderUdpSocket.setSendBufferSize(packetSize);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            Send();
            Simple2ByteRead();
        }
    }

    private void Sync_Read(){
        if(!isDataDevided)
            return;
        getterDatagram.setData(syncBuf,0,4);
        while(!Arrays.equals(syncBuf,sync)) {
            try {
                GetterUdpSocket.receive(getterDatagram);
                Log.i(TAG,"failed to sync");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void Simple2ByteRead(){
        (readHandler=Util.GetNewHandler("SimpleReadThread")).post(new Runnable() {
            @Override
            public void run() {
                while(run) {
                    getterDatagram.setData(simpleGetterBuffer,0,2);
                        try {
                            GetterUdpSocket.receive(getterDatagram);
                            System.arraycopy(simpleGetterBuffer,0,simpleGetter,0,2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    listener.onRead(simpleGetter);
                }
            }
        });
    }

    private void Read(){
        (readHandler=Util.GetNewHandler("ReadThread")).post(new Runnable() {
            @Override
            public void run() {
                while(run) {
                    Sync_Read();
                        ;/*try {
                            udpSocket_Client.receive(getterDatagram);
                           } catch (IOException e) {
                                e.printStackTrace();
                           }*/
                    getterDatagram.setData(getterPacketBuffer, 0, packetSize);
                    while (getterOffset < bufferSize) {
                        try {
                            GetterUdpSocket.receive(getterDatagram);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        getterPacketNum = getterPacketBuffer[0] & 0xff;//packet番号
                        if (getterPacketNum < maxIndexNum) {//64kbyteコピー
                            System.arraycopy(getterPacketBuffer, 1, getterBuffer, getterPacketNum * (packetSize - 1), packetSize - 1);
                        } else if (getterPacketNum == maxIndexNum) {//端数コピー
                            System.arraycopy(getterPacketBuffer, 1, getterBuffer, getterPacketNum * (packetSize - 1), getterDatagram.getLength() - 1);
                        } else//syncのとき
                            break;
                        getterOffset += getterDatagram.getLength() - 1;
                    }
                    if(!isDataDevided){
                        try {
                            GetterUdpSocket.receive(getterDatagram);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.arraycopy(getterPacketBuffer,0,getter,0,bufferSize);
                    }else {
                        System.arraycopy(getterBuffer, 0, getter, 0, bufferSize);
                        getterOffset = 0;
                    }

                    listener.onRead(getter);
                }
            }
        });
    }

    private void Sync_Send(){
        if(!isDataDevided)
            return;
        senderDatagram.setData(sync,0,4);
        senderDatagram_sub.setData(sync,0,4);
        try {
            SenderUdpSocket.send(senderDatagram);
            SenderUdpSocket.send(senderDatagram_sub);
            try {
                Thread.sleep(sendDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Send() {
        sendHandler=Util.GetNewHandler("SendThread");
    }

    public void Send2Byte(final byte data[]){
        if(sendHandler==null)
            return;
        sendHandler.post(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(data,0,byteSender,0,2);
                senderDatagram.setData(byteSender,0,2);
                try {
                    SenderUdpSocket.send(senderDatagram);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void SendData(final byte[] data){
        if(sendHandler==null)
            return;
        sendHandler.post(new Runnable() {
            @Override
            public void run() {
            Sync_Send();
            if((senderBuffer=data)==null)
                return;
            System.arraycopy(senderBuffer,0,sender,0,senderBuffer.length);
             //while(offset<) {
                //senderDatagram.setData(sender, offset += packetSize, packetSize);
                while (senderOffset+packetSize < bufferSize) {
                    senderPacketBuffer[0]=(byte)senderPacketNum;
                    System.arraycopy(sender,senderOffset,senderPacketBuffer,1,packetSize-1);
                    senderDatagram.setData(senderPacketBuffer,0,packetSize);
                    senderDatagram_sub.setData(senderPacketBuffer,0,packetSize);
                    try {
                        SenderUdpSocket.send(senderDatagram);
                        SenderUdpSocket.send(senderDatagram_sub);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    senderOffset += packetSize-1;
                    senderPacketNum++;
                }
                if(isDataDevided) {
                    senderPacketBuffer[0] = (byte) senderPacketNum;
                    System.arraycopy(sender, senderOffset, senderPacketBuffer, 1, bufferSize - senderOffset);
                    senderDatagram.setData(senderPacketBuffer, 0, bufferSize - senderOffset);
                    senderDatagram_sub.setData(senderPacketBuffer,0,bufferSize-senderOffset);
                }else{
                    System.arraycopy(sender,0,senderPacketBuffer,0,sender.length);
                    senderDatagram.setData(senderBuffer,0,senderBuffer.length);
                    senderDatagram_sub.setData(senderBuffer,0,senderBuffer.length);
                }
                try {
                    SenderUdpSocket.send(senderDatagram);
                    SenderUdpSocket.send(senderDatagram_sub);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                senderPacketNum=0;
                senderOffset=0;
            }
        });
    }


    public void Disconnect(){
        run=false;
        if(SenderUdpSocket!=null)
            SenderUdpSocket.close();
        if(GetterUdpSocket!=null)
            GetterUdpSocket.close();
    }
}

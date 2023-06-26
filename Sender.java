import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Vector;

public class Sender implements Runnable {

    //instantiate the sockets, set them to null for now
    private DatagramSocket socket1 = null;
    private DatagramSocket2 socket2 = null;
    private DatagramSocket3 socket3 = null;
    private DatagramSocket4 socket4 = null;

    static AudioRecorder recorder = null;

    private int PORT = 55555;
    private InetAddress clientIP = null;
    private int authCode = 69420;
    private int encryptionKey = 25512361;

    //constructor, takes socket number as string and initialises the socket based on which one was passed through
    public Sender(String socket) {
        switch (socket) {
            case "1":
                try {
                    socket1 = new DatagramSocket();
                    recorder = new AudioRecorder();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when declaring DatagramSocket1!");
                    e.printStackTrace();
                    System.exit(0);
                }
            case "2":
                try{
                    socket2 = new DatagramSocket2();
                    recorder = new AudioRecorder();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when declaring DatagramSocket2!");
                    e.printStackTrace();
                    System.exit(0);
                }
            case "3":
                try{
                    socket3 = new DatagramSocket3();
                    recorder = new AudioRecorder();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when declaring DatagramSocket3!");
                    e.printStackTrace();
                    System.exit(0);
                }
            case "4":
                try{
                    socket4 = new DatagramSocket4();
                    recorder = new AudioRecorder();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when declaring DatagramSocket4!");
                    e.printStackTrace();
                    System.exit(0);
                }
            default:
                System.out.println("[Sender] Invalid parameter was passed to Sender constructor, shouldn't happen.");
                System.exit(0);
        }
    }


    @Override
    public void run() {

        //fetch client IP
        try {
            clientIP = InetAddress.getByName("localhost");
        }
        catch (UnknownHostException e) {
            System.out.println("ERROR: [Sender] Problem occurred when fetching client IP!");
            e.printStackTrace();
            System.exit(0);
        }

        //testing the socket
        int noOfDummyPacketsToSend = 1000;
        int dummyPacketsSent = 0;

        if(socket1 != null) {

            //ANALYSING SOCKET
            System.out.println("------------------------------------");
            System.out.println("[Sender] Testing socket 1...");

            for(int i = 0; i < noOfDummyPacketsToSend; i++){
                ByteBuffer dummyPacket = ByteBuffer.allocate(8); //520 allocated
                dummyPacket.putInt(authCode); //packet number header
                dummyPacket.putInt(dummyPacketsSent); //packet auth code header
                DatagramPacket packet = new DatagramPacket(dummyPacket.array(), dummyPacket.array().length, clientIP, PORT);
                try {
                    socket1.send(packet);
                    dummyPacketsSent++;
                }
                catch (IOException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when attempting to send dummy packets for testing!");
                    e.printStackTrace();
                }
            }
            System.out.println("[Sender] Total dummy packets sent across socket 1: " + dummyPacketsSent);

            try
            {
                Thread.sleep(5500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("[Sender] Speak into the microphone.");

            boolean running = true;

            while(running) {

                byte[] block = new byte[0];
                try {

                    //get user voice input and store in blocks
                    block = recorder.getBlock(); //512 bytes allocated to byte array

                    ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                    ByteBuffer plainText = ByteBuffer.wrap(block);
                    for(int j = 0; j < block.length / 4; j++) {
                        int fourByte = plainText.getInt();
                        fourByte = fourByte ^ encryptionKey;
                        unwrapEncrypt.putInt(fourByte);
                    }
                    byte[] encryptedBlock = unwrapEncrypt.array();

                    //create a packet with 516 bytes capacity, 512 for payload 4 for packetNumberHeader
                    ByteBuffer VoIPpacket = ByteBuffer.allocate(512);
                    VoIPpacket.put(encryptedBlock);

                    //send the packet
                    DatagramPacket packet = new DatagramPacket(VoIPpacket.array(), VoIPpacket.array().length, clientIP, PORT);
                    socket1.send(packet);



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            socket1.close();
        }
        if(socket2 != null) {

            //ANALYSING SOCKET
            System.out.println("------------------------------------");
            System.out.println("[Sender] Testing socket 2...");

            for(int i = 0; i < noOfDummyPacketsToSend; i++){
                ByteBuffer dummyPacket = ByteBuffer.allocate(8); //520 allocated
                dummyPacket.putInt(authCode); //packet number header
                dummyPacket.putInt(dummyPacketsSent); //packet auth code header
                DatagramPacket packet = new DatagramPacket(dummyPacket.array(), dummyPacket.array().length, clientIP, PORT);
                try {
                    socket2.send(packet);
                    dummyPacketsSent++;
                }
                catch (IOException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when attempting to send dummy packets for testing!");
                    e.printStackTrace();
                }
            }
            System.out.println("[Sender] Total dummy packets sent across socket 2: " + dummyPacketsSent);

            try
            {
                Thread.sleep(5500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("[Sender] Speak into the microphone.");

            int packetNumber = 0;
            boolean running = true;
            Vector<DatagramPacket> packetVector = new Vector<DatagramPacket>();
            Interleaver interleaver = null; // 4x4 Matrix
            try
            {
                interleaver = new Interleaver(6);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            while(running) {

                byte[] block = new byte[0];
                try {

                    //get user voice input and store in blocks
                    block = recorder.getBlock(); //512 bytes allocated to byte array

                    //encrypt the block data
                    ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                    ByteBuffer plainText = ByteBuffer.wrap(block);
                    for(int j = 0; j < block.length / 4; j++) {
                        int fourByte = plainText.getInt();
                        fourByte = fourByte ^ encryptionKey;
                        unwrapEncrypt.putInt(fourByte);
                    }
                    byte[] encryptedBlock = unwrapEncrypt.array();

                    //create a packet with 516 bytes capacity, 512 for payload 4 for packetNumberHeader
                    ByteBuffer VoIPpacket = ByteBuffer.allocate(520);
                    VoIPpacket.putInt(authCode);
                    VoIPpacket.putInt(packetNumber);
                    VoIPpacket.put(encryptedBlock);
                    packetNumber++;

                    //send the packet
                    DatagramPacket packet = new DatagramPacket(VoIPpacket.array(), VoIPpacket.array().length, clientIP, PORT);

                    if (packetVector.size() < 36)
                        packetVector.add(packet);
                    else {
                        DatagramPacket[][] populatedArray = interleaver.populateArray(packetVector);
                        int matrixSpread = interleaver.getSpread();

                        for (int i = 0; i < matrixSpread; i++) {
                            for (int j = 0; j < matrixSpread; j++) {
                                socket2.send(populatedArray[j][i]);
                            }
                        }

                        packetVector.removeAllElements();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(socket3 != null) {

            //ANALYSING SOCKET
            System.out.println("------------------------------------");
            System.out.println("[Sender] Testing socket 3...");

            for(int i = 0; i < noOfDummyPacketsToSend; i++){
                ByteBuffer dummyPacket = ByteBuffer.allocate(8); //520 allocated
                dummyPacket.putInt(authCode); //packet number header
                dummyPacket.putInt(dummyPacketsSent); //packet auth code header
                DatagramPacket packet = new DatagramPacket(dummyPacket.array(), dummyPacket.array().length, clientIP, PORT);
                try {
                    socket3.send(packet);
                    dummyPacketsSent++;
                }
                catch (IOException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when attempting to send dummy packets for testing!");
                    e.printStackTrace();
                }
            }
            System.out.println("[Sender] Total dummy packets sent across socket 3: " + dummyPacketsSent);

            try
            {
                Thread.sleep(5500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("[Sender] Speak into the microphone.");

            int packetNumber = 0;
            boolean running = true;
            Vector<DatagramPacket> packetVector = new Vector<DatagramPacket>();
            Interleaver interleaver = null; // 4x4 Matrix
            try
            {
                interleaver = new Interleaver(5);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            while(running) {

                byte[] block = new byte[0];
                try {

                    //get user voice input and store in blocks
                    block = recorder.getBlock(); //512 bytes allocated to byte array

                    //encrypt the block data
                    ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                    ByteBuffer plainText = ByteBuffer.wrap(block);
                    for(int j = 0; j < block.length / 4; j++) {
                        int fourByte = plainText.getInt();
                        fourByte = fourByte ^ encryptionKey;
                        unwrapEncrypt.putInt(fourByte);
                    }
                    byte[] encryptedBlock = unwrapEncrypt.array();

                    //create a packet with 516 bytes capacity, 512 for payload 4 for packetNumberHeader
                    ByteBuffer VoIPpacket = ByteBuffer.allocate(520);
                    VoIPpacket.putInt(authCode);
                    VoIPpacket.putInt(packetNumber);
                    VoIPpacket.put(encryptedBlock);
                    packetNumber++;

                    //send the packet
                    DatagramPacket packet = new DatagramPacket(VoIPpacket.array(), VoIPpacket.array().length, clientIP, PORT);

                    if (packetVector.size() < 25)
                        packetVector.add(packet);
                    else {
                        DatagramPacket[][] populatedArray = interleaver.populateArray(packetVector);
                        int matrixSpread = interleaver.getSpread();

                        for (int i = 0; i < matrixSpread; i++) {
                            for (int j = 0; j < matrixSpread; j++) {
                                socket3.send(populatedArray[j][i]);
                            }
                        }

                        packetVector.removeAllElements();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(socket4 != null) {

            //ANALYSING SOCKET
            System.out.println("------------------------------------");
            System.out.println("[Sender] Testing socket 4...");

            for(int i = 0; i < noOfDummyPacketsToSend; i++){
                ByteBuffer dummyPacket = ByteBuffer.allocate(8); //520 allocated
                dummyPacket.putInt(authCode); //packet number header
                dummyPacket.putInt(dummyPacketsSent); //packet auth code header
                //byte[] emptyPayload = new byte[512];
                //dummyPacket.put(emptyPayload);
                DatagramPacket packet = new DatagramPacket(dummyPacket.array(), dummyPacket.array().length, clientIP, PORT);
                try {
                    socket4.send(packet);
                    dummyPacketsSent++;
                }
                catch (IOException e) {
                    System.out.println("ERROR: [Sender] Problem occurred when attempting to send dummy packets for testing!");
                    e.printStackTrace();
                }
            }
            System.out.println("[Sender] Total dummy packets sent across socket 4: " + dummyPacketsSent);

            try
            {
                Thread.sleep(5500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("[Sender] Speak into the microphone.");

            int packetNumber = 0;
            boolean running = true;
            Vector<DatagramPacket> packetVector = new Vector<DatagramPacket>();
            Interleaver interleaver = null; // 4x4 Matrix
            try
            {
                interleaver = new Interleaver(6);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            while(running) {

                byte[] block = new byte[0];
                try {

                    //get user voice input and store in blocks
                    block = recorder.getBlock(); //512 bytes allocated to byte array

                    //encrypt the block data
                    ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                    ByteBuffer plainText = ByteBuffer.wrap(block);
                    for(int j = 0; j < block.length / 4; j++) {
                        int fourByte = plainText.getInt();
                        fourByte = fourByte ^ encryptionKey;
                        unwrapEncrypt.putInt(fourByte);
                    }
                    byte[] encryptedBlock = unwrapEncrypt.array();

                    //create a packet with 516 bytes capacity, 512 for payload 4 for packetNumberHeader
                    ByteBuffer VoIPpacket = ByteBuffer.allocate(520);
                    VoIPpacket.putInt(authCode);
                    VoIPpacket.putInt(packetNumber);
                    VoIPpacket.put(encryptedBlock);
                    packetNumber++;

                    //send the packet
                    DatagramPacket packet = new DatagramPacket(VoIPpacket.array(), VoIPpacket.array().length, clientIP, PORT);

                    if (packetVector.size() < 36)
                        packetVector.add(packet);
                    else {
                        DatagramPacket[][] populatedArray = interleaver.populateArray(packetVector);
                        int matrixSpread = interleaver.getSpread();

                        for (int i = 0; i < matrixSpread; i++) {
                            for (int j = 0; j < matrixSpread; j++) {
                                socket4.send(populatedArray[j][i]);
                            }
                        }

                        packetVector.removeAllElements();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

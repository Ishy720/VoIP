import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Receiver implements Runnable {

    //instantiate the sockets, set them to null for now
    private DatagramSocket receiving_socket1 = null;
    private DatagramSocket2 receiving_socket2 = null;
    private DatagramSocket3 receiving_socket3 = null;
    private DatagramSocket4 receiving_socket4 = null;

    private int PORT = 55555;
    private int authCode = 69420;
    private AudioPlayer player;

    public Receiver(String socket) {
        switch (socket) {
            case "1":
                try {
                    receiving_socket1 = new DatagramSocket(PORT);
                    player = new AudioPlayer();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Receiver] Problem occurred when declaring DatagramSocket1!");
                    e.printStackTrace();
                    System.exit(0);
                }
            case "2":
                try{
                    receiving_socket2 = new DatagramSocket2(PORT);
                    player = new AudioPlayer();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Receiver] Problem occurred when declaring DatagramSocket2!");
                    e.printStackTrace();
                    System.exit(0);
                }
            case "3":
                try{
                    receiving_socket3 = new DatagramSocket3(PORT);
                    player = new AudioPlayer();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Receiver] Problem occurred when declaring DatagramSocket3!");
                    e.printStackTrace();
                    System.exit(0);
                }
            case "4":
                try{
                    receiving_socket4 = new DatagramSocket4(PORT);
                    player = new AudioPlayer();
                    break;
                }
                catch (SocketException | LineUnavailableException e) {
                    System.out.println("ERROR: [Receiver] Problem occurred when declaring DatagramSocket4!");
                    e.printStackTrace();
                    System.exit(0);
                }
            default:
                System.out.println("Invalid parameter was passed to Receiver constructor, shouldn't happen.");
                System.exit(0);
        }
    }

    @Override
    public void run() {

        int[] packet_burst_histogram = new int[35]; //maximum burst length we will look for is 10, after manually analysing

        int previousPacketNumberReceived = 0;
        int currentPacketNumber = 0;
        int genuinePacketsReceived = 0;
        int packetsReceived = 0;

        Vector<byte[]> bufferVector = new Vector<byte[]>(16);
        List<BufferObject> bufferObjectList = new ArrayList<BufferObject>(16);

        if(receiving_socket1 != null) {
            System.out.println("[Receiver] Receiving through socket 1: Analyzing...");

            //receive dummy packets
            boolean receiving = true;

            while(receiving) {

                try {

                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket1.receive(packet);
                    receiving_socket1.setSoTimeout(5000); //5 seconds
                    packetsReceived++;

                    //------------------------------------------------------------------
                    //extract the headers
                    //------------------------------------------------------------------

                    //extract the packet number header associated with the packet
                    ByteBuffer wrapped = ByteBuffer.wrap(buffer);
                    int authCodeReceived = wrapped.getInt();
                    if(authCodeReceived == 69420) {
                        genuinePacketsReceived++;
                    }
                    int packetNumberReceived = wrapped.getInt();

                    if(authCodeReceived == authCode) {
                        if(packetNumberReceived == 0) {
                            currentPacketNumber = packetNumberReceived;
                        }
                        else {
                            int tempCurrentPacketNumber = currentPacketNumber;
                            currentPacketNumber = packetNumberReceived;
                            previousPacketNumberReceived = tempCurrentPacketNumber;

                            int burstLength = currentPacketNumber - previousPacketNumberReceived;
                            if(burstLength > 1 && burstLength < 36) {
                                packet_burst_histogram[burstLength]++;
                            }
                        }
                    }

                }
                catch (SocketTimeoutException e) {
                    System.out.println("[Receiver] Analysis complete.");
                    receiving = false;
                }
                catch (SocketException e) {
                    System.out.println("[Receiver] Issue with socket during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("[Receiver] Problem occurred during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }

            System.out.println("[Receiver] Number of packets received: " + packetsReceived);
            System.out.println("[Receiver] Number of genuine packets received: " + genuinePacketsReceived);
            System.out.println("[Receiver] Loss rate: " + (1000 - genuinePacketsReceived) / 10 + "%");

            System.out.println("[Receiver] Socket 1 burst analysis [MAX BURST LENGTH RECORDING IS 35]:");
            for(int i = 1; i < 36; i++) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            for(int i: packet_burst_histogram) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            System.out.println("------------------------------------");

            receiving = true;
            while(receiving) {

                //decryption key
                int key = 25512361;

                try {
                    byte[] buffer = new byte[512]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 512);
                    receiving_socket1.receive(packet);

                    ByteBuffer wrappedPayload = ByteBuffer.wrap(buffer);
                    //instantiate a new bytebuffer to store the decrypted payload
                    ByteBuffer unwrappedPayload = ByteBuffer.allocate(buffer.length);

                    for(int i = 0; i < wrappedPayload.array().length / 4; i++) {
                        int fourByte = wrappedPayload.getInt();
                        fourByte = fourByte ^ key;
                        unwrappedPayload.putInt(fourByte);
                    }

                    byte[] decryptedAudioBlock = unwrappedPayload.array();

                    player.playBlock(buffer);

                }
                catch (Exception e) {
                    System.out.println("ERROR: Receiver: Something wrong with receiving packets!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }

        if(receiving_socket2 != null) {
            System.out.println("[Receiver] Receiving through socket 2: Analyzing...");

            //receive dummy packets
            boolean receiving = true;

            while(receiving) {

                try {

                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket2.receive(packet);
                    receiving_socket2.setSoTimeout(5000); //5 seconds
                    packetsReceived++;

                    //------------------------------------------------------------------
                    //extract the headers
                    //------------------------------------------------------------------

                    //extract the packet number header associated with the packet
                    ByteBuffer wrapped = ByteBuffer.wrap(buffer);
                    int authCodeReceived = wrapped.getInt();
                    if(authCodeReceived == 69420) {
                        genuinePacketsReceived++;
                    }
                    int packetNumberReceived = wrapped.getInt();

                    if(authCodeReceived == authCode) {
                        if(packetNumberReceived == 0) {
                            currentPacketNumber = packetNumberReceived;
                        }
                        else {
                            int tempCurrentPacketNumber = currentPacketNumber;
                            currentPacketNumber = packetNumberReceived;
                            previousPacketNumberReceived = tempCurrentPacketNumber;

                            int burstLength = currentPacketNumber - previousPacketNumberReceived;
                            if(burstLength > 1 && burstLength < 36) {
                                packet_burst_histogram[burstLength]++;
                            }
                        }
                    }

                }
                catch (SocketTimeoutException e) {
                    System.out.println("[Receiver] Analysis complete.");
                    receiving = false;
                }
                catch (SocketException e) {
                    System.out.println("[Receiver] Issue with socket during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("[Receiver] Problem occurred during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            System.out.println("[Receiver] Number of packets received: " + packetsReceived);
            System.out.println("[Receiver] Number of genuine packets received: " + genuinePacketsReceived);
            System.out.println("[Receiver] Loss rate: " + (1000 - genuinePacketsReceived) / 10 + "%");

            System.out.println("[Receiver] Socket 2 burst analysis [MAX BURST LENGTH RECORDING IS 35]:");
            for(int i = 1; i < 36; i++) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            for(int i: packet_burst_histogram) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            System.out.println("------------------------------------");

            receiving = true;
            while(receiving) {

                //decryption key
                int key = 25512361;

                try {
                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket2.receive(packet);
                    bufferVector.add(buffer);

                    if (bufferVector.size() == 36) {
                        //For loop to remove non authorised buffers (packets).
                        for (int i = 0; i < bufferVector.size(); i++) {
                            ByteBuffer wrapped = ByteBuffer.wrap(bufferVector.get(i));
                            if (wrapped.getInt() != 69420)
                                bufferVector.remove(i);
                        }

                        for (int i = 0; i < bufferVector.size(); i++) {
                            BufferObject bufferObject = new BufferObject(bufferVector.get(i));
                            bufferObjectList.add(bufferObject);
                        }

                        Collections.sort(bufferObjectList);


                        for(int i = 0; i < bufferObjectList.size(); i++) {
                            byte[] payload = bufferObjectList.get(i).retrievePayload();
                            player.playBlock(payload);
                        }


                        bufferVector.removeAllElements();
                        bufferObjectList.clear();
                    }

                }
                catch (Exception e) {
                    System.out.println("ERROR: Receiver: Something wrong with receiving packets!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }

        if(receiving_socket3 != null) {
            System.out.println("[Receiver] Receiving through socket 3: Analyzing...");

            //receive dummy packets
            boolean receiving = true;

            while(receiving) {

                try {

                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket3.receive(packet);
                    receiving_socket3.setSoTimeout(5000); //5 seconds
                    packetsReceived++;
                    boolean analyzePackets = false;

                    //------------------------------------------------------------------
                    //extract the headers
                    //------------------------------------------------------------------


                    if (analyzePackets) {
                        bufferVector.add(buffer);

                        if (bufferVector.size() == 837) {


                            for (int i = 0; i < bufferVector.size(); i++) {
                                BufferObject bufferObject = new BufferObject(bufferVector.get(i));
                                bufferObjectList.add(bufferObject);
                            }

                            Collections.sort(bufferObjectList);

                            for (BufferObject bufferedObject : bufferObjectList) {
                                int authCodeReceived = bufferedObject.getAuthCode();
                                if(authCodeReceived == 69420) {
                                    genuinePacketsReceived++;
                                }
                                int packetNumberReceived = bufferedObject.getPacketNumber();

                                if(authCodeReceived == authCode) {
                                    if(packetNumberReceived == 0) {
                                        currentPacketNumber = packetNumberReceived;
                                    }
                                    else {
                                        int tempCurrentPacketNumber = currentPacketNumber;
                                        currentPacketNumber = packetNumberReceived;
                                        previousPacketNumberReceived = tempCurrentPacketNumber;

                                        int burstLength = currentPacketNumber - previousPacketNumberReceived;
                                        if(burstLength > 1 && burstLength < 36) {
                                            packet_burst_histogram[burstLength]++;
                                        }
                                    }
                                }
                            }


                        }
                    }


                    //extract the packet number header associated with the packet


                }
                catch (SocketTimeoutException e) {
                    System.out.println("[Receiver] Analysis complete.");
                    receiving = false;
                }
                catch (SocketException e) {
                    System.out.println("[Receiver] Issue with socket during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("[Receiver] Problem occurred during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            System.out.println("[Receiver] Number of packets received: " + packetsReceived);
            System.out.println("[Receiver] Number of genuine packets received: " + genuinePacketsReceived);
            System.out.println("[Receiver] Loss rate: " + (1000 - genuinePacketsReceived) / 10 + "%");

            System.out.println("[Receiver] Socket 3 burst analysis [MAX BURST LENGTH RECORDING IS 35]:");
            for(int i = 1; i < 36; i++) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            for(int i: packet_burst_histogram) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            System.out.println("------------------------------------");

            receiving = true;
            while(receiving) {

                //decryption key
                int key = 25512361;

                try {
                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket3.receive(packet);
                    bufferVector.add(buffer);

                    if (bufferVector.size() == 25) {
                        //For loop to remove non authorised buffers (packets).
                        for (int i = 0; i < bufferVector.size(); i++) {
                            ByteBuffer wrapped = ByteBuffer.wrap(bufferVector.get(i));
                            if (wrapped.getInt() != 69420)
                                bufferVector.remove(i);
                        }

                        for (int i = 0; i < bufferVector.size(); i++) {
                            BufferObject bufferObject = new BufferObject(bufferVector.get(i));
                            bufferObjectList.add(bufferObject);
                        }

                        Collections.sort(bufferObjectList);


                        for(int i = 0; i < bufferObjectList.size(); i++) {
                            byte[] payload = bufferObjectList.get(i).retrievePayload();
                            player.playBlock(payload);
                        }


                        bufferVector.removeAllElements();
                        bufferObjectList.clear();
                    }

                }
                catch (Exception e) {
                    System.out.println("ERROR: Receiver: Something wrong with receiving packets!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }

        if(receiving_socket4 != null) {
            System.out.println("[Receiver] Receiving through socket 4: Analyzing...");

            //receive dummy packets
            boolean receiving = true;

            while(receiving) {

                try {

                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket4.receive(packet);
                    receiving_socket4.setSoTimeout(5000); //5 seconds
                    packetsReceived++;

                    //------------------------------------------------------------------
                    //extract the headers
                    //------------------------------------------------------------------

                    //extract the packet number header associated with the packet
                    ByteBuffer wrapped = ByteBuffer.wrap(buffer);
                    int authCodeReceived = wrapped.getInt();
                    if(authCodeReceived == 69420) {
                        genuinePacketsReceived++;
                    }
                    int packetNumberReceived = wrapped.getInt();

                    if(authCodeReceived == authCode) {
                        if(packetNumberReceived == 0) {
                            currentPacketNumber = packetNumberReceived;
                        }
                        else {
                            int tempCurrentPacketNumber = currentPacketNumber;
                            currentPacketNumber = packetNumberReceived;
                            previousPacketNumberReceived = tempCurrentPacketNumber;

                            int burstLength = currentPacketNumber - previousPacketNumberReceived;
                            if(burstLength > 1 && burstLength < 36) {
                                packet_burst_histogram[burstLength]++;
                            }
                        }
                    }

                }
                catch (SocketTimeoutException e) {
                    System.out.println("[Receiver] Analysis complete.");
                    receiving = false;
                }
                catch (SocketException e) {
                    System.out.println("[Receiver] Issue with socket during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("[Receiver] Problem occurred during analysis!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            System.out.println("[Receiver] Number of packets received: " + packetsReceived);
            System.out.println("[Receiver] Number of genuine packets received: " + genuinePacketsReceived);
            System.out.println("[Receiver] Loss rate: " + (1000 - genuinePacketsReceived) / 10 + "%");

            System.out.println("[Receiver] Socket 4 burst analysis [MAX BURST LENGTH RECORDING IS 35]:");
            for(int i = 1; i < 36; i++) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            for(int i: packet_burst_histogram) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            System.out.println("------------------------------------");

            receiving = true;
            while(receiving) {

                //decryption key
                int key = 25512361;

                try {
                    byte[] buffer = new byte[520]; //byte array of size 520, 8 bytes for two int headers | 512 for payload
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
                    receiving_socket4.receive(packet);
                    bufferVector.add(buffer);

                    if (bufferVector.size() == 36) {

                        for (int i = 0; i < bufferVector.size(); i++) {
                            BufferObject bufferObject = new BufferObject(bufferVector.get(i));
                            if (bufferObject.getAuthCode() == 69420)
                                bufferObjectList.add(bufferObject);
                        }

                        Collections.sort(bufferObjectList);


                        for(int i = 0; i < bufferObjectList.size(); i++) {
                            byte[] payload = bufferObjectList.get(i).retrievePayload();
                            player.playBlock(payload);
                        }


                        bufferVector.removeAllElements();
                        bufferObjectList.clear();
                    }

                }
                catch (Exception e) {
                    System.out.println("ERROR: Receiver: Something wrong with receiving packets!");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }

    }
}

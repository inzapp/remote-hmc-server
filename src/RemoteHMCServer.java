import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class pRes {
    public static final int PORT = 8000;
    public static final int BUFSIZE = 128;
}

class PacketFileName {
    public static final String PACKET_DIR = "packet";
    public static final String SEARCH = PACKET_DIR + "\\search.txt";
}

class Packet {
    public static byte[] SEARCH;
}

class PacketLoader {
    public void loadAllPacket() {
        System.out.println("start loading packet from file...");
        Packet.SEARCH = load(PacketFileName.SEARCH);
        System.out.println("loading packet from file success\n");
    }

    private byte[] load(String fileName) {
        List<Byte> byteList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(PacketFileName.SEARCH));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;

                String[] splits = line.split(" ");
                for (String cur : splits) {
                    byte parsed10Base = (byte) Integer.parseInt(cur, 16);
                    byteList.add(parsed10Base);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] byteArr = new byte[pRes.BUFSIZE];
        for (int i = 0; i < byteList.size(); ++i)
            byteArr[i] = byteList.get(i);

//        for (byte cur : byteArr)
//            System.out.println(cur & 0xFF);
        return byteArr;
    }
}

public class RemoteHMCServer {
    public static void main(String[] args) {
        PacketLoader packetLoader = new PacketLoader();
        packetLoader.loadAllPacket();
//        try {
//            String myIp = InetAddress.getLocalHost().getHostAddress();
//            DatagramSocket dSocket = new DatagramSocket();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
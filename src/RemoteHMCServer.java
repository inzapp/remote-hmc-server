import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class pRes {
    public static final int PORT_TCP = 8000;
    public static final int PORT_UDP = 30600;
    public static final int TCP_CONN_TIMEOUT = 5000;
    public static final int BUFSIZE = 128;
}

class PacketFileName {
    public static final String PACKET_DIR = "packet";
    public static final String SEARCH = PACKET_DIR + "\\search.txt";
    public static final String MATRIX = PACKET_DIR + "\\matrix.txt";
    public static final String MULTI_VIEWER_111 = PACKET_DIR + "\\multi_viewer_111.txt";

}

class Packet {
    public static byte[] SEARCH;
    public static byte[] MATRIX;
    public static byte[] MULTI_VIEWER_111;
}

class Command {
    public static final int MATRIX = 1;
    public static final int MULTI_VIEWER_111 = 2;
}

class PacketLoader {
    public void loadAllPacket() {
        System.out.println("start loading packet from file...");
        Packet.SEARCH = load(PacketFileName.SEARCH);
        Packet.MATRIX = load(PacketFileName.MATRIX);
        Packet.MULTI_VIEWER_111 = load(PacketFileName.MULTI_VIEWER_111);
        System.out.println("loading packet from file success\n");
    }

    private byte[] load(String fileName) {
        List<Byte> byteList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
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

        byte[] byteArr = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); ++i)
            byteArr[i] = byteList.get(i);

//        for (byte cur : byteArr)
//            System.out.println(cur & 0xFF);

        return byteArr;
    }
}

class Server {
    private Socket socket;
    private Thread receiveThread;

    public Server() {
        receiveThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[pRes.BUFSIZE];
                InputStream is = socket.getInputStream();
                while (true) {
                    int res = is.read(buffer);
                    System.out.println(res + " byte packet is received and ignored");
                }
            } catch (IOException e) {
                System.out.println("connection down");
                try {
                    socket.getOutputStream().close();
                } catch (IOException ignored) {
                    // ignore
                }
            }
        });
    }

    public void connect() {
        try {
            socket = new Socket();
            String switcherIp = getSwitcherIpAddress();
            System.out.println("switcher ip address : " + switcherIp);
            socket.connect(new InetSocketAddress(switcherIp, pRes.PORT_TCP), pRes.TCP_CONN_TIMEOUT);
            System.out.println("connection success");
            receiveThread.start();
        } catch (Exception e) {
            System.out.println("connection failure");
        }
    }

    private String getSwitcherIpAddress() {
        try {
            DatagramSocket dSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(Packet.SEARCH, Packet.SEARCH.length, InetAddress.getByName("255.255.255.255"), pRes.PORT_UDP);
            dSocket.setBroadcast(true);
            dSocket.send(sendPacket);

            byte[] buffer = new byte[pRes.BUFSIZE];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), pRes.PORT_UDP);
            dSocket.receive(receivePacket);

            return receivePacket.getAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "found failure";
    }

    public void command(int command) {
        switch (command) {
            case Command.MATRIX:
                try {
                    socket.getOutputStream().write(Packet.MATRIX);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_111:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_111);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }
}

public class RemoteHMCServer {
    public static void main(String[] args) {
        PacketLoader packetLoader = new PacketLoader();
        packetLoader.loadAllPacket();

        Server server = new Server();
        server.connect();

        server.command(Command.MULTI_VIEWER_111);
    }
}
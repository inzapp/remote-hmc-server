import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class pRes {
    static final int PORT_TCP = 8000;
    static final int PORT_UDP = 30600;
    static final int TCP_CONN_TIMEOUT = 5000;
    static final int BUFSIZE = 128;
}

class PacketFileName {
    private static final String PACKET_DIR = "packet";
    static final String SEARCH = PACKET_DIR + "\\search.txt";
    static final String MATRIX = PACKET_DIR + "\\matrix.txt";
    static final String MULTI_VIEWER_ENTER = PACKET_DIR + "\\enter_multi_viewer.txt";
    static final String MULTI_VIEWER_MODE_1 = PACKET_DIR + "\\multi_viewer_mode_1.txt";
    static final String MULTI_VIEWER_MODE_2 = PACKET_DIR + "\\multi_viewer_mode_2.txt";
    static final String MULTI_VIEWER_MODE_3 = PACKET_DIR + "\\multi_viewer_mode_3.txt";
    static final String MULTI_VIEWER_MODE_4 = PACKET_DIR + "\\multi_viewer_mode_4.txt";
    static final String MULTI_VIEWER_MAIN_1 = PACKET_DIR + "\\multi_viewer_main_1.txt";
    static final String MULTI_VIEWER_MAIN_2 = PACKET_DIR + "\\multi_viewer_main_2.txt";
    static final String MULTI_VIEWER_MAIN_3 = PACKET_DIR + "\\multi_viewer_main_3.txt";
    static final String MULTI_VIEWER_MAIN_4 = PACKET_DIR + "\\multi_viewer_main_4.txt";
}

class Packet {
    static byte[] SEARCH;
    static byte[] MATRIX;

    static byte[] MULTI_VIEWER_ENTER;
    static byte[] MULTI_VIEWER_MODE_1;
    static byte[] MULTI_VIEWER_MODE_2;
    static byte[] MULTI_VIEWER_MODE_3;
    static byte[] MULTI_VIEWER_MODE_4;
    static byte[] MULTI_VIEWER_MAIN_1;
    static byte[] MULTI_VIEWER_MAIN_2;
    static byte[] MULTI_VIEWER_MAIN_3;
    static byte[] MULTI_VIEWER_MAIN_4;
}

class Command {
    static final int MATRIX = 1;

    static final int MULTI_VIEWER_ENTER = 2;
    static final int MULTI_VIEWER_MODE_1 = 10;
    static final int MULTI_VIEWER_MODE_2 = 20;
    static final int MULTI_VIEWER_MODE_3 = 30;
    static final int MULTI_VIEWER_MODE_4 = 40;
    static final int MULTI_VIEWER_MAIN_1 = 100;
    static final int MULTI_VIEWER_MAIN_2 = 200;
    static final int MULTI_VIEWER_MAIN_3 = 300;
    static final int MULTI_VIEWER_MAIN_4 = 400;
}

class PacketLoader {
    void loadAllPacket() {
        System.out.println("start loading packet from file...");
        Packet.SEARCH = load(PacketFileName.SEARCH);
        Packet.MATRIX = load(PacketFileName.MATRIX);
        Packet.MULTI_VIEWER_ENTER = load(PacketFileName.MULTI_VIEWER_ENTER);
        Packet.MULTI_VIEWER_MODE_1 = load(PacketFileName.MULTI_VIEWER_MODE_1);
        Packet.MULTI_VIEWER_MODE_2 = load(PacketFileName.MULTI_VIEWER_MODE_2);
        Packet.MULTI_VIEWER_MODE_3 = load(PacketFileName.MULTI_VIEWER_MODE_3);
        Packet.MULTI_VIEWER_MODE_4 = load(PacketFileName.MULTI_VIEWER_MODE_4);
        Packet.MULTI_VIEWER_MAIN_1 = load(PacketFileName.MULTI_VIEWER_MAIN_1);
        Packet.MULTI_VIEWER_MAIN_2 = load(PacketFileName.MULTI_VIEWER_MAIN_2);
        Packet.MULTI_VIEWER_MAIN_3 = load(PacketFileName.MULTI_VIEWER_MAIN_3);
        Packet.MULTI_VIEWER_MAIN_4 = load(PacketFileName.MULTI_VIEWER_MAIN_4);
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

    Server() {
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

    void connect() {
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

    void command(int command) {
        switch (command) {
            case Command.MATRIX:
                try {
                    socket.getOutputStream().write(Packet.MATRIX);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_ENTER:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_ENTER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MODE_1:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MODE_1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MODE_2:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MODE_2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MODE_3:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MODE_3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MODE_4:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MODE_4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;


            case Command.MULTI_VIEWER_MAIN_1:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MAIN_1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MAIN_2:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MAIN_2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MAIN_3:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MAIN_3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.MULTI_VIEWER_MAIN_4:
                try {
                    socket.getOutputStream().write(Packet.MULTI_VIEWER_MAIN_4);
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
    public static void main(String[] args) throws InterruptedException {
        PacketLoader packetLoader = new PacketLoader();
        packetLoader.loadAllPacket();

        Server server = new Server();
        server.connect();

        int sleepTime = 1000;
        while (true) {
            server.command(Command.MATRIX);
            Thread.sleep(sleepTime);

            server.command(Command.MULTI_VIEWER_ENTER);
            server.command(Command.MULTI_VIEWER_MODE_1);
            server.command(Command.MULTI_VIEWER_MAIN_1);
            Thread.sleep(sleepTime);

            server.command(Command.MULTI_VIEWER_ENTER);
            server.command(Command.MULTI_VIEWER_MODE_2);
            Thread.sleep(sleepTime);

            server.command(Command.MULTI_VIEWER_ENTER);
            server.command(Command.MULTI_VIEWER_MODE_3);
            Thread.sleep(sleepTime);

            server.command(Command.MULTI_VIEWER_ENTER);
            server.command(Command.MULTI_VIEWER_MODE_4);
            Thread.sleep(sleepTime);
        }
    }
}
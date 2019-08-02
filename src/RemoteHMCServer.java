import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class pRes {
    static final int PORT_TCP = 8000;
    static final int PORT_UDP = 30600;
    static final int PORT_REMOTE_HTTP = 10232;
    static final int TCP_CONN_TIMEOUT = 5000;
    static final int SLEEP_WALL = 1500;
    static final int BUFSIZE = 128;
    static boolean IS_WALL_MODE = false;
}

class PacketFileName {
    private static final String PACKET_DIR = "packet\\";
    static final String SEARCH = PACKET_DIR + "search.txt";
    static final String MATRIX = PACKET_DIR + "matrix.txt";

    static final String MULTI_VIEWER_ENTER = PACKET_DIR + "enter_multi_viewer.txt";
    static final String MULTI_VIEWER_MODE_1 = PACKET_DIR + "multi_viewer_mode_1.txt";
    static final String MULTI_VIEWER_MODE_2 = PACKET_DIR + "multi_viewer_mode_2.txt";
    static final String MULTI_VIEWER_MODE_3 = PACKET_DIR + "multi_viewer_mode_3.txt";
    static final String MULTI_VIEWER_MODE_4 = PACKET_DIR + "multi_viewer_mode_4.txt";
    static final String MULTI_VIEWER_MAIN_1 = PACKET_DIR + "multi_viewer_main_1.txt";
    static final String MULTI_VIEWER_MAIN_2 = PACKET_DIR + "multi_viewer_main_2.txt";
    static final String MULTI_VIEWER_MAIN_3 = PACKET_DIR + "multi_viewer_main_3.txt";
    static final String MULTI_VIEWER_MAIN_4 = PACKET_DIR + "multi_viewer_main_4.txt";

    static final String VIDEO_WALL_ENTER = PACKET_DIR + "video_wall_enter.txt";
    static final String VIDEO_WALL_INPUT_1 = PACKET_DIR + "video_wall_input_1.txt";
    static final String VIDEO_WALL_INPUT_2 = PACKET_DIR + "video_wall_input_2.txt";
    static final String VIDEO_WALL_INPUT_3 = PACKET_DIR + "video_wall_input_3.txt";
    static final String VIDEO_WALL_INPUT_4 = PACKET_DIR + "video_wall_input_4.txt";
}

class Packet {
    static byte[] SEARCH;
    static byte[] MATRIX;

    static byte[] MULTI_VIEWER_ENTER;
    static byte[][] MULTI_VIEWER_MODE;
    static byte[][] MULTI_VIEWER_MAIN;

    static byte[] VIDEO_WALL_ENTER;
    static byte[][] VIDEO_WALL_INPUT;
}

class PacketLoader {
    PacketLoader() {
        Packet.MULTI_VIEWER_MODE = new byte[4][];
        Packet.MULTI_VIEWER_MAIN = new byte[4][];
        Packet.VIDEO_WALL_INPUT = new byte[4][];
    }

    void loadAllPacket() {
        System.out.println("start loading packet from file...");
        Packet.SEARCH = load(PacketFileName.SEARCH);
        Packet.MATRIX = load(PacketFileName.MATRIX);

        Packet.MULTI_VIEWER_ENTER = load(PacketFileName.MULTI_VIEWER_ENTER);
        Packet.MULTI_VIEWER_MODE[0] = load(PacketFileName.MULTI_VIEWER_MODE_1);
        Packet.MULTI_VIEWER_MODE[1] = load(PacketFileName.MULTI_VIEWER_MODE_2);
        Packet.MULTI_VIEWER_MODE[2] = load(PacketFileName.MULTI_VIEWER_MODE_3);
        Packet.MULTI_VIEWER_MODE[3] = load(PacketFileName.MULTI_VIEWER_MODE_4);
        Packet.MULTI_VIEWER_MAIN[0] = load(PacketFileName.MULTI_VIEWER_MAIN_1);
        Packet.MULTI_VIEWER_MAIN[1] = load(PacketFileName.MULTI_VIEWER_MAIN_2);
        Packet.MULTI_VIEWER_MAIN[2] = load(PacketFileName.MULTI_VIEWER_MAIN_3);
        Packet.MULTI_VIEWER_MAIN[3] = load(PacketFileName.MULTI_VIEWER_MAIN_4);

        Packet.VIDEO_WALL_ENTER = load(PacketFileName.VIDEO_WALL_ENTER);
        Packet.VIDEO_WALL_INPUT[0] = load(PacketFileName.VIDEO_WALL_INPUT_1);
        Packet.VIDEO_WALL_INPUT[1] = load(PacketFileName.VIDEO_WALL_INPUT_2);
        Packet.VIDEO_WALL_INPUT[2] = load(PacketFileName.VIDEO_WALL_INPUT_3);
        Packet.VIDEO_WALL_INPUT[3] = load(PacketFileName.VIDEO_WALL_INPUT_4);
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

        return byteArr;
    }
}

class HMCServer {
    private Socket socket;
    private Thread receiveThread;

    boolean connect() {
        try {
            refresh();
            socket = new Socket();
            String hmcIpAddress = getHMCIpAddress();
            System.out.println("\nnew connect");
            System.out.println("switcher ip address : " + hmcIpAddress);
            socket.connect(new InetSocketAddress(hmcIpAddress, pRes.PORT_TCP), pRes.TCP_CONN_TIMEOUT);
            System.out.println("connection success");
            receiveThread.start();
            return true;
        } catch (Exception e) {
            System.out.println("connection failure");
            e.printStackTrace();
            return false;
        }
    }

    private void refresh() {
        disconnect();
        interruptThread();
        receiveThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[pRes.BUFSIZE];
                InputStream is = socket.getInputStream();
                while (true) {
                    int res = is.read(buffer);
                    System.out.println(res + " byte packet is received and ignored");
                }
            } catch (Exception e) {
                System.out.println("connection down\n");
                disconnect();
            }
        });
    }

    void disconnect() {
        try {
            socket.getOutputStream().close();
        } catch (Exception e) {
            // empty
        }
    }

    private void interruptThread() {
        try {
            receiveThread.interrupt();
        } catch (Exception e) {
            // empty
        }
    }

    private String getHMCIpAddress() {
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

    void send(byte[] buffer) {
        try {
            socket.getOutputStream().write(buffer);
        } catch (IOException e) {
            // empty
        }
    }
}

public class RemoteHMCServer {
    public static void main(String[] args) {
        PacketLoader packetLoader = new PacketLoader();
        packetLoader.loadAllPacket();

        System.out.println("start hmc server connection test");
        HMCServer hmcServer = new HMCServer();
        if (!hmcServer.connect()) {
            System.out.println("hmc server connection failure");
            return;
        }

        hmcServer.disconnect();
        System.out.println("hmc server connection success");

        RemoteHMCServer remoteHMCServer = new RemoteHMCServer();
        remoteHMCServer.start(hmcServer);
    }

    private void start(HMCServer hmcServer) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(pRes.PORT_REMOTE_HTTP), 0);
            httpServer.createContext("/matrix", exchange -> {
                if (!hmcServer.connect()) {
                    response(exchange, "connection failure");
                    return;
                }

                hmcServer.send(Packet.MATRIX);
                hmcServer.disconnect();
                response(exchange, "command success");
                pRes.IS_WALL_MODE = false;
            });

            httpServer.createContext("/multi_viewer/", exchange -> {
                String param = exchange.getRequestURI().toString().split("/")[2];
                char[] iso = param.toCharArray();
                int viewMode = (iso[0] - '0') - 1;
                int mainWindow = (iso[1] - '0') - 1;

                if (!hmcServer.connect()) {
                    response(exchange, "connection failure");
                    return;
                }

                hmcServer.send(Packet.MULTI_VIEWER_ENTER);
                sleepIfWallModeIs(true);
                hmcServer.send(Packet.MULTI_VIEWER_MODE[viewMode]);
                hmcServer.send(Packet.MULTI_VIEWER_MAIN[mainWindow]);
                hmcServer.disconnect();
                response(exchange, "command success");
                pRes.IS_WALL_MODE = false;
            });

            httpServer.createContext("/wall/", exchange -> {
                String param = exchange.getRequestURI().toString().split("/")[2];
                char[] iso = param.toCharArray();
                int wallMain = (iso[0] - '0') - 1;

                if(!hmcServer.connect()) {
                    response(exchange, "connection failure");
                    return;
                }

                hmcServer.send(Packet.VIDEO_WALL_ENTER);
                sleepIfWallModeIs(false);
                hmcServer.send(Packet.VIDEO_WALL_INPUT[wallMain]);
                hmcServer.disconnect();
                response(exchange, "command success");
                pRes.IS_WALL_MODE = true;
            });

            httpServer.start();
            System.out.println("web server initialization success");
            System.out.println("start waiting for client\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void response(HttpExchange exchange, String msg) {
        try {
            exchange.sendResponseHeaders(200, msg.length());
            OutputStream os = exchange.getResponseBody();
            os.write(msg.getBytes());
            os.close();
        } catch (Exception e) {
            // empty
        }
    }

    private void sleepIfWallModeIs(boolean wallMode) {
        if(pRes.IS_WALL_MODE == wallMode) {
            try {
                Thread.sleep(pRes.SLEEP_WALL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
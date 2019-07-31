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
    static final int BUFSIZE = 128;
    static final String REMOTE_SESSION_KEY = "[REMOTE_HMC_SERVER_KEY_10200392812738945698304958]";
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

    static final String VIDEO_WALL_ENTER = PACKET_DIR + "\\video_wall_enter.txt";

    static final String VIDEO_WALL_INPUT_1 = PACKET_DIR + "\\video_wall_input_1.txt";
    static final String VIDEO_WALL_INPUT_2 = PACKET_DIR + "\\video_wall_input_2.txt";
    static final String VIDEO_WALL_INPUT_3 = PACKET_DIR + "\\video_wall_input_3.txt";
    static final String VIDEO_WALL_INPUT_4 = PACKET_DIR + "\\video_wall_input_4.txt";
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

    static byte[] VIDEO_WALL_ENTER;
    static byte[] VIDEO_WALL_INPUT_1;
    static byte[] VIDEO_WALL_INPUT_2;
    static byte[] VIDEO_WALL_INPUT_3;
    static byte[] VIDEO_WALL_INPUT_4;
}

class Command {
    static final int MATRIX = 1;
    static final int MULTI_VIEWER_ENTER = 2;

    static final int MULTI_VIEWER_MODE_1 = 21;
    static final int MULTI_VIEWER_MODE_2 = 22;
    static final int MULTI_VIEWER_MODE_3 = 23;
    static final int MULTI_VIEWER_MODE_4 = 24;

    static final int MULTI_VIEWER_MAIN_1 = 201;
    static final int MULTI_VIEWER_MAIN_2 = 202;
    static final int MULTI_VIEWER_MAIN_3 = 203;
    static final int MULTI_VIEWER_MAIN_4 = 204;

    static final int VIDEO_WALL_ENTER = 3;
    static final int VIDEO_WALL_INPUT_1 = 31;
    static final int VIDEO_WALL_INPUT_2 = 32;
    static final int VIDEO_WALL_INPUT_3 = 33;
    static final int VIDEO_WALL_INPUT_4 = 34;
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

        Packet.VIDEO_WALL_ENTER = load(PacketFileName.VIDEO_WALL_ENTER);
        Packet.VIDEO_WALL_INPUT_1 = load(PacketFileName.VIDEO_WALL_INPUT_1);
        Packet.VIDEO_WALL_INPUT_2 = load(PacketFileName.VIDEO_WALL_INPUT_2);
        Packet.VIDEO_WALL_INPUT_3 = load(PacketFileName.VIDEO_WALL_INPUT_3);
        Packet.VIDEO_WALL_INPUT_4 = load(PacketFileName.VIDEO_WALL_INPUT_4);
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

class HMCServer {
    private Socket socket;
    private Thread receiveThread;

    boolean connect() {
        try {
            refresh();
            socket = new Socket();
            String switcherIp = getSwitcherIpAddress();
            System.out.println("switcher ip address : " + switcherIp);
            socket.connect(new InetSocketAddress(switcherIp, pRes.PORT_TCP), pRes.TCP_CONN_TIMEOUT);
            System.out.println("connection success\n");
            receiveThread.start();
            return true;
        } catch (Exception e) {
            System.out.println("connection failure\n");
            e.printStackTrace();
            return false;
        }
    }

    private void refresh() {
        disconnect();

        try {
            receiveThread.interrupt();
        } catch(Exception e) {
            // empty
        }

        receiveThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[pRes.BUFSIZE];
                InputStream is = socket.getInputStream();
                while (true) {
                    int res = is.read(buffer);
                    System.out.println(res + " byte packet is received and ignored");
                }
            } catch (Exception e) {
                System.out.println("connection down");
                try {
                    socket.getOutputStream().close();
                } catch (IOException ignored) {
                    // ignore
                }
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

            case Command.VIDEO_WALL_ENTER:
                try {
                    socket.getOutputStream().write(Packet.VIDEO_WALL_ENTER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.VIDEO_WALL_INPUT_1:
                try {
                    socket.getOutputStream().write(Packet.VIDEO_WALL_INPUT_1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.VIDEO_WALL_INPUT_2:
                try {
                    socket.getOutputStream().write(Packet.VIDEO_WALL_INPUT_2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.VIDEO_WALL_INPUT_3:
                try {
                    socket.getOutputStream().write(Packet.VIDEO_WALL_INPUT_3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Command.VIDEO_WALL_INPUT_4:
                try {
                    socket.getOutputStream().write(Packet.VIDEO_WALL_INPUT_4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        try {
            Thread.sleep(30);
        } catch (InterruptedException ignored) {
        }
    }
}

public class RemoteHMCServer {
    public static void main(String[] args) {
        PacketLoader packetLoader = new PacketLoader();
        packetLoader.loadAllPacket();

        HMCServer hmcServer = new HMCServer();
        hmcServer.connect();

        RemoteHMCServer remoteHMCServer = new RemoteHMCServer();
        remoteHMCServer.start(hmcServer);
    }

    void start(HMCServer hmcServer) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(pRes.PORT_REMOTE_HTTP), 0);
            httpServer.createContext("/matrix", exchange -> {
                if (!hmcServer.connect()) {
                    response(exchange, "connection failure");
                    return;
                }

                hmcServer.command(Command.MATRIX);
                hmcServer.disconnect();;
                response(exchange, "true");
            });

            httpServer.createContext("/multi111", exchange -> {

            });
            httpServer.start();
            System.out.println("web server initialization success\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void response(HttpExchange exchange, String msg) {
        try {
            String response = msg;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception ignored) {
            // empty
        }
    }
}
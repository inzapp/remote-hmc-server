import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class RemoteHMCServer {
    public static void main(String[] args) {
        try {
            DatagramSocket dSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
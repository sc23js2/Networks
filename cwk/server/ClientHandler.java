import java.net.Socket;
import java.net.*;
import java.io.*;
import java.util.*;

public class ClientHandler extends Thread {
    
    private Socket socket = null;

    public ClientHandler(Socket socket) {
		super("ClientHandler");
		this.socket = socket;
    }

}

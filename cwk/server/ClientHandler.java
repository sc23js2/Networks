import java.io.*;
import java.net.*;

//the ClientHandler class is an interface between the server and the client
//it handles the client input, and uses the server protocol to process the input and send the server response back to the client

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ServerProtocol serverProtocol;

    //sets socket and server protocol
    public ClientHandler(Socket socket, ServerProtocol serverProtocol) {
        this.clientSocket = socket;
        this.serverProtocol = serverProtocol;
    }

    @Override
    public void run() {
        try {
            
            // Create writer and reader for the client socket
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine, outputLine;

            //read input from the client
            inputLine = in.readLine();

            //procces the client input using the server protocol
            outputLine = serverProtocol.processInput(inputLine);
                
            //send servers response to the client
            out.println(outputLine);

            //close all connections
            out.close();
            in.close();
            clientSocket.close();
        } 
        catch (IOException e) 
        {
            System.err.println("Error handling the client.");
        }
    }
}
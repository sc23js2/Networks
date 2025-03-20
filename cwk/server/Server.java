import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;
import java.io.*;

public class Server {

    private ServerSocket serverSocket = null;
    private ServerProtocol sp = null;
    private ExecutorService threadPool = null;

    public Server(String[] voteOptions) {

        //create server socket
        try {
            serverSocket = new ServerSocket(7777);
        }
        catch (IOException e) {
            System.err.println("Could not listen on port: 7777.");
            System.exit(1);
        }
        System.out.println("Socket initalised.");

        //create executor threadPool with 30 threads
        try {
            threadPool = Executors.newFixedThreadPool(30);
        }
        catch (Exception e) {
            System.err.println("Could not create executor threadPool.");
            System.exit(1);
        }

        //set server protocol, create poll and log file
        setServerProtocol(voteOptions);
    }

    public int setServerProtocol(String[] voteOptions) {
        
        int success = 0;
        sp = new ServerProtocol();

        //create poll and fill options
        try {
            System.out.println("Creating poll...");
            success = sp.CreatePoll(voteOptions);
        }
        catch(Exception e) {
            System.err.println("Unable to create poll.");
            System.err.println(e);
            System.exit(1);
        }

        //create log file
        try {
            System.out.println("Creating log file...");
            sp.makeFile();
        }
        catch(Exception e) {
            System.err.println("Error creating log file.");
            System.err.println(e);
            System.exit(1);
        }

        return success;
    }

    public Socket acceptClient() {
        Socket clientSocket = null;

        //accept client on thread
        try {
            clientSocket = serverSocket.accept();
            threadPool.submit( new ClientHandler(clientSocket, sp) );
        }
        catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        return clientSocket;
    }

    public void runServer() {

        Socket clientSocket = null;

        while( true ) {

            //call subroutine accept client
            clientSocket = acceptClient();

            //print address and port on server terminal
            System.out.println("clientSocket address: " + clientSocket.getInetAddress() );
            System.out.println("clientSocket port: " + clientSocket.getPort() );

        }
    }

    public static void main( String[] args ) {

        //check args length
        if (args.length < 2) {
            System.err.println("To open a vote you must provide at least 2 poll options.\n Usage: java Server <option1> <option2> ... <optionN>");
            System.exit(1);
        }
        
        //create server
        Server votingServer = new Server(args);

        //run server
        votingServer.runServer();
    }
}


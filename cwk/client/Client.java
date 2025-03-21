import java.io.*;
import java.net.*;

public class Client {

    private Socket mySocket = null;
    private PrintWriter socketOutput = null;
    private BufferedReader socketInput = null;

    public Client(Socket socket) {
        //set socket
		this.mySocket = socket;

        try {
            //creating the socket to connect to server (must first be running in another shell.)
            mySocket = new Socket( "localhost", 7777 );

            //creating writing (output) stream
            socketOutput = new PrintWriter(mySocket.getOutputStream(), true);

            //creating reading (input) stream
            socketInput = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

        }
        catch (UnknownHostException e) {
            System.err.println("Unknown Host.\n");
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to host.\n");
            System.exit(1);
        }

    }

    public void vote(String clientString) {

        // Chain a reader from the keyboard.
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in) );
        
        //initialize strings
        String fromServer;
        String fromUser;

        try
        {
            // Get client input from args
            fromUser = clientString;
            
            //process user input
    	    if( fromUser != null )
    	    {
                // Echo client string.
                System.out.println( "Client: " + fromUser );

                // Write string to client handler. handler then proccesses the input
                socketOutput.println(fromUser);
            }
            
            // Read the output from server after processing the input.
            fromServer = socketInput.readLine();

            // Output servers response
            System.out.println( "Server: " + fromServer);          

            //close all connections
            socketOutput.close();
            socketInput.close();
            stdIn.close();
            mySocket.close();
        }
        catch (IOException e) {
            System.err.println("I/O exception during execution\n");
            System.exit(1);
        }
    }

    //turns string array into space separated string
    public static String stringify(String[] userInput)
    {
        StringBuilder inputString = new StringBuilder();

        for (int i=0; i < userInput.length; i++)
        {
            inputString.append(userInput[i]).append(" ");
        }

        return inputString.toString();
    }

    public static void main(String[] args) {

        //check that parameters exist, so that a null exception is not thrown
        if (args.length < 1)
        {
            System.err.println("Error: Not enough arguments. \n Usage: java Client <list> or <vote> <option>");
            System.exit(1);
        }
        
        //turn input from array into one big string
        String userInput = stringify(args);

        //make client connection
        Client myClient = new Client(new Socket());

        //vote
        myClient.vote(userInput);
    }

}


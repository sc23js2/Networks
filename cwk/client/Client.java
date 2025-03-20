/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;

public class Client extends Thread {

    private Socket mySocket = null;
    private PrintWriter socketOutput = null;
    private BufferedReader socketInput = null;

    public Client(Socket socket) {
		super("Client");
		this.mySocket = socket;

        try {
            // Try and create the socket. The server is assumed to be running on the same host ('localhost'),
            // so first run 'Server' in another shell.
            mySocket = new Socket( "localhost", 7777 );

            // Chain a writing stream
            socketOutput = new PrintWriter(mySocket.getOutputStream(), true);

            // Chain a reading stream
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

        System.out.println("ecouweoidwqovgewpih.");
    }

    public void vote(String clientString) {

        // Chain a reader from the keyboard.
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in) );
        String fromServer;
        String fromUser;

        // Read from server.
        try
        {
            // Get client input from args
            fromUser = clientString;
            
            //process user input
    	    if( fromUser!=null )
    	    {
                // Echo client string.
                System.out.println( "Client: " + fromUser );

                // Write to server.
                socketOutput.println(fromUser);
            }
            
            // Read from server.
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
        String inputString = "";

        for (int i=0; i < userInput.length; i++)
        {
            inputString = inputString + userInput[i] + " ";
        }

        return inputString;
    }

    public static void main(String[] args) {
        //check that parameters exist, so that a null exception is not thrown
        if (args.length < 1)
        {
            System.err.println("Error: Not enough arguments. \n Usage: java Client <list> or <vote> <option>");
            System.exit(1);
        }
        
        //turn input into one big string
        String userInput = stringify(args);

        //make client
        Client myClient = new Client(new Socket());

        //vote
        myClient.vote(userInput);
    }

}


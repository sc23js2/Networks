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

import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;
import java.io.*;

public class Server {

    private ServerSocket serverSocket = null;
    private ServerProtocol sp = null;
    private ExecutorService service = null;

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

        //crete executor service with 30 threads
        try {
            service = Executors.newFixedThreadPool(30);
        }
        catch (Exception e) {
            System.err.println("Could not create executor service.");
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

        try {
            clientSocket = serverSocket.accept();
            service.submit( new ClientHandler(clientSocket) );
        }
        catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        return clientSocket;
    }

    public void runServer() {

        Socket clientSocket = null;

        while( true ){

            clientSocket = acceptClient();
            System.out.println("clientSocket address: " + clientSocket.getInetAddress() );
            System.out.println("clientSocket port: " + clientSocket.getPort() );

            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine, outputLine;

                //read input
                inputLine = in.readLine();
                
                //process input to get output
                outputLine = sp.processInput(inputLine);
                out.println(outputLine);
                     
                out.close();
                in.close();
                clientSocket.close();
            }
            catch (IOException e) {
                System.out.println( e );
            }
        }
    }

    public static void main( String[] args ) {

        if (args.length < 2) {
            System.err.println("To open a vote you must provide at least 2 poll options.\n Usage: java Server <option1> <option2> ... <optionN>");
            System.exit(1);
        }
        
        Server votingServer = new Server(args);

        votingServer.runServer();
    }
}


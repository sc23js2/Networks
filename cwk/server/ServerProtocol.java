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
import java.sql.Time;

import javax.swing.plaf.synth.SynthStyle;

import java.io.*;
import java.util.Date;
import java.time.*;;

public class ServerProtocol {

    private static final int WELCOME = 0;
    private static final int PENDING = 1;
    private static final int VOTED = 2;
    private static final int LIST = 3;

    private int state = PENDING;

    private String[] pollOptions;
    private int[] pollResults;

    private static File logFile;
    private static FileWriter myWriter;

    private static Date date;
    private static Time time;

    private static InetAddress ip;
    private static String ipAddress;

    public void makeFile() {
        try {
            logFile = new File("log.txt");

            if (logFile.exists())
            {
                System.out.println("File already exists. Deleleting and creating new one.");
                logFile.delete();
            }
        
            if (logFile.createNewFile()) {
                System.out.println("File created: " + logFile.getName());
            } 
            else
            {
                throw new IOException();
            }
        } 
        catch (IOException e) 
        {
            System.err.println("An Error occured while creating the log file.");
            System.exit(1);
        }
    }

    private void logAction(String request) {
        try {
            //Create file Writer
            myWriter = new FileWriter(logFile, true);

            //create date/time
            date = new Date();
            time = new Time(date.getTime());

            //create IP address getter
            ip = InetAddress.getLocalHost();
            ipAddress = ip.getHostAddress();

            //date|time|client IP address|request
            String logString = date + "|" + time + "|" + ipAddress + "|" + request + "\n";

            myWriter.write(logString);
            myWriter.close();
            System.out.println("Request logged in file.");
        } 
        catch (IOException e) 
        {
            System.err.println("An error occured while logging the request.");
        }
    }

    public int CreatePoll(String[] voteOptions) {

        //add options to the polls
        pollOptions = voteOptions;
        
        //all votes should initially be 0
        pollResults = new int[voteOptions.length];

        for(int i = 0; i < pollResults.length; i++) {
            pollResults[i] = 0; 
        }

        System.out.println("Poll created with " + voteOptions.length + " options.");
        state = WELCOME;

        return 1; //success
    }

    public String listOptions() {

        String list = "The Poll: ";

        for (int i = 0; i < pollOptions.length; i++) {
            list += (" '" + pollOptions[i] + "' has " + pollResults[i] + " votes, ");
        }

        list += "\n";

        System.out.println("list created");
        return list;
    }

    public int validateVote(String voteOption) {

        for (int i = 0; i < pollOptions.length; i++) {
            if (pollOptions[i].equalsIgnoreCase(voteOption)) {
                return 1; //valid
            }
        }

        return 0; //invalid
    }

    public String vote(String voteOption) {

        System.out.println("voting...");

        for (int i = 0; i < pollOptions.length; i++) {
            if (pollOptions[i].equalsIgnoreCase(voteOption)) {

                //increase poll count in corresponding position
                pollResults[i]++;
                return "You cast your vote for: " + voteOption;
            }
        }
        
        return "Error: Vote " + voteOption + " not found.";
    }

    public String getWord(String theInput, int wordNumber)
    {   
        //if we are looking for word number 1, then we need to count 0 spaces before parsing.
        //if we are looking for word number 2 then we need to count 1 space before parsing.
        int numOfSpaces = 0;
        String word = "";

        for (int i=0; i< theInput.length(); i++)
        {
            if (theInput.charAt(i) == ' ' && numOfSpaces == wordNumber - 1)
            { 
                //we have found the whole word
                break;
            }
            else if (theInput.charAt(i) == ' ')
            {
                //increase number of spaces counted
                numOfSpaces +=1;
            }
            else if (theInput.charAt(i) != ' ' && numOfSpaces == wordNumber - 1)
            {
                //we have found the correct num of spaces, we are now in the middle of parsing the word
                word = word += theInput.charAt(i);
            }
        }

        return word;
    }

    public String processInput(String theInput) {
        
        String theOutput = null;

        if (theInput == null)
        {
            state = -1;
        }
        else //get state list or vote.
        {

            if (getWord(theInput, 1).equalsIgnoreCase("list")) 
            {
                state = LIST;
            }
            else if (getWord(theInput, 1).equalsIgnoreCase("vote") && state != VOTED) //you cant vote twice
            {
                state = PENDING;
            }
            else {

                if (state != VOTED) //dont say invalif input if they have already voted
                    state = -1; //switch will default
            }
        }
    
        switch (state) {

            case LIST:
                theOutput = listOptions();
                state = PENDING;
                logAction("list");
                break;
        
            case PENDING:
                theOutput = vote(getWord(theInput, 2));

                //only log and change state if valid
                if (validateVote(getWord(theInput, 2)) == 1)
                {
                    state = VOTED;
                    logAction("vote");
                }                        
               
                break;

            case VOTED:
                theOutput = "You have already voted.";
                break;

            default:
                theOutput = "Invalid Input";
                break;
        }

        System.out.println("Returning Output " + theOutput);

        return theOutput;
    } 
}

import java.net.*;
import java.sql.Time;

import javax.swing.plaf.synth.SynthStyle;

import java.io.*;
import java.util.Date;
import java.time.*;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class ServerProtocol {

    private static final int PENDING = 1; //(waiting for vote)
    private static final int LIST = 2;

    private int state = PENDING;

    private String[] pollOptions;
    private int[] pollResults;

    private static File logFile;
    private static FileWriter myWriter;

    private static Date date;
    private static Time time;

    private static InetAddress ip;
    private static String ipAddress;

    private final Lock lock = new ReentrantLock();

    public void makeFile() {

        try {
            //create log file
            logFile = new File("log.txt");

            //if the file already exists then delete it
            if (logFile.exists())
            {
                //System.out.println("File already exists. Deleleting and creating new one.");
                logFile.delete();
            }

            //create new log file
            if (logFile.createNewFile()) {
                System.out.println("File created.");
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

    private synchronized void logAction(String request) {

        try {
            //Create file Writer
            myWriter = new FileWriter(logFile, true);

            //create date/time
            date = new Date();
            time = new Time(date.getTime());

            //create IP address getter
            ip = InetAddress.getLocalHost();
            ipAddress = ip.getHostAddress();

            //create string date|time|client IP address|request
            String logString = date + "|" + time + "|" + ipAddress + "|" + request + "\n";

            //write string to logfile and close file
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
        //set all votes to 0
        for(int i = 0; i < pollResults.length; i++) {
            pollResults[i] = 0; 
        }

        System.out.println("Poll created with " + voteOptions.length + " options.");
        state = PENDING;

        return 1; //success
    }

    public synchronized String listOptions() {

        String list = "The Poll: ";

        //lock the poll results for synchronisation
        lock.lock();

        for (int i = 0; i < pollOptions.length; i++) {
            list += (" '" + pollOptions[i] + "' has " + pollResults[i] + " votes, ");
        }

        //unlock the poll results
        lock.unlock();

        list += "\n";
        System.out.println("list created");
        return list;
    }

    public int validateVote(String voteOption) {

        //check if the vote is in the poll options

        for (int i = 0; i < pollOptions.length; i++) {
            if (pollOptions[i].equalsIgnoreCase(voteOption)) {
                return 1; //valid
            }
        }

        return 0; //invalid
    }

    public synchronized String vote(String voteOption) {

        System.out.println("voting...");

        for (int i = 0; i < pollOptions.length; i++) {
            if (pollOptions[i].equalsIgnoreCase(voteOption)) {

                //lock the poll results
                lock.lock();

                //increase poll count in corresponding position
                pollResults[i]++;

                //unlock the poll results
                lock.unlock();

                return "You cast your vote for: " + voteOption;
            }
        }
        
        return "Error: Vote " + voteOption + " not found.";
    }

    //gets the word at the specified position in a string
    public String getWord(String theInput, int wordNumber) {   
        //EXAMPLE USAGE --
        //getWord("vote chicken", 1) will return "vote"
        //getWord("vote chicken", 2) will return "chicken"

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

        //if input is null then there is nothing to process or log. set state to -1 to give error message before exit
        if (theInput == null)
        {
            state = -1;
        }
        else //get state list or vote.
        {
            if (getWord(theInput, 1).equalsIgnoreCase("list")) //if first word is list
            {
                state = LIST;
            }
            else if (getWord(theInput, 1).equalsIgnoreCase("vote") ) //if first word is vote
            {
                state = PENDING;
            }
            else {

                state = -1; //switch will default to error message
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

                //only log if valid vote
                if (validateVote(getWord(theInput, 2)) == 1) //1 is valid 
                {
                    logAction("vote");
                }                        
                break;

            default:
                theOutput = "Invalid Input";
                break;
        }

        System.out.println("Returning Output " + theOutput);

        return theOutput;
    } 
}

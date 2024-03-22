package com;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class ClientHandler implements Runnable {
    
    static final char CLOSE = '1';
    static final char MESSAGE = '0';

    private static Integer ClientCount = 0;
    private static Map<Integer, ClientHandler> allClientHandlers= new HashMap<>();
    
    private final Socket connection;
    public final PrintWriter outgoing;
    private final BufferedReader incoming;
    private final Integer clientID;

    public ClientHandler(Socket connection, PrintWriter outgoing, BufferedReader incoming) {
        
        this.connection = connection;
        this.outgoing = outgoing;
        this.incoming = incoming;

        ClientCount++;
        this.clientID = ClientCount;
        allClientHandlers.put(clientID, this);
    }

    @Override
    public void run() {

        String messageIn;         // A message received from the client.


        try (Scanner userInput = new Scanner(System.in)) {
            
            System.out.println("Client: " + this.clientID +" Connected\n");
            
            while (true) {

                // System.out.println("WAITING...");
                messageIn = incoming.readLine();
                if (messageIn.length() > 0) {
                        // The first character of the message is a command. If 
                        // the command is CLOSE, then the connection is closed.  
                        // Otherwise, remove the command character from the 
                        // message and proceed.
                    if (messageIn.charAt(0) == CLOSE) {
                        System.out.println("Connection closed at other end.");
                        connection.close();
                        break;
                    }
                    messageIn = messageIn.substring(1);
                }
                // System.out.println("RECEIVED:  " + messageIn);
                
                for (Map.Entry<Integer, ClientHandler> entry : allClientHandlers.entrySet()) {

                    if (entry.getKey() != this.clientID) {

                        entry.getValue().outgoing.println(MESSAGE + messageIn);
                        entry.getValue().outgoing.flush(); // Make sure the data is sent!
                        
                    }
                }
                
                if (outgoing.checkError()) {
                    throw new IOException("Error occurred while transmitting message.");
                }
            }
        }
        catch (Exception e) {
            System.out.println("Sorry, an error has occurred.  Connection lost.");
            System.out.println("Error:  " + e);
        }

    }
}

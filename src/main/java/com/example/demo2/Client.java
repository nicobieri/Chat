package com.example.demo2;

import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        String username = "Rainer";
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("Mit dem Server verbunden!");

            // Streams für Ein- und Ausgabe
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Username an den Server senden
            out.println(username);

            // Thread für den Empfang von Nachrichten vom Server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            System.out.println(serverMessage);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Nachrichten senden
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                System.out.println("Me: " + userMessage);

                // Wenn die Nachricht mit @ beginnt, ist es eine private Nachricht
                if (userMessage.startsWith("@")) {
                    out.println(userMessage);
                } else {
                    out.println(userMessage);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

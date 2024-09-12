package com.example.demo2;

import java.io.*;
import java.net.*;

import java.util.*;

public class Server {

    // Liste für die verbundenen Clients
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server gestartet...");

            while (true) {
                // Warten auf neue Client-Verbindung
                Socket socket = serverSocket.accept();
                System.out.println("Neuer Client verbunden ");

                // Erstellen eines neuen Client-Handlers
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);

                // Starten des Handlers in einem eigenen Thread
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methode, um Nachrichten an alle Clients zu senden
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // Entfernen eines Clients
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }
}

class ClientHandler implements Runnable {
    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Eingabe- und Ausgabestreams erstellen
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Begrüßungsnachricht
            this.username = in.readLine();
            out.println("Willkommen im Chat " + username);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
                // Nachricht an alle anderen Clients senden
                Server.broadcastMessage(message, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Remove client when they disconnect
            Server.removeClient(this);
            System.out.println(username + " hat die Verbindung getrennt.");
        }
    }

    // Nachricht an den Client senden
    public void sendMessage(String message) {
        out.println(message);
    }
}

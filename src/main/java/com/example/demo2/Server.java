package com.example.demo2;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    // Liste für die verbundenen Clients (mit dem Benutzernamen als Schlüssel)
    private static Map<String, ClientHandler> clientHandlers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server gestartet...");

            while (true) {
                // Warten auf neue Client-Verbindung
                Socket socket = serverSocket.accept();
                System.out.println("Neuer Client verbunden ");

                // Erstellen eines neuen Client-Handlers
                ClientHandler clientHandler = new ClientHandler(socket);

                // Starten des Handlers in einem eigenen Thread
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methode, um eine Nachricht an einen bestimmten Client zu senden
    public static void sendMessageToSpecificClient(String targetUsername, String message, ClientHandler sender) {
        ClientHandler targetClient = clientHandlers.get(targetUsername);
        if (targetClient != null && targetClient != sender) {
            targetClient.sendMessage(message);
        } else {
            sender.sendMessage("Benutzer " + targetUsername + " nicht gefunden oder nicht online.");
        }
    }

    //broadcast
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // Methode, um einen Client der Liste hinzuzufügen
    public static void addClient(String username, ClientHandler clientHandler) {
        clientHandlers.put(username, clientHandler);
    }

    // Methode, um einen Client zu entfernen
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler.getUsername());
    }

    // Liste aller verbundenen Clients zurückgeben
    public static String getAllUsernames() {
        return String.join(", ", clientHandlers.keySet());
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

            // Begrüßungsnachricht und Benutzername speichern
            this.username = in.readLine();
            Server.addClient(username, this);
            out.println("Willkommen im Chat " + username);
            System.out.println(username + " hat den Chat betreten.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);

                // Nachricht kann im Format "@username: Nachricht" gesendet werden
                if (message.startsWith("@")) {
                    int colonIndex = message.indexOf(" ");
                    if (colonIndex != -1) {
                        String targetUsername = message.substring(1, colonIndex);
                        String privateMessage = message.substring(colonIndex + 1).trim();
                        System.out.println(targetUsername + " " + privateMessage);
                        Server.sendMessageToSpecificClient(targetUsername, username + " (privat): " + privateMessage, this);
                    }
                } else {
                    // Nachricht an alle anderen Clients senden
                    Server.broadcastMessage(username + ": " + message, this);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Client entfernen, wenn er sich trennt
            Server.removeClient(this);
            System.out.println(username + " hat die Verbindung getrennt.");
        }
    }

    // Nachricht an den Client senden
    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }
}

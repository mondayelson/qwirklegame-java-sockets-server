package com.elson.server;

import com.elson.client.Player;
import com.elson.controller.GroupController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {

    public static void main(String[] args) {

        Server server = new Server();
        server.start();
    }

    public void start() {

        GroupController groupController=new GroupController();

        // Create new socket
        ServerSocket socket;
        try {
            socket = new ServerSocket(5050);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Display IP and Port
        try {
            System.out.printf(">> Server started on ip= %s port= 5050\n"
                    , InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // Accept clients connections
        while (true) {

            Socket clientSocket = null;

            try {
                clientSocket = socket.accept();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            groupController.addPlayer(new Player(clientSocket));

        }


    }


}

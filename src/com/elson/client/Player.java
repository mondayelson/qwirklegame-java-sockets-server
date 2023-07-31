package com.elson.client;


import com.elson.controller.GameController;
import com.elson.messages.Package;
import com.elson.qwirkle.Tile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Player {
    public BlockingQueue<Package> outMessages = new LinkedBlockingQueue<>();
    public int playerNo, groupNo, score;
    public String name;
    public List<Tile> hand;

    private ReadThread readThread;
    private WriteThread writeThread;
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private GameController gameController;

    public Player(Socket clientSocket) {
        this.name = null;
        this.score = 0;
        this.clientSocket = clientSocket;
        readThread = new ReadThread();
        readThread.start();
    }

    public void setPlayerNo(int playerNo) {
        this.playerNo = playerNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }

    public void setController(GameController gameController) {
        this.gameController = gameController;
    }

    public void interruptAllThreads() {
        readThread.interrupt();
        writeThread.interrupt();

        System.out.println(">> Group " + groupNo + ", player " + playerNo + " disconnected.");
    }

    public void send(Package p) {
        try {
            outMessages.put(p);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {

            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(clientSocket.getInputStream());

                writeThread = new WriteThread();
                writeThread.start();

                Package p;
                do {

                    p = (Package) in.readObject();



                   gameController.receive(Player.this, p);
                } while (true);


            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private class WriteThread extends Thread {

        @Override
        public void run() {
            while (true) {

                Package p = null;
                try {
                    p = outMessages.take();
                    out.writeObject(p);
                    out.flush();

                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

}

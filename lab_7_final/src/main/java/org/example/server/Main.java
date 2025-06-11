package org.example.server;

import org.example.data.*;
import org.example.server.manager.CollectionManager;
import org.example.server.manager.DataBaseManager;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        DataBaseManager dataBaseManager = new DataBaseManager();


        CollectionManager collectionManager = new CollectionManager();

        Server server = new Server(collectionManager);

        server.startServer();


    }
}
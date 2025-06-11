package org.example.server;


import org.example.data.Worker;
import org.example.data.network.Request;
import org.example.data.network.Response;
import org.example.server.manager.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {



    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);
    private final WorkerCreator workerCreator = new WorkerCreator();
    private final CollectionManager collectionManager;
    private final CommandManager commandManager;
    private final DataBaseManager dataBaseManager = new DataBaseManager();
    private final HashMap<SocketChannel, String> registeredUsers = new HashMap<>();

    public Server(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.commandManager = new CommandManager(collectionManager, workerCreator, dataBaseManager);
    }



    public void startServer() throws IOException, ClassNotFoundException {



        dataBaseManager.checkTables();

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.bind(new InetSocketAddress(InetAddress.getByName("localhost"), 24555));
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);



        System.out.println("TCP-сервер запущен на порту 24555");



        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                // Обработка событий
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                keyIterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);

        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Новое подключение от " + clientChannel.getRemoteAddress());

    }

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            clientChannel.close();
            key.cancel();
            return;
        }

        buffer.flip();

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Request request;
        try {
            request = (Request) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // многопоточная обработка
        threadPool.submit(() -> {
            try {
                String result = commandManager.doCommandAsync(request).get();
                Response response = new Response(result);

                sendAnswer(clientChannel, response);

                clientChannel.register(selector, SelectionKey.OP_READ);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


    public void sendAnswer(SocketChannel socket, Response r) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(r);
            objectOutputStream.flush();

            byte[] data = byteArrayOutputStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(data);

            int chunkSize = 8192;

            while (buffer.hasRemaining()) {
                int length = Math.min(chunkSize, buffer.remaining());
                ByteBuffer chunkBuffer = ByteBuffer.wrap(data, buffer.position(), length);

                int bytesWritten = socket.write(chunkBuffer);
                buffer.position(buffer.position() + bytesWritten);
            }
        }
    }
}

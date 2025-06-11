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

public class Server {



    private final WorkerCreator workerCreator = new WorkerCreator();
    private final CollectionManager collectionManager;
    private final CommandManager commandManager;
    private final DataBaseManager dataBaseManager;
    private final HashMap<SocketChannel, String> registeredUsers = new HashMap<>();

    public Server(CollectionManager collectionManager, DataBaseManager dataBaseManager) {
        this.collectionManager = collectionManager;
        this.commandManager = new CommandManager(collectionManager, workerCreator, dataBaseManager);
        this.dataBaseManager = dataBaseManager;
    }



    public void startServer() throws IOException, ClassNotFoundException {




        // Создаем серверный канал
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Привязываем канал к порту
        serverChannel.bind(new InetSocketAddress(InetAddress.getByName("localhost"), 24555));

        // Создаем селектор
        Selector selector = Selector.open();
        // Регистрируем серверный канал для приема подключений
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);



        System.out.println("TCP-сервер запущен на порту 24555");



        // Основной цикл обработки событий
        while (true) {
            selector.select(); // Количество ключей, чьи каналы готовы к операции. БЛОКИРУЕТ, ПОКА НЕ БУДЕТ КЛЮЧЕЙ
            Set<SelectionKey> selectedKeys = selector.selectedKeys(); // получаем список ключей от каналов, готовых к работе
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator(); // получаем итератор ключей
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                // Обработка событий
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    Thread thread = new Thread(() -> {
                        try {
                            handleRead(key, selector);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    thread.start();
                }
                keyIterator.remove();
            }
        }
    }

    // Обработка события ACCEPT (новое подключение)
    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);

        // Регистрируем клиентский канал для чтения
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Новое подключение от " + clientChannel.getRemoteAddress());

    }

    // Обработка события READ (получение данных)
    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // Получаем или создаём накопительный буфер для этого канала
        ByteArrayOutputStream baos = (ByteArrayOutputStream) key.attachment();
        if (baos == null) {
            baos = new ByteArrayOutputStream();
            key.attach(baos);
        }


        ByteBuffer buffer = ByteBuffer.allocate(8192); // Размер буфера

        final ByteArrayOutputStream baos2 = baos;

        Thread thread = new Thread(() -> {
            try {
                process(clientChannel, buffer, key, selector, baos2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    public void process(SocketChannel clientChannel, ByteBuffer buffer, SelectionKey key, Selector selector, ByteArrayOutputStream baos) throws IOException {
        try {
            // Читаем данные из канала
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                // Клиент закрыл соединение
                System.err.println("Клиент " + clientChannel.getRemoteAddress() + " отключился");
                collectionManager.clearCollection();
                clientChannel.close();
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                buffer.flip(); // Переключаем буфер в режим чтения
                baos.write(buffer.array(), 0, buffer.limit()); // Добавляем данные
                buffer.clear(); // Очищаем буфер

                // Проверяем, можно ли десериализовать данные
                byte[] data = baos.toByteArray();
                final ByteArrayOutputStream baos2 = (ByteArrayOutputStream) key.attachment();
                if (data.length > 0) {
                    try (ByteArrayInputStream bi = new ByteArrayInputStream(data);
                                                            ObjectInputStream oi = new ObjectInputStream(bi)) {
                        Request request = (Request) oi.readObject();
                        Response response;
                        // Сбрасываем baos после успешной обработки
                        baos2.reset();
                        key.attach(null); // Очищаем attachment

                        if (!registeredUsers.containsKey(clientChannel) && !(request.getCommand().split(" ")[0].equals("register") || request.getCommand().split(" ")[0].equals("login"))){
                            System.out.println("Получено сообщение от клиента: " + clientChannel.getRemoteAddress());
                            response = new Response("Вы не вошли в систему!");
                        } else {
                            try {
                                response = new Response(commandManager.doCommand(request));
                                if ((response.toString().equals("Вы вошли") || response.toString().equals("Вы зарегестрированы")) && (request.getCommand().split(" ")[0].equals("register") || request.getCommand().split(" ")[0].equals("login"))){
                                    registeredUsers.put(clientChannel, request.getLogin());
                                    for(Worker w: dataBaseManager.getWorkers(request.getLogin())){
                                        collectionManager.add(w);
                                    }
                                }
                            } catch (Exception e) {
                                response = new Response("Возникла ошибка" + e.getMessage());
                            }
                        }

                        System.out.println(request.getLogin());


                        Response finalResponse = response;
                        Thread thread1 = new Thread(() -> {
                            try {
                                sendAnswer(clientChannel, finalResponse);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        thread1.start();

                        clientChannel.register(selector, SelectionKey.OP_READ);
                    } catch (StreamCorruptedException e) {
                        System.err.println("Некорректный формат данных: " + e.getMessage());
                    } catch (EOFException e) {
                        System.err.println("Неполные данные, ждём продолжения...");
                    } catch (ClassNotFoundException | IOException e) {
                        System.err.println("Неизвестный класс объекта: " + e.getMessage());
                    }
                    }


                }
        } catch (IOException e) {
            System.err.println("Соединение сброшено: " + e.getMessage());
            clientChannel.close();
            key.cancel();
        }
    }

    public void sendAnswer(SocketChannel socket, Response r) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(r);
            objectOutputStream.flush(); // Гарантируем запись всех данных

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

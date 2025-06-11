package org.example.server.commands;

import org.example.data.network.Request;
import org.example.server.manager.CollectionManager;

public class ShuffleCommand implements BaseCommand {

    private final CollectionManager collectionManager;

    public ShuffleCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String executeCommand(Request i) {
        collectionManager.shuffle();
        return "Элементы коллекции были перемешаны";
    }

    @Override
    public String getCommandName() {
        return "shuffle";
    }

    @Override
    public String getCommandDescription() {
        return "перемешать элементы коллекции в случайном порядке";
    }
}

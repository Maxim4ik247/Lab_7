package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.manager.CollectionManager;
import org.example.server.manager.DataBaseManager;

public class RemoveFirstCommand implements BaseCommand {

    private final CollectionManager collectionManager;
    private final DataBaseManager dataBaseManager;

    public RemoveFirstCommand(CollectionManager collectionManager, DataBaseManager dataBaseManager) {
        this.collectionManager = collectionManager;
        this.dataBaseManager = dataBaseManager;
    }


    @Override
    public String executeCommand(Request i) {

        if(dataBaseManager.removeFirst(i.getLogin())){
            collectionManager.removeFirst();
            return "Первый элемент коллекции был удален";
        }
        return "Ощибка при удалении worker";
    }

    @Override
    public String getCommandName() {
        return "remove_first";
    }

    @Override
    public String getCommandDescription() {
        return "удалить первый элемент из коллекции";
    }
}

package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.manager.CollectionManager;
import org.example.server.manager.DataBaseManager;

public class ClearCommand implements BaseCommand{
    private final CollectionManager collectionManager;
    private final DataBaseManager dataBaseManager;

    public ClearCommand(CollectionManager collectionManager, DataBaseManager dataBaseManager) {
        this.collectionManager = collectionManager;
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public String executeCommand(Request i) {
        if (dataBaseManager.clear(i.getLogin())){
            collectionManager.clearCollection();
            return "Коллекция была очищена";
        }
        return "Ошибка при удалении";
    }

    @Override
    public String getCommandName() {
        return "clear";
    }

    @Override
    public String getCommandDescription() {
        return "очистить коллекцию";
    }
}

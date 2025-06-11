package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.manager.CollectionManager;
import org.example.server.manager.DataBaseManager;

public class AddCommand implements BaseCommand {
    private final CollectionManager collectionManager;
    private final DataBaseManager dataBaseManager;

    public AddCommand(CollectionManager collectionManager, DataBaseManager dataBaseManager) {
        this.collectionManager = collectionManager;
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public String executeCommand(Request i) {
        if(dataBaseManager.addWorker(i.getLogin(), i.getWorker())) {
            collectionManager.add(i.getWorker());
            return "Работник добавлен";
        }
        return "Ошибка. Проверьте корректность данных";
    }

    @Override
    public String getCommandName() {
        return "add";
    }

    @Override
    public String getCommandDescription() {
        return "добавить новый элемент в коллекцию";
    }
}

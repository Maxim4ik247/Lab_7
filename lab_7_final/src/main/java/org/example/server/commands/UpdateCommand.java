package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.manager.*;

public class UpdateCommand implements BaseCommand {


    private CollectionManager collectionManager;
    private WorkerCreator workerCreator;
    private final DataBaseManager dataBaseManager;

    public UpdateCommand(DataBaseManager dataBaseManager, WorkerCreator workerCreator, CollectionManager collectionManager) {
        this.dataBaseManager = dataBaseManager;
        this.workerCreator = workerCreator;
        this.collectionManager = collectionManager;
    }

    @Override
    public String executeCommand(Request i) {
        int id = Integer.parseInt(i.getCommand().split(" ")[1]);
        if(dataBaseManager.updateWorker(i.getLogin(), i.getWorker(), id)) {
            collectionManager.add(i.getWorker());
            return "Работник добавлен";
        }
        return "Ошибка. Проверьте корректность данных";
    }

    @Override
    public String getCommandName() {
        return "update";
    }

    @Override
    public String getCommandDescription() {
        return "обновить значение элемента коллекции, id которого равен заданному";
    }
}

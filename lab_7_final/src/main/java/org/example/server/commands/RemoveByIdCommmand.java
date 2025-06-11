package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.manager.CollectionManager;
import org.example.server.manager.DataBaseManager;

public class RemoveByIdCommmand implements BaseCommand {


    private final CollectionManager collectionManager;
    private final DataBaseManager dataBaseManager;


    public RemoveByIdCommmand(CollectionManager collectionManager, DataBaseManager dataBaseManager) {
        this.collectionManager = collectionManager;
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public String executeCommand(Request i) {

        String[] args = i.getCommand().trim().split(" ");
        if (args.length < 2) {
            return "Ошибка: команда remove_by_id требует указания ID.";
        }

        try {
            int id = Integer.parseInt(args[1]);

            int before = collectionManager.getworkerLinkedList().size();
            collectionManager.removeId(id);
            int after = collectionManager.getworkerLinkedList().size();
            if (before == after) {
                return "Ничего не было удалено, введите корректный id";
            }
            dataBaseManager.removeById(id, i.getLogin());
            return "Worker был удален";

        } catch (NumberFormatException e) {
            return "id должен быть числом.";
        }
    }


    @Override
    public String getCommandName() {
        return "remove_by_id";
    }

    @Override
    public String getCommandDescription() {
        return "удалить элемент из коллекции по его id";
    }
}

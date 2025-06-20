package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.comparators.WorkerComparatorByName;
import org.example.server.manager.CollectionManager;

public class PrintAscendingCommand implements BaseCommand {

    private final CollectionManager collectionManager;

    public PrintAscendingCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String executeCommand(Request i) {
        WorkerComparatorByName comparator = new WorkerComparatorByName();
        collectionManager.getworkerLinkedList().stream().sorted(comparator).forEach(worker -> {
            System.out.println(worker);
        });
        return "";
    }

    @Override
    public String getCommandName() {
        return "print_ascending";
    }

    @Override
    public String getCommandDescription() {
        return "вывести элементы коллекции в порядке возрастания";
    }
}

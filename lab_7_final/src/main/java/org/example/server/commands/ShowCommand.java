package org.example.server.commands;


import org.example.data.Worker;
import org.example.data.network.Request;
import org.example.server.manager.DataBaseManager;

public class ShowCommand implements BaseCommand {

    private final DataBaseManager dataBaseManager;

    public ShowCommand(DataBaseManager dataBaseManager) {
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public String executeCommand(Request i) {
        String p = "";
        for (Worker w : dataBaseManager.getWorkers(i.getLogin()))
            p += w.toString() + "\n";

        return p;
    }

    @Override
    public String getCommandName() {
        return "show";
    }

    @Override
    public String getCommandDescription() {
        return "вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    }
}

package org.example.server.manager;





import org.example.data.network.Request;
import org.example.data.network.Response;
import org.example.server.commands.*;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;


public class CommandManager {
    private final HashMap<String, BaseCommand> commandMap = new HashMap<>();

    public CommandManager(CollectionManager collectionManager, WorkerCreator workerCreator, DataBaseManager dataBaseManager) {
        commandMap.put("help", new HelpCommand(this));
        commandMap.put("info", new InfoCommand(collectionManager));
        commandMap.put("show", new ShowCommand(dataBaseManager));
        commandMap.put("add", new AddCommand(collectionManager, dataBaseManager));
        commandMap.put("update", new UpdateCommand(dataBaseManager, workerCreator, collectionManager));
        commandMap.put("remove_by_id", new RemoveByIdCommmand(collectionManager, dataBaseManager));
        commandMap.put("clear", new ClearCommand(collectionManager, dataBaseManager));
        commandMap.put("remove_first", new RemoveFirstCommand(collectionManager, dataBaseManager));
        commandMap.put("shuffle", new ShuffleCommand(collectionManager));
        commandMap.put("reorder", new ReorderCommand(collectionManager));
        commandMap.put("average_of_salary", new AverageOfSalaryCommand(collectionManager));
        commandMap.put("print_ascending", new PrintAscendingCommand(collectionManager));
        commandMap.put("print_field_descending_salary", new PrintFieldDescendingSalary(collectionManager));
        commandMap.put("login", new LoginCommand(dataBaseManager));
        commandMap.put("register", new RegisterCommand(dataBaseManager));
    }

    public String doCommand(Request input) {
        String commandName = input.getCommand().split(" ")[0];
        BaseCommand command = commandMap.get(commandName);
        System.out.println(commandName);
        if (command != null) {
            return command.executeCommand(input);
        } else {
            return "Неправильная команда: " + commandName;
        }
    }

    public CompletableFuture<String> doCommandAsync(Request input) {
        return CompletableFuture.supplyAsync(() -> doCommand(input));
    }


    public String help() {
        String output = "";
        for (BaseCommand c : commandMap.values()) {
            output += c.getCommandName() + " - " + c.getCommandDescription() + "\n";
            System.out.println(c.getCommandName() + " - " + c.getCommandDescription());
        }
        return output;
    }

}

package org.example.server.commands;


import org.example.data.network.Request;
import org.example.server.manager.CommandManager;

public class HelpCommand implements BaseCommand {
    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String executeCommand(Request i) {
        return commandManager.help();
    }


    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public String getCommandDescription() {
        return "вывести справку по доступным командам";
    }
}

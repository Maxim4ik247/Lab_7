package org.example.server.commands;

import org.example.data.network.Request;
import org.example.server.manager.DataBaseManager;

public class RegisterCommand implements BaseCommand{

    private String login;
    private String password;
    private final DataBaseManager dataBaseManager;

    public RegisterCommand(DataBaseManager dataBaseManager) {
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public String executeCommand(Request input) {
        login = input.getCommand().split(" ")[1];
        password = input.getCommand().split(" ")[2];
        if(dataBaseManager.insertUser(login,password)) {
            return "Вы зарегистрированы";
        }
        return "";
    }

    @Override
    public String getCommandName() {
        return "";
    }

    @Override
    public String getCommandDescription() {
        return "";
    }
}

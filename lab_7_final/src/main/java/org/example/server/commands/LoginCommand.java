package org.example.server.commands;

import org.example.data.network.Request;
import org.example.server.manager.DataBaseManager;


public class LoginCommand implements BaseCommand{

    private String login;
    private String password;
    private final DataBaseManager dataBaseManager;

    public LoginCommand(DataBaseManager dataBaseManager) {
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public String executeCommand(Request input) {
        login = input.getCommand().split(" ")[1];
        password = input.getCommand().split(" ")[2];
        if (dataBaseManager.checkUser(login,password)){
            return ("Вы вошли");
        }
        else {
            return "Ошибка. Неверный логин или пароль";
        }
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

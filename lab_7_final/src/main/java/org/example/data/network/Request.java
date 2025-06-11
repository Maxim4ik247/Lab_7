package org.example.data.network;




import org.example.data.Worker;

import java.io.Serializable;

public class Request implements Serializable {
    private String command;
    private Worker worker;
    private String login;
    private String password;

    public Request(String command) {
        this.command = command;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public Worker getWorker() {
        return worker;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Request{" +
                "command='" + command + '\'' +
                '}';
    }
}

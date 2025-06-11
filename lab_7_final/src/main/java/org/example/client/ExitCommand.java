package org.example.client;

public class ExitCommand {
    public String executeCommand() {
        System.out.println("Программа завершает свою работу");
        System.exit(0);
        return "";
    }
}
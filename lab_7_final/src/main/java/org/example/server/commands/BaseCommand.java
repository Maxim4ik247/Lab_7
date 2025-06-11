package org.example.server.commands;


import org.example.data.network.Request;

public interface BaseCommand {
    String executeCommand(Request input);

    String getCommandName();

    String getCommandDescription();
}

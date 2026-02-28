package com.xbuilders.engine.server.commands;

import com.xbuilders.engine.client.ClientSettings;
import com.xbuilders.engine.common.players.Player;

import java.util.Objects;

public class Command {
    public final String commandName;
    public final String commandHelp;

    @FunctionalInterface
    public interface CommandFunction {
        // No need to declare the abstract method apply(T t) here
        // because Function already declares it and this interface inherits it.

        // You can add default or static methods if needed,
        // but the core requirement of a functional interface is a single abstract method.
        public String apply(String[] command, Player askingPlayer);
    }

    public CommandFunction serverExecute;
    public boolean requiresOp = false;

    public Command(String name, String help) {
        this.commandName = Objects.requireNonNull(name).toLowerCase();
        this.commandHelp = Objects.requireNonNull(help);
    }

    protected String runCommand(String[] input, Player player) {
        if (
                (requiresOp
                        && !player.isOperator()
                        && !ClientSettings.load().dev_allowOPCommands)
        ) return "You do not have the required permissions";
        if (serverExecute != null) return serverExecute.apply(input, player);
        return null;
    }

    public Command executesServerSide(CommandFunction handle) {
        this.serverExecute = handle;
        return this;
    }


    public Command requiresOP(boolean requiresOp) {
        this.requiresOp = requiresOp;
        return this;
    }
}

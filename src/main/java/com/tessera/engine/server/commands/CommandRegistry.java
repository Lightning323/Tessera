package com.tessera.engine.server.commands;

import com.tessera.engine.common.players.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Commands are registered and handled JUST on the Server
 * When a command is executed
 * - the client sends the "command"
 * - the server executes the command
 * - returns any output to the client
 */
public class CommandRegistry {

    public CommandRegistry() {
    }

    private  final HashMap<String, Command> commands = new HashMap<>();

    public  void registerCommand(Command command) {
        commands.put(command.commandName.toLowerCase(), command);
    }

    private String[] removeFirstN(String[] input, int n) {
        if (input == null || input.length == 0 || n <= 0) {
            return input; // Handle null, empty array, or non-positive n
        }
        if (n >= input.length) {
            return new String[0]; // If n is greater than or equal to the length, return an empty array
        }
        return Arrays.copyOfRange(input, n, input.length);
    }

    private static String[] splitWhitespacePreserveQuotes(String input) {
        ArrayList<String> parts = new ArrayList<>();

        Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1)); // Add double-quoted string without quotes
            } else if (matcher.group(2) != null) {
                parts.add(matcher.group(2)); // Add single-quoted string without quotes
            } else {
                parts.add(matcher.group()); // Add unquoted word
            }
        }

        return parts.toArray(new String[0]);
    }

    public String handleCommand(String inputString, Player player) {
        try {
            String[] parts = splitWhitespacePreserveQuotes(inputString);
            System.out.println("handling command: " + Arrays.toString(parts));
            if (parts.length == 0) return null;

            if (parts[0].equalsIgnoreCase("help")) { //Help builtin command
                if (parts.length >= 2) {
                    Command command = commands.get(parts[1].toLowerCase());
                    if (command == null) return "Unknown command.";
                    else return command.commandHelp;
                }
                final StringBuilder out = new StringBuilder("Commands:\n");
                commands.forEach((key, command) -> {
                    out.append(key).append(": ").append(command.commandHelp).append("\n\n");
                });
                return out.toString();
            } else { //Other commandRegistry
                Command command = commands.get(parts[0].toLowerCase());
                if (command == null) return "Unknown command. Type 'help' for a list of commandRegistry";
                else {
                    parts = removeFirstN(parts, 1);
                    String out = command.runCommand(parts,player);
                    if (out == null) return command.commandHelp;
                    else return out;
                }
            }
        } catch (Exception e) {
            return "Error with command: " + e;
        }
    }
}

package io.github.patbattb.moderant;

public class ArgsParser {
    public String getBotToken(String[] args) {
        if (args.length != 1) {
            System.out.println("You can pass bot token as single argument.");
            System.exit(1);
        }
        return args[0];
    }
}

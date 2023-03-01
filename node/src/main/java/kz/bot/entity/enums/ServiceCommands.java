package kz.bot.entity.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");
    private final String cmd;


    @Override
    public String toString() {
        return cmd;
    }

    public boolean equals(String cmd) {
        return this.toString().equals(cmd);
    }
}

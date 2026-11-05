package com.motol.dialogsplus.model;

public class MenuAction {

    public enum Type {
        RUN_COMMAND,
        RUN_COMMAND_AS_CONSOLE,
        OPEN_MENU,
        CLOSE,
        SUGGEST_COMMAND,
        SEND_MESSAGE,
        OPEN_URL
    }

    private Type type;
    private String command;
    private String target;
    private String message;
    private String url;

    public MenuAction() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
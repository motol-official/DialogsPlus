package com.motol.dialogsplus.model;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private String id;
    private String title;
    private String externalTitle;
    private boolean canCloseWithEscape = true;
    private List<String> body = new ArrayList<>();
    private List<MenuComponent> components = new ArrayList<>();

    public Menu() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExternalTitle() {
        return externalTitle;
    }

    public void setExternalTitle(String externalTitle) {
        this.externalTitle = externalTitle;
    }

    public boolean isCanCloseWithEscape() {
        return canCloseWithEscape;
    }

    public void setCanCloseWithEscape(boolean canCloseWithEscape) {
        this.canCloseWithEscape = canCloseWithEscape;
    }

    public List<String> getBody() {
        return body;
    }

    public void setBody(List<String> body) {
        this.body = body;
    }

    public List<MenuComponent> getComponents() {
        return components;
    }

    public void setComponents(List<MenuComponent> components) {
        this.components = components;
    }

    private String permission;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
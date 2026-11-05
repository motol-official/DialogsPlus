package com.motol.dialogsplus.model;

import java.util.ArrayList;
import java.util.List;

public class MenuComponent {

    public enum Type {
        BUTTON,
        TEXT_INPUT,
        BOOLEAN_INPUT,
        NUMBER_INPUT,
        SINGLE_OPTION_INPUT
    }

    private String id;
    private String label;
    private String icon;
    private String tooltip;
    private Type type;
    private String placeholder;
    private String permission;
    private Double min;
    private Double max;
    private Double step;
    private Double initial;
    private List<String> options = new ArrayList<>();
    private List<MenuAction> actions = new ArrayList<>();

    public MenuComponent() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

    public Double getInitial() {
        return initial;
    }

    public void setInitial(Double initial) {
        this.initial = initial;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<MenuAction> getActions() {
        return actions;
    }

    public void setActions(List<MenuAction> actions) {
        this.actions = actions;
    }
}
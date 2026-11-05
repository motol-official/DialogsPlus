package com.motol.dialogsplus.editor;

import com.motol.dialogsplus.dialog.ActionExecutor;
import com.motol.dialogsplus.dialog.DialogBuilder;
import com.motol.dialogsplus.model.Menu;
import com.motol.dialogsplus.model.MenuAction;
import com.motol.dialogsplus.model.MenuComponent;
import com.motol.dialogsplus.storage.MenuManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class MenuEditor {

    public static final String PERMISSION = "dialogsplus.edit";

    private static final int TEXT_WIDTH = 300;
    private static final int TEXT_MAX = 512;
    private static final Pattern MENU_ID = Pattern.compile("[a-zA-Z0-9_-]+");
    private static final Pattern COMPONENT_ID = Pattern.compile("[a-zA-Z0-9_]+");

    private final MenuManager menuManager;
    private final ActionExecutor actionExecutor;

    public MenuEditor(MenuManager menuManager, ActionExecutor actionExecutor) {
        this.menuManager = menuManager;
        this.actionExecutor = actionExecutor;
    }

    public void open(Player player) {
        showMenuList(player, null);
    }

    public void open(Player player, String menuId) {
        if (menuManager.getMenu(menuId) == null) {
            showMenuList(player, "Menu not found: " + menuId);
        } else {
            showMenu(player, menuId, null);
        }
    }

    private void showMenuList(Player p, String notice) {
        List<String> body = new ArrayList<>();
        if (notice != null) body.add(notice);
        body.add("Pick a menu to edit. Changes are saved to plugins/DialogsPlus/menus/ immediately.");

        List<ActionButton> buttons = new ArrayList<>();
        List<String> ids = new ArrayList<>(menuManager.getAllMenus().keySet());
        Collections.sort(ids);
        for (String id : ids) {
            buttons.add(button(id, (pl, v) -> showMenu(pl, id, null)));
        }
        buttons.add(button("+ New menu", (pl, v) -> showNewMenu(pl, null)));
        buttons.add(button("Close", (pl, v) -> pl.closeDialog()));

        show(p, "Dialogs+ Editor", body, List.of(), buttons, 2);
    }

    private void showMenu(Player p, String id, String notice) {
        Menu menu = menuManager.getMenu(id);
        if (menu == null) {
            showMenuList(p, "Menu not found: " + id);
            return;
        }

        int buttonCount = 0;
        for (MenuComponent c : menu.getComponents()) {
            if (c.getType() == MenuComponent.Type.BUTTON) buttonCount++;
        }

        List<String> body = new ArrayList<>();
        if (notice != null) body.add(notice);
        body.add("Menu: " + id + "   (open with /dialogs " + id + ")");
        body.add("Title: " + menu.getTitle());
        body.add("External title: " + (isBlank(menu.getExternalTitle()) ? "-" : menu.getExternalTitle()));
        body.add("Permission: " + (isBlank(menu.getPermission()) ? "none" : menu.getPermission()));
        body.add("Close with Escape: " + (menu.isCanCloseWithEscape() ? "yes" : "no"));
        body.add("Body lines: " + menu.getBody().size() + "   Components: " + menu.getComponents().size());
        if (buttonCount == 0) {
            body.add("WARNING: this menu has no buttons, so it will fail to open. Add at least one BUTTON.");
        }

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Settings", (pl, v) -> showSettings(pl, menu, null)));
        buttons.add(button("Body text", (pl, v) -> showBody(pl, menu)));
        buttons.add(button("Components (" + menu.getComponents().size() + ")",
                (pl, v) -> showComponents(pl, menu, null)));
        buttons.add(button("Preview",
                "Opens the real menu. Its buttons really run their actions!",
                (pl, v) -> {
                    try {
                        pl.showDialog(DialogBuilder.build(menu, pl, actionExecutor::execute));
                    } catch (Exception e) {
                        showMenu(pl, id, "Preview failed: " + e.getMessage());
                    }
                }));
        buttons.add(button("Delete menu", (pl, v) -> showConfirm(pl,
                "Delete menu",
                "Really delete '" + id + "'? A copy is kept in plugins/DialogsPlus/backups/.",
                () -> {
                    try {
                        menuManager.delete(id);
                        showMenuList(pl, "Deleted menu '" + id + "'.");
                    } catch (IOException e) {
                        showMenu(pl, id, "Delete failed: " + e.getMessage());
                    }
                },
                () -> showMenu(pl, id, null))));
        buttons.add(button("Back to list", (pl, v) -> showMenuList(pl, null)));

        show(p, "Editing: " + id, body, List.of(), buttons, 2);
    }

    private void showNewMenu(Player p, String error) {
        List<String> body = new ArrayList<>();
        if (error != null) body.add(error);
        body.add("The ID is the file name and what you type in /dialogs <id>. Letters, numbers, - and _ only.");

        List<DialogInput> inputs = List.of(
                text("id", "Menu ID", ""),
                text("title", "Title", "")
        );

        List<ActionButton> buttons = List.of(
                button("Create", (pl, v) -> {
                    String id = clean(v.getText("f_id"));
                    String title = clean(v.getText("f_title"));
                    if (id == null || !MENU_ID.matcher(id).matches()) {
                        showNewMenu(pl, "Invalid ID. Use letters, numbers, - and _ only.");
                        return;
                    }
                    if (menuManager.getMenu(id) != null) {
                        showNewMenu(pl, "A menu with ID '" + id + "' already exists.");
                        return;
                    }

                    Menu menu = new Menu();
                    menu.setId(id);
                    menu.setTitle(title == null ? id : title);
                    menu.setBody(new ArrayList<>());

                    MenuComponent close = new MenuComponent();
                    close.setId("close");
                    close.setLabel("Close");
                    close.setType(MenuComponent.Type.BUTTON);
                    MenuAction closeAction = new MenuAction();
                    closeAction.setType(MenuAction.Type.CLOSE);
                    close.getActions().add(closeAction);
                    menu.getComponents().add(close);

                    String err = save(menu);
                    showMenu(pl, id, err != null ? err : "Menu created.");
                }),
                button("Cancel", (pl, v) -> showMenuList(pl, null))
        );

        show(p, "New menu", body, inputs, buttons, 2);
    }

    private void showSettings(Player p, Menu menu, String error) {
        List<String> body = new ArrayList<>();
        if (error != null) body.add(error);
        body.add("Leave external title / permission empty to disable them.");

        List<DialogInput> inputs = List.of(
                text("title", "Title", menu.getTitle()),
                text("external", "External title (optional)", menu.getExternalTitle()),
                text("permission", "Permission to open (optional)", menu.getPermission()),
                DialogInput.bool("f_escape", Component.text("Can close with Escape"))
                        .initial(menu.isCanCloseWithEscape())
                        .build()
        );

        List<ActionButton> buttons = List.of(
                button("Save", (pl, v) -> {
                    String title = clean(v.getText("f_title"));
                    if (title == null) {
                        showSettings(pl, menu, "Title cannot be empty.");
                        return;
                    }
                    menu.setTitle(title);
                    menu.setExternalTitle(clean(v.getText("f_external")));
                    menu.setPermission(clean(v.getText("f_permission")));
                    menu.setCanCloseWithEscape(Boolean.TRUE.equals(v.getBoolean("f_escape")));
                    String err = save(menu);
                    showMenu(pl, menu.getId(), err != null ? err : "Settings saved.");
                }),
                button("Cancel", (pl, v) -> showMenu(pl, menu.getId(), null))
        );

        show(p, "Settings: " + menu.getId(), body, inputs, buttons, 2);
    }

    private void showBody(Player p, Menu menu) {
        List<String> body = List.of("One line per message shown above the inputs. PlaceholderAPI placeholders work.");

        List<DialogInput> inputs = List.of(
                multiline("body", "Body text", menu.getBody())
        );

        List<ActionButton> buttons = List.of(
                button("Save", (pl, v) -> {
                    String raw = v.getText("f_body");
                    List<String> lines = new ArrayList<>();
                    if (raw != null && !raw.isBlank()) {
                        for (String line : raw.split("\\R", -1)) {
                            lines.add(line);
                        }
                        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
                            lines.remove(lines.size() - 1);
                        }
                    }
                    menu.setBody(lines);
                    String err = save(menu);
                    showMenu(pl, menu.getId(), err != null ? err : "Body saved.");
                }),
                button("Cancel", (pl, v) -> showMenu(pl, menu.getId(), null))
        );

        show(p, "Body text: " + menu.getId(), body, inputs, buttons, 2);
    }

    private void showComponents(Player p, Menu menu, String notice) {
        List<String> body = new ArrayList<>();
        if (notice != null) body.add(notice);
        body.add("Components in the order they appear. Inputs are shown above the buttons.");

        List<ActionButton> buttons = new ArrayList<>();
        List<MenuComponent> list = menu.getComponents();
        for (int i = 0; i < list.size(); i++) {
            MenuComponent c = list.get(i);
            String type = c.getType() == null ? "?" : c.getType().name();
            buttons.add(button((i + 1) + ". " + type + " - " + c.getId(),
                    (pl, v) -> showComponent(pl, menu, c, null)));
        }
        buttons.add(button("+ Add component", (pl, v) -> showAddComponent(pl, menu, null)));
        buttons.add(button("Back", (pl, v) -> showMenu(pl, menu.getId(), null)));

        show(p, "Components: " + menu.getId(), body, List.of(), buttons, 1);
    }

    private void showAddComponent(Player p, Menu menu, String error) {
        List<String> body = new ArrayList<>();
        if (error != null) body.add(error);
        body.add("ID: letters, numbers and _ only. Use {id} in actions to insert an input's value.");

        List<DialogInput> inputs = List.of(
                enumInput("type", "Type", MenuComponent.Type.values(), MenuComponent.Type.BUTTON),
                text("id", "ID", ""),
                text("label", "Label", "")
        );

        List<ActionButton> buttons = List.of(
                button("Create", (pl, v) -> {
                    String id = clean(v.getText("f_id"));
                    String label = clean(v.getText("f_label"));
                    MenuComponent.Type type;
                    try {
                        type = MenuComponent.Type.valueOf(String.valueOf(v.getText("f_type")));
                    } catch (IllegalArgumentException e) {
                        showAddComponent(pl, menu, "Pick a type.");
                        return;
                    }
                    if (id == null || !COMPONENT_ID.matcher(id).matches()) {
                        showAddComponent(pl, menu, "Invalid ID. Use letters, numbers and _ only.");
                        return;
                    }
                    for (MenuComponent other : menu.getComponents()) {
                        if (id.equals(other.getId())) {
                            showAddComponent(pl, menu, "A component with ID '" + id + "' already exists.");
                            return;
                        }
                    }

                    MenuComponent c = new MenuComponent();
                    c.setId(id);
                    c.setLabel(label == null ? id : label);
                    c.setType(type);
                    if (type == MenuComponent.Type.SINGLE_OPTION_INPUT) {
                        c.setOptions(new ArrayList<>(List.of("Option 1", "Option 2")));
                    }
                    menu.getComponents().add(c);

                    String err = save(menu);
                    showComponent(pl, menu, c, err != null ? err : "Component created.");
                }),
                button("Cancel", (pl, v) -> showComponents(pl, menu, null))
        );

        show(p, "Add component", body, inputs, buttons, 2);
    }

    private void showComponent(Player p, Menu menu, MenuComponent c, String notice) {
        MenuComponent.Type type = c.getType();
        if (type == null) {
            showComponents(p, menu, "Component '" + c.getId() + "' has no valid type. Delete it in the YAML file.");
            return;
        }

        List<String> body = new ArrayList<>();
        if (notice != null) body.add(notice);
        body.add("Type: " + type.name() + "  (to change the type, delete and re-add the component)");
        if (type == MenuComponent.Type.BUTTON) {
            body.add("Icon examples: GRASS_BLOCK, player_head:{self}, player_head:Notch, sprite:item/iron_sword");
        } else {
            body.add("Use {" + c.getId() + "} in an action's command or message to insert this input's value.");
        }

        List<DialogInput> inputs = new ArrayList<>();
        inputs.add(text("id", "ID (letters, numbers, _)", c.getId()));
        inputs.add(text("label", "Label", c.getLabel()));
        inputs.add(text("permission", "Permission (optional)", c.getPermission()));
        switch (type) {
            case BUTTON -> {
                inputs.add(text("icon", "Icon (optional)", c.getIcon()));
                inputs.add(text("tooltip", "Tooltip (optional)", c.getTooltip()));
            }
            case NUMBER_INPUT -> {
                inputs.add(text("min", "Min (default 0)", fmt(c.getMin())));
                inputs.add(text("max", "Max (default 100)", fmt(c.getMax())));
                inputs.add(text("step", "Step (optional)", fmt(c.getStep())));
                inputs.add(text("initial", "Initial value (optional)", fmt(c.getInitial())));
            }
            case SINGLE_OPTION_INPUT ->
                    inputs.add(multiline("options", "Options (one per line)", c.getOptions()));
            default -> {
            }
        }

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Save", (pl, v) -> {
            String err = applyComponentFields(menu, c, v);
            if (err != null) {
                showComponent(pl, menu, c, err);
                return;
            }
            String saveErr = save(menu);
            showComponents(pl, menu, saveErr != null ? saveErr : "Component saved.");
        }));
        if (type == MenuComponent.Type.BUTTON) {
            buttons.add(button("Actions (" + c.getActions().size() + ")", (pl, v) -> {
                String err = applyComponentFields(menu, c, v);
                if (err != null) {
                    showComponent(pl, menu, c, err);
                    return;
                }
                String saveErr = save(menu);
                showActions(pl, menu, c, saveErr);
            }));
        }
        buttons.add(button("Move up", (pl, v) -> moveComponent(pl, menu, c, v, -1)));
        buttons.add(button("Move down", (pl, v) -> moveComponent(pl, menu, c, v, 1)));
        buttons.add(button("Delete", (pl, v) -> showConfirm(pl,
                "Delete component",
                "Delete '" + c.getId() + "'?",
                () -> {
                    menu.getComponents().remove(c);
                    String err = save(menu);
                    showComponents(pl, menu, err != null ? err : "Component deleted.");
                },
                () -> showComponent(pl, menu, c, null))));
        buttons.add(button("Back (discard changes)", (pl, v) -> showComponents(pl, menu, null)));

        show(p, "Component: " + c.getId(), body, inputs, buttons, 2);
    }

    private void moveComponent(Player p, Menu menu, MenuComponent c, DialogResponseView v, int delta) {
        String err = applyComponentFields(menu, c, v);
        if (err != null) {
            showComponent(p, menu, c, err);
            return;
        }
        move(menu.getComponents(), c, delta);
        String saveErr = save(menu);
        showComponent(p, menu, c, saveErr != null ? saveErr : "Moved.");
    }

    private String applyComponentFields(Menu menu, MenuComponent c, DialogResponseView v) {
        String newId = clean(v.getText("f_id"));
        if (newId == null || !COMPONENT_ID.matcher(newId).matches()) {
            return "Invalid ID. Use letters, numbers and _ only.";
        }
        for (MenuComponent other : menu.getComponents()) {
            if (other != c && newId.equals(other.getId())) {
                return "Another component already uses the ID '" + newId + "'.";
            }
        }

        Double min = null, max = null, step = null, initial = null;
        List<String> options = null;

        if (c.getType() == MenuComponent.Type.NUMBER_INPUT) {
            try {
                min = parseNumber(v.getText("f_min"));
                max = parseNumber(v.getText("f_max"));
                step = parseNumber(v.getText("f_step"));
                initial = parseNumber(v.getText("f_initial"));
            } catch (NumberFormatException e) {
                return "Min, Max, Step and Initial must be numbers (or empty).";
            }
            double effMin = min != null ? min : 0;
            double effMax = max != null ? max : 100;
            if (effMin >= effMax) return "Min must be lower than Max.";
            if (step != null && step <= 0) return "Step must be greater than 0.";
            if (initial != null && (initial < effMin || initial > effMax)) {
                return "Initial value must be between Min and Max.";
            }
        }

        if (c.getType() == MenuComponent.Type.SINGLE_OPTION_INPUT) {
            options = new ArrayList<>();
            String raw = v.getText("f_options");
            if (raw != null) {
                for (String line : raw.split("\\R")) {
                    if (!line.isBlank()) options.add(line.trim());
                }
            }
            if (options.isEmpty()) return "Add at least one option.";
        }

        c.setId(newId);
        String label = clean(v.getText("f_label"));
        c.setLabel(label == null ? newId : label);
        c.setPermission(clean(v.getText("f_permission")));

        switch (c.getType()) {
            case BUTTON -> {
                c.setIcon(clean(v.getText("f_icon")));
                c.setTooltip(clean(v.getText("f_tooltip")));
            }
            case NUMBER_INPUT -> {
                c.setMin(min);
                c.setMax(max);
                c.setStep(step);
                c.setInitial(initial);
            }
            case SINGLE_OPTION_INPUT -> c.setOptions(options);
            default -> {
            }
        }
        return null;
    }

    private void showActions(Player p, Menu menu, MenuComponent c, String notice) {
        List<String> body = new ArrayList<>();
        if (notice != null) body.add(notice);
        body.add("Actions run in order when the button is clicked.");

        List<ActionButton> buttons = new ArrayList<>();
        List<MenuAction> list = c.getActions();
        for (int i = 0; i < list.size(); i++) {
            MenuAction a = list.get(i);
            buttons.add(button((i + 1) + ". " + describe(a), (pl, v) -> showAction(pl, menu, c, a, null)));
        }
        buttons.add(button("+ Add action", (pl, v) -> showAction(pl, menu, c, null, null)));
        buttons.add(button("Back", (pl, v) -> showComponent(pl, menu, c, null)));

        show(p, "Actions: " + c.getId(), body, List.of(), buttons, 1);
    }

    private void showAction(Player p, Menu menu, MenuComponent c, MenuAction existing, String error) {
        List<String> body = new ArrayList<>();
        if (error != null) body.add(error);
        body.add("Fill in the field that matches the type; the others are ignored.");
        body.add("Use {input_id} to insert the value of an input from this menu.");

        List<DialogInput> inputs = new ArrayList<>();
        inputs.add(enumInput("type", "Action type", MenuAction.Type.values(),
                existing != null && existing.getType() != null ? existing.getType() : MenuAction.Type.RUN_COMMAND));
        inputs.add(text("command", "Command (RUN_COMMAND, RUN_COMMAND_AS_CONSOLE, SUGGEST_COMMAND)",
                existing == null ? null : existing.getCommand()));
        inputs.add(text("target", "Target menu ID (OPEN_MENU)", existing == null ? null : existing.getTarget()));
        inputs.add(text("message", "Message (SEND_MESSAGE)", existing == null ? null : existing.getMessage()));
        inputs.add(text("url", "URL (OPEN_URL)", existing == null ? null : existing.getUrl()));

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Save", (pl, v) -> saveAction(pl, menu, c, existing, v)));
        if (existing != null) {
            buttons.add(button("Move up", (pl, v) -> {
                move(c.getActions(), existing, -1);
                String err = save(menu);
                showActions(pl, menu, c, err != null ? err : "Moved.");
            }));
            buttons.add(button("Move down", (pl, v) -> {
                move(c.getActions(), existing, 1);
                String err = save(menu);
                showActions(pl, menu, c, err != null ? err : "Moved.");
            }));
            buttons.add(button("Delete", (pl, v) -> {
                c.getActions().remove(existing);
                String err = save(menu);
                showActions(pl, menu, c, err != null ? err : "Action deleted.");
            }));
        }
        buttons.add(button("Back (discard changes)", (pl, v) -> showActions(pl, menu, c, null)));

        show(p, existing == null ? "New action" : "Edit action", body, inputs, buttons, 2);
    }

    private void saveAction(Player p, Menu menu, MenuComponent c, MenuAction existing, DialogResponseView v) {
        MenuAction.Type type;
        try {
            type = MenuAction.Type.valueOf(String.valueOf(v.getText("f_type")));
        } catch (IllegalArgumentException e) {
            showAction(p, menu, c, existing, "Pick an action type.");
            return;
        }

        String command = clean(v.getText("f_command"));
        String target = clean(v.getText("f_target"));
        String message = clean(v.getText("f_message"));
        String url = clean(v.getText("f_url"));

        String problem = switch (type) {
            case RUN_COMMAND, RUN_COMMAND_AS_CONSOLE, SUGGEST_COMMAND ->
                    command == null ? "This action type needs a command." : null;
            case OPEN_MENU -> target == null ? "This action type needs a target menu ID." : null;
            case SEND_MESSAGE -> message == null ? "This action type needs a message." : null;
            case OPEN_URL -> (url == null || !(url.startsWith("http://") || url.startsWith("https://")))
                    ? "URL must start with http:// or https://." : null;
            case CLOSE -> null;
        };
        if (problem != null) {
            showAction(p, menu, c, existing, problem);
            return;
        }

        boolean isCommand = type == MenuAction.Type.RUN_COMMAND
                || type == MenuAction.Type.RUN_COMMAND_AS_CONSOLE
                || type == MenuAction.Type.SUGGEST_COMMAND;
        if ((type == MenuAction.Type.RUN_COMMAND || type == MenuAction.Type.RUN_COMMAND_AS_CONSOLE)
                && command != null && command.startsWith("/")) {
            command = command.substring(1);
        }

        MenuAction action = existing != null ? existing : new MenuAction();
        action.setType(type);
        action.setCommand(isCommand ? command : null);
        action.setTarget(type == MenuAction.Type.OPEN_MENU ? target : null);
        action.setMessage(type == MenuAction.Type.SEND_MESSAGE ? message : null);
        action.setUrl(type == MenuAction.Type.OPEN_URL ? url : null);
        if (existing == null) {
            c.getActions().add(action);
        }

        String err = save(menu);
        String note;
        if (err != null) {
            note = err;
        } else if (type == MenuAction.Type.OPEN_MENU && menuManager.getMenu(target) == null) {
            note = "Action saved. Note: menu '" + target + "' doesn't exist (yet).";
        } else {
            note = "Action saved.";
        }
        showActions(p, menu, c, note);
    }

    private String describe(MenuAction a) {
        if (a.getType() == null) return "(invalid action)";
        String detail = switch (a.getType()) {
            case RUN_COMMAND, RUN_COMMAND_AS_CONSOLE, SUGGEST_COMMAND -> a.getCommand();
            case OPEN_MENU -> a.getTarget();
            case SEND_MESSAGE -> a.getMessage();
            case OPEN_URL -> a.getUrl();
            case CLOSE -> null;
        };
        if (detail == null) return a.getType().name();
        if (detail.length() > 36) detail = detail.substring(0, 33) + "...";
        return a.getType().name() + ": " + detail;
    }

    private void showConfirm(Player p, String title, String message, Runnable onYes, Runnable onNo) {
        show(p, title, List.of(message), List.of(), List.of(
                button("Yes", (pl, v) -> onYes.run()),
                button("Cancel", (pl, v) -> onNo.run())
        ), 2);
    }

    private String save(Menu menu) {
        try {
            menuManager.save(menu);
            return null;
        } catch (IOException e) {
            return "Save failed: " + e.getMessage();
        }
    }

    private void show(Player player, String title, List<String> bodyLines, List<DialogInput> inputs,
                      List<ActionButton> buttons, int columns) {
        List<DialogBody> body = new ArrayList<>();
        for (String line : bodyLines) {
            body.add(DialogBody.plainMessage(Component.text(line)));
        }

        DialogBase base = DialogBase.builder(Component.text(title))
                .canCloseWithEscape(true)
                .body(body)
                .inputs(inputs)
                .build();

        player.showDialog(Dialog.create(builder -> builder
                .empty()
                .base(base)
                .type(DialogType.multiAction(buttons).columns(columns).build())
        ));
    }

    @FunctionalInterface
    private interface Click {
        void run(Player player, DialogResponseView view);
    }

    private ActionButton button(String label, Click click) {
        return button(label, null, click);
    }

    private ActionButton button(String label, String tooltip, Click click) {
        ActionButton.Builder builder = ActionButton.builder(Component.text(label));
        if (tooltip != null) {
            builder.tooltip(Component.text(tooltip));
        }
        builder.action(DialogAction.customClick(
                (view, audience) -> {
                    if (audience instanceof Player player) {
                        click.run(player, view);
                    }
                },
                ClickCallback.Options.builder().build()
        ));
        return builder.build();
    }

    private DialogInput text(String key, String label, String initial) {
        return DialogInput.text("f_" + key, Component.text(label))
                .width(TEXT_WIDTH)
                .maxLength(TEXT_MAX)
                .initial(initial == null ? "" : initial)
                .build();
    }

    private DialogInput multiline(String key, String label, List<String> lines) {
        return DialogInput.text("f_" + key, Component.text(label))
                .width(TEXT_WIDTH)
                .maxLength(2048)
                .multiline(TextDialogInput.MultilineOptions.create(null, 110))
                .initial(lines == null ? "" : String.join("\n", lines))
                .build();
    }

    private <E extends Enum<E>> DialogInput enumInput(String key, String label, E[] values, E selected) {
        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
        for (E value : values) {
            entries.add(SingleOptionDialogInput.OptionEntry.create(
                    value.name(), Component.text(value.name()), value == selected));
        }
        return DialogInput.singleOption("f_" + key, Component.text(label), entries).build();
    }

    private static <T> void move(List<T> list, T item, int delta) {
        int from = list.indexOf(item);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= list.size()) return;
        Collections.swap(list, from, to);
    }

    private static Double parseNumber(String s) {
        String cleaned = clean(s);
        if (cleaned == null) return null;
        return Double.parseDouble(cleaned.replace(',', '.'));
    }

    private static String fmt(Double d) {
        if (d == null) return "";
        if (d == Math.rint(d)) return String.valueOf(d.longValue());
        return String.valueOf(d);
    }

    private static String clean(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
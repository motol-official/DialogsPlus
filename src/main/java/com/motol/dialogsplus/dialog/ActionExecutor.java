package com.motol.dialogsplus.dialog;

import com.motol.dialogsplus.model.Menu;
import com.motol.dialogsplus.model.MenuAction;
import com.motol.dialogsplus.model.MenuComponent;
import com.motol.dialogsplus.storage.MenuManager;
import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionExecutor {

    private final MenuManager menuManager;

    public ActionExecutor(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public void execute(Player player, Menu menu, MenuComponent component, DialogResponseView view) {
        for (MenuAction action : component.getActions()) {
            switch (action.getType()) {
                case RUN_COMMAND ->
                        player.performCommand(substitute(action.getCommand(), menu, view));
                case RUN_COMMAND_AS_CONSOLE ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), substitute(action.getCommand(), menu, view));
                case CLOSE -> player.closeDialog();
                case SEND_MESSAGE ->
                        player.sendMessage(substitute(action.getMessage(), menu, view));
                case OPEN_MENU -> {
                    Menu target = menuManager.getMenu(action.getTarget());
                    if (target != null) {
                        player.showDialog(DialogBuilder.build(target, player, this::execute));
                    }
                }
                case OPEN_URL -> {
                    String url = substitute(action.getUrl(), menu, view);
                    player.sendMessage(Component.text("Click here to open the link")
                            .clickEvent(ClickEvent.openUrl(url)));
                }
                case SUGGEST_COMMAND -> {
                    String cmd = substitute(action.getCommand(), menu, view);
                    player.sendMessage(Component.text("Click here to run: " + cmd)
                            .clickEvent(ClickEvent.suggestCommand(cmd)));
                }
            }
        }
    }

    private String substitute(String template, Menu menu, DialogResponseView view) {
        if (template == null || view == null) {
            return template;
        }
        String result = template;
        for (MenuComponent input : menu.getComponents()) {
            String token = "{" + input.getId() + "}";
            if (!result.contains(token)) {
                continue;
            }
            String value = switch (input.getType()) {
                case TEXT_INPUT, SINGLE_OPTION_INPUT -> {
                    String text = view.getText("f_" + input.getId());
                    yield text != null ? text : "";
                }
                case BOOLEAN_INPUT -> {
                    Boolean b = view.getBoolean("f_" + input.getId());
                    yield b != null ? String.valueOf(b) : "";
                }
                case NUMBER_INPUT -> {
                    Float f = view.getFloat("f_" + input.getId());
                    yield f != null ? String.valueOf(f) : "";
                }
                default -> "";
            };
            result = result.replace(token, value);
        }
        return result;
    }
}
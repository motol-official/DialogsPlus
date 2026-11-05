package com.motol.dialogsplus.dialog;

import com.motol.dialogsplus.model.Menu;
import com.motol.dialogsplus.model.MenuComponent;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DialogBuilder {

    public static Dialog build(Menu menu, Player viewer, ActionRunner actionRunner) {
        List<DialogBody> bodyLines = new ArrayList<>();
        for (String line : menu.getBody()) {
            bodyLines.add(DialogBody.plainMessage(Component.text(PlaceholderSupport.apply(viewer, line))));
        }

        List<DialogInput> inputs = new ArrayList<>();
        List<ActionButton> buttons = new ArrayList<>();

        for (MenuComponent component : menu.getComponents()) {

            if (component.getPermission() != null
                    && !component.getPermission().isBlank()
                    && !viewer.hasPermission(component.getPermission())) {
                continue;
            }

            switch (component.getType()) {
                case TEXT_INPUT -> inputs.add(DialogInput.text(
                        "f_" + component.getId(),
                        Component.text(component.getLabel())
                ).build());

                case BOOLEAN_INPUT -> inputs.add(DialogInput.bool(
                        "f_" + component.getId(),
                        Component.text(component.getLabel())
                ).build());

                case SINGLE_OPTION_INPUT -> {
                    List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
                    for (String option : component.getOptions()) {
                        entries.add(SingleOptionDialogInput.OptionEntry.create(
                                option, Component.text(option), false
                        ));
                    }
                    inputs.add(DialogInput.singleOption(
                            "f_" + component.getId(),
                            Component.text(component.getLabel()),
                            entries
                    ).build());
                }

                case NUMBER_INPUT -> {
                    float min = component.getMin() != null ? component.getMin().floatValue() : 0f;
                    float max = component.getMax() != null ? component.getMax().floatValue() : 100f;
                    var builder = DialogInput.numberRange(
                            "f_" + component.getId(),
                            Component.text(component.getLabel()),
                            min,
                            max
                    );
                    if (component.getStep() != null) {
                        builder.step(component.getStep().floatValue());
                    }
                    if (component.getInitial() != null) {
                        builder.initial(component.getInitial().floatValue());
                    }
                    inputs.add(builder.build());
                }

                case BUTTON -> buttons.add(buildButton(menu, component, viewer, actionRunner));
            }
        }

        DialogBase.Builder baseBuilder = DialogBase.builder(Component.text(PlaceholderSupport.apply(viewer, menu.getTitle())))
                .canCloseWithEscape(menu.isCanCloseWithEscape())
                .body(bodyLines)
                .inputs(inputs);

        if (menu.getExternalTitle() != null) {
            baseBuilder.externalTitle(Component.text(PlaceholderSupport.apply(viewer, menu.getExternalTitle())));
        }

        return Dialog.create(builder -> builder
                .empty()
                .base(baseBuilder.build())
                .type(DialogType.multiAction(buttons).build())
        );
    }

    private static ActionButton buildButton(Menu menu, MenuComponent component, Player viewer, ActionRunner actionRunner) {
        String label = PlaceholderSupport.apply(viewer, component.getLabel());

        Component labelComponent = Component.text(label);
        Component iconComponent = IconParser.toComponent(component.getIcon(), viewer);
        if (iconComponent != null) {
            labelComponent = Component.empty()
                    .append(iconComponent)
                    .append(Component.space())
                    .append(labelComponent);
        }

        ActionButton.Builder buttonBuilder = ActionButton.builder(labelComponent);

        if (component.getTooltip() != null) {
            buttonBuilder.tooltip(Component.text(PlaceholderSupport.apply(viewer, component.getTooltip())));
        }

        if (component.getActions().size() == 1) {
            var single = component.getActions().get(0);
            if (single.getType() == com.motol.dialogsplus.model.MenuAction.Type.OPEN_URL && single.getUrl() != null) {
                buttonBuilder.action(DialogAction.staticAction(ClickEvent.openUrl(single.getUrl())));
                return buttonBuilder.build();
            }
            if (single.getType() == com.motol.dialogsplus.model.MenuAction.Type.SUGGEST_COMMAND && single.getCommand() != null) {
                buttonBuilder.action(DialogAction.staticAction(ClickEvent.suggestCommand(single.getCommand())));
                return buttonBuilder.build();
            }
        }

        buttonBuilder.action(DialogAction.customClick(
                (view, audience) -> {
                    if (audience instanceof Player player) {
                        actionRunner.run(player, menu, component, view);
                    }
                },
                ClickCallback.Options.builder().build()
        ));

        return buttonBuilder.build();
    }

    public interface ActionRunner {
        void run(Player player, Menu menu, MenuComponent component, io.papermc.paper.dialog.DialogResponseView view);
    }
}
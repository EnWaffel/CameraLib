package de.enwaffel.mc.camlib.plugin;

import de.enwaffel.mc.camlib.api.Animation;
import de.enwaffel.mc.camlib.api.CameraLibrary;
import de.enwaffel.mc.dlib.Display;
import de.enwaffel.mc.dlib.DisplayItem;
import de.enwaffel.mc.dlib.DisplayState;
import de.enwaffel.mc.dlib.FillStyle;
import de.enwaffel.mc.dlib.util.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class CamLibGUI {

    public static void openGUI(Player player) {
        Display.Builder builder = Display.newBuilder();

        DisplayState mainState = DisplayState.newState("main", "Camera Library | Menu", 27);
        mainState.addFillItem(Material.WHITE_STAINED_GLASS_PANE);

        mainState.addItem(DisplayItem.fromStack(
                new ItemBuilder()
                        .material(Material.COMMAND_BLOCK)
                        .displayName("§6§lCreate Animation")
                        .lore("§eLeft-Click to create a new animation")
                        .build(),
                11,
                "create_anim"
        ));

        mainState.addItem(DisplayItem.fromStack(
                ItemBuilder.newBuilder(Material.ENDER_PEARL)
                        .displayName("§a§lView Animations")
                        .lore("§eLeft-Click to view all animations")
                        .build(),
                13,
                "show_anims"
        ));

        mainState.addItem(DisplayItem.fromStack(
                new ItemBuilder()
                        .material(Material.BARRIER)
                        .displayName("§c?")
                        .lore("§c?")
                        .build(),
                15,
                "?"
        ));

        List<DisplayState> viewStates = new ArrayList<>();

        int _i = 0;
        for (int i = 0; i < (AnimationManager.CACHED_ANIMATIONS.size() / 28) + 1; i++) {
            DisplayState state = DisplayState.newState("view_anims" + i, "Camera Library | Page: #" + (i + 1), 54);
            state.setFillStyle(new FillStyle.Border());
            state.addFillItem(Material.WHITE_STAINED_GLASS_PANE);

            state.addItem(DisplayItem.fromStack(
                    ItemBuilder.newBuilder(Material.ARROW)
                            .displayName("§7Back")
                            .build(),
                    45,
                    "back"
            ));

            if (AnimationManager.CACHED_ANIMATIONS.size() / 28 > i) {
                state.addItem(DisplayItem.fromStack(
                        ItemBuilder.newBuilder(Material.ARROW)
                                .displayName("§7Next Page")
                                .build(),
                        53,
                        "next"
                ));
            }

            int slot = 10;
            for (int j = 0; j < 28; j++) {
                if (_i >= AnimationManager.CACHED_ANIMATIONS.size()) break;
                String name = AnimationManager.CACHED_ANIMATIONS.keySet().stream().toList().get(_i);
                AnimationData data = AnimationManager.CACHED_ANIMATIONS.values().stream().toList().get(_i);

                DisplayItem item = DisplayItem.fromStack(
                        ItemBuilder.newBuilder(Material.ENDER_EYE)
                                .displayName("§a" + name)
                                .lore("§eLeft-Click to play", "§cRight-Click to delete")
                                .build(),
                        slot,
                        "anim_" + name
                );

                slot++;
                if (slot == 17) {
                    slot = 19;
                }
                if (slot == 26) {
                    slot = 28;
                }
                if (slot == 35) {
                    slot = 37;
                }

                state.addItem(item);
                _i++;
            }

            viewStates.add(state);
        }


        builder.states(mainState);
        builder.states(viewStates.toArray(new DisplayState[]{}));
        Display display = builder.build();

        AtomicInteger currentPage = new AtomicInteger(0);

        display.setItemClickedCallback((ignored, displayState, displayItem, clickType) -> {
            switch (displayState.getId()) {
                case "main" -> {
                    switch (displayItem.getId()) {
                        case "create_anim" -> {
                            AnimationManager.setupAnimationCreation(player);
                            display.dump();
                        }
                        case "show_anims" -> {
                            display.changeState("view_anims0");
                        }
                    }
                }
            }
            if (displayState.getId().startsWith("view_anims")) {
                if (displayItem.getId().equals("back")) {
                    display.changeState("main");
                    currentPage.set(0);
                } else if (displayItem.getId().equals("next")) {
                    display.changeState("view_anims" + currentPage.incrementAndGet());
                } else if (displayItem.getId().startsWith("anim_")) {
                    String name = displayItem.getId().split("_")[1];
                    if (clickType.isRightClick()) {
                        AnimationManager.deleteAnimation(name);
                    } else if (clickType.isLeftClick()) {
                        AnimationData data = AnimationManager.CACHED_ANIMATIONS.get(name);
                        Animation animation = CameraLibrary.getInstance().newAnimation()
                                .player(player)
                                .start(data.start())
                                .end(data.end())
                                .points(data.points())
                                .time(data.ms())
                                .easing(data.easing())
                                .linear(false)
                                .build();
                        animation.play();
                    }
                    display.dump();
                }
            }
        });

        display.setDumpOnClose(true);
        display.show(player);
    }

}

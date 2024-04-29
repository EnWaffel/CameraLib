package de.enwaffel.mc.camlib.plugin;

import de.enwaffel.mc.camlib.api.tween.Easing;
import org.bukkit.Location;

import java.util.List;

public record AnimationData(Location start, Location end, List<Location> points, int ms, Easing easing) {
}

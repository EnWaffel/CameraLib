package de.enwaffel.mc.camlib.api;

import de.enwaffel.mc.camlib.CamLib;
import de.enwaffel.mc.camlib.impl.v1_20_R3.TimelineImpl;

public interface CameraLibrary {

    static CameraLibrary getInstance() {
        if (CamLib.getInstance() == null) {
            CamLib.init();
        }
        return CamLib.getInstance();
    }

    static void disable() {
        CamLib.disable();
    }

    TimelineImpl.Builder newTimeline();

    Animation.Builder newAnimation();

}

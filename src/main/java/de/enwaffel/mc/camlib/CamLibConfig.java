package de.enwaffel.mc.camlib;

import de.enwaffel.mc.camlib.api.Config;

import java.util.Timer;

public class CamLibConfig implements Config {

    public boolean blockPackets = true;
    public int updateRate = 1;

    @Override
    public void setBlockPackets(boolean blockPackets) {
        this.blockPackets = blockPackets;
    }

    @Override
    public void setUpdateRate(int updateRate) {
        this.updateRate = updateRate;
        CamLib.timer.cancel();
        CamLib.timer = new Timer();
        CamLib.startTimer();
    }

}

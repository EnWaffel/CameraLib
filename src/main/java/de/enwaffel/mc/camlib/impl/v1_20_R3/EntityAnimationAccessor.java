package de.enwaffel.mc.camlib.impl.v1_20_R3;

import aurelienribon.tweenengine.TweenAccessor;

public class EntityAnimationAccessor implements TweenAccessor<EntityAnimationImpl> {

    @Override
    public int getValues(EntityAnimationImpl target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case 0 -> {
                returnValues[0] = target.x;
                returnValues[1] = target.y;
                returnValues[2] = target.z;
                returnValues[3] = target.yaw;
                returnValues[4] = target.pitch;
                return 5;
            }
            default -> {
                return 0;
            }
        }
    }

    @Override
    public void setValues(EntityAnimationImpl target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case 0 -> {
                target.x = newValues[0];
                target.y = newValues[1];
                target.z = newValues[2];
                target.yaw = newValues[3];
                target.pitch = newValues[4];
            }
        }
    }

}

package de.enwaffel.mc.camlib.impl;

import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenEquations;
import de.enwaffel.mc.camlib.api.tween.Easing;

public final class Utils {

    public static TweenEquation translateEasing(Easing easing) {
        TweenEquation equation;
        try {
            equation = (TweenEquation) TweenEquations.class.getDeclaredField("ease" + easing.name()).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return TweenEquations.easeNone;
        }
        return equation;
    }

}

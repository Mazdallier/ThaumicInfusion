package drunkmafia.thaumicinfusion.common.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by DrunkMafia on 12/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@SuppressWarnings("ALL")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Effect {
    /**
     * Separate Aspects by using Comma (,)
     */
    String aspect();

    int cost();

    boolean hasGUI() default false;
}

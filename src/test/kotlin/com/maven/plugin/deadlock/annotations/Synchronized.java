package com.maven.plugin.deadlock.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/** Methodes waarvan de types deze annotatie hebben worden met een synchronized block uitgevoerd. */
public @interface Synchronized {
}

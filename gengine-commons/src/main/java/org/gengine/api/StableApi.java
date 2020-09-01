package org.gengine.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to denote a class or method as part
 * of the set of stable, unchanging elements.
 * <p>
 * When a class or method
 * is so designated it will not change within a minor release,
 * i.e. 1.1 -> 1.2, in a way that would make it no longer backwardly compatible
 * with an earlier version within the release.
 * <p>
 * Classes or methods with the annotation may still change in major releases,
 * i.e. 1.x -> 2.0.
 *
 */
@Target( {ElementType.TYPE,ElementType.METHOD} )
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StableApi
{
}

package dev.bodewig.java_based_migration.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Migration {

	/**
	 * The version to migrate from
	 */
	String fromVersion();

	/**
	 * The version to migrate to
	 */
	String toVersion();

	/**
	 * Use to execute migrations in a specific order
	 */
	int order() default -1;
}
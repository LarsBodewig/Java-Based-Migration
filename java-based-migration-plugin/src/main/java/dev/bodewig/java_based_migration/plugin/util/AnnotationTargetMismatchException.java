package dev.bodewig.java_based_migration.plugin.util;

import java.lang.annotation.Annotation;
import spoon.reflect.declaration.CtElement;

public class AnnotationTargetMismatchException extends RuntimeException {

	private static final long serialVersionUID = 2395039039130877208L;

	protected final Annotation annotation;
	protected final CtElement target;

	public AnnotationTargetMismatchException(String message, Annotation annotation, CtElement target) {
		super("Incorrect target " + target + " for annotation " + annotation + ": ");
		this.annotation = annotation;
		this.target = target;
	}
}

package dev.bodewig.java_based_migration.plugin.util;

import dev.bodewig.java_based_migration.core.Migration;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.logging.Log;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.support.JavaOutputProcessor;

public class Spoon {

	private final Log log;
	private final Launcher launcher;
	private final CtModel model;

	public Spoon(Log log, Collection<File> classFiles) {
		this.log = log;
		this.launcher = new Launcher();
		classFiles.forEach(classFile -> {
			this.log.debug("Reading " + classFile.getAbsolutePath());
			this.launcher.addInputResource(classFile.getAbsolutePath());
		});
		this.model = this.launcher.buildModel();
		this.log.info("Read " + getElements(this.model, CtType.class).count() + " types");
	}

	public Spoon(Log log, File mavenProjectDir) {
		this.log = log;
		this.log.debug("Reading maven project " + mavenProjectDir.getAbsolutePath());
		this.launcher = new MavenLauncher(mavenProjectDir.getAbsolutePath(), MavenLauncher.SOURCE_TYPE.APP_SOURCE);
		this.model = this.launcher.buildModel();
		this.log.info("Read " + getElements(this.model, CtType.class).count() + " types");
	}

	public void createMigrationMain(List<CtMethod<?>> migrations) {
		CtPackage basePkg = getBasePackage(this.model);
		Factory factory = this.launcher.getFactory();
		CtPackage migratorPkg = factory.createPackage(basePkg, "migrator");
		CtClass<?> migratorClass = factory.createClass(migratorPkg, "MigratorMain");
		CtMethod<?> mainMethod = factory.createMethod(migratorClass, Set.of(ModifierKind.PUBLIC, ModifierKind.STATIC),
				null, "main",
				List.of(factory.createParameter(null, factory.createCtTypeReference(String[].class), "args")),
				Collections.emptySet());
		CtBlock<?> mainBody = factory.createBlock();
		migrations.forEach(migration -> {
			CtTypeAccess<?> type = factory.createTypeAccess(migration.getDeclaringType().getReference());
			mainBody.addStatement(factory.createInvocation(type, migration.getReference()));
		});
		mainMethod.setBody(mainBody);
		System.out.println(migratorClass.prettyprint());
	}

	public List<CtMethod<?>> findMigrations(VersionRange versionRange) {
		List<CtMethod<?>> migrations = this.model.getElements(new AnnotationFilter<CtMethod<?>>(Migration.class));
		migrations.removeIf(migration -> {
			String from = migration.getAnnotation(Migration.class).fromVersion();
			String to = migration.getAnnotation(Migration.class).toVersion();
			ArtifactVersion fromVersion = new DefaultArtifactVersion(from);
			ArtifactVersion toVersion = new DefaultArtifactVersion(to);
			return !versionRange.containsVersion(fromVersion) || !versionRange.containsVersion(toVersion);
		});
		return migrations;
	}

	public static void validateMigrations(List<CtMethod<?>> migrations) {
		for (CtMethod<?> migration : migrations) {
			Migration annotation = migration.getAnnotation(Migration.class);
			if (!migration.isStatic()) {
				throw new AnnotationTargetMismatchException("Method is not static", annotation, migration);
			}
			if (!migration.isPublic()) {
				throw new AnnotationTargetMismatchException("Method is not public", annotation, migration);
			}
			if (!migration.getFormalCtTypeParameters().isEmpty()) {
				throw new AnnotationTargetMismatchException("Method requires generic type parameters", annotation,
						migration);
			}
			if (!migration.getParameters().isEmpty()) {
				throw new AnnotationTargetMismatchException("Method requires parameters", annotation, migration);
			}
			if (migration.getType() != null) {
				throw new AnnotationTargetMismatchException("Method has return value", annotation, migration);
			}
		}
	}

	public static void sortMigrations(List<CtMethod<?>> migrations) {
		migrations.sort((leftMigration, rightMigration) -> {
			Migration left = leftMigration.getAnnotation(Migration.class);
			Migration right = rightMigration.getAnnotation(Migration.class);
			// migrations operating on earlier fromVersion
			ComparableVersion leftFromVersion = new ComparableVersion(left.fromVersion());
			ComparableVersion rightFromVersion = new ComparableVersion(right.fromVersion());
			int compareFromVersion = leftFromVersion.compareTo(rightFromVersion);
			if (compareFromVersion != 0) {
				return compareFromVersion;
			}
			// migrations operating on same fromVersion and earlier toVersion
			ComparableVersion leftToVersion = new ComparableVersion(left.toVersion());
			ComparableVersion rightToVersion = new ComparableVersion(right.toVersion());
			int compareToVersion = leftToVersion.compareTo(rightToVersion);
			if (compareToVersion != 0) {
				return compareToVersion;
			}
			// migrations operating on same fromVersion and toVersion order
			return Integer.compare(left.order(), right.order());
		});
	}

	public void rewritePackages(String name) {
		CtPackage basePkg = getBasePackage(this.model);
		this.log.debug("Identified base package " + basePkg.getQualifiedName());
		CtPackage newPkg = basePkg.getFactory().createPackage().setSimpleName(name);
		List<Pair<CtTypeReference<?>, CtType<?>>> refs = getInternalReferences(basePkg);
		movePackageContents(basePkg, newPkg);
		basePkg.addPackage(newPkg);
		this.log.debug("Created new base package " + newPkg.getQualifiedName());
		basePkg.updateAllParentsBelow();
		updateInternalReferences(refs);
		this.log.debug("Rewrote package internal references");
	}

	public void writeClassModel(File outDir) {
		Environment env = this.launcher.getFactory().getEnvironment();
		env.setSourceOutputDirectory(outDir);
		env.setAutoImports(true);
		JavaOutputProcessor writer = this.launcher.createOutputWriter();
		getElements(this.model, CtType.class).forEach(type -> {
			this.log.debug("Writing type " + type.getQualifiedName());
			writer.createJavaFile(type);
		});
		this.log.info("Wrote " + writer.getCreatedFiles().size() + " frozen files");
	}

	private static CtPackage getBasePackage(CtModel model) {
		Iterator<CtPackage> itr = model.getAllPackages().iterator();
		CtPackage basePackage = model.getRootPackage();
		CtPackage next = itr.next();
		while (!next.hasTypes() && itr.hasNext()) {
			basePackage = next;
			next = itr.next();
		}
		if (basePackage.isUnnamedPackage()) {
			basePackage = next;
		}
		return basePackage;
	}

	private static void movePackageContents(CtPackage from, CtPackage to) {
		from.getPackages().forEach(pkg -> {
			pkg.delete();
			to.addPackage(pkg);
		});
		from.getTypes().forEach(type -> {
			type.delete();
			to.addType(type);
		});
	}

	private static List<Pair<CtTypeReference<?>, CtType<?>>> getInternalReferences(CtPackage pkg) {
		List<Pair<CtTypeReference<?>, CtType<?>>> referencedTypes = getElements(pkg, CtTypeReference.class)
				.filter(ref -> ref.getTypeDeclaration().hasParent(pkg))
				.map(ref -> new Pair<CtTypeReference<?>, CtType<?>>(ref, ref.getTypeDeclaration())).toList();
		return referencedTypes;
	}

	@SuppressWarnings("unchecked")
	private static <T> Stream<T> getElements(CtQueryable base, Class<T> type) {
		return base.filterChildren(e -> type.isAssignableFrom(e.getClass())).list().stream().map(e -> (T) e);
	}

	private static void updateInternalReferences(List<Pair<CtTypeReference<?>, CtType<?>>> referencedTypes) {
		referencedTypes.forEach(entry -> entry.left().replace(entry.right().getReference()));
	}
}

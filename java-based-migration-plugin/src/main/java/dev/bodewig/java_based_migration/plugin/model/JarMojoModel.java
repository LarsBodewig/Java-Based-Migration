package dev.bodewig.java_based_migration.plugin.model;

import java.io.File;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

public abstract class JarMojoModel extends AbstractMojo {

	@Parameter
	protected MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

	/** The classifier for the packed jar */
	@Parameter(defaultValue = "migration")
	protected String classifier;

	/**
	 * List of fully qualified class names to exclude in the jar, useful for classes
	 * Spoon does identify but are not actually used
	 */
	@Parameter
	private String[] excludeClasses;

	/** The filename to be used for the generated archive file. */
	@Parameter(defaultValue = "${project.build.finalName}")
	protected String finalName;

	/** Directory of the frozen persistence files */
	@Parameter(defaultValue = "${project.basedir}/src/migration/")
	protected File frozenDir;

	/**
	 * List of fully qualified class names to include in the jar, useful for classes
	 * Spoon does not identify but are used, e.g. loaded via ClassLoader
	 */
	@Parameter
	private String[] includeClasses;

	@Component(role = Archiver.class, hint = "jar")
	protected JarArchiver jarArchiver;

	/** The directory where the generated archive file will be put. */
	@Parameter(defaultValue = "${project.build.directory}")
	protected File outputDirectory;

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	/** Frozen versions to include in the default maven version range format */
	@Parameter(required = true)
	protected String versionRange;
}

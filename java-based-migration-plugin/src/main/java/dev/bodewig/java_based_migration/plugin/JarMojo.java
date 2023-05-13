package dev.bodewig.java_based_migration.plugin;

import dev.bodewig.java_based_migration.plugin.model.JarMojoModel;
import dev.bodewig.java_based_migration.plugin.util.Spoon;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.archiver.jar.ManifestException;
import spoon.reflect.declaration.CtMethod;

@Mojo(name = "jar", defaultPhase = LifecyclePhase.PACKAGE)
@Execute(phase = LifecyclePhase.VALIDATE)
public class JarMojo extends JarMojoModel {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = this.getLog();
		Spoon spoon = new Spoon(log, this.project.getBasedir());
		List<CtMethod<?>> migrations = spoon.findMigrations(this.getVersionRange());
		Spoon.validateMigrations(migrations);
		Spoon.sortMigrations(migrations);
		spoon.createMigrationMain(migrations);
		// Thaw persisted classes in version range
		// Use spoon to identify shadow classes (referenced from the migration
		// functions) and load them on the classpath
		// Compile classes using spoon
		// Build jar in separate build run (parameter on mojo annotation) with
		// classifier
	}

	protected VersionRange getVersionRange() throws MojoExecutionException {
		try {
			return VersionRange.createFromVersionSpec(this.versionRange);
		} catch (InvalidVersionSpecificationException e) {
			throw new MojoExecutionException("Invalid versionRange: " + this.versionRange, e);
		}
	}

	protected File getJarFile() {
		if (this.outputDirectory == null) {
			throw new IllegalArgumentException("outputDirectory is not allowed to be null");
		}
		if (this.finalName == null) {
			throw new IllegalArgumentException("finalName is not allowed to be null");
		}

		String fileName = this.finalName + (this.classifier.isEmpty() ? "-" + this.classifier : "") + ".jar";

		return new File(this.outputDirectory, fileName);
	}

	protected void createArchive() throws ManifestException, IOException, DependencyResolutionRequiredException {
		File jarFile = this.getJarFile();
		MavenArchiver archiver = new MavenArchiver();
		archiver.setArchiver(this.jarArchiver);
		archiver.setOutputFile(jarFile);
		archiver.createArchive(this.session, this.project, this.archive);
	}
}

package dev.bodewig.java_based_migration.plugin;

import dev.bodewig.java_based_migration.plugin.model.JarMojoModel;
import java.io.File;
import java.io.IOException;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.archiver.jar.ManifestException;

@Mojo(name = "jar", defaultPhase = LifecyclePhase.PACKAGE)
@Execute(phase = LifecyclePhase.VALIDATE)
public class JarMojo extends JarMojoModel {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Build main-function calling migration functions
		// Thaw persisted classes in version range
		// Use spoon to identify shadow classes (referenced from the migration
		// functions) and load them on the classpath
		// Compile classes using spoon
		// Build jar in separate build run (parameter on mojo annotation) with
		// classifier
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

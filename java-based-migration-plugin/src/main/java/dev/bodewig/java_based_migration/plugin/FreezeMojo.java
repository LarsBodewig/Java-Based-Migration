package dev.bodewig.java_based_migration.plugin;

import dev.bodewig.java_based_migration.plugin.model.FreezeMojoModel;
import dev.bodewig.java_based_migration.plugin.util.Spoon;
import dev.bodewig.java_based_migration.plugin.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Offers functionality to freeze the configured persistence classes to create
 * Java based migration functions between different versions of a persistence
 * layer.
 */
@Mojo(name = "freeze", defaultPhase = LifecyclePhase.DEPLOY)
public class FreezeMojo extends FreezeMojoModel {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = this.getLog();
		List<File> classesFiles = this.getPersistenceClasses();
		log.info("Configured " + classesFiles.size() + " classes to freeze");
		List<File> resourcesFiles = this.getPersistenceResources();
		log.info("Configured " + resourcesFiles.size() + " resources to freeze");
		String version = this.getFreezeVersion();
		log.info("Freezing version " + version);
		Spoon spoon = new Spoon(log, classesFiles);
		log.info("Rewriting packages");
		String normalizedVersion = normalizedFreezeVersion(version);
		spoon.rewritePackages(normalizedVersion);
		log.info("Writing frozen class files");
		File javaOutputDir = this.getJavaOuputDir(version);
		log.info("Configured java output directory " + javaOutputDir.getAbsolutePath());
		spoon.writeClassModel(javaOutputDir);
		log.info("Writing frozen resources");
		File resourcesOutputDir = this.getResourcesOutputDir(version);
		log.info("Configured resources output directory " + resourcesOutputDir.getAbsolutePath());
		freezeResources(log, resourcesFiles, resourcesOutputDir);
	}

	protected String getFreezeVersion() throws MojoExecutionException {
		if (this.freezeVersion.isBlank()) {
			throw new MojoExecutionException("Missing configuration for freeze version");
		}
		return this.freezeVersion;
	}

	protected File getJavaOuputDir(String version) {
		File javaOutputDir = new File(this.frozenDir, version + "/java/");
		javaOutputDir.mkdirs();
		return javaOutputDir;
	}

	protected File getResourcesOutputDir(String version) {
		File resourcesOutputDir = new File(this.frozenDir, version + "/resources/");
		resourcesOutputDir.mkdirs();
		return resourcesOutputDir;
	}

	protected List<File> getPersistenceClasses() throws MojoExecutionException {
		List<File> persistenceClasses = new ArrayList<>();
		boolean anySet = false;
		if (this.persistenceClassesFileList != null) {
			anySet |= persistenceClasses.addAll(
					Utils.flatMapList(this.persistenceClassesFileList, f -> FileUtils.listFiles(f, null, true)));
		}
		if (this.persistenceClassesGlobList != null) {
			anySet |= persistenceClasses
					.addAll(Utils.flatMapList(this.persistenceClassesGlobList, g -> g.listMatches()));
		}
		if (!anySet) {
			throw new MojoExecutionException("Missing configuration for persistence classes: atleast one of "
					+ "[persistenceClassesFileList, persistenceClassesGlobList] " + "has to be set");
		}
		return persistenceClasses;
	}

	protected List<File> getPersistenceResources() {
		List<File> persistenceResources = new ArrayList<>();
		if (this.persistenceResourcesFileList != null) {
			persistenceResources.addAll(
					Utils.flatMapList(this.persistenceResourcesFileList, f -> FileUtils.listFiles(f, null, true)));
		}
		if (this.persistenceResourcesGlobList != null) {
			persistenceResources.addAll(Utils.flatMapList(this.persistenceResourcesGlobList, g -> g.listMatches()));
		}
		return persistenceResources;
	}

	protected static void freezeResources(Log log, List<File> from, File to) throws MojoExecutionException {
		try {
			for (File file : from) {
				log.debug("Copying resource " + file.getAbsolutePath());
				if (file.isDirectory()) {
					FileUtils.copyDirectory(file, to);
				} else {
					FileUtils.copyFileToDirectory(file, to);
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to copy resource file", e);
		}
	}

	protected static String normalizedFreezeVersion(String version) throws MojoExecutionException {
		return Utils.toJavaIdentifier(version);
	}
}

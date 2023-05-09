package dev.bodewig.java_based_migration.plugin.model;

import dev.bodewig.java_based_migration.plugin.Glob;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class FreezeMojoModel extends AbstractMojo {

	/**
	 * The version used to store frozen files and to calculate the base package name
	 */
	@Parameter(defaultValue = "${project.version}")
	protected String freezeVersion;

	/** Output directory for the frozen persistence files */
	@Parameter(defaultValue = "${project.basedir}/src/migration/")
	protected File frozenDir;

	/**
	 * Define persistence classes to freeze (can be used in conjunction with {@code
	 * persistenceClassesGlobList})
	 *
	 * <pre>
	 * &lt;persistenceClassesFileList&gt;
	 * 	&lt;file&gt;src/main/java/my/db/classes/Person.java&lt;/file&gt;
	 * 	&lt;file&gt;src/main/java/my/db/classes/pkg&lt;/file&gt;
	 * 	...
	 * &lt;/persistenceClassesFileList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<File> persistenceClassesFileList;

	/**
	 * Define persistence classes to freeze (can be used in conjunction with {@code
	 * persistenceClassesFileList})
	 *
	 * <pre>
	 * &lt;persistenceClassesGlobList&gt;
	 * 	&lt;glob&gt;path/relative/to/./src/main/java/my/db/classes/C*.java&lt;/glob&gt;
	 * 	...
	 * 	&lt;glob&gt;
	 * 		&lt;basePath&gt;${project.build.sourceDirectory}&lt;/basePath&gt;
	 * 		&lt;pattern&gt;Bi*.java&lt;/pattern&gt;
	 * 	&lt;/glob&gt;
	 * &lt;/persistenceClassesGlobList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<Glob> persistenceClassesGlobList;

	/**
	 * Define resources to freeze (can be used in conjunction with {@code
	 * persistenceResourcesGlobList})
	 *
	 * <pre>
	 * &lt;persistenceResourcesFileList&gt;
	 * 	&lt;file&gt;src/main/resources/logging.xml&lt;/file&gt;
	 * 	&lt;file&gt;src/main/resources/config&lt;/file&gt;
	 * 	...
	 * &lt;/persistenceResourcesFileList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<File> persistenceResourcesFileList;

	/**
	 * Define resources to freeze (can be used in conjunction with {@code
	 * persistenceResourcesFileList})
	 *
	 * <pre>
	 * &lt;persistenceResourcesGlobList&gt;
	 * 	&lt;glob&gt;path/relative/to/./src/main/resources/*.xml_bak&lt;/glob&gt;
	 * 	...
	 * 	&lt;glob&gt;
	 * 		&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;
	 * 		&lt;pattern&gt;*.conf&lt;/pattern&gt;
	 * 	&lt;/glob&gt;
	 * &lt;/persistenceResourcesGlobList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<Glob> persistenceResourcesGlobList;
}

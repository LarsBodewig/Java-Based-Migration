# Java-Based-Migration

The Java Based Migration Maven Plugin is a simple tool to manage migrations of JPA-generated classes or other files between application versions. It offers goals to *freeze* files in a specific version and *thaw* them in a later version to migrate persisted data using Java. This allows more flexibility than simple database migration tools using SQL scripts.

## Goals

* **Freeze (defaultPhase=Deploy):**

  Copy a set of files to a separate project directory, resolve the common package and place them in a versioned, named package.

  | Configuration option | Description | Default/Required | Example |
  | --- | --- | --- | --- |
  | freezeVersion | The version used to store frozen files and to calculate the base package name | `${project.version}` | `1.0.0` |
  | frozenDir | Output directory for the frozen persistence files | `${project.basedir}/src/migration/` | `${project.basedir}/migration/` |
  | persistenceClassesFileList | Define persistence classes to freeze (can be used in conjunction with persistenceClassesGlobList) | Requires one of persistenceClassesFileList, persistenceClassesGlobList | &lt;persistenceClassesFileList&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/java/my/db/classes/Person.java&lt;/file&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/java/my/db/classes/pkg&lt;/file&gt;<br>&nbsp;&nbsp;...<br>&lt;/persistenceClassesFileList&gt; |
  | persistenceClassesGlobList | Define persistence classes to freeze (can be used in conjunction with persistenceClassesFileList) | Requires one of persistenceClassesFileList, persistenceClassesGlobList | &lt;persistenceClassesGlobList&gt;<br>&nbsp;&nbsp;&lt;glob&gt;path/relative/to/./src/main/java/my/db/classes/C\*.java&lt;/glob&gt;<br>&nbsp;&nbsp;...<br>&nbsp;&nbsp;&lt;glob&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;basePath&gt;${project.build.sourceDirectory}&lt;/basePath&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;pattern&gt;Bi\*.java&lt;/pattern&gt;<br>&nbsp;&nbsp;&lt;/glob&gt;<br>&lt;/persistenceClassesGlobList&gt; |
  | persistenceResourcesFileList | Define resources to freeze (can be used in conjunction with persistenceResourcesGlobList) | Requires one of persistenceResourcesFileList, persistenceResourcesGlobList | &lt;persistenceResourcesFileList&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/resources/logging.xml&lt;/file&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/resources/config&lt;/file&gt;<br>&nbsp;&nbsp;...<br>&lt;/persistenceResourcesFileList&gt; |
  | persistenceResourcesGlobList | Define resources to freeze (can be used in conjunction with persistenceResourcesFileList) | Requires one of persistenceResourcesFileList, persistenceResourcesGlobList | &lt;persistenceResourcesGlobList&gt;<br>&nbsp;&nbsp;&lt;glob&gt;path/relative/to/./src/main/resources/\*.xml_bak&lt;/glob&gt;<br>&nbsp;&nbsp;...<br>&nbsp;&nbsp;&lt;glob&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;pattern&gt;\*.conf&lt;/pattern&gt;<br>&nbsp;&nbsp;&lt;/glob&gt;<br>&lt;/persistenceResourcesGlobList&gt; |

* **Thaw (defaultPhase=Initialize):**

  Target a range of versioned frozen files to include them for compilation.

  | Configuration option | Description | Default/Required | Example |
  | --- | --- | --- | --- |
  | frozenDir | Directory of the frozen persistence files | `${project.basedir}/src/migration/` | `${project.basedir}/migration/` |
  | versionRange | Frozen versions to include in the default maven version range format | required | `[1.0.0,2.0.0]` |

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.

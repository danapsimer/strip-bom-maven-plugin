# Introduction #

This plugin is configured in the Maven POM file just as any other Maven Plugin.  Please see http://maven.apache.org for documentation on the Maven 2 build tool.  The Plugin's Site is available here:  http://wiki.strip-bom-maven-plugin.googlecode.com/hg/site/snapshot/index.html.


# Details #

Since this plugin has not been released to the central repository, you will need to use the project repo which is hosted at sonatype here: https://oss.sonatype.org/content/repositories/snapshots.  Once this plugin is released it will be available in the central maven repo since we will be deploying to the sonatype release repository and promoting the artifacts for sync with central.

If you are going to use a SNAPSHOT, Add the following to your POM:

```
  <pluginRepositories>
    ...
    <pluginRepository>
      <id>strip-bom-repo</id>
      <name>Strip BOM Plugin Repository</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <layout>default</layout>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
      <releases>
        <enabled>false</enabled>
      </releases>
    </pluginRepository>
    ...
  </pluginRepositories>  
```

To configure the plugin you add the following to your POM:

```
  <build>
    ...
    <plugins>
      ...
      <plugin>
        <groupId>com.dhptech.maven</groupId>
        <artifactId>strip-bom-plugin</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <configuration>
          <files>
            <fileSet>
              <directory>${basedir}/src/main/java</directory>
              <includes>
                <include>**/*.java</include>
              </includes>
            </fileSet>
            ...
          </files>
        </configuration>
        <executions>
          <execution>
            <goals><goal>strip-bom</goal></goals>
          </execution>
        </executions>
      </plugin>
      ...
    </pluings>
    ...
  </build>
```
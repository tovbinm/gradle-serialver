/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package com.darylteo.gradle.javassist.tasks;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javassist.build.IClassTransformer;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import com.darylteo.gradle.javassist.transformers.GroovyClassTransformation;

import groovy.lang.Closure;


/**
 * Closely based on {@link TransformationTask}.
 */
public class IncrementalTransformationTask extends DefaultTask {
    private Object destinationDir;

    private Object classesDir;

    private IClassTransformer transformation;

    private FileCollection classpath;

    public IncrementalTransformationTask() {
        // empty classpath
        this.classpath = this.getProject().files();

        this.destinationDir = Paths.get(this.getProject().getBuildDir().toString(), "transformations", this.getName())
                                   .toFile();
    }

    @OutputDirectory
    public File getDestinationDir() {
        return this.getProject().file(destinationDir);
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    public IClassTransformer getTransformation() {
        return transformation;
    }

    public void setTransformation(IClassTransformer transformation) {
        this.transformation = transformation;
    }

    @InputFiles
    public FileCollection getClasspath() {
        return this.classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @InputFiles
    public FileCollection getSources() {
        if (this.classesDir == null) {
            return this.getProject().files();
        }

        ConfigurableFileTree result = this.getProject().fileTree(this.classesDir);
        result.include("**/*.class");

        return result;
    }

    public void transform(Closure closure) {
        this.transformation = new GroovyClassTransformation(closure);
    }

    public void where(Closure closure) {
        this.transformation = new GroovyClassTransformation(null, closure);
    }

    public void from(Object dir) {
        this.classesDir = dir;
    }

    public void into(Object dir) {
        this.destinationDir = dir;
    }

    public void eachFile(Closure closure) {
        closure.call(this.getSources().getFiles());
    }

    protected void exec(IncrementalTaskInputs inputs) {

        Set<File> sourceFiles = getSources().getFiles();

        List<File> outOfDate = new LinkedList<>();
        inputs.outOfDate(inputFileDetails -> {
            if (sourceFiles.contains(inputFileDetails.getFile())) {
                outOfDate.add(inputFileDetails.getFile());
            }
        });

        Collection<File> classPath = this.classpath.getFiles();
        if (classesDir != null) {
            classPath.add(this.getProject().file(classesDir));
        }

        if (outOfDate.isEmpty()) {
            this.setDidWork(false);
        } else {
            boolean workDone = new TransformationAction(this.getDestinationDir(),
                                                        outOfDate,
                                                        classPath,
                                                        this.transformation).execute();

            this.setDidWork(workDone);
        }
    }

}

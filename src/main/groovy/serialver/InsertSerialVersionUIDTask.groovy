package serialver

import com.darylteo.gradle.javassist.tasks.IncrementalTransformationTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

public class InsertSerialVersionUIDTask extends IncrementalTransformationTask {

    @Input
    def serialver

    @Input
    def overwrite = true

    @Input
    def forceUIDOnException = false

    @Input
    def copyAll = true

    InsertSerialVersionUIDTask() {
        dependsOn(project.classes)
        from(project.sourceSets.main.output.classesDir)
        classpath += project.configurations.compile
        project.jar.mustRunAfter(this)
    }

    @TaskAction
    public void exec(IncrementalTaskInputs inputs) {
        if (!inputs.incremental) {
            project.delete(getDestinationDir().listFiles())
        }

        def serialVerAsLong
        if (serialver instanceof String) {
            serialVerAsLong = Long.parseLong(serialver.replaceAll('L', '').replaceAll('l', ''))
        } else {
            serialVerAsLong = serialver
        }

        setTransformation(new SerialVersionUIDTransformer(serialVerAsLong, overwrite, forceUIDOnException, copyAll))

        super.exec(inputs)
    }

}

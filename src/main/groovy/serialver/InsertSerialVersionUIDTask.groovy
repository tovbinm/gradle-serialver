package serialver

import com.darylteo.gradle.javassist.tasks.IncrementalTransformationTask
import com.darylteo.gradle.javassist.tasks.TransformationAction
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

public class InsertSerialVersionUIDTask extends IncrementalTransformationTask {

    def serialver
    def overwrite = true
    def forceUIDOnException = false

    InsertSerialVersionUIDTask() {
        dependsOn(project.classes)
        project.jar.mustRunAfter(this)
    }

    @TaskAction
    public void exec(IncrementalTaskInputs inputs) {
        if (!inputs.incremental) {
            project.delete(getDestinationDir().listFiles())
        }

        classpath += project.configurations.compile
        def serialVerAsLong
        if (serialver instanceof String) {
            serialVerAsLong = Long.parseLong(serialver.replaceAll('L', '').replaceAll('l', ''))
        } else {
            serialVerAsLong = serialver
        }

        setTransformation(new SerialVersionUIDTransformer(serialVerAsLong, overwrite, forceUIDOnException))

        // in place transformation
        from(project.sourceSets.main.output[0])
        //into(project.sourceSets.main.output[0])
        // ^ let users configure where to copy serialver classes into

        super.exec(inputs)
    }

}

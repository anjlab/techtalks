package com.anjlab.gradle.docker

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import org.gradle.api.tasks.OutputFile

class CachingDockerBuildImage extends DockerBuildImage {
    CachingDockerBuildImage() {
        doLast({
            getCachedImageId().write(getImageId())
        })
    }

    @Override
    String getImageId() {
        return super.getImageId() ?: getCachedImageId()?.text
    }

    @OutputFile
    File getCachedImageId() {
        return new File("${project.buildDir}/docker-cache/${name}")
    }
}
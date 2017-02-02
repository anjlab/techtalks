package com.anjlab.gradle.git

import groovy.transform.ToString

class WorkingTree {

    @ToString
    static class Status {
        Boolean clean
        String commit
        String branch
    }

    static Status status() {
        return new Status(
                clean: exec('git', 'status', '--porcelain')?.empty,
                commit: exec('git', 'rev-parse', '--short', 'HEAD')?.trim(),
                branch: exec('git', 'rev-parse', '--abbrev-ref', 'HEAD')?.trim()
        )
    }

    private static String exec(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(false)
                    .start()

            return process.waitFor() == 0 ? process.getInputStream().text : null
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

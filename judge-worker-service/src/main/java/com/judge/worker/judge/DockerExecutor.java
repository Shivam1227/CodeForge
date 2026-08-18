package com.judge.worker.judge;

import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DockerExecutor {

    private static final int COMPILE_TIMEOUT_SECONDS = 10;

    /** Runs the compile step once, with its own fixed timeout — never tied to the problem's time limit. */
    public ExecutionResult compile(String dockerImage, String hostWorkDir, String compileCommand, int memoryLimitMb) throws Exception {
        List<String> cmd = baseDockerFlags(dockerImage, hostWorkDir, memoryLimitMb);
        cmd.add("timeout");
        cmd.add(String.valueOf(COMPILE_TIMEOUT_SECONDS));
        cmd.add("sh");
        cmd.add("-c");
        cmd.add(compileCommand);

        System.out.println("DOCKER COMMAND: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();
        process.getOutputStream().close(); // no stdin needed for compilation

        boolean finishedInTime = process.waitFor(COMPILE_TIMEOUT_SECONDS + 5L, TimeUnit.SECONDS);
        if (!finishedInTime) {
            process.destroyForcibly();
            return new ExecutionResult("", "compilation timed out", -1, true);
        }

        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());
        return new ExecutionResult(stdout, stderr, process.exitValue(), false);
    }

    /** Runs the already-compiled program against one test case. Time limit applies only here. */
    public ExecutionResult run(String dockerImage, String hostWorkDir, String runCommand,
                               int timeLimitSeconds, int memoryLimitMb, String stdinInput) throws Exception {
        List<String> cmd = baseDockerFlags(dockerImage, hostWorkDir, memoryLimitMb);
        cmd.add("timeout");
        cmd.add(String.valueOf(timeLimitSeconds));
        cmd.add("sh");
        cmd.add("-c");
        cmd.add(runCommand);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();

        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(stdinInput.getBytes());
            stdin.flush();
        }

        boolean finishedInTime = process.waitFor(timeLimitSeconds + 5L, TimeUnit.SECONDS);
        if (!finishedInTime) {
            process.destroyForcibly();
            return new ExecutionResult("", "killed: exceeded wall-clock limit", -1, true);
        }

        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());
        return new ExecutionResult(stdout, stderr, process.exitValue(), false);
    }

    private List<String> baseDockerFlags(String dockerImage, String hostWorkDir, int memoryLimitMb) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-i");
        cmd.add("--memory=" + memoryLimitMb + "m");
        cmd.add("--cpus=0.5");
        cmd.add("--pids-limit=64");
        cmd.add("--network=none");
        cmd.add("--read-only");
        cmd.add("-v");
        cmd.add(hostWorkDir + ":/box");
        cmd.add("-w");
        cmd.add("/box");
        cmd.add(dockerImage);
        return cmd;
    }
}
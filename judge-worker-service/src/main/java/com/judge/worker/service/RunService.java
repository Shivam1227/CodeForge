package com.judge.worker.service;

import com.judge.worker.dto.RunEvent;
import com.judge.worker.dto.TestCaseResultEvent;
import com.judge.worker.entity.Problem;
import com.judge.worker.entity.TestCase;
import com.judge.worker.judge.DockerExecutor;
import com.judge.worker.judge.ExecutionResult;
import com.judge.worker.judge.LanguageConfig;
import com.judge.worker.kafka.RunResultProducer;
import com.judge.worker.repository.ProblemRepository;
import com.judge.worker.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RunService {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final DockerExecutor dockerExecutor;
    private final RunResultProducer runResultProducer;

    public void run(RunEvent event) {
        Path workDir = null;

        try {
            Problem problem = problemRepository.findById(event.getProblemId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Problem not found: " + event.getProblemId()
                            )
                    );

            List<TestCase> samples =
                    testCaseRepository.findByProblemId(event.getProblemId())
                            .stream()
                            .filter(TestCase::isSample)
                            .toList();

            LanguageConfig config =
                    LanguageConfig.forLanguage(event.getLanguage());

            int timeLimitSeconds =
                    Math.max(1, problem.getTimeLimitMs() / 1000);

            workDir = Files.createDirectories(
                    Path.of("/tmp/judge/run-" + event.getRunId())
            );

            try (FileWriter writer = new FileWriter(
                    new File(workDir.toFile(), config.getFileName()))) {

                writer.write(event.getSourceCode());
            }

            String hostWorkDirRoot = System.getenv("HOST_WORK_DIR");

            if (hostWorkDirRoot == null || hostWorkDirRoot.isBlank()) {
                throw new IllegalStateException(
                        "HOST_WORK_DIR environment variable is not configured"
                );
            }

            String hostWorkDir = Path.of(
                    hostWorkDirRoot,
                    "run-" + String.valueOf(event.getRunId())
            ).toString();

            if (config.getCompileCommand() != null) {

                ExecutionResult compileResult = dockerExecutor.compile(
                        config.getDockerImage(),
                        hostWorkDir,
                        config.getCompileCommand(),
                        problem.getMemoryLimitMb()
                );

                if (compileResult.exitCode() != 0) {

                    runResultProducer.publish(
                            event.getRunId(),
                            "COMPILE_ERROR",
                            truncate(compileResult.stderr()),
                            List.of()
                    );

                    return;
                }
            }

            List<TestCaseResultEvent> results = new ArrayList<>();

            for (TestCase tc : samples) {

                ExecutionResult result = dockerExecutor.run(
                        config.getDockerImage(),
                        hostWorkDir,
                        config.getRunCommand(),
                        timeLimitSeconds,
                        problem.getMemoryLimitMb(),
                        tc.getInput()
                );

                String status;

                if (result.processTimedOut() || result.exitCode() == 124) {
                    status = "TIME_LIMIT_EXCEEDED";

                } else if (result.exitCode() == 137) {
                    status = "MEMORY_LIMIT_EXCEEDED";

                } else if (result.exitCode() != 0) {
                    status = "RUNTIME_ERROR";

                } else {
                    status = normalize(result.stdout())
                            .equals(normalize(tc.getExpectedOutput()))
                            ? "PASSED"
                            : "FAILED";
                }

                results.add(
                        new TestCaseResultEvent(
                                tc.getInput(),
                                tc.getExpectedOutput(),
                                result.stdout(),
                                status
                        )
                );
            }

            runResultProducer.publish(
                    event.getRunId(),
                    "COMPLETED",
                    null,
                    results
            );

        } catch (Exception e) {

            runResultProducer.publish(
                    event.getRunId(),
                    "RUNTIME_ERROR",
                    "Internal judge error",
                    List.of()
            );

        } finally {

            if (workDir != null) {
                deleteDirectory(workDir);
            }
        }
    }

    private String truncate(String s) {

        if (s == null) {
            return null;
        }

        s = s.trim();

        return s.length() > MAX_ERROR_LENGTH
                ? s.substring(0, MAX_ERROR_LENGTH) + "\n...(truncated)"
                : s;
    }

    private String normalize(String output) {

        return output == null
                ? ""
                : output.trim().replaceAll("\\r\\n", "\n");
    }

    private void deleteDirectory(Path dir) {

        try (Stream<Path> walk = Files.walk(dir)) {

            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }
    }
}
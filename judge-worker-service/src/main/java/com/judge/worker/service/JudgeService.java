package com.judge.worker.service;

import com.judge.worker.dto.SubmissionEvent;
import com.judge.worker.entity.Problem;
import com.judge.worker.entity.SubmissionStatus;
import com.judge.worker.entity.TestCase;
import com.judge.worker.judge.DockerExecutor;
import com.judge.worker.judge.ExecutionResult;
import com.judge.worker.judge.LanguageConfig;
import com.judge.worker.kafka.ResultProducer;
import com.judge.worker.repository.ProblemRepository;
import com.judge.worker.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class JudgeService {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final DockerExecutor dockerExecutor;
    private final ResultProducer resultProducer;

    public void judge(SubmissionEvent event) {
        Path workDir = null;

        try {
            resultProducer.publish(
                    event.getSubmissionId(),
                    SubmissionStatus.RUNNING.name(),
                    null,
                    null,
                    null
            );

            Problem problem = problemRepository.findById(event.getProblemId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Problem not found: " + event.getProblemId()
                            )
                    );

            List<TestCase> testCases =
                    testCaseRepository.findByProblemId(event.getProblemId());

            LanguageConfig config =
                    LanguageConfig.forLanguage(event.getLanguage());

            int timeLimitSeconds =
                    Math.max(1, problem.getTimeLimitMs() / 1000);

            workDir = Files.createDirectories(
                    Path.of("/tmp/judge/" + event.getSubmissionId())
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
                    String.valueOf(event.getSubmissionId())
            ).toString();

            // --- Compile once, before any test case runs ---
            if (config.getCompileCommand() != null) {

                ExecutionResult compileResult = dockerExecutor.compile(
                        config.getDockerImage(),
                        hostWorkDir,
                        config.getCompileCommand(),
                        problem.getMemoryLimitMb()
                );

                if (compileResult.exitCode() != 0) {

                    resultProducer.publish(
                            event.getSubmissionId(),
                            SubmissionStatus.COMPILE_ERROR.name(),
                            truncate(compileResult.stderr()),
                            0,
                            testCases.size()
                    );

                    return;
                }
            }

            // --- Run each test case against the compiled/interpreted program ---

            int totalCount = testCases.size();
            int passedCount = 0;

            SubmissionStatus verdict = SubmissionStatus.ACCEPTED;
            String errorMessage = null;

            for (TestCase testCase : testCases) {

                ExecutionResult result = dockerExecutor.run(
                        config.getDockerImage(),
                        hostWorkDir,
                        config.getRunCommand(),
                        timeLimitSeconds,
                        problem.getMemoryLimitMb(),
                        testCase.getInput()
                );

                if (result.processTimedOut() || result.exitCode() == 124) {
                    verdict = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                    break;
                }

                if (result.exitCode() == 137) {
                    verdict = SubmissionStatus.MEMORY_LIMIT_EXCEEDED;
                    break;
                }

                if (result.exitCode() != 0) {
                    verdict = SubmissionStatus.RUNTIME_ERROR;
                    errorMessage = truncate(result.stderr());
                    break;
                }

                if (!normalize(result.stdout())
                        .equals(normalize(testCase.getExpectedOutput()))) {

                    verdict = SubmissionStatus.WRONG_ANSWER;
                    break;
                }

                passedCount++;
            }

            resultProducer.publish(
                    event.getSubmissionId(),
                    verdict.name(),
                    errorMessage,
                    passedCount,
                    totalCount
            );

        } catch (Exception e) {

            resultProducer.publish(
                    event.getSubmissionId(),
                    SubmissionStatus.RUNTIME_ERROR.name(),
                    "Internal judge error",
                    null,
                    null
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
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }
    }
}
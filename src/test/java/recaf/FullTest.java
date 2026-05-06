package recaf;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FullTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath().normalize();

    @Test
    void runGoodTestsWithO2() throws IOException, InterruptedException {
        runOrbCommand("./test", "tests/good", "-O2");
    }

    @Test
    void runBenchmarkWithO2() throws IOException, InterruptedException {
        runOrbCommand("./test", "benchmark", "-O2");
    }

    @Test
    void runBadTests() throws IOException, InterruptedException {
        runOrbCommand("./test", "tests/bad", "--bad");
    }

    private static void runOrbCommand(String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("orb");
        command.addAll(Arrays.asList(args));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(PROJECT_ROOT.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        assertEquals(
            0,
            exitCode,
            "Command failed:\n"
                + String.join(" ", command)
                + "\n\nExit code: "
                + exitCode
                + "\n\nOutput:\n"
                + output
        );
    }
}

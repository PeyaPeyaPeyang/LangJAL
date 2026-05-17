package tokyo.peya.langjal.jalp;

import java.nio.file.Path;
import java.time.Instant;

public record ClassInfo(
        Path classFile,
        Instant lastModified,
        long size,
        String sha256,
        byte[] bytes
) {
}

package io.upgradepilot.scan.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Unzips uploads with protection against zip-slip and zip bombs. */
public final class ZipExtractor {

    private static final long MAX_TOTAL_BYTES = 300L * 1024 * 1024;
    private static final int MAX_ENTRIES = 50_000;

    private ZipExtractor() {
    }

    public static void extract(InputStream in, Path target) throws IOException {
        Path root = target.toAbsolutePath().normalize();
        long total = 0;
        int entries = 0;
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                if (++entries > MAX_ENTRIES) {
                    throw new IOException("Archive has more than " + MAX_ENTRIES + " files");
                }
                Path out = root.resolve(entry.getName()).normalize();
                if (!out.startsWith(root)) {
                    throw new IOException("Archive contains an unsafe path: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                    continue;
                }
                Files.createDirectories(out.getParent());
                try (OutputStream os = Files.newOutputStream(out)) {
                    int n;
                    while ((n = zis.read(buffer)) > 0) {
                        total += n;
                        if (total > MAX_TOTAL_BYTES) {
                            throw new IOException("Archive is larger than 300 MB when unzipped");
                        }
                        os.write(buffer, 0, n);
                    }
                }
            }
        }
    }
}

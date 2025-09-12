package io.github.patbattb.moderant.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileService {

    public static final String ZIP_FILE_NAME = "message.zip";
    private static final String TEXT_FILE_NAME = "text.txt";

    public static void zipMessageText(String text) {
        try (FileOutputStream fos = new FileOutputStream(ZIP_FILE_NAME);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            ZipEntry entry = new ZipEntry(TEXT_FILE_NAME);
            zos.putNextEntry(entry);
            zos.write(text.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteZipFromDisk() {
        try {
            Files.delete(Path.of(ZIP_FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

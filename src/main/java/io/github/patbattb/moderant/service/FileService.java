package io.github.patbattb.moderant.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileService {
    public static void zipMessageText(String text, String zipFileName) {
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            ZipEntry entry = new ZipEntry("text.txt");
            zos.putNextEntry(entry);
            zos.write(text.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteZipFromDisk(String zipFileName) {
        try {
            Files.delete(Path.of(zipFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.sdnly;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) {
        String sinceDate = "2024-09-19";
        List<String> projectFolders = List.of(
                "project-path",
                "project-path",
                "project-path",
                "project-path"
        );

        String mainFolderName = "KSMO_" + new SimpleDateFormat("MMyyyy").format(new Date());
        File mainFolder = new File(mainFolderName);
        if (!mainFolder.exists()) {
            mainFolder.mkdir();
        }

        for (String folder : projectFolders) {
            try {
                String folderName = Paths.get(folder).getFileName().toString();
                String outputFileName = "pkup_" + folderName + ".txt";
                File outputFile = new File(mainFolder, outputFileName);

                ProcessBuilder builder = new ProcessBuilder(
                        "git", "log", "-p", "--author=Kamil Smolnik", "--since=" + sinceDate, "--all"
                );
                builder.directory(new File(folder));
                builder.redirectErrorStream(true);
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     PrintWriter writer = new PrintWriter(outputFile)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.println(line);
                    }
                }

                if (outputFile.length() == 0) {
                    outputFile.delete();
                } else {
                    zipFile(outputFile.getPath());
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.out.println("Error running git command in folder: " + folder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            attachWordDocument(mainFolderName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void zipFile(String filePath) throws Exception {
        Path zipFilePath = Paths.get(filePath.replace(".txt", ".zip"));
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            Path sourcePath = Paths.get(filePath);
            zipOut.putNextEntry(new ZipEntry(sourcePath.getFileName().toString()));
            Files.copy(sourcePath, zipOut);
            zipOut.closeEntry();
        }
    }

    private static void attachWordDocument(String mainFolderName) throws Exception {
        String datePattern = mainFolderName.split("_")[1];
        String wordFileName = "RAPORT MIESIĘCZNY PRACY TWÓRCZEJ_" + datePattern + "_DemoEngineering_Development.doc";
        Path sourceWordFile = Paths.get("pkup_word_template.doc");
        Path destinationWordFile = Paths.get(mainFolderName, wordFileName);

        Files.copy(sourceWordFile, destinationWordFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}


package com.mjsasha.lesdoc.services;

import com.mjsasha.lesdoc.TestData;
import com.mjsasha.lesdoc.configs.FileStorageProperties;
import com.mjsasha.lesdoc.exceptions.FileNotFoundException;
import com.mjsasha.lesdoc.exceptions.FileStorageException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    private static final String NOT_EMPTY_DIRECTORY_NAME = "not-empty-dir";
    private static final String TEST_DIRECTORY_NAME = "test-dir";
    private static final Path TEST_DIRECTORY_LOCATION = Paths.get(TEST_DIRECTORY_NAME).toAbsolutePath().normalize();

    private final FileStorageService fileStorageService;

    FileStorageServiceTest() {
        FileStorageProperties fileStorageProperties = Mockito.mock(FileStorageProperties.class);
        Mockito.when(fileStorageProperties.getUploadDir()).thenReturn(TEST_DIRECTORY_NAME);

        fileStorageService = new FileStorageService(fileStorageProperties);
    }

    @BeforeAll
    static void createTestDirectory() throws IOException {
        Files.createDirectories(TEST_DIRECTORY_LOCATION);
    }

    @AfterAll
    static void removeTestDirectory() throws IOException {
        File[] allContents = TEST_DIRECTORY_LOCATION.toFile().listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }

        Files.delete(TEST_DIRECTORY_LOCATION);
    }

    @Test
    void storeFileIncorrectFileName() {
        assertThrows(FileStorageException.class,
                () -> fileStorageService.storeFile(TestData.invalidMockFile, "not matter"));
    }

    @Test
    void getFilenamesFromNotExistingFolder() {
        assertThrows(FileStorageException.class,
                () -> fileStorageService.getAllFilesNames("not-existing-folder-name"));
    }

    @Test
    void getFilenamesFromEmptyFolder() throws IOException {
        String notEmptyDirectoryName = "empty-dir";
        Files.createDirectory(TEST_DIRECTORY_LOCATION.resolve(notEmptyDirectoryName));

        assertThrows(FileNotFoundException.class,
                () -> fileStorageService.getAllFilesNames(notEmptyDirectoryName));
    }

    @Test
    void getFilenamesFromNotEmptyFolder() throws IOException {
        Files.createDirectory(TEST_DIRECTORY_LOCATION.resolve(NOT_EMPTY_DIRECTORY_NAME));
        Files.copy(TestData.mockFile.getInputStream(),
                TEST_DIRECTORY_LOCATION
                        .resolve(NOT_EMPTY_DIRECTORY_NAME)
                        .resolve(TestData.mockFile.getOriginalFilename()));

        String[] allFilesNames = fileStorageService.getAllFilesNames(NOT_EMPTY_DIRECTORY_NAME);

        assertTrue(Arrays.stream(allFilesNames).anyMatch(name -> name.equals(TestData.mockFile.getOriginalFilename())));
    }

    @Test
    void directoryCreating() {
        String subdirectoryName = "sub-directory";

        fileStorageService.createDirectory(subdirectoryName);

        assertTrue(Files.exists(TEST_DIRECTORY_LOCATION.resolve(subdirectoryName)));
    }

    @Test
    void directoryCantBeCreated() {
        String incorrectDirectoryName = "!@#$%^&*()";

        assertThrows(FileStorageException.class,
                () -> fileStorageService.createDirectory(incorrectDirectoryName));
    }
}
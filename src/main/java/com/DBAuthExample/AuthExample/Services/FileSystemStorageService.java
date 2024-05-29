package com.DBAuthExample.AuthExample.Services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;

import com.DBAuthExample.AuthExample.Entity.FileToUpload;

import com.DBAuthExample.AuthExample.Storage.StorageException;
import com.DBAuthExample.AuthExample.Storage.StorageFileNotFoundException;
import com.DBAuthExample.AuthExample.Storage.StorageProperties;
import com.DBAuthExample.AuthExample.Storage.StorageService;
import com.DBAuthExample.AuthExample.repository.FileRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    private FileRepository fileReposiroty;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {

        if(properties.getLocation().trim().length() == 0){
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file, byte LvlAccess) {
        try {

            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
                FileToUpload fileToUpload = new FileToUpload();
                fileToUpload.setLvl(LvlAccess);
                fileToUpload.setName(file.getOriginalFilename());
                fileToUpload.setFiletype(file.getContentType());
                fileToUpload.setPath(destinationFile.toString());
                fileReposiroty.save(fileToUpload);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public  Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void update(String filepath, String content, String filename) {
        try {
            // Находим путь к файлу по его имени
            Path existingFile = Path.of(filepath);

            if (!Files.exists(existingFile)) {
                throw new StorageFileNotFoundException("Файл не найден: " + filepath);
            }
            Files.delete(existingFile);

            String[] parts2 = filename.split("\\.");

            switch (parts2[parts2.length-1]){
                case ("docx"):
                    CreateDocx(filepath, content);
                break;
                case ("txt"):
                    createFileWithContent(filepath, content);
                    break;
            }


        } catch (IOException e) {
            throw new StorageException("Ошибка при обновлении файла: " + filepath, e);
        }
    }

    public void createFileWithContent(String filename, String content) throws IOException {
        Path filePath = Path.of(filename);
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void CreateDocx(String filename, String content){

        XWPFDocument document = new XWPFDocument();

        // Создание нового параграфа
        XWPFParagraph paragraph = document.createParagraph();

        // Создание нового текстового элемента в параграфе и добавление в него содержимого строки
        XWPFRun run = paragraph.createRun();
        run.setText(content);

        // Сохранение документа в файл
        try (FileOutputStream out = new FileOutputStream(filename)) {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

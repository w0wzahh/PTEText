package com.ptetext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class PteTextApplication {

    public static void main(String[] args) {
        SpringApplication.run(PteTextApplication.class, args);
        try{
        checkCreateUploadsFolder();}
        catch(IOException e){

        }
    }

    public static void checkCreateUploadsFolder()throws IOException {
        final String uploadFolderPath  = "C:\\ptetext\\uploads";
        Path uploadFolder = Paths.get(uploadFolderPath);
        if(!Files.exists(uploadFolder)){
            Files.createDirectories(uploadFolder);
        }

    }
}

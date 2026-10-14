package com.sinnarmobile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.*;

@RestController
public class UploadController implements WebMvcConfigurer {
    private final Path directory;
    public UploadController(@Value("${app.upload-dir}") String directory) {this.directory=Path.of(directory).toAbsolutePath().normalize();}
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations(directory.toUri().toString()+"/").setCachePeriod(86400);
    }
    @PostMapping("/api/admin/uploads") @ResponseStatus(HttpStatus.CREATED)
    Api<Map<String,String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()||file.getSize()>5*1024*1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a PNG or JPEG smaller than 5 MB.");
        try(var input=ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The file is not a valid image.");
            var reader=readers.next();
            try {
                reader.setInput(input);String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!Set.of("png","jpeg","jpg").contains(format)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Only PNG and JPEG images are accepted.");
                int w=reader.getWidth(0),h=reader.getHeight(0);
                if(w>6000||h>6000||(long)w*h>20_000_000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Image dimensions are too large.");
                BufferedImage decoded=reader.read(0);
                String suffix=format.equals("png")?"png":"jpg";String name=UUID.randomUUID()+"."+suffix;
                Files.createDirectories(directory);Path output=directory.resolve(name).normalize();
                if(!output.startsWith(directory)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid path.");
                ImageIO.write(decoded,suffix,output.toFile());return Api.ok(Map.of("url","/uploads/"+name));
            } finally {reader.dispose();}
        } catch(javax.imageio.IIOException e) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Image is corrupt.");}
    }
}

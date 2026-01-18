package com.allinone.serviceImpl;

import com.allinone.entity.video.Video;
import com.allinone.properties.VideoProperties;
import com.allinone.repository.video.VideoRepository;
import com.allinone.service.VideoService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoServiceImpl implements VideoService {

    VideoRepository videoRepository;
    VideoProperties videoProperties;

    @PostConstruct
    public void init() {

        File file = new File(videoProperties.getVideo());
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("Video directory created at: " + file.getAbsolutePath());
        } else {
            System.out.println("Video directory already exists at: " + file.getAbsolutePath());
        }
    }

    @Override
    public Video save(Video video, MultipartFile file) {

        try {

            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            assert fileName != null;


            String cleanFileName = StringUtils.cleanPath(fileName);
            String cleanFolder = StringUtils.cleanPath(videoProperties.getVideo());

            Path path = Paths.get(cleanFolder, cleanFileName);

            System.err.println(contentType);
            System.err.println(path);

            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            video.setContentType(contentType);
            video.setFilePath(path.toString());

            return videoRepository.save(video);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Video get(Long id) {
        return null;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return null;
    }
}

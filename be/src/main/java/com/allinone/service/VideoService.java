package com.allinone.service;

import com.allinone.entity.video.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    Video save(Video video, MultipartFile file);

    Video get(Long id);

    Video getByTitle(String title);

    List<Video> getAll();
}

package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.entity.video.Video;
import com.allinone.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/video")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VideoController {

    VideoService videoService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);

        Video saveVideo = videoService.save(video, file);
        return ResponseEntity.ok(
                ApiResponse.<Video>builder()
                        .code(200)
                        .message("Video saved successfully")
                        .result(saveVideo)
                        .build());
    }
}

package com.jiangmai.platform.controller;

import com.jiangmai.platform.utils.Result;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Path VIDEO_DIR = Paths.get("uploads", "videos");
    private static final Path IMAGE_DIR = Paths.get("uploads", "images");
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp4", ".webm", ".mov", ".m4v", ".avi", ".mkv"
    ));
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    ));

    @PostMapping("/upload/video")
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择视频文件");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!contentType.startsWith("video/")) {
            return Result.error("仅支持视频文件上传");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0) {
            ext = original.substring(idx).toLowerCase();
        }
        if (!ALLOWED_VIDEO_EXTENSIONS.contains(ext)) {
            return Result.error("仅支持 mp4/webm/mov/m4v/avi/mkv 格式视频");
        }

        long maxBytes = 200L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            return Result.error("视频大小不能超过 200MB");
        }
        try {
            Files.createDirectories(VIDEO_DIR);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = VIDEO_DIR.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Map<String, String> data = new HashMap<>();
            data.put("url", "/api/files/videos/" + filename);
            data.put("filename", filename);
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("视频上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/videos/{filename}")
    public ResponseEntity<Resource> getVideo(@PathVariable String filename) throws IOException {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path path = VIDEO_DIR.resolve(filename).normalize();
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path.toFile());
        String contentType = Files.probeContentType(path);
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PostMapping("/upload/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择图片文件");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!contentType.startsWith("image/")) {
            return Result.error("仅支持图片文件上传");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0) {
            ext = original.substring(idx).toLowerCase();
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            return Result.error("仅支持 jpg/jpeg/png/webp/gif 格式图片");
        }

        long maxBytes = 10L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            return Result.error("图片大小不能超过 10MB");
        }
        try {
            Files.createDirectories(IMAGE_DIR);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = IMAGE_DIR.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Map<String, String> data = new HashMap<>();
            data.put("url", "/api/files/images/" + filename);
            data.put("filename", filename);
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path path = IMAGE_DIR.resolve(filename).normalize();
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path.toFile());
        String contentType = Files.probeContentType(path);
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}

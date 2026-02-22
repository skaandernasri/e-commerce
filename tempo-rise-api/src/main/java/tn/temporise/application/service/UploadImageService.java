package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.ImageUploadResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadImageService {
  private final ExceptionFactory exceptionFactory;
  @Value("${uploadDir}")
  private String uploadDir;

  public ImageUploadResponse uploadImage(MultipartFile file) {
    try {
      if (file == null || file.isEmpty() || file.getContentType() == null
          || !file.getContentType().startsWith("image/")) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }

      File dir = new File(uploadDir);
      if (!dir.exists() && !dir.mkdirs()) { // check if mkdirs fails
        throw new IOException("Failed to create directory: " + uploadDir);
      }

      String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path path = Paths.get(dir.getAbsolutePath(), filename); // safer path
      Files.copy(file.getInputStream(), path);
      ImageUploadResponse imageUploadResponse = new ImageUploadResponse();
      imageUploadResponse.setFilename(filename);
      log.info("Uploaded image successfully: {}", filename);
      return imageUploadResponse;

    } catch (BadRequestException e) {
      log.warn("Failed to upload image with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Failed to upload image", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }


  public void deleteImage(String filename) {
    try {
      File file = new File(uploadDir, filename); // safer join
      if (file.exists()) {
        if (!file.delete()) {
          log.warn("Failed to delete file: {}", file.getAbsolutePath());
        } else {
          log.info("Deleted file: {}", file.getAbsolutePath());
        }

      }
    } catch (Exception e) {
      log.error("Failed to delete image", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteAllImages() {
    try {
      File dir = new File(uploadDir);
      if (dir.exists() && dir.isDirectory()) {
        File[] files = dir.listFiles();
        if (files != null) {
          for (File file : files) {
            if (file.isFile()) { // 🔹 only delete regular files
              if (!file.delete()) {
                log.warn("Failed to delete file: {}", file.getAbsolutePath());
              } else {
                log.info("Deleted file: {}", file.getAbsolutePath());
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to delete all images", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteByContent(String content) {
    try {
      File dir = new File(uploadDir);
      if (dir.exists()) {
        File[] files = dir.listFiles();
        if (files != null) {
          for (File file : files) {
            if (content.contains(file.getName())) {
              if (!file.delete()) {
                log.warn("Failed to delete file by content: {}", file.getAbsolutePath());
              } else {
                log.info("Deleted file by content: {}", file.getAbsolutePath());
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to delete by content", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteByOldImage(String newContent, String oldContent) {
    try {
      File dir = new File(uploadDir);
      if (dir.exists()) {
        File[] files = dir.listFiles();
        if (files != null) {
          for (File file : files) {
            if (oldContent.contains(file.getName()) && !newContent.contains(file.getName())) {
              if (!file.delete()) {
                log.warn("Failed to delete file by content: {}", file.getName());
              } else {
                log.info("Deleted file by content: {}", file.getName());
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to delete by content", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

}

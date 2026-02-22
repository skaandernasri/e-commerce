package tn.temporise.application.service;

import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.ImageProduitRepo;
import tn.temporise.domain.port.VariantRepo;
import tn.temporise.infrastructure.client.meta.SendEventsApi;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaPixelService {
  @Value("${meta.access.token}")
  private String metaAccessToken;
  @Value("${meta.pixel.id}")
  private String pixelId;
  @Value("${uploadDir}")
  private String uploadDir;
  @Value("${prod}")
  private String isProd;
  @Value("${csv.access.token}")
  private String csvAccessToken;
  private final ExceptionFactory exceptionFactory;
  private final CommandeRepo commandeRepo;
  private final SendEventsApi sendEventsApi;
  private final VariantRepo variantRepo;
  private final ImageProduitRepo imageProduitRepo;
  private final UploadImageService uploadImageService;
  private final HttpServletResponse response;
  private final HttpServletRequest request;
  private final JwtUtil jwtUtil;

  @Transactional
  public Resource generateCatalogueCsv() {
    try {
      processBearerToken();
      File dir = new File(uploadDir);
      if (!dir.exists() && !dir.mkdirs()) {
        throw new IOException("Failed to create directory: " + uploadDir);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

        // Write header first
        String[] header = {"id", "title", "description", "rich_text_description", "availability",
            "condition", "price", "link", "image_link", "brand", "size", "color", "sale_price",
            "sale_price_effective_date"};
        writer.writeNext(header);

        // Get all variants
        List<Variant> variants = variantRepo.findAll();
        log.info("Processing {} variants for catalog", variants.size());

        // Process each variant
        for (Variant variant : variants) {
          try {
            Produit produit = variant.produit();
            Optional<Promotion> promotion = produit.promotions().stream()
                .filter(prom -> (prom.dateDebut().isBefore(LocalDateTime.now())
                    || prom.dateDebut().isEqual(LocalDateTime.now()))
                    && prom.dateFin().isAfter(LocalDateTime.now()))
                .findFirst();
            if (produit == null) {
              log.warn("Variant {} has null product, skipping", variant.id());
              continue;
            }

            log.debug("Processing variant {} for product {}", variant.id(), produit.nom());

            CatalogueCsvDto catalogueCsvDto = new CatalogueCsvDto();
            catalogueCsvDto.setId(variant.id());
            catalogueCsvDto.setTitle(produit.nom());
            catalogueCsvDto.setDescription("description " + produit.nom());
            catalogueCsvDto
                .setAvailability(variant.quantity() > 0 ? AvailabilityEnum.IN_STOCK.getValue()
                    : AvailabilityEnum.OUT_OF_STOCK.getValue());
            catalogueCsvDto.setCondition(ConditionEnum.NEW.getValue());
            catalogueCsvDto.setPrice(produit.prix() + " " + "TND");

            // Set product link
            String baseUrl = "true".equalsIgnoreCase(isProd) ? "https://www.temposphere.tn"
                : "http://localhost:4200";
            catalogueCsvDto.setLink(baseUrl + "/product/" + produit.id());

            // Process image
            String imageLink = processProductImage(produit, dir);
            catalogueCsvDto.setImageLink(imageLink);

            catalogueCsvDto.setBrand(produit.marque());
            catalogueCsvDto.setSize(variant.size());
            catalogueCsvDto.setColor(variant.color());
            catalogueCsvDto.setRichTextDescription(produit.description());

            if (promotion.isPresent()) {
              Promotion activePromotion = promotion.get();
              if (activePromotion.type().equals(PromotionType.FIXED))
                catalogueCsvDto
                    .setSalePrice((produit.prix() - activePromotion.reduction()) + " " + "TND");
              if (activePromotion.type().equals(PromotionType.PERCENTAGE))
                catalogueCsvDto.setSalePrice(
                    (produit.prix() - (produit.prix() * (activePromotion.reduction() / 100))) + " "
                        + "TND");
              OffsetDateTime startUtc = activePromotion.dateDebut().atOffset(ZoneOffset.UTC);
              OffsetDateTime endUtc = activePromotion.dateFin().atOffset(ZoneOffset.UTC);
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
              catalogueCsvDto.setSalePriceEffectiveDate(
                  startUtc.format(formatter) + "/" + endUtc.format(formatter));
            }

            // Write row
            writer.writeNext(new String[] {String.valueOf(catalogueCsvDto.getId()),
                catalogueCsvDto.getTitle(), catalogueCsvDto.getDescription(),
                catalogueCsvDto.getRichTextDescription(), catalogueCsvDto.getAvailability(),
                catalogueCsvDto.getCondition(), catalogueCsvDto.getPrice(),
                catalogueCsvDto.getLink(), catalogueCsvDto.getImageLink(),
                catalogueCsvDto.getBrand() != null ? catalogueCsvDto.getBrand() : "",
                catalogueCsvDto.getSize() != null ? catalogueCsvDto.getSize() : "",
                catalogueCsvDto.getColor() != null ? catalogueCsvDto.getColor() : "",
                catalogueCsvDto.getSalePrice() != null ? catalogueCsvDto.getSalePrice() : "",
                catalogueCsvDto.getSalePriceEffectiveDate() != null
                    ? catalogueCsvDto.getSalePriceEffectiveDate()
                    : ""});

            writer.flush();

          } catch (NotFoundException e) {
            throw e;
          } catch (Exception e) {
            log.error("Error processing variant {}: {}", variant.id(), e.getMessage(), e);
            throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
            // Continue processing other variants
          }
        }
        // File dirCat = new File(catalogueDir);
        // if (!dirCat.exists() && !dirCat.mkdirs()) { // check if mkdirs fails
        // throw new IOException("Failed to create directory: " + dirCat);
        // }
        // Path outputPath = Paths.get(dirCat.getAbsolutePath(), "catalogue.csv");
        // Files.write(outputPath, baos.toByteArray());
        // log.info("Catalog CSV generation completed successfully");
        response.addHeader("Content-Disposition", "attachment; filename=catalogue.csv");
        return new ByteArrayResource(baos.toByteArray());
      }

    } catch (UnauthorizedException e) {
      throw e;
    } catch (Exception e) {
      log.error("Fatal error generating catalog CSV: {}", e.getMessage(), e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Process product image and return the appropriate image link
   */
  private String processProductImage(Produit produit, File uploadDirectory) {
    try {
      // Check if product has images
      if (produit.imageProduits() == null || produit.imageProduits().isEmpty()) {
        log.debug("No images for product {}", produit.id());
        return "";
      }

      // Get first image
      ImageProduit imageProduit = produit.imageProduits().stream().findFirst().get();
      if (imageProduit.url() == null || imageProduit.url().isEmpty()) {
        log.warn("Product {} has invalid image entry", produit.id());
        return "";
      }

      log.debug("Processing image: {}", imageProduit.url());

      // Build file path
      Path imagePath = Paths.get(uploadDirectory.getAbsolutePath(), imageProduit.url());
      File imageFile = imagePath.toFile();

      // Check if original file exists
      if (!imageFile.exists()) {
        log.warn("Image file not found: {}", imagePath);

        // Try to find PNG version
        String baseFileName = removeExtension(imageProduit.url());
        Path pngPath = Paths.get(uploadDirectory.getAbsolutePath(), baseFileName + ".png");

        if (pngPath.toFile().exists()) {
          log.info("Found PNG version for missing file: {}", pngPath.getFileName());
          return buildImageUrl(baseFileName + ".png");
        }

        log.error("No image file found for product {}: {}", produit.id(), imageProduit.url());
        return "";
      }

      String extension = getExtension(imageProduit.url());

      // If already PNG or JPEG, return as is
      if (extension != null && (extension.equalsIgnoreCase("png")
          || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg"))) {
        return buildImageUrl(imageProduit.url());
      }

      // Need to convert to PNG
      log.info("Converting image {} from {} to PNG", imageProduit.url(), extension);
      String convertedFileName =
          convertImageToPng(imageFile, imageProduit, produit, uploadDirectory);

      if (convertedFileName != null && !convertedFileName.isEmpty()) {
        return buildImageUrl(convertedFileName);
      }

      return "";

    } catch (Exception e) {
      log.error("Error processing image for product {}: {}", produit.id(), e.getMessage(), e);
      return "";
    }
  }

  /**
   * Convert image to PNG format
   */
  private String convertImageToPng(File imageFile, ImageProduit imageProduit, Produit produit,
      File uploadDirectory) {
    ByteArrayOutputStream imageBuffer = null;

    try {
      // Read image
      BufferedImage bufferedImage = readImage(imageFile);

      if (bufferedImage == null) {
        log.error("Failed to read image: {} (format may not be supported)", imageProduit.url());
        return null;
      }

      // Generate new filename
      String newFileName = removeExtension(imageProduit.url()) + ".png";
      Path newImagePath = Paths.get(uploadDirectory.getAbsolutePath(), newFileName);

      // Check if converted file already exists
      if (newImagePath.toFile().exists()) {
        log.info("PNG version already exists: {}", newFileName);

        // Update database if needed
        if (!imageProduit.url().equals(newFileName)) {
          updateImageProduit(imageProduit, produit, newFileName, imageFile);
        }

        return newFileName;
      }

      // Convert to PNG
      imageBuffer = new ByteArrayOutputStream();
      boolean writeSuccess = ImageIO.write(bufferedImage, "png", imageBuffer);

      if (!writeSuccess) {
        log.error("Failed to write PNG for: {}", imageProduit.url());
        return null;
      }

      byte[] imageBytes = imageBuffer.toByteArray();

      // Validate converted image
      if (imageBytes.length == 0) {
        log.error("Converted image is empty for: {}", imageProduit.url());
        return null;
      }

      // Write to file
      Files.write(newImagePath, imageBytes);
      log.info("Successfully wrote PNG file: {} ({} bytes)", newFileName, imageBytes.length);

      // Update database and delete old file
      updateImageProduit(imageProduit, produit, newFileName, imageFile);

      return newFileName;

    } catch (Exception e) {
      log.error("Error converting image {}: {}", imageProduit.url(), e.getMessage(), e);
      return null;
    } finally {
      if (imageBuffer != null) {
        try {
          imageBuffer.close();
        } catch (IOException e) {
          log.warn("Error closing image buffer: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Update ImageProduit in database and delete old file
   */
  private void updateImageProduit(ImageProduit imageProduit, Produit produit, String newFileName,
      File oldFile) {
    try {
      // Delete old image file
      String oldUrl = imageProduit.url();
      if (!oldUrl.equals(newFileName)) {
        uploadImageService.deleteImage(oldUrl);
        log.info("Deleted old image file: {}", oldUrl);
      }

      // Update database
      ImageProduit updatedImage =
          ImageProduit.builder().id(imageProduit.id()).produit(produit).url(newFileName).build();

      imageProduitRepo.save(updatedImage);
      log.info("Updated image in database: {} -> {}", oldUrl, newFileName);

    } catch (Exception e) {
      log.error("Error updating image produit: {}", e.getMessage(), e);
      // Don't throw - conversion was successful, DB update is secondary
    }
  }

  /**
   * Build image URL based on environment
   */
  private String buildImageUrl(String fileName) {
    String baseUrl =
        "true".equalsIgnoreCase(isProd) ? "https://api.temposphere.tn" : "http://localhost:8080";
    return baseUrl + "/uploads/" + fileName;
  }

  private String buildCatalogueUrl() {
    String baseUrl =
        "true".equalsIgnoreCase(isProd) ? "https://api.temposphere.tn" : "http://localhost:8080";
    return baseUrl + "/catalogue/catalogue.csv";
  }

  /**
   * Sanitize description for CSV
   */
  private String sanitizeDescription(String description) {
    if (description == null) {
      return "";
    }

    // Remove HTML tags if needed
    String sanitized = description.replaceAll("<[^>]*>", "");

    // Limit length if too long (Meta catalogs have limits)
    if (sanitized.length() > 5000) {
      sanitized = sanitized.substring(0, 4997) + "...";
    }

    return sanitized;
  }

  private String getExtension(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    int lastDot = url.lastIndexOf('.');
    if (lastDot == -1 || lastDot == url.length() - 1) {
      return null;
    }
    return url.substring(lastDot + 1);
  }

  private String removeExtension(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }

    int lastDot = url.lastIndexOf('.');
    if (lastDot == -1) {
      return url;
    }
    return url.substring(0, lastDot);
  }

  /**
   * Read image with support for various formats
   *
   * SUPPORTED FORMATS (with TwelveMonkeys ImageIO dependencies): - Standard: PNG, JPEG, JPG, GIF,
   * BMP, WBMP - Extended: WebP, TIFF, PSD, ICO, ICNS, TGA, PCX, PICT, PNM, SGI
   */
  private BufferedImage readImage(File imageFile) throws IOException {
    if (imageFile == null || !imageFile.exists()) {
      throw new IOException("Image file does not exist: " + imageFile);
    }

    String fileName = imageFile.getName();
    String extension = getExtension(fileName);

    try {
      log.debug("Reading image: {} (format: {}, size: {} bytes)", fileName, extension,
          imageFile.length());

      // First try standard ImageIO.read()
      BufferedImage image = ImageIO.read(imageFile);
      if (image != null) {
        log.debug("Successfully read image using ImageIO.read(): {} ({}x{})", fileName,
            image.getWidth(), image.getHeight());
        return image;
      }

      // If ImageIO.read returns null, try with ImageReader
      log.debug("ImageIO.read() returned null, trying ImageReader for: {}", fileName);

      try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile)) {
        if (iis == null) {
          throw new IOException("Could not create ImageInputStream for: " + fileName);
        }

        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        if (readers.hasNext()) {
          ImageReader reader = readers.next();
          String readerFormat = reader.getFormatName();
          log.debug("Using ImageReader: {} for format: {}", reader.getClass().getSimpleName(),
              readerFormat);

          try {
            reader.setInput(iis, true);
            BufferedImage result = reader.read(0);

            if (result != null) {
              log.debug("Successfully read image using ImageReader: {} ({}x{})", fileName,
                  result.getWidth(), result.getHeight());
              return result;
            }
          } finally {
            reader.dispose();
          }
        } else {
          log.warn("No ImageReader found for: {} (extension: {})", fileName, extension);
          log.warn("Available formats: {}", getAvailableImageFormats());
        }
      }

      throw new IOException("No suitable reader found for image: " + fileName);

    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error reading image {}: {}", fileName, e.getMessage(), e);
      throw new IOException("Failed to read image: " + fileName, e);
    }
  }

  /**
   * Get list of available image formats
   */
  private String getAvailableImageFormats() {
    String[] readerFormats = ImageIO.getReaderFormatNames();
    return String.join(", ", readerFormats);
  }

  public Response sendMetaPixelEvent(MetaPixelEventRequest request) {
    try {
      if ("true".equalsIgnoreCase(isProd)) {
        if (request == null || request.getData() == null || request.getData().isEmpty()
            || request.getData().getFirst().getEventName() == null)
          throw exceptionFactory.badRequest("badrequest.invalid_request");
        MetaPixelEventRequestDataInner data = request.getData().getFirst();

        if (data.getUserData() == null) {
          MetaPixelEventRequestDataInnerUserData userData =
              new MetaPixelEventRequestDataInnerUserData();
          userData.setClientIpAddress(getClientIpAddress());
          userData.setClientUserAgent(getClientUserAgent());
          data.setUserData(userData);
        } else {
          data.getUserData().setClientIpAddress(getClientIpAddress());
          data.getUserData().setClientUserAgent(getClientUserAgent());
        }

        sendEventsApi.sendEvents(pixelId, metaAccessToken, request);
        Response response = new Response();
        response.setCode("200");
        response.setMessage(data.getEventName() + " event envoyé avec succée");
        log.info("meta pixel event:" + data.getEventName() + "send");
        return response;
      }
      Response response = new Response();
      response.setCode("200");
      response.setMessage("We are in dev mod can't send meta pixel event");
      return response;
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void sendLeadEvent(String content_name, String email) {
    if ("true".equalsIgnoreCase(isProd)) {
      MetaPixelEventRequestDataInner data = new MetaPixelEventRequestDataInner();
      data.setEventName("Lead");
      data.setEventTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
      if (!email.isEmpty()) {
        MetaPixelEventRequestDataInnerUserData userData =
            new MetaPixelEventRequestDataInnerUserData();
        userData.addEmItem(hashSHA256(email));
        data.setUserData(userData);
      }
      MetaPixelEventRequestDataInnerCustomData customData =
          new MetaPixelEventRequestDataInnerCustomData();
      customData.setContentName(content_name);
      data.setCustomData(customData);
      MetaPixelEventRequest metaPixelEventRequest = new MetaPixelEventRequest();
      metaPixelEventRequest.getData().add(data);
      sendEventsApi.sendEvents(pixelId, metaAccessToken, metaPixelEventRequest);
      log.info("Successfully sent lead event");
    }
  }

  private String hashSHA256(String input) {
    try {
      if (input == null) {
        throw new IllegalArgumentException("Input cannot be null");
      }

      // String inputLC = input.toLowerCase().trim();
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();

    } catch (Exception e) {
      log.error("Error hashing input: {}", e.getMessage());
      throw new RuntimeException("Failed to hash input", e);
    }
  }

  private void processMetaToken(String token) {
    if (token == null || token.isEmpty())
      throw exceptionFactory.unauthorized("unauthorized.invalid_token");
    if (token.equals(csvAccessToken))
      System.out.println("valid csv download token");
    else
      throw exceptionFactory.unauthorized("unauthorized.invalid_token");
  }

  private void processBearerToken() throws UnauthorizedException {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      log.debug("JWT found in Authorization header");
      String token = authorizationHeader.substring(7);
      String subject = jwtUtil.extractSubject(token);
      processMetaToken(subject);
      System.out.println("valid jwt token");
    } else
      throw exceptionFactory.unauthorized("unauthorized.invalid_token");
  }

  private String getClientIpAddress() {
    String ip = request.getHeader("X-Forwarded-For");

    if (ip == null) {
      ip = request.getRemoteAddr();
    } else {
      ip = ip.split(",")[0]; // take first IP
    }
    return ip;
  }

  private String getClientUserAgent() {
    return request.getHeader("User-Agent");
  }
}

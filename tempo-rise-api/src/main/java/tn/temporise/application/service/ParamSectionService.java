package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.SectionMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.SectionRepo;
import tn.temporise.domain.port.UserRepo;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParamSectionService {
  private final ExceptionFactory exceptionFactory;
  private final SectionRepo sectionRepo;
  private final SectionMapper sectionMapper;
  private final UploadImageService uploadImageService;
  private final UserRepo userRepo;

  @Transactional
  public ParamSectionResponse createSection(ParamSectionRequest paramSectionRequest) {
    try {
      if (paramSectionRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      Section section = sectionMapper.dtoToModel(paramSectionRequest);
      if (section.active()) {
        disableOtherSections(section.type(), section.typePage());
      }
      section = sectionRepo.save(section);

      return sectionMapper.modelToDto(sectionRepo.findById(section.id()));
    } catch (NotFoundException | BadRequestException | ConflictException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public ParamSectionResponse updateSection(Long id, ParamSectionRequest paramSectionRequest) {
    try {
      if (paramSectionRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      UtilisateurModel user = userRepo.findByEmail(userDetails.getUsername());
      Section savedSection = sectionRepo.findById(id);
      Section sectionUpdated = sectionMapper.dtoToModel(paramSectionRequest);
      if (sectionUpdated.active()) {
        disableOtherSections(sectionUpdated.type(), sectionUpdated.typePage());
      }
      log.info("Section updated: {}", sectionUpdated);
      sectionUpdated = sectionUpdated.toBuilder().updatedBy(user)
          .createdAt(savedSection.createdAt()).createdBy(savedSection.createdBy())
          .imageUrl(savedSection.imageUrl()).updatedAt(LocalDateTime.now()).id(id).build();
      sectionUpdated = sectionRepo.save(sectionUpdated);
      return sectionMapper.modelToDto(sectionRepo.findById(id));
    } catch (NotFoundException | BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Response deleteSection(Long id) {
    try {
      Section section = sectionRepo.findById(id);
      sectionRepo.delete(id);
      Response response = new Response();
      response.setCode("200");
      response.setMessage("section deleted");
      return response;
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public ParamSectionResponse getSectionById(Long id) {
    try {
      Section section = sectionRepo.findById(id);
      return sectionMapper.modelToDto(section);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ParamSectionResponse> getSections() {
    try {
      List<Section> sections = sectionRepo.findAll();
      return sections.stream().map(sectionMapper::modelToDto).toList();
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ParamSectionResponse> getSectionsByType(TypeSectionDto typeSectionDto) {
    try {
      List<Section> sections = sectionRepo.findByType(typeSectionDtoToTypeSection(typeSectionDto));
      return sections.stream().map(sectionMapper::modelToDto).toList();
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ParamSectionResponse> getSectionsByPageType(TypePageDto typePageDto) {
    try {
      List<Section> sections = sectionRepo.findByTypePage(typePageDtoToTypePage(typePageDto));
      return sections.stream().map(sectionMapper::modelToDto).toList();
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public ImageUploadResponse uploadImage(Long id, MultipartFile image) {
    try {
      Section section = sectionRepo.findById(id);
      ImageUploadResponse imageUploadResponse = uploadImageService.uploadImage(image);
      if (section.imageUrl() != null && !section.imageUrl().isBlank())
        uploadImageService.deleteImage(section.imageUrl());
      section = section.toBuilder().imageUrl(imageUploadResponse.getFilename()).build();
      sectionRepo.save(section);
      return imageUploadResponse;
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void deleteImage(Long id) {
    try {
      Section section = sectionRepo.findById(id);
      if (section.imageUrl() != null && !section.imageUrl().isBlank()) {
        uploadImageService.deleteImage(section.imageUrl());
        log.info("image deleted {}", section.imageUrl());
      }
      section = section.toBuilder().imageUrl(null).build();
      sectionRepo.save(section);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private TypeSection typeSectionDtoToTypeSection(TypeSectionDto typeSectionDto) {
    if (typeSectionDto == null) {
      return null;
    }

    // TypeSection typeSection;
    for (TypeSection type : TypeSection.values()) {
      if (type.name().equals(typeSectionDto.name())) {
        return type;
      }
    }
    throw exceptionFactory.badRequest("badrequest.invalid_input");
    // switch (typeSectionDto) {
    // case HERO -> typeSection = TypeSection.HERO;
    // case FOOTER -> typeSection = TypeSection.FOOTER;
    // case HEADER -> typeSection = TypeSection.HEADER;
    // case CONTACT -> typeSection = TypeSection.CONTACT;
    // case FAQ -> typeSection = TypeSection.FAQ;
    // case OUR_PRODUCTS -> typeSection = TypeSection.OUR_PRODUCTS;
    // case CONTENT1 -> typeSection = TypeSection.CONTENT1;
    // case CONTENT2 -> typeSection = TypeSection.CONTENT2;
    // case CONTENT3 -> typeSection = TypeSection.CONTENT3;
    // case CONTENT4 -> typeSection = TypeSection.CONTENT4;
    // case CONTENT5 -> typeSection = TypeSection.CONTENT5;
    // case CONTENT6 -> typeSection = TypeSection.CONTENT6;
    // case CONTENT7 -> typeSection = TypeSection.CONTENT7;
    // case CONTENT8 -> typeSection = TypeSection.CONTENT8;
    // case HTML -> typeSection = TypeSection.HTML;
    // case OUR_REVIEWS -> typeSection = TypeSection.OUR_REVIEWS;
    // case CUSTOM -> typeSection = TypeSection.CUSTOM;
    // case BANNER_SCROLLING -> typeSection = TypeSection.BANNER_SCROLLING;
    // case OUR_BLOGS -> typeSection = TypeSection.OUR_BLOGS;
    // case DESCRIPTION -> typeSection = TypeSection.DESCRIPTION;
    // case MAIN -> typeSection = TypeSection.MAIN;
    // case SOCIATY_INFO -> typeSection = TypeSection.SOCIATY_INFO;
    // default -> throw new IllegalArgumentException("Unexpected enum constant: " + typeSectionDto);
    // }
    //
    // return typeSection;
  }

  private TypePage typePageDtoToTypePage(TypePageDto typePageDto) {
    if (typePageDto == null) {
      return null;
    }

    // TypePage typePage;
    for (TypePage type : TypePage.values()) {
      if (type.name().equals(typePageDto.name())) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unexpected enum constant: " + typePageDto);

    // switch (typePageDto) {
    // case HOME -> typePage = TypePage.HOME;
    // case PRODUCT -> typePage = TypePage.PRODUCT;
    // case ALL -> typePage = TypePage.ALL;
    // case CONTACT -> typePage = TypePage.CONTACT;
    // case BLOG -> typePage = TypePage.BLOG;
    // case ABOUT -> typePage = TypePage.ABOUT;
    // case TERMS_AND_CONDITIONS -> typePage = TypePage.TERMS_AND_CONDITIONS;
    // case PRODUCT_DETAILS -> typePage = TypePage.PRODUCT_DETAILS;
    // default -> throw new IllegalArgumentException("Unexpected enum constant: " + typePageDto);
    // }
    //
    // return typePage;
  }

  private void disableOtherSections(TypeSection typeSection, TypePage typePage) {
    sectionRepo.checkAllTypeInactive(typeSection, typePage);
  }


}

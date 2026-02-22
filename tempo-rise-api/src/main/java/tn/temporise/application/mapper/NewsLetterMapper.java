package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.NewsLetter;
import tn.temporise.infrastructure.persistence.entity.NewsLetterEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface NewsLetterMapper {

  NewsLetterEntity modelToEntity(NewsLetter newsLetter);

  NewsLetter entityToModel(NewsLetterEntity newsLetterEntity);
}

package tn.temporise.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;


@Component
public class ExceptionMessageUtil {

  private final MessageSource messageSource;

  @Autowired
  public ExceptionMessageUtil(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String getMessage(String code) {
    return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
  }

  public String getFormattedMessage(String code, Object... args) {
    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
  }
}

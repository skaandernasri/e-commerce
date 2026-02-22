import {NgcCookieConsentConfig} from 'ngx-cookieconsent';
import { environment } from '../../../environments/environment';
function getCssVar(varName: string): string {
  return getComputedStyle(document.documentElement)
    .getPropertyValue(varName)
    .trim();
}
export const cookieConfig:NgcCookieConsentConfig = {
  cookie: {
    domain: environment.domain,
    name: 'cookieconsent_status' // or 'your.domain.com' // it is mandatory to set a domain, for cookies to work properly (see https://goo.gl/S2Hy2A)
  },
  palette: {
    popup: {
      background: getCssVar('--background'),
    },
    button: {
      background: getCssVar('--primary'),
      text: getCssVar('--text2')
    }
  },
  theme: 'edgeless',
  type: 'opt-out',
    content: {
    message: 'We use cookies to improve your experience.',
    allow: 'Accept',
    deny: 'Decline',
  }
};
package tn.temporise.domain.port;

import tn.temporise.domain.model.Notif;


public interface NotifRepo {
  Notif save(Notif notif);
}

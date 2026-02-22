package tn.temporise.domain.port;


import tn.temporise.domain.model.ConfigurationGlobal;

public interface ConfigurationGlobalRepo {
  ConfigurationGlobal save(ConfigurationGlobal config);

  ConfigurationGlobal getConfig();

}

package bj.salledereunion.security;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public final class ConfigurationJaas extends Configuration {
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String nom) {
        if (!"SalleDeReunion".equals(nom)) {
            return null;
        }
        return new AppConfigurationEntry[]{
            new AppConfigurationEntry(UtilisateurLoginModule.class.getName(),
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, Map.of())
        };
    }
}

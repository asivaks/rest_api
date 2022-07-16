package config;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:config/dodo/credentials.properties"  //path from source root
        //NB in Jenkins path from repository root
})

public interface DodoConfig extends Config {
    @Key("webUrl")
    String webUrl();

    @Key("apiUrl")
    String apiUrl();

    @Key("userLogin")
    String userLogin();

    @Key("userPassword")
    String userPassword();

    @Key("authCookieName")
    @DefaultValue("NOPCOMMERCE.AUTH")
    String authCookieName();

}

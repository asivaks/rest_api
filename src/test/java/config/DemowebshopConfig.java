package config;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:app.properties"
})

public interface DemowebshopConfig extends Config {
    @Key("webUrl")
    String webUrl();

    @Key("apiUrl")
    String apiUrl();

    @Key("userLogin")
    String userLogin();

    @Key("userPassword")
    String userPassword();

}

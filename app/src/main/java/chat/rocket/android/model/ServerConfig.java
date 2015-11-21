package chat.rocket.android.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.NotNull;
import ollie.annotation.Table;
import ollie.annotation.Unique;

/**
 * Now we can register only 1 server (because of Ollie's restriction)
 * However, in future multi-server (seemless switching) should be supported.
 */
@Table("server_config")
public class ServerConfig extends Model {
    @Column("displayname")
    public String displayname;

    @Column("hostname")
    @Unique
    @NotNull
    public String hostname;

    @Column("account")
    public String account;

    @Column("auth_token")
    public String authToken;

    @Column("is_primary")
    public Boolean isPrimary;
}

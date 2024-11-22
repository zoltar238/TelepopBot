package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CookieModel2 {
    private String domain;
    private long expirationDate;
    private boolean hostOnly;
    private boolean httpOnly;
    private String name;
    private String path;
    private String sameSite;
    private boolean secure;
    private boolean session;
    private String storeId;
    private String value;
}

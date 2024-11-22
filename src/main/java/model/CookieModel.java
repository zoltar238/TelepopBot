package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CookieModel {
    public String name;
    public String value;
    public String url;
    public String domain;
    public String path;
    public Long expires;
    public Boolean httpOnly;
    public Boolean secure;
    public String sameSite;
}

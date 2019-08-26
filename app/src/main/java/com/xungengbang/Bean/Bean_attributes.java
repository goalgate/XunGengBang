package com.xungengbang.Bean;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * 当前类注释:
 *
 * @author wzw
 * @date 2019/8/16 17:50
 */

@Root(name = "cas:attributes",strict = false)
public class Bean_attributes {
    @Element(name = "cas:credentialType")
    public String credentialType;

    @Element(name = "cas:zhiwu")
    public String zhiwu;

    @Element(name = "cas:isFromNewLogin")
    public String isFromNewLogin;

    @Element(name = "cas:authenticationDate")
    public String authenticationDate;

    @Element(name = "cas:user_id")
    public String user_id;

    @Element(name = "cas:authenticationMethod")
    public String authenticationMethod;

    @Element(name = "cas:successfulAuthenticationHandlers")
    public String successfulAuthenticationHandlers;

    @Element(name = "cas:real_name")
    public String real_name;

    @Element(name = "cas:longTermAuthenticationRequestTokenUsed")
    public String longTermAuthenticationRequestTokenUsed;

    @Element(name = "cas:comp_id")
    public String comp_id;

    @Element(name = "cas:authentication")
    public String authentication;

}

package com.xungengbang.Bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * 当前类注释:
 *
 * @author wzw
 * @date 2019/8/16 17:47
 */

@Root(name = "cas:authenticationSuccess",strict = false)
public class Bean_authenticationSuccess {

    @Element(name = "cas:user")
    public String user;

    @Element(name = "cas:attributes")
    public Bean_attributes attributes;

}

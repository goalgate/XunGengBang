package com.xungengbang.Bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * 当前类注释:
 *
 * @author wzw
 * @date 2019/8/16 17:35
 */
@Root(name = "cas:serviceResponse",strict = false)
public class Bean_serviceResponse {

    @Element(name = "cas:authenticationSuccess")
    public Bean_authenticationSuccess authenticationSuccess;


}

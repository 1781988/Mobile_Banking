package com.mobilebanking.platform.common.web;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        // 不直接信任客户端可伪造的 X-Forwarded-For。部署在反向代理后时，
        // 由 Spring 的 ForwardedHeaderFilter 在可信边界内改写 remoteAddr。
        return request.getRemoteAddr();
    }
}

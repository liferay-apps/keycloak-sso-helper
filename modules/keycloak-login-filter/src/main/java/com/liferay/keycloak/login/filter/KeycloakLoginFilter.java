package com.liferay.keycloak.login.filter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProviderRegistry;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectServiceHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Vitaliy Koshelenko
 */
@Component(
		immediate = true,
		property = {
				"servlet-context-name=",
				"servlet-filter-name=Keycloak Login Filter",
				"url-pattern=/c/portal/login"
		},
		service = Filter.class
)
public class KeycloakLoginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
						 FilterChain filterChain) throws IOException, ServletException {
		try {
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;

			//Get OpenId Providers
			Collection<String> openIdConnectProviderNames =
					openIdConnectProviderRegistry.getOpenIdConnectProviderNames();
			if (openIdConnectProviderNames == null || openIdConnectProviderNames.isEmpty()) {
				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}

			// Get first OpenID Provider
			String openIdConnectProviderName = openIdConnectProviderNames.iterator().next();

			// Request Provider's authentication
			openIdConnectServiceHandler.requestAuthentication(openIdConnectProviderName, request, response);

		} catch (Exception e) {
			_log.error("Error in KeycloakLoginFilter: " + e.getMessage(), e);
		} finally {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	@Override
	public void destroy() {
	}

	@Reference
	private OpenIdConnectProviderRegistry openIdConnectProviderRegistry;
	@Reference
	private OpenIdConnectServiceHandler openIdConnectServiceHandler;

	private static final Log _log = LogFactoryUtil.getLog(KeycloakLoginFilter.class);
}

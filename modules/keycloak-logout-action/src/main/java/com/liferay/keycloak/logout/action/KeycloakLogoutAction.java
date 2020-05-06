package com.liferay.keycloak.logout.action;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PrefsProps;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProvider;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProviderRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @author Vitaliy Koshelenko
 */
@Component(
		immediate = true,
		property = "key=logout.events.post",
		service = LifecycleAction.class
)
public class KeycloakLogoutAction implements LifecycleAction {

	@SuppressWarnings("unchecked")
	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {

		try {
			HttpServletRequest request = lifecycleEvent.getRequest();
			HttpServletResponse response = lifecycleEvent.getResponse();

			Collection<String> openIdConnectProviderNames =
					openIdConnectProviderRegistry.getOpenIdConnectProviderNames();
			if (openIdConnectProviderNames == null || openIdConnectProviderNames.isEmpty()) {
				_log.warn("No OpenID Connect Providers found.");
				return;
			}

			String openIdConnectProviderName = openIdConnectProviderNames.iterator().next();
			OpenIdConnectProvider openIdConnectProvider =
					openIdConnectProviderRegistry.getOpenIdConnectProvider(openIdConnectProviderName);
			Object oidcProviderMetadata = openIdConnectProvider.getOIDCProviderMetadata();
			String oidcJson = oidcProviderMetadata.toString();
			JSONObject oidcJsonObject = JSONFactoryUtil.createJSONObject(oidcJson);
			Object authEndpoint = oidcJsonObject.get("authorization_endpoint");
			String authEndpointUrl = authEndpoint.toString();
			String logoutEndpoint = StringUtil.replaceLast(authEndpointUrl, "/auth", "/logout");
			String redirectUri = getRedirectUrl(request);
			String logoutUrl = logoutEndpoint + "?redirect_uri=" + redirectUri;
			response.sendRedirect(logoutUrl);

		} catch (Exception e) {
			_log.error("Error in KeycloakLogoutPostAction: " + e.getMessage(), e);
		}
	}

	private String getRedirectUrl(HttpServletRequest request) {
		String portalURL = portal.getPortalURL(request);
		long companyId = portal.getCompanyId(request);
		PortletPreferences preferences = prefsProps.getPreferences(companyId);
		String logoutPath =  prefsProps.getString(preferences, PropsKeys.DEFAULT_LOGOUT_PAGE_PATH);
		return portalURL + logoutPath;
	}

	@Reference
	private Portal portal;
	@Reference
	private PrefsProps prefsProps;
	@Reference
	private OpenIdConnectProviderRegistry openIdConnectProviderRegistry;

	private static final Log _log = LogFactoryUtil.getLog(KeycloakLogoutAction.class);
}

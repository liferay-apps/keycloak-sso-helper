# Keycloak SSO Helper

## Overview

Liferay can be connected to the Keycloak server using OpenID.
This article describes the required configuration steps:
[https://lifedev-solutions.blogspot.com/2019/10/liferay-keycloak-integration-using.html](https://lifedev-solutions.blogspot.com/2019/10/liferay-keycloak-integration-using.html)

However, such solution are two limitations:
- Keycloak can't be set as a default authorization mechanism;
- Single Logout (SLO) is not working.

[Here](https://lifedev-solutions.blogspot.com/2020/03/liferay-keycloak-integration-sso-and.html) explained how to overcome these issues.

This repository contains modules, which customize Keycloak integration in order to support features mentioned above.

## Modules

###keycloak-login-filter

This is a servlet filter, which intercepts `/c/portal/login` URLs and performs the OpenId Connect Provider authorization.
Once user hits the "Sign In" link - he should be redirected to Keycloak Sign In form automatically.

###keycloak-logout-action

This is a `logout.events.post` LifecycleAction, which is invoked after logout. 
It makes a request to the OpenId Connect Provider logout URL, and then redirects to Liferay's default logout page path.
Once user hits the "Sign Out" link - he should be signed out in Keycloak automatically.

## Deployment 

Deploy `keycloak-login-filter` and `keycloak-logout-action` modules with Gradle.

Liferay version: 7.3.1 CE GA2

## Future Plans

As OpenId Connect Provider mechanism is used for SSO, this tool can be applied for any SSO provider, not only Keycloak.

## Limitations

Liferay's SSO OpenId Connect Provider does not have a configuration option for the logout URL. That's why the following
trick is used to obtain the logout URL:
    
    String logoutEndpoint = StringUtil.replaceLast(authEndpointUrl, "/auth", "/logout");
    
It works for Keycloak currently, but may not work for other SSO providers, and may potentially change in future.
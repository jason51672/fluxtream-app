package com.fluxtream;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.WordUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.fluxtream.utils.DesEncrypter;
import com.google.api.client.http.LowLevelHttpRequest;

public class Configuration implements InitializingBean {

    Logger logger = Logger.getLogger(Configuration.class);

	private DesEncrypter encrypter;
	
	public PropertiesConfiguration commonProperties;
	
	public PropertiesConfiguration bodytrackProperties;
	
	public PropertiesConfiguration targetEnvironmentProps;
	
	public PropertiesConfiguration connectors;
	
	public PropertiesConfiguration oauth;

    public PropertiesConfiguration lastCommitProperties;

    private Map<String,String> countries;
	
	private Map<String,String> countryCodes;

    public Map<String,String> bodytrackToFluxtreamConnectorNames;

    public void setLastCommitProperties(PropertiesConfiguration properties) throws IOException {
        this.lastCommitProperties = properties;
    }

    public void setCommonProperties(PropertiesConfiguration properties) throws IOException {
		this.commonProperties = properties;
	}
	
	public void setTargetEnvProperties(PropertiesConfiguration properties) throws IOException {
		this.targetEnvironmentProps = properties;
	}
	
	public void setConnectorsProperties(PropertiesConfiguration properties) throws IOException {
		this.connectors = properties;
	}
	
	public void setOauthProperties(PropertiesConfiguration properties) throws IOException {
		this.oauth = properties;
	}
	
	public void setBodytrackProperties(PropertiesConfiguration properties) throws IOException {
        final Iterator<String> keys = properties.getKeys();
        bodytrackToFluxtreamConnectorNames = new ConcurrentHashMap<String,String>();
        while(keys.hasNext()) {
            String key = keys.next();
            if (key.indexOf(".dev_nickname")!=-1) {
                bodytrackToFluxtreamConnectorNames.put(properties.getString(key),
                                                       key.substring(0, key.indexOf(".")));
            }
        }
		this.bodytrackProperties = properties;
	}
	
	public void setCountries(Properties properties) throws IOException {
		countries = new ConcurrentHashMap<String,String>();
		countryCodes = new ConcurrentHashMap<String,String>();
		for(Object key:properties.keySet()) {
			String code = (String) key;
			String countryName = properties.getProperty(code);
			String capitalizedCountryName = WordUtils.capitalize(countryName.toLowerCase());
			String upperCaseCountryCode = code.toUpperCase();
			countries.put(upperCaseCountryCode, capitalizedCountryName);
			countryCodes.put(capitalizedCountryName, upperCaseCountryCode);
		}
	}
	
	public String encrypt(String s) {
		return encrypter.encrypt(s);
	}
	
	public String decrypt(String s) {
		return encrypter.decrypt(s);
	}

    public String get(String key) {
        String property = getAsString(commonProperties, key);
        if (property==null)
            property = getAsString(lastCommitProperties, key);
        if (property==null)
            property = getAsString(targetEnvironmentProps, key);
        if (property==null)
            property = getAsString(oauth, key);;
        if (property==null)
            property = getAsString(connectors, key);;
        if (property==null)
            property = getAsString(bodytrackProperties, key);;
        if (property!=null) return property.trim();
        return property;
    }

    private String getAsString(PropertiesConfiguration properties, String key) {
        final Object property = properties.getProperty(key);
        if (property==null)
            return null;
        if (!(property instanceof String)) {
            final String message = "Property " + key + " was supposed to be a String, found " + property.getClass();
            logger.error(message);
            System.out.println(message);
            return "";
        }
        return (String) property;
    }

	public long getInt(String key) {
		return Integer.valueOf(get(key));
	}

    public float getFloat(String key) {
        return Float.valueOf(get(key));
    }

    public HttpClient getHttpClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		return client;
	}

	public void setProxyAuthHeaders(LowLevelHttpRequest request) {
		if (get("proxyUser")==null) return;
		String credentials = get("proxyUser")+":"+get("proxyPassword");
		String encodedPassword = new String(Base64.encodeBase64(credentials.getBytes()));
		request.addHeader("Proxy-Authorization", "Basic " + encodedPassword);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.encrypter = new DesEncrypter(get("crypto"));
	}

	public String getCountry(String geo_country_code) {
		return countries.get(geo_country_code.toUpperCase());
	}

	public String getCountryCode(String country) {
		return countryCodes.get(country);
	}
	
}

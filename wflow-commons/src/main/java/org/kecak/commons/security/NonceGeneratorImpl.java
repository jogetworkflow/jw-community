/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kecak.commons.security;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasypt.salt.RandomSaltGenerator;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.NonceGenerator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;


public class NonceGeneratorImpl implements NonceGenerator {

    private Cache cache;

    public Cache getCache() {
        return this.cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public String generateNonce(String[] attributes, int lifepanHour) {
        String nonce = this.getRandomNonce();
        String cacheKey = this.getCacheKey(nonce);
        Element element = new Element(cacheKey, attributes);
        element.setEternal(false);
        element.setTimeToLive(lifepanHour * 60 * 60);
        this.cache.put(element);
        return nonce;
    }

    public boolean verifyNonce(String nonce, String[] attributes) {
        String cacheKey = this.getCacheKey(nonce);
        Element element = this.cache.get(cacheKey);
        if (element != null) {
            String[] cacheAttributes = (String[]) element.getObjectValue();
            boolean valid = true;
            if (cacheAttributes != null && attributes != null && cacheAttributes.length == attributes.length) {
                for (int i = 0; i < cacheAttributes.length; ++i) {
                    if (cacheAttributes[i].equals(attributes[i])) continue;
                    valid = false;
                    break;
                }
            }
            return valid;
        }
        return false;
    }

    protected String getRandomNonce() {
        RandomSaltGenerator g = new RandomSaltGenerator();
        byte[] b = g.generateSalt(10);
        try {
            String nonce = new String(b, "UTF-8");
            nonce = URLEncoder.encode(nonce, "UTF-8");
            return nonce;
        } catch (Exception e) {
            return "";
        }
    }

    protected String getCacheKey(String nonce) {
        HttpSession session;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String sessionId = "";
        if (request != null && (session = request.getSession()) != null) {
            sessionId = session.getId();
        }
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "_NonceGenerator_" + nonce + "_" + sessionId;
        return cacheKey;
    }

}

package org.esupportail.helpdesk.services.i18n;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.esupportail.commons.aop.cache.SessionCache;
import org.esupportail.commons.services.i18n.I18nUtils;

public class ReloadableResourceBundleMessageSource extends
        org.springframework.context.support.ReloadableResourceBundleMessageSource implements Map<String, String> {

    @SessionCache
    public Map<String, String> getStrings(Locale locale) {
        Map<String, String> ret = new HashMap<String, String>();
        PropertiesHolder propertiesHolder = getMergedProperties(locale);
        Properties properties = propertiesHolder.getProperties();
        for (Object key : properties.keySet()) {
			ret.put((String)key, (String)properties.get(key));
        }
        return ret;
    }

    @Override
    public int size() {
        return getStrings(I18nUtils.getDefaultLocale()).size();
    }

    @Override
    public boolean isEmpty() {
        return getStrings(I18nUtils.getDefaultLocale()).isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getStrings(I18nUtils.getDefaultLocale()).containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getStrings(I18nUtils.getDefaultLocale()).containsValue(value);
    }

    @Override
    public String get(Object key) {
        return getStrings(I18nUtils.getDefaultLocale()).get(key);
    }

    @Override
    public String put(String key, String value) {
        return getStrings(I18nUtils.getDefaultLocale()).put(key, value);
    }

    @Override
    public String remove(Object key) {
        return getStrings(I18nUtils.getDefaultLocale()).remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        getStrings(I18nUtils.getDefaultLocale()).putAll(m);
    }

    @Override
    public void clear() {
        getStrings(I18nUtils.getDefaultLocale()).clear();
    }

    @Override
    public Set<String> keySet() {
        return getStrings(I18nUtils.getDefaultLocale()).keySet();
    }

    @Override
    public Collection<String> values() {
        return getStrings(I18nUtils.getDefaultLocale()).values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return getStrings(I18nUtils.getDefaultLocale()).entrySet();
    }

}

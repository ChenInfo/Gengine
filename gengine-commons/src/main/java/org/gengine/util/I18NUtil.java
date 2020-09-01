
package org.gengine.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Utility class providing methods to access the Locale of the current thread and to get
 * Localised strings.
 *
 */
public class I18NUtil
{
    /**
     * Thread-local containing the general Locale for the current thread
     */
    private static ThreadLocal<Locale> threadLocale = new ThreadLocal<Locale>();

    /**
     * Thread-local containing the content Locale for for the current thread.  This
     * can be used for content and property filtering.
     */
    private static ThreadLocal<Locale> threadContentLocale = new ThreadLocal<Locale>();
    private static ThreadLocal<Locale> threadContentLocaleLang = new ThreadLocal<Locale>();

    /**
     * List of registered bundles
     */
    private static Set<String> resouceBundleBaseNames = new HashSet<String>();

    /**
     * Map of loaded bundles by Locale
     */
    private static Map<Locale, Set<String>> loadedResourceBundles = new HashMap<Locale, Set<String>>();

    /**
     * Map of cached messaged by Locale
     */
    private static Map<Locale, Map<String, String>> cachedMessages = new HashMap<Locale, Map<String, String>>();

    /**
     * Lock objects
     */
    private static ReadWriteLock lock = new ReentrantReadWriteLock();
    private static Lock readLock = lock.readLock();
    private static Lock writeLock = lock.writeLock();

    /**
     * Set the locale for the current thread.
     *
     * @param locale    the locale
     */
    public static void setLocale(Locale locale)
    {
        threadLocale.set(locale);
        threadContentLocaleLang.set(null);
    }

    /**
     * Get the general local for the current thread, will revert to the default locale if none
     * specified for this thread.
     *
     * @return  the general locale
     */
    public static Locale getLocale()
    {
        Locale locale = threadLocale.get();
        if (locale == null)
        {
            // Get the default locale
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Get the general local for the current thread.<br/>
     * This will revert <tt>null</tt> if no value has been defined.
     *
     * @return  Returns the general locale
     */
    public static Locale getLocaleOrNull()
    {
        return threadLocale.get();
    }

    /**
     * Set the <b>content locale</b> for the current thread.
     *
     * @param locale    the content locale
     */
    public static void setContentLocale(Locale locale)
    {
        threadContentLocale.set(locale);
        threadContentLocaleLang.set(null);
    }

    /**
     * Get the content local for the current thread.<br/>
     * This will revert to {@link #getLocale()} if no value has been defined.
     *
     * @return  Returns the content locale
     */
    public static Locale getContentLocale()
    {
        Locale locale = threadContentLocale.get();
        if (locale == null)
        {
            // Revert to the normal locale
            locale = getLocale();
        }
        return locale;
    }

    /**
     * Get the content local language only (ignore the country and variant) for the current thread.<br/>
     * This will revert to {@link #getLocale()} if no value has been defined.
     *
     * @return  Returns the content locale language
     */
    public static Locale getContentLocaleLang()
    {
        Locale locale = threadContentLocaleLang.get();
        if (locale == null)
        {
            if (threadContentLocale.get() != null)
            {
                locale = new Locale(threadContentLocale.get().getLanguage());
            }
            else
            {
                // Revert to the normal locale lang
                locale = new Locale(getLocale().getLanguage());
            }
            threadContentLocaleLang.set(locale);
        }
        return locale;
    }

    /**
     * Get the content local for the current thread.<br/>
     * This will revert <tt>null</tt> if no value has been defined.
     *
     * @return  Returns the content locale
     */
    public static Locale getContentLocaleOrNull()
    {
        return threadContentLocale.get();
    }

    /**
     * Searches for the nearest locale from the available options.  To match any locale, pass in
     * <tt>null</tt>.
     *
     * @param templateLocale the template to search for or <tt>null</tt> to match any locale
     * @param options the available locales to search from
     * @return Returns the best match from the available options, or the <tt>null</tt> if
     *      all matches fail
     */
    public static Locale getNearestLocale(Locale templateLocale, Set<Locale> options)
    {
        if (options.isEmpty())                          // No point if there are no options
        {
            return null;
        }
        else if (templateLocale == null)
        {
            for (Locale locale : options)
            {
                return locale;
            }
        }
        else if (options.contains(templateLocale))      // First see if there is an exact match
        {
            return templateLocale;
        }
        // make a copy of the set
        Set<Locale> remaining = new HashSet<Locale>(options);

        // eliminate those without matching languages
        Locale lastMatchingOption = null;
        String templateLanguage = templateLocale.getLanguage();
        if (templateLanguage != null && !templateLanguage.equals(""))
        {
            Iterator<Locale> iterator = remaining.iterator();
            while (iterator.hasNext())
            {
                Locale option = iterator.next();
                if (option != null && !templateLanguage.equals(option.getLanguage()))
                {
                    iterator.remove();                  // It doesn't match, so remove
                }
                else
                {
                    lastMatchingOption = option;       // Keep a record of the last match
                }
            }
        }
        if (remaining.isEmpty())
        {
            return null;
        }
        else if (remaining.size() == 1 && lastMatchingOption != null)
        {
            return lastMatchingOption;
        }

        // eliminate those without matching country codes
        lastMatchingOption = null;
        String templateCountry = templateLocale.getCountry();
        if (templateCountry != null && !templateCountry.equals(""))
        {
            Iterator<Locale> iterator = remaining.iterator();
            while (iterator.hasNext())
            {
                Locale option = iterator.next();
                if (option != null && !templateCountry.equals(option.getCountry()))
                {
                    // It doesn't match language - remove
                    // Don't remove the iterator. If it matches a language but not the country, returns any matched language
                    // iterator.remove();
                }
                else
                {
                    lastMatchingOption = option;       // Keep a record of the last match
                }
            }
        }
        if (remaining.size() == 1 && lastMatchingOption != null)
        {
            return lastMatchingOption;
        }
        else
        {
            // We have done an earlier equality check, so there isn't a matching variant
            // Also, we know that there are multiple options at this point, either of which will do.

            // This gets any country match (there will be worse matches so we take the last the country match)
            if(lastMatchingOption != null)
            {
                return lastMatchingOption;
            }
            else
            {
                for (Locale locale : remaining)
                {
                    return locale;
                }
            }
        }
        // The logic guarantees that this code can't be called
        throw new RuntimeException("Logic should not allow code to get here.");
    }

    /**
     * Factory method to create a Locale from a <tt>lang_country_variant</tt> string.
     *
     * @param localeStr e.g. fr_FR
     * @return Returns the locale instance, or the {@link Locale#getDefault() default} if the
     *      string is invalid
     */
    public static Locale parseLocale(String localeStr)
    {
        if (localeStr == null)
        {
            return null;
        }
        Locale locale = Locale.getDefault();

        StringTokenizer t = new StringTokenizer(localeStr.toLowerCase(), "_-");
        int tokens = t.countTokens();
        if (tokens == 1)
        {
           locale = new Locale(t.nextToken());
        }
        else if (tokens == 2)
        {
           locale = new Locale(t.nextToken(), t.nextToken());
        }
        else if (tokens == 3)
        {
           locale = new Locale(t.nextToken(), t.nextToken(), t.nextToken());
        }

        return locale;
    }

    /**
     * Register a resource bundle.
     * <p>
     * This should be the bundle base name eg, spring-surf.messages.errors
     * <p>
     * Once registered the messges will be available via getMessage
     *
     * @param bundleBaseName    the bundle base name
     */
    public static void registerResourceBundle(String bundleBaseName)
    {
        try
        {
            writeLock.lock();
            resouceBundleBaseNames.add(bundleBaseName);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Get message from registered resource bundle.
     *
     * @param messageKey    message key
     * @return              localised message string, null if not found
     */
    public static String getMessage(String messageKey)
    {
        return getMessage(messageKey, getLocale());
    }

    /**
     * Get a localised message string
     *
     * @param messageKey        the message key
     * @param locale            override the current locale
     * @return                  the localised message string, null if not found
     */
    public static String getMessage(String messageKey, Locale locale)
    {
        String message = null;
        Map<String, String> props = getLocaleProperties(locale);
        if (props != null)
        {
            message = props.get(messageKey);
        }
        return message;
    }

    /**
     * Get a localised message string, parameterized using standard MessageFormatter.
     *
     * @param messageKey    message key
     * @param params        format parameters
     * @return              the localised string, null if not found
     */
    public static String getMessage(String messageKey, Object ... params)
    {
        return getMessage(messageKey, getLocale(), params);
    }

    /**
     * Get a localised message string, parameterized using standard MessageFormatter.
     *
     * @param messageKey        the message key
     * @param locale            override current locale
     * @param params            the localised message string
     * @return                  the localaised string, null if not found
     */
    public static String getMessage(String messageKey, Locale locale, Object ... params)
    {
        String message = getMessage(messageKey, locale);
        if (message != null && params != null)
        {
            message = MessageFormat.format(message, params);
        }
        return message;
    }

    /**
     * @return the map of all available messages for the current locale
     */
    public static Map<String, String> getAllMessages()
    {
        return getLocaleProperties(getLocale());
    }

    /**
     * @return the map of all available messages for the specified locale
     */
    public static Map<String, String> getAllMessages(Locale locale)
    {
        return getLocaleProperties(locale);
    }

    /**
     * Get the messages for a locale.
     * <p>
     * Will use cache where available otherwise will load into cache from bundles.
     *
     * @param locale    the locale
     * @return          message map
     */
    private static Map<String, String> getLocaleProperties(Locale locale)
    {
        Set<String> loadedBundles = null;
        Map<String, String> props = null;
        int loadedBundleCount = 0;
        try
        {
            readLock.lock();
            loadedBundles = loadedResourceBundles.get(locale);
            props = cachedMessages.get(locale);
            loadedBundleCount = resouceBundleBaseNames.size();
        }
        finally
        {
            readLock.unlock();
        }

        if (loadedBundles == null)
        {
            try
            {
                writeLock.lock();
                loadedBundles = new HashSet<String>();
                loadedResourceBundles.put(locale, loadedBundles);
            }
            finally
            {
                writeLock.unlock();
            }
        }

        if (props == null)
        {
            try
            {
                writeLock.lock();
                props = new HashMap<String, String>();
                cachedMessages.put(locale, props);
            }
            finally
            {
                writeLock.unlock();
            }
        }

        if (loadedBundles.size() != loadedBundleCount)
        {
            try
            {
                writeLock.lock();
                for (String resourceBundleBaseName : resouceBundleBaseNames)
                {
                    if (loadedBundles.contains(resourceBundleBaseName) == false)
                    {
                        ResourceBundle resourcebundle = ResourceBundle.getBundle(resourceBundleBaseName, locale);
                        Enumeration<String> enumKeys = resourcebundle.getKeys();
                        while (enumKeys.hasMoreElements() == true)
                        {
                            String key = enumKeys.nextElement();
                            props.put(key, resourcebundle.getString(key));
                        }
                        loadedBundles.add(resourceBundleBaseName);
                    }
                }
            }
            finally
            {
                writeLock.unlock();
            }
        }

        return props;
    }
}

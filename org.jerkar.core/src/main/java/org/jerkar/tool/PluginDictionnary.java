package org.jerkar.tool;

import java.lang.reflect.Modifier;
import java.util.*;

import org.jerkar.api.java.JkClassLoader;
import org.jerkar.api.utils.JkUtilsString;

/**
 * Contains the Plugin description for all concrete plugin classes extending a
 * given base class.
 * <p>
 * Jerkar offers a very simple, yet powerful, plugin mechanism.<br/>
 * Basically it offers to discover every classes in the classpath that inherit
 * to a given class and that respect a certain naming convention.<br/>
 * <p>
 * The convention naming is as follow : The class simple name should be prefixed
 * by the simple name of the plugin base class.<br/>
 * For example, a plugin class for
 * <code>or.jerkar.java.build.JkBuildPlugin</code> class must be named
 * 'my.package.JkJavaBuildPluginXxxxx.class' to be discovered :Xxxxx will be its
 * short name, while my.package.JkJavaBuildPluginXxxxx will be its full name.
 * 
 * @param <T>
 *            The plugin base class.
 * @author Jerome Angibaud
 * 
 * @see {@link JkPluginDescription}
 */
final class PluginDictionnary<T> {

    private static final Map<Class<?>, Set<Class<?>>> CACHE = new HashMap<Class<?>, Set<Class<?>>>();

    /**
     * Creates a {@link PluginDictionnary} for the specified extension points.
     * That means, this instance will refer to all plugin extending the
     * specified extension point in the parameter templateClass.
     */
    public static <T> PluginDictionnary<T> of(Class<T> templateClass) {
        final PluginDictionnary<T> result = new PluginDictionnary<T>(templateClass);
        if (CACHE.containsKey(templateClass)) {
            final Set<Class<?>> pluginClasses = CACHE.get(templateClass);
            result.plugins = toPluginSet(templateClass, pluginClasses);
        }
        return result;
    }

    private Set<JkPluginDescription<T>> plugins;

    private final Class<T> templateClass;

    private PluginDictionnary(Class<T> extendingClass) {
        super();
        this.templateClass = extendingClass;
    }

    /**
     * Returns all the plugins present in classpath for this template class.
     */
    public Set<JkPluginDescription<T>> getAll() {
        if (plugins == null) {
            synchronized (this) {
                final Set<JkPluginDescription<T>> result = loadAllPlugins(templateClass);
                this.plugins = Collections.unmodifiableSet(result);
            }
        }
        return this.plugins;
    }

    /**
     * Returns the plugin having a full name equals to the specified name. If
     * not found, returns the plugin having a short name equals to the specified
     * name. Note that the short name is capitalized for you so using
     * "myPluging" or "MyPlugin" is equal. If not found, returns
     * <code>null</code>.
     */
    public JkPluginDescription<T> loadByName(String name) {
        if (!name.contains(".")) {
            final JkPluginDescription<T> result = loadPluginHavingShortName(templateClass,
                    JkUtilsString.capitalize(name));
            if (result != null) {
                return result;
            }
        }
        return loadPluginsHavingLongName(templateClass, name);
    }

    public JkPluginDescription<T> loadByNameOrFail(String name) {
        final JkPluginDescription<T> result = loadByName(name);
        if (result == null) {
            throw new IllegalArgumentException("No class found having name "
                    + simpleClassName(templateClass, name) + " for plugin '" + name + "'.");
        }
        return result;
    }

    private static String simpleClassName(Class<?> templateClass, String pluginName) {
        return templateClass.getSimpleName() + JkUtilsString.capitalize(pluginName);
    }

    @Override
    public String toString() {
        if (this.plugins == null) {
            return "Not loaded (template class = " + this.templateClass + ")";
        }
        return this.plugins.toString();
    }

    private static <T> Set<JkPluginDescription<T>> loadAllPlugins(Class<T> templateClass) {
        final String nameSuffix = templateClass.getSimpleName();
        return loadPlugins(templateClass, "**/" + nameSuffix + "*", "**/*$" + nameSuffix + "*");
    }

    private static <T> JkPluginDescription<T> loadPluginHavingShortName(Class<T> templateClass,
            String shortName) {
        final String simpleName = simpleClassName(templateClass, shortName);
        final Set<JkPluginDescription<T>> set = loadPlugins(templateClass, "**/" + simpleName);
        set.addAll(loadPlugins(templateClass, "**/*$" + simpleName));
        if (set.size() > 1) {
            throw new JkException("Several plugin have the same short name : '" + shortName
                    + "'. Please disambiguate with using plugin long name (full class name)."
                    + " Following plugins have same shortName : " + set);
        }
        if (set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    private static <T> JkPluginDescription<T> loadPluginsHavingLongName(Class<T> templateClass,
            String longName) {
        final Class<? extends T> pluginClass = JkClassLoader.current().loadIfExist(longName);
        if (pluginClass == null) {
            return null;
        }
        return new JkPluginDescription<T>(templateClass, pluginClass);
    }

    private static <T> Set<JkPluginDescription<T>> loadPlugins(Class<T> templateClass,
            String... patterns) {
        final Set<Class<?>> matchingClasses = JkClassLoader.of(templateClass).loadClasses(patterns);
        final Set<Class<?>> result = new HashSet<Class<?>>();
        for (final Class<?> candidate : matchingClasses) {
            if (templateClass.isAssignableFrom(candidate)
                    && !Modifier.isAbstract(candidate.getModifiers())
                    && !candidate.equals(templateClass)) {
                result.add(candidate);
            }
        }
        return toPluginSet(templateClass, result);
    }

    @SuppressWarnings("unchecked")
    private static <T> Set<JkPluginDescription<T>> toPluginSet(Class<T> extendingClass,
            Iterable<Class<?>> classes) {
        final Set<JkPluginDescription<T>> result = new TreeSet<JkPluginDescription<T>>();
        for (final Class<?> clazz : classes) {
            result.add(new JkPluginDescription<T>(extendingClass, (Class<? extends T>) clazz));
        }
        return result;
    }

    /**
     * Give the description of a plugin class as its name, its purpose and its
     * base class.
     * 
     * @author Jerome Angibaud
     * @param <T>
     */
    public static class JkPluginDescription<T> implements Comparable<JkPluginDescription<T>> {

        private static String shortName(Class<?> extendingClass, Class<?> clazz) {
            return JkUtilsString.uncapitalize(JkUtilsString.substringAfterFirst(clazz.getSimpleName(),
                    extendingClass.getSimpleName()));
        }

        private static String longName(Class<?> extendingClass, Class<?> clazz) {
            return clazz.getName();
        }

        /**
         * Returns all <code>JkPlugins</code> instances declared as field in the
         * specified instance. It includes fields declared in the specified
         * instance class and the ones declared in its super classes.
         */
        public static List<JkPluginDescription<?>> declaredAsField(JkBuild hostingInstance) {
            final List<JkPluginDescription<?>> result = new LinkedList<JkPluginDescription<?>>();
            final List<Class<Object>> templateClasses = hostingInstance.pluginTemplateClasses();
            for (final Class<Object> clazz : templateClasses) {
                final PluginDictionnary<Object> plugins = PluginDictionnary.of(clazz);
                result.addAll(plugins.getAll());
            }
            return result;
        }

        private final String shortName;

        private final String fullName;

        private final Class<T> templateClass;

        private final Class<? extends T> clazz;

        public JkPluginDescription(Class<T> templateClass, Class<? extends T> clazz) {
            super();
            this.templateClass = templateClass;
            this.shortName = shortName(templateClass, clazz);
            this.fullName = longName(templateClass, clazz);
            this.clazz = clazz;
        }

        public String shortName() {
            return this.shortName;
        }

        public String fullName() {
            return this.fullName;
        }

        public Class<T> templateClass() {
            return templateClass;
        }

        public Class<? extends T> pluginClass() {
            return clazz;
        }

        public List<String> explanation() {
            if (this.clazz.getAnnotation(JkDoc.class) == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(this.clazz.getAnnotation(JkDoc.class).value());
        }

        @Override
        public String toString() {
            return "name=" + this.shortName + "(" + this.fullName + ")";
        }

        @Override
        public int compareTo(JkPluginDescription<T> o) {
            return this.shortName.compareTo(o.shortName);
        }
    }

}

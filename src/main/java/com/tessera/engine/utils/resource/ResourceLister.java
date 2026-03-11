package com.tessera.engine.utils.resource;

import com.tessera.Main;
import com.tessera.window.utils.preformance.Stopwatch;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static com.tessera.engine.utils.resource.ResourceLoader.FILE_SEPARATOR;

/**
 * list resources available from the classpath @ *
 */
public class ResourceLister {

    private static String[] resourceList;
    /**
     * We have to get the directories from the resource folder to add to our classpath list
     */
    private static final String[] INIT_RESOURCE_DIRECTORIES = {"assets", "data", "builtin"};
    /**
     * For our IDE to load classpath resources from the filesystem
     */
    private static final String INIT_LOCAL_PREFIX = "/target/classes/";

    private static String getPathToJar() throws URISyntaxException {
        // Get the path of the JAR file
        File jarFile = new File(ResourceLister.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        return jarFile.getPath();
    }

    private static String compileInitRegex(boolean localPrefix, String[] baseDirs) {
        if (baseDirs == null || baseDirs.length == 0) {
            return null;
        }
        StringBuilder patternBuilder = new StringBuilder()
                .append("^"); //Beginning of string

        //If we are running from the IDE
        if (localPrefix) patternBuilder.append(".*").append("(").append(Pattern.quote(INIT_LOCAL_PREFIX)).append(")");

        //The base directory
        patternBuilder.append("(");
        for (int i = 0; i < baseDirs.length; i++) {
            patternBuilder.append(Pattern.quote(baseDirs[i]));
            if (i < baseDirs.length - 1) {
                patternBuilder.append("|");
            }
        }
        patternBuilder.append(")");

        //Match the rest as long as it does not end in a slash (We dont want directories because they only show up on the JAR file)
        patternBuilder.append("(.*?)(?<!/)");
        System.out.println(patternBuilder.toString());
        return patternBuilder.toString();
    }

    public static void init() {
        if (resourceList != null) return;
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        // 1. First, try the standard classpath (IDE behavior)
        Pattern idePattern = Pattern.compile(compileInitRegex(true, INIT_RESOURCE_DIRECTORIES));
        List<String> list = new ArrayList<>(_listAllJarfileResources(idePattern));

        boolean isRunningAsJar = list.isEmpty();

        // 2. FAILSAFE: If list is empty, we are in the packaged EXE.
        // Use your getPathToJar() logic directly.
        if (isRunningAsJar) {
            Pattern jarPattern = Pattern.compile(compileInitRegex(false, INIT_RESOURCE_DIRECTORIES));
            try {
                File actualJar = new File(getPathToJar());
                if (actualJar.exists()) {
                    list.addAll(_getResourcesFromJarFile(actualJar, jarPattern));
                }
            } catch (Exception e) {
                System.err.println("Critical: Could not resolve JAR path.");
            }
        }

        // 3. Initialize the array even if empty to prevent the NPE
        resourceList = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            resourceList[i] = list.get(i);

            if (!isRunningAsJar && resourceList[i].contains(INIT_LOCAL_PREFIX)) {
                resourceList[i] = resourceList[i].replaceFirst(".*\\Q" + INIT_LOCAL_PREFIX + "\\E", "");
            }
            resourceList[i] = ResourceLoader.formatPath(resourceList[i]);
        }

        stopwatch.calculateElapsedTime();
        System.out.println("Resource listing init took " + stopwatch.getElapsedSeconds()
                + "s; Running from jar: " + isRunningAsJar + " (Found " + list.size() + " items)");
    }

    private static String regexPattern(String path) {
        path = path.replace("\\", FILE_SEPARATOR);
        //Strip beginning and ending slashes
        if (path.startsWith(FILE_SEPARATOR)) path = path.substring(1);
        if (path.endsWith(FILE_SEPARATOR)) path = path.substring(0, path.length() - 1);

        //A blank path is just /
        if (path.isBlank()) return ".*";
            //We want to get what is after the last slash. If we match with a .*, we will also match the file/folder itself
        else return "\\Q" + FILE_SEPARATOR + path + FILE_SEPARATOR + "\\E(.+)";
    }

    public static String[] listSubResources(String path) {
        init();
        //Get all matching regex patterns in resourceList
        Pattern pattern = Pattern.compile(regexPattern(path));

        List<String> list = new ArrayList<>();
        for (int i = 0; i < resourceList.length; i++) {
            if (pattern.matcher(resourceList[i]).matches()) {
                list.add(resourceList[i]);
            }
        }
        return list.toArray(new String[0]);
    }

    public static String[] listDirectSubResources(String path) {
        init();
        //Get all matching regex patterns in resourceList
        Pattern pattern = Pattern.compile(regexPattern(path));

        //We only want unique values
        HashSet<String> list = new HashSet<>();
        for (int i = 0; i < resourceList.length; i++) {
            if (pattern.matcher(resourceList[i]).matches()) {

                String direct = resourceList[i]
                        .substring(path.length() + 1);//Truncate the base path from the resource path

                int end = direct.indexOf(FILE_SEPARATOR, 1); //Truncate Anything after the first \ (get rid of any subfiles)
                if (end == -1) end = direct.length();
                direct = direct.substring(0, end);

                //System.out.println("DIRECT: " + direct);
                list.add(direct);
            }
        }

        //Add return list
        String[] reval = list.toArray(new String[0]);

        //Add the base path back in
        for (int i = 0; i < reval.length; i++) {
            reval[i] = path + FILE_SEPARATOR + reval[i];
            reval[i] = ResourceLoader.formatPath(reval[i]);
        }
        return reval;
    }

    /**
     * for all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     *
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    private static Collection<String> _listAllJarfileResources(final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (final String element : classPathElements) {
            retval.addAll(_listAllJarfileResources(element, pattern));
        }
        return retval;
    }


    /**
     * Returns jarfile resources directly inside this jarfile and can even detect if a resource is a directory.
     * Pattern pattern = Pattern.compile(".*"); gets all resources
     *
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    private static Collection<JarEntry> _listAllJarfileResourcesAsZip(final Pattern pattern) {
        try (JarFile jarFile = new JarFile(getPathToJar())) {
            final ArrayList<JarEntry> retval = new ArrayList<JarEntry>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (pattern.matcher(entry.getName().replace("\\", FILE_SEPARATOR)).matches()) {
                    retval.add(entry);
                }
            }
            return retval;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static Collection<String> _listAllJarfileResources(final String element, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();

        // Safety: Ignore empty/null strings immediately
        if (element == null || element.trim().isEmpty()) {
            return retval;
        }

        final File file = new File(element);

        // If it doesn't exist on disk, don't even try
        if (!file.exists()) {
            return retval;
        }

        if (file.isDirectory()) {
            retval.addAll(_getResourcesFromDirectory(file, pattern));
        } else {
            // Only try to scan as a Jar if it actually looks like one
            // or at least handle the failure gracefully
            retval.addAll(_getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    private static Collection<String> _getResourcesFromJarFile(final File file, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();

        // The "Try-with-resources" block here is CRITICAL.
        // It catches the error if 'file' is not a valid zip (like the .exe itself)
        try (ZipFile zf = new ZipFile(file)) {
            final Enumeration<? extends ZipEntry> e = zf.entries();
            while (e.hasMoreElements()) {
                final ZipEntry ze = e.nextElement();
                final String fileName = ze.getName();
                if (pattern.matcher(fileName).matches()) {
                    retval.add(fileName);
                }
            }
        } catch (ZipException e) {
            // This is where the fix happens!
            // If it's a file but NOT a zip (like an .exe or .txt), we just skip it.
            System.out.println("Skipping non-zip classpath element: " + file.getName());
        } catch (IOException e) {
            System.err.println("Could not read file: " + file.getPath());
        }

        return retval;
    }

    private static Collection<String> _getResourcesFromDirectory(final File directory, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList) {
            if (file.isDirectory()) {
                retval.addAll(_getResourcesFromDirectory(file, pattern));
            } else {
                try {
                    final String fileName = file.getCanonicalPath().replace("\\", FILE_SEPARATOR); //VERY IMPORTANT. ALL SLASHES MUCH BE BACKWARDS SLASHES

                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        retval.add(fileName);
                    }
                } catch (final IOException e) {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }
}
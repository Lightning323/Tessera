package com.xbuilders.utils.resource;

import com.xbuilders.Main;
import com.xbuilders.window.utils.preformance.Stopwatch;

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

import static com.xbuilders.utils.resource.ResourceLoader.FILE_SEPARATOR;

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
        String jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        return jarPath;
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

    /**
     * When we load resources, if we are running from the IDE, we get something like this
     * C:/Local Files/github/Xbuilders/target/classes/assets/xbuilders/textures/item/pp/crimson_door.png
     * <p>
     * Whereas if we are running from the JAR file, we get something like this
     * assets/xbuilders/textures/item/pp/crimson_door.png
     */
    public static void init() {
        if (resourceList != null) return;
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        //The easiest way to test if we are running inside of a jarfile, is if the results of this are empty
        Pattern pattern = Pattern.compile(compileInitRegex(true, INIT_RESOURCE_DIRECTORIES));
        List<String> list = ResourceLister._listAllJarfileResources(pattern).stream().toList();
        System.out.println("List size: " + list.size());
        boolean isRunningAsJar = list.isEmpty();

        //If we are runing as a jar file, try again
        if (isRunningAsJar) {
            pattern = Pattern.compile(compileInitRegex(false, INIT_RESOURCE_DIRECTORIES));
            list = ResourceLister._listAllJarfileResources(pattern).stream().toList();
        }

        //Add all elements from the list to a string array
        resourceList = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            resourceList[i] = list.get(i);

            //remove the local init prefix
            if (!isRunningAsJar)
                resourceList[i] = resourceList[i].replaceFirst(".*\\Q" + INIT_LOCAL_PREFIX + "\\E", "");

            //Every path has to look like the traditional resource path
            resourceList[i] = ResourceLoader.formatPath(resourceList[i]);
        }

        stopwatch.calculateElapsedTime();
        System.out.println(
                "Resource listing init took " + stopwatch.getElapsedSeconds()
                        + "s; Running from jar: " + isRunningAsJar);
//        for (String s : resourceList) System.out.println(s);
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
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.addAll(_getResourcesFromDirectory(file, pattern));
        } else {
            retval.addAll(_getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    private static Collection<String> _getResourcesFromJarFile(final File file, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        ZipFile zf;
        try {
            zf = new ZipFile(file);
        } catch (final ZipException e) {
            throw new Error(e);
        } catch (final IOException e) {
            throw new Error(e);
        }
        final Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if (accept) {
                retval.add(fileName);
            }
        }
        try {
            zf.close();
        } catch (final IOException e1) {
            throw new Error(e1);
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
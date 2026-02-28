package com.xbuilders.engine.common.resource;

import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.common.utils.LoggingUtils;
import com.xbuilders.engine.common.utils.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.xbuilders.Main.LOGGER;

public class ResourceLoader {
    public ResourceLoader() {
    }

    /**
     * If a path starts with a slash (leading slash) it is an absolute path, otherwise it is a relative path
     * All paths are separated by a forward slash
     */
    public static final String FILE_SEPARATOR = "/";


    protected static String formatPath(String path) {
        path = path.replace("\\", FILE_SEPARATOR);
        if (!path.startsWith(FILE_SEPARATOR)) path = FILE_SEPARATOR + path;
        return path;
    }

    public InputStream getResourceAsStream(String path) {
        path = formatPath(path);
        final InputStream in = getContextClassLoader().getResourceAsStream(path);
        return in == null ? getClass().getResourceAsStream(path) : in;
    }

    public URL getResource(String path) {
        path = formatPath(path);
        URL url = getContextClassLoader().getResource(path);
        return url == null ? getClass().getResource(path) : url;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * https://stackoverflow.com/questions/20105554/is-there-a-way-to-tell-if-a-classpath-resource-is-a-file-or-a-directory
     * Java does not really have a way to determine if a resource is a file or a directory, so we have to get a little creative
     * 1. If there is a file extension, then it is a file, otherwise it is a directory
     *
     * @param path
     * @return
     */
    public boolean isDirectory(String path) {
        path = formatPath(path);

        //If there is a file extension, then it is a file
        if (FileUtils.hasFileExtension(path)) return false;
        else return true;

//        try (
//                InputStream in = getResourceAsStream(path);
//                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
//            String resource;
//
//            while ((resource = br.readLine()) != null) {
//                if (resource.startsWith(FILE_SEPARATOR) || path.endsWith(FILE_SEPARATOR)) resource = path + resource;
//                else resource = path + FILE_SEPARATOR + resource;
//                //If there is at least one valid resource, then it is a directory
//                return getResource(resource) != null;
//            }
//        } catch (IOException e) {
//        }
//        return false;
    }


    public List<String> listResourceFiles(String path) {
        List<String> filenames = new ArrayList<>();
        path = formatPath(path);
//        System.out.println("Resource path: " + path);
        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                if (resource.startsWith(FILE_SEPARATOR) || path.endsWith(FILE_SEPARATOR))
                    resource = path + resource;
                else resource = path + FILE_SEPARATOR + resource;
                filenames.add(resource);
//                System.out.println("\tResource: " + resource);
            }
        } catch (IOException | NullPointerException e) {
//            throw new RuntimeException(e);
        }
//        System.out.println("Found " + filenames.size() + " resources.");
        return filenames;
    }

    public String getName(String path) {
        path = formatPath(path);
        return path.substring(path.lastIndexOf(FILE_SEPARATOR) + 1);
    }


    public byte[] readResource(String path) throws IOException {
        InputStream is = getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + path);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }


    /*



    //Overloading methods with other parameters
     */
    public byte[] readResource(String path1, String path2) throws IOException {
        path1 = formatPath(path1 + FILE_SEPARATOR + path2);
        return readResource(path1);
    }

    public InputStream getResourceAsStream(String path1, String path2) {
        path1 = formatPath(path1 + FILE_SEPARATOR + path2);
        return getResourceAsStream(path1);
    }

    public List<String> getResourceFiles(String path1, String path2) throws IOException {
        path1 = formatPath(path1 + FILE_SEPARATOR + path2);
        return listResourceFiles(path1);
    }

    public byte[] getResourceBytes(String path) throws IOException {
        try (InputStream is = getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int bytesRead;
            try {
                while ((bytesRead = is.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
            } catch (IOException e) {
                LOGGER.info("error", e);
            }
            return buffer.toByteArray();
        }
    }
}

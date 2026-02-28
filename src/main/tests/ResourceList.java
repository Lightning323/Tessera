import com.xbuilders.utils.resource.JarResourceLister;

import java.util.List;

/**
 * list resources available from the classpath @ *
 */
public class ResourceList {


    /**
     * list the resources that match args[0]
     *
     * @param args args[0] is the pattern to match, or list all resources if
     *             there are no args
     */
    public static void main(final String[] args) {
        final List<String> list = JarResourceLister.listAllFiles("\\assets");
        for (final String name : list) {
            System.out.println(name);
        }
    }
}  
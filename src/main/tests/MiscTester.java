import com.xbuilders.utils.BooleanBuffer;
import org.lwjgl.system.MemoryStack;

public class MiscTester {
    private static long referenceTime = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            BooleanBuffer booleanBuffer = new BooleanBuffer(10, stack);
            System.out.println(booleanBuffer.toString());

            booleanBuffer.put(0, true);
            booleanBuffer.put(1, false);
            booleanBuffer.put(2, true);
            booleanBuffer.put(3, false);
            booleanBuffer.put(4, true);
            booleanBuffer.put(5, false);
            booleanBuffer.put(6, true);
            booleanBuffer.put(7, false);
            booleanBuffer.put(8, true);
            booleanBuffer.put(9, false);

            //Print the booleanBuffer
            System.out.println(booleanBuffer.toString());


            booleanBuffer.put(16, false);
        }
    }

}

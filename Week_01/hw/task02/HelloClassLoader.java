package hw.task02;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: zhangyc
 * @date: 2021/1/9
 */
public class HelloClassLoader extends ClassLoader {

    public static void main(String[] args) {
        try {
            Object clazz =  new HelloClassLoader().findClass("Hello.xlass").newInstance();
            clazz.getClass().getMethod("hello").invoke(clazz);

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不违背双亲委派模型，重写此方法即可；否则需要再重写{@link #loadClass loadClass}方法.
     * <p>
     * Finds the class with the specified <a href="#name">binary name</a>.
     * This method should be overridden by class loader implementations that
     * follow the delegation model for loading classes, and will be invoked by
     * the {@link #loadClass loadClass} method after checking the
     * parent class loader for the requested class.
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return The resulting {@code Class} object
     * @throws ClassNotFoundException If the class could not be found
     * @implSpec The default implementation throws {@code ClassNotFoundException}.
     * @since 1.2
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = new byte[0];
        try {
            bytes = file2Bytes(new File(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defineClass(name.split("\\.")[0], bytes, 0, bytes.length);
    }

    private byte[] file2Bytes(File file) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] bytes = new byte[(int) file.length()];
        bufferedInputStream.read(bytes);
        bufferedInputStream.close();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ~bytes[i];
        }
        return bytes;
    }

}

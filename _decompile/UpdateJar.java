import java.io.*;
import java.util.jar.*;
import java.util.zip.*;

public class UpdateJar {
    public static void main(String[] args) throws Exception {
        String jarPath = args[0];
        String clsDir = args[1];
        String entryPrefix = "com/netease/mc/mod/skin/";
        String[] files = {"SkinHandler.class", "SkinHandler$1.class", "SkinHandler$2.class", "SkinHandler$3.class", "SkinHandler$4.class"};

        File jarFile = new File(jarPath);
        File tmpFile = new File(jarPath + ".tmp");

        JarInputStream jis = new JarInputStream(new FileInputStream(jarFile));
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tmpFile));

        ZipEntry entry;
        byte[] buf = new byte[8192];
        while ((entry = jis.getNextEntry()) != null) {
            String name = entry.getName();
            boolean skip = false;
            for (String f : files) {
                if (name.equals(entryPrefix + f)) { skip = true; break; }
            }
            if (skip) continue;

            jos.putNextEntry(new ZipEntry(name));
            int len;
            while ((len = jis.read(buf)) > 0) jos.write(buf, 0, len);
            jos.closeEntry();
        }
        jis.close();

        for (String f : files) {
            File clsFile = new File(clsDir, f);
            byte[] data = new byte[(int) clsFile.length()];
            FileInputStream fis = new FileInputStream(clsFile);
            fis.read(data);
            fis.close();

            ZipEntry newEntry = new ZipEntry(entryPrefix + f);
            newEntry.setMethod(ZipEntry.DEFLATED);
            jos.putNextEntry(newEntry);
            jos.write(data);
            jos.closeEntry();
            System.out.println("added: " + f + " (" + data.length + " bytes)");
        }

        jos.close();

        jarFile.delete();
        tmpFile.renameTo(jarFile);
        System.out.println("done");
    }
}

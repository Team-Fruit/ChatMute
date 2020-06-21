package net.teamfruit.muteplugin;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;
import java.io.*;
import java.util.logging.Level;

public class DataUtils {
    public static final Gson gson = new Gson();

    private static void reportRead(final Exception e, final @Nullable String description) {
        if (description != null)
            Log.log.log(Level.WARNING, "Failed to load " + description + ": ", e);
    }

    private static void reportWrite(final Exception e, final @Nullable String description) {
        if (description != null)
            Log.log.log(Level.WARNING, "Failed to save " + description + ": ", e);
    }

    private static <T> T readStream(final InputStream stream, final Class<T> clazz) throws Exception {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(stream, Charsets.UTF_8));
            return gson.fromJson(reader, clazz);
        } finally {
            closeQuietly(reader);
            closeQuietly(stream);
        }
    }

    public static @Nullable <T> T loadStream(
            final InputStream stream, final Class<T> clazz,
            final @Nullable String description
    ) {
        try {
            return readStream(stream, clazz);
        } catch (final Exception e) {
            reportRead(e, description);
        }
        return null;
    }

    private static <T> boolean writeWriter(final Writer stream, final Class<T> clazz, final T object)
            throws Exception {
        JsonWriter writer = null;
        try {
            writer = new JsonWriter(stream);
            writer.setIndent("  ");
            gson.toJson(object, clazz, writer);
            return true;
        } finally {
            closeQuietly(writer);
            closeQuietly(stream);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (final IOException ioe) {
            // ignore
        }
    }

    private static <T> boolean writeStream(final OutputStream stream, final Class<T> clazz, final T object)
            throws Exception {
        return writeWriter(new OutputStreamWriter(stream, Charsets.UTF_8), clazz, object);
    }

    private static @Nullable <T> String writeString(final Class<T> clazz, final T object)
            throws Exception {
        final StringWriter sbw = new StringWriter();
        if (writeWriter(sbw, clazz, object))
            return sbw.toString();
        return null;
    }

    public static <T> boolean saveStream(
            final OutputStream stream, final Class<T> clazz, final T object,
            final @Nullable String description
    ) {
        try {
            return writeStream(stream, clazz, object);
        } catch (final Exception e) {
            reportWrite(e, description);
        }
        return false;
    }

    public static @Nullable <T> String saveString(
            final Class<T> clazz, final T object,
            final @Nullable String description
    ) {
        try {
            return writeString(clazz, object);
        } catch (final Exception e) {
            reportWrite(e, description);
        }
        return null;
    }

    private static <T> T readFile(final File file, final Class<T> clazz) throws Exception {
        return readStream(new FileInputStream(file), clazz);
    }

    public static @Nullable <T> T loadFile(final File file, final Class<T> clazz, final @Nullable String description) {
        try {
            return readFile(file, clazz);
        } catch (final Exception e) {
            reportRead(e, description);
        }
        return null;
    }

    public static @Nullable <T> T loadFileIfExists(final File file, final Class<T> clazz, final @Nullable String description) {
        if (file.exists())
            return loadFile(file, clazz, description);
        return null;
    }

    private static <T> boolean writeFile(final File file, final Class<T> clazz, final T object) throws Exception {
        return writeStream(new FileOutputStream(file), clazz, object);
    }

    public static <T> boolean saveFile(
            final File file, final Class<T> clazz, final T object,
            final @Nullable String description
    ) {
        try {
            return writeFile(file, clazz, object);
        } catch (final Exception e) {
            reportWrite(e, description);
        }
        return false;
    }
}

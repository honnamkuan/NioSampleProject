package com.th;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by ASUS on 1/17/2016.
 * JAVA NIO Test Class
 */
public class NioTestClass
{
    private static ResourceBundle resourceBundle;
    private static Properties properties;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public NioTestClass()
    {
        if (resourceBundle == null)
        {
            resourceBundle = ResourceBundle.getBundle("config");
            logger.debug("resource bundle loaded: {}", resourceBundle);
        }
        if (properties == null)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource("config.properties");

            Path path = null;
            try
            {
                path = Paths.get(resource.toURI());
                InputStream in = Files.newInputStream(path);
                properties = new Properties();
                properties.load(in);

                logger.debug("property file loaded: {}", properties);


            }
            catch (URISyntaxException e)
            {
                logger.error("URISyntaxException", e);
            }
            catch (IOException e)
            {
                logger.error("IOException", e);
            }


        }

    }

    public static Properties getProperties()
    {
        return properties;
    }

    public static void setProperties(Properties properties)
    {
        NioTestClass.properties = properties;
    }

    public static void main(String... args)
    {
        Logger logger = LoggerFactory.getLogger("");

        NioTestClass nioTestClass = new NioTestClass();

        Properties prop = nioTestClass.getProperties();

        Path homePath = Paths.get(prop.getProperty("home.path"));
        Path relativePath = Paths.get(prop.getProperty("relative.path"));
        Path fullPath = homePath.resolve(relativePath);

        try
        {
            //Create directories
            if (Files.notExists(fullPath))
            {
                Files.createDirectories(fullPath);
            }

            Path fileName1 = Paths.get(prop.getProperty("fileName1"));

            Path fullFilePath = fullPath.resolve(fileName1);

            //Delete file
            boolean existsAndDeleted = Files.deleteIfExists(fullFilePath);
            if (existsAndDeleted)
            {
                logger.debug("File existsAndDeleted");
            }

            //Create file
            Files.createFile(fullFilePath);

            DosFileAttributes dosFileAttributes = Files.readAttributes(fullFilePath, DosFileAttributes.class);

            logger.debug("ReadOnly: {}", String.valueOf(dosFileAttributes.isReadOnly()));
            logger.debug("isHidden: {}", String.valueOf(dosFileAttributes.isHidden()));

            DosFileAttributeView dosFileAttributeView = Files.getFileAttributeView(fullFilePath, DosFileAttributeView
                    .class);

            dosFileAttributeView.setHidden(true);
            //dosFileAttributeView.setReadOnly(true);

            FileTime timeCreated = FileTime.fromMillis(System.currentTimeMillis() - 3600000); //1 hour before
            FileTime timeAccessed = FileTime.fromMillis(System.currentTimeMillis() + 3600000); // 1hour after
            FileTime timeModified = FileTime.fromMillis(System.currentTimeMillis() + 3600000 * 2); //2 hours after

            dosFileAttributeView.setTimes(timeModified, timeAccessed, timeCreated);

            Path copiedFile = Files.copy(fullFilePath, fullPath.resolve(Paths.get("..")).resolve(fileName1));

            Files.deleteIfExists(fullFilePath);

        }
        catch (IOException e)
        {
            logger.error("error", e);
        }
    }


}

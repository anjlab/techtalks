package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils
{
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);

  static String verboseOutput(ServletRequest request)
  {
    if (!request.getParameterMap().containsKey("verbose"))
    {
      return "";
    }

    return String.format("%s %s",
            System.getProperty("build.timestamp"),
            System.getProperty("ec2.public-ipv4"));
  }

  static Properties readPropertiesFromFile(String name)
  {
    Properties properties = new Properties();

    try (InputStream input = new FileInputStream(name))
    {
      properties.load(input);

      return properties;
    }
    catch (IOException e)
    {
      logger.error("Error loading config", e);

      throw new RuntimeException(e);
    }
  }

  static void sleep(Long millis)
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }
}

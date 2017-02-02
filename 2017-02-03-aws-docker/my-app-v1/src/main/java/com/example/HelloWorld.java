package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static com.example.Utils.getVersion;
import static com.example.Utils.readPropertiesFromFile;
import static java.lang.String.format;
import static java.lang.System.getProperty;

public class HelloWorld implements Filter
{
  private static final Logger logger = LoggerFactory.getLogger(HelloWorld.class);

  private Properties properties;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
    properties = readPropertiesFromFile(getProperty("config.path"));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
  {
    logger.info("Serving {}", ((HttpServletRequest) request).getRequestURL());

    sleep(request);

    setHeaders(response);

    response.setContentType("text/plain");
    response.getWriter().print(
            format("Welcome to AnjLab TechTalks! %s\n",
                    getVersion(request)));
  }

  private void sleep(ServletRequest request)
  {
    Optional.ofNullable(request.getParameter("server-side-sleep"))
            .map(Long::parseLong)
            .ifPresent(Utils::sleep);
  }

  private void setHeaders(ServletResponse servletResponse)
  {
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    response.setHeader("My-Secret", (String) properties.get("secret"));
  }

  @Override
  public void destroy()
  {

  }
}

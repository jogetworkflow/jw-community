package org.joget.commons.spring.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomContextLoaderListenerTest {

    public static class NullMessageBean {
        public NullMessageBean() {
            throw new NullPointerException();
        }
    }

    /**
     * A startup failure whose root cause has no message must still be handled by the listener.
     * If it escapes, Tomcat fails the whole /jw context and every URL, /jw/setup included,
     * returns 404.
     */
    @Test
    public void testRootCauseWithoutMessageDoesNotEscape() {
        Map<String, Object> attributes = new HashMap<>();
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM))
                .thenReturn("classpath:org/joget/commons/spring/web/nullMessageBeanContext.xml");
        when(servletContext.getClassLoader()).thenReturn(getClass().getClassLoader());
        doAnswer(i -> attributes.put(i.getArgument(0), i.getArgument(1)))
                .when(servletContext).setAttribute(anyString(), any());
        when(servletContext.getAttribute(anyString())).thenAnswer(i -> attributes.get((String) i.getArgument(0)));
        doAnswer(i -> attributes.remove((String) i.getArgument(0)))
                .when(servletContext).removeAttribute(anyString());
        when(servletContext.getAttributeNames()).thenAnswer(i -> Collections.enumeration(new ArrayList<>(attributes.keySet())));

        new CustomContextLoaderListener().contextInitialized(new ServletContextEvent(servletContext));

        assertNull(attributes.get(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
    }
}

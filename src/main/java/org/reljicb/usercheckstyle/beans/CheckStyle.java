package org.reljicb.usercheckstyle.beans;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "checkstyle")
public class CheckStyle
{
    @JacksonXmlProperty(localName = "version")
    private String version;

    @JacksonXmlProperty(localName = "file")
    private TargetFile targetFile;

    public String getVersion()
    {
        return version;
    }

    public TargetFile getTargetFile()
    {
        return targetFile;
    }

    public static class TargetFile
    {
        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "error")
        @JacksonXmlElementWrapper(useWrapping = false)
        private CSError[] errors;

        public String getName()
        {
            return name;
        }

        public List<CSError> getErrors()
        {
            return Lists.newArrayList(errors).stream()
                  .filter(error -> error != null)
                  .collect(Collectors.toList());
        }

        public static class CSError
        {
            @JacksonXmlProperty(localName = "line")
            private int line;

            @JacksonXmlProperty(localName = "column")
            private int column;

            @JacksonXmlProperty(localName = "severity")
            private String severity;

            @JacksonXmlProperty(localName = "message")
            private String message;

            @JacksonXmlProperty(localName = "source")
            private String source;

            public int getColumn()
            {
                return column;
            }

            public int getLine()
            {
                return line;
            }

            public String getSeverity()
            {
                return severity;
            }

            public String getMessage()
            {
                return message;
            }

            public String getSource()
            {
                return source;
            }

            @Override public String toString()
            {
                return MoreObjects.toStringHelper(this)
                      .add("line", line)
                      .add("column", column)
                      .add("severity", severity)
                      .add("message", message)
                      .add("source", source)
                      .toString();
            }
        }
    }
}

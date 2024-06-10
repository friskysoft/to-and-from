package com.friskysoft.tools.taf.utils;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.friskysoft.tools.taf.models.DataFormat;
import com.friskysoft.tools.taf.models.ToAndFromException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class MapperUtil {

    private final ObjectMapper mapper;
    private final DataFormat format;

    private static final MapperUtil json = new MapperUtil(DataFormat.JSON);
    private static final MapperUtil yaml = new MapperUtil(DataFormat.YAML);
    private static final MapperUtil xml = new MapperUtil(DataFormat.XML);

    public static MapperUtil json() {
        return json;
    }

    public static MapperUtil yaml() {
        return yaml;
    }

    public static MapperUtil xml() {
        return xml;
    }

    public static MapperUtil forFormat(DataFormat format) {
        switch (format) {
            case XML:
                return xml;
            case YAML:
                return yaml;
            case JSON:
                return json;
            default:
                throw new UnsupportedOperationException(format + " is not supported yet");
        }
    }

    public ObjectMapper getJacksonMapper() {
        return mapper;
    }

    public MapperUtil(DataFormat format) {
        this.format = format;
        switch (format) {
            case XML:
                final XMLInputFactory inputFactory = new WstxInputFactory();
                inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
                final XmlFactory xmlFactory = new XmlFactory(inputFactory, new WstxOutputFactory());

                mapper = XmlMapper.builder(xmlFactory).defaultUseWrapper(false).build();
                mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
                mapper.disable(DeserializationFeature.UNWRAP_ROOT_VALUE);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                break;
            case YAML:
                mapper = new ObjectMapper(new YAMLFactory());
                break;
            case JSON:
                mapper = new ObjectMapper(new JsonFactory());
                break;
            default:
                throw new UnsupportedOperationException(format + " is not supported yet");
        }
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String write(Object data) {
        return write(data, true);
    }

    public String write(Object data, boolean prettyPrint) {
        try {
            final ObjectWriter writer = prettyPrint ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
            if (format == DataFormat.XML) {
                return writer.withoutRootName().writeValueAsString(data)
                        .replace("</>", "")
                        .replace("  <", "<")
                        .replace("  ", "    ")
                        .replace("<>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            } else {
                return writer.writeValueAsString(data);
            }
        } catch (Exception ex) {
            throw new ToAndFromException("Unable to convert data to " + format, ex);
        }
    }

    public Map<String, Object> read(String data) {
        return read(data, new TypeReference<>() {});
    }

    public <T> T read(String data, TypeReference<T> returnType) {
        try {
            if (format == DataFormat.XML && returnType.getType().getTypeName().startsWith(Map.class.getName())) {
                DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = fact.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(data)));
                String root = doc.getDocumentElement().getNodeName();
                Map<String, Object> map = new HashMap();
                map.put(root, mapper.readValue(data, returnType));
                return (T) map;
            } else {
                return mapper.readValue(data, returnType);
            }
        } catch (Exception ex) {
            throw new ToAndFromException("Invalid format for " + format, ex);
        }
    }

    public <T> T read(String data, Class<T> returnType) {
        try {
            return mapper.readValue(data, returnType);
        } catch (Exception ex) {
            throw new ToAndFromException("Invalid format for " + format, ex);
        }
    }
}

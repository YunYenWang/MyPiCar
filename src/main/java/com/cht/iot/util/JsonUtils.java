package com.cht.iot.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.cht.iot.ServiceException;

public class JsonUtils {
	static final ObjectMapper JACKSON = new ObjectMapper();
	
	static {
		JACKSON.setSerializationInclusion(Inclusion.NON_NULL);
	}
	
	public static final String getField(String json, String field) {
		try {
			JsonNode node = JACKSON.readTree(json);
			node = node.get(field);
			if (node != null) {
				return node.asText();
			}
			
			return null;
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
	
	public static final <T> T fromJson(Reader r, Class<T> clazz) throws IOException {
		return JACKSON.readValue(r, clazz);
	}
	
	public static final <T> T fromJson(String json, Class<T> clazz) {
		try {
			StringReader sr = new StringReader(json);		
			return fromJson(sr, clazz);
			
		} catch (Exception e) {
			String error = String.format("%s - %s", json, e.getMessage());			
			throw new ServiceException(error, e);
		}
	}
	
	public static final void toJson(Writer w, Object value) throws IOException {
		JACKSON.writeValue(w, value);
	}

	public static final String toJson(Object value) {
		try {
			StringWriter sw = new StringWriter();
			toJson(sw, value);		
			return sw.toString();
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}	
	
	public static final String toPrettyPrintJson(Object value) {
		try {
			StringWriter sw = new StringWriter();
			JACKSON.writerWithDefaultPrettyPrinter().writeValue(sw, value);		
			return sw.toString();
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
}

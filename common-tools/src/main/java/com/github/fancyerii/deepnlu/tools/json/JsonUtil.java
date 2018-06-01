package com.github.fancyerii.deepnlu.tools.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonUtil {
	private static ObjectMapper objectMapper;

	public static ObjectMapper objectMapper() {
		final ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		om.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		om.enable(new JsonParser.Feature[] { JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS });
		om.enable(new JsonParser.Feature[] { JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER });
		om.setFilterProvider((FilterProvider) simpleFilterProvider(false));
		return om;
	}

	public static SimpleFilterProvider simpleFilterProvider(final boolean failOnUnknownId) {
		return new SimpleFilterProvider().setFailOnUnknownId(failOnUnknownId);
	}

	static {
		objectMapper = objectMapper();
	}

	public static <T> T toBean(final String jp, final Class<T> valueType) throws IOException {
		return (T) JsonUtil.objectMapper.readValue(jp, valueType);
	}

	@SuppressWarnings("unchecked")
	public static <T> T toBean(final String jp, final TypeReference<T> valueType) throws IOException {
		return (T) JsonUtil.objectMapper.readValue(jp, valueType);
	}
	
	public static String toJsonStringWithoutException(final Object value) {
		try {
			return JsonUtil.objectMapper.writeValueAsString(value);
		}catch(Exception e) {
			return null;
		}
	}

	public static String toJsonString(final Object value) throws IOException {
		return JsonUtil.objectMapper.writeValueAsString(value);
	}
}

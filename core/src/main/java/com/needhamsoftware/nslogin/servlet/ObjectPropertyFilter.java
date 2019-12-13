/*
 *    Copyright (c) 2019, Needham Software LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.needhamsoftware.nslogin.servlet;

import com.needhamsoftware.nslogin.model.Persisted;
import com.needhamsoftware.nslogin.service.Filter;
import com.needhamsoftware.nslogin.service.ObjectService;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectPropertyFilter implements Filter {
  public static final Pattern FILTER_OPER_PATTERN = Pattern.compile("(=|>=|!=|<=|>|<)\\s*(.*)");
  private String field;
  private String operator;
  private Object value;
  private Field objField;

  public ObjectPropertyFilter(String field, String filterStr, Field objField) {
    this.field = field;
    filterStr = URLDecoder.decode(filterStr, StandardCharsets.UTF_8);
    Matcher m = FILTER_OPER_PATTERN.matcher(filterStr);
    if(!m.matches()) {
      throw new IllegalArgumentException("Could not parse filter pattern:" + filterStr);
    }
    this.operator = m.group(1);
    this.value = parseToFieldType(m.group(2),objField);
    this.objField = objField;
  }

  @Override
  public String getField() {
    return field;
  }

  @Override
  public String getOperator() {
    return operator;
  }

  @Override
  public Object getValue(ObjectService service) {
    if (value == null) {
      return null;
    }
    if (Persisted.class.isAssignableFrom(objField.getType()) && !(value instanceof Persisted)) {
      //noinspection unchecked
      value = service.get((Class<? extends Persisted>) objField.getType(), Long.valueOf(value.toString()));
    }
    return value;
  }

  private Object parseToFieldType(String s, Field f) {
    Class<?> type = f.getType();
    if (type == String.class) {
      if ("null".equals(s.trim())) {
        return null;
      } else {
        return s;
      }
    }
    if (type == Boolean.class || type == Boolean.TYPE) {
      if ("null".equals(s.trim())) {
        return null;
      } else {
        return Boolean.valueOf(s);
      }
    }
    if (type == Integer.class || type == Integer.TYPE) {
      if ("null".equals(s.trim())) {
        return null;
      } else {
        return Integer.valueOf(s);
      }
    }
    if (type == Long.class || type == Long.TYPE) {
      if ("null".equals(s.trim())) {
        return null;
      } else {
        return Long.valueOf(s);
      }
    }
    if (type == Date.class) {
      if ("null".equals(s.trim())) {
        return null;
      } else {
        return new Date(Long.parseLong(s));
      }
    }
    if (type == Instant.class) {
      if ("null".equals(s.trim())) {
        return null;
      } else {
        try {
          return Instant.parse(s);
        } catch (DateTimeParseException e) {
          return Instant.ofEpochMilli(Long.parseLong(s));
        }
      }
    }
    return s;
  }

  @Override
  public String toString() {
    return "ObjectPropertyFilter{" +
        "field='" + field + '\'' +
        ", operator='" + operator + '\'' +
        ", value=" + value +
        ", objField=" + objField +
        '}';
  }
}

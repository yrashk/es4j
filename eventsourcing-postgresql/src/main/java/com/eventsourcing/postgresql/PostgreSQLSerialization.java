/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.Property;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.layout.types.*;
import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PostgreSQLSerialization {
    @SneakyThrows
    public static String getMappedType(Connection connection, TypeHandler typeHandler) {
        if (typeHandler instanceof BigDecimalTypeHandler) {
            return "NUMERIC";
        }
        if (typeHandler instanceof BooleanTypeHandler) {
            return "BOOLEAN";
        }
        if (typeHandler instanceof ByteArrayTypeHandler) {
            return "BYTEA";
        }
        if (typeHandler instanceof ByteTypeHandler) {
            return "SMALLINT";
        }
        if (typeHandler instanceof DateTypeHandler) {
            return "TIMESTAMP";
        }
        if (typeHandler instanceof DoubleTypeHandler) {
            return "DOUBLE PRECISION";
        }
        if (typeHandler instanceof EnumTypeHandler) {
            return "INTEGER";
        }
        if (typeHandler instanceof FloatTypeHandler) {
            return "REAL";
        }
        if (typeHandler instanceof IntegerTypeHandler) {
            return "INTEGER";
        }
        if (typeHandler instanceof ListTypeHandler) {
            return getMappedType(connection, ((ListTypeHandler) typeHandler).getWrappedHandler()) +
                    "[]";
        }
        if (typeHandler instanceof LongTypeHandler) {
            return "BIGINT";
        }
        if (typeHandler instanceof ObjectTypeHandler) {
            Layout<?> layout = ((ObjectTypeHandler) typeHandler).getLayout();
            byte[] fingerprint = layout.getHash();
            String encoded = BaseEncoding.base16().encode(fingerprint);
            String typname = "layout_" + encoded;

            PreparedStatement check = connection
                    .prepareStatement("SELECT * FROM pg_catalog.pg_type WHERE lower(typname) = lower(?)");

            check.setString(1, typname);

            boolean shouldCreateType;
            try (ResultSet resultSet = check.executeQuery()) {
                shouldCreateType = !resultSet.next();
            }
            check.close();

            if (shouldCreateType) {
                String columns = PostgreSQLJournal.defineColumns(connection, layout);
                String createType = "CREATE TYPE " + typname + " AS (" +
                        columns +
                        ")";
                PreparedStatement s = connection.prepareStatement(createType);
                s.execute();
                s.close();
                s = connection.prepareStatement("COMMENT ON TYPE " + typname + " IS '" + layout.getName() + "'");
                s.execute();
                s.close();
            }

            return typname;
        }
        if (typeHandler instanceof OptionalTypeHandler) {
            return getMappedType(connection, ((OptionalTypeHandler) typeHandler).getWrappedHandler());
        }
        if (typeHandler instanceof ShortTypeHandler) {
            return "SMALLINT";
        }
        if (typeHandler instanceof StringTypeHandler) {
            return "TEXT";
        }
        if (typeHandler instanceof UUIDTypeHandler) {
            return "UUID";
        }
        throw new RuntimeException("Unsupported type handler " + typeHandler.getClass());
    }

    public static int getMappedSqlType(TypeHandler typeHandler) {
        if (typeHandler instanceof BigDecimalTypeHandler) {
            return Types.DECIMAL;
        }
        if (typeHandler instanceof BooleanTypeHandler) {
            return Types.BOOLEAN;
        }
        if (typeHandler instanceof ByteArrayTypeHandler) {
            return Types.BINARY;
        }
        if (typeHandler instanceof ByteTypeHandler) {
            return Types.SMALLINT;
        }
        if (typeHandler instanceof DateTypeHandler) {
            return Types.TIMESTAMP;
        }
        if (typeHandler instanceof DoubleTypeHandler) {
            return Types.DOUBLE;
        }
        if (typeHandler instanceof EnumTypeHandler) {
            return Types.INTEGER;
        }
        if (typeHandler instanceof FloatTypeHandler) {
            return Types.FLOAT;
        }
        if (typeHandler instanceof IntegerTypeHandler) {
            return Types.INTEGER;
        }
        if (typeHandler instanceof ListTypeHandler) {
            return Types.ARRAY;
        }
        if (typeHandler instanceof LongTypeHandler) {
            return Types.BIGINT;
        }
        if (typeHandler instanceof ObjectTypeHandler) {
            return Types.VARCHAR;
        }
        if (typeHandler instanceof OptionalTypeHandler) {
            return getMappedSqlType(((OptionalTypeHandler) typeHandler).getWrappedHandler());
        }
        if (typeHandler instanceof ShortTypeHandler) {
            return Types.SMALLINT;
        }
        if (typeHandler instanceof StringTypeHandler) {
            return Types.VARCHAR;
        }
        if (typeHandler instanceof UUIDTypeHandler) {
            return Types.VARCHAR;
        }
        throw new RuntimeException("Unsupported type handler " + typeHandler.getClass());
    }

    @SneakyThrows
    public static int setValue(Connection connection, PreparedStatement s, int i, Object value, TypeHandler
            typeHandler) {
        if (typeHandler instanceof BigDecimalTypeHandler) {
            s.setBigDecimal(i, value == null ? BigDecimal.ZERO : (BigDecimal) value);
        } else
        if (typeHandler instanceof BooleanTypeHandler) {
            s.setBoolean(i, value == null ? false: (Boolean) value);
        } else
        if (typeHandler instanceof ByteArrayTypeHandler) {
            if (((ByteArrayTypeHandler) typeHandler).isPrimitive()) {
                s.setBytes(i, value == null ? new byte[]{} : (byte[]) value);
            } else {
                s.setBytes(i, value == null ?  new byte[]{} :
                        (byte[]) ((ByteArrayTypeHandler) typeHandler).toPrimitive(value));
            }
        } else
        if (typeHandler instanceof ByteTypeHandler) {
            s.setByte(i, value == null ? 0 : (Byte) value);
        } else
        if (typeHandler instanceof DateTypeHandler) {
            s.setTimestamp(i, value == null ? Timestamp.from(Instant.EPOCH) :
                    Timestamp.from(((java.util.Date)value).toInstant()));
        } else
        if (typeHandler instanceof DoubleTypeHandler) {
            s.setDouble(i, value == null ? 0 : (Double) value);
        } else
        if (typeHandler instanceof EnumTypeHandler) {
            s.setInt(i, value == null ? 0 : ((Enum)value).ordinal());
        } else
        if (typeHandler instanceof FloatTypeHandler) {
            s.setFloat(i, value == null ? 0 : (Float) value);
        } else
        if (typeHandler instanceof IntegerTypeHandler) {
            s.setInt(i, value == null ? 0 : (Integer) value);
        } else
        if (typeHandler instanceof ListTypeHandler) {
            int j=i;
            TypeHandler handler = ((ListTypeHandler) typeHandler).getWrappedHandler();
            for (Object item : (value == null ? Arrays.asList() : (List)value)) {
                j = setValue(connection, s, j, item, handler);
            }
            return j;
        } else
        if (typeHandler instanceof LongTypeHandler) {
            s.setLong(i, value == null ? 0 : (Long) value);
        } else
        if (typeHandler instanceof ObjectTypeHandler) {
            Layout<?> layout = ((ObjectTypeHandler) typeHandler).getLayout();
            Object value_ = value == null ? layout.instantiate() : value;
            List<? extends Property<?>> properties = layout.getProperties();
            int j=i;
            for (Property p : properties) {
                j = setValue(connection, s, j, p.get(value_), p.getTypeHandler());
            }
            return j;
        } else
        if (typeHandler instanceof OptionalTypeHandler) {
            TypeHandler handler = ((OptionalTypeHandler) typeHandler).getWrappedHandler();
            if (value != null && ((Optional)value).isPresent()) {
                i = setValue(connection, s, i, ((Optional) value).get(),
                             handler);
            } else {
                s.setNull(i, PostgreSQLSerialization.getMappedSqlType(handler));
                i++;
            }
            return i;
        } else
        if (typeHandler instanceof ShortTypeHandler) {
            s.setShort(i, value == null ? 0 : (Short) value);
        } else
        if (typeHandler instanceof StringTypeHandler) {
            s.setString(i, value == null ? "" : (String) value);
        } else
        if (typeHandler instanceof UUIDTypeHandler) {
            s.setString(i, value == null ? new UUID(0,0).toString() : value.toString());
        } else {
            throw new RuntimeException("Unsupported type handler " + typeHandler.getClass());
        }
        return i+1;
    }

    @SneakyThrows
    public static Object getValue(ResultSet resultSet, AtomicInteger i, TypeHandler typeHandler) {
        if (typeHandler instanceof BigDecimalTypeHandler) {
            return resultSet.getBigDecimal(i.getAndIncrement());
        }
        if (typeHandler instanceof BooleanTypeHandler) {
            return resultSet.getBoolean(i.getAndIncrement());
        }
        if (typeHandler instanceof ByteArrayTypeHandler) {
            byte[] bytes = resultSet.getBytes(i.getAndIncrement());
            if (((ByteArrayTypeHandler) typeHandler).isPrimitive()) {
                return bytes;
            } else {
                return ((ByteArrayTypeHandler) typeHandler).toObject(bytes);
            }
        }
        if (typeHandler instanceof ByteTypeHandler) {
            return resultSet.getByte(i.getAndIncrement());
        }
        if (typeHandler instanceof DateTypeHandler) {
            return Date.from(resultSet.getTimestamp(i.getAndIncrement()).toInstant());
        }
        if (typeHandler instanceof DoubleTypeHandler) {
            return resultSet.getDouble(i.getAndIncrement());
        }
        if (typeHandler instanceof EnumTypeHandler) {
            Class<? extends Enum> enumClass = ((EnumTypeHandler) typeHandler).getEnumClass();
            String[] enumNames = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
            return Enum.valueOf(enumClass, enumNames[resultSet.getInt(i.getAndIncrement())]);
        }
        if (typeHandler instanceof FloatTypeHandler) {
            return resultSet.getFloat(i.getAndIncrement());
        }
        if (typeHandler instanceof IntegerTypeHandler) {
            return resultSet.getInt(i.getAndIncrement());
        }
        if (typeHandler instanceof ListTypeHandler) {
            if (((ListTypeHandler) typeHandler).getWrappedHandler() instanceof ObjectTypeHandler) {
                ObjectTypeHandler handler = (ObjectTypeHandler) ((ListTypeHandler) typeHandler).getWrappedHandler();
                Layout<?> objectLayout = handler.getLayout();
                List<? extends Property<?>> properties = objectLayout.getProperties();
                List<Map<String, Object>> list = new ArrayList();
                for (Property p : properties) {
                    Array array = resultSet.getArray(i.getAndIncrement());
                    ResultSet arrayResultSet = array.getResultSet();
                    int j=0;
                    while (arrayResultSet.next()) {
                        j++;
                        if (list.size() < j) {
                            list.add(new HashMap<>());
                        }
                        Map<String, Object> o = list.get(j - 1);
                        o.put(p.getName(), getValue(arrayResultSet, new AtomicInteger(2), p.getTypeHandler()));
                    }
                }
                return list.stream().map(new ObjectArrayCollector(objectLayout, properties)).collect(
                        Collectors.toList());
            } else {
                Array array = resultSet.getArray(i.getAndIncrement());
                ResultSet arrayResultSet = array.getResultSet();
                List list = new ArrayList();
                TypeHandler handler = ((ListTypeHandler) typeHandler).getWrappedHandler();
                while (arrayResultSet.next()) {
                    list.add(getValue(arrayResultSet, new AtomicInteger(2), handler));
                }
                return list;
            }
        }
        if (typeHandler instanceof LongTypeHandler) {
            return resultSet.getLong(i.getAndIncrement());
        }
        if (typeHandler instanceof ObjectTypeHandler) {
            Layout<?> layout = ((ObjectTypeHandler) typeHandler).getLayout();
            List<? extends Property<?>> properties = layout.getProperties();

            Map<Property<?>, Object> props = new HashMap<>();
            for (Property property : properties) {
                props.put(property, getValue(resultSet, i, property.getTypeHandler()));
            }
            @SuppressWarnings("unchecked")
            Object o = ((Layout) layout).instantiate(props);
            return o;
        }
        if (typeHandler instanceof OptionalTypeHandler) {
            return Optional.ofNullable(getValue(resultSet, i, ((OptionalTypeHandler) typeHandler)
                    .getWrappedHandler()));
        }
        if (typeHandler instanceof ShortTypeHandler) {
            return resultSet.getShort(i.getAndIncrement());
        }
        if (typeHandler instanceof StringTypeHandler) {
            return resultSet.getString(i.getAndIncrement());
        }
        if (typeHandler instanceof UUIDTypeHandler) {
            return UUID.fromString(resultSet.getString(i.getAndIncrement()));
        }
        throw new RuntimeException("Unsupported type handler " + typeHandler.getClass());
    }

    @SneakyThrows
    public static String getParameter(Connection connection, TypeHandler typeHandler, Object object) {
        if (typeHandler instanceof UUIDTypeHandler) {
            return "?::UUID";
        } else if (typeHandler instanceof ObjectTypeHandler) {
            Layout layout = ((ObjectTypeHandler) typeHandler).getLayout();
            final Object o = object == null ?
                    layout.instantiate() : object;
            @SuppressWarnings("unchecked")
            List<? extends Property> properties = layout.getProperties();
            @SuppressWarnings("unchecked")
            String rowParameters = Joiner.on(",").join(
                    properties.stream().map(p1 -> getParameter(connection, p1.getTypeHandler(), p1.get(o))
                    ).collect(Collectors.toList()));
            return "ROW(" + rowParameters + ")";
        } else if (typeHandler instanceof ListTypeHandler) {
            TypeHandler handler = ((ListTypeHandler) typeHandler).getWrappedHandler();
            List<?> list = object == null ? Arrays.asList() : (List<?>) object;
            String listParameters = Joiner.on(",").join(
                    list.stream().map(i -> getParameter(connection, handler, i))
                        .collect(Collectors.toList()));
            return "ARRAY[" + listParameters + "]::" + PostgreSQLSerialization.getMappedType(connection, handler) + "[]";
        } else if (typeHandler instanceof OptionalTypeHandler) {
            if (object == null || !((Optional) object).isPresent()) {
                return "?";
            } else {
                return getParameter(connection, ((OptionalTypeHandler) typeHandler).getWrappedHandler(), ((Optional)
                        object).get());
            }
        } else {
            return "?";
        }
    }


    private static class ObjectArrayCollector implements Function<Map<String,Object>, Object> {
        private final Layout objectLayout;
        private final List<? extends Property<?>> properties;

        public ObjectArrayCollector(Layout<?> objectLayout, List<? extends Property<?>> properties) {
            this.objectLayout = objectLayout;
            this.properties = properties;
        }

        @SneakyThrows
        @Override public Object apply(Map<String, Object> map) {
            Map<Property, Object> props = new HashMap<>();
            for (Property property : properties) {
                props.put(property, map.get(property.getName()));
            }
            @SuppressWarnings("unchecked")
            Object o = objectLayout.instantiate(props);
            return o;
        }
    }
}

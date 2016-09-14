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
import com.google.common.io.BaseEncoding;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.jdbc.PGConnectionImpl;
import com.impossibl.postgres.jdbc.PGStruct;
import com.impossibl.postgres.system.Version;
import com.impossibl.postgres.system.tables.PgAttribute;
import com.impossibl.postgres.system.tables.PgProc;
import com.impossibl.postgres.system.tables.PgType;
import io.netty.buffer.ByteBufInputStream;
import lombok.SneakyThrows;

import java.io.DataInput;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PostgreSQLSerialization {

    private static List<PgType.Row> pgTypes = new ArrayList<>();
    private static List<PgAttribute.Row> pgAttrs = new ArrayList<>();
    private static List<PgProc.Row> pgProcs = new ArrayList<>();

    @SneakyThrows
    public static void refreshTypes(Connection connection) {
        PGConnection conn = getPgConnection(connection);

        PGConnectionImpl connImpl = (PGConnectionImpl) conn;
        Version serverVersion = connImpl.getServerVersion();

        //Load types
        String typeSQL = PgType.INSTANCE.getSQL(serverVersion);
        pgTypes = connImpl.queryResults(typeSQL, PgType.Row.class);

        //Load attributes
        String attrsSQL = PgAttribute.INSTANCE.getSQL(serverVersion);
        pgAttrs = connImpl.queryResults(attrsSQL, PgAttribute.Row.class);

        //Load procs
        String procsSQL = PgProc.INSTANCE.getSQL(serverVersion);
        pgProcs = connImpl.queryResults(procsSQL, PgProc.Row.class);
    }

    protected static PGConnection getPgConnection(Connection connection) {
        PGConnection conn;
        if (connection instanceof Wrapper) {
            try {
                conn = connection.unwrap(PGConnection.class);
            } catch (SQLException e) {
                throw new RuntimeException("pgjdbc-ng is required");
            }
        } else if (connection instanceof PGConnection) {
            conn = (PGConnection) connection;
        } else {
            throw new RuntimeException("pgjdbc-ng is required");
        }
        return conn;
    }

    public static void refreshConnectionRegistry(Connection connection) {
        PGConnection conn = getPgConnection(connection);

        PGConnectionImpl connImpl = (PGConnectionImpl) conn;
        //Update the registry with known types
        connImpl.getRegistry().update(pgTypes, pgAttrs, pgProcs);
    }



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
            TypeHandler wrappedHandler = ((ListTypeHandler) typeHandler).getWrappedHandler();
            String mappedType = getMappedType(connection, wrappedHandler);
            if (wrappedHandler instanceof ListTypeHandler) {
                mappedType = mappedType.replaceAll("\\[\\]$", "");
                String typname = "layout_v1_arr_" + mappedType;

                PreparedStatement check = connection
                        .prepareStatement("SELECT * FROM pg_catalog.pg_type WHERE lower(typname) = lower(?)");

                check.setString(1, typname);

                boolean shouldCreateType;
                try (ResultSet resultSet = check.executeQuery()) {
                    shouldCreateType = !resultSet.next();
                }
                check.close();

                if (shouldCreateType) {
                    String createType = "CREATE TYPE " + typname + " AS (" +
                            "\"value\" " + mappedType + "[])";
                    PreparedStatement s = connection.prepareStatement(createType);
                    s.execute();
                    s.close();
                    s = connection.prepareStatement("COMMENT ON TYPE " + typname + " IS '" + mappedType + " array'");
                    s.execute();
                    s.close();
                    refreshTypes(connection);
                }

                return typname + "[]";
            } else {
                return mappedType + "[]";
            }
        }
        if (typeHandler instanceof LongTypeHandler) {
            return "BIGINT";
        }
        if (typeHandler instanceof ObjectTypeHandler) {
            Layout<?> layout = ((ObjectTypeHandler) typeHandler).getLayout();
            byte[] fingerprint = layout.getHash();
            String encoded = BaseEncoding.base16().encode(fingerprint);
            String typname = "layout_v1_" + encoded;

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
                refreshTypes(connection);
            }

            return typname.toLowerCase();
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
        if (typeHandler instanceof MapTypeHandler) {
            String keyType = getMappedType(connection, ((MapTypeHandler) typeHandler).getWrappedKeyHandler());
            String valueType = getMappedType(connection, ((MapTypeHandler) typeHandler).getWrappedValueHandler());

            String typname = ("map_v1_" + keyType + "_" + valueType).replaceAll("\\[\\]", "__");

            boolean shouldCreateType;

            try (PreparedStatement check = connection
                    .prepareStatement("SELECT * FROM pg_catalog.pg_type WHERE lower(typname) = lower(?)")) {
                check.setString(1, typname);

                try (ResultSet resultSet = check.executeQuery()) {
                    shouldCreateType = !resultSet.next();
                }
            }

            if (shouldCreateType) {
                String columns = "key " + keyType + ", value " + valueType;
                String createType = "CREATE TYPE " + typname + " AS (" +
                        columns +
                        ")";
                PreparedStatement s = connection.prepareStatement(createType);
                s.execute();
                s.close();
                s = connection.prepareStatement("COMMENT ON TYPE " + typname + " IS 'Map[" + keyType + "][" + valueType + "]'");
                s.execute();
                s.close();
                refreshTypes(connection);
            }

            return typname.toLowerCase() + "[]";
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
        if (typeHandler instanceof MapTypeHandler) {
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
    private static Object prepareValue(Connection connection, TypeHandler typeHandler, Object value) {
        if (typeHandler instanceof BigDecimalTypeHandler) {
            return value == null ? BigDecimal.ZERO : (BigDecimal) value;
        } else if (typeHandler instanceof BooleanTypeHandler) {
            return value == null ? false: (Boolean) value;
        } else if (typeHandler instanceof ByteArrayTypeHandler) {
            if (((ByteArrayTypeHandler) typeHandler).isPrimitive()) {
                return value == null ? new byte[]{} : (byte[]) value;
            } else {
                return value == null ?  new byte[]{} :
                        (byte[]) ((ByteArrayTypeHandler) typeHandler).toPrimitive(value);
            }
        } else if (typeHandler instanceof ByteTypeHandler) {
            return value == null ? 0 : (Byte) value;
        } else if (typeHandler instanceof DateTypeHandler) {
            return value == null ? Timestamp.from(Instant.EPOCH) : Timestamp.from(((java.util.Date)value).toInstant());
        } else if (typeHandler instanceof DoubleTypeHandler) {
            return value == null ? 0 : (Double) value;
        } else if (typeHandler instanceof EnumTypeHandler) {
            return value == null ? 0 : ((Enum)value).ordinal();
        } else if (typeHandler instanceof FloatTypeHandler) {
            return value == null ? 0 : (Float) value;
        } else if (typeHandler instanceof IntegerTypeHandler) {
            return value == null ? 0 : (Integer) value;
        } else if (typeHandler instanceof ListTypeHandler) {
            TypeHandler handler = ((ListTypeHandler)typeHandler).getWrappedHandler();
            List val = value == null ? Collections.emptyList() : (List) value;
            String typeName = getMappedType(connection, handler).toLowerCase();
            if (handler instanceof ListTypeHandler) {
                String arrTypeName = getMappedType(connection, typeHandler).toLowerCase().replaceAll("\\[\\]$", "");
                Object[] arr = val.stream().map(new Function() {
                    @SneakyThrows
                    @Override public Object apply(Object v) {
                        return connection.createStruct(arrTypeName,
                                                       new Object[]{prepareValue(connection,
                                                                                 handler, v)});
                    }
                }).toArray();
                return connection.createArrayOf(arrTypeName, arr);
            } else {
                Object[] arr = val.stream().map(v -> prepareValue(connection, handler, v)).toArray();
                return connection.createArrayOf(typeName, arr);
            }
        } else if (typeHandler instanceof MapTypeHandler) {
            if (value == null) {
                value = new HashMap<>();
            }
            String typeName = getMappedType(connection, typeHandler).replaceAll("\\[\\]", "");
            Object[] arr = ((Map)value).entrySet().stream()
                                       .map(new Function() {
                                           @SneakyThrows
                                           @Override public Object apply(Object e) {
                                               Map.Entry entry = (Map.Entry) e;
                                               return connection.createStruct(typeName,
                                                                              new Object[]{
                                                                                      prepareValue(connection,
                                                                                                   ((MapTypeHandler) typeHandler)
                                                                                                           .getWrappedKeyHandler(),
                                                                                                   entry.getKey()),
                                                                                      prepareValue(connection,
                                                                                                   ((MapTypeHandler) typeHandler)
                                                                                                           .getWrappedValueHandler(),
                                                                                                   entry.getValue())});
                                           }
                                       }).toArray();
            return connection.createArrayOf(typeName, arr);
        } else if (typeHandler instanceof LongTypeHandler) {
            return value == null ? 0 : (Long) value;
        } else if (typeHandler instanceof ObjectTypeHandler) {
            Layout<Object> objectLayout = ((ObjectTypeHandler) typeHandler).getLayout();
            Object val = value == null ? objectLayout.instantiate() : value;
            String typeName = "layout_v1_" + BaseEncoding.base16().encode(objectLayout.getHash()).toLowerCase();
            Object[] objects = objectLayout.getProperties().stream()
                                           .map(p -> prepareValue(connection, p.getTypeHandler(), p.get(val)))
                                           .toArray();

            return connection.createStruct(typeName, objects);
        } else if (typeHandler instanceof OptionalTypeHandler) {
            if (value != null && ((Optional)value).isPresent()) {
                return ((Optional) value).get();
            } else {
                return null;
            }
        } else if (typeHandler instanceof ShortTypeHandler) {
            return value == null ? 0 : (Short) value;
        } else if (typeHandler instanceof StringTypeHandler) {
            return value == null ? "" : (String) value;
        } else if (typeHandler instanceof UUIDTypeHandler) {
            return value == null ? new UUID(0,0).toString() : value.toString();
        } else {
            throw new RuntimeException("Unsupported type handler " + typeHandler.getClass());
        }
    }

    @SneakyThrows
    public static int setValue(Connection connection, PreparedStatement s, int i, Object value, TypeHandler
            typeHandler) {
        if (typeHandler instanceof BigDecimalTypeHandler) {
            s.setBigDecimal(i, (BigDecimal) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof BooleanTypeHandler) {
            s.setBoolean(i, (Boolean) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof ByteArrayTypeHandler) {
            s.setBytes(i, (byte[]) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof ByteTypeHandler) {
            s.setByte(i, (Byte) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof DateTypeHandler) {
            s.setTimestamp(i, (Timestamp) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof DoubleTypeHandler) {
            s.setDouble(i, (Double) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof EnumTypeHandler) {
            s.setInt(i, (Integer) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof FloatTypeHandler) {
            s.setFloat(i, (Float) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof IntegerTypeHandler) {
            s.setInt(i, (Integer) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof ListTypeHandler) {
            s.setArray(i, (Array) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof MapTypeHandler) {
            s.setArray(i, (Array) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof LongTypeHandler) {
            s.setLong(i, (Long) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof ObjectTypeHandler) {
            s.setObject(i,  prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof OptionalTypeHandler) {
            TypeHandler handler = ((OptionalTypeHandler) typeHandler).getWrappedHandler();
            if (value != null && ((Optional)value).isPresent()) {
                i = setValue(connection, s, i, ((Optional) value).get(), handler);
            } else {
                s.setNull(i, PostgreSQLSerialization.getMappedSqlType(handler));
                i++;
            }
            return i;
        } else
        if (typeHandler instanceof ShortTypeHandler) {
            s.setShort(i, (Short) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof StringTypeHandler) {
            s.setString(i, (String) prepareValue(connection, typeHandler, value));
        } else
        if (typeHandler instanceof UUIDTypeHandler) {
            s.setString(i, (String) prepareValue(connection, typeHandler, value));
        } else {
            throw new RuntimeException("Unsupported type handler " + typeHandler.getClass());
        }
        return i+1;
    }

    @SneakyThrows
    private static Object instantiateObject(Layout<?> layout, PGStruct struct) {

        Iterator<Object> structIterator = Arrays.asList(struct.getAttributes()).iterator();

        Map<Property<?>, Object> props = new HashMap<>();
        for (Property property : layout.getProperties()) {
            Object v = structIterator.next();
            props.put(property, getPropertyValue(property.getTypeHandler(), v));
        }

        @SuppressWarnings("unchecked")
        Object o = ((Layout) layout).instantiate(props);
        return o;
    }

    @SneakyThrows
    private static Object getPropertyValue(TypeHandler t, Object v) {
        if (t instanceof OptionalTypeHandler) {
            return v == null ? Optional.empty() : Optional.of(v);
        } else if (t instanceof EnumTypeHandler) {
            EnumTypeHandler typeHandler = (EnumTypeHandler) t;
            Class<? extends Enum> enumClass = typeHandler.getEnumClass();
            String[] enumNames = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
            return Enum.valueOf(enumClass, enumNames[(int) v]);
        } else if (v instanceof Short && t instanceof ByteTypeHandler) {
            return ((Short) v).byteValue();
        } else if (v instanceof Timestamp) {
            return new Date(((Timestamp) v).getTime());
        } else if (v instanceof ByteBufInputStream && t instanceof ByteArrayTypeHandler) {
            int available = ((ByteBufInputStream) v).available();
            byte[] b = new byte[available];
            ((DataInput) v).readFully(b);
            ByteArrayTypeHandler typeHandler = (ByteArrayTypeHandler) t;
            boolean isPrimitive = typeHandler.isPrimitive();
            return isPrimitive ? b : typeHandler.toObject(b);
        } else if (v instanceof PGStruct) {
            if (t instanceof ObjectTypeHandler) {
                return instantiateObject(((ObjectTypeHandler) t).getLayout(),
                                                      (PGStruct) v);
            }
            if (t instanceof ListTypeHandler) {
                return getPropertyValue(t, ((PGStruct) v).getAttributes()[0]);
            }
        } else if (t instanceof ListTypeHandler && v.getClass().isArray()) {
            ListTypeHandler typeHandler = (ListTypeHandler) t;
            TypeHandler wrappedHandler = typeHandler.getWrappedHandler();
            if (wrappedHandler instanceof ObjectTypeHandler &&
                    Struct.class.isAssignableFrom(v.getClass().getComponentType())) {
                Struct[] val = (Struct[]) v;
                ArrayList<Object> objects = new ArrayList<>();
                for (Struct value : val) {
                    objects.add(instantiateObject(((ObjectTypeHandler) wrappedHandler).getLayout(), (PGStruct) value));
                }
                return objects;
            } else {
                return Arrays.stream(((Object[]) v)).map(i -> getPropertyValue(t, i))
                             .collect(Collectors.toList());
            }
        } else if (t instanceof MapTypeHandler && v.getClass().isArray()) {
            MapTypeHandler typeHandler = (MapTypeHandler) t;
            TypeHandler keyHandler = typeHandler.getWrappedKeyHandler();
            TypeHandler valueHandler = typeHandler.getWrappedValueHandler();
            Object[] arr = (Object[]) v;
            return Arrays.stream(arr)
                         .collect(Collectors.toMap(new Function<Object, Object>() {
                             @SneakyThrows
                             @Override public Object apply(Object i) {
                                 return getPropertyValue(keyHandler, ((PGStruct) i).getAttributes()[0]);
                               }
                           },
                           new Function<Object, Object>() {
                               @SneakyThrows
                               @Override public Object apply(Object i) {
                                   return getPropertyValue(valueHandler,
                                                           ((PGStruct) i).getAttributes()[1]);
                               }
                           }));
        } else {
            return v;
        }
    ;    throw new RuntimeException("unexpected error");
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
            Array array = resultSet.getArray(i.getAndIncrement());
            try (ResultSet arrayResultSet = array.getResultSet()) {
                List list = new ArrayList();
                TypeHandler handler = ((ListTypeHandler) typeHandler).getWrappedHandler();
                while (arrayResultSet.next()) {
                    Object value = getValue(arrayResultSet, new AtomicInteger(2), handler);
                    if (value instanceof PGStruct) {
                        list.add(instantiateObject(((ObjectTypeHandler) handler).getLayout(), (PGStruct) value));
                    } else {
                        list.add(value);
                    }
                }
                return list;
            }
        }
        if (typeHandler instanceof LongTypeHandler) {
            return resultSet.getLong(i.getAndIncrement());
        }
        if (typeHandler instanceof ObjectTypeHandler) {
            Layout<?> layout = ((ObjectTypeHandler) typeHandler).getLayout();
            PGStruct struct = (PGStruct) resultSet.getObject(i.getAndIncrement());

            return instantiateObject(layout, struct);
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
        } else if (typeHandler instanceof OptionalTypeHandler) {
            if (object == null || !((Optional) object).isPresent()) {
                return "?";
            } else {
                return getParameter(connection, ((OptionalTypeHandler) typeHandler).getWrappedHandler(),
                                    ((Optional) object).get());
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

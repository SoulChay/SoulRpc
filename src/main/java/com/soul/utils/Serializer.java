package com.soul.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.*;
import com.soul.exception.ExceptionType;
import com.soul.exception.RpcException;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public interface Serializer {

    /**
     * 序列化
     * @param object
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T object);

    /**
     * 反序列化
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    enum Algorithm implements Serializer {

        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) ois.readObject();
                } catch (Exception e) {
                    throw new RpcException(ExceptionType.DESERIALIZER_FAILURE.getMessage(),e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (Exception e) {
                    throw new RpcException(ExceptionType.SERIALIZER_FAILURE.getMessage(),e);
                }
            }
        },

        Json {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String jsonStr = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(jsonStr, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String jsonStr = gson.toJson(object);
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        },

        Kryo {
            @Override
            public <T> byte[] serialize(T object) {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     Output output = new Output(byteArrayOutputStream)) {
                    Kryo kryo = KryoSerializer.kryoThreadLocal.get();
                    kryo.writeObject(output, object);
                    KryoSerializer.kryoThreadLocal.remove();
                    return output.toBytes();
                } catch (Exception e) {
                    throw new RpcException(ExceptionType.SERIALIZER_FAILURE.getMessage(),e);
                }
            }

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                     Input input = new Input(byteArrayInputStream)) {
                    Kryo kryo = KryoSerializer.kryoThreadLocal.get();
                    Object o = kryo.readObject(input, clazz);
                    KryoSerializer.kryoThreadLocal.remove();
                    return (T) o;
                } catch (Exception e) {
                    throw new RpcException(ExceptionType.DESERIALIZER_FAILURE.getMessage(),e);
                }
            }
        }
    }

    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public Class<?> deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(src.getName());
        }
    }

    class KryoSerializer {
        private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(false);
            ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                    .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        });
    }
}

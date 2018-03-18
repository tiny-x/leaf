package com.leaf.serialization.protostuff.test;

import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.protostuff.ProtoStuffSerializer;
import org.junit.Test;

public class ProtostuffSerializerTest {

    private static Serializer serializer = new ProtoStuffSerializer();

    @Test
    public void serialize() {
        User user = new User(1, "he he");
        byte[] bytes = serializer.serialize(user);

        User deserializeUser = serializer.deserialize(bytes, User.class);
        System.out.println(deserializeUser);
    }

    static class User {

        private int id;

        private String name;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}

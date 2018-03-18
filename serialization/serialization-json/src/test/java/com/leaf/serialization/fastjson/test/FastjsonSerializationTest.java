package com.leaf.serialization.fastjson.test;

import com.leaf.serailization.fastjson.FastjsonSerialization;
import com.leaf.serialization.api.Serializer;
import org.junit.Test;

import java.io.Serializable;

public class FastjsonSerializationTest {

    private static Serializer serializer = new FastjsonSerialization();

    @Test
    public void serialize() {
        User user = new User(1, "he he");
        byte[] bytes = serializer.serialize(user);

        User deserializeUser = serializer.deserialize(bytes, User.class);
        System.out.println(deserializeUser);
    }

    static class User implements Serializable {

        private int id;

        private String name;

        /**
         * fast json 要有无参构造方法
         */
        public User() {
        }

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

package com.example.easyvote;
import org.apache.commons.lang3.RandomStringUtils;
public class CustomIdGenerator {
    public static String generateCustomId(int length) {
        // Generate a random alphanumeric string of the desired length
        return RandomStringUtils.randomAlphanumeric(length);
    }

}

package com.configlite.util;

import android.text.TextUtils;
import android.util.Base64;

import com.configlite.ConfigManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class ConfigEncryption {
    private final static String INITIAL_KEY = "EncryptionHandler";

    public static String get(String fbServerId) {
        if(!TextUtils.isEmpty(ConfigManager.getInstance().getEncDataKey())){
            return decrypt(ConfigManager.getInstance().getEncDataKey(), fbServerId);
        }else {
            return fbServerId;
        }
    }

    public static String encrypt(String keyStr, String enStr){
        try {
            byte[] bytes = encrypt(INITIAL_KEY, keyStr, enStr.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.encode(bytes ,Base64.DEFAULT), StandardCharsets.UTF_8).trim()
                    .replaceAll("\\\n","");
        } catch (Exception e) {
            NetworkLog.logError(e.toString());
            return enStr;
        }
    }

    public static String decrypt(String keyStr, String deStr){
        try {
            byte[] bytes = decrypt(INITIAL_KEY, keyStr, Base64.decode(deStr.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT));
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            NetworkLog.logError(e.toString());
            return deStr;
        }
    }

    public static String encBase64(String enStr){
        return new String(Base64.encode(enStr.getBytes() ,Base64.DEFAULT), StandardCharsets.UTF_8).trim();
    }
    public static String decBase64(String decStr){
        return new String(Base64.decode(decStr.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT)).trim();
    }

//    public static String encryptValid(String keyStr, String enStr){
//        return encrypt(keyStr, enStr).replaceAll("\\\n","");
//    }
    /************************************ AES Encryption Methods *************************************/

    private static byte[] encrypt(String ivStr, String keyStr, byte[] bytes) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ivStr.getBytes());
        byte[] ivBytes = md.digest();

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(keyStr.getBytes());
        byte[] keyBytes = sha.digest();

        return encrypt(ivBytes, keyBytes, bytes);
    }

    private static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] bytes) throws Exception{
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(bytes);
    }

    private static byte[] decrypt(String ivStr, String keyStr, byte[] bytes) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ivStr.getBytes());
        byte[] ivBytes = md.digest();

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(keyStr.getBytes());
        byte[] keyBytes = sha.digest();

        return decrypt(ivBytes, keyBytes, bytes);
    }

    private static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] bytes)  throws Exception{
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(bytes);
    }
}

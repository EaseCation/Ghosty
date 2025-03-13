package net.easecation.ghosty;

import cn.nukkit.Server;
import cn.nukkit.utils.Binary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * MD5加密工具类
 * @author pibigstar
 *
 */
public class MD5Util {

    public static String encode(String password) {
        String passwordMd5 = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(password.getBytes(StandardCharsets.UTF_8));
            passwordMd5 = Binary.bytesToHexString(bytes).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            Logger.getServer().logException(e);
        }
        return passwordMd5;
    }

    public static String md5SkinData(byte[] skinData) {
        return MD5Util.encode(Base64.getEncoder().encodeToString(skinData));
    }

}
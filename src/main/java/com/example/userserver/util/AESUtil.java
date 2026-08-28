package com.example.userserver.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 对称加密工具（演示「敏感信息加密存储」）。
 *
 * 真实生产注意：
 *   1) 密钥绝不能硬编码在代码里，要从配置中心/环境变量读取，并定期轮换。
 *   2) IV 必须随机且随密文一起存储（本例为演示固定 IV，生产不可用）。
 *   3) 卡号这类高敏感数据，更推荐「应用层信封加密 + KMS 托管密钥」。
 *
 * 这里用 AES/CBC/PKCS5Padding，密钥/IV 16 字节 = AES-128。
 */
public class AESUtil {

    private static final String KEY = "demoKey123456789"; // 16 字节（演示用，生产需外置）
    private static final String IV = "demoIv1234567890";  // 16 字节（演示用固定 IV）
    private static final String TRANSFORM = "AES/CBC/PKCS5Padding";

    public static String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES"),
                    new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
            byte[] enc = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    public static String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES"),
                    new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
            byte[] dec = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(dec, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }
}

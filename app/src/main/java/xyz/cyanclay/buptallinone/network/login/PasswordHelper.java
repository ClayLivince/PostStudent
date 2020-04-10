package xyz.cyanclay.buptallinone.network.login;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PasswordHelper {

    public static void saveEncrypt(File resourceDir, String id, String vpnPass, String infoPass, @Nullable String jwxtPass, @Nullable String jwglPass) throws IOException {
        File tokenDir = verifyTokenDir(resourceDir);
        SecretKey key = getKey(tokenDir, true);
        FileOutputStream fos = null;
        try {
            File detail = new File(tokenDir, "detail");
            if (detail.exists()) {
                if (!detail.delete()) throw new IOException("Failed to delete detail file.");
            }
            if (detail.createNewFile()) {
                fos = new FileOutputStream(detail);
                fos.write(encrypt(key, id));
                fos.write("\n".getBytes());
                fos.write(encrypt(key, vpnPass));
                fos.write("\n".getBytes());
                fos.write(encrypt(key, infoPass));
                fos.write("\n".getBytes());
                if (jwxtPass != null && jwxtPass.length() != 0) {
                    fos.write(encrypt(key, jwxtPass));
                    fos.write("\n".getBytes("ISO8859-1"));
                }
                if (jwglPass != null && jwglPass.length() != 0) fos.write(encrypt(key, jwglPass));
                fos.flush();
            } else throw new IOException("Failed to create detail file.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String[] loadDecrypt(File resourceDir) throws IOException {
        File tokenDir = verifyTokenDir(resourceDir);
        SecretKey key = getKey(tokenDir, false);
        InputStreamReader isr = null;
        BufferedReader br = null;
        String[] details = new String[5];
        try {
            File detail = new File(tokenDir, "detail");
            if (detail.exists()) {
                isr = new InputStreamReader(new FileInputStream(detail), "ISO8859-1");
                br = new BufferedReader(isr);
                for (int i = 0; i < 5; i++) {
                    String line = br.readLine();
                    if (line != null) {
                        details[i] = decrypt(key, line.replace("\n", ""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return details;
    }

    private static File verifyTokenDir(File resourceDir) throws IOException {
        File tokenDir = new File(resourceDir, "token");
        if (tokenDir.exists()) {
            return tokenDir;
        } else {
            if (tokenDir.mkdir()) {
                return tokenDir;
            } else throw new IOException("Failed to create tokenDir");
        }
    }

    private static synchronized SecretKey getKey(File tokenDir, boolean forceRefresh) throws IOException {
        if (Arrays.asList(tokenDir.list()).contains("key") && !forceRefresh) {
            return readKey(tokenDir);
        } else {
            return writeKey(tokenDir);
        }
    }

    private static synchronized SecretKey readKey(File tokenDir) throws IOException {
        File keyFile = new File(tokenDir, "key");
        ObjectInputStream ois = null;
        SecretKey key = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(keyFile));
            key = (SecretKey) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return key;
    }

    private static synchronized SecretKey writeKey(File tokenDir) throws IOException {
        File keyFile = new File(tokenDir, "key");
        ObjectOutputStream oos = null;
        SecretKey key = null;
        try {
            key = generateKey();
            if (keyFile.exists()) {
                if (!keyFile.delete()) throw new IOException("Failed to delete key File.");
            }
            if (keyFile.createNewFile()) {
                oos = new ObjectOutputStream(new FileOutputStream(keyFile));
                oos.writeObject(key);
                oos.flush();
            } else throw new IOException("Failed to create keyFile.");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return key;
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;
        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(outputKeyLength, secureRandom);
        return keyGenerator.generateKey();
    }

    /**
     * @param clear clear text string
     * @param mode  this should either be Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return byte[] encoded bytes
     * @throws Exception cipher exception
     */
    private static byte[] translate(SecretKey key, String clear, int mode) throws Exception {
        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE)
            throw new IllegalArgumentException("Encryption invalid. Mode should be either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE");
        SecretKeySpec skeySpec = new SecretKeySpec(key.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(mode, skeySpec);
        return cipher.doFinal(clear.getBytes("ISO8859-1"));
    }

    private static byte[] encrypt(SecretKey key, String clear) throws Exception {
        return translate(key, clear, Cipher.ENCRYPT_MODE);
    }

    private static String decrypt(SecretKey key, String encrypted) throws Exception {
        return new String(translate(key, encrypted, Cipher.DECRYPT_MODE));
    }
}
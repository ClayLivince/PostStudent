package xyz.cyanclay.buptallinone.network.login;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordHelper {

    private IvParameterSpec IV;
    private SecretKeySpec key;
    private static String charset = "ISO-8859-1";

    public PasswordHelper(){
        byte[] iv = "APPCREATEDBYCLAY".getBytes();
        IV = new IvParameterSpec(iv);
        String sKey = "BUPTISTHEBESTONE";
        byte[] keyArray = sKey.getBytes(StandardCharsets.US_ASCII);
        key = new SecretKeySpec(keyArray, "AES");
    }

    public void saveEncrypt(File resourceDir, String id, String vpnPass, String infoPass, @Nullable String jwxtPass, @Nullable String jwglPass) throws IOException {
        File tokenDir = verifyTokenDir(resourceDir);
        FileOutputStream fos = null;
        try {
            File detail = new File(tokenDir, "detail");
            if (detail.exists()) {
                if (!detail.delete()) throw new IOException("Failed to delete detail file.");
            }
            if (detail.createNewFile()) {
                fos = new FileOutputStream(detail);
                fos.write(encrypt(id.getBytes(charset)));
                fos.write(encrypt(vpnPass.getBytes(charset)));
                fos.write(encrypt(infoPass.getBytes(charset)));
                if (jwglPass != null && jwglPass.length() != 0) {
                    fos.write(encrypt(jwglPass.getBytes(charset)));
                }
                if (jwxtPass != null && jwxtPass.length() != 0) {
                    fos.write(encrypt(jwxtPass.getBytes(charset)));
                    fos.write("\n".getBytes());
                } else fos.write("\n".getBytes());

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

    public String[] loadDecrypt(File resourceDir) throws IOException {
        File tokenDir = verifyTokenDir(resourceDir);
        InputStreamReader isr = null;
        BufferedReader br = null;
        String[] details = new String[5];
        try {
            File detail = new File(tokenDir, "detail");
            if (detail.exists()) {
                isr = new InputStreamReader(new FileInputStream(detail));
                br = new BufferedReader(isr);
                for (int i = 0; i < 5; i++) {
                    String line = br.readLine();
                    if (line != null) {
                        details[i] = decrypt(line.replace("\n", ""));
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

    private File verifyTokenDir(File resourceDir) throws IOException {
        File tokenDir = new File(resourceDir, "token");
        if (tokenDir.exists()) {
            return tokenDir;
        } else {
            if (tokenDir.mkdir()) {
                return tokenDir;
            } else throw new IOException("Failed to create tokenDir");
        }
    }

    /**
     * @param clear clear text string
     * @param mode  this should either be Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return byte[] encoded bytes
     * @throws Exception cipher exception
     */
    private byte[] translate(byte[] clear, int mode) throws Exception {
        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE)
            throw new IllegalArgumentException("Encryption invalid. Mode should be either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, key, IV);
        return cipher.doFinal(clear);
    }

    private byte[] encrypt(byte[] clear) throws Exception {
        byte[] cipherText = translate(clear, Cipher.ENCRYPT_MODE);
        return Base64.encode(cipherText, Base64.DEFAULT);
    }

    private String decrypt(String encrypted) throws Exception {
        byte[] s = Base64.decode(encrypted, Base64.DEFAULT);
        String decrypted = new String(translate(s, Cipher.DECRYPT_MODE));
        Log.e("decrypted", decrypted);
        return decrypted;
    }
}
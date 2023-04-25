package cn.ios.casegen.zother.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.ios.casegen.util.log.Log;

public class UNZip {

	public static void openMerge(String jdkVersion1, String jdkVersion2, String javaFile) {
		String srcZip1 = "G:\\jdkCompare\\" + jdkVersion1 + "src.zip";
		String srcZip2 = "G:\\jdkCompare\\" + jdkVersion2 + "src.zip";
		String outputDir = "G:\\jdkCompare\\java";
		String outPath1 = outputDir + File.separator + javaFile + jdkVersion1 + ".java";
		String outPath2 = outputDir + File.separator + javaFile + jdkVersion2 + ".java";
		decompressZip(srcZip1, javaFile, outPath1);
		decompressZip(srcZip2, javaFile, outPath2);
		try {
			String cmd = "\"C:\\Program Files\\TortoiseSVN\\bin\\TortoiseMerge.exe\" " + outPath1 + " " + outPath2;
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void decompressZip(String zipPath, String javaFile, String outputFile) {
		File zipFile = new File(zipPath);
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile, Charset.forName("gbk"));
			for (Enumeration<?> entries = zip.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String zipEntryName = entry.getName();
				if (!zipEntryName.replace("/", ".").endsWith(javaFile + ".java")) {
					continue;
				}
				Log.e(zipEntryName);
				InputStream in = zip.getInputStream(entry);

				File file = new File(outputFile.substring(0, outputFile.lastIndexOf(File.separator)));
				if (!file.exists()) {
					file.mkdirs();
				}
				OutputStream out = new FileOutputStream(outputFile);
				byte[] buf1 = new byte[2048];
				int len;
				while ((len = in.read(buf1)) > 0) {
					out.write(buf1, 0, len);
				}
				in.close();
				out.close();
			}
			zip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

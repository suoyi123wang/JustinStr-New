//package cn.ios.junit.util;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//import java.util.Enumeration;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//
///**
// * ʵ���ļ�[��]��ѹ
// *
// * @author Tyler Zhang
// *
// */
//public class UnZipFile {
//
//	/**
//	 * ��ѹ��ָ��Ŀ¼
//	 *
//	 * @param zipPath
//	 * @param descDir
//	 */
//	public static void unZipFiles(String zipPath, String descDir) throws IOException {
//		unZipFiles(new File(zipPath), descDir);
//	}
//
//	/**
//	 * ��ѹ�ļ���ָ��Ŀ¼ ��ѹ����ļ�������֮ǰһ��
//	 *
//	 * @param zipFile ����ѹ��zip�ļ�
//	 * @param descDir ָ��Ŀ¼
//	 */
//	@SuppressWarnings("rawtypes")
//	public static void unZipFiles(File zipFile, String descDir) throws IOException {
//
//		ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));// ��������ļ�������
////        String name = zip.getName().substring(zip.getName().lastIndexOf('\\')+1, zip.getName().lastIndexOf('.'));
////        File pathFile = new File(descDir +name);
//		File pathFile = new File(descDir);
//		if (!pathFile.exists()) {
//			pathFile.mkdirs();
//		}
//
//		for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
//			ZipEntry entry = (ZipEntry) entries.nextElement();
//			String zipEntryName = entry.getName();
//			InputStream in = zip.getInputStream(entry);
////            String outPath = (descDir + name +"/"+ zipEntryName).replaceAll("\\*", "/");
//			String outPath = (descDir + "/" + zipEntryName).replaceAll("\\*", "/");
//
//			// �ж�·���Ƿ����,�������򴴽��ļ�·��
//			File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
//			if (!file.exists()) {
//				file.mkdirs();
//			}
//			// �ж��ļ�ȫ·���Ƿ�Ϊ�ļ���,����������Ѿ��ϴ�,����Ҫ��ѹ
//			if (new File(outPath).isDirectory()) {
//				continue;
//			}
//			// ����ļ�·����Ϣ
////            System.out.println(outPath);
//
//			FileOutputStream out = new FileOutputStream(outPath);
//			byte[] buf1 = new byte[1024];
//			int len;
//			while ((len = in.read(buf1)) > 0) {
//				out.write(buf1, 0, len);
//			}
//			in.close();
//			out.close();
//		}
//		System.out.println("******************Unzip finished********************");
//		return;
//	}
//
//}

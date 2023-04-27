/*
 * Copyright (C) 2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 and only version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see 
 * <https://www.gnu.org/licenses>.
 * 
 * Linking DEPONS statically or dynamically with other modules is making a combined work based on DEPONS. 
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of DEPONS give you permission to combine DEPONS 
 * with free software programs or libraries that are released under the GNU LGPL and with code included in the 
 * standard release of Repast Simphony under the Repast Suite License (or modified versions of such code, with unchanged license). 
 * You may copy and distribute such a system following the terms of the GNU GPL for DEPONS and the licenses of the 
 * other code concerned.
 * 
 * Note that people who make modified versions of DEPONS are not obligated to grant this special exception for 
 * their modified versions; it is their choice whether to do so. 
 * The GNU General Public License gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */

package dk.au.bios.porpoise.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.jgoodies.common.base.Objects;

public class DownloadLandscapes {

	private static final List<LandscapeFile> LANDSCAPES = List.of(
			new LandscapeFile("DanTysk", "DanTysk.zip", "https://depons.eu/files/landscapes/DanTysk-2ce68aba.zip",
					2409804, "2ce68aba7efa1b19358654e150320115dd6b676093e174451dfde3a809d6086e"),
			new LandscapeFile("Gemini", "Gemini.zip", "https://depons.eu/files/landscapes/Gemini-ebd7be0a.zip", 1340405,
					"ebd7be0ab28c898780776f1f48c7652d62b4a56a4a2d87e35c62b775705ccc0f"),
			new LandscapeFile("Homogeneous", "Homogeneous.zip",
					"https://depons.eu/files/landscapes/Homogeneous-fc68407d.zip", 82075,
					"fc68407dd2c7ddd74ad8c9f19ee90fc92f97bb90822eac8473a8fe365f22565d"),
			new LandscapeFile("Kattegat", "Kattegat.zip", "https://depons.eu/files/landscapes/Kattegat-6604cb49.zip",
					16228688, "6604cb494e5508155f06d882777c7cd9eaeb46fa6992465a67477fe8144c1df1"),
			new LandscapeFile("NorthSea", "NorthSea.zip", "https://depons.eu/files/landscapes/NorthSea-02048b58.zip",
					84613681, "02048b58aa2ff59107cdfde38e2b073bce1813de049e2f342a981796f0fa97d7"));

	public static void main(String[] args) throws Exception {
		for (var landscape : LANDSCAPES) {
			System.out.print(landscape.name + ": ");
			try {
				landscape.downloadAndVerify(false);
				System.out.println("completed");
			} catch (IOException e) {
				System.out.println("failed: " + e.getMessage());
			}
		}
	}

	private static class LandscapeFile {
		public final String name;
		public final String filename;
		public final String url;
		public final long numBytes;
		public final String sha256;

		public LandscapeFile(String name, String filename, String url, long numBytes, String sha256) {
			this.name = name;
			this.filename = filename;
			this.url = url;
			this.numBytes = numBytes;
			this.sha256 = sha256;
		}

		void downloadAndVerify(boolean overwriteExisting) throws IOException, NoSuchAlgorithmException {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			File fOut = Paths.get("data", filename).toFile();

			if (fOut.exists() && !overwriteExisting) {
				throw new IOException("File " + filename + " exists and will not be overwritten");
			}

			try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
					FileOutputStream out = new FileOutputStream(fOut)) {

				var buffer = new byte[4096];
				int actRead;
				while ((actRead = in.read(buffer, 0, 4096)) >= 0) {
					out.write(buffer, 0, actRead);
					digest.update(buffer, 0, actRead);
				}

				out.flush();
				if (fOut.length() != numBytes) {
					throw new IOException(
							"Incorrect size for file " + name + ". Is: " + fOut.length() + ". Expected: " + numBytes);
				}

				var readSha256 = digest.digest();
				var readSha256Str = bytesToHex(readSha256);
				if (!Objects.equals(sha256, readSha256Str)) {
					throw new IOException("Incorrect SHA256 checksum for file " + name);
				}
			}
		}

		private static String bytesToHex(byte[] hash) {
			StringBuilder hexString = new StringBuilder(2 * hash.length);
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		}

	}

}

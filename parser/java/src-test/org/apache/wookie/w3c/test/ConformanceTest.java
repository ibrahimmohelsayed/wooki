/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wookie.w3c.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.wookie.w3c.W3CWidgetFactory;
import org.apache.wookie.w3c.exceptions.BadManifestException;
import org.apache.wookie.w3c.exceptions.BadWidgetZipFileException;
import org.apache.wookie.w3c.exceptions.InvalidContentTypeException;
import org.apache.wookie.w3c.W3CWidget;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Abstract base class for conformance tests, including basic utility methods for loading and testing widget test cases from W3C
 */
public abstract class ConformanceTest {
	
	static File download;
	static File output;
	
	/**
	 * Create temporary folders for widget downloads and for outputting results
	 * @throws IOException
	 */
	@BeforeClass
	public static void setup() throws IOException{
		download = File.createTempFile("wookie-download", "wgt");
		output = File.createTempFile("wookie-output", "tmp");
	}
	
	/**
	 * Delete temporary folders
	 */
	@AfterClass
	public static void tearDown(){
		download.delete();
		output.delete();
	}
	
	/**
	 * Download a widget from a URL and parse it
	 * @param url the URL to download the .wgt file from 
	 * @return a W3C Widget object representing the manifest of the downloaded widget
	 * @throws InvalidContentTypeException
	 * @throws BadWidgetZipFileException
	 * @throws BadManifestException
	 * @throws Exception
	 */
	private W3CWidget downloadWidget(String url) throws InvalidContentTypeException, BadWidgetZipFileException, BadManifestException, Exception{
		return downloadWidget(url, true);
	}
	
	/**
	 * Download a widget from a URL and parse it
	 * @param url the URL to download the .wgt file from 
	 * @param ignoreContentType set to true to ignore the content type of the request; otherwise the test will fail if the type is not application/widget
	 * @return a W3C Widget object representing the manifest of the downloaded widget
	 * @throws InvalidContentTypeException
	 * @throws BadWidgetZipFileException
	 * @throws BadManifestException
	 * @throws Exception
	 */
	protected W3CWidget downloadWidget(String url, boolean ignoreContentType) throws InvalidContentTypeException, BadWidgetZipFileException, BadManifestException, Exception{
		W3CWidgetFactory fac = new W3CWidgetFactory();
		fac.setLocalPath("http:localhost/widgets");
		fac.setFeatures(new String[]{"feature:a9bb79c1"});
		fac.setEncodings(new String[]{"UTF-8", "ISO-8859-1","Windows-1252"});
		if (download.exists()) download.delete();
		if (output.exists()) output.delete();
		output.mkdir();
		fac.setOutputDirectory(output.getAbsolutePath());
		return fac.parse(new URL(url),ignoreContentType);
	}
	
	/**
	 * Download a widget from a URL, and return the errors generated by attempting to parse it. 
	 * @param url the URL to download the .wgt file from 
	 * @return the error message generated when parsing the widget
	 */
	public String processWidgetWithErrors(String url){
		try {
			downloadWidget(url);
		} catch (BadWidgetZipFileException e) {
			if (e.getMessage()!=null) return e.getMessage();
			return "Bad Widget Zip File";
		} catch (BadManifestException e) {
			if (e.getMessage()!=null) return e.getMessage();
			return "Bad Manifest";
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
	
	/**
	 * Download a widget from a URL and parse it, returning the resulting W3C Widget object. Will fail if any errors occur in parsing.
	 * @param url the URL to download the .wgt file from 
	 * @return a W3C Widget object representing the manifest of the downloaded widget
	 */
	public W3CWidget processWidgetNoErrors(String url){
		try {
			return downloadWidget(url);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return null;
		}
	}

}

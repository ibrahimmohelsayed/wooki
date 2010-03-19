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
package org.apache.wookie.w3c.impl;

import org.apache.wookie.w3c.ILocalizedEntity;
import org.apache.wookie.w3c.IW3CXMLConfiguration;
import org.apache.wookie.w3c.util.LocalizationUtils;
import org.apache.wookie.w3c.util.UnicodeUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

/**
 * Abstract base class for entities containing i18n or l10n content, including
 * utility methods for extracting and processing text that uses language tags
 * and text direction (e.g. RTL)
 */
public abstract class AbstractLocalizedEntity implements ILocalizedEntity {
	
	/**
	 * a Language string conforming to BCP47
	 */
	protected String lang;
	/**
	 * Text direction conforming to http://www.w3.org/TR/2007/REC-its-20070403/
	 */
	protected String dir;
	
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	
	/**
	 * Checks whether the language tag for the entity is OK.
	 * A null value is OK, as is a BCP47 tag
	 */
	public boolean isValid(){
		if (getLang() == null) return true;
		if (LocalizationUtils.isValidLanguageTag(getLang())) return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.wookie.manifestmodel.IManifestModelBase#fromXML(org.jdom.Element)
	 */
	public void fromXML(Element element) {
		String lang =  UnicodeUtils.normalizeSpaces(element.getAttributeValue(IW3CXMLConfiguration.LANG_ATTRIBUTE, Namespace.XML_NAMESPACE));
		if (!lang.equals("")) setLang(lang);
		dir = getTextDirection(element);
	}

	/**
	 * Returns the text content of an element, recursively adding
	 * any text nodes found in its child nodes AND any <span> elements
	 * that include localization information
	 * @param element
	 * @return a string containing the element text
	 */
	public static String getLocalizedTextContent(Element element){
		String content = "";
		for (Object node:element.getContent()){
			if (node instanceof Element){
				if ((((Element) node).getAttribute("dir")!= null || ((Element) node).getAttribute("lang")!= null)  && ((Element)node).getName().equals("span")){
					content += "<span dir=\""+getTextDirection((Element)node)+"\"";
					if (((Element)node).getAttribute("lang")!=null)
						content += " xml:lang=\""+((Element)node).getAttribute("lang").getValue()+"\"";
					content +=">";
					content += getLocalizedTextContent((Element)node);
					content += "</span>";
				} else {
					content += getLocalizedTextContent((Element)node);
				}
			}
			if (node instanceof Text){
				content += ((Text)node).getText();
			}
		}
		return UnicodeUtils.normalizeWhitespace(content);
	}
	

	public static final String LEFT_TO_RIGHT = "ltr";
	
	public static final String RIGHT_TO_LEFT = "rtl";
	
	/**
	 * Returns the direction (rtl or ltr) of the child text of an element
	 * @param element the element to parse
	 * @return the string "ltr" or "rtl"
	 */
	public static String getTextDirection(Element element){
		try {
			if (element.isRootElement()) return LEFT_TO_RIGHT;
			Attribute dir = element.getAttribute(IW3CXMLConfiguration.DIR_ATRRIBUTE);
			if (dir == null){
				return getTextDirection(element.getParentElement());
			} else {
				String dirValue = UnicodeUtils.normalizeSpaces(dir.getValue());
				if (dirValue.equals("rtl")) return RIGHT_TO_LEFT;
				if (dirValue.equals("ltr")) return LEFT_TO_RIGHT;
				return getTextDirection(element.getParentElement());			
			}
		} catch (Exception e) {
			// In the case of an error we always return the default value
			return LEFT_TO_RIGHT;
		}
	}


}
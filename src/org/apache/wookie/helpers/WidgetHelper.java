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
package org.apache.wookie.helpers;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wookie.beans.License;
import org.apache.wookie.beans.PreferenceDefault;
import org.apache.wookie.beans.Widget;
import org.apache.wookie.beans.WidgetIcon;
import org.apache.wookie.beans.WidgetType;

/**
 * A helper for Widgets
 * @author scott wilson
 *
 */
public class WidgetHelper {
	
	private static final String XMLDECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	/**
	 * Generate a Widgets representation doc in XML for a single widget
	 * @param widget
	 * @param localIconPath
	 * @return
	 */
	public static String createXMLWidgetsDocument(Widget widget, String localIconPath){
		Widget[] widgets = {widget};
		return createXMLWidgetsDocument(widgets, localIconPath);
	}
	
	/**
	 * Generate a Widgets representation doc in XML for an array of widgets, e.g. a catalogue
	 * 
	 * @param widgets
	 * @param localIconPath
	 * @return
	 */
	public static String createXMLWidgetsDocument(Widget[] widgets, String localIconPath){
		String document = XMLDECLARATION;
		document += "\n<widgets>\n";
		for (Widget widget:widgets){
			document += toXml(widget, localIconPath);
		}
		document += "</widgets>\n";
		return document;
	}
	
	/**
	 * Returns an XML representation of the given widget.
	 * 
	 * @param widget the widget to represent
	 * @param localIconPath the local path to prefix any local icons, typically the server URL
	 * @return the XML representation of the widget
	 */
	public static String toXml(Widget widget, String localIconPath){
		String width = "";
		String height = "";
		if (widget.getWidth()!=null)  width = widget.getWidth().toString();
		if (widget.getHeight()!=null) height = widget.getHeight().toString();
		
		if (widget == null) return null;
		String out = "";
		URL urlWidgetIcon = null;
		out += "\t<widget " +
				"id=\""+widget.getId()
				+"\" identifier=\"" + widget.getGuid() 
				+"\" width=\"" + width
				+"\" height=\"" + height
				+ "\" version=\"" + widget.getVersion() 
				+ "\">\n";
		out += "\t\t<title "; 
		if (widget.getWidgetShortName() != null) out +="short=\""+widget.getWidgetShortName() + "\"";
		out +=">" + widget.getWidgetTitle() + "</title>\n";
		out += "\t\t<description>" + widget.getWidgetDescription()
				+ "</description>\n";
		
		WidgetIcon[] icons = WidgetIcon.findForWidget(widget);
		if (icons!=null){
		for (WidgetIcon icon: icons){
			try {
				// If local...
				if (!icon.getSrc().startsWith("http")) {
					urlWidgetIcon = new URL(localIconPath+icon.getSrc());
				} else {
					urlWidgetIcon = new URL(icon.getSrc());
				}
				out += "\t\t<icon";
				if (icon.getHeight()!=null) out += " height=\""+icon.getHeight()+"\"";
				if (icon.getWidth()!=null) out += " width=\""+icon.getWidth()+"\"";
				out += ">"+urlWidgetIcon.toString() + "</icon>\n";
			} catch (MalformedURLException e) {
				// don't export icon field if its not a valid URL
			}
		}
		}
		
		// Do tags/services/categories
		WidgetType[] types = WidgetType.findByValue("widget", widget);
		for (WidgetType type:types){
			out +="\t\t<category>"+type.getWidgetContext()+"</category>\n";
		}
		
		// Do author
		out += "\t\t<author";
		if (widget.getWidgetAuthorEmail() != null) out+= " email=\""+widget.getWidgetAuthorEmail()+"\"";
		if (widget.getWidgetAuthorHref() != null) out+= " href=\""+widget.getWidgetAuthorHref()+"\"";
		out += ">";
		if (widget.getWidgetAuthor()!=null) out += widget.getWidgetAuthor();
		out += "</author>\n";
		
		// Do license
		License[] licenses = License.findByValue("widget", widget);
		for (License license: licenses){
			out +="\t\t<license ";
			if (license.getLang()!=null) out+=" xml:lang=\""+license.getLang()+"\"";
			if (license.getHref()!=null) out+=" href=\""+license.getHref()+"\"";
			if (license.getDir()!=null) out+=" its:dir=\""+license.getDir()+"\"";
			out+=">"+license.getText()+"</license>\n";
		}

		// Do preference defaults
		PreferenceDefault[] prefs = PreferenceDefault.findByValue("widget",widget);
		for (PreferenceDefault pref : prefs) {
			out += "\t\t<preference name=\"" + pref.getPreference() + "\"  value=\""+pref.getValue()+"\"  readonly=\"" + (pref.isReadOnly()? "true" : "false") + "\"/>";
		}
		out += "\t</widget>\n";

		return out;
	}

}

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

package org.apache.wookie.manifestmodel.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wookie.exceptions.BadManifestException;
import org.apache.wookie.manifestmodel.IAccessEntity;
import org.apache.wookie.manifestmodel.IAuthorEntity;
import org.apache.wookie.manifestmodel.IContentEntity;
import org.apache.wookie.manifestmodel.IDescriptionEntity;
import org.apache.wookie.manifestmodel.IFeatureEntity;
import org.apache.wookie.manifestmodel.IIconEntity;
import org.apache.wookie.manifestmodel.ILicenseEntity;
import org.apache.wookie.manifestmodel.IManifestModel;
import org.apache.wookie.manifestmodel.INameEntity;
import org.apache.wookie.manifestmodel.IPreferenceEntity;
import org.apache.wookie.manifestmodel.IW3CXMLConfiguration;
import org.apache.wookie.util.RandomGUID;
import org.apache.wookie.util.UnicodeUtils;
import org.jdom.Element;
import org.jdom.Namespace;
/**
 * @author Paul Sharples
 * @version $Id: WidgetManifestModel.java,v 1.3 2009-09-02 18:37:31 scottwilson Exp $
 */
public class WidgetManifestModel implements IManifestModel {
	
	static Logger fLogger = Logger.getLogger(WidgetManifestModel.class.getName());
	
	private String fIdentifier;
	private String fVersion;
	private int fHeight;
	private int fWidth;
	private String fViewModes;
	private String fLang;
	private List<INameEntity> fNamesList;
	private List<IDescriptionEntity> fDescriptionsList;
	private IAuthorEntity fAuthor;
	private List<ILicenseEntity> fLicensesList;
	private List<IIconEntity> fIconsList;
	private List<IAccessEntity> fAccessList;
	private IContentEntity fContent;
	private List<IFeatureEntity> fFeaturesList;
	private List<IPreferenceEntity> fPreferencesList;

	public WidgetManifestModel() {
		super();
		fNamesList = new ArrayList<INameEntity>();
		fDescriptionsList = new ArrayList<IDescriptionEntity>();
		fLicensesList = new ArrayList<ILicenseEntity>();
		fIconsList = new ArrayList<IIconEntity>();
		fAccessList = new ArrayList<IAccessEntity>();
		fFeaturesList = new ArrayList<IFeatureEntity>();
		fPreferencesList = new ArrayList<IPreferenceEntity>();
	}
	
	public String getViewModes() {
		return fViewModes;
	}
	
	public String getVersion() {
		return fVersion;
	}
	
	public List<IPreferenceEntity> getPrefences(){
		return fPreferencesList;
	}
	
	public List<IFeatureEntity> getFeatures(){
		return fFeaturesList;
	}
	
	public String getFirstIconPath(){
		if(fIconsList.size() > 0){
			return fIconsList.get(0).getSrc();
		}
		else {
			return IW3CXMLConfiguration.DEFAULT_ICON_PATH;
		}
	}
	
	public String getAuthor(){
		if (fAuthor == null) return null;
		return fAuthor.getAuthorName();
	}
	
	public void updateIconPaths(String path){
		for(IIconEntity icon : fIconsList){
			if(!icon.getSrc().startsWith("http:")){
				icon.setSrc(path + icon.getSrc());
			}
		}
	}
	
	public List<IDescriptionEntity> getDescriptions(){
		return fDescriptionsList;
	}
	
	public List<INameEntity> getNames() {
		return fNamesList;
	}
	
	public String getLocalName(String locale){
		String nonlocalizedvalue = null;
		for (INameEntity name:fNamesList.toArray(new INameEntity[fNamesList.size()])){
			if (name.getLanguage().equals(locale)) return name.getName();
			if (name.getLanguage().equals("")) nonlocalizedvalue = name.getName();
		}
		if (nonlocalizedvalue == null) return IW3CXMLConfiguration.UNKNOWN;
		return nonlocalizedvalue;
	}
	
	public String getLocalDescription(String locale){
		String nonlocalizedvalue = "";
		for (IDescriptionEntity desc:fDescriptionsList.toArray(new IDescriptionEntity[fDescriptionsList.size()])){
			if (desc.getLanguage().equals(locale)) return desc.getDescription();
			if (desc.getLanguage().equals("")) nonlocalizedvalue = desc.getDescription();
		}
		if (nonlocalizedvalue == null) return IW3CXMLConfiguration.UNKNOWN;
		return nonlocalizedvalue;	
	}
	
	public List<IIconEntity> getIconsList() {
		return fIconsList;
	}

	public void setIconsList(List<IIconEntity> iconsList) {
		fIconsList = iconsList;
	}
	
	public String getIdentifier() {
		return fIdentifier;
	}
	
	public IContentEntity getContent() {
		return fContent;
	}

	public void setContent(IContentEntity content) {
		fContent = content;
	}

	public int getHeight() {
		return fHeight;
	}

	public void setHeight(int height) {
		fHeight = height;
	}
	
	public int getWidth() {
		return fWidth;
	}

	public void setWidth(int width) {
		fWidth = width;
	}

	public boolean hasContentEntity(){
		if(fContent == null){
			return false;
		}
		return true;
	}
	
	public String getXMLTagName() {
		return IW3CXMLConfiguration.WIDGET_ELEMENT;
	}

	@SuppressWarnings("deprecation")
	public void fromXML(Element element) throws BadManifestException {						
		// check the namespace uri 
		if(!element.getNamespace().getURI().equals(IW3CXMLConfiguration.MANIFEST_NAMESPACE)){			
			throw new BadManifestException("'"+element.getNamespace().getURI() 
					+ "' is a bad namespace. (Should be '" + IW3CXMLConfiguration.MANIFEST_NAMESPACE +"')");
		}				
		// IDENTIFIER IS OPTIONAL
		fIdentifier = element.getAttributeValue(IW3CXMLConfiguration.ID_ATTRIBUTE);
		if(fIdentifier == null){
			// try the old one
			fIdentifier = element.getAttributeValue(IW3CXMLConfiguration.UID_ATTRIBUTE);
		}
		if(fIdentifier == null){
			//give up & generate one
			RandomGUID r = new RandomGUID();
			fIdentifier = "generated-uid-" + r.toString();
		} else {
			//normalize spaces
			fIdentifier = UnicodeUtils.normalizeSpaces(fIdentifier);
		}
		// VERSION IS OPTIONAL		
		fVersion = element.getAttributeValue(IW3CXMLConfiguration.VERSION_ATTRIBUTE);
		if(fVersion == null){
			// give up 
			fVersion = IW3CXMLConfiguration.DEFAULT_WIDGET_VERSION;
		} else {
			fVersion = UnicodeUtils.normalizeSpaces(fVersion);
		}
		// HEIGHT IS OPTIONAL		
		String height  = element.getAttributeValue(IW3CXMLConfiguration.HEIGHT_ATTRIBUTE);
		if(height != null){
			fHeight  = Integer.valueOf(height);
		}
		else{ 
			// give up
			fHeight = IW3CXMLConfiguration.DEFAULT_HEIGHT_LARGE;
		}
		// WIDTH IS OPTIONAL		
		String width  = element.getAttributeValue(IW3CXMLConfiguration.WIDTH_ATTRIBUTE);
		if(width != null){
			fWidth = Integer.valueOf(width);
		}
		else{
			// give up
			fWidth = IW3CXMLConfiguration.DEFAULT_WIDTH_LARGE;
		}
		// VIEWMODES IS OPTIONAL	
		fViewModes = element.getAttributeValue(IW3CXMLConfiguration.MODE_ATTRIBUTE);
		if(fViewModes == null){
			fViewModes = IW3CXMLConfiguration.DEFAULT_VIEWMODE;
		} else {
			fViewModes = UnicodeUtils.normalizeSpaces(fViewModes);
		}
		// xml:lang optional
		fLang = element.getAttributeValue(IW3CXMLConfiguration.LANG_ATTRIBUTE, Namespace.XML_NAMESPACE);
		if(fLang == null){
			fLang = IW3CXMLConfiguration.DEFAULT_LANG;
		}

		
		// parse the children
		for(Object o : element.getChildren()) {
			Element child = (Element)o;
			String tag = child.getName();			

			// NAME IS OPTIONAL - get the name elements (multiple based on xml:lang)
			if(tag.equals(IW3CXMLConfiguration.NAME_ELEMENT)) {				
				INameEntity aName = new NameEntity();
				aName.fromXML(child);				
				// add it to our list
				fNamesList.add(aName);
			}
			
			// DESCRIPTION IS OPTIONAL multiple on xml:lang
			if(tag.equals(IW3CXMLConfiguration.DESCRIPTION_ELEMENT)) {				
				IDescriptionEntity aDescription = new DescriptionEntity();
				aDescription.fromXML(child);
				// add it to our list
				fDescriptionsList.add(aDescription);
			}
			
			// AUTHOR IS OPTIONAL - can only be one, ignore subsequent repetitions
			if(tag.equals(IW3CXMLConfiguration.AUTHOR_ELEMENT) && fAuthor == null) {
				fAuthor = new AuthorEntity();
				fAuthor.fromXML(child);
			}		
		
			// LICENSE IS OPTIONAL - can be many
			if(tag.equals(IW3CXMLConfiguration.LICENSE_ELEMENT)) {				
				ILicenseEntity aLicense = new LicenseEntity();
				aLicense.fromXML(child);
				fLicensesList.add(aLicense);
			}
			
			// ICON IS OPTIONAL - can be many
			if(tag.equals(IW3CXMLConfiguration.ICON_ELEMENT)) {						
				IIconEntity anIcon = new IconEntity();
				anIcon.fromXML(child);
				fIconsList.add(anIcon);
			}
			
			// ACCESS IS OPTIONAL  can be many 
			// (not sure if this has been removed from the spec?)
			if(tag.equals(IW3CXMLConfiguration.ACCESS_ELEMENT)) {											
				IAccessEntity access = new AccessEntity();
				access.fromXML(child);
				fAccessList.add(access);
			}
			
			// CONTENT IS OPTIONAL - can only be 0 or 1
			// Only the first CONTENT element should be considered, further instances MUST be ignored
			if(tag.equals(IW3CXMLConfiguration.CONTENT_ELEMENT)) {	
				if (fContent == null){
					fContent = new ContentEntity();						
					fContent.fromXML(child);
				}
			}
			
			// FEATURE IS OPTIONAL - can be many
			if(tag.equals(IW3CXMLConfiguration.FEATURE_ELEMENT)) {
				IFeatureEntity feature = new FeatureEntity();
				feature.fromXML(child);
				fFeaturesList.add(feature);
			}
			
			// PREFERENCE IS OPTIONAL - can be many
			if(tag.equals(IW3CXMLConfiguration.PREFERENCE_ELEMENT)) {
				IPreferenceEntity preference = new PreferenceEntity();
				preference.fromXML(child);
				fPreferencesList.add(preference);
			}
			
		}
	}

}
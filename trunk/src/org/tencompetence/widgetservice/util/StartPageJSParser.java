/*
 * Copyright (c) 2007, Consortium Board TENCompetence
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the TENCompetence nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY CONSORTIUM BOARD TENCOMPETENCE ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONSORTIUM BOARD TENCOMPETENCE BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.tencompetence.widgetservice.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
/**
 * Parse the HTML start page & add the JS file links
 * 
 * We need to add in the following links to the HTML file.
 *
 * <script type="text/javascript" src="/wookie/dwr/util.js"></script>
 * <script type="text/javascript" src="/wookie/dwr/engine.js"></script>
 * <script type="text/javascript" src="/wookie/dwr/interface/WidgetImpl.js"></script>
 * <script type="text/javascript" src="/wookie/shared/js/wookie-wrapper.js"></script>
 *
 * @author Paul Sharples
 * @version $Id: StartPageJSParser.java,v 1.1 2008-12-02 18:39:42 ps3com Exp $
 */
public class StartPageJSParser implements IStartPageConfiguration {
	
	static Logger _logger = Logger.getLogger(StartPageJSParser.class.getName());

	private HtmlCleaner fCleaner = null;
	private CleanerProperties fProps = null;
	private File fStartPage = null;
	
	public StartPageJSParser(File startPage) {
		fStartPage = startPage;
		doParse();
	}
	
	private boolean doesAttributeValueExistsInNode(TagNode node, String attrName, String attrValue){
		TagNode foundNode = node.findElementByAttValue(attrName, attrValue, false, true);
		if(foundNode == null){
			return false;
		}
		return true;
	}
	
	private TagNode createScriptTag(String srcAttribute){
		TagNode js = new TagNode(SCRIPT_TAG);
		js.addAttribute(TYPE_ATTRIBUTE, TYPE_ATTRIBUTE_VALUE);
		js.addAttribute(SRC_ATTRIBUTE, srcAttribute);
		return js;
	}
	
	private void doParse(){
		fCleaner = new HtmlCleaner();
		// take default cleaner properties		
		fProps = fCleaner.getProperties();
		fProps.setOmitDoctypeDeclaration(false);
		fProps.setOmitXmlDeclaration(true);
		fProps.setUseCdataForScriptAndStyle(false);
		fProps.setUseEmptyElementTags(false);		
		try {
			TagNode htmlNode = fCleaner.clean(fStartPage);
			// TODO catch any fall out here from not having a head tag in the html
			TagNode headNode = htmlNode.findElementByName(HEAD_TAG, false);
			if(headNode != null){
				if(!doesAttributeValueExistsInNode(headNode, SRC_ATTRIBUTE, DWR_UTIL_SRC_VALUE)){
					_logger.debug("DWR_UTIL_SRC_VALUE NOT found");
					TagNode jsTag = createScriptTag(DWR_UTIL_SRC_VALUE);
					headNode.addChild(jsTag);					
				}
				if(!doesAttributeValueExistsInNode(headNode, SRC_ATTRIBUTE, DWR_ENGINE_SRC_VALUE)){
					_logger.debug("DWR_ENGINE_SRC_VALUE NOT found");
					TagNode jsTag = createScriptTag(DWR_ENGINE_SRC_VALUE);
					headNode.addChild(jsTag);
				}
				if(!doesAttributeValueExistsInNode(headNode, SRC_ATTRIBUTE, WIDGET_IMPL_SRC_VALUE)){
					_logger.debug("WIDGET_IMPL_SRC_VALUE NOT found");
					TagNode jsTag = createScriptTag(WIDGET_IMPL_SRC_VALUE);
					headNode.addChild(jsTag);
				}
				if(!doesAttributeValueExistsInNode(headNode, SRC_ATTRIBUTE, WOOKIE_WRAPPER_SRC_VALUE)){
					_logger.debug("WOOKIE_WRAPPER_SRC_VALUE NOT found");
					TagNode jsTag = createScriptTag(WOOKIE_WRAPPER_SRC_VALUE);
					headNode.addChild(jsTag);
				}			
				PrettyXmlSerializer ser = new PrettyXmlSerializer(fProps);						
				ser.writeXmlToFile(htmlNode, fStartPage.getAbsolutePath());		
			}
		} 
		catch (IOException ex) {
			_logger.error("doParse() failed:", ex);
		}
	}
	
}
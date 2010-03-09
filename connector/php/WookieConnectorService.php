<?php

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

require("WookieConnectorExceptions.php");
require("WookieServerConnection.php");
require("WidgetInstances.php");
require("Widget.php");
require("WidgetInstance.php");
require("WidgetProperties.php");
require("User.php");
require("HTTP_Response.php");


class WookieConnectorService {
	private $conn;
	public  $WidgetInstances;
	private $user;
	private $httpStreamCtx;

	function __construct($url, $apiKey, $sharedDataKey, $loginName, $screenName = null) {
		$this->setConnection(new WookieServerConnection($url, $apiKey, $sharedDataKey));
		$this->setWidgetInstancesHolder();
		$this->setUser($loginName, $screenName);
		$this->setHttpStreamContext(array('http' => array('timeout' => 15)));
	}

	public function setConnection($newConn) {
		$this->conn = $newConn;
	}

	public function getConnection() {
		return $this->conn;
	}

	public function setWidgetInstancesHolder() {
		$this->WidgetInstances = new WidgetInstances();
	}

	public function setUser($loginName, $screenName = null) {
		if($screenName == null) {
			$screenName = $loginName;
		}
		$this->user = new User($loginName, $screenName);
	}

	public function getUser() {
		return $this->user;
	}
	
	private function setHttpStreamContext($params = null) {
		$this->httpStreamCtx = @stream_context_create($params);
	}
	
	private function getHttpStreamContext() {
		return $this->httpStreamCtx;
	}

	/* Do HTTP request
	 /* @return new HTTP_Response instance */

	private function do_request($url, $data, $method = 'POST')
	{
		if(is_array($data)) {
		 // convert variables array to string:
			$_data = array();
			while(list($n,$v) = each($data)){
				$_data[] = urlencode($n)."=".urlencode($v);
			}
			$data = implode('&', $_data);
		}

		$params = array('http' => array(
                  'method' => $method,
                  'content' => $data,
     			  'timeout' => 15
		));
		$this->setHttpStreamContext($params);
		$response = @file_get_contents($url, false, $this->getHttpStreamContext());
		
		//revert back to default value for other requests
		$this->setHttpStreamContext(array('http' => array('timeout' => 15)));

		return new HTTP_Response($response, $http_response_header);
	}


	/**
	 * Get or create an instance of a widget.
	 *
	 * @param widget
	 * @return the ID of the widget instance
	 * @throws IOException
	 * @throws SimalRepositoryException
	 */

	public function getOrCreateInstance($Widget_or_GUID = null) {
		try {
			if(is_object($Widget_or_GUID)) {
				$guid = $Widget_or_GUID->getIdentifier();
			} else {
				$guid = $Widget_or_GUID;
			}
			if($guid == '') {
				throw new WookieConnectorException("No GUID nor Widget object");
			}
			$requestUrl = $this->getConnection()->getURL().'widgetinstances';
			$request.= '&api_key='.$this->getConnection()->getApiKey();
			$request.= '&servicetype=';
			$request.= '&userid='.$this->getUser()->getLoginName();
			$request.= '&shareddatakey='.$this->getConnection()->getSharedDataKey();
			$request.= '&widgetid='.$guid;

			if(!$this->checkURL($requestUrl)) {
				throw new WookieConnectorException("URL for supplied Wookie Server is malformed: ".$requestUrl);
			}
			$response = $this->do_request($requestUrl, $request);

			//if instance was created, perform second request to get widget instance
			if($response->getStatusCode() == 201) {
				$response = $this->do_request($requestUrl, $request);
			}
			if($response->getStatusCode() == 401) { throw new WookieConnectorException("Invalid API key"); }

			$instance = $this->parseInstance($guid, $response->getResponseText());
			$this->WidgetInstances->put($instance);
			return $instance;
		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		}
		return false;
	}


	/**
	 * Record an instance of the given widget.
	 *
	 * @param xml description of the instance as returned by the widget server when the widget was instantiated.
	 * @return new Widget instance
	 */
	private function parseInstance($widgetGuid, $xml) {
		$xmlWidgetData = @simplexml_load_string($xml);
		if(is_object($xmlWidgetData)) {
			//print_r($xmlWidgetData);
			$url = (string) $xmlWidgetData->url;
			$title = (string) $xmlWidgetData->title;
			$height = (string) $xmlWidgetData->height;
			$width = (string) $xmlWidgetData->width;
			$maximize = (string) $xmlWidgetData->maximize;
			$instance = new WidgetInstance($url, $widgetGuid, $title, $height, $width, $maximize);
			return $instance;
		}
		return false;
	}

	/**
	 * Check if URL is parsable.
	 *
	 * @param url
	 * @return boolean
	 */

	private function checkURL($url) {
		$UrlCheck = @parse_url($url);
		if($UrlCheck['scheme'] != 'http' || $UrlCheck['host'] == null || $UrlCheck['path'] == null) {
			return false;
		}
		return true;
	}

	/**
	 * @refactor At time of writing the REST API for adding a participant is broken so we are
	 * using the non-REST approach. The code for REST API is commented out and should be used
	 * in the future.
	 * @return boolean true - if added/exists - false if some error
	 */

	public function addParticipant($widgetInstance, $User)  {
		$Url = $this->getConnection()->getURL().'participants';

		try {
			if(!is_object($widgetInstance)) throw new WookieWidgetInstanceException('No Widget instance');
			if(!is_object($User)) throw new WookieConnectorException('No User object');

			$data = array(
				'api_key' => $this->getConnection()->getApiKey(),
				'shareddatakey' => $this->getConnection()->getSharedDataKey(),
				'userid' => $this->getUser()->getLoginName(),
				'widgetid' => $widgetInstance->getIdentifier(),
				'participant_id' => $this->getUser()->getLoginName(),
				'participant_display_name' => $User->getScreenName(),
				'participant_thumbnail_url' => $User->getThumbnailUrl(),
			);

			if(!$this->checkURL($Url)) {
				throw new WookieConnectorException("Participants rest URL is incorrect: ".$Url);
			}

			$response = $this->do_request($Url, $data);
			$statusCode = $response->getStatusCode();

			switch($statusCode) {
		  case 200: //participant already exists
		  	return true;
		  	break;
		  case 201:
		  	return true; //new participant added
		  	break;
		  case ($statusCode > 201):
		  	throw new WookieConnectorException($response->headerToString().'<br />'.$response->getResponseText());
		  	break;
			}

		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		} catch (WookieWidgetInstanceException $e) {
			echo '<b>function.addParticipant:</b> '.$e->getMessage().'<br />';
		}
	return false;
	}

	/**
	 * @refactor Delete participant
	 * @param WidgetInstance $widgetInstance
	 * @param UserInstance $User
	 * @return boolean true - if deleted, false - if not found
	 */

	public function deleteParticipant($widgetInstance, $User)  {
		$Url = $this->getConnection()->getURL().'participants';

		try {
			if(!is_object($widgetInstance)) throw new WookieWidgetInstanceException('No Widget instance');
			if(!is_object($User)) throw new WookieConnectorException('No User object');

			$request = '?api_key='.$this->getConnection()->getApiKey();
			$request .= '&shareddatakey='.$this->getConnection()->getSharedDataKey();
			$request .= '&userid='.$this->getUser()->getLoginName();
			$request .= '&widgetid='.$widgetInstance->getIdentifier();
			$request .= '&participant_id='.$this->getUser()->getLoginName();


			if(!$this->checkURL($Url)) {
				throw new WookieConnectorException("Participants rest URL is incorrect: ".$Url);
			}

			$response = $this->do_request($Url.$request, false, 'DELETE');
			$statusCode = $response->getStatusCode();

		switch($statusCode) {
		  case 200: //participant deleted
		  	return true;
		  	break;
		  case 404:
		  	return false; //participant not found
		  	break;
		  case ($statusCode > 201):
		  	throw new WookieConnectorException($response->headerToString().'<br />'.$response->getResponseText());
		  	break;
		}

		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		} catch (WookieWidgetInstanceException $e) {
			echo '<b>function.deleteParticipant:</b> '.$e->getMessage().'<br />';
		}
		return false;
	}

	/**
	 * Get the array of users for a widget instance
	 * @param instance
	 * @return an array of users
	 * @throws WookieConnectorException
	 */
	public function getUsers($widgetInstance) {
		$Url = $this->getConnection()->getURL().'participants';
		$Users = array();
		try {
			if(!is_object($widgetInstance)) throw new WookieWidgetInstanceException('No Widget instance');
			$request = '?api_key='.$this->getConnection()->getApiKey();
			$request .= '&shareddatakey='.$this->getConnection()->getSharedDataKey();
			$request .= '&userid='.$this->getUser()->getLoginName();
			$request .= '&widgetid='.$widgetInstance->getIdentifier();

			if(!$this->checkURL($Url)) {
				throw new WookieConnectorException("Participants rest URL is incorrect: ".$Url);
			}

			$response = new HTTP_Response(@file_get_contents($Url.$request, false, $this->getHttpStreamContext()), $http_response_header);
			if($response->getStatusCode() > 200) throw new WookieConnectorException($response->headerToString().'<br />'.$response->getResponseText());

			$xmlObj = @simplexml_load_string($response->getResponseText());

			if(is_object($xmlObj)) {
				foreach($xmlObj->children() as $participant) {
					$participantAttr = $participant->attributes();

					$id = (string) $participantAttr->id;
					$name = (string) $participantAttr->display_name;
					$thumbnail_url = (string) $participantAttr->thumbnail_url;

					$newUser = new User($id, $name, $thumbnail_url);
					array_push($Users, $newUser);
				}
			} else {
				throw new WookieConnectorException('Problem getting participants');
			}

		} catch (WookieWidgetInstanceException $e) {
			echo '<b>function.getUsers:</b> '.$e->getMessage().'<br />';
		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		}
		return $Users;
	}



	/**
	 * Get a set of all the available widgets in the server. If there is an error
	 * communicating with the server return an empty set, or the set received so
	 * far in order to allow the application to proceed. The application should
	 * display an appropriate message in this case.
	 *
	 * @return array of available widgets
	 * @throws WookieConnectorException
	 */

	public function getAvailableWidgets() {
		$widgets = array();
		try {
			$request = $this->getConnection()->getURL().'widgets?all=true';

			if(!$this->checkURL($request)) {
				throw new WookieConnectorException("URL for Wookie is malformed");
			}

			$response = new HTTP_Response(@file_get_contents($request, false, $this->getHttpStreamContext()), $http_response_header);
			$xmlObj = @simplexml_load_string($response->getResponseText());

			if(is_object($xmlObj)) {
				foreach($xmlObj->children() as $widget) {
				 $id = (string) $widget->attributes()->identifier;
				 $title = (string) $widget->title;
				 $description = (string) $widget->description;
				 $iconURL = (string) $widget->attributes()->icon;
				 if($iconURL == '') {
						$iconURL = (string) 'http://www.oss-watch.ac.uk/images/logo2.gif';
					}
					$Widget = new Widget($id, $title, $description, $iconURL);
					$widgets[$id] = $Widget;
				}
			} else {
				throw new WookieConnectorException('Problem getting available widgets');
			}

	 } catch(WookieConnectorException $e) {
			echo $e->errorMessage();
		}
		return $widgets;
	}

	/**
	 * Set property for Widget instance
	 *
	 * @return new Property instance
	 * @throws WookieConnectorException, WookieWidgetInstanceException
	 */

	public function setProperty($widgetInstance = null, $propertyInstance = null) {
		$Url = $this->getConnection()->getURL().'properties';

		try {
			if(!is_object($widgetInstance)) throw new WookieWidgetInstanceException('No Widget instance');
			if(!is_object($propertyInstance)) throw new WookieConnectorException('No properties instance');

			$data = array(
				'api_key' => $this->getConnection()->getApiKey(),
				'shareddatakey' => $this->getConnection()->getSharedDataKey(),
				'userid' => $this->getUser()->getLoginName(),
				'widgetid' => $widgetInstance->getIdentifier(),
				'propertyname' => $propertyInstance->getName(),
				'propertyvalue' => $propertyInstance->getValue(),
				'is_public' => $propertyInstance->isPublic(),
			);

			if(!$this->checkURL($Url)) {
				throw new WookieConnectorException("Properties rest URL is incorrect: ".$Url);
			}

			$response = $this->do_request($Url, $data);
			$statusCode = $response->getStatusCode();

		switch($statusCode) {
		  case 201:
		  	return $propertyInstance; //new property added, let's return initial Property instance
		  	break;
		  case ($statusCode != 201):
		  	throw new WookieConnectorException($response->headerToString().'<br />'.$response->getResponseText());
		  	break;
			}

		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		} catch (WookieWidgetInstanceException $e) {
			echo '<b>function.setProperty:</b> '.$e->getMessage().'<br />';
		}
		return false;
	}

	/**
	 * Get property for Widget instance
	 *
	 * @return new Property(), if request fails, return false;
	 * @throws WookieConnectorException, WookieWidgetInstanceException
	 */

	public function getProperty($widgetInstance = null, $propertyInstance = null) {
		$Url = $this->getConnection()->getURL().'properties';

		try {
			if(!is_object($widgetInstance)) throw new WookieWidgetInstanceException('No Widget instance');
			if(!is_object($propertyInstance)) throw new WookieConnectorException('No properties instance');

			$data = array(
				'api_key' => $this->getConnection()->getApiKey(),
				'shareddatakey' => $this->getConnection()->getSharedDataKey(),
				'userid' => $this->getUser()->getLoginName(),
				'widgetid' => $widgetInstance->getIdentifier(),
				'propertyname' => $propertyInstance->getName()
			);
			$request = @http_build_query($data);

			if(!$this->checkURL($Url)) {
				throw new WookieConnectorException("Properties rest URL is incorrect: ".$Url);
			}

			$response = new HTTP_Response(@file_get_contents($Url.'?'.$request, false, $this->getHttpStreamContext()), $http_response_header);
			$statusCode = $response->getStatusCode();
			if($statusCode != 200) {
				throw new WookieConnectorException($response->headerToString().'<br />'.$response->getResponseText());
			}
			return new Property($propertyInstance->getName(), $response->getResponseText());

		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		} catch (WookieWidgetInstanceException $e) {
			echo '<b>function.getProperty:</b> '.$e->getMessage().'<br />';
		}
		return false;
	}

	/**
	 * Delete property for Widget instance
	 *
	 * @access Public
	 * @return boolean true/false -- true if deleted, false if doesnt exist
	 * @throws WookieConnectorException, WookieWidgetInstanceException
	 */

	public function deleteProperty($widgetInstance = null, $propertyInstance = null) {
		$Url = $this->getConnection()->getURL().'properties';

		try {
			if(!is_object($widgetInstance)) throw new WookieWidgetInstanceException('No Widget instance');
			if(!is_object($propertyInstance)) throw new WookieConnectorException('No properties instance');

			$request = '?api_key='.$this->getConnection()->getApiKey();
			$request .= '&shareddatakey='.$this->getConnection()->getSharedDataKey();
			$request .= '&userid='.$this->getUser()->getLoginName();
			$request .= '&widgetid='.$widgetInstance->getIdentifier();
			$request .= '&propertyname='.$propertyInstance->getName();

			if(!$this->checkURL($Url)) {
				throw new WookieConnectorException("Properties rest URL is incorrect: ".$Url);
			}

			$response = $this->do_request($Url.$request, false, 'DELETE');
			$statusCode = $response->getStatusCode();
			
			if($statusCode != 200 && $statusCode != 404) {
				throw new WookieConnectorException($response->headerToString().'<br />'.$response->getResponseText());
			}
			if($statusCode == 404) {
				return false;
			}
			return true;

		} catch (WookieConnectorException $e) {
			echo $e->errorMessage();
		} catch (WookieWidgetInstanceException $e) {
			echo '<b>function.getProperty:</b> '.$e->getMessage().'<br />';
		}
		return false;
	}

}
?>
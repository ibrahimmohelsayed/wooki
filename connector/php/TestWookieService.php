<%--
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
--%>
<?php

//test service
ini_set('display_errors', 1);
error_reporting(E_ALL &~ E_NOTICE);

require("WookieConnectorService.php");

$test = new WookieConnectorService("http://127.0.0.1:8080/wookie/", "TEST", "localhost_test", 'demo_2');
//setup different userName
$test->getUser()->setLoginName("demo_1");
//get all available widgets
$availableWidgets = $test->getAvailableWidgets();
//print_r($availableWidgets);

//create select menus

echo '<form action="" method="GET">';
echo '<select name="widget_id">';
echo '<option value="">No widget selected</option>';

foreach($availableWidgets as $Widget) {
	unset($sel);

	if($Widget->getIdentifier() == $_GET['widget_id']) {
		$sel = 'selected="selected"';
	}
	echo '<option value="'.$Widget->getIdentifier().'" '.$sel.'>'.$Widget->getTitle().'</option>';

}

echo '</select>';

//second select menu

echo '<select name="widget_id2">';
echo '<option value="">No widget selected</option>';

foreach($availableWidgets as $Widget) {
	unset($sel);

	if($Widget->getIdentifier() == $_GET['widget_id2']) {
		$sel = 'selected="selected"';
	}
	echo '<option value="'.$Widget->getIdentifier().'" '.$sel.'>'.$Widget->getTitle().'</option>';

}

echo '</select>';

echo '<input type="submit" value="Select" />';
echo '</form>';

if($_GET['widget_id'] != '') {
	$widget = $test->getOrCreateInstance($_GET['widget_id']);
	echo '<iframe src="'.$widget->getUrl().'" width="'.$widget->getWidth().'" height="'.$widget->getHeight().'"></iframe><br />';
}

if($_GET['widget_id2'] != '') {

	$widget2 = $test->getOrCreateInstance($_GET['widget_id2']);
	echo '<iframe src="'.$widget2->getUrl().'" width="'.$widget2->getWidth().'" height="'.$widget2->getHeight().'"></iframe><br />';
}

//call WidgetInstances->get <-- after widgets has been initialized
echo '<pre>';
print_r($test->WidgetInstances->get());


//add participant
$testUser = new User('proov1', 'proov2');


//NOTHING BELOW THIS LINE WORKS PROPERLY.
//$test->addParticipant($widget, $testUser);

//$test->getUsers($widget);
?>
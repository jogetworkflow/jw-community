<?

function runSQL($rsql) {

	$db['default']['hostname'] = "localhost";
	$db['default']['username'] = '';
	$db['default']['password'] = "";
	$db['default']['database'] = "";
	
	$db['live']['hostname'] = 'localhost';
	$db['live']['username'] = '';
	$db['live']['password'] = '';
	$db['live']['database'] = '';
	
	$active_group = 'default';
	
	$base_url = "http://".$_SERVER['HTTP_HOST'];
	$base_url .= str_replace(basename($_SERVER['SCRIPT_NAME']),"",$_SERVER['SCRIPT_NAME']);
	if (strpos($base_url,'webplicity.net')) $active_group = "live";

	$connect = mysql_connect($db[$active_group]['hostname'],$db[$active_group]['username'],$db[$active_group]['password']) or die ("Error: could not connect to database");
	$db = mysql_select_db($db[$active_group]['database']);
	
	$result = mysql_query($rsql) or die ('test');
	return $result;
	mysql_close($connect);
}

function countRec($fname,$tname) {
	$sql = "SELECT count($fname) FROM $tname ";
	$result = runSQL($sql);
	while ($row = mysql_fetch_array($result)) {
		return $row[0];
	}	
}
$page = $_POST['page'];
$rp = $_POST['rp'];
$sortname = $_POST['sortname'];
$sortorder = $_POST['sortorder'];

if (!$sortname) $sortname = 'name';
if (!$sortorder) $sortorder = 'desc';

$sort = "ORDER BY $sortname $sortorder";

if (!$page) $page = 1;
if (!$rp) $rp = 10;

$start = (($page-1) * $rp);

$limit = "LIMIT $start, $rp";

$sql = "SELECT iso,name,printable_name,iso3,numcode FROM country $sort $limit";
$result = runSQL($sql);

$total = countRec('iso','country');

header("Expires: Mon, 26 Jul 1997 05:00:00 GMT" ); 
header("Last-Modified: " . gmdate( "D, d M Y H:i:s" ) . "GMT" ); 
header("Cache-Control: no-cache, must-revalidate" ); 
header("Pragma: no-cache" );
header("Content-type: text/xml");
$xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
$xml .= "<rows>";
$xml .= "<page>$page</page>";
$xml .= "<total>$total</total>";
while ($row = mysql_fetch_array($result)) {
	$xml .= "<row id='".$row['iso']."'>";
	$xml .= "<cell><![CDATA[".$row['iso']."]]></cell>";		
	$xml .= "<cell><![CDATA[".utf8_encode($row['name'])."]]></cell>";
	//$xml .= "<cell><![CDATA[".print_r($_POST,true)."]]></cell>";				
	$xml .= "<cell><![CDATA[".utf8_encode($row['printable_name'])."]]></cell>";		
	$xml .= "<cell><![CDATA[".utf8_encode($row['iso3'])."]]></cell>";		
	$xml .= "<cell><![CDATA[".utf8_encode($row['numcode'])."]]></cell>";		
	$xml .= "</row>";		
}

$xml .= "</rows>";
echo $xml;
?>